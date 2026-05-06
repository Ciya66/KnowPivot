package com.knowpivot.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentStatus {

    UPLOADED(0, "已上传"),
    PARSING(1, "解析中"),
    INDEXED(2, "已索引"),
    FAILED(3, "失败");

    private final int code;
    private final String desc;
}
