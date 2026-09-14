package com.wwl.mapper;

import com.wwl.model.dto.OrganizationActivityDTO;
import com.wwl.model.entity.OrganizationActivity;
import com.wwl.model.entity.OrganizationActivityMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrganizationActivityMapper {
    List<OrganizationActivity> selectActivityList(OrganizationActivityDTO dto);

    OrganizationActivity selectById(Long id);

    int insertActivity(OrganizationActivity activity);

    int updateActivity(OrganizationActivity activity);

    int countByCreatorOrBrand(@Param("creatorUserId") String creatorUserId, @Param("brandName") String brandName);

    int syncGroupCountByCreatorOrBrand(@Param("creatorUserId") String creatorUserId, @Param("brandName") String brandName);

    int insertMember(OrganizationActivityMember member);

    int updateMemberStatus(@Param("activityId") Long activityId, @Param("userId") String userId, @Param("status") String status);

    OrganizationActivityMember selectMember(@Param("activityId") Long activityId, @Param("userId") String userId);

    int increaseJoinNum(Long activityId);

    int decreaseJoinNum(Long activityId);

    List<OrganizationActivity> selectCreatedByUser(String userId);

    List<OrganizationActivity> selectJoinedByUser(String userId);

    List<String> selectAvatarList(Long activityId);

    List<OrganizationActivityMember> selectMembersByActivityId(Long activityId);

    /**
     * AI 助手用：多关键词 OR 搜索组局活动
     * 例如 keywords=["今晚","活动"] → WHERE title LIKE '%今晚%' OR title LIKE '%活动%'
     */
    List<OrganizationActivity> selectByKeywords(@Param("keywords") List<String> keywords, @Param("limit") int limit);
}