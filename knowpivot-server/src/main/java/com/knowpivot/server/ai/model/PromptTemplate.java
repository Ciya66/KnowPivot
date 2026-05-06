package com.knowpivot.server.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt 模板，支持占位符替换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {

    private String systemPrompt;
    private String userPromptTemplate;

    /**
     * 渲染 user prompt，替换 {key} 占位符
     */
    public String renderUserPrompt(Map<String, Object> context) {
        if (userPromptTemplate == null) return "";
        String result = userPromptTemplate;
        Pattern pattern = Pattern.compile("\\{(\\w+)}");
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = context.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
