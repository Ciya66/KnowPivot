package com.knowpivot.server.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {

    RECHARGE(1, "充值"),
    CONSUMPTION(2, "对话消耗"),
    REFUND(3, "失败回退");

    private final int code;
    private final String desc;
}
