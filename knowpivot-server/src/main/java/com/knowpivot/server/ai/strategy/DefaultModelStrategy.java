package com.knowpivot.server.ai.strategy;

import org.springframework.stereotype.Component;

/**
 * 默认模型策略 - Qwen
 */
@Component
public class DefaultModelStrategy implements ModelStrategy {

    @Override
    public String getModelCode() {
        return "deepseek-4pro";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
