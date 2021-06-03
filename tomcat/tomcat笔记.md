[TOC]

## tomcat结构介绍

### **1.tomcat目录结构：**

1.1 bin目录：

![image-20200901091040583](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200901091040583.png)

1.2 conf目录：图中logging.properties说明有误，该文件应该是关于日志的配置

![image-20200901092245814](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200901092245814.png)

1.3 work：存放过程文件，jsp编译、运行时会产生一些过程文件就存放在这里。

那么redirectPort属性的作用是什么呢？
当用户用http请求某个资源，而该资源本身又被设置了必须要https方式访问，此时Tomcat会自动重定向到这个redirectPort设置的https端口。



### **2.浏览器访问服务器流程：**

http请求的处理过程：

![image-20200902163319412](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902163319412.png)

![image-20200902164526317](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902164526317.png)

上图中http请求只是定义了数据的通信格式，是应用层协议。真正**数据传输依靠的是TCP/IP协议**，期间经历**三次握手**。第6步tcp包括请求头和数据，**http请求头和请求体就在tcp数据**里。



## http无状态、无连接？

无状态：

无连接：

session和cookie的优缺点

https://www.cnblogs.com/lingyejun/p/9282169.html

### 3.Tomcat 系统总体架构：

**3.1 Tomcat 请求处理大致过程**
![image-20200902164053600](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902164053600.png)

如果上图tomcat直接调用java业务处理类会出现耦合问题，所以Servlet容器出现解决该问题。

![image-20200902164257337](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902164257337.png)

Tomcat的两个重要身份
1）http服务器
2）Tomcat是⼀个Servlet容器（按照Servlet规范的要求去实现了Servlet容器）

**Servlet规范：**Servlet 容器通过Servlet接⼝调⽤业务类。Servlet接⼝和Servlet容器这⼀整套内容叫作Servlet规范。

**3.2 Tomcat Servlet容器处理流程**

当⽤户请求某个URL资源时
1）HTTP服务器会把请求信息使⽤ServletRequest对象封装起来
2）进⼀步去调用Servlet容器中某个具体的Servlet
3）在 2）中，Servlet容器拿到请求后，根据URL和Servlet的映射关系，找到相应的Servlet
4）如果Servlet还**没有被加载**，就用**反射机制创建这个Servlet**，并**调用Servlet的init方法来完成初始化**
5）接着调用这个具体Servlet的service⽅法来处理请求，请求处理结果使⽤ServletResponse对象封装
6）把ServletResponse对象返回给HTTP服务器，HTTP服务器会把响应发送给客户端

![image-20200902170602309](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902170602309.png)

**3.3 Tomcat 系统总体架构**

![image-20200902171410355](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902171410355.png)

Tomcat 设计了两个核⼼组件连接器（Connector）和容器（Container）来完成 Tomcat 的两⼤核⼼
功能。
**连接器，负责对外交流：** 处理Socket连接，负责⽹络字节流与Request和Response对象的转化；
**容器，负责内部处理：**加载和管理Servlet，以及具体处理Request请求；



### 4.Tomcat 连接器组件 Coyote

**4.1 Coyote 简介**

Coyote 是Tomcat 中连接器的组件名称 , **是对外的接口**。客户端通过Coyote与服务器建立连接、发送请
求并接受响应 。

（1）Coyote 封装了底层的网络通信（就是封装了Socket 请求及响应处理）
（2）Coyote 使Catalina 容器（容器组件）与具体的请求协议及IO操作方式完全解耦（意思是具体的请求协议及IO操作方式交给Coyote）
（3）Coyote 将Socket 输⼊转换封装为 Request 对象，进⼀步封装后交由Catalina 容器进行处理，处
理请求完成后, Catalina 通过Coyote 提供的Response 对象将结果写入输出流（Request 转为 ServletRequest 是由Coyote 的adapter负责的）
（4）Coyote 负责的是具体协议（应用层）和IO（传输层）相关内容

![image-20200902172412255](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902172412255.png)

Tomcat Coyote 支持的 IO模型与协议
Tomcat支持多种应用层协议和I/O模型，如下：

![image-20200902172533614](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902172533614.png)

**默认协议是HTTP/1.1 ,默认IO模型是NIO。**

在 8.0 之前 ，Tomcat 默认采⽤的I/O⽅式为 BIO，之后改为 NIO。 ⽆论 NIO、NIO2 还是 APR， 在性
能⽅⾯均优于以往的BIO。 如果采⽤APR， 甚⾄可以达到 Apache HTTP Server 的影响性能。

**4.2 Coyote 的内部组件及流程**

![image-20200902190454949](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902190454949.png)

![image-20200902190459935](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902190459935.png)

