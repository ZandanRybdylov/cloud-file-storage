package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface MinioService {

    void createBucket();

    List<ResourceDto> saveResources(Map<String, MultipartFile> fileMap);

    ResourceDto getResource(String path);

    void deleteFile(String path);

    void writeFile(String path, OutputStream outputStream);

    ResourceDto moveFile(String fromPath, String toPath);

    List<ResourceDto> searchResources(String query);

    List<ResourceDto> getResourcesFromDirectory(String path);

    DirectoryCreatedResponse createDirectory(String path);

    ResourceDto moveFolder(String fromFolderPath, String toFolderPath);

    void deleteFolder(String folderPath);
}
