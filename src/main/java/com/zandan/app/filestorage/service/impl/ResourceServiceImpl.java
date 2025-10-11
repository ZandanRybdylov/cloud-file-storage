package com.zandan.app.filestorage.service.impl;

import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.exception.QueryIsNotValidException;
import com.zandan.app.filestorage.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final MinioServiceImpl minioServiceImpl;
    private final PathServiceImpl pathServiceImpl;

    @Override
    public List<ResourceDto> uploadResources(String path, List<MultipartFile> files) {
        String fullPath = pathServiceImpl.getFullPath(path);

        Map<String, MultipartFile> fileMap = new HashMap<>();
        for (MultipartFile file : files) {
            fileMap.put(fullPath + file.getOriginalFilename(), file);
        }

        return minioServiceImpl.saveResources(fileMap);
    }

    @Override
    public ResourceDto getResource(String path) {
        String fullPath = pathServiceImpl.getFullPath(path);
        return minioServiceImpl.getResource(fullPath);
    }

    @Override
    public void deleteResource(String path) {
        String fullPath = pathServiceImpl.getFullPath(path);
        if (fullPath.endsWith("/")) {
            minioServiceImpl.deleteFolder(fullPath);
        }
        minioServiceImpl.deleteFile(fullPath);
    }

    @Override
    public void downloadFile(String path, OutputStream outputStream) {
        String fullPath = pathServiceImpl.getFullPath(path);
        minioServiceImpl.writeFile(fullPath, outputStream);
    }

    @Override
    public ResourceDto moveResource(String fromPath, String toPath) {
        String fullFromPath = pathServiceImpl.getFullPath(fromPath);
        String fullToPath = pathServiceImpl.getFullPath(toPath);
        if (fullToPath.endsWith("/")) {
            return minioServiceImpl.moveFolder(fullFromPath, fullToPath);
        }
        return minioServiceImpl.moveFile(fullFromPath, fullToPath);
    }

    @Override
    public List<ResourceDto> searchResources(String query) {
        if (query.isBlank() || query.isEmpty()) {
            throw new QueryIsNotValidException("Query is blank or null");
        }
        String fullQuery = pathServiceImpl.getFullPath(query);
        return minioServiceImpl.searchResources(fullQuery);
    }
}
