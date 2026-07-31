package com.documind.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A single chunk of text extracted from an uploaded document.
 * Chunks are the retrieval unit for the RAG pipeline.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chunks")
public class Chunk {
    @Id
    private String id;
    private String sourceName;
    private int chunkIndex;
    private String text;
    private long createdAt;
}
