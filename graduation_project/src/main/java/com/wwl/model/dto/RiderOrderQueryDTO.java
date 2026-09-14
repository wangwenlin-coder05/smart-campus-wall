package com.wwl.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RiderOrderQueryDTO {
    @NotNull(message = "骑手ID不能为空")
    private String riderUserId;

    private Integer orderStatus;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String parentType;

    private String subType;

    /** 排序字段 */
    private String sortField;
    /** 排序方式 asc/desc 默认倒序 */
    private String sortOrder = "desc";
}
