package com.zandan.app.filestorage.service.impl;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.service.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final MinioServiceImpl minioServiceImpl;
    private final PathServiceImpl pathServiceImpl;

    @Override
    public List<ResourceDto> getResourcesFromDirectory(String path) {
        String fullPath = pathServiceImpl.getFullPath(path);
        return minioServiceImpl.getResourcesFromDirectory(fullPath);
    }

    @Override
    public DirectoryCreatedResponse createDirectory(String path) {
        String fullPath = pathServiceImpl.getFullPath(path);
        return minioServiceImpl.createDirectory(fullPath);
    }
}
