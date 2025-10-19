package com.zandan.app.filestorage.controller;

import com.zandan.app.filestorage.dto.ResourceDto;
import com.zandan.app.filestorage.service.impl.ResourceServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceServiceImpl resourceServiceImpl;

    @Operation(
            summary = "Загрузка ресурсов в облако",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл загружен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "409", description = "Файл уже сущетсвует")}
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceDto>> uploadResources(@RequestParam String path,
                                                    @RequestPart("object") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceServiceImpl.uploadResources(path, files));
    }

    @Operation(
            summary = "Показать ресурс",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ресурс найден"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")}
    )
    @GetMapping()
    public ResponseEntity<ResourceDto> showResource(@RequestParam String path) {
        return ResponseEntity.ok().body(resourceServiceImpl.getResource(path));
    }

    @Operation(
            summary = "Удаление ресурса",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Ресурс успешно удален"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @DeleteMapping()
    public ResponseEntity<Void> deleteResource(@RequestParam String path) {
        log.info("ResourceController deleteResource: {}", path);
        resourceServiceImpl.deleteResource(path);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Скачать ресурс",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ресурс успешно скачан"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
            }
    )
    @SneakyThrows
    @GetMapping("/download")
    public void downloadResource(@RequestParam String path, HttpServletResponse response) {
        ResourceDto resource = resourceServiceImpl.getResource(path);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + resource.name() + "\"");
        resourceServiceImpl.downloadFile(path, response.getOutputStream());
    }

    @Operation(
            summary = "Переименовать или переместить ресурс",
            description = "Метод принимает 2 параметра: старый путь и новый путь",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Операция прошла успешно"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден"),
                    @ApiResponse(responseCode = "409", description = "Ресурс лежащий по этому пути уже существует")
            }
    )
    @GetMapping("/move")
    public ResponseEntity<ResourceDto> moveResource(@RequestParam("from") String fromPath,
                                                    @RequestParam("to") String toPath) {
        return ResponseEntity.ok().body(resourceServiceImpl.moveResource(fromPath, toPath));
    }

    @Operation(
            summary = "Поиск ресурсов",
            description = "Метод ищет рекурсивно ресурсы начинающиеся на ключевое слово",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Найденные ресурсы"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий поисковый запрос"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<List<ResourceDto>> searchResources(@RequestParam String query) {
        return ResponseEntity.ok().body(resourceServiceImpl.searchResources(query));
    }
}
