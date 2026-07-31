package com.documind.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadTextRequest {
    @NotBlank
    private String sourceName;
    @NotBlank
    private String content;
}