| 组件            | 作用描述                                                     |
| --------------- | ------------------------------------------------------------ |
| EndPoint        | 通信监听的接口，是**具体Socket接收和发送处理器**，是对传输层的抽象，因此EndPoint⽤来**实现TCP/IP协议**的 |
| Processor       | 协议处理接口，**用来实现HTTP协议**，接收来⾃EndPoint的Socket，**读取字节流解析成Tomcat Request和Response**。 |
| ProtocolHandler | 协议接⼝，**针对协议解析处理**，Tomcat 按照协议和I/O 提供了6个实现类。 |
| Adapter         | 将tomcat的原生Request转成ServletRequest，再调⽤容器          |

### 5.Tomcat Servlet 容器 Catalina

Catalina 是 Tomcat 的核心，其他模块是为Catalina提供支撑的。

tomcat模块分层结构：

![](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902191829362.png)

**Catalina结构：**

![image-20200902192131460](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200902192131460.png)

可以认为整个Tomcat就是⼀个Catalina实例，Tomcat 启动的时候会初始化这个实例，Catalina
实例通过加载server.xml完成其他实例的创建，创建并管理⼀个Server，Server创建并管理多个服务（Service），每个服务⼜可以有多个Connector和⼀个Container。

- **Catalina：**负责解析Tomcat的配置⽂件（server.xml） , 以此来创建服务器Server组件并进行管理。

- **Server：**负责组装并启动Servlet引擎,Tomcat连接器。Server通过实现Lifecycle接⼝，提供了⼀种优雅的启动和关闭整个系统的方式

- **Service：**它将若⼲个Connector组件绑定到⼀个Container。

- **Container：**容器，负责处理⽤户的servlet请求，并返回对象给web⽤户的模块。

  **|-Engine：**表示整个Catalina的Servlet引擎，⽤来管理多个虚拟站点，⼀个Service最多只能有⼀  					      个Engine，但是⼀个引擎可包含多个Host。

  ​      **|-Host：**代表⼀个虚拟主机，或者说⼀个站点。可以给Tomcat配置多个虚拟主机地址，而⼀个虚					   拟主机下可包含多个Context。

  ​	  **|-Context：**表示⼀个Web应⽤程序， ⼀个Web应⽤可包含多个Wrapper	

  ​	  **|-Wrapper：**表示⼀个Servlet，Wrapper 作为容器中的最底层，不能包含子容器

**上述组件的配置其实就体现在conf/server.xml中。**

### **6.Tomcat 服务器核心配置详解**

- Tomcat 作为服务器的配置，主要是 server.xml ⽂件的配置；

- server.xml中包含了 Servlet容器的相关配置，即 Catalina 的配置；

  

