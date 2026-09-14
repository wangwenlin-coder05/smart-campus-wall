package com.wwl.model.vo;

import lombok.Data;

@Data
public class OrderCountVO {
    private Integer received;      //1 已接单
    private Integer delivering;    //2 配送中
    private Integer finished;     //3 已送达
    private Integer dispute;      //4 纠纷中
    private Integer complete;     //5 已完成
    private Integer cancel;       //6 已撤销
}
