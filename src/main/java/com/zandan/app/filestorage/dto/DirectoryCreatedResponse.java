package com.zandan.app.filestorage.dto;

public record DirectoryCreatedResponse(
        String path,
        String name,
        ResourceType type
) {
}
