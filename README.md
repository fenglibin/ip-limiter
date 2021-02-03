# ip-limiter

####  **一、 IP限流平台介绍** 

 **1. 为什么要做IP限流** 

网络世界中，真实的用户与爬虫机器人等混杂在一起，正常的请求与异常请求也是相互交织，做为服务的提供方都希望请求来自于真实的用户的真实请求，这么才能够达到成本收益的最大化，但是总会有一些人来非法尝试获取提供方的信息，爬取有价值的数据供自己使用。作为数据的拥有者，肯定不希望自己的数据被非常利用，因为就需要做一些防御措施，达到数据保护的目的，IP限流就是其中一种比较有效的方案。因为通常用于爬取数据的爬虫机器人，都是在一台或者固定的几台服务器上执行的数据爬取操作（当然他可以购买更多的动态，只是会花更多的成本），针对这种访问量异常的ＩＰ进行记录和跟踪，然后就可以确认来自这些ＩＰ的请求是不是真实的用户请求了。

![输入图片说明](https://images.gitee.com/uploads/images/2020/1230/174416_6a9ef67b_306225.jpeg "b5acae60b9ae68da3938c84103187cf2.jpeg")

 **2. 要达到的目标** 
- 监控ＩＰ的访问行为，将其访问量及访问行为进行记录（完成）；
- 支持手动将单个IP、IP段加入到黑名单中，并对加入到黑名单中的IP进行过滤（完成）；
- 支持手动将单个IP、IP段加入到白名单中，并对加入到白名单中的IP进行过滤（完成）；
- 在基于一定的规则下，能够自动将异常访问的ＩＰ加入的ＩＰ黑名单中（完成）；
- 并对异常访问的ＩＰ进行告警，并支持应用自定义IP黑名单发现规则及告警实现（待实现）；
- 支持针对集群应用进行汇总统计（完成）；
- 高效的数据统计、存储及读取，减少对应用效率的影响，目前存在Redis中（完成）；
- 支持对规则不同存储的扩展支持，如可将规则持久化到DB中、ZK中等，实现已经抽象化，Redis的已经完成，其它存储逻辑可根据情况自定义（完成）；
- 能够支持按分纬度及秒纬度对访问的ＩＰ进行统计（完成）；
- 有一定的管理界面，能够对黑名单及TOP IP的访问情况进行查询及操作（完成）；
- 支持将统计数据输出到不同的存储，如Redis、Influxdb等，实现已经抽象化，Redis的已经完成，其它存储逻辑可根据情况自定义（完成）；
- 支持多平台对访问数据进行呈现，如以后数据可以在Grafana中呈现，目前有基本的管理界面，更精细的管理界面及Grafana中展示数据后期再考虑实现（部分完成）；
- 支持对ＩＰ及ＩＰ段访问最高TPS进行统一配置（完成），也支持对单个ＩＰ及ＩＰ段进行访问控制（完成）；
- 多应用集群统一流量管控平台，可管理所有接入的应用的访问情况（完成）；


####  **二、架构** 
 **1. 交互架构** 

![ＩＰ限流平台交互架构](https://images.gitee.com/uploads/images/2020/1231/181209_ad1ef1db_306225.png "962ebe8c87cfb79393e4faaa7e50b47a (1).png")

 **2. 实现架构** 

![IP限流平台架构图](https://images.gitee.com/uploads/images/2020/1230/174256_2b6b8f8c_306225.png "20201111144948719 (1).png")

 **3. 系统模块** 

- 收集访问纬度数据的Agent模块，需要应用集成；
- 用于查看及管理的控制台模块，控制台模块为Master/Slave架构，Master用于执行定时任务、对节点进行检查、检查系统收集的IP黑名单情况等；

####  **三、设计原则** 
 **1. 高性能** 
- 访问纬度数据的统计、过滤及收集，必须是高效的，并且尽可能的减少由于这些纬度数据的收集给集成应用带来的性能损耗；
- 访问纬度数据收集时，尽可能的采用全内存操作，减少或避免本地ＩＯ及远程ＩＯ的操作；
- 访问纬度数据的上报、从远程同步数据，使用异常操作；

 **2. 高可用** 
- 当IP限流系统的控制台不可用时，不能够影响到集成访问纬度数据的统计的应用端；
- 对集成访问纬度统计的应用定期健康检查，确保其可用性；
- IP限流系统的控制台支持水平扩展，且无状态；

 **3. 高可扩展** 
- 纬度数据的存储支持扩展，可以根据实际情况支持不同的后端存储结构，默认支持Redis，可扩展为支持MySQL、Elasticsearch、Influxdb等；
- 对超限访问ＩＰ处理规则的可扩展性，同时支持在控制台及集成客户端进行扩展，且提供默认的处理规则；

 **4. 简单易用** 
- 集成简单，尽可能的除了在pom.xml中引入Jar包以外，不需要做其它任何的操作；
- 使用简单，提供基本的管理界面给用户，可对IP的访问情况、IP白名单、IP黑白单、QPS设置等进行管理和查看；

####  **四、存储设计** 
系统默认设计只会保存最多一个小时的访问纬度数据，且每个应用的数据是存放在单的hashkey中的，因而单个应用数据量不大，且了访问上的高效性，此处就默认使用Redis做为存储，也支持扩展为使用其它的存储。

 **1. black-ips** 

用于存储全局黑名单IP的Key，存储结构为Hash，HashKey为IP，HashValue为对象com.eeefff.limiter.common.vo.BlackIpVO，其定义的字段如下：

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/181916_7ec06ae2_306225.png "c7f707069f4ee8cba555071afc437162.png")

注：

每个应用的IP黑名单数据都会单独存储到不同的Key中，其命名规则为"应用名称-black-ips"，如IP限制平台控制台的IP黑名单数据，保存的Key为：ip-limiter-dashboard-black-ips；

 **2. white-ips** 

用于存储全局白名单IP的Key，存储结构为Hash，HashKey为IP，HashValue为对象com.eeefff.limiter.common.vo.WhiteIpVO，其定义的字段如下：

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/181935_423ed591_306225.png "8e94d98285f75dbcee6b5dbfd1daa1a9.png")

注：

每个应用的IP白名单数据都会单独存储到不同的Key中，其命名规则为"应用名称-white-ips"，如IP限制平台控制台的IP白名单数据，保存的Key为：ip-limiter-dashboard-white-ips；

 **3. minute-access** 

用于存储所有接入了IP限流平台的应用客户端的每分钟的访问统计汇总的Key，存储结构为Hash，HashKey为代表访问的分钟，HashValue为对象List，com.eeefff.limiter.common.vo.AccessVO定义的字段如下：

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/181952_2768bfae_306225.png "f9629ac5adb233a90a82686825374848.png")