```xml
<!--port：关闭服务器的监听端⼝ shutdown：关闭服务器的指令字符串-->
<Server port="8005" shutdown="SHUTDOWN">
	<!-- 以⽇志形式输出服务器 、操作系统、JVM的版本信息 -->
	<Listener className="org.apache.catalina.startup.VersionLoggerListener" />
	<!-- 加载（服务器启动） 和 销毁 （服务器停⽌） APR。 如果找不到APR库， 则会输出⽇志， 并不影响   			 Tomcat启动 -->
	<Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
	<!-- 避免JRE内存泄漏问题 -->
	<Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
	<!-- 加载（服务器启动） 和 销毁（服务器停⽌） 全局命名服务 -->
	<Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
	<!-- 在Context停⽌时重建 Executor 池中的线程， 以避免ThreadLocal 相关的内存泄漏 -->
	<Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
	<!--GlobalNamingResources 中定义了全局命名服务-->
    <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
    type="org.apache.catalina.UserDatabase"
    description="User database that can be updated and saved"
    factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
    pathname="conf/tomcat-users.xml" />
    </GlobalNamingResources>

<!--
该标签⽤于创建 Service 实例，默认使⽤ org.apache.catalina.core.StandardService。
默认情况下，Tomcat 仅指定了Service 的名称， 值为 "Catalina"。
Service ⼦标签为 ： Listener、Executor、Connector、Engine，
其中：
Listener ⽤于为Service添加⽣命周期监听器，
Executor ⽤于配置Service 共享线程池，
Connector ⽤于配置Service 包含的链接器，
Engine ⽤于配置Service中链接器对应的Servlet 容器引擎
-->
<Service name="Catalina">
    <!--
    默认情况下，Service 并未添加共享线程池配置。 如果我们想添加⼀个线程池，可以在
    <Service> 下添加如下配置：
    name：线程池名称，⽤于 Connector中指定
    namePrefix：所创建的每个线程的名称前缀，⼀个单独的线程名称为
    namePrefix+threadNumber
    maxThreads：池中最⼤线程数
    minSpareThreads：活跃线程数，也就是核⼼池线程数，这些线程不会被销毁，会⼀直存在
    maxIdleTime：线程空闲时间，超过该时间后，空闲线程会被销毁，默认值为6000（1分钟），单位
    毫秒
    maxQueueSize：在被执⾏前最⼤线程排队数⽬，默认为Int的最⼤值，也就是⼴义的⽆限。除⾮特
    殊情况，这个值 不需要更改，否则会有请求不会被处理的情况发⽣
    prestartminSpareThreads：启动线程池时是否启动 minSpareThreads部分线程。默认值为
    false，即不启动
    threadPriority：线程池中线程优先级，默认值为5，值从1到10
    className：线程池实现类，未指定情况下，默认实现类为
    org.apache.catalina.core.StandardThreadExecutor。如果想使⽤⾃定义线程池⾸先需要实现
    org.apache.catalina.Executor接⼝
    -->
    <Executor name="commonThreadPool"
    namePrefix="thread-exec-"
    maxThreads="200"
    minSpareThreads="100"
    maxIdleTime="60000"
    maxQueueSize="Integer.MAX_VALUE"
    prestartminSpareThreads="false"
    threadPriority="5"
    className="org.apache.catalina.core.StandardThreadExecutor"/>
    
    <!--
    port：
    端⼝号，Connector ⽤于创建服务端Socket 并进⾏监听， 以等待客户端请求链接。如果该属性设置
    为0， Tomcat将会随机选择⼀个可⽤的端⼝号给当前Connector 使⽤
    protocol：
    当前Connector ⽀持的访问协议。 默认为 HTTP/1.1 ， 并采⽤⾃动切换机制选择⼀个基于 JAVA
    NIO 的链接器或者基于本地APR的链接器（根据本地是否含有Tomcat的本地库判定）
    connectionTimeOut:
    Connector 接收链接后的等待超时时间， 单位为 毫秒。 -1 表示不超时。
    redirectPort：
    当前Connector 不⽀持SSL请求， 接收到了⼀个请求， 并且也符合security-constraint 约束，
    需要SSL传输，Catalina⾃动将请求重定向到指定的端⼝。
    executor：
    指定共享线程池的名称， 也可以通过maxThreads、minSpareThreads 等属性配置内部线程池。
    URIEncoding:
    ⽤于指定编码URI的字符编码， Tomcat8.x版本默认的编码为 UTF-8 , Tomcat7.x版本默认为ISO-
    8859-1
    -->
    <!--org.apache.coyote.http11.Http11NioProtocol ， ⾮阻塞式 Java NIO 链接器-->
    <Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000"
    redirectPort="8443" />
    <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
    
    <!--可以使⽤共享线程池-->
    <Connector port="8080"
    protocol="HTTP/1.1"
    executor="commonThreadPool"
    maxThreads="1000"
    minSpareThreads="100"
    acceptCount="1000"
    maxConnections="1000"
    connectionTimeout="20000"
    compression="on"
    compressionMinSize="2048"
    disableUploadTimeout="true"
    redirectPort="8443"
    URIEncoding="UTF-8" />
    <!--
    name： ⽤于指定Engine 的名称， 默认为Catalina
    defaultHost：默认使⽤的虚拟主机名称， 当客户端请求指向的主机⽆效时， 将交由默认的虚拟主机处
    理， 默认为localhost
    -->
    <Engine name="Catalina" defaultHost="localhost">
        <!-- Host 标签⽤于配置⼀个虚拟主机 -->
		<Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true">
            <!--
            docBase：Web应⽤⽬录或者War包的部署路径。可以是绝对路径，也可以是相对于 Host appBase的
            相对路径。
            path：Web应⽤的Context 路径。如果我们Host名为localhost， 则该web应⽤访问的根路径为：
            http://localhost:8080/web3。
            -->
			<Context docBase="/Users/yingdian/web_demo" path="/web3"></Context>
		</Host>
	</Engine>
</Service>
</Server>

配置多个Host可以实现不同路径进入不同的webapp文件：比如www.abc.com:8080进入webapp2的文件，localhost:8080进入webapp的文件

配置多个Context可以实现不同端口后缀不一样进入不同文件，比如www.abc.com:8080/root进入a文件，www.abc.com:8080/root1 进入b文件。
```

**service的作用是统一管理connector和container**，一个service可以包括多个connector和一个container。而server的作用是，管理所有的service，**一个server可以包括多个service**。**server负责管理所有service的生命周期**，这样就管理了所有的connector和container，以及connector和container的所有内部组件。**这样就不需要单独对connector和container单独进行开启或关闭了**。



## tomcat源码剖析

### Tomcat启动流程：

tomcat启动器调用catalina.bat

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200903202620528.png" alt="image-20200903202620528" style="zoom:80%;" />

catalina.bat调用bootstrap

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200903202753660.png" alt="image-20200903202753660" style="zoom:80%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200903193138027.png" alt="image-20200903193138027" style="zoom: 80%;" />



- **Executor：**共享线程池。
- **protocolhandler：**进行socket处理

