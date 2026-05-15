package com.knowpivot.server.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成对话标题请求模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTitleRequest {

    private String userMessage;
}