注：

每个应用的分钟访问纬度数据都会单独存储到不同的Key中，其命名规则为"应用名称-minute-access"，如IP限制平台控制台的分钟访问纬度数据，保存的Key为：ip-limiter-dashboard-minute-access；

每个应用每个节点的的分钟访问纬度数据都会单独存储到不同的Key中，其命名规则为"应用名称-ip及端口-minute-access"，如IP限制平台控制台的分钟访问纬度数据，保存的Key为：ip-limiter-dashboard-127.0.0.1:20520-minute-access；

 **4. ip-limit** 

用于存储全局IP QPS设置的Key，存储结构为Hash，HashKey为IP，HashValue为对象com.eeefff.limiter.common.vo.IpLimitVO，其定义的字段如下：

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/182017_514cc4cc_306225.png "bb44e9662ec695a2632ea2d538a17240.png")

注：

每个应用的IP QPS设置数据都会单独存储到不同的Key中，其命名规则为"应用名称-ip-limit"，如IP限制平台控制台的IP白名单数据，保存的Key为：ip-limiter-dashboard-ip-limit；

 **5. registered-clients** 

用于存储注册到IP限流平台管理控制台的Key，存储结构为Hash，HashKey为应用名称，HashValue为对象Map，用于存储该应用所有注册的客户端，Map的Key为应用客户端的IP+端口，com.eeefff.limiter.common.vo.Client对定义的字段如下：

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/182034_dc06f189_306225.png "46b585047d9f13b93bdbb3a044fc789c.png")

 **6. ip-limiter-dashboard-master** 

用于存储IP限流平台的Master节点的Key，存储结构为普通的字符串，其值为当前master节点的IP+端口。

 **7. permitsPerSecondEachIp** 

用于存储每个ＩＰ默认的最大ＱＰＳ值的Key，存储结构为普通的数字，其值为当每个ＩＰ默认的最大ＱＰＳ值。

注：

每个应用都会单独存储IP默认值最大ＱＰＳ值，存储结构为普通的数字，其值为当前应用每个ＩＰ默认的最大ＱＰＳ值，存储每个应用默认访问QPS的Redis的Key命名规则为“应用名称-permitsPerSecondEachIp”，如ip-limiter-dashboard的key为"ip-limiter-dashboard-permitsPerSecondEachIp"。

