package com.knowpivot.server.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档切片实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSegment {

    private Long id;
    private Long docId;
    private Long kbId;
    private String vectorId;
    private String content;
    private Integer pageNum;
    private LocalDateTime createdAt;
}
