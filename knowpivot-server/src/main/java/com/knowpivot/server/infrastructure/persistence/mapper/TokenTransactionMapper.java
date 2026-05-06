package com.knowpivot.server.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowpivot.server.infrastructure.persistence.po.TokenTransactionPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TokenTransactionMapper extends BaseMapper<TokenTransactionPO> {
}
