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
public class UserInfoResponse {

    @JsonLongId
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Long tokenQuota;
    private Integer role;
}
