package indiv.hmdp.utils;

import indiv.hmdp.common.CommonCode;
import indiv.hmdp.common.ResultCode;

import java.io.Serializable;

public class ResultUtil<T> implements Serializable {

    /**
     * 状态
     */
    public boolean status;
    /**
     * 状态码
     */
    public Integer code;
    /**
     * 返回信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    public ResultUtil() {

    }

    public ResultUtil(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
        this.status = resultCode.isSuccess();
    }

    public ResultUtil(Integer code,String message,boolean status, T data) {
        this.code=code;
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public static <Type> ResultUtil<Type> success(Type data) {
        return new ResultUtil<>(CommonCode.SUCCESS, data);
    }

    public static <Type> ResultUtil<Type> success() {
        return new ResultUtil<>(CommonCode.SUCCESS, null);
    }

    public static <Type> ResultUtil<Type> fail() {
        return new ResultUtil<>(CommonCode.FAIL, null);
    }

    public static <Type> ResultUtil<Type> fail(String message) {
        return new ResultUtil<>(CommonCode.FAIL.getCode(),message,CommonCode.FAIL.isSuccess(), null);
    }

    public static <Type> ResultUtil<Type> failWithExMessage(ResultCode resultCode) {
        return new ResultUtil<>(resultCode, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return status;
    }
}
