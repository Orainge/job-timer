package com.orainge.tools.jobtimer.controller;

import com.orainge.tools.jobtimer.config.JobConfig;
import com.orainge.tools.jobtimer.job.JobManager;
import com.orainge.tools.jobtimer.util.JSONUtils;
import com.orainge.tools.jobtimer.vo.JobResult;
import com.orainge.tools.jobtimer.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 手动执行定时任务 Controller
 *
 * @author orainge
 * @since 2021/6/10
 */
@Controller
@ConditionalOnMissingBean({JobController.class})
public class JobController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    @Resource
    private JSONUtils jsonUtils;

    @Resource
    private JobConfig jobConfig;

    private String token;

    /**
     * 执行任务 API 前缀配置
     */
    private static final String apiPrefix = "/job";

    /**
     * 访问地址 URL - 任务名键值对
     */
    private final Map<String, String> jobTaskUrls = new HashMap<>();

    @PostConstruct
    public void init() {
        // 获取配置 URL
        jobConfig.getApi().getUrl().forEach((key, value) -> jobTaskUrls.put(value, key));

        // 配置 token
        token = jobConfig.getApi().getToken();
    }

    @GetMapping(apiPrefix + "/*")
    @ResponseBody
    public Result task(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestParam(value = "token", required = false) String token) {
        // 检查是否有 token
        if (StringUtils.isEmpty(token) || !this.token.equals(token)) {
            return Result.forbidden().setMessage("无权限访问");
        }

        try {
            if (jobTaskUrls.isEmpty()) {
                // 路径配置文件为空，返回 404
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return Result.notFound();
            } else {
                // 获取访问地址
                String requestUri = request.getRequestURI();
                requestUri = requestUri.substring(requestUri.indexOf(apiPrefix) + apiPrefix.length());

                String jobName = jobTaskUrls.get(requestUri);
                if (StringUtils.isEmpty(jobName)) {
                    // 路径不正确，返回 404
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return Result.notFound();
                } else {
                    // 执行任务
                    // 获取参数
                    Map<String, String[]> requestParam = request.getParameterMap();
                    JobResult execResult = JobManager.execute(jobName, requestParam);

                    // 返回结果
                    log.info("[任务 API 控制器] - 任务通过 API 执行完成: {}", jsonUtils.toJSONString(execResult));
                    return Result.ok().setMessage("任务通过 API 执行完成").setData(execResult);
                }
            }
        } catch (Exception e) {
            log.error("[任务 API 控制器] - 任务通过 API 执行失败", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return Result.ok().setMessage("任务通过 API 执行失败" + ": " + e.getMessage());
        }
    }
}
