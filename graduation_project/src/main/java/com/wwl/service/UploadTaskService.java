package com.wwl.service;

import com.wwl.model.entity.UploadTask;

import java.util.List;
import java.util.Map;

/**
 * OSS鍒嗙墖涓婁紶浠诲姟鏈嶅姟鎺ュ彛
 */
public interface UploadTaskService {
    
    /**
     * 鍒濆鍖栧垎鐗囦笂浼犱换鍔?     * @param fileName 鏂囦欢鍚嶇О
     * @param fileSize 鏂囦欢澶у皬
     * @param contentType 鏂囦欢绫诲瀷
     * @param userUid 鐢ㄦ埛UID
     * @return 鍖呭惈uploadId銆乷bjectKey绛変俊鎭殑Map
     */
    Map<String, Object> initMultipartUpload(String fileName, Long fileSize, 
                                             String contentType, String userUid);
    
    /**
     * 淇濆瓨涓婁紶杩涘害锛坈heckpoint锛?     * @param uploadId OSS涓婁紶ID
     * @param checkpoint 妫?鏌ョ偣鏁版嵁锛圝SON瀛楃涓诧級
     * @param uploadedParts 宸蹭笂浼犲垎鐗囨暟
     */
    void saveCheckpoint(String uploadId, String checkpoint, Integer uploadedParts);
    
    /**
     * 鑾峰彇鏈畬鎴愮殑涓婁紶浠诲姟锛堢敤浜庣画浼狅級
     * @param userUid 鐢ㄦ埛UID
     * @return 鏈畬鎴愮殑浠诲姟鍒楄〃
     */
    List<UploadTask> getUnfinishedTasks(String userUid);
    
    /**
     * 鏍规嵁uploadId鑾峰彇浠诲姟璇︽儏
     * @param uploadId OSS涓婁紶ID
     * @return 涓婁紶浠诲姟瀹炰綋
     */
    UploadTask getTaskByUploadId(String uploadId);
    
    /**
     * 瀹屾垚涓婁紶浠诲姟
     * @param uploadId OSS涓婁紶ID
     */
    void completeUpload(String uploadId);
    
    /**
     * 鍙栨秷涓婁紶浠诲姟
     * @param uploadId OSS涓婁紶ID
     */
    void cancelUpload(String uploadId);
    
    /**
     * 鏍囪涓婁紶澶辫触
     * @param uploadId OSS涓婁紶ID
     */
    void failUpload(String uploadId);
    
    /**
     * 娓呯悊杩囨湡鐨勪笂浼犱换鍔★紙瀹氭椂浠诲姟璋冪敤锛?     * @return 娓呯悊鐨勪换鍔℃暟閲?     */
    int cleanExpiredTasks();
}
