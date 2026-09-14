package com.wwl.service.impl;

import com.wwl.model.entity.UserDutyRel;
import com.wwl.mapper.UserDutyRelMapper;
import com.wwl.service.UserDutyRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserDutyRelServiceImpl implements UserDutyRelService {

    @Autowired
    private UserDutyRelMapper userDutyRelMapper;

    /**
     * 鏌ヨ鐢ㄦ埛鎷ユ湁鐨勬墍鏈夎亴鍔d
     */
    @Override
    public List<Integer> getUserDutyList(Long userId) {
        return userDutyRelMapper.getDutyIdByUserId(userId);
    }

    /**
     * 鏌ヨ鐢ㄦ埛鎷ユ湁鐨勬墍鏈夎亴鍔″悕绉?
     */
    public List<String> getUserDutyNameList(Long userId) {
        return userDutyRelMapper.getDutyNameByUserId(userId);
    }


    /**
     * 鎵归噺淇濆瓨鐢ㄦ埛鑱屽姟鍕鹃??
     * 鏍稿績閫昏緫锛氬厛娓呯┖鏃у叧绯?鈫?鍐嶆壒閲忔柊澧炴柊鍏崇郴  涓嶇敤update淇敼
     */
    @Transactional
    @Override
    public void saveUserDuty(Long userId, List<Integer> dutyIdList) {
        // 1.鍏堝垹鎺夎鐢ㄦ埛涔嬪墠鎵?鏈夎亴鍔″叧鑱?
        userDutyRelMapper.deleteAllByUserId(userId);

        // 2.鎵归噺娣诲姞鏂板嬀閫夌殑鑱屽姟
        if(dutyIdList != null && !dutyIdList.isEmpty()){
            for (Integer dutyId : dutyIdList) {
                UserDutyRel rel = new UserDutyRel();
                rel.setUserId(userId);
                rel.setDutyId(dutyId);
                userDutyRelMapper.insert(rel);
            }
        }
    }
}
