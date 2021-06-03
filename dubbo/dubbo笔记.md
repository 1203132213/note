[TOC]

## 单体架构优缺点？

单体架构就是把所有业务都放一个Tomcat里面。

优点：

- 开发快，成本低
- 架构简单，易于测试和部署

缺点：

- 大项目耦合严重，不易开发和维护，出现问题会互相影响
- 新增业务困难



## 垂直架构优缺点？

根据业务垂直切割成多个项目，如图

![image-20201009140233511](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201009140233511.png)

优点：

- 流量分担，解决并发问题
- 方便增加业务，负载均衡，容错率提高
- 可以针对不同系统进行优化

缺点：

- 服务系统之间调用硬编码

- 搭建集群时，负载均衡比较复杂

- 充斥慢查询，主从同步延迟大

- 慢查询：

  慢查询是指执行时间超过慢查询时间的sql语句。
  
  查看慢查询时间的方法
  
  ```sql
  show variables like 'long_query_time';
  ```
  
  可以显示当前慢查询时间。MySql默认慢查询时间为10秒
  可以通过如下语句对慢查询的定义进行修改
  
  ```sql
  set global long_query_time=1;
  ```
  
  

## SOA是什么？SOA的优缺点？

SOA中文名为**分布式架构**，在垂直架构基础上，将每个项目拆分出多个松耦合的服务。<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201009235004467.png" alt="image-20201009235004467" style="zoom:80%;" />

1. 应用层：离用户最近的一层，使用tomcat作为web容器，接收用户请求，禁止访问数据库。

2. 业务服务层：里面是具体的业务场景。

3. 基础业务层：业务的核心，存放基础服务

4. 基础服务层：与业务无关，存放一些通用服务。

   这类服务特点：请求量大、逻辑简单、特性明显、功能独立

5. 存储层：不同的存储类型 Mysql Mongodb ES fastDFS

   上述分层满足两条件：1.二八定律（网站80%流量在核心功能上）。2.调用是单向的，可跨层调用，不能逆向调用。

   **SOA优点：**

   - 以接口为粒度，屏蔽底层调用细节，也更稳定安全。
- 每个业务职责单一，扩展性更强。
   - 服务应用本身无状态化（下面讲解）。
   - 每个服务可以确定责任人，更容易保证服务质量和稳定
   
   **SOA缺点：**

   - 服务力度控制复杂，没控制好的话服务模块越来越多，会引发超时等分布式事务问题。
   
- 版本升级兼容困难 尽量不要删除方法 字段 枚举类型的新增字段也可能不兼容 
  
- 调用链路长 服务质量不可监控

## Dubbo介绍：

### Dubbo是什么？

Dubbo是一款高性能的java RPC框架，可以和Spring框架无缝集成，服务治理使用SOA。

### 为什么要用 Dubbo？ 

随着服务化的进一步发展，服务越来越多，服务之间的调用和依赖关系也越来越 复杂，诞生了**面向服务的架构体系(SOA)**， 也因此衍生出了一系列相应的技术，如对**服务提供、服务调用、连接处理、通信 协议、序列化方式、服务发现、服务路由、日志输出等行为**进行封装的服务框架。 就这样为分布式系统的服务治理框架就出现了，Dubbo 也就这样产生了。

### Dubbo 接口 与HTTP 接口有什么区别？

1、协议层区别（http使用的是应用层协议；Dubbo接口使用的是传输层协议）

HTTP ，HTTPS 使用的是应用层协议 

应用层协议：定义了用于在网络中进行**通信和传输数据的接口**

DUBBO接口使用的是 TCP/IP是传输层协议  

传输层协议：管理着网络中的**端到端的数据传输；因此要比 HTTP协议快**

2、socket 层区别（Dubbo接口使用长连接，http1.1协议默认使用短连接，http2.0协议开始默认使用长连接）

dubbo默认使用socket长连接，即首次访问建立连接以后，**后续网络请求使用相同的网络通道**

http1.1协议默认使用短连接，每次请求均需要进行三次握手，而http2.0协议开始将默认socket连接改为了长连接(后面这个没区别)

### Dubbo 默认使用什么动态代理方式：

Dubbo 用 Javassist 动态代理，所以很可能会问你为什么要用这个代理，可能还会引申出 JDK 的动态代理、ASM、CGLIB。

所以这也是个注意点，如果你不太清楚的话上面的回答就不要扯到动态代理了，如果清楚的话那肯定得提，来诱导面试官来问你动态代理方面的问题，这很关键。

**面试官是需要诱导的**，毕竟他也想知道你优秀的方面到底有多优秀，你也取长补短，双赢双赢。

来回答下为什么用 Javassist，很简单，**就是快，且字节码生成方便**。

ASM 比 Javassist 更快，但是没有快一个数量级，而Javassist 只需用字符串拼接就可以生成字节码，而 ASM 需要手工生成，成本较高，比较麻烦

### Dubbo 如何优雅停机？ 

Dubbo 是通过 JDK 的 ShutdownHook 来完成优雅停机的，所以如果使用 kill -9 PID 等强制关闭指令，是不会执行优雅停机的，只有通过 kill PID 时，才会执行。

```java
static {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
            if (logger.isInfoEnabled()) {
                logger.info("Run shutdown hook now.");
            }
            ProtocolConfig.destroyAll();
        }
    }, "DubboShutdownHook"));
}
```

### Dubbo几种宕机的情况

【监控中心宕机】 不影响使用，丢失部分采样数据
【数据库宕机】注册中心通过缓存提供服务列表查询，但是不能注册新服务
【注册中心对等集群】任意一台宕机，将自动切换到另一台
【注册中心全部宕机】服务提供者与消费者通过本地缓存通信
【服务提供者宕机】服务消费者无法使用，**无限次重连**等待提供者恢复 

### Dubbo 和 Dubbox 之间的区别？ 

Dubbox 是继 Dubbo 停止维护后，当当网基于 Dubbo 做的一个扩展项目，如加了服务可 Restful 调用，更新了开源组件等。

### Dubbo的管理控制台能做什么？

管理控制台主要包含：路由规则，动态配置，服务降级，访问控制，权重调整，负载均衡等管理功能。

### Dubbo 使用过程中都遇到了些什么问题？ 

1. 在注册中心找不到对应的服务,检查 service 实现类是否添加了@service 注解无法连接到注册中心,检查配置文件中的对应的测试 ip是否正确。
2. Dubbo 的设计目的是为了满足高并发**小数据量**的 rpc 调用，在**大数据量下的性能表现并不好**，建议使用 rmi 或 http 协议。
3. QoS端口重复导致启动不起来（QoS就是运维的端口，配置它就可以通过控制台来进行管理）

### Dubbo 服务提供者能实现失效踢出是什么原理？

服务失效踢出基于 zookeeper 的临时节点原理，zookeeper临时节点随着会话失效而删除。

### Dubbo在安全机制方面是如何解决？

Dubbo通过Token令牌防止用户绕过注册中心直连，然后在注册中心上管理授权。Dubbo还提供服务黑白名单，来控制服务所允许的调用方。

### Dubbo与Spring的关系？

Dubbo采用全Spring配置方式，透明化接入应用，对应用没有任何API侵入，只需用Spring加载Dubbo的配置即可，Dubbo基于Spring的Schema扩展进行加载。

### Dubbo 配置文件是如何加载到 Spring 中的？

Spring 容器在启动的时候，会读取到 Spring 默认的一些 schema 以及 Dubbo 自 定义的 schema，每个 schema 都会对应一个自己的 NamespaceHandler， NamespaceHandler 里面通过 BeanDefinitionParser 来解析配置信息并转化为 需要加载的 bean 对象（具体需消化）



### Dubbo的体系架构：

![ ](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201015111720138.png)

### Dubbo注册和发现流程图：

在这里主要由四部分组成: 

- Provider:暴露服务的服务提供方

  Protocol：负责提供者和消费者之间协议交互数据

  Service：真实的业务服务信息可以理解成接口和实现 

  Container：Dubbo的运行环境 ,**服务运行容器**负责启动加载运行服务提供者

- Consumer：调用远程服务的服务消费方

  Protocol：负责提供者和消费者之间协议交互数据 

  Cluster：感知提供者端的列表信息 

  Proxy：可以理解成 提供者的服务调用代理类 由它接管 Consumer中的接口调用逻辑

- Registry：注册中心，用于作为**服务发现和路由配置**等工作，提供者和消费者都会在这里进行注册 

- Monitor：用于**提供者和消费者中的数据统计**，比如**调用频次，成功失败次数**等信息。 

![image-20201015104347747](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201015104347747.png)

**调用流程:** 

