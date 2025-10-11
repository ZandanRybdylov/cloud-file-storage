package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.ResourceDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

public interface ResourceService {

    List<ResourceDto> uploadResources(String path, List<MultipartFile> files);

    ResourceDto getResource(String path);

    void deleteResource(String path);

    void downloadFile(String path, OutputStream outputStream);

    ResourceDto moveResource(String fromPath, String toPath);

    List<ResourceDto> searchResources(String query);
}
