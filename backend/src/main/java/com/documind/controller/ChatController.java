package com.documind.controller;

import com.documind.dto.ChatRequest;
import com.documind.dto.ChatResponse;
import com.documind.dto.SourceDto;
import com.documind.service.GroqService;
import com.documind.service.RetrievalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final int TOP_K = 4;

    private final RetrievalService retrievalService;
    private final GroqService groqService;

    public ChatController(RetrievalService retrievalService, GroqService groqService) {
        this.retrievalService = retrievalService;
        this.groqService = groqService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        List<RetrievalService.ScoredChunk> retrieved = retrievalService.retrieve(request.getQuestion(), TOP_K);

        if (retrieved.isEmpty()) {
            return new ChatResponse(
                    "I don't have any relevant document content to answer that yet. Upload a document first, then ask again.",
                    List.of());
        }

        String context = IntStream.range(0, retrieved.size())
                .mapToObj(i -> "[" + (i + 1) + "] " + retrieved.get(i).chunk().getText())
                .collect(Collectors.joining("\n\n"));

        String answer = groqService.generateAnswer(request.getQuestion(), context);

        List<SourceDto> sources = retrieved.stream()
                .map(RetrievalService.ScoredChunk::toDto)
                .collect(Collectors.toList());

        return new ChatResponse(answer, sources);
    }
}
