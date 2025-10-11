package com.zandan.app.filestorage.controller;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.service.impl.DirectoryServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryServiceImpl directoryServiceImpl;

    @GetMapping()
    public ResponseEntity<List<ResourceDto>> getResourcesFromDirectory(@RequestParam String path) {
        return ResponseEntity.ok().body(directoryServiceImpl.getResourcesFromDirectory(path));
    }

    @PostMapping()
    public ResponseEntity<DirectoryCreatedResponse> createDirectory(@RequestParam String path) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directoryServiceImpl.createDirectory(path));
    }
}