1. 服务提供者在服务容器启动时向注册中心注册自己提供的服务。
2. 服务消费者在启动时向注册中心订阅自己所需的服务，将服务列表发送至Consumer 应用缓存 。
3. 注册中心返回服务提供者地址列表给消费者如果有变更注册中心会基于长连接推送变更数据给消费者。
4. 服务消费者 从提供者地址列表中基于软负载均衡算法 选一台提供者进行调用如果调用失败则重新选择一台 。
5. 服务提供者和消费者在内存中的调用次数和调用时间定时每分钟发送给监控中心。

**Dubbo的核心功能：**

主要就是如下3个核心功能：

- **Remoting：**网络通信框架，提供对多种NIO框架抽象封装，包括“同步转异步”和“请求-响应”模式的信息交换方式。
- **Cluster：服务框架**，提供基于接口方法的透明远程过程调用，包括多协议支持，以及软负载均衡，失败容错，地址路由，动态配置等集群支持。
- **Registry：服务注册**，基于注册中心目录服务，使服务消费方能动态的查找服务提供方，使地址透明，使服务提供方可以平滑增加或减少机器。
- 

### Dubbo什么时候更新本地的zookeeper信息缓存文件?订阅zookeeper信息的整体过程是怎么样的?

dubbo向zk发送了订阅请求以后，会去监听zk的回调，（如果zk有回调就会去调用notify方法），接着会去创建接口配置信息的持久化节点，同时dubbo也设置了对该节点的监听，zk节点如果发生了变化那么会触发回调方法，**去更新zk信息的缓存文件**，同时注册服务在调用的时候会去对比最新的配置信息节点，有差别的话会以最新信息为准重新暴露。



### Dubbo推荐方案：

| 组件名称                    | 推荐方案  | 其他方案                                                     |
| --------------------------- | --------- | ------------------------------------------------------------ |
| Language（支持语言）        | java      | node.js、Python、PHP、Go、Erlang                             |
| API（使用API方式）          | XML       | Annotation（注解）、SpringBoot、Plain Java                   |
| Registry（注册中心）        | zookeeper | Redis、Simple、MultiCast、Etcd3                              |
| Cluster（集群容错）         | Fail Over | Fail safe、Fail fast、Fail back、Forking BroadCast           |
| Load balance（负载均衡）    | Random    | Least Active、Round Robin、Consistent Hash                   |
| Protocol（协议）            | Dubbo     | RMI、Hessian、HTTP、WebService、Thrift、Native Thrift、Memcached、Redis、Rest、JsonRPC、XmlRPC、JmsRPC |
| Transport（通信方式）       | Netty3    | Netty4、Gizzly、Jetty、Mina、P2P、ZooKeeper                  |
| Serialization（序列化方式） | Hessian2  | java、JSON、Fst、Kryo、Native Hessian、Avro                  |

**Dubbo支持哪些协议，每种协议的应用场景，优缺点？**

- **Dubbo：** 单一长连接和NIO异步通讯，适合大并发小数据量的服务调用，以及消费者远大于提供者。传输协议TCP，异步，Hessian序列化；

- **rmi：** 采用JDK标准的rmi协议实现，传输参数和返回参数对象需要实现Serializable接口，使用java标准序列化机制，使用阻塞式短连接，传输数据包大小混合，消费者和提供者个数差不多，可传文件，传输协议TCP。
  多个短连接，TCP协议传输，同步传输，适用常规的远程服务调用和rmi互操作。在依赖低版本的Common-Collections包，java序列化存在安全漏洞；

- **webservice：** 基于WebService的远程调用协议，集成CXF实现，提供和原生WebService的互操作。多个短连接，基于HTTP传输，同步传输，适用系统集成和跨语言调用；

- **http：** 基于Http表单提交的远程调用协议，使用Spring的HttpInvoke实现。多个短连接，传输协议HTTP，传入参数大小混合，提供者个数多于消费者，需要给应用程序和浏览器JS调用；

- **hessian：** 集成Hessian服务，基于HTTP通讯，采用Servlet暴露服务，Dubbo内嵌Jetty作为服务器时默认实现，提供与Hession服务互操作。多个短连接，同步HTTP传输，Hessian序列化，传入参数较大，提供者大于消费者，提供者压力较大，可传文件；

- **memcache：** 基于memcached实现的RPC协议

- **redis：** 基于redis实现的RPC协议

  

**Dubbo支持哪些注册中心，以及优缺点：**

1. Zookeeper(官方推荐) 

   **优点:**支持分布式.很多周边产品。 （基于分布式协调系统Zookeeper实现，采用Zookeeper的watch机制实现数据变更）

   **缺点:** 受限于Zookeeper软件的稳定性。Zookeeper专门分布式辅助软件,稳定较优。 

2. Multicast 

   **优点:**去中心化,不需要单独安装软件。 （Multicast注册中心不需要任何中心节点，只要广播地址，就能进行服务注册和发现。基于网络中组播传输实现）

   **缺点:**Provider和Consumer和Registry不能跨机房(路由)。

3. Redis 

   **优点:**支持集群,性能高。 （基于redis实现，采用key/Map存储，住key存储服务名和类型，Map中key存储服务URL，value服务过期时间。基于redis的发布/订阅模式通知数据变更）

   **缺点:**要求服务器时间同步。否则可能出现集群失败问题。 

4. Simple 

   **优点:** 标准RPC服务.没有兼容问题。

   **缺点:** 不支持集群。

| 集群容错方案      | 说明                                       |
| ----------------- | ------------------------------------------ |
| Failover Cluster  | 失败自动切换，自动重试其它服务器（默认）   |
| Failfast Cluster  | 快速失败，立即报错，只发起一次调用         |
| Failsafe Cluster  | 失败安全，出现异常时，直接忽略             |
| Failback Cluster  | 失败自动恢复，记录失败请求，定时重发       |
| Forking Cluster   | 并行调用多个服务器，只要一个成功即返回     |
| Broadcast Cluster | 广播逐个调用所有提供者，任意一个报错则报错 |

| 负载均衡策略              | 说明                                         |
| ------------------------- | -------------------------------------------- |
| Random LoadBalance        | 随机，按权重设置随机概率（默认）             |
| RoundRobin LoadBalance    | 轮询，按公约的权重设置轮询比率               |
| LeastActive LoadBalance   | 最少活跃调用数，相同活跃数的随机             |
| ConsisterHash LoadBalance | 一致性Hash，相同参数的请求总是发到同一提供者 |

**Dubbo内置容器：**

Spring Container

Jetty Container

Log4j Container

Dubbo 的服务容器只是一个简单的 Main 方法，并加载一个简单的 Spring 容器，用于暴露服务。

### Dubbo管理控制台是怎么监控到服务的？

通过zookeeper的端口进行监控的。

### 服务上线怎么不影响旧版本？

采用多版本开发，不影响旧版本（服务端配置文件提供版本号字段）。

### 如何解决服务调用链过长的问题？

Dubbo 可以使用 Pinpoint 和 Apache Skywalking(Incubator) 、zipkin实现分布式服务追踪，当然还有其他很多方案。https://blog.csdn.net/liaokailin/article/details/52077620

### 服务调用超时问题怎么解决？

Dubbo在调用服务不成功时，默认是会重试两次的

### 同一个服务多个注册的情况下可以直连某一个服务吗？

可以点对点直连，修改配置即可，也可以通过 telnet 直接某个服务。

###  Dubbo SPI 和 Java SPI 区别？

SPI是什么？

简单说就是可将接口定制自己的实现类，

**JDK SPI**

JDK 标准的 SPI 会一次性加载所有的扩展实现，如果有的扩展吃实话很耗时，但也没用上，很浪费资源。 

所以只希望加载某个的实现，就不现实了 。

**Dubbo SPI** 

1，对 Dubbo 进行扩展，不需要改动 Dubbo 的源码 

2，延迟加载，可以一次只加载自己想要加载的扩展实现。 

3，增加了对扩展点 IOC 和 AOP 的支持，一个扩展点可以直接 setter 注入其它扩展点。

4，Dubbo 的扩展机制能很好的支持第三方 IoC 容器，默认支持 Spring Bean。

**Dubbo SPI中的Adaptive功能**

```java
public class DubboAdaptiveMain {
	public static void main(String[] args) {
        //这里的dog就是文件的key
		URL url = URL.valueOf("test://localhost/hello?hello.service=dog");
		final HelloService adaptiveExtension =
ExtensionLoader.getExtensionLoader(HelloService.class).getAdaptiveExtension();
adaptiveExtension.sayHello(url);
	}
}
```

### Dubbo Monitor 实现原理？

