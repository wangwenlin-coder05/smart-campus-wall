package com.wwl.service;

import com.wwl.model.entity.SchoolDuty;
import java.util.List;

/**
 * 校园职务业务接口
 * 提供职务信息的增删改查功能
 */
public interface SchoolDutyService {

    /**
     * 新增校园职务
     *
     * @param duty 职务信息对象，包含职务名称和职务说明
     * @return 新增成功返回true，失败返回false
     */
    boolean addDuty(SchoolDuty duty);

    /**
     * 根据ID查询职务详情
     *
     * @param id 职务ID
     * @return 职务信息对象，未找到返回null
     */
    SchoolDuty getById(Integer id);

    /**
     * 查询所有职务列表
     *
     * @return 职务信息列表，无数据返回空列表
     */
    List<SchoolDuty> listAll();

    /**
     * 多条件组合查询职务列表
     * 支持按职务名称、职务说明等字段进行模糊或精确查询
     *
     * @param duty 查询条件对象，传入的字段将作为筛选条件
     * @return 符合条件的职务信息列表
     */
    List<SchoolDuty> conditionList(SchoolDuty duty);

    /**
     * 修改职务信息
     *
     * @param duty 要修改的职务信息对象，必须包含ID
     * @return 修改成功返回true，失败返回false
     */
    boolean updateDuty(SchoolDuty duty);

    /**
     * 删除职务
     *
     * @param id 要删除的职务ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteDuty(Integer id);
}
