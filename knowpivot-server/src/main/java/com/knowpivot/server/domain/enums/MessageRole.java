package com.knowpivot.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageRole {

    USER(0, "用户"),
    ASSISTANT(1, "助手"),
    SYSTEM(2, "系统");

    private final int code;
    private final String desc;
}
