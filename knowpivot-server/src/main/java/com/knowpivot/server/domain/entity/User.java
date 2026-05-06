package com.knowpivot.server.domain.entity;

import com.knowpivot.server.domain.enums.UserRole;
import com.knowpivot.server.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private Long tokenQuota;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    /**
     * 检查用户是否可用
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE && (this.isDeleted == null || this.isDeleted == 0);
    }

    /**
     * 检查 Token 配额是否充足
     */
    public boolean hasEnoughToken(long required) {
        return this.tokenQuota != null && this.tokenQuota >= required;
    }

    /**
     * 扣减 Token
     */
    public void deductToken(long amount) {
        if (!hasEnoughToken(amount)) {
            throw new IllegalStateException("Token 配额不足");
        }
        this.tokenQuota -= amount;
    }

    /**
     * 充值 Token
     */
    public void rechargeToken(long amount) {
        this.tokenQuota = (this.tokenQuota == null ? 0 : this.tokenQuota) + amount;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
