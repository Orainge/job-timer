package com.orainge.tools.jobtimer.job;

import com.orainge.tools.jobtimer.vo.JobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 定时任务抽象类
 *
 * @author orainge
 * @since 2021/6/10
 */
public abstract class JobBean {
    private static Logger log = LoggerFactory.getLogger(JobBean.class);

    private final String name;
    private final String description;

    public JobBean(String name, String description) {
        log = LoggerFactory.getLogger(this.getClass());

        if (StringUtils.isEmpty(name)) {
            this.name = JobBean.class.getSimpleName();
        } else {
            this.name = name;
        }

        if (StringUtils.isEmpty(description)) {
            this.description = "定时任务";
        } else {
            this.description = description;
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 执行任务
     *
     * @param execParam 执行任务的参数
     * @return 执行结果
     */
    public JobResult execute(Map<String, String[]> execParam) {
        try {
            log.info("[" + name + "] - " + description + ": 开始");
            JobResult execResult = doExecute(execParam);
            log.info("[" + name + "] - " + description + ": 结束");
            return execResult;
        } catch (Exception e) {
            log.error("[" + name + "] - " + description + ": 错误", e);
            throw e;
        }
    }

    /**
     * 执行任务<br>
     * 需要重写具体的执行方法
     *
     * @param execParam 执行参数
     * @return 执行结果
     */
    public abstract JobResult doExecute(Map<String, String[]> execParam);

    protected void logInfo(String message, Object... arguments) {
        log.info("[" + name + "] - " + description + ": " + message, arguments);
    }

    protected void logWarn(String message, Object... arguments) {
        log.warn("[" + name + "] - " + description + ": " + message, arguments);
    }

    protected void logError(String message, Object... arguments) {
        log.error("[" + name + "] - " + description + ": " + message, arguments);
    }

    protected void logError(String message, Throwable e) {
        log.error("[" + name + "] - " + description, e);
    }
}