Consumer 端在发起调用之前会先走 filter 链；provider 端在接收到请求时也是 先走 filter 链，然后才进行真正的业务逻辑处理。

 默认情况下，在 consumer 和 provider 的 filter 链中都会有 Monitorfilter。

1、MonitorFilter 向 DubboMonitor 发送数据 

2、DubboMonitor 将数据进行聚合后（默认聚合 1min 中的统计数据）暂存到 ConcurrentMap statisticsMap，然后使用一个 含有 3 个线程（线程名字：DubboMonitorSendTimer）的线程池每隔 1min 钟， 调用 SimpleMonitorService 遍历发送 statisticsMap 中的统计数据，每发送完毕 一个，就重置当前的 Statistics 的 AtomicReference 

3、SimpleMonitorService 将这些聚合数据塞入 BlockingQueue queue 中（队 列大写为 100000）

4、SimpleMonitorService 使用一个后台线程（线程名为： DubboMonitorAsyncWriteLogThread）将 queue 中的数据写入文件（该线程以 死循环的形式来写） 

5、SimpleMonitorService 还会使用一个含有 1 个线程（线程名字： DubboMonitorTimer）的线程池每隔 5min 钟，将文件中的统计数据画成图表

### Dubbo的@Activate的作用？

满足括号的条件就激活该类。

例：

```java
@Activate(group = Constants.PROVIDER, value = Constants.TOKEN_KEY)
public class TokenFilter implements Filter {
}
```

表示如果过滤器使用方（通过group指定）属于Constants.PROVIDER（服务提供方）并且 URL中有参数 Constants.TOKEN_KEY（token）时就激活使用这个过滤器。

### Dubbo 支持分布式事务吗？ 

目前暂时不支持，可与通过 tcc-transaction 框架实现 

介绍：tcc-transaction 是开源的 TCC 补偿性分布式事务框架 

Git 地址：https://github.com/changmingxie/tcc-transaction 

TCC-Transaction 通过 Dubbo 隐式传参的功能，避免自己对业务代码的入侵。



### Dubbo 可以对结果进行缓存吗？ 

为了提高数据访问的速度。Dubbo 提供了声明式缓存，以减少用户加缓存的工作量 

```xml
<Dubbo:reference cache="true" />
```

其实比普通的配置文件就多了一个标签 cache="true"。



### Dubbo 必须依赖的包有哪些？ 

Dubbo 必须依赖 JDK，其他为可选。



### Dubbo telnet 命令能做什么？

Dubbo 服务发布之后，我们可以利用 telnet 命令进行调试、管理。 

Dubbo2.0.5 以上版本服务提供端口支持 telnet 命令 

**连接服务** 

telnet localhost 20880 //键入回车进入 Dubbo 命令模式。 

**查看服务列表** 

```
Dubbo>ls 
com.test.TestService

Dubbo>ls com.test.TestService 
create 
delete 
query 

ls : 显示服务列表。
ls -l : 显示服务详细信息列表。 
ls XxxService：显示服务的方法列表。
ls -l XxxService：显示服务的方法详细信息列表。
```

### 当一个服务接口有多种实现时怎么做？

当一个接口有多种实现时，可以用 group 属性来分组，服务提供方和消费方都指定同一个 group 即可。

group使用：两个同一接口使用同一注册中心时，使用group可将两个区分开

```xml
<dubbo:reference id="userServiceOne" interface="com.UserService" group="userServiceOne" check="false"/>

<dubbo:reference id="userServiceTwo" interface="com.UserService" group="userServiceTwo" check="false"/>
```

### Dubbo的调用链路？

上方淡绿色代表了服务生产者的范围 ；淡蓝色代表了服务消费者的范围；红色箭头代表了调用的方向 

![image-20201011163151396](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201011163151396.png)

1. 消费者通过代理模式调用接口，使用jdk、javassist模式。

2. Filter过滤请求（比如调用时间统计等等）。

3. Invoker调用逻辑 （重要）。

   通过Directory 去配置中新读取信息，通过list方法**获取所有的Invoker** 。

   通过Cluster模块**根据选择的具体路由规则来选取Invoker列表** 。

   通过LoadBalance模块**根据负载均衡策略选择一个具体的Invoker**来处理我们的请求 。

   如果执行中出现错误 并且Consumer阶段配**置了重试机制则会重新尝试执行。**

4. 继续经过Filter进行**执行功能的前后封装** （上下文、计算、监听器等操作）。

5. Invoker**选择具体的执行协议**。

6. 客户端进行编码和序列化然后发送数据。

7. 到达Consumer中的 Server在这里进行反编码和反序列化的接收数据 。

8. 使用Exporter**选择执行器**。

9. 交给Filter进行一个提供者端的过滤到达Invoker执行器 （上下文、计算、监听器等操作）。

10. 通过**Invoker调用接口的具体实现**然后返回。

**Dubbo框架设计一共划分了10个层：**

**服务接口层（Service）**：该层是与实际业务逻辑相关的，根据服务提供方和服务消费方的业务设计对应的接口和实现。

**配置层（Config）**：对外配置接口，以ServiceConfig和ReferenceConfig为中心。

**服务代理层（Proxy**）：服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton,以 ServiceProxy 为中心，扩展接口为 ProxyFactory 。

**服务注册层（Registry）**：**封装服务地址的注册与发现，以服务URL为中心**， 扩展接口为 RegistryFactory、Registry、RegistryService。

**集群层（Cluster）**：封装多个提供者的路由及负载均衡，并桥接注册中心，以Invoker为中心，扩展接口为 Cluster、Directory、Router 和 LoadBlancce 。

**监控层（Monitor）**：RPC调用次数和调用时间监控，以 Statistics 为中心，扩展接口为 MonitorFactory、Monitor 和 MonitorService 。

**远程调用层（Protocol）**：封将RPC调用，以Invocation和Result为中心，扩展接口为Protocol、Invoker和Exporter。

**信息交换层（Exchange）**：封装请求响应模式，同步转异步，以 Request 和 Response 为中心，扩展接口为 Exchanger、ExchangeChannel、 ExchangeClient 和 ExchangeServer。

**网络传输层（Transport）**：抽象mina和netty为统一接口，以 Message 为 中心，扩展接口为 Channel、Transporter、Client、Server 和 Codec 。

**数据序列化层（Serialize）：**可复用的一些工具，扩展接口为 Serialization、 ObjectInput、ObjectOutput 和 ThreadPool。

## Dubbo开发实战：

### 搭建一个简单的Dubbo程序：

程序实现分为以下几步骤:

1. 建立maven工程 并且 创建API模块: 用于规范双方接口协定 （ibs-interface）
2. 提供provider模块，引入API模块，并且对其中的服务进行实现。将其注册到注册中心上，对外来 统一提供服务。（ibs-provider）
3. 提供consumer模块，引入API模块，并且引入与提供者相同的注册中心。再进行服务调用。（ibs-consumer）

#### ibs-interface：

1. 定义maven。

```xml
<groupId>com.lagou</groupId>
<artifactId>ibs-interface</artifactId>
<version>1.0-SNAPSHOT</version>
```

2. 定义接口，这里为了方便，只是写一个基本的方法

```java
public interface HelloService {
String sayHello(String name);
}
```

#### ibs-provider：

1. 引入API模块。

```xml
<dependency>
<groupId>com.lagou</groupId>
<artifactId>ibs-interface</artifactId>
<version>${project.version}</version>
</dependency>
```

2. 引入Dubbo相关依赖，这里为了方便，使用注解方式。

```xml
<dependency>
	<groupId>org.apache.dubbo</groupId>
	<artifactId>dubbo</artifactId>
</dependency>
<dependency>
	<groupId>org.apache.dubbo</groupId>
	<artifactId>dubbo-registry-zookeeper</artifactId>
</dependency>
<dependency>
	<groupId>org.apache.dubbo</groupId>
	<artifactId>dubbo-rpc-dubbo</artifactId>
</dependency>
<dependency>
	<groupId>org.apache.dubbo</groupId>
	<artifactId>dubbo-remoting-netty4</artifactId>
</dependency>
<dependency>
	<groupId>org.apache.dubbo</groupId>
	<artifactId>dubbo-serialization-hessian2</artifactId>
</dependency>
```

3. 编写实现类。注意这里也使用了Dubbo中的 @Service 注解来声明他是一个服务的提供者。

```java
@Service
public class HelloServiceImpl implements HelloService {
	@Override
	public String sayHello(String name) {
		return "hello: " + name;
	}
}
```

4. 编写配置文件，用于配置dubbo。比如这里我就叫 dubbo-provider.properties ，放入到 resources 目录下。

