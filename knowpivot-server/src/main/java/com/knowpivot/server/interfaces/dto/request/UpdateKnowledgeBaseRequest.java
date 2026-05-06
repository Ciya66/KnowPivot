package com.knowpivot.server.interfaces.dto.request;

import com.knowpivot.server.domain.valueobject.KnowledgeConfig;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateKnowledgeBaseRequest {

    @Size(max = 128, message = "名称不能超过 128 字符")
    private String name;

    @Size(max = 512, message = "描述不能超过 512 字符")
    private String description;

    private KnowledgeConfig config;
}
