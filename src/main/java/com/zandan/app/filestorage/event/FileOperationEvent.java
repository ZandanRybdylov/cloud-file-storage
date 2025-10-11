package com.zandan.app.filestorage.event;

import com.zandan.app.filestorage.dto.ResourceType;

public record FileOperationEvent(
        String path,
        String name,
        Long size,
        ResourceType type,
        String username,
        OperationType operationType
) {}
