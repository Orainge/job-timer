package com.orainge.tools.jobtimer.util.http;

/**
 * 自定义的 URLEncode 方法
 *
 * @author orainge
 * @since 2021/8/22
 */
public interface UrlEncodeMethod {
    /**
     * 使用 URLEncode 进行编码
     *
     * @param str 原字符串
     * @return 编码后的字符串
     */
    String encode(String str);
}

