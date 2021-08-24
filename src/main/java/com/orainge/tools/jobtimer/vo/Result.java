package com.orainge.tools.jobtimer.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

/**
 * 返回结果类
 *
 * @author orainge
 * @since 2021/8/19
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    private Integer code;
    private String message;
    private Object data;

    public static Result build() {
        return new Result();
    }

    public static Result build(HttpStatus status) {
        Result result = new Result();
        result.setCode(status.value());
        result.setMessage(status.getReasonPhrase());
        return result;
    }

    public static Result ok() {
        return build(HttpStatus.OK);
    }

    public static Result error() {
        return build(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static Result forbidden() {
        return build(HttpStatus.FORBIDDEN);
    }

    public static Result notFound() {
        return build(HttpStatus.NOT_FOUND);
    }

    public Integer getCode() {
        return code;
    }

    public Result setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "code: " + code +
                ", message: \"" + message + '\"' +
                ", data: \"" + data + '\"' +
                '}';
    }
}