- 2：创建Catalina实例
- 4：Catalina的load( )调用**createStartDigester( )**：**xml配置文件的解析器：如server.xml**
- 6：调用LifecycleBase的initInternal( )使用**模板方法模式**。（后面也有用到该模式）
- 12：Connector类使用Adapter adapter = new CoyoteAdapter(this);将Request转为ServletRequest
- 13：protocolhandler实现类AbstractProtocol调用endpoint.init( )完成socket通信。（其中bind( )实现采用i/o模型是Nio，如图1.1）
- 23：protocolhandler启动start方法时调用startAcceptorThreads( )（如图2.1.2）,启动Acceptor线程（如图2.1.3）

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904094513182.png" alt="image-20200904094513182" style="zoom:80%;" />

**2.1.1**

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904100359616.png" alt="image-20200904100359616" style="zoom:80%;" />

**2.1.2**

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904102115762.png" alt="image-20200904102115762" style="zoom:80%;" />

**2.1.3**

Tomcat中的各容器组件都会涉及创建、销毁等，因此设计了⽣命周期接⼝Lifecycle进⾏统⼀规范，各容
器组件实现该接⼝。

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200903195847731.png" alt="image-20200903195847731" style="zoom:80%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200903195623305.png" alt="image-20200903195623305" style="zoom:80%;" />



### Tomcat请求处理流程：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200903193423494.png" alt="image-20200903193423494" style="zoom:80%;" />

Mapper体系结构：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904103046126.png" alt="image-20200904103046126" style="zoom:80%;" />

Server.xml层级关系如图2.2.1

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904112735673.png" alt="image-20200904112735673" style="zoom:80%;" />

**Mapper完成映射：**都继承MapElement静态抽象类，MappedHost和MappedContext实现一对多的关系（另外一个类似）代码如图2.2.1、2.2.2、2.2.3。

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904104302415.png" alt="image-20200904104302415" style="zoom:80%;" />

2.2.1

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904104341129.png" alt="image-20200904104341129" style="zoom:80%;" />

2.2.2

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904104447225.png" alt="image-20200904104447225" style="zoom:80%;" />

**请求处理流程示意图：**

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200903193442035.png" alt="image-20200903193442035" style="zoom:80%;" />

## tomcat类加载机制剖析、https支持、tomcat调优

### 第一部分：tomcat类加载机制剖析

Java类（.java）—> 字节码⽂件(.class) —> 字节码⽂件需要被加载到jvm内存当中（这个过程就是⼀个
类加载的过程）
类加载器（ClassLoader，说⽩了也是⼀个类，jvm启动的时候先把类加载器读取到内存当中去，其他的
类（比如各种jar中的字节码⽂件，⾃⼰开发的代码编译之后的.class⽂件等等））
要说 Tomcat 的类加载机制，⾸先需要来看看 Jvm 的类加载机制，因为 Tomcat 类加载机制是在 Jvm 类
加载机制基础之上进行了⼀些变动。

##### 第 1 节 JVM 的类加载机制

JVM 的类加载机制中有⼀个⾮常重要的⻆⾊叫做类加载器（ClassLoader），类加载器有自己的体系，
Jvm内置了⼏种类加载器，包括：引导类加载器、扩展类加载器、系统类加载器，他们之间形成父子关
系，通过 Parent 属性来定义这种关系，最终可以形成树形结构。

![image-20200904154623625](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904154623625.png)

![image-20200904154553691](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904154553691.png)

| 类加载器                                      | 作用                                                         |
| --------------------------------------------- | ------------------------------------------------------------ |
| 引导启动类加载器 BootstrapClassLoader         | c++编写，加载java核⼼库 java.*,⽐如rt.jar中的类，构造ExtClassLoader和AppClassLoader |
| 扩展类加载器 ExtClassLoader                   | java编写，加载扩展库 JAVA_HOME/lib/ext⽬录下的jar中的类，如classpath中的jre ，javax.*或者java.ext.dir指定位置中的类 |
| 系统类加载器 SystemClassLoader/AppClassLoader | 默认的类加载器，搜索环境变量 classpath 中指明的路径          |

**另外：用户可以自定义类加载器（Java编写，用户自定义的类加载器，可加载指定路径的 class ⽂件）**
　　当 JVM 运⾏过程中，用户自定义了类加载器去加载某些类时，会按照下面的步骤（⽗类委托机制）
　　1） 用户自己的类加载器，把加载请求传给父加载器，父加载器再传给其父加载器，⼀直到加载器
树的顶层
　　2 ）最顶层的类加载器⾸先针对其特定的位置加载，如果加载不到就转交给子类（意思是先去引导类加载器查找，没有再去扩展类加载器找，以此类推，这就是**双亲委派机制**）
　　3 ）如果⼀直到底层的类加载都没有加载到，那么就会抛出异常 ClassNotFoundException
　 因此，按照这个过程可以想到，如果同样在 classpath 指定的目录中和自己⼯作目录中存放相同的
class，会优先加载 classpath 目录中的文件

##### 第 2 节 双亲委派机制

**2.1 什么是双亲委派机制**
先加载上级类加载器，递归这个操作，如果上级的类加载器没有加载，自己才会去加载这个类。
**2.2 双亲委派机制的作用**

