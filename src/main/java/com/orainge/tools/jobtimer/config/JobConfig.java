package com.orainge.tools.jobtimer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 定时任务 API 配置文件
 *
 * @author orainge
 * @since 2021/6/10
 */
@Configuration
@ConfigurationProperties(prefix = "job-timer.job")
public class JobConfig {
    private static final Logger log = LoggerFactory.getLogger(JobConfig.class);

    /**
     * API 管理配置
     */
    private JobApiConfig api;

    /**
     * 定时任务配置
     */
    private JobTaskConfig task;

    /**
     * 线程池配置
     */
    private JobMultiThreadConfig multiThread;

    @PostConstruct
    public void init() {
        // 配置定时任务和 API 的地址
        initApiAndTask();

        // 配置线程池
        initMultiThread();

        // 返回配置结果
        initResult();
    }

    public static class JobApiConfig {
        /**
         * 是否启用 API 管理
         */
        private boolean enable;

        /**
         * token
         */
        private String token;

        /**
         * 每个定时任务的 API 地址
         */
        private Map<String, String> url;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Map<String, String> getUrl() {
            return url;
        }

        public void setUrl(Map<String, String> url) {
            this.url = url;
        }
    }

    public static class JobTaskConfig {
        /**
         * 是否启用定时任务
         */
        private boolean enable;

        /**
         * 定时任务 cron 表达式
         */
        private Map<String, String> cron;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public Map<String, String> getCron() {
            return cron;
        }

        public void setCron(Map<String, String> cron) {
            this.cron = cron;
        }
    }

    public static class JobMultiThreadConfig {
        /**
         * 同步线程池配置
         */
        private JobSchedulerConfig scheduler;

        /**
         * 异步执行线程池
         */
        private JobExecutorConfig executor;

        public JobSchedulerConfig getScheduler() {
            return scheduler;
        }

        public void setScheduler(JobSchedulerConfig scheduler) {
            this.scheduler = scheduler;
        }

        public JobExecutorConfig getExecutor() {
            return executor;
        }

        public void setExecutor(JobExecutorConfig executor) {
            this.executor = executor;
        }
    }

    public static class JobSchedulerConfig {
        /**
         * 线程名称前缀
         */
        private String threadNamePrefix;

        /**
         * 线程池大小
         */
        private int poolSize;

        /**
         * 项目停止时是否等待线程完成任务
         */
        private boolean waitForTasksToCompleteOnShutdown;

        /**
         * 项目停止时等待线程池完成的时间<br/>
         * 仅当 waitForTasksToCompleteOnShutdown 设置为 ture 时使用
         */
        private int awaitTerminationSeconds;

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }

        public boolean isWaitForTasksToCompleteOnShutdown() {
            return waitForTasksToCompleteOnShutdown;
        }

        public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
            this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        }

        public int getAwaitTerminationSeconds() {
            return awaitTerminationSeconds;
        }

        public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
            this.awaitTerminationSeconds = awaitTerminationSeconds;
        }
    }

    public static class JobExecutorConfig {
        /**
         * 线程名称前缀
         */
        private String threadNamePrefix;

        /**
         * 线程池大小
         */
        private int maxPoolSize;

        /**
         * 线程池核心线程数
         */
        private int corePoolSize;

        /**
         * 线程池队列长度
         */
        private int queueCapacity;

        /**
         * 核心线程存在时间（单位：秒）
         */
        private int keepAliveSeconds;

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }
    }

    private void initApiAndTask() {
    }

    private void initMultiThread() {
        // 修改线程池的名称
        String a = multiThread.scheduler.threadNamePrefix;
        String b = multiThread.executor.threadNamePrefix;

        if (a.lastIndexOf("-") != a.length() - 1) {
            a += "-";
            multiThread.scheduler.threadNamePrefix = a;
        }

        if (b.lastIndexOf("-") != b.length() - 1) {
            b += "-";
            multiThread.executor.threadNamePrefix = b;
        }
    }

    private void initResult() {
        if (api.isEnable()) {
            log.info("[定时任务配置工具] - 定时任务 API 控制已开启");
        } else {
            log.warn("[定时任务配置工具] - 定时任务 API 控制已关闭");
        }

        if (task.isEnable()) {
            log.info("[定时任务配置工具] - 定时任务已开启");
        } else {
            log.warn("[定时任务配置工具] - 定时任务已关闭");
        }
    }

    public JobApiConfig getApi() {
        return api;
    }

    public void setApi(JobApiConfig api) {
        this.api = api;
    }

    public JobTaskConfig getTask() {
        return task;
    }

    public void setTask(JobTaskConfig task) {
        this.task = task;
    }

    public JobMultiThreadConfig getMultiThread() {
        return multiThread;
    }

    public void setMultiThread(JobMultiThreadConfig multiThread) {
        this.multiThread = multiThread;
    }
}
