package com.orainge.tools.jobtimer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 外部 API 配置文件
 *
 * @author orainge
 * @since 2021/6/10
 */
@Configuration
@ConfigurationProperties(prefix = "job-timer.system")
public class SystemConfig {
    private CheckAliveApiConfig checkAliveApi;

    public CheckAliveApiConfig getCheckAliveApi() {
        return checkAliveApi;
    }

    public void setCheckAliveApi(CheckAliveApiConfig checkAliveApi) {
        this.checkAliveApi = checkAliveApi;
    }

    public static class CheckAliveApiConfig {
        private boolean enable;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }
}
