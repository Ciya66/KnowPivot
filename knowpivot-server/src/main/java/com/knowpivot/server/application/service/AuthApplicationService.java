package com.knowpivot.server.application.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.knowpivot.server.domain.entity.User;
import com.knowpivot.server.domain.enums.UserRole;
import com.knowpivot.server.domain.enums.UserStatus;
import com.knowpivot.server.domain.repository.UserRepository;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import com.knowpivot.server.infrastructure.common.ResultCode;
import com.knowpivot.server.infrastructure.util.IdGenerator;
import com.knowpivot.server.interfaces.dto.request.LoginRequest;
import com.knowpivot.server.interfaces.dto.request.RegisterRequest;
import com.knowpivot.server.interfaces.dto.response.LoginResponse;
import com.knowpivot.server.interfaces.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证用例服务
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final IdGenerator idGenerator;

    @Value("${knowpivot.default-token-quota:10000}")
    private long defaultTokenQuota;

    /**
     * 用户注册
     */
    @Transactional
    public Long register(RegisterRequest request) {
        // 检查用户名唯一
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "用户名已存在");
        }

        User user = User.builder()
                .id(idGenerator.nextId())
                .username(request.getUsername())
                .passwordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .tokenQuota(defaultTokenQuota)
                .role(UserRole.NORMAL)
                .status(UserStatus.ACTIVE)
                .isDeleted(0)
                .build();

        userRepository.save(user);
        return user.getId();
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        if (!user.isActive()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
        }

        // Sa-Token 登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(7200)
                .userInfo(LoginResponse.UserInfoVO.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .tokenQuota(user.getTokenQuota())
                        .build())
                .build();
    }

    /**
     * 获取当前登录用户信息
     */
    public UserInfoResponse getCurrentUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .tokenQuota(user.getTokenQuota())
                .role(user.getRole().getCode())
                .build();
    }
}
