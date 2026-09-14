package com.wwl.service.impl;

import com.wwl.model.entity.WallAnonymousConfig;
import com.wwl.mapper.WallAnonymousConfigMapper;
import com.wwl.service.WallAnonymousConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 鍖垮悕閰嶇疆涓氬姟瀹炵幇绫?
 * 鎻愪緵鍖垮悕閰嶇疆鐨勫鍒犳敼鏌ヤ笟鍔￠?昏緫
 */
@Service
public class WallAnonymousConfigServiceImpl implements WallAnonymousConfigService {

    @Autowired
    private WallAnonymousConfigMapper wallAnonymousConfigMapper;

    /**
     * 鏂板鍖垮悕閰嶇疆
     * @param config 閰嶇疆瀹炰綋
     * @return 鏄惁鏂板鎴愬姛
     */
    @Override
    public boolean addAnonymousConfig(WallAnonymousConfig config) {
        return wallAnonymousConfigMapper.insertAnonymousConfig(config) > 0;
    }

    /**
     * 淇敼鍖垮悕閰嶇疆
     * @param config 寰呬慨鏀瑰疄浣?
     * @return 鏄惁淇敼鎴愬姛
     */
    @Override
    public boolean editAnonymousConfig(WallAnonymousConfig config) {
        return wallAnonymousConfigMapper.updateAnonymousConfig(config) > 0;
    }

    /**
     * 閫昏緫鍒犻櫎鍖垮悕閰嶇疆
     * @param id 閰嶇疆涓婚敭
     * @return 鏄惁鍒犻櫎鎴愬姛
     */
    @Override
    public boolean removeAnonymousConfig(Long id) {
        return wallAnonymousConfigMapper.deleteAnonymousConfigById(id) > 0;
    }

    /**
     * 鏍规嵁ID鏌ヨ鍗曟潯閰嶇疆
     * @param id 閰嶇疆涓婚敭
     * @return 閰嶇疆瀹炰綋
     */
    @Override
    public WallAnonymousConfig getConfigById(Long id) {
        return wallAnonymousConfigMapper.selectAnonymousConfigById(id);
    }

    /**
     * 鑾峰彇鎵?鏈夋甯稿彲鐢ㄧ殑鍖垮悕閰嶇疆鍒楄〃
     * 渚涘笘瀛愩?佽瘎璁烘帴鍙ｅ仛鍖垮悕澶村儚鏄电О闅忔満鏇挎崲浣跨敤
     * @return 閰嶇疆闆嗗悎
     */
    @Override
    public List<WallAnonymousConfig> listNormalAnonymousConfig() {
        return wallAnonymousConfigMapper.selectAllNormalAnonymousConfig();
    }
}
