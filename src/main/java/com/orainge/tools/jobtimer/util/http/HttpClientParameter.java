package com.orainge.tools.jobtimer.util.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP 客户端请求
 *
 * @author orainge
 * @date 2021/8/23
 */
public class HttpClientParameter {
    public static final Integer noUrlEncode = 0;
    public static final Integer defaultUrlEncode = 1;
    public static final Integer customUrlEncode = 2;

    /**
     * 请求方式
     */
    private HttpMethod method = null;

    /**
     * 请求 URL
     */
    private String url = null;

    /**
     * 请求头
     */
    private final HttpHeaders headers = new HttpHeaders();

    /**
     * 请求参数
     */
    private final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    /**
     * 请求体
     */
    private Object body = null;

    /**
     * 是否使用 URLEncoder 方式拼接请求参数<br>
     * 0: 不使用 1: 使用默认的 URLEncoder 方式 2: 使用自定义的 URLEncoder 方式
     */
    private int urlEncodeMethod = defaultUrlEncode;

    /**
     * 是否无限时间等待请求结果
     */
    private boolean infiniteTimeout = false;

    /**
     * "请求参数是否合法"检查结果
     */
    private String checkResult = null;

    public HttpClientParameter() {
        // 默认设置为 JSON
        this.headers.setContentType(MediaType.parseMediaType("application/json; charset=utf-8"));
    }

    /**
     * 创建 HTTP 请求参数类
     */
    public static HttpClientParameter build() {
        return new HttpClientParameter();
    }

    /**
     * 设置请求方式
     *
     * @param method 请求方式
     */
    public HttpClientParameter setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    /**
     * 设置请求 URL
     *
     * @param url 请求 URL
     */
    public HttpClientParameter setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 添加请求头
     *
     * @param headerName  请求头名称
     * @param headerValue 请求头值
     */
    public HttpClientParameter addHeader(String headerName, String headerValue) {
        if (StringUtils.isEmpty(headerName) || StringUtils.isEmpty(headerValue)) {
            return this;
        }

        this.headers.add(headerName, headerValue);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param headersMap 请求参数 Map
     */
    public HttpClientParameter addHeaders(Map<String, String> headersMap) {
        if (headersMap == null || headersMap.isEmpty()) {
            return this;
        }

        headersMap.forEach(this.headers::add);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param headersMap 请求参数 Map
     */
    public HttpClientParameter addHeaders(MultiValueMap<String, String> headersMap) {
        if (headersMap == null || headersMap.isEmpty()) {
            return this;
        }

        this.headers.addAll(headersMap);
        return this;
    }

    /**
     * 添加请求头
     *
     * @param headersMap 请求参数 Map
     */
    public HttpClientParameter addHeadersObjectMap(Map<String, Object> headersMap) {
        if (headersMap == null || headersMap.isEmpty()) {
            return this;
        }

        headersMap.forEach((headerKey, headerValue) -> this.headers.add(headerKey, headerValue.toString()));
        return this;
    }

    /**
     * 添加请求头
     *
     * @param headersMap 请求参数 Map
     */
    public HttpClientParameter addHeadersObjectMap(MultiValueMap<String, Object> headersMap) {
        if (headersMap == null || headersMap.isEmpty()) {
            return this;
        }

        headersMap.forEach((headerKey, headerValueList) -> {
            if (headerValueList != null) {
                headerValueList.forEach(value -> this.headers.add(headerKey, value.toString()));
            }
        });
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param paramName  请求参数名称
     * @param paramValue 请求参数值
     */
    public HttpClientParameter addParam(String paramName, String paramValue) {
        if (StringUtils.isEmpty(paramName) || StringUtils.isEmpty(paramValue)) {
            return this;
        }

        this.params.add(paramName, paramValue);
        return this;
    }

    /**
     * 设置请求参数
     *
     * @param paramsMap 请求参数 Map
     */
    public HttpClientParameter addParams(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return this;
        }

        paramsMap.forEach(this.params::add);
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param paramsMap 请求参数 Map
     */
    public HttpClientParameter addParams(MultiValueMap<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return this;
        }

        this.params.addAll(paramsMap);
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param paramsMap 请求参数 Map
     */
    public HttpClientParameter addParamsObjectMap(Map<String, Object> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return this;
        }

        paramsMap.forEach((paramKey, paramValue) -> this.params.add(paramKey, paramValue.toString()));
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param paramsMap 请求参数 Map
     */
    public HttpClientParameter addParamsObjectMap(MultiValueMap<String, Object> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return this;
        }

        paramsMap.forEach((paramKey, paramValueList) -> {
            if (paramValueList != null) {
                paramValueList.forEach(value -> this.params.add(paramKey, value.toString()));
            }
        });
        return this;
    }

    /**
     * 设置请求体
     *
     * @param body 请求体
     */
    public HttpClientParameter setBody(Object body) {
        this.body = body;
        return this;
    }

    /**
     * 设置 Content-Type
     */
    public HttpClientParameter setContentType(MediaType mediaType) {
        if (mediaType == null) {
            return this;
        }

        this.headers.setContentType(mediaType);
        return this;
    }

    /**
     * 设置 Content-Type
     */
    public HttpClientParameter setContentType(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            return this;
        }

        this.headers.setContentType(MediaType.parseMediaType(contentType));
        return this;
    }

    /**
     * 设置 URLEncoder 方式为：不编码
     */
    public HttpClientParameter noUrlEncode() {
        this.urlEncodeMethod = noUrlEncode;
        return this;
    }

    /**
     * 设置 URLEncoder 方式为：使用默认的 URLEncoder 方式
     */
    public HttpClientParameter defaultUrlEncode() {
        this.urlEncodeMethod = defaultUrlEncode;
        return this;
    }

    /**
     * 设置 URLEncoder 方式为：使用自定义的 URLEncoder 方式
     */
    public HttpClientParameter customUrlEncode() {
        this.urlEncodeMethod = customUrlEncode;
        return this;
    }

    /**
     * 设置是否无限时间等待请求结果
     *
     * @param infiniteTimeout 是否无限时间等待请求结果
     */
    public HttpClientParameter setInfiniteTimeout(boolean infiniteTimeout) {
        this.infiniteTimeout = infiniteTimeout;
        return this;
    }

    /**
     * 检查该请求参数类是否合法
     */
    public boolean isValid() {
        // 检查请求 URL 是否为空
        if (StringUtils.isEmpty(this.url)) {
            checkResult = "请求 URL 为空";
            return false;
        }

        // 检查请求方式是否为空
        HttpMethod requestMethod = this.method;
        if (requestMethod == null) {
            checkResult = "请求方式 [HttpMethod] 为空";
            return false;
        }

        // 检查通过
        return true;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public MultiValueMap<String, String> getParams() {
        return params;
    }

    public Object getBody() {
        return body;
    }

    public int getUrlEncodeMethod() {
        return urlEncodeMethod;
    }

    public boolean isInfiniteTimeout() {
        return infiniteTimeout;
    }

    public String getCheckResult() {
        return this.checkResult;
    }

    @Override
    public String toString() {
        return "{" +
                "method=" + method +
                ", headers=" + headers +
                ", params=" + params +
                ", body=" + body +
                ", infiniteTimeout=" + infiniteTimeout +
                '}';
    }
}