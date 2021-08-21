package com.orainge.tools.jobtimer.job;

import com.orainge.tools.jobtimer.vo.JobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 定时任务抽象类
 *
 * @author orainge
 * @date 2021/6/10
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

    public JobResult execute() {
        try {
            log.info("[" + name + "] - " + description + ": 开始");
            JobResult execResult = doExecute();
            log.info("[" + name + "] - " + description + ": 结束");
            return execResult;
        } catch (Exception e) {
            log.error("[" + name + "] - " + description + ": 错误", e);
            throw e;
        }
    }

    public abstract JobResult doExecute();

    protected void logInfo(String message, Object... arguments) {
        log.info("[" + name + "] - " + description + ": " + message, arguments);
    }

    protected void logWarn(String message, Object... arguments) {
        log.warn("[" + name + "] - " + description + ": " + message, arguments);
    }

    protected void logError(String message, Object... arguments) {
        log.error("[" + name + "] - " + description + ": " + message, arguments);
    }
}
