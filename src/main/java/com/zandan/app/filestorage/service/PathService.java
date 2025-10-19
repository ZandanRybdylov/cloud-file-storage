package com.zandan.app.filestorage.service;

public interface PathService {

    String getFullPath(String path);

    String extractPathWithoutFileName(String path, String fileName);
}
