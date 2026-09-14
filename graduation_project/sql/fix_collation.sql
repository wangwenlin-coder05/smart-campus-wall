-- 修复 chat_message 与 chat_conversation_read 表排序规则不一致的问题
-- 错误：Illegal mix of collations (utf8mb4_general_ci,IMPLICIT) and (utf8mb4_0900_ai_ci,IMPLICIT)

SET NAMES utf8mb4;

-- 方案一：将 chat_message 转为 utf8mb4_general_ci（与其他表保持一致）
ALTER TABLE chat_message
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 如果上面的语句报错（比如有外键约束），可以用方案二：
-- 方案二：将 chat_conversation_read 转为 utf8mb4_0900_ai_ci
-- ALTER TABLE chat_conversation_read
--     CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;