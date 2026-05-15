package com.knowpivot.server.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成对话标题响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTitleResponse {

    private String title;
    private boolean success;
    private String errorMessage;
}
