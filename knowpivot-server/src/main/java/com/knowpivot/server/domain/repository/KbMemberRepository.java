package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.KbMember;

import java.util.List;

public interface KbMemberRepository {

    void save(KbMember member);

    void deleteByKbIdAndUserId(Long kbId, Long userId);

    KbMember findByKbIdAndUserId(Long kbId, Long userId);

    List<KbMember> findByKbId(Long kbId);

    boolean existsByKbIdAndUserId(Long kbId, Long userId);
}
