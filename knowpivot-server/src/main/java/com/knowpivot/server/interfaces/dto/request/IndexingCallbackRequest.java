package com.knowpivot.server.interfaces.dto.request;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class IndexingCallbackRequest {

    @JsonLongId
    @NotNull(message = "文档 ID 不能为空")
    private Long docId;

    @JsonLongId
    @NotNull(message = "知识库 ID 不能为空")
    private Long kbId;

    @NotBlank(message = "索引名不能为空")
    private String indexName;

    @NotEmpty(message = "切片列表不能为空")
    @Valid
    private List<ChunkData> chunks;

    @Data
    public static class ChunkData {

        @NotBlank(message = "切片内容不能为空")
        private String content;

        @NotNull(message = "Embedding 向量不能为空")
        private List<Float> embedding;

        private Integer pageNum;
    }
}