```properties
dubbo.application.name=dubbo-demo-annotation-provider //当前提供者的名称
dubbo.protocol.name=dubbo //对外提供的时候使用的协议
dubbo.protocol.port=20880//该服务对外暴露的端口,在消费者使用时，则会使用这个端口并且使用指定的协议与提供者建立连接
```

5. 编写启动的 main 函数。这里面做的比较简单，主要要注意注解方式中的注册中心这里是使用的本 机2181端口来作为注册中心。

```java
public class DubboPureMain {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new
               AnnotationConfigApplicationContext(ProviderConfiguration.class);
        context.start();
        System.in.read();
    }
    @Configuration
    @EnableDubbo(scanBasePackages = "com.lagou.service.impl")
    @PropertySource("classpath:/dubbo-provider.properties")
    static class ProviderConfiguration {
        @Bean
        public RegistryConfig registryConfig() {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress("zookeeper://127.0.0.1:2181");
            return registryConfig;
        }
    }
}
```

#### ibs-consumer：

1. 引入API模块。

```xml
<dependency>
	<groupId>com.lagou</groupId>
	<artifactId>service-api</artifactId>
	<version>${project.version}</version>
</dependency>
```

2. 引入Dubbo依赖 ,同服务提供者。

3. 编写服务，用于真实的引用dubbo接口并使用。因为这里是示例，所以比较简单一些。这里面 @Reference 中所指向的就是真实的第三方服务接口。

```java
@Component
public class ConsumerComponent {
    @Reference
    private HelloService helloService;
    public String sayHello(String name) {
        return helloService.sayHello(name);
    }
}
```

4. 编写消费者的配置文件。这里比较简单，主要就是指定了当前消费者的名称和注册中心的位置。通 过这个注册中心地址，消费者就会注册到这里并且也可以根据这个注册中心找到真正的提供者列表。

```properties
dubbo.application.name=service-consumer
dubbo.registry.address=zookeeper://127.0.0.1:2181
```

5. 编写启动类，这其中就会当用户在控制台输入了一次换行后，则会发起一次请求。

```java
public class AnnotationConsumerMain {
    public static void main(String[] args) throws IOException,
            InterruptedException {
        AnnotationConfigApplicationContext context = new
                AnnotationConfigApplicationContext(ConsumerConfiguration.class);
        context.start();
        ConsumerComponent service =
                context.getBean(ConsumerComponent.class);
        while (true) {
            System.in.read();
            try {
                String hello = service.sayHello("world");
                System.out.println("result :" + hello);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Configuration
    @EnableDubbo(scanBasePackages = "com.lagou.service")
    @PropertySource("classpath:/dubbo-consumer.properties")
    @ComponentScan(value = {"com.lagou.bean.consumer"})
    static class ConsumerConfiguration {
    }
}
```



### Dubbo的xml配置？

**超时优先级：**上面优先级最高

![image-20201014115130667](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201014115130667.png)



**提供者：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>    
	<Dubbo:application name="Dubbo-demo-annotation-provider"/>
	<Dubbo:registry address="zookeeper://127.0.0.1:2181"/>
	<Dubbo:protocol name="Dubbo"/>
	<bean id="helloService" class="com.lagou.service.impl.HelloServiceImpl"/>
	<Dubbo:service interface="com.lagou.service.HelloService" ref="helloService"/>
</beans>
```

**消费者：**

```xml
<beans>
	<Dubbo:application name="demo-consumer"/>
	<Dubbo:registry address="zookeeper://127.0.0.1:2181"/>
	<Dubbo:reference id="helloService" interface="com.lagou.service.HelloService">
	</Dubbo:reference>
</beans>
```



### Dubbo配置项有哪些？

- **Dubbo:application标签：代表当前应用的信息**

  1. name：应用名称。
  2. owner: 当前应用程序的负责人。
  3. qosEnable : 是否启动QoS 默认true（运维相关，cmd再talnet+qos端口就能看到相关服务）。
  4. qosPort : 启动QoS绑定的端口 默认22222。
  5. qosAcceptForeignIp: 是否允许远程访问默认是false。

- **Dubbo:registry标签：一个模块中的服务可以将其注册到一个或多个注册中心上。后面再service和reference也会引入这个注册中心。**

  1. id：注册中心唯一标识。
  2. address：注册中心地址。
  3. protocol：注册中心的协议，可在address直接写入，如使用 zookeeper，就可以写成 zookeeper://xx.xx.xx.xx:2181。
  4. timeout： 当与注册中心不再同一个机房时，大多会把该参数延长。

- **Dubbo:protocol标签：指定服务在进行数据传输所使用的协议。**

  1. id：唯一标识， 在大公司，可能因为各个部门技术栈不同，所以可能会选择使用不同的协议进行交互。这里在多个协议使用时，需要指定。

     ```xml
     例：
     < Dubbo:protocol id = "rmi1" name = "rmi" port = "1099" />
     < Dubbo:protocol id = "rmi2" name = "Dubbo" port = "2099" />
      
     < Dubbo:service protocol = "rmi1" />
     ```

  2. name : 指定协议名称。默认使用 Dubbo。

     

- **Dubbo:method标签：用于在制定的 Dubbo:service 或者 Dubbo:reference 中的更具体一个层级，指定具体方法级别在进行RPC操作时候的配置**

  1. name : 指定方法名称，用于对这个方法名称的RPC调用进行特殊配置。 
  2. async: 是否异步 默认false

-  **Dubbo:service标签：org.apache.Dubbo.config.ServiceConfig, 用于指定当前需要对外暴露的服务信息**

  1. interface：对外暴露接口名。
  2. ref：具体实现对象的引用，一般我们在生产级别都是使用Spring去进行Bean托管的，所以这里面 一般也指的是Spring中的BeanId。
  3. version： 对外暴露的版本号。不同的版本号，消费者在消费的时候只会根据固定的版本号进行消费。 

- **Dubbo:reference标签：org.apache.Dubbo.config.ReferenceConfig, 消费者的配置**

  1. id : 指定该Bean在注册到Spring中的id。 

  2. interface: 服务接口名 
  3. version : 指定当前服务版本，与服务提供者的版本一致。
  4. registry : 指定所具体使用的注册中心地址。和上面 Dubbo:registry 中所声明的id对应。

-  **Dubbo:service和Dubbo:reference详解：**

  1. mock: 用于在方法调用出现错误时，当做服务**降级**来统一对外返回结果。
  2. timeout: 用于指定当前方法或者接口中所有方法的超时时间。
  3. check: 用于在启动时，检查生产者是否有该服务。我们一般都会将这个值设置为false，不让其进行检查。因为如果出现模块之间循环引用的话，那么则可能会出现相互依赖，都进行check的话， 那么这两个服务永远也启动不起来。
  4.  retries: 用于指定当前**服务在执行时出现错误或者超时时的重试机制**。
     - 注意提供者是否有幂等（请求一次和多次结果都一样），否则可能出现数据一致性问题。
     - 注意提供者是否有类似缓存机制，如没有缓存，出现大面积错误时，可能因为不停重试导致雪崩。
  5.  executes: 用于在**提供者**做配置，来确保最大的并行度（有消费者可以同时访问）。
     - 可能导致集群功能无法充分利用或者堵塞 
     - 但是也可以启动部分对应用的保护功能 
     - 可以不做配置，结合后面的熔断限流使用
  
- **Dubbo:service 与 Dubbo:provider的区别？**

  provider是原始的服务提供方式：配置参数超级多，比较繁琐，学习成本大

  service是在provider的基础上给了很多默认值，用户使用时只需配置少量必需的值，大大降低学习成本。

  reference与consumer同理。
  
  

### Dubbo自定义负载均衡规则：

```xml
onlyFirst=包名.负载均衡器
```

```java
public class OnlyFirstLoadbalancer implements LoadBalance {
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> list, URL url, Invocation invocation) throws RpcException {
        // 所有的服务提供者 按照IP  + 端口排序   选择第一个
        return  list.stream().sorted((i1,i2)->{
      final int ipCompare = i1.getUrl().getIp().compareTo(i2.getUrl().getIp());
            if(ipCompare == 0){
            return Integer.compare(i1.getUrl().getPort(),i2.getUrl().getPort());
            }
            return ipCompare;
        }).findFirst().get();

    }
}

```

```java
@Component
public class ConsumerComponent {
    @Reference(loadbalance = "onlyFirst")
    private HelloService helloService;

