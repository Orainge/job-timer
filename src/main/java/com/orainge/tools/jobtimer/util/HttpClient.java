package com.orainge.tools.jobtimer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * 请求客户端
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

    @Resource
    private ObjectUtils objectUtils;

    @Value("${http-client.show-log: false}")
    private Boolean showLog;

    /**
     * 发起 GET 请求
     *
     * @param url    请求地址
     * @param params 请求报文
     * @return 返回的报文
     * @author orainge
     * @date 2021/6/10
     */
    public String doGet(String url, Object params) {
        return doGet(url, params, false);
    }

    /**
     * 发起 GET 请求
     *
     * @param url             请求地址
     * @param params          请求报文
     * @param infiniteTimeout 是否无限等待
     * @return 返回的报文
     * @author orainge
     * @date 2021/6/10
     */
    public String doGet(String url, Object params, boolean infiniteTimeout) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        RestTemplate restTemplate;

        // 根据是否需要无限等待而选择 RestTemplate
        if (infiniteTimeout) {
            restTemplate = noReadTimeoutRestTemplate;
        } else {
            restTemplate = defaultRestTemplate;
        }

        try {
            Map<String, Object> paramsMap = objectUtils.objectToMap(params);


            if (showLog) {
                String sendUrl = url;
                if (!Objects.isNull(paramsMap)) {
                    try {
                        for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue().toString();
                            sendUrl = sendUrl.replaceAll("\\{" + key + "}", value);
                        }
                    } catch (Exception ignore) {
                    }
                }
                log.debug("[Web 客户端] 发起请求: GET {}", sendUrl);
            }

            // 发起请求
            String response;

            if (Objects.isNull(paramsMap)) {
                // 参数为空
                response = restTemplate.getForObject(url, String.class);
            } else {
                // 参数不为空
                response = restTemplate.getForObject(url, String.class, paramsMap);
            }

            if (showLog) {
                log.debug("[Web 客户端] GET 请求结果: {}", response);
            }

            // 返回请求结果
            return response;
        } catch (Exception e) {
            log.error("[WEB 客户端] GET 请求出错: url: {}, 错误原因: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 发起 GET 请求
     *
     * @param url    请求地址
     * @param params 请求报文
     * @return 返回的报文 Map
     * @author orainge
     * @date 2021/6/10
     */
    public Map<String, Object> doGetMap(String url, Object params) {
        return doGetMap(url, params, false);
    }

    /**
     * 发起 GET 请求
     *
     * @param url             请求地址
     * @param params          请求报文
     * @param infiniteTimeout 是否无限等待
     * @return 返回的报文 Map
     * @author orainge
     * @date 2021/6/10
     */
    public Map<String, Object> doGetMap(String url, Object params, boolean infiniteTimeout) {
        try {
            String result = doGet(url, params, infiniteTimeout);
            return objectUtils.stringToMap(result);
        } catch (Exception e) {
            log.error("[WEB 客户端] GET 请求出错: url: {}, 错误原因: {}", url, e.getMessage());
            return null;
        }
    }
}
