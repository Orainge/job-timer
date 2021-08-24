package com.orainge.tools.jobtimer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

/**
 * JSON 工具类
 *
 * @author orainge
 * @since 2021/4/21
 */
@Component
@ConditionalOnMissingBean(JSONUtils.class)
public class JSONUtils {
    @Resource
    private ObjectMapper objectMapperBean;

    private static ObjectMapper objectMapper = null;

    @PostConstruct
    public void init() {
        objectMapper = objectMapperBean;
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    public String toJSONString(Object obj) {
        try {
            return obj == null ? null : objectMapper.writeValueAsString(obj);
        } catch (Exception ignore) {
            return null;
        }
    }

    public <T> T parseObject(String text, Class<T> clazz) {
        try {
            return StringUtils.isEmpty(text) || clazz == null ? null : objectMapper.readValue(text, clazz);
        } catch (Exception ignore) {
            return null;
        }
    }

    public Map<String, Object> parseObjectToMap(String text) {
        try {
            return StringUtils.isEmpty(text) ? null : objectMapper.readValue(text, ModelMap.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