####  **五、系统设计** 
 **1. IP限流Agent** 

该Agent用于集成到应用端，提供了一些方便性的功能，如IP访问数据的收集与上报、从远程控制台获取ＩＰ黑/白名单等；

每个节点本地默认保留60份以1分钟为纬度的访问统计数据，即保存60分钟以分钟为纬度统计的访问数据，默认保留60份以秒为统计纬度的访问数据，这都可以根据相应的配置项进行调整。

 **１）功能模块** 

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192521_c5de6140_306225.png "56e31f7c1a610c65d3716ab4ca9bce29.png")

 **２）限流算法** 

常用的限流算法有以下四种：
- 计数器（固定窗口）算法
- 滑动窗口算法
- 漏桶算法
- 令牌桶算法
- 各种算法的优缺点，可以[查看这里](https://blog.csdn.net/weixin_41846320/article/details/95941361)。

选择的算法为计数器（固定窗口）算法，因为我们的应用场景为单个ＩＰ的限流，而不是针对所有请求限流，且该算法实现简单，能够满足一定的突发情况的处理，该算法的实现类为com.eeefff.limiter.core.interceptor.IpQpsRateLimiter。

 **３）核心功能** 
IP流控请求合法性检查：确保IP的访问不会超过QPS的限制，如果是黑名单的IP直接拒绝访问，确保系统的安全性

本地访问数据的上报：目前只是以分纬度主动上报了每分钟IP的访问统计数据

从远程更新IP黑白名单及QPS限制配置：更新到的配置会存放在本地缓存中，以确保本地检查合法性访问的高效性

提供秒纬度访问统计接口：远程控制台可以调用本地以秒纬度统计的实时QPS，更方便的了解当前应用的运行情况

 **４）核心流程** 

请求合法性检查

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192701_7aae7c1b_306225.png "f2867a56a7dea94cfd3e268f75d1fffe.png")

注：IP白名单、IP黑名单及IP的QPS都是在本地内存或本地缓存中，没有远程的ＩＯ操作，因而该校验过程仅会对整体性能产生非常微弱的影响。

请求合法性检查　子逻辑之　IP白名单校验逻辑

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192727_a94786bb_306225.png "52ff9b97c945d1b5c2a4fced9a3afd84.png")

请求合法性检查　子逻辑之　IP黑名单校验逻辑

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192745_f20b2315_306225.png "f958b4e68cbb732547ba22835694e352.png")

请求合法性检查　子逻辑之　IP QPS超限访问校验及增加IP的访问量

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192801_22d14474_306225.png "d0b4135bb8d467d0f7b78182e559c9cf.png")

请求合法性检查　子逻辑之　IP QPS超限访问校验及增加IP的访问量　子逻辑之　当前IP是否超限访问的判断

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192818_071c9742_306225.png "160917b37474761b2ae164b0ff8c36ae.png")

从远程更新配置

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192836_f4538952_306225.png "d84659ecd64dc64128f4faa5648ddda2.png")

 **５）核心配置** 

```
#IP Limiter控制台的地址
ip.limiter.core.dashboardAddress = 127.0.0.1:8080
#当前服务器的IP地址
ip.limiter.core.serverAddress = [不配置则自动获取，如果指定则例用指定值]
#本地缓存中保留多少份以秒纬度统计的数据，单位为秒
ip.limiter.core.secondsMetricLocalKeeped = 60
#本地应用最多保留多少份以分钟为单位的统计，如值为60时，则表示本地会保留60份以分钟为统计纬度统计的访问最多的Ip，也可以理解为保留60分钟每分钟访问最多的Ip的统计
ip.limiter.core.maxTopAccessMinutes = 60
#IP的QPS限制配置及IP黑/白名单从远程控制台更新时间间隔，单位为毫秒
ip.limiter.core.ipQpsLimitAndBlackIpUpdateTimeInterval = 10000
```

 **2. IP限流控制台** 

 **１）功能模块** 

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/192914_f3beff84_306225.png "77843af00c6b06f0b67677da6753fead.png")

 **２）核心功能** 

 **a）超限访问IP自动限制访问** 

针对超限访问的IP，会临时将其加入到黑名单中，以临时阻止其对系统的访问。

限制分为四个等级：

- 限制访问一分钟
- 限制访问一小时
- 限制访问二十四小时
- 限制永久访问

限制规则及限制升级规则：

