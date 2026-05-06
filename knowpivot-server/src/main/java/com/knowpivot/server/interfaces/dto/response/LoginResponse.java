package com.knowpivot.server.interfaces.dto.response;

import com.knowpivot.server.infrastructure.annotation.JsonLongId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;
    private Integer expiresIn;
    private UserInfoVO userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoVO {
        @JsonLongId
        private Long userId;
        private String nickname;
        private String avatarUrl;
        private Long tokenQuota;
    }
}
