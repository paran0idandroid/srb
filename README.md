#### 介绍
尚融宝是一个网络借贷信息中介服务平台，为个人投资者、个人融资用户和小微企业提供专业的线上信贷及出借撮合服务。

#### 软件架构
![软件架构图](https://images.gitee.com/uploads/images/2022/0302/222745_8862d4de_10366186.png "0d2630acf4428d33859d89e8f78be575.png")

#### 后台管理系统
![后台管理系统](https://images.gitee.com/uploads/images/2022/0302/223125_5de56f95_10366186.png "0f84ecba-944c-4030-85f1-2c15d6021579.png")

#### 前台网站系统
![前台网站系统](https://images.gitee.com/uploads/images/2022/0302/223142_629d0f01_10366186.jpeg "dd457f94-d902-4428-8c34-fff376e9ec19.jpg")

#### 业务流程总结
![业务流程总结](https://images.gitee.com/uploads/images/2022/0302/223156_5f4160b3_10366186.png "27528777bac0634fd778ca60094057b4.png")

#### 技术栈
1、后端  
SpringBoot 2.3.4.RELEASE  
SpringCloud Hoxton.SR8：微服务基础设施 - 服务注册、服务发现、服务熔断、微服务网关、配置中心等  
SpringCloud Alibaba 2.2.2.RELEASE  
MyBatis Plus：持久层框架和代码生成器  
Lombok：简化实体类开发  
Swagger2：Api接口文档生成工具  
Logback：日志系统  
alibaba-easyexcel：Excel读写  
Spring Data Redis：Spring项目中访问Redis缓存  
HTTPClient: 基于Http协议的客户端，用来实现远程调用  
Spring Task：定时任务  

2、数据库和中间件  
MySQL 5.7：关系型数据库     管理工具：Navicat  
Redis 5.0：缓存技术     管理工具：RedisDesktopManager  
RabbitMQ 3.8：消息中间件  

3、三方接口  
阿里云短信：短信网关  
阿里云OSS：分布式文件存储  
资金托管平台API对接：汇付宝  

4、前端  
Node.js： JavaScript 运行环境  
ES6：JavaScript的模块化版本  
axios：一个发送Ajax请求的工具  
Vue.js：web 界面的渐进式框架  
Element-UI：前端组件库  
模块化开发：解决javascript变量全局空间污染的问题  
NPM：模块资源管理器  
vue-element-admin：基于Vue.js的后台管理系统UI集成方案  
NuxtJS：基于Vue.js构建的服务器端渲染应用的轻量级框架  