package com.wwl.mapper;



import com.wwl.model.entity.Rider;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 骑手Mapper接口
 * 只定义方法，SQL全部手写在xml文件中
 * 原生MyBatis，完全自主可控
 */
@Mapper
public interface RiderMapper {

    // 新增骑手信息
    int insertRider(Rider rider);

    // 根据id查询单个骑手
    Rider selectRiderById(Long id);

    // 根据userId查询骑手
    Rider selectRiderByUserId(String userId);

    // 查询所有骑手列表
    List<Rider> selectRiderList();

    // 修改骑手信息
    int updateRider(Rider rider);

    // 根据id删除骑手
    int deleteRiderById(Long id);

    // 通用多条件查询
    List<Rider> selectRiderListByCondition(Rider rider);
}
