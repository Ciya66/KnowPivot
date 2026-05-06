-- =============================================
-- KnowPivot 数据库初始化脚本
-- MySQL 8.0+
-- =============================================

CREATE DATABASE IF NOT EXISTS `knowpivot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `knowpivot`;

-- =============================================
-- 1. 用户与权限模块
-- =============================================

CREATE TABLE `t_user` (
    `id` BIGINT NOT NULL COMMENT '主键，雪花算法',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
    `token_quota` BIGINT NOT NULL DEFAULT 0 COMMENT '剩余Token配额',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0普通，1管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `t_knowledge_base` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `name` VARCHAR(128) NOT NULL COMMENT '知识库名称',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `index_name` VARCHAR(64) NOT NULL COMMENT 'Redis向量索引名',
    `config` JSON DEFAULT NULL COMMENT '配置：切片大小、相似度阈值',
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

CREATE TABLE `t_kb_member` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `kb_id` BIGINT NOT NULL COMMENT '知识库ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` TINYINT NOT NULL COMMENT '角色：0查看者，1编辑者，2管理员',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_kb_user` (`kb_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库成员表';

-- =============================================
-- 2. 文档与向量化模块
-- =============================================

CREATE TABLE `t_document` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `kb_id` BIGINT NOT NULL COMMENT '所属知识库ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `storage_path` VARCHAR(512) NOT NULL COMMENT 'MinIO存储路径',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0已上传，1解析中，2已索引，3失败',
    `chunk_count` INT NOT NULL DEFAULT 0 COMMENT '切片数量',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_kb_id` (`kb_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

CREATE TABLE `t_document_segment` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `doc_id` BIGINT NOT NULL COMMENT '所属文档ID',
    `kb_id` BIGINT NOT NULL COMMENT '所属知识库ID',
    `vector_id` VARCHAR(64) NOT NULL COMMENT 'Redis向量Key',
    `content` TEXT NOT NULL COMMENT '切片文本内容',
    `page_num` INT DEFAULT NULL COMMENT '来源页码',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_doc_id` (`doc_id`),
    UNIQUE KEY `uk_vector_id` (`vector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档切片表';

-- =============================================
-- 3. 对话与Agent模块
-- =============================================

CREATE TABLE `t_conversation` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `kb_id` BIGINT DEFAULT NULL COMMENT '关联知识库ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '会话标题',
    `last_message_time` DATETIME NOT NULL COMMENT '最后消息时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

CREATE TABLE `t_message` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `conversation_id` BIGINT NOT NULL COMMENT '所属会话ID',
    `role` TINYINT NOT NULL COMMENT '角色：0用户，1助手，2系统',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `token_count` INT NOT NULL DEFAULT 0 COMMENT '消耗Token数',
    `references` JSON DEFAULT NULL COMMENT '引用来源列表',
    `feedback` TINYINT DEFAULT NULL COMMENT '反馈：1点赞，2点踩',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =============================================
-- 4. 系统与配置模块
-- =============================================

CREATE TABLE `t_prompt_template` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `code` VARCHAR(64) NOT NULL COMMENT '模板唯一标识',
    `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt模板表';

CREATE TABLE `t_token_transaction` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` BIGINT NOT NULL COMMENT '变动数量',
    `balance_after` BIGINT NOT NULL COMMENT '变动后余额',
    `type` TINYINT NOT NULL COMMENT '类型：1充值，2消耗，3回退',
    `related_id` VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token流水表';

-- =============================================
-- 初始数据
-- =============================================

-- 默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO `t_user` (`id`, `username`, `password_hash`, `nickname`, `token_quota`, `role`, `status`)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 999999, 1, 1);

-- 默认 Prompt 模板
INSERT INTO `t_prompt_template` (`id`, `code`, `name`, `content`, `is_active`)
VALUES (1, 'SYSTEM_PROMPT', '系统提示词', '你是一个智能知识库助手，请基于提供的文档内容回答用户问题。如果文档中没有相关内容，请如实告知。', 1);
