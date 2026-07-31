package com.documind.controller;

import com.documind.dto.UploadTextRequest;
import com.documind.repository.ChunkRepository;
import com.documind.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final IngestionService ingestionService;
    private final ChunkRepository chunkRepository;

    public DocumentController(IngestionService ingestionService, ChunkRepository chunkRepository) {
        this.ingestionService = ingestionService;
        this.chunkRepository = chunkRepository;
    }

    /** Upload a .txt/.md file. */
    @PostMapping(value = "/upload-file", consumes = "multipart/form-data")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "untitled.txt";
        int count = ingestionService.ingest(name, content);
        return Map.of("sourceName", name, "chunks", count);
    }

    /** Upload raw pasted text as a "document". */
    @PostMapping("/upload-text")
    public Map<String, Object> uploadText(@Valid @RequestBody UploadTextRequest request) {
        int count = ingestionService.ingest(request.getSourceName(), request.getContent());
        return Map.of("sourceName", request.getSourceName(), "chunks", count);
    }

    /** List distinct uploaded document names with chunk counts. */
    @GetMapping
    public List<Map<String, Object>> listDocuments() {
        return chunkRepository.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getSourceName(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> Map.<String, Object>of("sourceName", e.getKey(), "chunks", e.getValue()))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{sourceName}")
    public void delete(@PathVariable String sourceName) {
        chunkRepository.deleteBySourceName(sourceName);
    }
}
