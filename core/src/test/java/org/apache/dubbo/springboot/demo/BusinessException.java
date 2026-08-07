package org.apache.dubbo.springboot.demo;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常
 */
public class BusinessException
        extends RuntimeException {
    /**
     * 业务错误码
     */
    @Getter
    @Setter
    private Integer errorCode;

    /**
     * 消息
     */
    @Getter
    @Setter
    private String message;

    /**
     * 数据
     */
    @Getter
    @Setter
    private Object data;

    /**
     * provider-all中抛出的异常(已记录一次)通过CommonResult返回给api层，在api层不需要再记录
     * true : 已记录（不需要重复记录）
     * false : 还没记录
     */
    @Getter
    private boolean recorded = false;

    public BusinessException() {
    }

    public BusinessException(String message) {
        this.message = message;
    }

    public BusinessException(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public BusinessException(String message, Throwable cause) {
        super(cause);
        this.message = message;
    }


    public BusinessException(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public BusinessException(int errorCode, String message, Object data) {
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }

    public BusinessException record(boolean recorded) {
        this.recorded = recorded;
        return this;
    }

}
