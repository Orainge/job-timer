package com.orainge.tools.jobtimer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 外部 API 配置文件
 *
 * @author orainge
 * @date 2021/6/10
 */
@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiConfig {
    private Map<String, ApiDetailsConfig> config;

    public Map<String, ApiDetailsConfig> getConfig() {
        return config;
    }

    public void setConfig(Map<String, ApiDetailsConfig> config) {
        this.config = config;
    }

    public static class ApiDetailsConfig {
        private String url;
        private boolean needKey = true;
        private String keyParameterName;
        private List<String> keys;
        private int retryTimes;
        private Map<String, String> api;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isNeedKey() {
            return needKey;
        }

        public String getKeyParameterName() {
            return keyParameterName;
        }

        public void setKeyParameterName(String keyParameterName) {
            this.keyParameterName = keyParameterName;
        }

        public void setNeedKey(boolean needKey) {
            this.needKey = needKey;
        }

        public List<String> getKeys() {
            return keys;
        }

        public void setKeys(List<String> keys) {
            this.keys = keys;
        }

        public int getRetryTimes() {
            return retryTimes;
        }

        public void setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
        }

        public Map<String, String> getApi() {
            return api;
        }

        public void setApi(Map<String, String> api) {
            this.api = api;
        }
    }
}
