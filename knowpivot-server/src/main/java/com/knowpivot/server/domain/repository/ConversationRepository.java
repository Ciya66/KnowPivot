package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.Conversation;

import java.util.List;

public interface ConversationRepository {

    Conversation findById(Long id);

    void save(Conversation conversation);

    void updateById(Conversation conversation);

    void deleteById(Long id);

    List<Conversation> findByUserId(Long userId, int pageNum, int pageSize);
}
