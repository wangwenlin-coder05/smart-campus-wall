package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WallPostMedia {
    private Long id;
    // 关联帖子id
    private Long postId;
    // 单个资源地址
    private String mediaUrl;
    // 排序
    private Integer sort;
    private Integer isDeleted;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}
