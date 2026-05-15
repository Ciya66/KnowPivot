package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowpivot.server.domain.entity.Message;
import com.knowpivot.server.domain.repository.MessageRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.MessageMapper;
import com.knowpivot.server.infrastructure.persistence.po.MessagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageMapper messageMapper;
    private final POConverter converter;

    @Override
    public void save(Message message) {
        MessagePO po = converter.toPO(message);
        messageMapper.insert(po);
        message.setId(po.getId());
    }

    @Override
    public List<Message> findByConversationId(Long conversationId, int pageNum, int pageSize) {
        LambdaQueryWrapper<MessagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessagePO::getConversationId, conversationId)
                .orderByAsc(MessagePO::getCreatedAt);
        Page<MessagePO> page = messageMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return page.getRecords().stream()
                .map(converter::toMessage)
                .collect(Collectors.toList());
    }

    @Override
    public long countByConversationId(Long conversationId) {
        LambdaQueryWrapper<MessagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessagePO::getConversationId, conversationId);
        return messageMapper.selectCount(wrapper);
    }
}
