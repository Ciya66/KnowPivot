package com.knowpivot.server.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowpivot.server.domain.entity.*;
import com.knowpivot.server.domain.enums.*;
import com.knowpivot.server.domain.valueobject.KnowledgeConfig;
import com.knowpivot.server.infrastructure.persistence.po.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * PO <-> Domain Entity 转换器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class POConverter {

    private final ObjectMapper objectMapper;

    // ==================== User ====================
    public UserPO toPO(User user) {
        if (user == null) return null;
        UserPO po = new UserPO();
        po.setId(user.getId());
        po.setUsername(user.getUsername());
        po.setPasswordHash(user.getPasswordHash());
        po.setNickname(user.getNickname());
        po.setAvatarUrl(user.getAvatarUrl());
        po.setTokenQuota(user.getTokenQuota());
        po.setRole(user.getRole() != null ? user.getRole().getCode() : 0);
        po.setStatus(user.getStatus() != null ? user.getStatus().getCode() : 1);
        po.setCreatedAt(user.getCreatedAt());
        po.setUpdatedAt(user.getUpdatedAt());
        po.setIsDeleted(user.getIsDeleted());
        return po;
    }

    public User toUser(UserPO po) {
        if (po == null) return null;
        return User.builder()
                .id(po.getId())
                .username(po.getUsername())
                .passwordHash(po.getPasswordHash())
                .nickname(po.getNickname())
                .avatarUrl(po.getAvatarUrl())
                .tokenQuota(po.getTokenQuota())
                .role(toEnum(UserRole.class, po.getRole()))
                .status(toEnum(UserStatus.class, po.getStatus()))
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .isDeleted(po.getIsDeleted())
                .build();
    }

    // ==================== KnowledgeBase ====================
    public KnowledgeBasePO toPO(KnowledgeBase kb) {
        if (kb == null) return null;
        KnowledgeBasePO po = new KnowledgeBasePO();
        po.setId(kb.getId());
        po.setName(kb.getName());
        po.setDescription(kb.getDescription());
        po.setIndexName(kb.getIndexName());
        po.setConfig(toJson(kb.getConfig()));
        po.setCreatorId(kb.getCreatorId());
        po.setCreatedAt(kb.getCreatedAt());
        po.setUpdatedAt(kb.getUpdatedAt());
        po.setIsDeleted(kb.getIsDeleted());
        return po;
    }

    public KnowledgeBase toKnowledgeBase(KnowledgeBasePO po) {
        if (po == null) return null;
        return KnowledgeBase.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .indexName(po.getIndexName())
                .config(fromJson(po.getConfig(), KnowledgeConfig.class))
                .creatorId(po.getCreatorId())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .isDeleted(po.getIsDeleted())
                .build();
    }

    // ==================== Document ====================
    public DocumentPO toPO(Document doc) {
        if (doc == null) return null;
        DocumentPO po = new DocumentPO();
        po.setId(doc.getId());
        po.setKbId(doc.getKbId());
        po.setFileName(doc.getFileName());
        po.setStoragePath(doc.getStoragePath());
        po.setFileSize(doc.getFileSize());
        po.setStatus(doc.getStatus() != null ? doc.getStatus().getCode() : 0);
        po.setChunkCount(doc.getChunkCount());
        po.setVersion(doc.getVersion());
        po.setCreatedAt(doc.getCreatedAt());
        po.setUpdatedAt(doc.getUpdatedAt());
        po.setIsDeleted(doc.getIsDeleted());
        return po;
    }

    public Document toDocument(DocumentPO po) {
        if (po == null) return null;
        return Document.builder()
                .id(po.getId())
                .kbId(po.getKbId())
                .fileName(po.getFileName())
                .storagePath(po.getStoragePath())
                .fileSize(po.getFileSize())
                .status(toEnum(DocumentStatus.class, po.getStatus()))
                .chunkCount(po.getChunkCount())
                .version(po.getVersion())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .isDeleted(po.getIsDeleted())
                .build();
    }

    // ==================== DocumentSegment ====================
    public DocumentSegmentPO toPO(DocumentSegment seg) {
        if (seg == null) return null;
        DocumentSegmentPO po = new DocumentSegmentPO();
        po.setId(seg.getId());
        po.setDocId(seg.getDocId());
        po.setKbId(seg.getKbId());
        po.setVectorId(seg.getVectorId());
        po.setContent(seg.getContent());
        po.setPageNum(seg.getPageNum());
        po.setCreatedAt(seg.getCreatedAt());
        return po;
    }

    public DocumentSegment toDocumentSegment(DocumentSegmentPO po) {
        if (po == null) return null;
        return DocumentSegment.builder()
                .id(po.getId())
                .docId(po.getDocId())
                .kbId(po.getKbId())
                .vectorId(po.getVectorId())
                .content(po.getContent())
                .pageNum(po.getPageNum())
                .createdAt(po.getCreatedAt())
                .build();
    }

    // ==================== Conversation ====================
    public ConversationPO toPO(Conversation conv) {
        if (conv == null) return null;
        ConversationPO po = new ConversationPO();
        po.setId(conv.getId());
        po.setUserId(conv.getUserId());
        po.setKbId(conv.getKbId());
        po.setTitle(conv.getTitle());
        po.setLastMessageTime(conv.getLastMessageTime());
        po.setCreatedAt(conv.getCreatedAt());
        po.setUpdatedAt(conv.getUpdatedAt());
        po.setIsDeleted(conv.getIsDeleted());
        return po;
    }

    public Conversation toConversation(ConversationPO po) {
        if (po == null) return null;
        return Conversation.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .kbId(po.getKbId())
                .title(po.getTitle())
                .lastMessageTime(po.getLastMessageTime())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .isDeleted(po.getIsDeleted())
                .build();
    }

    // ==================== Message ====================
    public MessagePO toPO(Message msg) {
        if (msg == null) return null;
        MessagePO po = new MessagePO();
        po.setId(msg.getId());
        po.setConversationId(msg.getConversationId());
        po.setRole(msg.getRole() != null ? msg.getRole().getCode() : 0);
        po.setContent(msg.getContent());
        po.setTokenCount(msg.getTokenCount());
        po.setReferences(toJson(msg.getReferences()));
        po.setFeedback(msg.getFeedback() != null ? msg.getFeedback().getCode() : null);
        po.setCreatedAt(msg.getCreatedAt());
        return po;
    }

    public Message toMessage(MessagePO po) {
        if (po == null) return null;
        return Message.builder()
                .id(po.getId())
                .conversationId(po.getConversationId())
                .role(toEnum(MessageRole.class, po.getRole()))
                .content(po.getContent())
                .tokenCount(po.getTokenCount())
                .references(fromJsonList(po.getReferences(), new TypeReference<List<Message.Reference>>() {}))
                .feedback(po.getFeedback() != null ? toEnum(FeedbackType.class, po.getFeedback()) : null)
                .createdAt(po.getCreatedAt())
                .build();
    }

    // ==================== KbMember ====================
    public KbMemberPO toPO(KbMember member) {
        if (member == null) return null;
        KbMemberPO po = new KbMemberPO();
        po.setId(member.getId());
        po.setKbId(member.getKbId());
        po.setUserId(member.getUserId());
        po.setRole(member.getRole() != null ? member.getRole().getCode() : 0);
        po.setCreatedAt(member.getCreatedAt());
        return po;
    }

    public KbMember toKbMember(KbMemberPO po) {
        if (po == null) return null;
        return KbMember.builder()
                .id(po.getId())
                .kbId(po.getKbId())
                .userId(po.getUserId())
                .role(toEnum(KbMemberRole.class, po.getRole()))
                .createdAt(po.getCreatedAt())
                .build();
    }

    // ==================== PromptTemplate ====================
    public PromptTemplatePO toPO(PromptTemplate template) {
        if (template == null) return null;
        PromptTemplatePO po = new PromptTemplatePO();
        po.setId(template.getId());
        po.setCode(template.getCode());
        po.setName(template.getName());
        po.setContent(template.getContent());
        po.setIsActive(template.getIsActive());
        po.setCreatedAt(template.getCreatedAt());
        po.setUpdatedAt(template.getUpdatedAt());
        return po;
    }

    public PromptTemplate toPromptTemplate(PromptTemplatePO po) {
        if (po == null) return null;
        return PromptTemplate.builder()
                .id(po.getId())
                .code(po.getCode())
                .name(po.getName())
                .content(po.getContent())
                .isActive(po.getIsActive())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    // ==================== TokenTransaction ====================
    public TokenTransactionPO toPO(TokenTransaction tx) {
        if (tx == null) return null;
        TokenTransactionPO po = new TokenTransactionPO();
        po.setId(tx.getId());
        po.setUserId(tx.getUserId());
        po.setAmount(tx.getAmount());
        po.setBalanceAfter(tx.getBalanceAfter());
        po.setType(tx.getType() != null ? tx.getType().getCode() : 0);
        po.setRelatedId(tx.getRelatedId());
        po.setRemark(tx.getRemark());
        po.setCreatedAt(tx.getCreatedAt());
        return po;
    }

    public TokenTransaction toTokenTransaction(TokenTransactionPO po) {
        if (po == null) return null;
        return TokenTransaction.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .amount(po.getAmount())
                .balanceAfter(po.getBalanceAfter())
                .type(toEnum(TransactionType.class, po.getType()))
                .relatedId(po.getRelatedId())
                .remark(po.getRemark())
                .createdAt(po.getCreatedAt())
                .build();
    }

    // ==================== Helpers ====================
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization failed", e);
            return null;
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON deserialization failed", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T fromJsonList(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return (T) Collections.emptyList();
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("JSON list deserialization failed", e);
            return (T) Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<?>> E toEnum(Class<E> enumClass, Integer code) {
        if (code == null) return null;
        for (E e : enumClass.getEnumConstants()) {
            // All enums have getCode() method
            try {
                int enumCode = (int) enumClass.getMethod("getCode").invoke(e);
                if (enumCode == code) return e;
            } catch (Exception ex) {
                log.error("Enum conversion failed", ex);
                return null;
            }
        }
        return null;
    }
}
