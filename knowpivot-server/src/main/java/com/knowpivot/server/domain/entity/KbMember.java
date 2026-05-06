package com.knowpivot.server.domain.entity;

import com.knowpivot.server.domain.enums.KbMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库成员实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbMember {

    private Long id;
    private Long kbId;
    private Long userId;
    private KbMemberRole role;
    private LocalDateTime createdAt;
}
