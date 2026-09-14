-- ============================================================
-- 清理重复会话 & 失物招领通知
-- 使用方法：在 MySQL 的 graduation_project 数据库中执行本文件。
-- 说明：
--   1. 按实际参与者（sender_id 集合）去重，同一组人只保留最新一条
--   2. 解决 user_host_*、org_*、friend_*、chat_* 不同格式的重复
--   3. 删除 conversation_title 包含"失物招领"的会话
--   4. 使用软删除（is_deleted=1），不会物理删除数据
-- ============================================================
SET NAMES utf8mb4;

-- 步骤1：查看所有重复的会话组（预览，不删除）
SELECT
    GROUP_CONCAT(DISTINCT cm.sender_id ORDER BY cm.sender_id SEPARATOR ',') AS participants,
    COUNT(DISTINCT cm.conversation_id) AS conv_count,
    GROUP_CONCAT(DISTINCT cm.conversation_id ORDER BY cm.conversation_id) AS conversation_ids
FROM chat_message cm
WHERE cm.is_deleted = 0
GROUP BY participants
HAVING COUNT(DISTINCT cm.conversation_id) > 1;

-- 步骤2：找出要保留的 conversation_id（每组中最新消息的那条）
-- 创建临时表存储每个参与者组的最新会话
CREATE TEMPORARY TABLE tmp_keep_conv AS
SELECT
    t.participants,
    t.keep_conv_id
FROM (
    SELECT
        GROUP_CONCAT(DISTINCT cm.sender_id ORDER BY cm.sender_id SEPARATOR ',') AS participants,
        SUBSTRING_INDEX(
            GROUP_CONCAT(DISTINCT cm.conversation_id ORDER BY cm.create_time DESC, cm.id DESC SEPARATOR ','),
            ',', 1
        ) AS keep_conv_id
    FROM chat_message cm
    WHERE cm.is_deleted = 0
    GROUP BY participants
) t;

-- 步骤3：软删除不在保留列表中的重复会话
UPDATE chat_message cm
SET cm.is_deleted = 1
WHERE cm.is_deleted = 0
  AND cm.conversation_id NOT IN (SELECT keep_conv_id FROM tmp_keep_conv)
  AND cm.conversation_id IN (
      -- 只删除那些参与者组有重复的 conversation_id
      SELECT DISTINCT dup.conversation_id
      FROM (
          SELECT
              cm2.conversation_id,
              GROUP_CONCAT(DISTINCT cm2.sender_id ORDER BY cm2.sender_id SEPARATOR ',') AS participants
          FROM chat_message cm2
          WHERE cm2.is_deleted = 0
          GROUP BY cm2.conversation_id
      ) dup
      INNER JOIN (
          SELECT
              GROUP_CONCAT(DISTINCT cm3.sender_id ORDER BY cm3.sender_id SEPARATOR ',') AS participants,
              COUNT(DISTINCT cm3.conversation_id) AS cnt
          FROM chat_message cm3
          WHERE cm3.is_deleted = 0
          GROUP BY participants
          HAVING cnt > 1
      ) grp ON dup.participants = grp.participants
  );

-- 删除临时表
DROP TEMPORARY TABLE IF EXISTS tmp_keep_conv;

-- 步骤4：查看"失物招领"相关会话（预览）
SELECT DISTINCT conversation_id, conversation_title
FROM chat_message
WHERE is_deleted = 0
  AND conversation_title LIKE '%失物招领%';

-- 步骤5：软删除"失物招领"相关会话
UPDATE chat_message
SET is_deleted = 1
WHERE is_deleted = 0
  AND conversation_title LIKE '%失物招领%';

-- 步骤6：验证清理结果
SELECT '清理完成' AS status,
    (SELECT COUNT(DISTINCT conversation_id) FROM chat_message WHERE is_deleted = 0) AS remaining_convs,
    (SELECT COUNT(DISTINCT conversation_id) FROM chat_message WHERE is_deleted = 0 AND conversation_title LIKE '%失物招领%') AS remaining_lost_found_convs;