package com.orainge.tools.jobtimer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定时任务多线程配置
 *
 * @author orainge
 * @since 2021/6/10
 */
@Configuration
@EnableScheduling
@ConditionalOnMissingBean({ScheduleExecutorConfig.class})
public class ScheduleExecutorConfig implements SchedulingConfigurer {
    @Resource
    private JobConfig jobConfig;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        //参数传入一个size为10的线程池
        scheduledTaskRegistrar.setScheduler(taskScheduler());
    }

    /**
     * 定时任务使用的同步线程池
     */
    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        JobConfig.JobSchedulerConfig config = jobConfig.getMultiThread().getScheduler();
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix(config.getThreadNamePrefix());
        scheduler.setPoolSize(config.getPoolSize());
        scheduler.setWaitForTasksToCompleteOnShutdown(config.isWaitForTasksToCompleteOnShutdown());
        scheduler.setAwaitTerminationSeconds(config.getAwaitTerminationSeconds());
        return scheduler;
    }

    /**
     * 定时任务使用的异步执行线程池
     */
    @Bean(name = "asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        JobConfig.JobExecutorConfig config = jobConfig.getMultiThread().getExecutor();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}