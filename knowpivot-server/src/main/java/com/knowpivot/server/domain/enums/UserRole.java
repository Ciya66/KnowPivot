package com.knowpivot.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    NORMAL(0, "普通用户"),
    ADMIN(1, "管理员");

    private final int code;
    private final String desc;
}
