package com.zandan.app.filestorage.service.impl;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.dto.ResourceType;
import com.zandan.app.filestorage.event.OperationType;
import com.zandan.app.filestorage.exception.ResourceAlreadyExistsException;
import com.zandan.app.filestorage.exception.ResourceNotFoundException;
import com.zandan.app.filestorage.service.MinioService;
import com.zandan.app.filestorage.service.PathService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final KafkaServiceImpl kafkaServiceImpl;
    private final PathService pathServiceImpl;

    @Value("${minio.bucket}")
    private String bucketName;

    @PostConstruct
    @SneakyThrows
    @Override
    public void createBucket() {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows
    @Override
    public List<ResourceDto> saveResources(Map<String, MultipartFile> fileMap) {
        List<ResourceDto> resources = new ArrayList<>();

        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            String path = entry.getKey();
            MultipartFile file = entry.getValue();

            try {
                if (this.getResource(path) != null) {
                    throw new ResourceAlreadyExistsException("Resource by path %s already exists".formatted(path));
                }
            } catch (ResourceNotFoundException e) {}

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(path)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }
            ResourceDto resource = new ResourceDto(pathServiceImpl.extractPathWithoutFileName(path, extractFileName(path)),
                    extractFileName(path),
                    Long.valueOf(file.getSize()), ResourceType.FILE);
            resources.add(resource);

            kafkaServiceImpl.publishFileOperationEvent(resource, OperationType.UPLOAD);
        }

        return resources;
    }

    @Override
    @SneakyThrows
    public ResourceDto getResource(String path) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );

            ResourceType type = path.endsWith("/") ? ResourceType.DIRECTORY : ResourceType.FILE;
            long size = type == ResourceType.FILE ? stat.size() : 0L;

            return new ResourceDto(pathServiceImpl.extractPathWithoutFileName(path, extractFileName(path)), extractFileName(path), size, type);

        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ResourceNotFoundException("Resource not found: " + path);
            }
            throw e;
        }
    }



    @SneakyThrows
    @Override
    public void deleteFile(String path) {
        try {
            ResourceDto resource = getResource(path);

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());

            kafkaServiceImpl.publishFileOperationEvent(resource, OperationType.DELETED);
        } catch (ErrorResponseException e) {
                if("NoSuchKey".equals(e.errorResponse().code())) {
                    throw new ResourceNotFoundException("File not exist or path %s is incorrect"
                            .formatted(path));
                }
                throw e;
        }
    }

    private String extractFileName(String path) {
        if (path.endsWith("/")) {
            int index = path.substring(0,path.length()-1).lastIndexOf("/");
            return path.substring(index+1);
        }
        int index = path.lastIndexOf("/");
        return index>0 ? path.substring(index + 1) : path;
    }

    @SneakyThrows
    @Override
    public void writeFile(String path, OutputStream outputStream) {
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(path).build())) {
            is.transferTo(outputStream);

            kafkaServiceImpl.publishFileOperationEvent(getResource(path), OperationType.DOWNLOAD);
        } catch (ErrorResponseException e) {
            if("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ResourceNotFoundException("File not exist or path %s is incorrect"
                        .formatted(path));
            }
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @SneakyThrows
    @Override
    public ResourceDto moveFile(String fromPath, String toPath) {
        try {
            if (this.getResource(toPath).type().equals(ResourceType.FILE)) {
                throw new ResourceAlreadyExistsException("File by path %s already exists".formatted(toPath));
            }
        } catch (ResourceNotFoundException e) {}

        try {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucketName)
                        .object(toPath)
                        .source(CopySource.builder()
                                .bucket(bucketName)
                                .object(fromPath)
                                .build())
                        .build());
            this.deleteFile(fromPath);
        } catch (ErrorResponseException e) {
            if("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ResourceNotFoundException("File not exist or path %s is incorrect"
                        .formatted(fromPath));
            }
            throw e;
        }

        ResourceDto resource = new ResourceDto(toPath, extractFileName(toPath), Long.valueOf(111), ResourceType.FILE);
        kafkaServiceImpl.publishFileOperationEvent(resource, OperationType.RENAMED_OR_REPLACED);
        return resource;
    }

    @SneakyThrows
    @Override
    public List<ResourceDto> searchResources(String query) {
        List<ResourceDto> resources = new ArrayList<>();

        int lastHashIndex = query.lastIndexOf("/");
        String prefix = "", suffix = "";
        if (lastHashIndex > 0) {
            prefix = query.substring(0, lastHashIndex+1);
            suffix = query.substring(lastHashIndex+1);
        }

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            String fileName = extractFileName(item.objectName());
            if(fileName.toLowerCase().startsWith(suffix.toLowerCase())) {
                resources.add(new ResourceDto(fileName, item.objectName(), item.size(), ResourceType.FILE));
            }
        }

        return resources;
    }

    @SneakyThrows
    @Override
    public List<ResourceDto> getResourcesFromDirectory(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }

        List<ResourceDto> resources = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(false)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();

            if (item.objectName().equals(path)) continue;

            resources.add(new ResourceDto(
                    pathServiceImpl.extractPathWithoutFileName(item.objectName(), extractFileName(item.objectName())),
                    extractFileName(item.objectName()),
                    item.isDir() ? 0L : item.size(),
                    item.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE
            ));
        }

        return resources;
    }


    //TODO: обработать исключения
    @Override
    @SneakyThrows
    public DirectoryCreatedResponse createDirectory(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }

        try {
            if (getResource(path).type() == ResourceType.DIRECTORY) {
                throw new ResourceAlreadyExistsException("Directory by path %s already exists".formatted(path));
            }
        } catch (ResourceNotFoundException e) {}

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(InputStream.nullInputStream(), 0, -1)
                        .build()
        );

        return new DirectoryCreatedResponse(path, extractFileName(path), ResourceType.DIRECTORY);
    }


    //TODO: обработать исключения
    @SneakyThrows
    @Override
    public ResourceDto moveFolder(String fromFolderPath, String toFolderPath) {
        if (isFolderExists(toFolderPath)) {
            throw new ResourceAlreadyExistsException("Target folder already exists: " + toFolderPath);
        }

        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(fromFolderPath)
                        .recursive(true)
                        .build());

        for (Result<Item> result : objects) {
            String oldObjectName = result.get().objectName();
            String newObjectName = oldObjectName.replace(fromFolderPath, toFolderPath);

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(newObjectName)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(oldObjectName)
                            .build())
                    .build());
        }

        deleteFolder(fromFolderPath);
        return new ResourceDto(toFolderPath, extractFileName(toFolderPath), Long.valueOf(0), ResourceType.DIRECTORY);
    }

    private boolean isFolderExists(String folderPath) {
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(folderPath)
                            .maxKeys(1)
                            .build());
            return objects.iterator().hasNext();
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    @Override
    public void deleteFolder(String folderPath) {
        if (!folderPath.endsWith("/")) folderPath += "/";

        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(folderPath)
                        .recursive(true)
                        .build());

        for (Result<Item> result : objects) {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(result.get().objectName())
                            .build());
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(folderPath)
                            .build());
        } catch (ErrorResponseException ignored) {}
    }
}
