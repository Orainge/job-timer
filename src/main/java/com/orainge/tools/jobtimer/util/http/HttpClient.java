package com.orainge.tools.jobtimer.util.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.*;

/**
 * HTTP 请求客户端
 *
 * @author orainge
 * @date 2021/6/10
 */
@Component
@ConditionalOnMissingBean({HttpClient.class})
public class HttpClient {
    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    @Resource
    @Qualifier("defaultRestTemplate")
    private RestTemplate defaultRestTemplate;

    @Resource
    @Qualifier("noReadTimeoutRestTemplate")
    private RestTemplate noReadTimeoutRestTemplate;

    @Value("${http-client.show-log: false}")
    private Boolean showLog;

    /**
     * 发起请求
     *
     * @param httpClientParameter HTTP 客户端请求参数
     * @param clazz               返回请求结果的类型
     * @return 请求结果 (Map 类型)
     */
    @SuppressWarnings("all")
    public Map<String, Object> exchangeForMap(HttpClientParameter httpClientParameter) {
        return exchange(httpClientParameter, Map.class);
    }

    /**
     * 发起请求
     *
     * @param httpClientParameter HTTP 客户端请求参数
     * @param clazz               返回请求结果的类型
     * @return 请求结果
     */
    public <T> T exchange(HttpClientParameter httpClientParameter, Class<T> clazz) {
        try {
            ResponseEntity<T> responseEntity = exchangeForEntity(httpClientParameter, clazz);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("[HTTP 客户端] - 获取请求结果出错", e);
            return null;
        }
    }

    /**
     * 发起请求
     *
     * @param httpClientParameter HTTP 客户端请求参数
     * @param clazz               返回请求结果的类型
     * @return 请求结果
     */
    public <T> ResponseEntity<T> exchangeForEntity(HttpClientParameter httpClientParameter, Class<T> clazz) {
        // 检查请求参数是否合法
        if (httpClientParameter == null || !httpClientParameter.isValid()) {
            log.error("[HTTP 客户端] - 请求参数不合法: {}", httpClientParameter == null ? "请求参数为 null" : httpClientParameter.getCheckResult());
            return null;
        }

        // 获取请求参数
        String requestUrl = httpClientParameter.getUrl(); // 请求 URL
        HttpMethod requestMethod = httpClientParameter.getMethod(); // 请求方式

        try {
            RestTemplate restTemplate;

            // 根据是否需要无限等待而选择 RestTemplate
            if (httpClientParameter.isInfiniteTimeout()) {
                restTemplate = noReadTimeoutRestTemplate;
            } else {
                restTemplate = defaultRestTemplate;
            }

            // 获取请求参数
            Object body = httpClientParameter.getBody(); // 请求体
            HttpHeaders headers = httpClientParameter.getHeaders(); // 请求头
            HttpEntity<?> requestEntity = Objects.isNull(body) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers); // 创建请求实体类

            // 创建请求参数
            MultiValueMap<String, String> params = httpClientParameter.getParams();

            if (params != null && !params.isEmpty()) {
                int urlEncodeMethod = httpClientParameter.getUrlEncodeMethod();
                if (HttpClientParameter.noUrlEncode.equals(urlEncodeMethod)) {
                    // 不使用 UrlEncode 方法创建请求 URL
                    requestUrl = concatParamToUrl(requestUrl, params);
                } else if (HttpClientParameter.defaultUrlEncode.equals(urlEncodeMethod)) {
                    // 使用默认的 UrlEncode 方法创建请求 URL
                    requestUrl = UriComponentsBuilder
                            .fromHttpUrl(requestUrl)
                            .queryParams(params).toUriString();
                } else if (HttpClientParameter.customUrlEncode.equals(urlEncodeMethod)) {
                    // 使用自定义的 URLEncode 方法创建请求URL
                    requestUrl = concatParamToUrl(requestUrl, params, (this::customUrlEncode));
                }
            }

            if (StringUtils.isEmpty(requestUrl)) {
                log.error("[HTTP 客户端] - URL 拼接请求参数错误 [{}]", httpClientParameter);
                return null;
            }

            // 请求 ID (用于日志显示)
            String requestId = null;

            if (showLog) {
                requestId = UUID.randomUUID().toString().substring(0, 8);
                log.info("[HTTP 客户端] - 请求 [{}]: {} {} , 请求体: [{}]", requestId, requestMethod.toString(), requestUrl, body);
            }

            // 返回请求结果
            ResponseEntity<T> exchange = restTemplate.exchange(requestUrl, requestMethod, requestEntity, clazz);

            if (showLog) {
                log.info("[HTTP 客户端] - 请求 [{}] 结果: {}", requestId, exchange.getBody());
            }

            return exchange;
        } catch (Exception e) {
            log.error("[HTTP 客户端] " + requestMethod.toString() + " 请求出错 [URL: " + requestUrl + ", Exception: " + e.getMessage() + "]", e);
            return null;
        }
    }

    /**
     * 自定义 URLEncode 方法<br>
     * 可以改写
     *
     * @param str 原字符串
     * @return 编码后的字符串
     */
    protected String customUrlEncode(String str) {
        try {
            // 自定义 URLEncode 编码
            str = str.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            str = str.replaceAll("\\+", "%2B");

            return str;
        } catch (Exception e) {
            log.error("[HTTP 客户端] - 对 " + str + " 进行 URLEncode 编码时错误", e);
            return null;
        }
    }

    /**
     * 将参数拼接到 URL 中
     *
     * @param url    请求 URL
     * @param params 参数列表
     * @return 拼接参数后的请求 URL
     */
    private String concatParamToUrl(String url, MultiValueMap<String, String> params) {
        return concatParamToUrl(url, params, null);
    }

    /**
     * 将参数拼接到 URL 中<br>
     * 使用自定义的 URLEncode 方法对请求参数名和请求参数进行编码
     *
     * @param url                   请求 URL
     * @param params                参数列表
     * @param customUrlEncodeMethod 自定义的 URLEncode 方法
     * @return 拼接参数后的请求 URL
     */
    private String concatParamToUrl(String url, MultiValueMap<String, String> params, UrlEncodeMethod customUrlEncodeMethod) {
        StringBuilder urlBuilder = new StringBuilder(url + "?");

        int i = 0;
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            i++;
            String paramKey = entry.getKey();
            List<String> paramValueList = entry.getValue();
            int j = 0;
            for (String paramValue : paramValueList) {
                j++;
                urlBuilder.append(customUrlEncodeMethod == null ? paramKey : customUrlEncodeMethod.encode(paramKey));
                urlBuilder.append("=");
                urlBuilder.append(customUrlEncodeMethod == null ? paramValue : customUrlEncodeMethod.encode(paramValue));
                if (j != paramValueList.size()) {
                    urlBuilder.append("&");
                }
            }
            if (i != params.size()) {
                urlBuilder.append("&");
            }
        }

        return urlBuilder.toString();
    }
}
