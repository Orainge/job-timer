package com.orainge.tools.jobtimer.util.key;

import com.orainge.tools.jobtimer.config.ApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * API KEY 管理器
 *
 * @author orainge
 * @since 2021/6/10
 */
public abstract class ApiKeyManager {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyManager.class);

    /**
     * key 管理器名称
     */
    private final String name;

    /**
     * key 参数名称
     */
    private String keyParameterName;

    /**
     * key 列表
     */
    private List<String> keys = new ArrayList<>();

    /**
     * key 大小
     */
    private int size = 0;

    /**
     * key 索引
     */
    private int index = 0;

    /**
     * key 过期标志
     */
    private Boolean[] keyIsExpire = null;

    private final LocalTime START_TIME = LocalTime.of(23, 59, 58);
    private final LocalTime END_TIME = LocalTime.of(0, 0, 10);

    public ApiKeyManager(String name) {
        if (StringUtils.isEmpty(name)) {
            name = this.getClass().getSimpleName();
        }
        this.name = name;
    }

    /**
     * 获取 Key 在 URL 中的参数名
     */
    public String getKeyParameterName() {
        return keyParameterName;
    }

    /**
     * 获取可用的key
     *
     * @return key: 可用的key; null: 今天配额已经用完，无可用key
     */
    public synchronized String getKey() {
        if (index == size) {
            // 今天配额已经用完，无可用key
            return null;
        } else {
            // 返回可用的key
            return keys.get(index);
        }
    }

    /**
     * 标记该 key 已经过期
     */
    public synchronized void setExpire(String key) {
        LocalTime nowTime = LocalTime.now();
        // 判断当前时间是否为两天时间的交界
        if (START_TIME.compareTo(nowTime) <= 0 && nowTime.compareTo(END_TIME) <= 0) {
            // 等待两秒后返回，继续使用该 Key
            try {
                Thread.sleep(2000);
            } catch (Exception ignore) {
            }
            return;
        }

        // 查找第几个 Key 过期
        boolean isKeyExpire = false;

        for (int i = 0; i < size; i++) {
            if (keys.get(i).equals(key)) {
                keyIsExpire[i] = true;
                isKeyExpire = true;
                break;
            }
        }

        if (!isKeyExpire) {
            throw new RuntimeException("[" + this.name + " Key 管理器] - key [" + key + "] 不存在，无法设置过期标志");
        }

        // 查找最早未过期的 Key
        int tempIndex = -1;
        for (int i = 0; i < size; i++) {
            if (!keyIsExpire[i]) {
                tempIndex = i;
                break;
            }
        }

        if (tempIndex == -1) {
            // 所有的 Key 都已经过期
            index = size;
        } else {
            // 有未过期的 key
            index = tempIndex;
        }
    }

    /**
     * 重置 Key 使用情况<br>
     * 每天晚上23点59分56秒执行一次
     */
    @Async("asyncExecutor")
    @Scheduled(cron = "56 59 23 * * ?")
    public synchronized void reset() {
        index = 0;
        for (int i = 0; i < size; i++) {
            keyIsExpire[i] = false;
        }
        log.info("[" + this.name + " Key 管理器] - Key 已重置");
    }

    /**
     * 初始化方法<br/>
     * 需要在实现类中用带 @PostConstruct 注解的方法执行
     *
     * @param apiConfig API 配置文件类
     * @param name      配置文件中该 Key 的键名
     */
    protected void initMethod(ApiConfig apiConfig, String name) {
        // 从配置文件读取该 API 的配置文件
        ApiConfig.ApiDetailsConfig config = apiConfig.getConfig().get(name);
        if (config == null) {
            throw new NullPointerException("[" + this.name + " Key 管理器] - 配置文件读取错误，请检查配置是否正确");
        }

        keyParameterName = config.getKeyParameterName();

        if (StringUtils.isEmpty(keyParameterName)) {
            throw new NullPointerException("[" + this.name + " Key 管理器] - 没有设置 Key 参数名，请检查配置是否正确");
        }

        keys = config.getKeys();

        if (Objects.isNull(keys) || keys.size() == 0) {
            throw new NullPointerException("[" + this.name + " Key 管理器] - 没有可用的 Key，请检查配置是否正确");
        }

        size = keys.size();
        keyIsExpire = new Boolean[size];
        for (int i = 0; i < size; i++) {
            keyIsExpire[i] = false;
        }

        log.info("[" + this.name + " Key 管理器] - Key 初始化完成");
    }
}
