package com.yealove.common;

/**
 * 配置文件校验异常类
 * Created by Yealove on 2016-09-26.
 */
public class ConfigCheckedException extends RuntimeException {
    public ConfigCheckedException() {
        super();
    }

    public ConfigCheckedException(String message) {
        super(message);
    }

    public ConfigCheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}
