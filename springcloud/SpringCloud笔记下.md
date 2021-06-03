

[TOC]

SpanID记录的是某个节点时的过程

TraceID记录的是请求响应的整个过程

##  Sleuth和Zipkin的关系

 Sleuth 的数据信息发送给 Zipkin 进行聚合，利用Zipkin 存储并展示数据（Sleuth 负责记录，Zipkin负责整合Sleuth 的记录并展示出来）。

![image-20201105222633952](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201105222633952.png)

## Sleuth配置

每个微服务添加依赖：

```xml
<dependency>
	 <groupId>org.springframework.cloud</groupId>
 	<artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

每个微服务修改application.yml配置⽂件，添加⽇志级别：

```yml
logging:
 	level:
 		org.springframework.web.servlet.DispatcherServlet: debug
 		org.springframework.cloud.sleuth: debug
```

输出日志：

![image-20201103234757392](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201103234757392.png)

## Zipkin配置

![image-20201103234906481](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201103234906481.png)

**Zipkin Server 构建：**

添加依赖：

```xml
<dependency>
 	<groupId>io.zipkin.java</groupId>
	 <artifactId>zipkin-server</artifactId>
 	<version>2.12.3</version>
 	<exclusions>
 <!--排除掉log4j2的传递依赖，避免和springboot依赖的⽇
志组件冲突-->
	 <exclusion>
 	<groupId>org.springframework.boot</groupId>
 	<artifactId>spring-boot-starterlog4j2</artifactId>
 	</exclusion>
 	</exclusions>
 </dependency>
 <!--zipkin-server ui界⾯依赖坐标-->
 <dependency>
 	<groupId>io.zipkin.java</groupId>
 	<artifactId>zipkin-autoconfigure-ui</artifactId>
 	<version>2.12.3</version>
 </dependency
```

```java
@EnableZipkinServer // 启动类添加注解，开启Zipkin Server功能
```

application.yml

```yml
server:
	 port: 9411
management:
 metrics:
 	web:
 		server:
 			auto-time-requests: false # 关闭自动检测请求（会检测相关指标）
```

**Zipkin Client构建：**

添加依赖：

```xml
<dependency>
 	<groupId>org.springframework.cloud</groupId>
 	<artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

application.yml 中添加对zipkin server的引用：

```yml
spring:
 application:
 	name: lagou-service-autodeliver
 zipkin:
 	base-url: http://127.0.0.1:9411 # zipkin server的请求地址
 	sender:
 # web 客户端将踪迹⽇志数据通过⽹络请求的⽅式传送到服务端，另外还有配置
 # kafka/rabbit 客户端将踪迹⽇志数据传递到mq进⾏中转
 		type: web
 sleuth:
 	sampler:
 # 采样率 1 代表100%全部采集 ，默认0.1 代表10% 的请求踪迹数据会被采集
 # ⽣产环境下，请求量⾮常⼤，没有必要所有请求的踪迹数据都采集分析，对于⽹络包括server端压⼒都是⽐较⼤的，可以配置采样率采集⼀定⽐例的请求的踪迹数据进行分析即可
 		probability: 1
```

## Zipkin持久化配置

添加到数据库mysql：

1. 新建zipkin数据库，在GitHub下载数据库建表语句

2. 引入依赖

   ```xml
   <dependency>
    	<groupId>io.zipkin.java</groupId>
   	 <artifactId>zipkin-autoconfigure-storagemysql</artifactId>
    	<version>2.12.3</version>
    </dependency>
    <dependency>
    	<groupId>mysql</groupId>
    	<artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
    	<groupId>com.alibaba</groupId>
   	 <artifactId>druid-spring-bootstarter</artifactId>
    	<version>1.1.10</version>
    </dependency>
    <dependency>
   	 <groupId>org.springframework</groupId>
    	<artifactId>spring-tx</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.springframework</groupId>
    	<artifactId>spring-jdbc</artifactId>
    </dependency>
   ```

3. yml配置

   ```yml
   spring:
    datasource:
    	driver-class-name: com.mysql.jdbc.Driver
   	 url: jdbc:mysql://localhost:3306/zipkin?
   useUnicode=true&characterEncoding=utf8&useSSL=false&allowMultiQueries=true
    	username: root
    	password: 123456
    	druid:
    		initialSize: 10
    		minIdle: 10
    		maxActive: 30
    		maxWait: 50000
   # 指定zipkin持久化介质为mysql
   zipkin:
    storage:
    	type: mysql
   ```

4. 启动类中注入事务管理器

   ```java
   @Bean
   public PlatformTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
   }
   ```
   

界面：![image-20201105232459280](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201105232459280.png)

## OAuth2认证使用

导入依赖：**使用exclusions可以将依赖传递过来的jar排除，使用自己定义版本的jar**

```xml
    <dependencies>
        <!--导⼊Eureka Client依赖-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eurekaclient</artifactId>
        </dependency>
        <!--导⼊spring cloud oauth2依赖-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-oauth2</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.security.oauth.boot</groupId>
                    <artifactId>spring-security-oauth2-
                        autoconfigure
                    </artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <!--application.yml（构建认证服务器，配置⽂件无特别之处）
            ⼊⼝类无特殊之处认证服务器配置类-->
            <groupId>org.springframework.security.oauth.boot</groupId>
            <artifactId>spring-security-oauth2-
                autoconfigure
            </artifactId>
            <version>2.1.11.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security.oauth</groupId>
            <artifactId>spring-security-oauth2</artifactId>
            <version>2.3.4.RELEASE</version>
        </dependency>
    </dependencies>
```

