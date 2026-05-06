package com.knowpivot.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedbackType {

    LIKE(1, "点赞"),
    DISLIKE(2, "点踩");

    private final int code;
    private final String desc;
}