- **防止重复加载同⼀个.class**。通过委托去向上⾯问⼀问，加载过了，就不⽤再加载⼀遍。保证数据安全。
- **保证核心.class不能被篡改。**（如果先加载自己定义的，会出现问题的，那么真正的Object类就可能被篡改了）。

##### 第 3 节 tomcat 的类加载机制

Tomcat 的类加载机制相对于 Jvm 的类加载机制做了⼀些改变。
但是没有严格的遵从双亲委派机制，也可以说打破了双亲委派机制

⽐如：有⼀个tomcat，webapps下部署了两个应⽤
app1/lib/a-1.0.jar com.lagou.edu.Abc
app2/lib/a-2.0.jar com.lagou.edu.Abc

不同版本中Abc类的内容是不同的，代码是不⼀样的。

![image-20200904161204225](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904161204225.png)

- 系统类加载器正常情况下加载的是 CLASSPATH 下的类，但是 Tomcat 的启动脚本并未使⽤该变
  量，而是加载tomcat启动的类，比如bootstrap.jar，**通常在catalina.bat或者catalina.sh中指定**。
  位于CATALINA_HOME/bin下。**(载CATALINA_HOME/bin下的jar包)**
- Common 通用类加载器加载Tomcat使⽤以及应⽤通⽤的⼀些类，位于CATALINA_HOME/lib下，
  ⽐如servlet-api.jar。**(加载CATALINA_HOME/lib下的jar包)**
- Catalina ClassLoader 用于加载服务器内部可见类，这些类应⽤程序不能访问**（加载Tomcat自带jar包）**
- Shared ClassLoader 用于加载应用程序共享类，这些类服务器不会依赖。**（加载部署的几个应用共享的jar包）**
- Webapp ClassLoader，每个应⽤程序都会有⼀个独⼀⽆⼆的Webapp ClassLoader，他⽤来加载
  本应⽤程序 /WEB-INF/classes 和 /WEB-INF/lib 下的类。**（加载应用内部独有的jar包）**

**tomcat 8.5** 默认改变了严格的双亲委派机制

