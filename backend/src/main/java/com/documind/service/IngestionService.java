package com.documind.service;

import com.documind.model.Chunk;
import com.documind.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    private static final int WORDS_PER_CHUNK = 120;
    private static final int OVERLAP_WORDS = 20;

    private final ChunkRepository chunkRepository;
    private final RetrievalService retrievalService;

    public IngestionService(ChunkRepository chunkRepository, RetrievalService retrievalService) {
        this.chunkRepository = chunkRepository;
        this.retrievalService = retrievalService;
    }

    public int ingest(String sourceName, String content) {
        chunkRepository.deleteBySourceName(sourceName); // re-uploads replace the old version
        List<String> pieces = retrievalService.chunk(content, WORDS_PER_CHUNK, OVERLAP_WORDS);

        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            chunks.add(new Chunk(null, sourceName, i, pieces.get(i), System.currentTimeMillis()));
        }
        chunkRepository.saveAll(chunks);
        return chunks.size();
    }
}
