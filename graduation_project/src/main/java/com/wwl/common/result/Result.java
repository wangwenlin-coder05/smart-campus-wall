package com.wwl.common.result;

import lombok.Data;

/**
 * 统一后端响应结果
 * 1 成功  0 失败
 */
@Data
public class Result<T> {

    // 响应码 1成功 0失败
    private Integer code;
    // 提示信息
    private String msg;
    // 泛型数据
    private T data;

    // 成功无参
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg("操作成功");
        return result;
    }

    // 成功带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 成功自定义提示
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 失败
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }

    // 自定义错误码失败
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
