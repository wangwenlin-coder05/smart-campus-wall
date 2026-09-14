package com.wwl.mapper;

import com.wwl.model.entity.OrderDict;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 璁㈠崟鍒嗙被瀛楀吀鎸佷箙灞?
 */
@Mapper
public interface OrderDictMapper {
    // 鏂板鍒嗙被
    int insertDict(OrderDict dict);

    // 鏍规嵁id鏌ヨ
    OrderDict getDictById(Integer id);

    // 鏌ヨ鍏ㄩ儴鍒嗙被鍒楄〃锛堢粰鍓嶇涓嬫媺鐢級
    List<OrderDict> listAllDict();

    // 澶氭潯浠舵煡璇?
    List<OrderDict> getDictByCondition(OrderDict dict);

    // 淇敼鍒嗙被
    int updateDict(OrderDict dict);

    // 鍒犻櫎鍒嗙被
    int deleteDict(Integer id);
}
