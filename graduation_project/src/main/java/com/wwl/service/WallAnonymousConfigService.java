package com.wwl.service;

import com.wwl.model.entity.WallAnonymousConfig;
import java.util.List;

/**
 * 匿名配置业务接口
 */
public interface WallAnonymousConfigService {

    /**
     * 新增匿名配置
     */
    boolean addAnonymousConfig(WallAnonymousConfig config);

    /**
     * 修改匿名配置
     */
    boolean editAnonymousConfig(WallAnonymousConfig config);

    /**
     * 逻辑删除匿名配置
     */
    boolean removeAnonymousConfig(Long id);

    /**
     * 根据ID查询详情
     */
    WallAnonymousConfig getConfigById(Long id);

    /**
     * 获取所有可用匿名配置列表（供帖子评论匿名使用）
     */
    List<WallAnonymousConfig> listNormalAnonymousConfig();
}
