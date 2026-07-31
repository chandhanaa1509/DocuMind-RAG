package com.documind.repository;

import com.documind.model.Chunk;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChunkRepository extends MongoRepository<Chunk, String> {
    List<Chunk> findBySourceName(String sourceName);
    void deleteBySourceName(String sourceName);
}
