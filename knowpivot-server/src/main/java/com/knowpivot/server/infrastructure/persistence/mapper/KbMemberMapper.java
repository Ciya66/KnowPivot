package com.knowpivot.server.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowpivot.server.infrastructure.persistence.po.KbMemberPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KbMemberMapper extends BaseMapper<KbMemberPO> {
}
