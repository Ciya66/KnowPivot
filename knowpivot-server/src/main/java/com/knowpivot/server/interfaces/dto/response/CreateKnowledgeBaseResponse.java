package com.knowpivot.server.interfaces.dto.response;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateKnowledgeBaseResponse {

    @JsonLongId
    private Long kbId;
    private String indexName;
}
