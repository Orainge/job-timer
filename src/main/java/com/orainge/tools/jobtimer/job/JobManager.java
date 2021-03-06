package com.orainge.tools.jobtimer.job;

import com.orainge.tools.jobtimer.config.JobConfig;
import com.orainge.tools.jobtimer.util.BeanUtils;
import com.orainge.tools.jobtimer.vo.JobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 定时任务动态配置
 *
 * @author orainge
 * @since 2021/6/10
 */
@Configuration
@EnableScheduling
@ConditionalOnMissingBean({JobManager.class})
public class JobManager implements SchedulingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(JobManager.class);

    @Resource
    private JobConfig jobConfig;

    @Resource
    @Qualifier("taskScheduler")
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Resource
    @Qualifier("asyncExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 指定定时任务的线程池
     */
    @Override
    @SuppressWarnings("all")
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //设定一个长度10的定时任务线程池
        taskRegistrar.setScheduler(threadPoolTaskScheduler);

        JobConfig.JobTaskConfig jobConfigTask = jobConfig.getTask();
        if (jobConfigTask.isEnable()) {
            // 如果开启定时任务，才需要添加执行器
            Map<String, String> cronMap = jobConfigTask.getCron();
            if (cronMap != null && !cronMap.isEmpty()) {
                // 如果配置了定时任务，才开启
                cronMap.forEach((jobName, jobCron) -> {
                    if (!StringUtils.isEmpty(jobCron)) {
                        // 如果 cron 不为空，就配置定时任务
                        taskRegistrar.addTriggerTask(
                                //1. 添加任务内容 (Runnable)
                                () -> threadPoolTaskExecutor.execute(() -> execute(jobName, null)),

                                //2. 设置执行周期 (Trigger)
                                triggerContext -> new CronTrigger(jobCron).nextExecutionTime(triggerContext)
                        );
                    }
                });
            }

            // 初始化完成提示
            log.info("[定时任务管理器] - 定时任务已配置完成");
        }
    }

    /**
     * 获取 Job Class 名称
     *
     * @param jobName Job 名称
     * @return Job Class 名称
     */
    public static boolean isExistJobBean(String jobName) {
        return BeanUtils.containsBean(getJobBeanName(jobName));
    }

    /**
     * 执行任务
     *
     * @param jobName   任务名称
     * @param execParam 执行任务参数
     */
    public static JobResult execute(String jobName, Map<String, String[]> execParam) {
        JobResult jobResult = null;
        try {
            // 获取 JobBean
            JobBean jobBean = BeanUtils.getBean(getJobBeanName(jobName), JobBean.class);

            // 执行任务
            jobResult = jobBean.execute(execParam);

            // 写入任务信息
            jobResult.setJobName(jobBean.getName());
            jobResult.setJobDescription(jobBean.getDescription());

            // 返回任务结果
            return jobResult;
        } catch (Exception e) {
            String errMsg = "[定时任务管理器] - 任务 [" + jobName + "] 出错";
            if (jobResult != null) {
                errMsg += "[任务名称: " + jobResult.getJobName() + ", 任务描述: " + jobResult.getJobDescription() + "]";
            }
            log.error(errMsg, e);

            // 返回错误结果
            return jobResult == null ? JobResult.fail() : jobResult.setSuccess(false);
        }
    }

    /**
     * 根据 JobName 获取在 Springboot 容器中对应的 JonBean 类
     *
     * @param jobName 任务名称
     * @return 对应的 JonBean 类
     */
    private static String getJobBeanName(String jobName) {
        return jobName.substring(0, 1).toLowerCase() + jobName.substring(1) + "Job";
    }
}