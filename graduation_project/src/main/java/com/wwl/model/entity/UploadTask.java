package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * OSS鍒嗙墖涓婁紶浠诲姟瀹炰綋绫? * 瀵瑰簲琛細upload_task
 * 鐢ㄤ簬璁板綍鍒嗙墖涓婁紶鐨勮繘搴︿俊鎭紝鏀寔鏂偣缁紶
 */
@Data
public class UploadTask {
    
    /**
     * 涓婚敭鑷ID
     */
    private Long id;
    
    /**
     * OSS涓婁紶ID锛堢敱闃块噷浜慜SS鐢熸垚锛?     */
    private String uploadId;
    
    /**
     * 鏂囦欢鍘熷鍚嶇О
     */
    private String fileName;
    
    /**
     * 鏂囦欢澶у皬锛堝瓧鑺傦級
     */
    private Long fileSize;
    
    /**
     * 鏂囦欢MIME绫诲瀷
     */
    private String contentType;
    
    /**
     * OSS瀛樺偍璺緞锛坥bject key锛?     */
    private String objectKey;
    
    /**
     * 鐢ㄦ埛ID
     */
    private String userUid;
    
    /**
     * 涓婁紶鐘舵?侊細0-鍒濆鍖?1-涓婁紶涓?2-宸插畬鎴?3-宸插彇娑?4-宸插け璐?     */
    private Integer status;
    
    /**
     * 妫?鏌ョ偣鏁版嵁锛圝SON鏍煎紡锛岃褰曞凡涓婁紶鐨勫垎鐗囦俊鎭級
     */
    private String checkpoint;
    
    /**
     * 宸蹭笂浼犲垎鐗囨暟閲?     */
    private Integer uploadedParts;
    
    /**
     * 鎬诲垎鐗囨暟閲?     */
    private Integer totalParts;
    
    /**
     * 閫昏緫鍒犻櫎鏍囪瘑锛?-鏈垹闄?1-宸插垹闄?     */
    private Integer isDeleted;
    
    /**
     * 鍒涘缓鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 鏇存柊鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 杩囨湡鏃堕棿锛堣秴杩囨鏃堕棿鐨勬湭瀹屾垚浠诲姟鍙娓呯悊锛?     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime expireTime;
}
