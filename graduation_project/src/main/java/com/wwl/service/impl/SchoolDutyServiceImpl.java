package com.wwl.service.impl;

import com.wwl.model.entity.SchoolDuty;
import com.wwl.mapper.SchoolDutyMapper;
import com.wwl.service.SchoolDutyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SchoolDutyServiceImpl implements SchoolDutyService {

    @Autowired
    private SchoolDutyMapper dutyMapper;

    @Override
    public boolean addDuty(SchoolDuty duty) {
        return dutyMapper.insertDuty(duty) > 0;
    }

    @Override
    public SchoolDuty getById(Integer id) {
        return dutyMapper.getDutyById(id);
    }

    @Override
    public List<SchoolDuty> listAll() {
        return dutyMapper.getDutyList();
    }

    @Override
    public List<SchoolDuty> conditionList(SchoolDuty duty) {
        return dutyMapper.getDutyByCondition(duty);
    }

    @Override
    public boolean updateDuty(SchoolDuty duty) {
        return dutyMapper.updateDuty(duty) > 0;
    }

    @Override
    public boolean deleteDuty(Integer id) {
        return dutyMapper.deleteDuty(id) > 0;
    }
}
