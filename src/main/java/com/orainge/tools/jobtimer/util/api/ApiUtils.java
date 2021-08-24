package com.orainge.tools.jobtimer.util.api;

import com.orainge.tools.jobtimer.config.ApiConfig;
import com.orainge.tools.jobtimer.util.JSONUtils;
import com.orainge.tools.jobtimer.util.http.HttpClient;
import com.orainge.tools.jobtimer.util.http.HttpClientParameter;
import com.orainge.tools.jobtimer.util.key.ApiKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 外部 API 调用工具类
 *
 * @author orainge
 * @since 2021/8/19
 */
public abstract class ApiUtils {
    @Resource
    private HttpClient httpClient;

    @Resource
    private ApiConfig apiConfig;

    @Resource
    private JSONUtils jsonUtils;

    protected String utilName;

    private ApiKeyManager apiKeyManager = null;

    private Logger log = LoggerFactory.getLogger(ApiUtils.class);

    private ApiConfig.ApiDetailsConfig apiDetailsConfig = null;

    /**
     * 当不需要 API 进行访问时，可以不自定义 ApiKeyManager
     *
     * @param configName 配置文件名
     * @param utilName   工具名称（用于日志显示）
     */
    protected void initMethod(String configName, String utilName) {
        initMethod(configName, utilName, null);
    }

