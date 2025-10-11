package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;

import java.util.List;

public interface DirectoryService {

    List<ResourceDto> getResourcesFromDirectory(String path);

    DirectoryCreatedResponse createDirectory(String path);
}
