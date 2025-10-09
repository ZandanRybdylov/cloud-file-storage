package com.zandan.app.filestorage.controller;

import com.zandan.app.filestorage.dto.DirectoryCreatedResponse;
import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.service.DirectoryService;
import com.zandan.app.filestorage.service.PathService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    @GetMapping()
    public ResponseEntity<List<ResourceDto>> getResourcesFromDirectory(@RequestParam String path) {
        return ResponseEntity.ok().body(directoryService.getResourcesFromDirectory(path));
    }

    @PostMapping()
    public ResponseEntity<DirectoryCreatedResponse> createDirectory(@RequestParam String path) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directoryService.createDirectory(path));
    }
}