    /**
     * 当需要 API 进行访问时，必须自定义 ApiKeyManager
     *
     * @param configName 配置文件名
     * @param utilName   工具名称（用于日志显示）
     */
    protected void initMethod(String configName, String utilName, ApiKeyManager apiKeyManager) {
        if (StringUtils.isEmpty(configName)) {
            throw new NullPointerException("[配置文件] - 配置文件名不存在，请检查配置文件");
        }

        apiDetailsConfig = apiConfig.getConfig().get(configName);

        // 检查配置
        if (apiDetailsConfig == null) {
            throw new NullPointerException("[" + utilName + " 配置文件] - 配置文件不存在，请检查配置文件");
        }

        // 检查 URL 配置
        String url = apiDetailsConfig.getUrl();
        if (StringUtils.isEmpty(url)) {
            throw new NullPointerException("[" + utilName + " 配置文件] - 未配置 URL，请检查配置文件");
        }

        if (StringUtils.isEmpty(utilName)) {
            utilName = this.getClass().getSimpleName();
        }

        // 检查 KeyManager
        // 当设置了请求 API 需要 Key 时才进行检查
        if (apiDetailsConfig.isNeedKey()) {
            if (apiKeyManager == null) {
                throw new NullPointerException("[" + utilName + " 配置文件] - 未配置 ApiKeyManager，请检查配置文件");
            } else {
                this.apiKeyManager = apiKeyManager;
            }
        } else {
            this.apiKeyManager = null;
        }

        this.utilName = utilName;
        log = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * 执行 GET 操作
     *
     * @param apiName     API名称
     * @param headers     请求头
     * @param params      请求参数
     * @param body        请求体
     * @param extraParams 额外参数，不参与请求
     */
    protected Map<String, Object> doGet(String apiName,
                                        MultiValueMap<String, String> headers,
                                        MultiValueMap<String, Object> params,
                                        Object body,
                                        Map<String, Object> extraParams) {
        return exchange(HttpMethod.GET, apiName, headers, params, body, extraParams);
    }

    /**
     * 执行 POST 操作
     *
     * @param apiName     API名称
     * @param headers     请求头
     * @param params      请求参数
     * @param body        请求体
     * @param extraParams 额外参数，不参与请求
     */
    protected Map<String, Object> doPost(String apiName,
                                         MultiValueMap<String, String> headers,
                                         MultiValueMap<String, Object> params,
                                         Object body,
                                         Map<String, Object> extraParams) {
        return exchange(HttpMethod.POST, apiName, headers, params, body, extraParams);
    }

    /**
     * 执行获取操作
     *
     * @param method      请求方式
     * @param apiName     API名称
     * @param headers     请求头
     * @param params      请求参数
     * @param body        请求体
     * @param extraParams 额外参数，不参与请求
     */
    protected Map<String, Object> exchange(HttpMethod method,
                                           String apiName,
                                           MultiValueMap<String, String> headers,
                                           MultiValueMap<String, Object> params,
                                           Object body,
                                           Map<String, Object> extraParams) {
        // 获取接口 url
        String api = apiDetailsConfig.getApi().get(apiName);
        if (StringUtils.isEmpty(api)) {
            throw new NullPointerException("[" + utilName + " 配置文件] - 接口 [" + apiName + "] 不存在，请检查配置文件");
        }
        String baseUrl = apiDetailsConfig.getUrl() + api;

        // 准备参数 Map
        MultiValueMap<String, Object> urlParams;
        if (params != null) {
            urlParams = new LinkedMultiValueMap<>(params);
        } else {
            urlParams = new LinkedMultiValueMap<>();
        }

        // 准备额外参数 Map
        if (extraParams == null) {
            extraParams = new LinkedHashMap<>();
        }

        // 准备结果数据 Map
        Map<String, Object> responseBody = null;

        while (true) {
            // Key 值
            String key = null;

            // 当请求需要 Key 时
            if (apiKeyManager != null) {
                // 先检查 Key 是否存在后，再进行查询
                key = apiKeyManager.getKey();
                if (Objects.isNull(key)) {
                    logError("获取信息错误: 所有 Key 已达到查询次数上限");
                    break;
                }

                // 添加/更新 Key 参数
                urlParams.add(apiKeyManager.getKeyParameterName(), key);
            }

            boolean isKeyExpire = false;
            boolean isSuccess = false;

            // 重试机制
            int retryTimes = apiDetailsConfig.getRetryTimes();
            for (int i = 0; i <= retryTimes; i++) {
                HttpClientParameter httpClientParameter = HttpClientParameter.build()
                        .setMethod(method)
                        .setUrl(baseUrl)
                        .addHeaders(headers)
                        .addParamsObjectMap(urlParams)
                        .setBody(body);

                // 调用接口进行查询
                ResponseEntity<String> resultEntity = httpClient.exchangeForEntity(httpClientParameter, String.class);

                if (Objects.isNull(resultEntity) || StringUtils.isEmpty(resultEntity.getBody())) {
                    // 获取结果为空时，执行回调函数
                    onResultNull(key, httpClientParameter, extraParams);
                } else {
                    // 获取到数据后，尝试将数据转换为 Map
                    String responseBodyStr = resultEntity.getBody();
                    responseBody = jsonUtils.parseObjectToMap(responseBodyStr);

                    if (responseBody == null) {
                        // 无法将获取到的数据转换为 Map
                        responseBody = onResultFailToMap(key, httpClientParameter, responseBodyStr, extraParams);
                        if (responseBody == null) {
                            // 不能处理请求结果，继续重试请求
                            continue;
                        }
                    }

                    // 获取到的结果能转换为 Map
                    // 判断数据是否获取成功
                    if (checkIfResultSuccess(httpClientParameter, responseBody, extraParams)) {
                        // 数据获取成功
                        // 如果需要 Key 才能进行访问，则需要判断 Key 是否过期
                        if (apiKeyManager != null && checkIfKeyExpire(httpClientParameter, responseBody, extraParams)) {
                            // Key 过期
                            apiKeyManager.setExpire(key); // 设置该 Key 已过期
                            isKeyExpire = true;
                            responseBody = null;
                        } else {
                            // 不需要Key 或 Key 没有过期，执行成功回调函数
                            onSuccess(key, httpClientParameter, responseBody, extraParams);
                            isSuccess = true;
                        }
                        break;
                    } else {
                        // 数据获取失败
                        onResultFail(key, httpClientParameter, responseBodyStr, responseBody, extraParams);
                        responseBody = null;
                    }
                }

                Integer nextRetryTimes = i + 1 <= retryTimes ? i + 1 : null;
                if (nextRetryTimes != null) {
                    logWarn("获取信息错误: 进行第 {} 次重试", nextRetryTimes);
                }
            }

            // 退出条件
            // 1. 不需要 KEY 访问，执行到这里代表重试次数已经用完，直接退出
            // 2. 需要 KEY 访问，就需要检查访问是否成功，如果成功就退出
            // 3. 需要 KEY 访问，且没有访问成功，且当前 Key 没有过期，则代表已经达到重试次数，退出
            // 4. 以上条件都不符合，代表当前 KEY 过期了，就继续 while 循环获取下一个 KEY
            if (apiKeyManager == null || isSuccess || !isKeyExpire) {
                break;
            }
        }

        return responseBody;
    }

    protected void logWarn(String message, Object... arguments) {
        log.warn("[" + utilName + " API 工具] - " + message, arguments);
    }

    protected void logError(String message, Object... arguments) {
        log.error("[" + utilName + " API 工具] - " + message, arguments);
    }

    /**
     * 判断函数：判断 Key 是否过期<br>
     * 当访问 API 不需要 KEY 时，该函数返回值忽略
     */
    public abstract boolean checkIfKeyExpire(HttpClientParameter httpClientParameter, Map<String, Object> responseBody, Map<String, Object> extraParams);

    /**
     * 判断函数：当有返回结果时，判断返回结果是否为符合"获取成功"的条件<br>
     * 例如，返回状态码的值是否正确
     */
    public abstract boolean checkIfResultSuccess(HttpClientParameter httpClientParameter, Map<String, Object> responseBody, Map<String, Object> extraParams);

    /**
     * 回调函数：当返回结果为 NULL 时
     */
    public abstract void onResultNull(String nowKey, HttpClientParameter httpClientParameter, Map<String, Object> extraParams);

    /**
     * 回调函数：有返回结果，但是无法将请求结果转化为 Map 类型<br>
     * 如果可以，可以在这里处理请求结果<br>
     *
     * @return null: 不能处理请求结果，继续重试请求; 非 null: 对请求结果进行处理，返回正确的 Map，继续执行下面的处理流程
     */
    public abstract Map<String, Object> onResultFailToMap(String nowKey, HttpClientParameter httpClientParameter, String responseBodyStr, Map<String, Object> extraParams);

    /**
     * 回调函数：有返回结果，但是结果错误时
     */
    public abstract void onResultFail(String nowKey, HttpClientParameter httpClientParameter, String responseBodyStr, Map<String, Object> responseBody, Map<String, Object> extraParams);

    /**
     * 回调函数：有返回结果，且结果正确时
     */
    public abstract void onSuccess(String nowKey, HttpClientParameter httpClientParameter, Map<String, Object> responseBody, Map<String, Object> extraParams);
}