    public String sayHello(String name, int timeToWait) {
        return helloService.sayHello(name, timeToWait);
    }
}
```



### Dubbo的异步调用：

Dubbo不只提供了堵塞式的的同步调用，同时提供了异步调用的方式。这种方式主要应用于提供者接口 响应耗时明显，消费者端可以利用调用接口的时间去做一些其他的接口调用,利用 Future 模式来异步等 待和获取结果即可。这种方式可以大大的提升消费者端的利用率。 目前这种方式可以通过XML的方式进 行引入。

异步配置：

```xml
<dubbo:reference id="helloService" interface="com.lagou.service.HelloService">
	<dubbo:method name="sayHello" async="true" />
</dubbo:reference>
```

**异步调用特殊说明：** 

需要特别说明的是，该方式的使用，请确保dubbo的版本在2.5.4及以后的版本使用。 原因在于在2.5.3 及之前的版本使用的时候，会出现异步状态传递问题。 比如我们的服务调用关系是 A -> B -> C , 这时候如果A向B发起了异步请求，在错误的版本时，B向C发 起的请求也会连带的产生异步请求。这是因为在底层实现层面，他是通过 RPCContext 中的 attachment 实现的。在A向B发起异步请求时，会在 attachment 中增加一个异步标示字段来表明异步 等待结果。B在接受到A中的请求时，会通过该字段来判断是否是异步处理。但是由于值传递问题，B向 C发起时同样会将该值进行传递，导致C误以为需要异步结果，导致返回空。这个问题在2.5.4及以后的 版本进行了修正。

### Dubbo自定义线程池：

**Dubbo已有线程池：**

dubbo在使用时，都是通过创建真实的业务线程池进行操作的。目前已知的线程池模型有两个和java中 的相互对应: String sayHello(String name, int timeToWait);  

- fix: 表示创建固定大小的线程池。也是Dubbo默认的使用方式，默认创建的执行线程数为200，并 且是没有任何等待队列的。所以再极端的情况下可能会存在问题，比如某个操作大量执行时，可能 存在堵塞的情况。后面也会讲相关的处理办法。 

- cache: 创建非固定大小的线程池，当线程不足时，会自动创建新的线程。但是使用这种的时候需 要注意，如果突然有高TPS的请求过来，方法没有及时完成，则会造成大量的线程创建，对系统的 CPU和负载都是压力，执行越多反而会拖慢整个系统。

```java
public class WachingThreadPool  extends FixedThreadPool  implements  Runnable{
    private  static  final Logger  LOGGER = LoggerFactory.getLogger(WachingThreadPool.class);
    // 定义线程池使用的阀值
    private  static  final  double  ALARM_PERCENT = 0.90;
    private  final Map<URL, ThreadPoolExecutor>    THREAD_POOLS = new ConcurrentHashMap<>();
    public  WachingThreadPool(){
        // 每隔3秒打印线程使用情况
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this,1,3, TimeUnit.SECONDS);
    }
    // 通过父类创建线程池
    @Override
    public Executor getExecutor(URL url) {
         final  Executor executor = super.getExecutor(url);
         if(executor instanceof  ThreadPoolExecutor){
             THREAD_POOLS.put(url,(ThreadPoolExecutor)executor);
         }
         return  executor;
    }

    @Override
    public void run() {
         // 遍历线程池
         for (Map.Entry<URL,ThreadPoolExecutor> entry: THREAD_POOLS.entrySet()){
              final   URL  url = entry.getKey();
              final   ThreadPoolExecutor  executor = entry.getValue();
              // 计算相关指标
              final  int  activeCount  = executor.getActiveCount();
              final  int  poolSize = executor.getCorePoolSize();
              double  usedPercent = activeCount / (poolSize*1.0);
              LOGGER.info("线程池执行状态:[{}/{}:{}%]",activeCount,poolSize,usedPercent*100);
              if (usedPercent > ALARM_PERCENT){
                  LOGGER.error("超出警戒线! host:{} 当前使用率是:{},URL:{}",url.getIp(),usedPercent*100,url);
              }

         }
    }
}
```



### Dubbo的路由与上线系统结合：

当公司到了一定的规模之后，一般都会有自己的上线系统，专门用于服务上线。方便后期进行维护和记录的追查。我们去想象这样的一个场景，一个dubbo的提供者要准备进行上线，一般都提供多台提供者 来同时在线上提供服务**。这时候一个请求刚到达一个提供者，提供者却进行了关闭操作。那么此次请求 就应该认定为失败了。所以基于这样的场景，我们可以通过路由的规则，把预发布(灰度)的机器进行从 机器列表中移除。并且等待一定的时间，让其把现有的请求处理完成之后再进行关闭服务。同时，在启动时，同样需要等待一定的时间，以免因为尚未重启结束，就已经注册上去。等启动到达一定时间之 后，再进行开启流量操作。**（就是上线的服务先不调用，后续重启完毕重新调用）

**实现思路：**

1.利用zookeeper的路径感知能力，在服务准备进行重启之前将当前机器的IP地址和应用名写入 zookeeper。 

2.服务消费者监听该目录，读取其中需要进行关闭的应用名和机器IP列表并且保存到内存中。 

3.当前请求过来时，判断是否是请求该应用，如果是请求重启应用，则将该提供者从服务列表中移除。

**实现代码：**

```java
public class ReadyRestartInstances  implements PathChildrenCacheListener {
    private  static  final Logger  LOGGER  = LoggerFactory.getLogger( ReadyRestartInstances.class);
    private  static  final  String LISTEN_PATHS ="/lagou/dubbo/restart/instances";
    private   final   CuratorFramework  zkClient;
    // 当节点变化时给这个集合赋值重启机器的信息列表
    private volatile Set<String>  restartInstances = new HashSet<>();

