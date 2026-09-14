package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallComment瑙嗗浘杩斿洖VO绫? */
@Data
public class WallCommentVO {
    @JsonIgnore
    private Long id;

    /**
     * 鏄惁鍖垮悕璇勮锛?-瀹炲悕 1-鍖垮悕
     */
    private Integer isAnonymous;

    /**
     * 璇勮鐐硅禐鏁伴噺
     */
    private Integer likeCount;

    /**
     * 鐖惰瘎璁篒D锛?浠ｈ〃椤剁骇璇勮
     */
    private Long parentId;

    /**
     * 鍥炲鐩爣璇勮ID
     */
    private Long replyCommentId;

    /**
     * 琚洖澶嶇敤鎴疯劚鏁忓瓧绗︿覆UID
     */
    private String replyUserUid;

}
