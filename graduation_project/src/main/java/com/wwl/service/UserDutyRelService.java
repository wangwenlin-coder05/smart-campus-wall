package com.wwl.service;

import java.util.List;

public interface UserDutyRelService {

    // 鑾峰彇鐢ㄦ埛宸查?夌殑鎵?鏈夎亴鍔d
    List<Integer> getUserDutyList(Long userId);

    // 鑾峰彇鐢ㄦ埛宸查?夌殑鎵?鏈夎亴鍔″悕绉?
    List<String> getUserDutyNameList(Long userId);


    // 淇濆瓨鐢ㄦ埛鍕鹃?夌殑涓?鎵硅亴鍔★紙鍒犳棫瀛樻柊锛?
    void saveUserDuty(Long userId, List<Integer> dutyIdList);
}
