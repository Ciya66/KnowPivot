package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowpivot.server.domain.entity.Conversation;
import com.knowpivot.server.domain.repository.ConversationRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.ConversationMapper;
import com.knowpivot.server.infrastructure.persistence.po.ConversationPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final ConversationMapper conversationMapper;
    private final POConverter converter;

    @Override
    public Conversation findById(Long id) {
        ConversationPO po = conversationMapper.selectById(id);
        return converter.toConversation(po);
    }

    @Override
    public void save(Conversation conversation) {
        ConversationPO po = converter.toPO(conversation);
        conversationMapper.insert(po);
        conversation.setId(po.getId());
    }

    @Override
    public void updateById(Conversation conversation) {
        ConversationPO po = converter.toPO(conversation);
        conversationMapper.updateById(po);
    }

    @Override
    public void deleteById(Long id) {
        conversationMapper.deleteById(id);
    }

    @Override
    public List<Conversation> findByUserId(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<ConversationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationPO::getUserId, userId)
                .orderByDesc(ConversationPO::getLastMessageTime);
        Page<ConversationPO> page = conversationMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return page.getRecords().stream()
                .map(converter::toConversation)
                .collect(Collectors.toList());
    }
}