- 针对首次访问超限的IP，会将其加到黑名单中，并限制其访问一分钟，如果一分钟后没有超限访问的行为，则将解除其访问限制；
- 针对被限制访问一分钟的IP，如果在一分钟后解除其访问限制后，还有继续超限访问的行为，则会将其访问限制升级为限制访问一小时，如果没有系统则会自动将其从黑名单中移除；
- 针对被限制访问一小时的IP，如果在一小时后解除其访问限制后，还有继续超限访问的行为，则会将其访问限制升级为限制访问一天，如果没有系统则会自动将其从黑名单中移除；
- 针对被限制访问二十四小时的IP，如果在二十四小时后解除其访问限制后，还有继续超限访问的行为，则会将其访问限制升级为限制记久访问，如果没有系统则会自动将其从黑名单中移除；

 **b）IP黑/白名单的管理** 

支持针对指定应用设置IP黑/白名单，生效范围为指定的应用，也支持从全局上设置IP黑/白名单，应用到所有应用中，管理功能包括IP黑/白名单增加、删除、查询的操作。

 **c）IP的最大QPS管理** 

IP的最大QPS设置分为以下几块内容：

- 针对指定应用指定IP的最大QPS进行设置，判断IP是否超限访问最优先适用的规则；
- 针对指定应用所有默认IP的最大QPS进行设置，这个设置适用于访问该应用所有未指定最大QPS的IP，判断IP是否超限访问次优先适用的规则；
- 针对所有应用默认IP的最大QPS进行设置，判断IP是否超限访问最后适用的规则；
- 满足以上任何规则，都认为该IP是超限访问IP。

 **d）注册客户端健康检查** 

定期对注册到控制台中所有的应用及每个应用的所有节点进行健康检查，检查逻辑如下：

- 应用的某个节点连续三次都检查不成功，那该节点就会从该应用删除掉；
- 应用的所有节点都检查不成功，或者该应用本身已经没有了任何可用的节点，那该应用就会被删除；

 **e）系统对自动增加为黑名单的ＩＰ进行自检** 

- 针对首次访问超限的IP，如果一分钟后没有超限访问的行为，则将解除其访问限制，系统会将其从黑名单中删除；
- 针对被限制访问一分钟的IP，如果在一分钟解除其访问限制后，没有继续超限访问的行为，系统会自动将其从黑名单中移除；
- 针对被限制访问一小时的IP，如果在一小时解除其访问限制后，没有继续超限访问的行为，系统会自动将其从黑名单中移除；
- 针对被限制访问二十四小时的IP，如果在二十四小时解除其访问限制后，没有继续超限访问的行为，系统会自动将其从黑名单中移除；

 **f）查询应用的实时访问情况** 

在控制台查询指定应用端节点的实时ＩＰ访问情况。

 **３）核心流程** 

 **a）注册客户端健康检查** 

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193241_ca4293f8_306225.png "50ff84418f6bb663a169c05d1b5002ec (1).png")

 **b）系统对自动增加为黑名单的ＩＰ进行自检** 

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193256_68332d73_306225.png "20201110164919389.png")

 **４）核心配置** 

