package com.orainge.tools.jobtimer.controller;

import com.orainge.tools.jobtimer.config.SystemConfig;
import com.orainge.tools.jobtimer.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 检测系统是否正常运行 Controller
 *
 * @author orainge
 * @date 2021/8/22
 */
@Controller
@ConditionalOnMissingBean({CheckAliveController.class})
public class CheckAliveController {
    private static final Logger log = LoggerFactory.getLogger(CheckAliveController.class);

    @Resource
    private SystemConfig systemConfig;

    private boolean enable = false;

    @PostConstruct
    public void init() {
        enable = systemConfig.getCheckAliveApiConfig().isEnable();

        if (enable) {
            log.info("[系统运行状态检测 API] - 已开启");
        } else {
            log.info("[系统运行状态检测 API] - 已关闭");
        }
    }

    @GetMapping("/checkAlive")
    @ResponseBody
    public Result checkAliveApi(HttpServletRequest request, HttpServletResponse response) {
        if (enable) {
            return checkAlive();
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
    }

    /**
     * 当访问检测 API 时执行的方法
     *
     * @return 返回结果
     */
    public Result checkAlive() {
        return Result.ok().setMessage("系统运行正常");
    }
}
