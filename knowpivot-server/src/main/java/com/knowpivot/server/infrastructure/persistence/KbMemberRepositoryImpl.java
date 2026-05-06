package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowpivot.server.domain.entity.KbMember;
import com.knowpivot.server.domain.repository.KbMemberRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.KbMemberMapper;
import com.knowpivot.server.infrastructure.persistence.po.KbMemberPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class KbMemberRepositoryImpl implements KbMemberRepository {

    private final KbMemberMapper kbMemberMapper;
    private final POConverter converter;

    @Override
    public void save(KbMember member) {
        KbMemberPO po = converter.toPO(member);
        kbMemberMapper.insert(po);
        member.setId(po.getId());
    }

    @Override
    public void deleteByKbIdAndUserId(Long kbId, Long userId) {
        LambdaQueryWrapper<KbMemberPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbMemberPO::getKbId, kbId)
                .eq(KbMemberPO::getUserId, userId);
        kbMemberMapper.delete(wrapper);
    }

    @Override
    public KbMember findByKbIdAndUserId(Long kbId, Long userId) {
        LambdaQueryWrapper<KbMemberPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbMemberPO::getKbId, kbId)
                .eq(KbMemberPO::getUserId, userId);
        KbMemberPO po = kbMemberMapper.selectOne(wrapper);
        return converter.toKbMember(po);
    }

    @Override
    public List<KbMember> findByKbId(Long kbId) {
        LambdaQueryWrapper<KbMemberPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbMemberPO::getKbId, kbId);
        return kbMemberMapper.selectList(wrapper).stream()
                .map(converter::toKbMember)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByKbIdAndUserId(Long kbId, Long userId) {
        LambdaQueryWrapper<KbMemberPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbMemberPO::getKbId, kbId)
                .eq(KbMemberPO::getUserId, userId);
        return kbMemberMapper.selectCount(wrapper) > 0;
    }
}
