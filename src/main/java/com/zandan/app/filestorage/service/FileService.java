package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.exception.QueryIsNotValidException;
import com.zandan.app.filestorage.security.MyUserDetails;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioService minioService;
    private final PathService pathService;

    public List<ResourceDto> uploadFiles(String path, List<MultipartFile> files) {
        String fullPath = pathService.getFullPath(path);

        Map<String, MultipartFile> fileMap = new HashMap<>();
        for (MultipartFile file : files) {
            fileMap.put(fullPath + "/" + file.getOriginalFilename(), file);
        }

        return minioService.saveResources(fileMap);
    }

    public ResourceDto getFile(String path) {
        String fullPath = pathService.getFullPath(path);
        return minioService.getFile(fullPath);
    }

    public void deleteFile(String path) {
        String fullPath = pathService.getFullPath(path);
        minioService.deleteResource(fullPath);
    }

    public void downloadFile(String path, OutputStream outputStream) {
        String fullPath = pathService.getFullPath(path);
        minioService.writeFile(fullPath, outputStream);
    }

    public ResourceDto moveFile(String fromPath, String toPath) {
        String fullFromPath = pathService.getFullPath(fromPath);
        String fullToPath = pathService.getFullPath(toPath);
        return minioService.moveFile(fullFromPath, fullToPath);
    }

    public List<ResourceDto> searchFiles(String query) {
        if (query.isBlank() || query.isEmpty()) {
            throw new QueryIsNotValidException("Query is blank or null");
        }
        String fullQuery = pathService.getFullPath(query);
        return minioService.searchFiles(fullQuery);
    }
}
