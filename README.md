# 1 系统介绍

该项目为定时器框架项目，可以实现定时任务的简单配置、手动调用接口执行定时器的功能。

主要功能：

- 通过API调用定时任务
- 管理调用第三方API的方式

# 2 系统集成

## 2.1 maven 安装

```shell
cd /path/to/project # 进入项目目录
cd websocket-forward-utils # 进入工具包目录

# 以下安装方式二选一
# 安装到本地 Maven 仓库（同时安装源码）
mvn source:jar install 
# 安装到本地 Maven 仓库（不安装源码）
mvn install
```

安装完成后，在项目 `pom.xml` 中引入当前项目即可。

```xml
 <dependency>
   <groupId>com.orainge.tools</groupId>
   <artifactId>job-timer</artifactId>
   <version>1.0</version>
 </dependency>
```

## 2.2 配置文件说明

### 2.2.1 系统执行任务配置

- 该配置文件为模板为 `job-comfig.yml`

- 当新加一个需要执行的任务时，需要继承`om.orainge.tools.jobtimer.job.JobBean`实现`doExecute`方法，并将该 JobBean 名称放入配置文件中。

```yaml
job-timer:
  job:
    # 任务 API 控制配置
    api:
      enable: true # 是否开启 API 控制，默认为不开启
      token: abcdefg # 调用的token
      url:
        # JobBean 名称: API 路径
        jobName1: /jobName1
    # 定时配置任务配置
    task:
      enable: false # 是否开启定时任务，默认为不开启
      cron:
        # spring bean 名称: cron 表达式
        jobName1: 0 0/10 * * * ?
```

### 2.2.2 调用第三方API配置

- 该配置文件为模板为 `job-comfig.yml`

- 如果需要开启第三方API调用管理，则需要实现`com.orainge.tools.jobtimer.util.api.ApiUtils`，自定义API管理行为。

```yaml
job-timer:
  # 外部 API 配置
  extra-api:
    config:
      # 接口1
      api1:
        url: https://api.xxx1.com
        need-key: true # 该API是否需要 key 验证
        key-parameter-name: key # Key 参数名称
        keys: key1, key2, key3 # 多个可用的 Key
        retry-times: 3 # 连接失败的重试次数
        api:
          # 第三方接口名称1：第三方接口 URL
          apiName1: /apiname1
      # 接口 2
      api2:
        url: https://api.xxx2.com
        need-key: false
        retry-times: 3 # 连接失败的重试次数
        api:
          apiName1: /apiname1
```

