package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.dto.ResourceType;
import com.zandan.app.filestorage.exception.QueryIsNotValidException;
import com.zandan.app.filestorage.exception.ResourceAlreadyExistsException;
import com.zandan.app.filestorage.exception.ResourceNotFoundException;
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
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @PostConstruct
    @SneakyThrows
    public void createBucket() {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows
    public List<ResourceDto> saveResources(Map<String, MultipartFile> fileMap) {
        List<ResourceDto> resources = new ArrayList<>();

        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            String path = entry.getKey();
            MultipartFile file = entry.getValue();

            try {
                if (this.getFile(path) != null) {
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
            ResourceDto resource = new ResourceDto(path, extractFileName(path),
                    Long.valueOf(file.getSize()), ResourceType.FILE);
            resources.add(resource);
        }

        return resources;
    }

    @SneakyThrows
    public ResourceDto getFile(String path) {
        StatObjectResponse stat = null;
        try {
            stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ResourceNotFoundException("File not exist or path %s is incorrect"
                        .formatted(path));
            }
            throw e;
        }

        return new ResourceDto(path, extractFileName(path), stat.size(), ResourceType.FILE);
    }

    @SneakyThrows
    public void deleteResource(String path) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
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
    public void writeFile(String path, OutputStream outputStream) {
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(path).build())) {
            is.transferTo(outputStream);
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
    public ResourceDto moveFile(String fromPath, String toPath) {
        try {
            if (this.getFile(toPath) != null) {
                throw new ResourceAlreadyExistsException("Resource by path %s already exists".formatted(toPath));
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
            this.deleteResource(fromPath);
        } catch (ErrorResponseException e) {
            if("NoSuchKey".equals(e.errorResponse().code())) {
                throw new ResourceNotFoundException("File not exist or path %s is incorrect"
                        .formatted(fromPath));
            }
            throw e;
        }
        return new ResourceDto(toPath, extractFileName(toPath), Long.valueOf(111), ResourceType.FILE);
    }

    @SneakyThrows
    public List<ResourceDto> searchFiles(String query) {
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
    public List<ResourceDto> getResourcesFromDirectory(String path) {
        List<ResourceDto> resources = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(false)
                            .build());

        for (Result<Item> result : results) {
            Item item = result.get();
            resources.add(new ResourceDto(
                    path,
                    extractFileName(item.objectName()),
                    item.isDir() ? 0 : item.size(),
                    item.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE
            ));
        }

        if (resources.size() == 0) {
            throw new ResourceNotFoundException("Directory not found");
        }
        return resources;
    }

    //TODO: обработать исключения
    @SneakyThrows
    public DirectoryCreatedResponse createDirectory(String path) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(InputStream.nullInputStream(), 0, -1)
                        .contentType("application/x-directory")
                        .build()
        );

        return new DirectoryCreatedResponse(path, extractFileName(path), ResourceType.DIRECTORY);
    }
}
