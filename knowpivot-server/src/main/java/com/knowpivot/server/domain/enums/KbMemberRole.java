package com.knowpivot.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KbMemberRole {

    VIEWER(0, "查看者"),
    EDITOR(1, "编辑者"),
    ADMIN(2, "管理员");

    private final int code;
    private final String desc;
}
