package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.Message;

import java.util.List;

public interface MessageRepository {

    void save(Message message);

    List<Message> findByConversationId(Long conversationId, int pageNum, int pageSize);

    long countByConversationId(Long conversationId);
}