    private  ReadyRestartInstances(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }
    public static  ReadyRestartInstances  create(){
        final   CuratorFramework  zookeeperClient = ZookeeperClients.client();
        try {
            // 检查监听路径是否存在
            final Stat stat =  zookeeperClient.checkExists().forPath(LISTEN_PATHS);
            // 如果监听路径不存在则创建
            if (stat == null){
         zookeeperClient.create().creatingParentsIfNeeded().forPath(LISTEN_PATHS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("确保基础路径存在");
        }
        final   ReadyRestartInstances  instances = new ReadyRestartInstances(zookeeperClient);
        // 创建一个NodeCache
        PathChildrenCache  nodeCache = new PathChildrenCache(zookeeperClient,LISTEN_PATHS,false);
        // 给节点缓存对象加入监听
        nodeCache.getListenable().addListener(instances);
        try {
            nodeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("启动路径监听失败");
        }
        return instances;
    }
    /** 返回应用名和主机拼接后的字符串 */
    private  String   buildApplicationAndInstanceString(String  applicationName,String host){
        return  applicationName + "_" + host;
    }
    /** 增加重启实例的配置信息方法 */
    public void  addRestartingInstance(String applicationName,String host) throws  Exception{
         zkClient.create().creatingParentsIfNeeded().forPath(LISTEN_PATHS + "/" + buildApplicationAndInstanceString(applicationName,host));
    }
    /** 删除重启实例的配置信息方法 */
    public void removeRestartingInstance(String applicationName,String host) throws  Exception{
        zkClient.delete().forPath(LISTEN_PATHS + "/" + buildApplicationAndInstanceString(applicationName,host));
    }
    /** 判断节点信息是否存在于 restartInstances */
    public  boolean  hasRestartingInstance(String applicationName,String host){
        return  restartInstances.contains(buildApplicationAndInstanceString(applicationName,host));
    }
    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
          // 查询出监听路径下 所有的目录配置信息
        final List<String>  restartingInstances = zkClient.getChildren().forPath(LISTEN_PATHS);
        // 给 restartInstances
        if(CollectionUtils.isEmpty(restartingInstances)){
            this.restartInstances = Collections.emptySet();
        }else{
            this.restartInstances = new HashSet<>(restartingInstances);
        }
    }
}
```



### Dubbo 支持服务降级吗？ 

**什么是服务降级 ？**

服务降级，当服务器压力剧增的情况下，根据当前业务情况及流量对一些服务有策略的降低服务级别， 以释放服务器资源，保证核心任务的正常运行。

**为什么要服务降级？**

而为什么要使用服务降级，这是防止分布式服务发生雪崩效应，什么是雪崩？就是蝴蝶效应，当一个请求发生超时，一直等待着服务响应，那么在高并发情况下，很多请求都是因为这样一直等着响应，直到服务资源耗尽产生宕机，而宕机之后会导致分布式其他服务调用该宕机的服务也会出现资源耗尽宕机， 这样下去将导致整个分布式服务都瘫痪，这就是雪崩。

dubbo 服务降级实现方式 

**第一种：在 dubbo 管理控制台配置服务降级**

![image-20201017180245722](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201017180245722.png)

 屏蔽和容错 

- mock=force:return+null **表示消费方对该服务的方法调用都直接返回 null 值，不发起远程调用**。**用来屏蔽不重要服务不可用时对调用方的影响。** 
- mock=fail:return+null 表示**消费方对该服务的方法调用在失败后，再返回 null 值，不抛异常**。**用来容忍不重要服务不稳定时对调用方的影响。**

**第二种：配置项指定**

```xml
<dubbo:reference id="xxService" check="false" interface="com.xx.XxService"
timeout="3000" mock="return null" />
    
<dubbo:reference id="xxService2" check="false" interface="com.xx.XxService2"
timeout="3000" mock="return 1234" />
```

**第三种：使用java代码 动态写入配置中心**

```java
RegistryFactory registryFactory =
ExtensionLoader.getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();

Registry registry = registryFactory.getRegistry(URL.valueOf("zookeeper://IP:端口"));

registry.register(URL.valueOf("override://0.0.0.0/com.foo.BarService?category=configurators&dynamic=false&application=foo&mock=force:return+null"));
```

**第四种：整合 hystrix 会在后期SpringCloud课程中详细讲解**





## Dubbo源码：

### Dubbo源码：注册中心Zookeeper剖析

**注册中心Zookeeper目录结构**

只有一个提供者和消费者。 com.lagou.service.HelloService 为我们所提供的服务

```java
public interface HelloService {
	String sayHello(String name);
}
```

服务下的节点列表：

![image-20201012230504682](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201012230504682.png)

![image-20201012234540671](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201012234540671.png)

如上图，每个服务下面又分别有四个配置项

1. consumers: 当前服务下面所有的消费者列表(URL) 
2. providers: 当前服务下面所有的提供者列表(URL) 
3. configuration: 当前服务下面的配置信息信息，provider或者consumer会通过读取这里的配 置信息来获取配置 
4. routers: 当消费者在进行获取提供者的时，会通过这里配置好的路由来进行适配匹配规则

![image-20201012231510414](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201012231510414.png)

通过这张图我们可以了解到如下信息:

- 提供者会在 providers 目录下进行自身的进行注册。 
- 消费者会在 consumers 目录下进行自身注册，并且监听 provider 目录，以此通过监听提供者增 加或者减少，实现服务发现。 
- Monitor模块会对整个服务级别做监听，用来得知整体的服务情况。以此就能更多的对整体情况做 监控

### Dubbo服务的本地暴露和远程暴露,他们的区别？

- 本地暴露是暴露在JVM中，不需要网络通信。每个服务默认都会在本地暴露。在引用服务的时候，优先引用本地服务。（引用服务的条件是要在同一JVM中）本地暴露不需要调用zk来进行通讯，是暴露在同一个JVM中的，同一个JVM中。
- 远程暴露是将IP，端口等信息暴露给远程客户端，调用时需要网络通信

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200817161709643.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3VuaXF1ZXdvbmRlcnE=,size_16,color_FFFFFF,t_70#pic_center)

如下图：通过Contants的字段判断是远程还是本地暴露

<img src="https://imgconvert.csdnimg.cn/aHR0cDovL2ltZy5ibG9nLmNzZG4ubmV0LzIwMTgwMTMxMjMzNjMyODEz?x-oss-process=image/format,png" alt="这里写图片描述" style="zoom:200%;" />

### Dubbo源码：服务的注册过程分析：

**服务注册（暴露）过程**

![image-20201012231852141](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201012231852141.png)

上图解析：

**具体服务到invoker的转换：**

```java
ServiceConfig.class
//1.拿到ProxyFactory类型
ProxyFactory PROXY_FACTORY = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
//2.代理对象执行getInvoker方法拿到invoker，ref（对外提供服务的实现类）
Invoker<?> invoker = PROXY_FACTORY.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(EXPORT_KEY, url.toFullString()));
//3.封装invoker
DelegateProviderMetaDataInvoker wrapperInvoker = new DelegateProviderMetaDataInvoker(invoker, this);
```

**invoker转换为Expoter（重点）：**

**1.ServiceConfig的export方法是入口：**

```java
ServiceConfig.class
//wrapperInvoker转换成exporter（下一步入口）
Exporter<?> exporter = PROTOCOL.export(wrapperInvoker);
exporters.add(exporter);
```

**2.上面方法调用RegistryProtocol的export方法：**

```java
RegistryProtocol.class
public <T> Exporter<T> export(final Invoker<T> originInvoker)  {
    	//获取注册中心地址（就是zookeeper节点存储地址，提供者一样）
        URL registryUrl = getRegistryUrl(originInvoker);
       	//提供者地址
        URL providerUrl = getProviderUrl(originInvoker);
    
    	//registeredProviderUrl：取当前服务需要注册到注册中心的providerURL(这个一步主要去除没必要参数，比如本地导出时所使用的qos参数等值)
    	final URL registeredProviderUrl = getUrlToRegistry(providerUrl,
registryUrl);

    	if (register) {
            //将当前的提供者注册到注册中心上去（下一步入口）
            register(registryUrl, registeredProviderUrl);
        }
}

 private void register(URL registryUrl, URL registeredProviderUrl) {
     	//获取注册中心
        Registry registry = registryFactory.getRegistry(registryUrl);
     	//对当前的服务进行注册（下一步入口）
        registry.register(registeredProviderUrl);
    }
