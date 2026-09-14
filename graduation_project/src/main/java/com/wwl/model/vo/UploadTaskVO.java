package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * UploadTask瑙嗗浘杩斿洖VO绫? */
@Data
public class UploadTaskVO {
    @JsonIgnore
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

}
