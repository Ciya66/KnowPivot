package com.knowpivot.server.interfaces.api;

import com.knowpivot.server.application.service.AuthApplicationService;
import com.knowpivot.server.infrastructure.common.Result;
import com.knowpivot.server.interfaces.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息接口
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthApplicationService authApplicationService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        UserInfoResponse response = authApplicationService.getCurrentUserInfo();
        return Result.ok(response);
    }
}
