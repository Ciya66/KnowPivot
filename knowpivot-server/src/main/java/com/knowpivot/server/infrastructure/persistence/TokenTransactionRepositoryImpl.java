package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowpivot.server.domain.entity.TokenTransaction;
import com.knowpivot.server.domain.repository.TokenTransactionRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.TokenTransactionMapper;
import com.knowpivot.server.infrastructure.persistence.po.TokenTransactionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TokenTransactionRepositoryImpl implements TokenTransactionRepository {

    private final TokenTransactionMapper tokenTransactionMapper;
    private final POConverter converter;

    @Override
    public void save(TokenTransaction transaction) {
        TokenTransactionPO po = converter.toPO(transaction);
        tokenTransactionMapper.insert(po);
        transaction.setId(po.getId());
    }

    @Override
    public List<TokenTransaction> findByUserId(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<TokenTransactionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TokenTransactionPO::getUserId, userId)
                .orderByDesc(TokenTransactionPO::getCreatedAt);
        Page<TokenTransactionPO> page = tokenTransactionMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return page.getRecords().stream()
                .map(converter::toTokenTransaction)
                .collect(Collectors.toList());
    }
}
