package com.wwl.mapper;

import com.wwl.model.entity.ChatMessage;
import com.wwl.model.vo.ChatConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 聊天消息 Mapper。
 * SQL 写在 ChatMessageMapper.xml 中，保持和项目其它模块一致。
 */
@Mapper
public interface ChatMessageMapper {

    /** 新增一条聊天消息 */
    int insert(ChatMessage chatMessage);

    /** 查询某个会话的历史消息 */
    List<ChatMessage> listByConversationId(@Param("conversationId") String conversationId,
                                           @Param("limit") Integer limit);

    /** 查询消息列表页需要的最近会话 */
    List<ChatConversationVO> listConversation(@Param("userId") String userId);

    int markRead(@Param("conversationId") String conversationId, @Param("userId") String userId);

    int deleteConversation(@Param("conversationId") String conversationId);

    /** 批量软删除指定会话 */
    int batchDeleteConversations(@Param("ids") List<String> ids);

    /** 查找所有会话及参与者，用于按实际用户对去重 */
    List<Map<String, Object>> findDuplicateConversationIds();

    /** 按会话标题模糊匹配查找 conversation_id */
    List<String> findConversationIdsByTitle(@Param("keyword") String keyword);

    /** 软删除单条消息 */
    int deleteById(@Param("id") Long id);
}