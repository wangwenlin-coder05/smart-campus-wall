package com.wwl.mapper;

import com.wwl.model.entity.WallReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 甯栧瓙/璇勮涓炬姤 Mapper
 */
@Mapper
public interface WallReportMapper {

    /**
     * 鍔ㄦ?佹潯浠舵煡璇妇鎶ュ垪琛?
     * @param postId 甯栧瓙ID
     * @param commentId 璇勮ID
     * @param status 澶勭悊鐘舵??
     */
    List<WallReport> selectReportList(
            @Param("postId") Long postId,
            @Param("commentId") Long commentId,
            @Param("status") Integer status
    );

    /**
     * 鏂板涓炬姤璁板綍
     */
    int insertReport(WallReport entity);

    /**
     * 淇敼涓炬姤澶勭悊鐘舵??
     */
    int updateReport(WallReport entity);

    /**
     * 閫昏緫鍒犻櫎涓炬姤
     */
    int logicDeleteReport(@Param("id") Long id);
}