- ⾸先从 Bootstrap Classloader加载指定的类（引导类加载器）
- 如果未加载到，则从 /WEB-INF/classes加载（WebApp类加载器）
- 如果未加载到，则从 /WEB-INF/lib/*.jar 加载（WebApp类加载器）
- 如果未加载到，则依次从 System、Common、Shared 加载（在这最后⼀步，遵从双亲委派
  机制）

**总结：**等于说tomcat执行顺序和JVM不同的是使用**引导类加载器后使用的是WebApp类加载器**，其他都和JVM一样。



### 第二部分：tomcat对Https支持

**第一节：HTTPS 简介**

**https可使用的协议：**

ssl协议
TLS(transport layer security)协议（比ssl协议晚出现）

**HTTPS和HTTP的主要区别**

- HTTPS协议使⽤时需要到电子商务认证授权机构（CA）申请SSL证书
- HTTP默认使⽤8080端口，HTTPS默认使⽤8443端口
- HTTPS则是具有SSL加密的安全性传输协议，对数据的传输进⾏加密，效果上相当于HTTP的升级
  版
- HTTP的连接是⽆状态的，不安全的；HTTPS协议是由SSL+HTTP协议构建的可进⾏加密传输、身
  份认证的网络协议，比HTTP协议安全

**第二节：HTTPS 工作原理**

![image-20200904170331029](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904170331029.png)

https第一次进行非对称加密（如图2.1），后面进行对称加密（如图2.2）

![image-20200904172327779](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904172327779.png)

2.1

![image-20200904172926402](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904172926402.png)

2.2

**第三节：tomcat配置Https**

1） 使⽤ JDK 中的 keytool ⼯具⽣成免费的秘钥库文件(证书)。

```java
//命令执行如下命令：-genkey：产生密钥库文件；-alias：别名 -keyalg：密钥算法 -keystor：指定证书的名称
keytool -genkey -alias lagou -keyalg RSA -keystore lagou.keystore
```

![image-20200904165842020](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904165842020.png)

2） 配置conf/server.xml

```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
maxThreads="150" schema="https" secure="true" SSLEnabled="true">
<SSLHostConfig>
<Certificate certificateKeystoreFile="/Users/yingdian/workspace/servers/apache-tomcat-8.5.50/conf/lagou.keystore" certificateKeystorePassword="lagou123" type="RSA"
/>
</SSLHostConfig>
</Connector>

SSLEnabled:是否开启ssl
certificateKeystoreFile：证书的位置
certificateKeystorePassword：密钥库文件密码
type：加密算法
```

3）使⽤https协议访问8443端⼝（https://localhost:8443）。

### 第三部分：tomcat的JVM调优

**系统性能的衡量指标，主要是响应时间和吞吐量。**
1）响应时间：执⾏某个操作的耗时；
2) 吞吐量：系统在给定时间内能够⽀持的事务数量，单位为TPS（Transactions PerSecond的缩写，也
就是事务数/秒，⼀个事务是指⼀个客户机向服务器发送请求然后服务器做出反应的过程。
Tomcat优化从两个方面进行
1）JVM虚拟机优化（优化内存模型）
2）Tomcat自身配置的优化（⽐如是否使用了共享线程池？IO模型？）
学习优化的原则
提供给⼤家优化思路，没有说有明确的参数值⼤家直接去使⽤，必须根据⾃⼰的真实⽣产环境来进⾏调
整，调优是⼀个过程

##### 3.1 虚拟机运行优化（参数调整）

Java 虚拟机的运⾏优化主要是内存分配和垃圾回收策略的优化：

- 内存直接影响服务的运⾏效率和吞吐量
- 垃圾回收机制会不同程度地导致程序运⾏中断（垃圾回收策略不同，垃圾回收次数和回收效率都是
  不同的）

 JVM内存模型回顾

![image-20200907153516321](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200907153516321.png)

 Java 虚拟机内存相关参数

| 参数                 | 参数作用                                          | 优化建议                                                     |
| -------------------- | ------------------------------------------------- | ------------------------------------------------------------ |
| -server              | 启动Server，以服务端模式运行                      | 服务端模式建议开启（**默认client开启，Server模式启动较耗性能，运行时效率较高**） |
| -Xms                 | 最小堆内存                                        | 建议与-Xmx设置相同（**假如最小和最大设置的值不同，动态调整会耗费资源，所以设置相同最好**） |
| -Xmx                 | 最大堆内存                                        | 建议设置为可用内存的80%                                      |
| -XX:MetaspaceSize    | 元空间初始值                                      |                                                              |
| -XX:MaxMetaspaceSize | 元空间最⼤内存                                    | 默认无限                                                     |
| -XX:NewRatio         | 年轻代和老年代大小比值，取值为整数，默认为2       | 不需要修改                                                   |
| -XX:SurvivorRatio    | Eden区与Survivor区大小的比值，取值为整数，默认为8 | 不需要修改                                                   |

根据以上参数信息配置最优方案(配置在catalina.bat或catalina.sh的注释下)：

```java
JAVA_OPTS="-server -Xms2048m -Xmx2048m -XX:MetaspaceSize=256m -
XX:MaxMetaspaceSize=512m"
```

**调整后查看可使用JDK提供的内存映射工具**



![image-20200907154125699](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200907154125699.png)

**3.2 垃圾回收（GC）策略**

垃圾回收性能指标

- 吞吐量：⼯作时间（排除GC时间）占总时间的百分比， ⼯作时间并不仅是程序运⾏的时间，还包
  含内存分配时间。
- 暂停时间：由垃圾回收导致的应用程序停⽌响应次数/时间。

**垃圾收集器**

- 串行收集器（Serial Collector）
  单线程执⾏所有的垃圾回收⼯作， 适⽤于单核CPU服务器
  **⼯作进程-----|（单线程）垃圾回收线程进⾏垃圾收集|---⼯作进程继续**
- 并行收集器（Parallel Collector）
  **⼯作进程-----|（多线程）垃圾回收线程进⾏垃圾收集|---⼯作进程继续**
  ⼜称为吞吐量收集器（关注吞吐量）， 以并⾏的⽅式执⾏年轻代的垃圾回收， 该⽅式可以显著降
  低垃圾回收的开销(指多条垃圾收集线程并⾏⼯作，但此时⽤户线程仍然处于等待状态)。适⽤于多
  处理器或多线程硬件上运⾏的数据量较⼤的应⽤
- 并发收集器（Concurrent Collector）
  以并发的⽅式执⾏⼤部分垃圾回收⼯作，以缩短垃圾回收的暂停时间。适⽤于那些响应时间优先于
  吞吐量的应⽤， 因为该收集器虽然最⼩化了暂停时间(指⽤户线程与垃圾收集线程同时执⾏,但不⼀
  定是并⾏的，可能会交替进⾏)， 但是会降低应⽤程序的性能
- CMS收集器（Concurrent Mark Sweep Collector）
  并发标记清除收集器， 适⽤于那些更愿意缩短垃圾回收暂停时间并且负担的起与垃圾回收共享处
  理器资源的应⽤
- G1收集器（Garbage-First Garbage Collector）
  适⽤于⼤容量内存的多核服务器， 可以在满⾜垃圾回收暂停时间⽬标的同时， 以最⼤可能性实现
  ⾼吞吐量(JDK1.7之后)

**垃圾回收器参数**

| 参数                           | 描述                                                         |
| :----------------------------- | ------------------------------------------------------------ |
| -XX:+UseSerialGC               | 启⽤串⾏收集器                                               |
| -XX:+UseParallelGC             | 启⽤并⾏垃圾收集器，配置了该选项，那么 -XX:+UseParallelOldGC默认启⽤ |
| -XX:+UseParNewGC               | 年轻代采⽤并⾏收集器，如果设置了 -XX:+UseConcMarkSweepGC选项，⾃动启⽤ |
| -XX:ParallelGCThreads          | 年轻代及⽼年代垃圾回收使⽤的线程数。默认值依赖于JVM使⽤的CPU个数 |
| -XX:+UseConcMarkSweepGC（CMS） | 对于⽼年代，启⽤CMS垃圾收集器。 当并⾏收集器⽆法满⾜应⽤的延迟需求是，推荐使⽤CMS或G1收集器。启⽤该选项后， -XX:+UseParNewGC⾃动启⽤。 |
| -XX:+UseG1GC                   | 启⽤G1收集器。 G1是服务器类型的收集器， ⽤于多核、⼤内存的机器。它在保持⾼吞吐量的情况下，⾼概率满⾜GC暂停时间的⽬标。 |

在bin/catalina.sh的脚本中 , 追加如下配置 :

```java
JAVA_OPTS="-XX:+UseConcMarkSweepGC"
```

**3.3 Tomcat 配置调优**

Tomcat⾃身相关的调优

- 调整tomcat线程池

![image-20200907165336483](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200907165336483.png)

- 调整tomcat的连接器
  调整tomcat/conf/server.xml 中关于链接器的配置可以提升应⽤服务器的性能。

| 参数           | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| maxConnections | 最⼤连接数，当到达该值后，服务器接收但不会处理更多的请求， 额外的请求将会阻塞直到连接数低于maxConnections 。可通过ulimit -a 查看服务器限制。对于CPU要求更高(计算密集型)时，建议不要配置过大; 对于CPU要求不是特别高时，建议配置在2000左右(受服务器性能影响)。 当然这个需要服务器硬件的⽀持 |
| maxThreads     | 最大线程数,需要根据服务器的硬件情况，进行⼀个合理的设置      |
| acceptCount    | 最⼤排队等待数,当服务器接收的请求数量到达maxConnections ，此时Tomcat会将后面的请求，存放在任务队列中进行排序， acceptCount指的就是任务队列中排队等待的请求数 。 ⼀台Tomcat的最⼤的请求处理数量，是maxConnections+acceptCount |

- 禁⽤ A JP 连接器

![image-20200907165513547](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200907165513547.png)

- 调整 IO 模式
  Tomcat8之前的版本默认使⽤BIO（阻塞式IO），对于每⼀个请求都要创建⼀个线程来处理，不适
  合⾼并发；Tomcat8以后的版本默认使用NIO模式（非阻塞式IO）

![image-20200907165530591](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200907165530591.png)

当Tomcat并发性能有较高要求或者出现瓶颈时，我们可以尝试使用APR模式，APR（Apache Portable
Runtime）是从操作系统级别解决异步IO问题，使⽤时需要在操作系统上安装APR和Native（因为APR
原理是使⽤使⽤JNI技术调用操作系统底层的IO接⼝）

- 动静分离

  可以使⽤Nginx+Tomcat相结合的部署⽅案，Nginx负责静态资源访问，Tomcat负责Jsp等动态资
  源访问处理（因为Tomcat不擅长处理静态资源）。

**3.4 io模型比较**

总结上述几种IO模型，将其功能和特性进行对比：https://blog.csdn.net/szxiaohe/article/details/81542605

![img](https://img-blog.csdn.net/20180809192713216?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N6eGlhb2hl/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

虽然AIO有很多优势，但并不意味着所有Java网络编程都必须选择AIO，具体选择什么IO模型或者框架，还是要基于业务的实际应用场景和性能诉求，如果客户端并发连接数不多，服务器的负载也不重，则完全没必要选择AIO做服务器，毕竟AIO编程难度相对BIO来说更大

#### Tomcat Servlet容器处理流程

   当⽤户请求某个URL资源时
   1）HTTP服务器会把请求信息使⽤ServletRequest对象封装起来
   2）进⼀步去调⽤Servlet容器中某个具体的Servlet
   3）在 2）中，Servlet容器拿到请求后，根据URL和Servlet的映射关系，找到相应的Servlet
   4）如果Servlet还没有被加载，就⽤反射机制创建这个Servlet，并调⽤Servlet的init⽅法来完成初始化
   5）接着调⽤这个具体Servlet的service⽅法来处理请求，请求处理结果使⽤ServletResponse对象封装
   6）把ServletResponse对象返回给HTTP服务器，HTTP服务器会把响应发送给客户端

#### Tomcat6升到8分别都有什么变化？





#### nginx进程之间如何通信的



#### Spring和nginx进程模型有什么区别



#### nginx是如何控制并发数量的

分为**限制并发连接数**和**限制并发请求数**：

- #### 限制并发连接数

  示例配置：

  ```nginx
  Copyhttp {
  	limit_conn_zone $binary_remote_addr zone=addr:10m;
      #limit_conn_zone $server_name zone=perserver:10m;
      
      server {
          limit_conn addr 1;
          limit_conn_log_level warn;
          limit_conn_status 503;
      }
  }
  ```

  **limit_conn_zone** key zone=name:size; 定义并发连接的配置

  - 可定义的模块为http模块。
  - key关键字是根据什么变量来限制连接数，示例中有binary_remote_addr、$server_name，根据实际业务需求。
  - zone定义配置名称和最大共享内存，若占用的内存超过最大共享内存，则服务器返回错误

  示例中的`$binary_remote_addr`是二进制的用户地址，用二进制来节省字节数，减少占用共享内存的大小。

  **limit_conn** zone number; 并发连接限制

  - 可定义模块为http、server、location模块
  - zone为指定使用哪个limit_conn_zone配置
  - number为限制连接数，示例配置中限制为 1 个连接。

  **limit_conn_log_level** info | notice | warn | error ; 限制发生时的日志级别

  - 可定义模块为http、server、location模块

  **limit_conn_status** code; 限制发生时的返回错误码，默认503

  - 可定义模块为http、server、location模块

- #### 限制并发请求数

  **limit_req_zone** key zone=name:size rate=rate； 定义限制并发请求的配置。

  - 若占用的内存超过最大共享内存，则服务器返回错误响应
  - rate定义的是请求速率，如10r/s 每秒传递10个请求，10r/m 每分钟传递10个请求

  **limit_req** zone=name [burst=number] [nodelay | delay=number];

  - zone 定义使用哪个 limit_req_zone配置
  - burst=number 设置桶可存放的请求数，就是请求的缓冲区大小
  - nodelay burst桶的请求不再缓冲，直接传递，rate请求速率失效。
  - delay=number 第一次接收请求时，可提前传递number个请求。
  - 可定义模块为http、server、location模块

  **limit_req_log_level** info | notice | warn | error; 限制发生时的日志级别

  - 可定义模块为http、server、location模块

  **limit_req_status** *code*；限制发生时的错误码

  - 可定义模块为http、server、location模块

  **示例配置1**

  ```nginx
  Copyhttp {
      limit_req_zone $binary_remote_addr zone=one:10m rate=1r/s;
      limit_req zone=one burst=5;
  }
  ```

  请求速率为每秒传递1个请求。burst桶大小可存放5个请求。超出限制的请求会返回错误。

  **示例配置2**

  ```nginx
  Copyhttp {
      limit_req_zone $binary_remote_addr zone=one:10m rate=1r/s;
      limit_req zone=one burst=5 nodelay;
  }
  ```

  示例配置2是在示例配置1当中添加了`nodelay`选项。那么rate请求速率则不管用了。会直接传递burst桶中的所有请求。超出限制的请求会返回错误。

  **示例配置3**

  ```nginx
  Copyhttp {
      limit_req_zone $binary_remote_addr zone=one:10m rate=1r/s;
      limit_req zone=one burst=5 delay=3;
  }
  ```

  示例配置3是在示例配置1当中添加了`delay=3`选项。表示前3个请求会立即传递，然后其他请求会按请求速率传递。超出限制的请求会返回错误。

- 若占用的内存超过最大共享内存，则服务器返回错误响应

- rate定义的是请求速率，如10r/s 每秒传递10个请求，10r/m 每分钟传递10个请求

**limit_req** zone=name [burst=number] [nodelay | delay=number];

- zone 定义使用哪个 limit_req_zone配置
- burst=number 设置桶可存放的请求数，就是请求的缓冲区大小
- nodelay burst桶的请求不再缓冲，直接传递，rate请求速率失效。
- delay=number 第一次接收请求时，可提前传递number个请求。
- 可定义模块为http、server、location模块

**limit_req_log_level** info | notice | warn | error; 限制发生时的日志级别

- 可定义模块为http、server、location模块

**limit_req_status** *code*；限制发生时的错误码

- 可定义模块为http、server、location模块

**示例配置1**

```nginx
Copyhttp {
    limit_req_zone $binary_remote_addr zone=one:10m rate=1r/s;
    limit_req zone=one burst=5;
}

```

请求速率为每秒传递1个请求。burst桶大小可存放5个请求。超出限制的请求会返回错误。

**示例配置2**

```nginx
Copyhttp {
    limit_req_zone $binary_remote_addr zone=one:10m rate=1r/s;
    limit_req zone=one burst=5 nodelay;
}

```

示例配置2是在示例配置1当中添加了`nodelay`选项。那么rate请求速率则不管用了。会直接传递burst桶中的所有请求。超出限制的请求会返回错误。

**示例配置3**

```nginx
Copyhttp {
    limit_req_zone $binary_remote_addr zone=one:10m rate=1r/s;
    limit_req zone=one burst=5 delay=3;
}

```

示例配置3是在示例配置1当中添加了`delay=3`选项。表示前3个请求会立即传递，然后其他请求会按请求速率传递。超出限制的请求会返回错误。