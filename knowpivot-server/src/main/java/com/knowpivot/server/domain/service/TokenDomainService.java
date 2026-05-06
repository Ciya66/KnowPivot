package com.knowpivot.server.domain.service;

import com.knowpivot.server.domain.entity.User;
import com.knowpivot.server.domain.entity.TokenTransaction;
import com.knowpivot.server.domain.enums.TransactionType;
import com.knowpivot.server.domain.repository.UserRepository;
import com.knowpivot.server.domain.repository.TokenTransactionRepository;
import com.knowpivot.server.infrastructure.exception.BusinessException;
import com.knowpivot.server.infrastructure.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Token 配额领域服务
 */
@Service
@RequiredArgsConstructor
public class TokenDomainService {

    private final UserRepository userRepository;
    private final TokenTransactionRepository tokenTransactionRepository;

    /**
     * 扣减 Token 并记录流水
     */
    public void deductToken(Long userId, long amount, String relatedId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (!user.hasEnoughToken(amount)) {
            throw new BusinessException(ResultCode.TOKEN_QUOTA_INSUFFICIENT);
        }
        user.deductToken(amount);
        userRepository.updateById(user);

        TokenTransaction transaction = TokenTransaction.builder()
                .userId(userId)
                .amount(-amount)
                .balanceAfter(user.getTokenQuota())
                .type(TransactionType.CONSUMPTION)
                .relatedId(relatedId)
                .remark("对话消耗")
                .build();
        tokenTransactionRepository.save(transaction);
    }

    /**
     * 回退 Token（AI 调用失败时）
     */
    public void refundToken(Long userId, long amount, String relatedId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return;
        }
        user.rechargeToken(amount);
        userRepository.updateById(user);

        TokenTransaction transaction = TokenTransaction.builder()
                .userId(userId)
                .amount(amount)
                .balanceAfter(user.getTokenQuota())
                .type(TransactionType.REFUND)
                .relatedId(relatedId)
                .remark("失败回退")
                .build();
        tokenTransactionRepository.save(transaction);
    }
}
