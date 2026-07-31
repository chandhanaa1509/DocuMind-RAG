package com.documind.service;

import com.documind.dto.SourceDto;
import com.documind.model.Chunk;
import com.documind.repository.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The "R" in RAG.
 *
 * Chunks incoming documents and, at query time, scores every stored chunk
 * against the question using cosine similarity over term-frequency vectors.
 * This keeps the whole pipeline self-contained (Groq for generation only) -
 * no separate embedding API or vector DB is required for a project this size.
 */
@Service
public class RetrievalService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-zA-Z0-9]+");
    private static final Set<String> STOPWORDS = Set.of(
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
            "to", "of", "in", "on", "for", "and", "or", "but", "with", "as",
            "at", "by", "from", "that", "this", "it", "its", "into", "than",
            "then", "so", "such", "if", "not", "no", "do", "does", "did",
            "can", "could", "will", "would", "should", "may", "might", "have",
            "has", "had", "i", "you", "he", "she", "they", "we", "what",
            "which", "who", "whom", "how", "why", "when", "where"
    );

    private final ChunkRepository chunkRepository;

    public RetrievalService(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    /** Splits raw text into overlapping word-based chunks. */
    public List<String> chunk(String text, int wordsPerChunk, int overlapWords) {
        String[] words = text.trim().split("\\s+");
        List<String> chunks = new ArrayList<>();
        int i = 0;
        while (i < words.length) {
            int end = Math.min(i + wordsPerChunk, words.length);
            chunks.add(String.join(" ", Arrays.asList(words).subList(i, end)));
            if (end == words.length) break;
            i += (wordsPerChunk - overlapWords);
        }
        return chunks;
    }

    /** Returns the top-K most relevant chunks for a question, across all stored documents. */
    public List<ScoredChunk> retrieve(String question, int topK) {
        List<Chunk> all = chunkRepository.findAll();
        Map<String, Integer> queryTf = termFrequencies(question);
        if (queryTf.isEmpty() || all.isEmpty()) return List.of();

        List<ScoredChunk> scored = all.stream()
                .map(c -> new ScoredChunk(c, cosineSimilarity(queryTf, termFrequencies(c.getText()))))
                .filter(sc -> sc.score() > 0.0)
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(topK)
                .collect(Collectors.toList());
        return scored;
    }

    private Map<String, Integer> termFrequencies(String text) {
        Map<String, Integer> freq = new HashMap<>();
        var matcher = TOKEN_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) {
            String token = matcher.group();
            if (STOPWORDS.contains(token) || token.length() < 2) continue;
            freq.merge(token, 1, Integer::sum);
        }
        return freq;
    }

    private double cosineSimilarity(Map<String, Integer> a, Map<String, Integer> b) {
        Set<String> vocab = new HashSet<>(a.keySet());
        vocab.retainAll(b.keySet());
        if (vocab.isEmpty()) return 0.0;

        double dot = 0, normA = 0, normB = 0;
        for (String term : vocab) dot += a.get(term) * b.get(term);
        for (int v : a.values()) normA += (double) v * v;
        for (int v : b.values()) normB += (double) v * v;
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public record ScoredChunk(Chunk chunk, double score) {
        public SourceDto toDto() {
            String snippet = chunk.getText().length() > 220
                    ? chunk.getText().substring(0, 220) + "…"
                    : chunk.getText();
            return new SourceDto(chunk.getSourceName(), chunk.getChunkIndex(), snippet, score);
        }
    }
}
