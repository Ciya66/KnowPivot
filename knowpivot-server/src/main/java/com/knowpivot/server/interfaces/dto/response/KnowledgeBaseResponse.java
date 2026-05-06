package com.knowpivot.server.interfaces.dto.response;

import com.knowpivot.server.domain.valueobject.KnowledgeConfig;
import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseResponse {

    @JsonLongId
    private Long kbId;
    private String name;
    private String description;
    private String indexName;
    private KnowledgeConfig config;
    @JsonLongId
    private Long creatorId;
    private Long docCount;
    private LocalDateTime createTime;
}
