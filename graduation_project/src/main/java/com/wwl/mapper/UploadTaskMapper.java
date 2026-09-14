package com.wwl.mapper;

import com.wwl.model.entity.UploadTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OSS鍒嗙墖涓婁紶浠诲姟 Mapper
 */
@Mapper
public interface UploadTaskMapper {
    
    /**
     * 鏂板涓婁紶浠诲姟
     * @param task 涓婁紶浠诲姟瀹炰綋
     * @return 鍙楀奖鍝嶈鏁?     */
    int insertUploadTask(UploadTask task);
    
    /**
     * 鏍规嵁uploadId鏌ヨ涓婁紶浠诲姟
     * @param uploadId OSS涓婁紶ID
     * @return 涓婁紶浠诲姟瀹炰綋
     */
    UploadTask selectByUploadId(@Param("uploadId") String uploadId);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜岀姸鎬佹煡璇㈡湭瀹屾垚鐨勪笂浼犱换鍔?     * @param userUid 鐢ㄦ埛UID
     * @param statusList 鐘舵?佸垪琛?     * @return 涓婁紶浠诲姟鍒楄〃
     */
    List<UploadTask> selectUnfinishedTasks(@Param("userUid") String userUid, 
                                           @Param("statusList") List<Integer> statusList);
    
    /**
     * 鏇存柊涓婁紶杩涘害锛坈heckpoint锛?     * @param uploadId OSS涓婁紶ID
     * @param checkpoint 妫?鏌ョ偣鏁版嵁锛圝SON鏍煎紡锛?     * @param uploadedParts 宸蹭笂浼犲垎鐗囨暟
     * @return 鍙楀奖鍝嶈鏁?     */
    int updateCheckpoint(@Param("uploadId") String uploadId,
                         @Param("checkpoint") String checkpoint,
                         @Param("uploadedParts") Integer uploadedParts);
    
    /**
     * 鏇存柊浠诲姟鐘舵??     * @param uploadId OSS涓婁紶ID
     * @param status 鏂扮姸鎬?     * @return 鍙楀奖鍝嶈鏁?     */
    int updateStatus(@Param("uploadId") String uploadId, @Param("status") Integer status);
    
    /**
     * 閫昏緫鍒犻櫎涓婁紶浠诲姟
     * @param id 浠诲姟ID
     * @return 鍙楀奖鍝嶈鏁?     */
    int deleteUploadTask(@Param("id") Long id);
    
    /**
     * 娓呯悊杩囨湡鐨勬湭瀹屾垚涓婁紶浠诲姟
     * @return 鍙楀奖鍝嶈鏁?     */
    int cleanExpiredTasks();
}