```

下图补充：

1. RegistryService接口提供方法：

   进行对URL的注册操作、 解除对指定URL的注册、增加对指定URL的路径监听、解除对指定URL的路径监听、 查询指定URL下面的URL列表操作

![image-20201013004019591](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201013004019591.png)

**3.FailbackRegistry的register方法，主要调用第三方实现方式，出现错误增加重试机制：**

```java
FailbackRegistry.class
@Override
public void register(URL url) {
    // 上层调用
    // 主要用于保存已经注册的地址列表
    super.register(url);
    // 将一些错误的信息移除(确保当前地址可以在出现一些错误的地址时可以被删除)
    removeFailedRegistered(url);
    removeFailedUnregistered(url);
    // 发送给第三方渠道进行注册操作（下一步入口）
	doRegister(url);
    // 后台异步进行重试，也是Failback比较关键的代码
	addFailedRegistered(url);
}
```

4.doRegister方法选择（不同注册中心选择不同入口，下面以zookeeper为例）

```java
ZookeeperRegistry.class
@Override
    public void doRegister(URL url) {
        try {
            //进行创建地址（注册完毕）
            zkClient.create(toUrlPath(url), url.getParameter(DYNAMIC_KEY, true));
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
//解读 toUrlPath 方法。可以看到这里的实现也是比较简单，也验证了zookeeper源码的路径规则如下图。
private String toUrlPath(URL url) {
        return toCategoryPath(url) + PATH_SEPARATOR + URL.encode(url.toFullString());
    }
private String toCategoryPath(URL url) {
   		// 服务名称 + category(在当前的例子中是providers)
        return toServicePath(url) + PATH_SEPARATOR + url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
    }
private String toServicePath(URL url) {
    	// 接口地址
        String name = url.getServiceInterface();
        if (ANY_VALUE.equals(name)) {
            return toRootPath();
        }
   		// 根节点 + 接口地址
        return toRootDir() + URL.encode(name);
    }
```

![image-20201013002022356](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201013002022356.png)

**总结：**

服务的注册过程：

具体服务转换成invoker：

1. 拿到ProxyFactory类型PROXY_FACTORY
2. PROXY_FACTORY调用getInvoker方法拿到invoker（ref拿到具体服务实现类）
3. 封装invoker

invoker转换为Expoter

1. RegistryProtocol获取注册中心、提供者地址（中间会过滤掉qos等参数）
2. FailbackRegistry将错误地址删除，增加重试机制

### Dubbo源码：URL规则详解和服务本地缓存

 URL规则：

```java
URL格式：protocol://host:port/path?key=value&key=value

实际URL：provider://192.168.20.1:20883/com.lagou.service.HelloService?
anyhost=true&application=serviceprovider2&bind.ip=192.168.20.1&bind.port=20883&category=configurators&check=false&deprecated=false&Dubbo=2.0.2&dynamic=true&generic=false&interface=com.lagou.service
```

URL主要有以下几部分组成： 

- protocol: 协议，一般像我们的 provider 或者 consumer 在这里都是人为具体的协议 
- host: 协议的地址， override 协议所指定的 host是 0.0.0.0 代表所有的机器都生效
- port: 和上面相同，代表所处理的端口号 
- path: 服务路径，在 provider 或者 consumer 代表着真实的业务接口 
- key=value: 这些则代表具体的参数，可以理解为对这个地址的配置。比如我们 provider 服务应用名，就可以是一个配置的方式设置上去。 

**注意：Dubbo中的URL与java中的URL是有一些区别的，如下**： 

- 这里提供了针对于参数的 parameter 的增加和减少(**支持动态更改**) 

- **提供本地缓存功能**，对一些基础的数据做缓存

  

**Dubbo服务本地缓存源码：**

```java
//AbstractRegistry.class
public AbstractRegistry(URL url) {
        setUrl(url);
        if (url.getParameter(REGISTRY__LOCAL_FILE_CACHE_ENABLED, true)) {
            // Start file save timer
            syncSaveFile = url.getParameter(REGISTRY_FILESAVE_SYNC_KEY, false);
            // 默认保存路径(home/.Dubbo/Dubbo-registry-appName-address-port.cache)
            String defaultFilename = System.getProperty("user.home") + "/.Dubbo/Dubbo-registry-" + url.getParameter(APPLICATION_KEY) + "-" + url.getAddress().replaceAll(":", "-") + ".cache";
            String filename = url.getParameter(FILE_KEY, defaultFilename);
            //创建文件
            File file = null;
            if (ConfigUtils.isNotEmpty(filename)) {
                file = new File(filename);
                if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        throw new IllegalArgumentException("Invalid registry cache file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
                    }
                }
            }
            this.file = file;
            
            //加载已有的配置文件
            loadProperties();
            notify(url.getBackupUrls());
        }
    }

```

```java
//AbstractRegistry.class
private void saveProperties(URL url) {
        if (file == null) {
            return;
        }

        try {
            StringBuilder buf = new StringBuilder();
            // 获取所有通知到的地址
            Map<String, List<URL>> categoryNotified = notified.get(url);
            if (categoryNotified != null) {
                for (List<URL> us : categoryNotified.values()) {
                    for (URL u : us) {
                        // 多个地址进行拼接
                        if (buf.length() > 0) {
                            buf.append(URL_SEPARATOR);
                        }
                        buf.append(u.toFullString());
                    }
                }
            }
            //保存数据
            properties.setProperty(url.getServiceKey(), buf.toString());
            // 保存为一个新的版本号
			// 通过这种机制可以保证后面保存的记录，在重试的时候，不会重试之前的版本
            long version = lastCacheChanged.incrementAndGet();
            if (syncSaveFile) {
                doSaveProperties(version);
            } else {
                // 否则则异步去进行处理
                registryCacheExecutor.execute(new SaveProperties(version));
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
 }
```

```java
 public void doSaveProperties(long version) {
        if (version < lastCacheChanged.get()) {
            return;
        }
        if (file == null) {
            return;
        }
        // Save
        try {
            // 使用文件级别所，来保证同一段时间只会有一个线程进行读取操作
            File lockfile = new File(file.getAbsolutePath() + ".lock");
            if (!lockfile.exists()) {
                lockfile.createNewFile();
            }
            try (RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
                 FileChannel channel = raf.getChannel()) {
           // 利用文件锁来保证并发的执行的情况下，只会有一个线程执行成功(原因在于可能是跨VM的)
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    throw new IOException("Can not lock the registry cache file " + file.getAbsolutePath() + ", ignore and retry later, maybe multi java process use the file, please config: Dubbo.registry.file=xxx.properties");
                }
                // Save
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                 // 将配置的文件信息保存到文件中
                 try (FileOutputStream outputFile = new FileOutputStream(file)) {
                        properties.store(outputFile, "Dubbo Registry Cache");
                    }
                } finally {
                    // 解开文件锁
                    lock.release();
                }
            }
        } catch (Throwable e) {
            // 执行出现错误时，则交给专门的线程去进行重试
            savePropertiesRetryTimes.incrementAndGet();
            if (savePropertiesRetryTimes.get() >= MAX_RETRY_TIMES_SAVE_PROPERTIES) {
                logger.warn("Failed to save registry cache file after retrying " + MAX_RETRY_TIMES_SAVE_PROPERTIES + " times, cause: " + e.getMessage(), e);
                savePropertiesRetryTimes.set(0);
                return;
            }
            if (version < lastCacheChanged.get()) {
                savePropertiesRetryTimes.set(0);
                return;
            } else {
                registryCacheExecutor.execute(new SaveProperties(lastCacheChanged.incrementAndGet()));
            }
            logger.warn("Failed to save registry cache file, will retry, cause: " + e.getMessage(), e);
        }
    }
```



### Dubbo源码：Dubbo 消费过程分析

这里主要做几件事

1、获取所有服务引用

2、获取所有invoker列表

3、经过cluster筛选出具体的某个invoker（路由、负载均衡）

4、创建服务代理

**服务引用时序图：**

![img](https://img2020.cnblogs.com/blog/1033661/202010/1033661-20201011142221328-1381170910.png)

https://www.cnblogs.com/qsky/p/13797566.html



### Dubbo最小活跃数算法中是如何统计这个活跃数的

**算法原理**：总的来说就是，选最小活跃数的节点加入到最小活跃数的数组中， 如果最小活跃数中只有一个，直接调用，如果有多个，权重是否一致，如果一致，随机调用invoker,如果不一致，那么进行 那么根据权重比例进行调用

**最少活跃数的含义：**

**官方解释**：最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差，使慢的机器收到更少。

**例如**，每个服务维护一个活跃数计数器。当A机器开始处理请求，该计数器加1，此时A还未处理完成。若处理完毕则计数器减1。而B机器接受到请求后很快处理完毕。那么A,B的活跃数分别是1，0。当又产生了一个新的请求，则选择B机器去执行(B活跃数最小)，这样使慢的机器A收到少的请求。

**最少活跃数的实现分析：**

LeastActiveLoadBalance 类实现了最小活跃负载均衡。
![这里写图片描述](https://img-blog.csdn.net/20170507141918353?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvUmV2aXZlZHN1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

实现代码如下。

```java
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    public static final String NAME = "leastactive";

    private final Random random = new Random();

    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size(); // 总个数
        int leastActive = -1; // 最小的活跃数
        int leastCount = 0; // 相同最小活跃数的个数
        int[] leastIndexs = new int[length]; // 相同最小活跃数的下标
        int totalWeight = 0; // 总权重
        int firstWeight = 0; // 第一个权重，用于于计算是否相同
        boolean sameWeight = true; // 是否所有权重相同
        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive(); // 活跃数
            int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT); // 权重
            if (leastActive == -1 || active < leastActive) { // 发现更小的活跃数，重新开始
                leastActive = active; // 记录最小活跃数
                leastCount = 1; // 重新统计相同最小活跃数的个数
                leastIndexs[0] = i; // 重新记录最小活跃数下标
                totalWeight = weight; // 重新累计总权重
                firstWeight = weight; // 记录第一个权重
                sameWeight = true; // 还原权重相同标识
            } else if (active == leastActive) { // 累计相同最小的活跃数
                leastIndexs[leastCount ++] = i; // 累计相同最小活跃数下标
                totalWeight += weight; // 累计总权重
                // 判断所有权重是否一样
                if (sameWeight && i > 0 
                        && weight != firstWeight) {
                    sameWeight = false;
                }
            }
        }
        // assert(leastCount > 0)
        if (leastCount == 1) {
            // 如果只有一个最小则直接返回
            return invokers.get(leastIndexs[0]);
        }
        if (! sameWeight && totalWeight > 0) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offsetWeight = random.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexs[i];
                offsetWeight -= getWeight(invokers.get(leastIndex), invocation);
                if (offsetWeight <= 0)
                    return invokers.get(leastIndex);
            }
        }
        // 如果权重相同或权重为0则均等随机
        return invokers.get(leastIndexs[random.nextInt(leastCount)]);
    }
}
```

这个算法，总体上可分为两部分。

1. 活跃数与权重统计。
2. 选择invoker。

**活跃数与权重统计：**

统计最少活跃invoker的数量，总权重，及当有多个最小活跃数相同的Invoker时其权重(weight)是否相等。

```java
        for (int i = 0; i < length; i++) {
            Invoker<T> invoker = invokers.get(i);
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive(); // 活跃数
            int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT); // 权重
            if (leastActive == -1 || active < leastActive) { // 发现更小的活跃数，重新开始
                leastActive = active; // 记录最小活跃数
                leastCount = 1; // 重新统计相同最小活跃数的个数
                leastIndexs[0] = i; // 重新记录最小活跃数下标
                totalWeight = weight; // 重新累计总权重
                firstWeight = weight; // 记录第一个权重
                sameWeight = true; // 还原权重相同标识
            } else if (active == leastActive) { // 累计相同最小的活跃数
                leastIndexs[leastCount ++] = i; // 累计相同最小活跃数下标
                totalWeight += weight; // 累计总权重
                // 判断所有权重是否一样
                if (sameWeight && i > 0 
                        && weight != firstWeight) {
                    sameWeight = false;
                }
            }
        }
