package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.TokenTransaction;

import java.util.List;

public interface TokenTransactionRepository {

    void save(TokenTransaction transaction);

    List<TokenTransaction> findByUserId(Long userId, int pageNum, int pageSize);
}
