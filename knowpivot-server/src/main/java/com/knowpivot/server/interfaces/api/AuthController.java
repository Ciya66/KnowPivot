package com.knowpivot.server.interfaces.api;

import com.knowpivot.server.application.service.AuthApplicationService;
import com.knowpivot.server.infrastructure.common.Result;
import com.knowpivot.server.interfaces.dto.request.LoginRequest;
import com.knowpivot.server.interfaces.dto.request.RegisterRequest;
import com.knowpivot.server.interfaces.dto.response.LoginResponse;
import com.knowpivot.server.interfaces.dto.response.RegisterResponse;
import com.knowpivot.server.interfaces.dto.response.UserInfoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证接口
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authApplicationService.register(request);
        return Result.ok("注册成功", new RegisterResponse(userId));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authApplicationService.login(request);
        return Result.ok("登录成功", response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        UserInfoResponse response = authApplicationService.getCurrentUserInfo();
        return Result.ok(response);
    }
}