```

**选择invoker：**

如果具有最小活跃数的invoker只有一个，直接返回该Invoker。

```java
        if (leastCount == 1) {
            // 如果只有一个最小则直接返回
            return invokers.get(leastIndexs[0]);
        }1234
```

如果最小活跃数的invoker有多个，且权重不相等同时总权重大于0，这是随机生成一个权重，范围在[0，totalWeight) 间内。最后**根据随机生成的权重，来选择invoker**。

```java
        if (! sameWeight && totalWeight > 0) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offsetWeight = random.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < leastCount; i++) {
                int leastIndex = leastIndexs[i];
                offsetWeight -= getWeight(invokers.get(leastIndex), invocation);
                if (offsetWeight <= 0)
                    return invokers.get(leastIndex);
            }
        }
```

例如有3个invoker，权重分别为100,200,300
![这里写图片描述](https://img-blog.csdn.net/20170507142619844?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvUmV2aXZlZHN1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
经过前面的统计，记录了总权重是600，随机生成的权重范围是[0,600) ，若随机值为180，那么用随机生成的权重180依次去A(100),B(200),C(300)的权重，当减到B时候结果 <= 0，因此选择B。

所以通过这种方法，即用随机权重值从前向后减每个invoker的权重，结果<=0说明落在哪个invoker的范围内，最终确定invoker。

如果不满足前面3中情况，则从最小活跃数相同的invoker中随机选择一个invoker。

```
// 如果权重相同或权重为0则均等随机
return invokers.get(leastIndexs[random.nextInt(leastCount)]);
```

**活跃数的变化：**

活跃数的修改发生在com.alibaba.dubbo.rpc.filter.ActiveLimitFilter中。若未配置actives属性，则每进行一次调用前该invoker关联的活跃数加1，调用结束后活跃数减1。

beginCount对活跃数加1，endCount对活跃数减1。

```java
            long begin = System.currentTimeMillis();
            RpcStatus.beginCount(url, methodName);
            try {
                Result result = invoker.invoke(invocation);
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, true);
                return result;
            } catch (RuntimeException t) {
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, false);
                throw t;
            }
```

如果使用LeastActive负载均衡，则需要启用ActiveLimitFilter，这样活跃数才会变化。

因此需要配置filter，filter 为 “activelimit”。

```xml
<dubbo:service interface="service.DemoService" protocol="in,out" ref = "demoService" loadbalance="leastactive" filter="activelimit"/>1
```

dubbo中包含一些内置filter,其描述在如下文件。

![这里写图片描述](https://img-blog.csdn.net/20170507143352986?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvUmV2aXZlZHN1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### Dubbo 用到哪些设计模式？

**工厂模式：**

Provider 在 export 服务时，会调用 ServiceConfig 的 export 方法。ServiceConfig 中有个字段：

```java
private static final Protocol protocol =
ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtensi
on();
```

**装饰器模式、责任链模式:**

什么是装饰器模式：

向一个现有的对象添加新的功能，同时又不改变其结构。这种类型的设计模式属于结构型模式，它是作为现有的类的一个包装。

```java
例：
public interface Shape {
   void draw();
}
public abstract class ShapeDecorator implements Shape {
}
创建扩展 ShapeDecorator 类的实体装饰类
public class RedShapeDecorator extends ShapeDecorator {
}
```

Dubbo 在启动和调用阶段都大量使用了装饰器模式。（还需待定）

**观察者模式：** 

什么是观察者模式？

当对象间存在一对多关系时，则使用观察者模式。比如，当一个对象被修改时，则会自动通知依赖它的对象。观察者模式属于行为型模式。（例：几个类都依赖一个接口，原始接口方法调用时，其他实现类方法就会被触发）

Dubbo 的 Provider 启动时，需要与注册中心交互，先注册自己的服务，再订阅自己的服务，订阅时，采用了观察者模式，开启一个listener。注册中心会每 5 秒定时检查是否有服务更新，如果有更新，向该服务的提供者发送一个notify消息， provider接受到notify 消息后，即运行NotifyListener 的 notify方法，执行监听器方法。

**动态代理模式 ：**

Dubbo 扩展 JDK SPI 的类 ExtensionLoader 的 Adaptive 实现是典型的动态代理实现。



### 粘包拆包问题：

​	dubbo在处理tcp的粘包和拆包时是借助InternalDecoder的buffer缓存对象来缓存**不完整**的dubbo协议栈数据，等待下次inbound事件，合并进去。所以说在dubbo中解决TCP拆包和粘包的时候是通过buffer 变量来解决的。（如果协议栈数据不完整，会触发ExchangeCodec返回NEED_MORE_INPUT，这时会回滚读索引，等待第二次的inbound消息的到来）

### Dubbo路由使用：

**路由规则：**

通过上面的程序，我们实际本质上就是通过在zookeeper中保存一个节点数据，来记录路由规则。消费者会通过监听这个服务的路径，来感知整个服务的路由规则配置，然后进行适配。这里主要介绍路由配置的参数。具体请参考文档, 这里只对关键的参数做说明。 

- route:// 表示路由规则的类型，支持条件路由规则和脚本路由规则，可扩展，必填。
- 0.0.0.0 表示对所有 IP 地址生效，如果只想对某个 IP 的生效，请填入具体 IP，必填。 com.lagou.service.HelloService 表示只对指定服务生效，必填。 
- category=routers 表示该数据为动态配置类型，必填。 
- dynamic : 是否为持久数据，当指定服务重启时是否继续生效。必填。
- runtime : 是否在设置规则时自动缓存规则，如果设置为true则会影响部分性能。 
- rule : 是整个路由最关键的配置，用于配置路由规则。 ... => ... 在这里 => 前面的就是表示消费者方的匹配规则，可以不填(代表全部)。 => 后方则必 须填写，表示当请求过来时，如果选择提供者的配置。官方这块儿也给出了详细的示例，可以按照 那里来讲。 
- 其中使用最多的便是 host 参数。 必填。

消费端想指定哪个服务端调用或者排除哪个服务端可使用该方法：

```java
public class DubboRouterMain {
	public static void main(String[] args) {
		RegistryFactory registryFactory =
ExtensionLoader.getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();
		Registry registry =
registryFactory.getRegistry(URL.valueOf("zookeeper://127.0.0.1:2181"));
		registry.register(URL.valueOf("condition://0.0.0.0/com.lagou.service.HelloServi
ce?category=routers&force=true&dynamic=true&rule=" + URL.encode("=> host != 你的
机器ip不能是127.0.0.1")));
	}
}
```

#### 路由规则使用实例：

路由规则-1 : 禁止所有消费者访问主机 192.168.0.101上的服务
路由规则-2 : 指定 application = dubbo.test.consumer-1 的消费者 访问 主机192.168.0.101上面的服务

```java
registry.register(URL.valueOf("condition://0.0.0.0/dubbo.test.interfaces.TestService?category=routers&name=路由规则-1&dynamic=true&priority=2&enabled=true&rule=" + URL.encode(" => host != 192.168.0.101")));

registry.register(URL.valueOf("condition://0.0.0.0/dubbo.test.interfaces.TestService?category=routers&name=路由规则-2&dynamic=true&priority=1&enabled=true&rule=" + URL.encode("application = dubbo.test.consumer-1 => host = 192.168.0.101")));
```

### 路由实现原理：

##### 这里主要对路由执行链的ConditionRouter 的实现来做说明：

路由源码有两个属性很关键：

```java
// 是否满足判断条件
protected Map<String, MatchPair> whenCondition;
// 当满足判断条件时如何选择invokers
protected Map<String, MatchPair> thenCondition;
```

MatchPair组成：）我们可以看到每一个 MatchPair 都有这两个属性,分别表示满足的条件和不满足的具体条件。

```java
final Set<String> matches = new HashSet<String>();
final Set<String> mismatches = new HashSet<String>()
```



### Dubbo服务之间的调用是阻塞的吗？

默认是同步等待结果阻塞的，支持异步调用。

Dubbo 是基于 NIO 的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小，异步调用会返回一个 Future 对象。

异步调用流程图如下。

![img](https://img.jbzj.com/file_images/article/202005/20200526170417154.jpg)



分库要说10个库，中间件重点，

zookeeper选举方法、分布式锁

spring用到的设计模式，最近看什么书，收获了什么

mvc问的比较多，比如插件

算法

![image-20210124173407266](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210124173407266.png)

nohup sh mqbroker -n 192.168.1.23:9876 -c broker.p autoCreateTopicEnable=true > /logs/rocketmqlogs/broker.log 2>&1 &