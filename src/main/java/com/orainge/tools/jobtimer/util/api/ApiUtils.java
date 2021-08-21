package com.orainge.tools.jobtimer.util.api;

import com.orainge.tools.jobtimer.config.ApiConfig;
import com.orainge.tools.jobtimer.util.HttpClient;
import com.orainge.tools.jobtimer.util.key.ApiKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 外部 API 调用工具类
 *
 * @author orainge
 * @date 2021/8/19
 */
public abstract class ApiUtils {
    @Resource
    private HttpClient httpClient;

    @Resource
    private ApiConfig apiConfig;

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
     * 执行获取操作<br>
     * 参数不包括 Key，需要重写 addKeyParams参数。
     *
     * @param apiName     API名称
     * @param params      params={p1:a, p2: b}, 结果url: url?p1={p1}&p2={p2}
     * @param extraParams 额外参数，不参与请求
     */
    protected Map<String, Object> doGetData(String apiName, Map<String, Object> params, Map<String, Object> extraParams) {
        // 获取接口 url
        String api = apiDetailsConfig.getApi().get(apiName);
        if (StringUtils.isEmpty(api)) {
            throw new NullPointerException("[" + utilName + " 配置文件] - 接口 [" + apiName + "] 不存在，请检查配置文件");
        }
        String baseUrl = apiDetailsConfig.getUrl() + api;

        // 准备参数 Map
        Map<String, Object> urlParams = new LinkedHashMap<>(params);

        // 准备额外参数 Map
        if (extraParams == null) {
            extraParams = new LinkedHashMap<>();
        }

        // 准备结果数据 Map
        Map<String, Object> resultData = null;

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
                urlParams.put(apiKeyManager.getKeyParameterName(), key);
            }

            // 拼接 url 参数
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            if (!urlParams.isEmpty()) {
                urlBuilder.append("?");
                int i = 0;
                for (String keyItem : urlParams.keySet()) {
                    i++;
                    urlBuilder.append(keyItem).append("=").append("{").append(keyItem).append("}");
                    if (i != urlParams.size()) {
                        urlBuilder.append("&");
                    }
                }
            }

            boolean isKeyExpire = false;
            boolean isSuccess = false;

            // 重试机制
            int retryTimes = apiDetailsConfig.getRetryTimes();
            int i = 0;
            for (; i <= retryTimes; i++) {
                // 调用接口进行查询
                resultData = httpClient.doGetMap(urlBuilder.toString(), urlParams);

                if (Objects.isNull(resultData)) {
                    // 获取结果为空时，执行回调函数
                    Integer nextRetryTimes = i + 1 <= retryTimes ? i + 1 : null;
                    onResultNull(key, urlParams, extraParams);
                    if (nextRetryTimes != null) {
                        logWarn("获取信息错误: 进行第 {} 次重试", nextRetryTimes);
                    }
                } else {
                    // 获取到数据后，判断数据是否获取成功
                    if (checkIfResultSuccess(resultData, extraParams)) {
                        // 数据获取成功
                        // 如果需要 Key 才能进行访问，则需要判断 Key 是否过期
                        if (apiKeyManager != null && checkIfKeyExpire(resultData, extraParams)) {
                            // Key 过期
                            apiKeyManager.setExpire(key); // 设置该 Key 已过期
                            isKeyExpire = true;
                            break;
                        } else {
                            // 不需要Key 或 Key 没有过期，执行成功回调函数
                            onSuccess(resultData, extraParams);
                            isSuccess = true;
                            break;
                        }
                    } else {
                        onResultFail(key, resultData, extraParams);
                    }
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

        return resultData;
    }

    protected void logWarn(String message, Object... arguments) {
        log.warn("[" + utilName + " 配置文件] - " + message, arguments);
    }

    protected void logError(String message, Object... arguments) {
        log.error("[" + utilName + " 配置文件] - " + message, arguments);
    }

    /**
     * 判断 Key 是否过期
     */
    public abstract boolean checkIfKeyExpire(Map<String, Object> resultData, Map<String, Object> extraParams);

    /**
     * 当有返回结果时，需要判断该返回结果是否为符合成功获取条件<br>
     * 例如，返回状态码的值是否正确<br>
     */
    public abstract boolean checkIfResultSuccess(Map<String, Object> resultData, Map<String, Object> extraParams);

    /**
     * 当返回结果为 NULL 时的回调函数
     */
    public abstract void onResultNull(String nowKey, Map<String, Object> urlParams, Map<String, Object> extraParams);

    /**
     * 当有返回结果，但结果错误时的回调函数
     */
    public abstract void onResultFail(String nowKey, Map<String, Object> resultData, Map<String, Object> extraParams);

    /**
     * 当有返回结果，且结果正确时的回调函数
     */
    public abstract void onSuccess(Map<String, Object> resultData, Map<String, Object> extraParams);
}
