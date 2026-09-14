package com.wwl.mapper;

import com.wwl.model.dto.LostFoundDTO;
import com.wwl.model.entity.LostFoundApplication;
import com.wwl.model.entity.LostFoundItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LostFoundMapper {
    List<LostFoundItem> selectList(LostFoundDTO dto);

    LostFoundItem selectById(@Param("id") Long id, @Param("userUid") String userUid);

    int insertItem(LostFoundItem item);

    int increaseViews(Long id);

    int insertApplication(LostFoundApplication application);

    LostFoundApplication selectApplicationById(Long id);

    LostFoundApplication selectActiveApplication(@Param("itemId") Long itemId, @Param("applicantUid") String applicantUid);

    int countApplicationsByItemAndApplicant(@Param("itemId") Long itemId, @Param("applicantUid") String applicantUid);

    Map<String, Object> selectNotificationCounts(@Param("userUid") String userUid);

    int updateApplicationStatus(@Param("id") Long id, @Param("status") String status);

    int resolveItem(@Param("id") Long id, @Param("applicationId") Long applicationId, @Param("status") Integer status, @Param("applicantName") String applicantName);

    List<LostFoundApplication> selectApplicationsByPublisher(String userUid);

    List<LostFoundApplication> selectApplicationsByApplicant(String userUid);

    Map<String, Object> selectStats();

    /**
     * AI 助手用：多关键词 OR 搜索失物招领
     */
    List<LostFoundItem> selectByKeywords(@Param("keywords") List<String> keywords, @Param("limit") int limit);

    int softDeleteItem(@Param("id") Long id, @Param("userUid") String userUid);

    LostFoundApplication selectOpenApplicationByItem(Long itemId);
}