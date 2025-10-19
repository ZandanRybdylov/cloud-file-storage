package com.zandan.app.filestorage.event;

import com.zandan.app.filestorage.dto.ResourceType;

public record FileOperationEvent(
        String filePath,
        String fileName,
        Long size,
        ResourceType type,
        String username,
        OperationType operationType,
        String userMail
) {}
