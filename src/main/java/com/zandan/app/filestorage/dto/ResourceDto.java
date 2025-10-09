package com.zandan.app.filestorage.dto;

public record ResourceDto(
        String path,
        String name,
        Long size,
        ResourceType type
) {
}
