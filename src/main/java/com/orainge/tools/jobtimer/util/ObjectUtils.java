package com.orainge.tools.jobtimer.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * 对象工具类
 *
 * @author orainge
 * @date 2021/6/10
 */
@Component
@ConditionalOnMissingBean({ObjectUtils.class})
public class ObjectUtils {
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

    /**
     * 根据属性名获取属性值
     *
     * @param obj       要获取属性值的对象
     * @param fieldName 字段名
     * @author orainge
     * @date 2021/6/10
     */
    public Object getFieldValueByName(Object obj, String fieldName) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = obj.getClass().getMethod(getter);
            return method.invoke(obj);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 根据属性名获取属性值
     *
     * @param obj       要获取属性值的对象
     * @param fieldName 字段名
     * @author orainge
     * @date 2021/6/10
     */
    @SuppressWarnings("all")
    public <T> T getFieldValueByName(Object obj, String fieldName, Class<T> clazz) {
        try {
            Object o = getFieldValueByName(obj, fieldName);
            if (Objects.isNull(o)) {
                return null;
            } else {
                return (T) o;
            }
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * map 对象转 object
     */
    public <T> T mapToObject(Map<String, Object> map, Class<T> beanClass) {
        if (map == null) {
            return null;
        }

        try {
            return objectMapper.convertValue(map, beanClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * object 对象转 Map
     */
    public Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * string 对象转 Map
     */
    public Map<String, Object> stringToMap(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        try {
            return objectMapper.convertValue(str, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * object 转 string
     */
    public String objectToString(Object object) {
        if (Objects.isNull(object)) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * object 转 object
     */
    public <T> T objectToObject(Object obj, Class<T> beanClass) {
        if (Objects.isNull(obj)) {
            return null;
        }

        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(obj), beanClass);
        } catch (Exception e) {
            return null;
        }
    }
}
