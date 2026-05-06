package com.knowpivot.server.domain.entity;

import com.knowpivot.server.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Token 流水记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenTransaction {

    private Long id;
    private Long userId;
    private Long amount;
    private Long balanceAfter;
    private TransactionType type;
    private String relatedId;
    private String remark;
    private LocalDateTime createdAt;
}
