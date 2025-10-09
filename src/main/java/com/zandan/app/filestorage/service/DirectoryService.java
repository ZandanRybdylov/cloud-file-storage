package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final MinioService minioService;
    private final PathService pathService;

    public List<ResourceDto> getResourcesFromDirectory(String path) {
        String fullPath = pathService.getFullPath(path);
        minioService.getResourcesFromDirectory(fullPath);
        return minioService.getResourcesFromDirectory(fullPath);
    }

    public DirectoryCreatedResponse createDirectory(String path) {
        String fullPath = pathService.getFullPath(path);
        return minioService.createDirectory(fullPath);
    }
}
