package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.event.OperationType;

public interface KafkaService {

    void publishFileOperationEvent(ResourceDto resource, OperationType type);
}
