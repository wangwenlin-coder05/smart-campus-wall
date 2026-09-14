package com.wwl.service;

import com.wwl.model.entity.WallSensitiveWord;
import java.util.List;

/**
 * 敏感词库 业务接口
 */
public interface IWallSensitiveWordService {

    /**
     * 动态条件查询敏感词列表
     * @param word 敏感词关键词（模糊查询，可选）
     * @return 敏感词列表
     */
    List<WallSensitiveWord> getSensitiveList(String word);

    /**
     * 新增敏感词
     * @param entity 敏感词实体
     * @return 操作结果
     */
    boolean addSensitive(WallSensitiveWord entity);

    /**
     * 逻辑删除敏感词
     * @param id 敏感词ID
     * @return 操作结果
     */
    boolean delSensitive(Long id);
}