```
spring.boot.enableautoconfiguration=true
#服务的端口号
server.port = 8080
spring.application.name = ip-limiter-dashboard

#以下是Apollo需要的相关配置参数，如果使用Apollo做为配置中心，在Apollo中创建名为ip-limiter-dashboard的项目
#通过Apollo启动，命令行要带对应的参数，如：
#-Dspring.profiles.active=DEV -Dapollo.meta=http://apollo.dev.xxx.com:8072
##该参数通过命令行传入，通过启动命令传入该参数，以便于支持多个不同的环境
##active.env= DEV
## 将 Apollo 配置加载提到初始化日志系统之前，需要托管日志配置时开启
#apollo.bootstrap.eagerLoad.enabled = true
## 应用全局唯一的身份标识
#app.id = ip-limiter-dashboard
## Apollo Meta Server 地址，通过启动命令传入该参数，以便于支持多个不同的环境
##apollo.meta = http://apollo.dev.xxx.com:8072
## 自定义本地配置文件缓存路径
#apollo.cacheDir = ./config
## 设置在应用启动阶段就加载 Apollo 配置
#apollo.bootstrap.enabled = true
## 注入 application namespace
#apollo.bootstrap.namespaces = application

#如果使用Apollo做为配置中心，将以下配置拷贝到Apollo中即可，并将其在当前配置文件中注释掉
ip.limiter.dashboard.permitsPerSecondEachIp = 50
ip.limiter.dashboard.maxTopAccessIps = 10
ip.limiter.dashboard.maxRedisTopAccessIps = 50
ip.limiter.dashboard.globalMaxRedisTopAccessIps = 50
ip.limiter.dashboard.maxRedisTopAccessMinutes = 60
ip.limiter.dashboard.globalMaxRedisTopAccessMinutes = 60
ip.limiter.dashboard.redisLockMaxWaitMillis = 60000
ip.limiter.dashboard.maxTopAccessMinutes = 30
ip.limiter.dashboard.connectTimeout = 5000
ip.limiter.dashboard.soTimeout = 5000
ip.limiter.dashboard.maxConnTotal = 100
ip.limiter.dashboard.maxConnPerRoute = 10
ip.limiter.dashboard.maxHttpRetryTimes = 5
ip.limiter.dashboard.httpRetryIntervalTime = 20
ip.limiter.dashboard.appClientHealthCheckRate = 1
ip.limiter.dashboard.systemAddBlackIpCheckRate = 1

spring.redis.host = 192.168.12.111
spring.redis.port = 6379
spring.redis.password = 8lFvrZh7d7Ik8LtNwpBMakleishen
spring.redis.database = 12
spring.redis.timeout = 5000
spring.redis.jedis.pool.max-idle = 8
spring.redis.jedis.pool.min-idle = 0
spring.redis.jedis.pool.max-wait = 8
spring.redis.jedis.pool.max-active = 20
ip.limiter.core.dashboardAddress = 127.0.0.1:8080
```

 **５）功能界面** 
（单个应用）分纬度访问TOP统计

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193507_d63e55c5_306225.png "6729de9401e57c8afbb8fec1f6198a10.png")

（单个应用）秒纬度访问TOP统计

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193523_326b2022_306225.png "48c6fcd3c4af4e47d0393147db3f946a.png")

（单个应用）IP白名单设置

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193539_50d60a02_306225.png "06bfb57641b2ebeed98edeffeeccd4ad.png")

（单个应用）IP黑名单设置

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193559_5b0621d8_306225.png "2063f4d2c3710412cb79706f85d0e5a8.png")

（单个应用）单个IP的QPS设置

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193616_95f44d28_306225.png "29128d757f06d0c00bb3c077291e5b54.png")

（分局）分纬度访问TOP统计

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193634_d4283692_306225.png "f479e4ca42c727eb0246e34d1b68997a.png")

（全局）黑名单设置

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193649_2279abf7_306225.png "e995bda907c3fbd1936af7d11f58f548.png")

（全局）白名单设置

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193706_9e08f76f_306225.png "95d541b0b4e8487d8a1235887f435c5a.png")

（全局）单个IP的QPS设置

