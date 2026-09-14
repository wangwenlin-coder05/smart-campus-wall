package com.wwl.service;

import com.wwl.model.dto.OrganizationActivityDTO;
import com.wwl.model.entity.OrganizationActivity;
import com.wwl.model.entity.OrganizationActivityMember;

import java.util.List;
import java.util.Map;

public interface OrganizationActivityService {
    List<Map<String, Object>> getCategories();

    List<OrganizationActivity> list(OrganizationActivityDTO dto);

    OrganizationActivity publish(OrganizationActivityDTO dto);

    OrganizationActivity join(OrganizationActivityDTO dto);

    boolean quit(OrganizationActivityDTO dto);

    boolean updateStatus(OrganizationActivityDTO dto);

    Map<String, Object> mine(String userId);

    OrganizationActivity getById(Long id);

    List<OrganizationActivityMember> getMembers(Long activityId);
}