package com.orainge.tools.jobtimer.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orainge.tools.jobtimer.job.JobBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 定时任务运行结果
 *
 * @author orainge
 * @date 2021/4/21
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobResult {
    /**
     * 任务是否执行成功
     */
    private boolean success;

    /**
     * 任务执行结果
     */
    private String message;

    /**
     * 任务执行结果描述
     */
    private List<String> messageDescription;

    public boolean isSuccess() {
        return success;
    }

    public JobResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public JobResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public List<String> getMessageDescription() {
        return messageDescription;
    }

    public JobResult setMessageDescription(List<String> messageDescription) {
        this.messageDescription = messageDescription;
        return this;
    }

    public JobResult addMessageDescription(String... messageDescription) {
        if (this.messageDescription == null) {
            this.messageDescription = new LinkedList<>();
        }
        this.messageDescription.addAll(Arrays.asList(messageDescription));
        return this;
    }

    public JobResult addMessageDescription(List<String> messageDescription) {
        if (this.messageDescription == null) {
            this.messageDescription = new LinkedList<>();
        }
        this.messageDescription.addAll(messageDescription);
        return this;
    }


    public static JobResult build() {
        return new JobResult();
    }

    public static JobResult success() {
        return new JobResult().setSuccess(true);
    }

    public static JobResult fail() {
        return new JobResult().setSuccess(false);
    }
}