![输入图片说明](https://images.gitee.com/uploads/images/2020/1231/193715_0dbf879a_306225.png "585204121c8ec2c6d1a540d595858168.png")

####  **六、IP限流平台的搭建** 
 **1. 下载源码并安排** 

```
git clone https://gitee.com/laofeng/ip-limiter.git
cd ip-limiter
```

 **2. 修改必要的核心配置文件** 

首先需要启动IP限流控制台，IP限流控制台默认使用Redis做为存储，因而需要先对Redis进行配置，否则应用运行时会报错，修改ＩＰ控制台的核心配置文件ip-limiter-dashboard/src/main/resources/application.properties，只需要修改其中的redis配置，其它的都可以使用默认值，如下所示：

```
spring.redis.host = 192.168.12.111
spring.redis.port = 6379
spring.redis.password = 8lFvrZh7d7Ik8LtNwpBMakleishen
spring.redis.database = 12
spring.redis.timeout = 5000
spring.redis.jedis.pool.max-idle = 8
spring.redis.jedis.pool.min-idle = 0
spring.redis.jedis.pool.max-wait = 8
spring.redis.jedis.pool.max-active = 20
```

 **3. 启动IP限流平台控制台** 

```
cd ip-limiter-dashboard
./start.sh
```

注：启动会报一些与Apollo相关的警告，可以不用理会，这个是方便配置后续接入Apollo的，如果实在是不喜欢，可以在pom中删除Apollo的引入，并且删除Apollo相关的代码即可。

控制台访问地址：
- 单个应用管理控制台地址：[http://127.0.0.1:8080/limiter](http://127.0.0.1:8080/limiter)
- 全局应用管理控制台地址：[http://127.0.0.1:8080/global-limiter](http://127.0.0.1:8080/global-limiter)

 **4. 启动演示应用** 

演示应用的目录ip-limiter-samples，里面包括了三个演示应用，分别为：
- ip-limiter-spring-boot-sample: 用于演示spring boot集用IP限流平台
- ip-limiter-spring-mvc-sample: 用于演示普通的spring mvc应用集用IP限流平台
- ip-limiter-spring-gateway-sample: 用于演示spring gateway集用IP限流平台

这三种应用类型应该包含了大部份的应用类型，每种应用都有自己的一些不同的集成方式及特点。

 **１）启动ip-limiter-spring-boot-sample** 

首先进入ip-limiter-spring-boot-sample目录，通过以下命令启动：

```
./install.sh
./start.sh
```
其默认的端口为10000，启动成功后，可以通过[http://localhost:10000/hello](http://localhost:10000/hello)访问验证，待控制台打印出往IP限流平台注册成功字样后，就可以在控制台的左上角的下拉框中看到该应用了。

 **２）启动ip-limiter-spring-mvc-sample** 

该演示应用为了启动上的方便，需要先在/etc/maven/setting.xml的pluginGroup中增加jetty的插件：

```
  <pluginGroups>
    <pluginGroup>org.mortbay.jetty</pluginGroup>
  </pluginGroups>
```
直接保存退出即可。

然后进入ip-limiter-spring-mvc-sample目录，通过以下命令启动：

```
./runJetty.sh
```
启动的过程中可能会报一些Jetty的错误，这个不需要理会，待启动完成后其默认的端口为10001，启动成功后可以通过[http://localhost:10001/app/health](http://localhost:10001/app/health)访问验证，待控制台打印出往IP限流平台注册成功字样后，就可以在控制台的左上角的下拉框中看到该应用了。

 **３）启动ip-limiter-spring-boot-sample** 

首先进入ip-limiter-spring-boot-sample目录，通过以下命令启动：

```
./install.sh
./start.sh
```
其默认的端口为10002，启动成功后，可以通过[http://localhost:10002/api_hello/hello](http://localhost:10002/api_hello/hello)访问验证，该请求实通过网关访问[http://localhost:10000/hello](http://localhost:10000/hello)，因而ip-limiter-spring-boot-sample需要先启动才可以验证该应用，待控制台打印出往IP限流平台注册成功字样后，就可以在控制台的左上角的下拉框中看到该应用了。

####  **七、应用的集成** 

目前应用的最新版本为1.1.0。

1. Spring Boot应用的集成

引入限流依赖的客户端starter核心模块

```
		<dependency>
		    <groupId>com.eeefff.limiter</groupId>
		    <artifactId>ip-limiter-core-web</artifactId>
		    <version>${ip-limiter-core.version}</version>
		</dependency>
```
其它的什么也不用做了，方便吧@_@。

2. Spring MVC应用的集成

Spring MVC应用的接入就会多一些步骤，不过也不麻烦。
１）引入限流依赖的客户端starter核心模块

```
		<dependency>
		    <groupId>com.eeefff.limiter</groupId>
		    <artifactId>ip-limiter-core-web</artifactId>
		    <version>${ip-limiter-core.version}</version>
		</dependency>
```

２）将IP限流平台的包加入扫描及增加Inteceptor

因为IP限流平台是基于Inteceptor的，在Spring的配置文件中增加如下配置：
```
<context:component-scan base-package="com.eeefff.limiter"></context:component-scan>
<mvc:interceptors>
    <ref bean="ipLimiterInterceptor" />
</mvc:interceptors>
```
如果遇到了集成上的问题，可以参考ip-limiter-spring-mvc-sample示例应用。

3. Spring Gateway的集成

Spring Gateway使用的是Netty做为通信模块，不能够像其它应用一样使用Inteceptor来实现，需要使用其本身的GlobalFilter结合ＷebFilter来实现，因而需要集成不同的包：
```
		<dependency>
		    <groupId>com.eeefff.limiter</groupId>
		    <artifactId>ip-limiter-core-spring-gateway</artifactId>
		    <version>${ip-limiter-core.version}</version>
		</dependency>
```
和普通的Spring Boot应用一样，也是不需要做任何事情，就是这么简单。

####  **参与贡献** 

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

加入组织：

![输入图片说明](https://images.gitee.com/uploads/images/2021/0108/111503_9d0246bd_306225.png "ip-limiter-group.png")

群图片如果过期了，请加13265554108，暗号ip-limiter，通过后再拉入群。