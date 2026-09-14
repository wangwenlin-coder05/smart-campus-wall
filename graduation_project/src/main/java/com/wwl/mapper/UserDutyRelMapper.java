package com.wwl.mapper;

import com.wwl.model.entity.UserDutyRel;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 鐢ㄦ埛鑱屽姟鍏宠仈琛ㄦ寔涔呭眰
 * 鍙仛锛氭煡璇€?佹柊澧炪?佸垹闄? 鏃犱慨鏀箄pdate
 */
@Mapper
public interface UserDutyRelMapper {

    /**
     * 鏍规嵁鐢ㄦ埛id 鏌ヨ璇ョ敤鎴锋墍鏈夊凡閫夎亴鍔d
     */
    List<Integer> getDutyIdByUserId(Long userId);

    /**
     * 鏍规嵁鐢ㄦ埛id 鏌ヨ 璇ョ敤鎴锋墍鏈夎亴鍔′腑鏂囧悕绉?
     */
    List<String> getDutyNameByUserId(Long userId);

    /**
     * 鍗曟潯鏂板鐢ㄦ埛鑱屽姟鍏宠仈
     */
    int insert(UserDutyRel rel);

    /**
     * 鍒犻櫎鐢ㄦ埛鏌愪竴鏉¤亴鍔″叧鑱?
     */
    int deleteByUserAndDuty(Long userId, Integer dutyId);

    /**
     * 娓呯┖璇ョ敤鎴锋墍鏈夎亴鍔″叧鑱旓紙鏀瑰嬀閫夋椂鍏堟竻绌烘棫鏁版嵁锛?
     */
    int deleteAllByUserId(Long userId);
}
