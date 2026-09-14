package com.wwl.mapper;

import com.wwl.model.entity.SchoolDuty;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SchoolDutyMapper {
    // 鏂板鑱屽姟
    int insertDuty(SchoolDuty duty);
    // 鏍规嵁id鏌ュ崟涓亴鍔?
    SchoolDuty getDutyById(Integer id);
    // 鏌ヨ鍏ㄩ儴鑱屽姟鍒楄〃
    List<SchoolDuty> getDutyList();
    // 澶氭潯浠舵煡璇?
    List<SchoolDuty> getDutyByCondition(SchoolDuty duty);
    // 淇敼鑱屽姟
    int updateDuty(SchoolDuty duty);
    // 鍒犻櫎鑱屽姟
    int deleteDuty(Integer id);
}
