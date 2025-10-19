package com.zandan.app.filestorage.service.impl;

import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.event.FileOperationEvent;
import com.zandan.app.filestorage.event.OperationType;
import com.zandan.app.filestorage.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishFileOperationEvent(ResourceDto resource, OperationType type) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        FileOperationEvent fileOperationEvent = new FileOperationEvent(resource.path(), resource.name(),
                resource.size(), resource.type(), username, type, username+"@gmail.com");

        log.info("FileOperated event: {}", fileOperationEvent);
        kafkaTemplate.send("file-operated-topic", fileOperationEvent);
        log.info("Send file operated event: {}", fileOperationEvent);
    }
}
