package com.orainge.tools.jobtimer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * RestTemplate 配置文件，兼容 http 和 https
 *
 * @author orainge
 * @date 2021/6/10
 */
@Configuration
@ConditionalOnMissingBean({RestTemplateConfig.class})
public class RestTemplateConfig {
    private final int connectTimeout = 60; // 单位：秒
    private final int readTimeout = 10; // 单位：秒

    @Bean("defaultRestTemplate")
    public RestTemplate defaultRestTemplate() {
        return buildRestTemplate(readTimeout);
    }

    @Bean("noReadTimeoutRestTemplate")
    public RestTemplate noReadTimeoutRestTemplate() {
        return buildRestTemplate(0);
    }

    private RestTemplate buildRestTemplate(int readTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout * 1000);
        factory.setConnectTimeout(connectTimeout * 1000);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8)); // 支持中文编码
        return restTemplate;
    }
}