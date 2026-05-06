package com.knowpivot.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {

    DISABLED(0, "禁用"),
    ACTIVE(1, "正常");

    private final int code;
    private final String desc;
}
