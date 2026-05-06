package com.knowpivot.server.ai.strategy;

/**
 * AI 模型选择策略接口
 */
public interface ModelStrategy {

    /**
     * 获取当前生效的模型代码
     */
    String getModelCode();

    /**
     * 获取策略优先级
     */
    int getPriority();
}
