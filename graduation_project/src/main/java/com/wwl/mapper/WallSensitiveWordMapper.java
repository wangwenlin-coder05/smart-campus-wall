package com.wwl.mapper;

import com.wwl.model.entity.WallSensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 鏁忔劅璇嶅簱 Mapper
 */
@Mapper
public interface WallSensitiveWordMapper {

    /**
     * 鍔ㄦ?佹煡璇㈡晱鎰熻瘝鍒楄〃
     * @param word 鏁忔劅璇嶅叧閿瘝
     */
    List<WallSensitiveWord> selectSensitiveList(@Param("word") String word);

    /**
     * 鏂板鏁忔劅璇?
     */
    int insertSensitive(WallSensitiveWord entity);

    /**
     * 閫昏緫鍒犻櫎鏁忔劅璇?
     */
    int logicDeleteSensitive(@Param("id") Long id);
}
