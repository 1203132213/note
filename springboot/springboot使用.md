[TOC]

## Spring Boot介绍

#### springboot思想：

约定优于配置

#### 什么是spring boot？

​	是 Spring 开源组织下的子项目，是 Spring 组件一站式解决方案，主要是简化了使用 Spring 的难度，简省了繁重的配置，提供了各种启动器，开发者能快速上手。

## spring优缺点：

#### 优点：

1. 容易上手，提高开发效率。
2. 不需要XML配置。
3. 提供了一系列大型项目通用的非业务性功能，例如：内嵌服务器、安全管理、运行数据监控、运行状况检查和外部化配置等。
4. 版本依赖集中管理，避免大量的 Maven 导入和各种版本冲突。
5. 通过一些相对简单的方法，通过依赖注入和面向切面编程，**用简单的java对象实现了EJB功能。**

#### 缺点：

XML配置复杂，依赖管理耗时耗力，一旦选错版本，不兼容会严重阻碍项目开发进度。

## springboot解决问题：

有效解决配置和业务问题思维切换，全身心投入到逻辑业务代码编写中。

#### 起步依赖：

把具备某种功能的坐标打包到一起，并提供默认的功能。

#### 自动配置：

1.会自动将一些配置类（指使用@Configuration的类，@Configuration: 指明当前类是一个配置类来替代之前的Spring配置文件）的bean注册进ioc容器，需要的地方使用@autowired或@rescource使用它；

2.只要引入想用功能的包，相关配置不用管，springboot会自动注入这些配置bean，直接使用即可。

## springboot单元测试搭建：

1.导入依赖

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-test</artifactId>
	<scope>test</scope>
</dependency>
```

2.编写单元测试类和测试方法

```java
@RunWith(SpringRunner.class) //测试启动器，加载Spring Boot测试注解
@SpringBootTest //标记为Spring Boot单元测试类，加载项目ApplicationContext上下文环境
class SpringbootDemoApplicationTests {
    @Autowired
    private DemoController demoController;
 
    @Test
    void contextLoads() {
        String demo = demoController.demo();
        System.out.println(demo);
    }
}
```

## springboot热部署搭建：

#### devtools方式:

##### 1.导入依赖

```java
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
</dependency>
```

##### 2.idea设置

![image-20200821195351187](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200821195351187.png)

![image-20200821195402884](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200821195402884.png)

##### 3.添加properties文件配置

```yml
spring:
  devtools:
    restart:
      enabled: true  #设置开启热部署
      additional-paths: src/main/java #重启目录
      exclude: WEB-INF/**
  freemarker:
    cache: false    #页面不加载缓存，修改即时生效

```

#### 使用springloaded方式：

1. 使用springloaded依赖
2. 配置pom.xml文件，使用mvn spring-boot:run启动

#### 配置JVM启动参数方式：

1. 本地下载springloaded包，配置jvm参数-javaagent:<jar包地址> -noverify



## springboot配置讲解：

![image-20200821201820545](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200821201820545.png)

```xml
     //上图是入口
       <resource>
        <filtering>true</filtering>
        <directory>${basedir}/src/main/resources</directory>
        <includes>
          <include>**/application*.yml</include>
          <include>**/application*.yaml</include>
          <include>**/application*.properties</include>
        </includes>
      </resource>
```

从上图中看出，yml文件是先加载的，所以后面加载的properties文件会将yml覆盖。

#### application.properties配置：

该文件可以是系统属性、环境变量、命令行参数等信息，也可以是自定义配置文件名称和位置。

```properties
server.port=8081
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.config.additional-location=
spring.config.location=
spring.config.name=application
```

demo:将配置文件属性注入到Person实体类对应属性中：

```java
public class Pet {
private String type;
private String name;
//省略get和set方法
}
```

```java
@Component 
@ConfigurationProperties(prefix = "person")//将配置文件中以person开头的属性通过set方法注入到实体类相应的属性中
public class Person {
private int id;
private String name; 
private List hobby; 
private String[] family; 
private Map map;
private Pet pet;
  //省略get和set方法
}
```

以上的properties文件改造：

```yml
person:  
  id: 1  
  name: lucy  
  hobby: [吃饭,睡觉,打豆豆] 
  family: [father,mother]  
  map: {k1: v1,k2: v2}  
  pet: {type: dog,name: 旺财}
```

导入依赖：

```xml
//有了该依赖，写properties文件时会有提示
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-configuration-processor</artifactId>
	<optional>true</optional>
</dependency>
```

#### application.yaml配置：

**缩进式（两种写法）：**

```yml
person:   
  hobby:     
    - play    
    - read    
    - sleep 
```

**或**

```yml
person:  
  hobby:    
    play,    
    read,    
    sleep
```

**数组：**

```yml
person:   
  hobby: [play,read,sleep]
```

**Map：**

```yml
//缩进式
person:   
  map:     
    k1: v1    
    k2: v2
```

```yml
//行内式
person:  
  map: {k1: v1,k2: v2}
```

#### @Value讲解：

配置文件属性值注入（使得上面配置文件生效）

```java
public class Student {

    @Value("3")
    private int id;//相当于id=3,一般不会这样用
    
    @Value("${person.name}")
    private String name;//将properties或ymal文件的值注入进来，不需要set方法。
                        //对于包含Map、对象以及ymal文件格式的行内式写法的配置文件的属性注入都不支持，如果赋值会出现错误。
}
```

#### 自定义配置properties文件：

需加@PropertySource

```properties
test.properties//不加注解是扫描不到的

test.id=110 
test.name=test 
```

```java
@Component
@PropertySource("classpath:test.properties")  //配置自定义配置文件的名称及位置
@ConfigurationProperties(prefix = "test")
public class MyProperties {
    private int id;
    private String name;
}
```

#### 使用@Configuration编写自定义配置类：

```java
@Configuration //标明该类为配置类
public class MyConfig {
    
    @Bean(name = "iservice")   //将返回值对象作为组件添加到spring容器中，标识id默认是方法名
    public MyService myService() {
        return new MyService();
    }
}
```

测试：

```java
 /*
      @Configuration进行测试
     */

    @Autowired
    private ApplicationContext context;

    @Test
    void iocTest() {
        System.out.println(context.containsBean("iservice"));
    }
```

#### properties配置文件随机值设置和参数引用：

随机值设置：

```properties
my.secret=${random.value}         // 配置随机数 
my.number=${random.int}           // 配置随机整数
my.bignumber=${random.long}      // 配置随机long类型数
my.uuid=${random.uuid}            //配置uuid类型数
my.number.less.than.ten=${random.int(10)}    // 配置小于10的随机整数
my.number.in.range=${random.int[1024,65536]} //配置范围在[1024,65536]之间的随机整数
```

参数引用:（省去多处修改的麻烦）

```properties
app.name=MyApp 
app.description=${app.name} is a Spring Boot application //${app.name}拿到值：MyApp
```



## springboot注解：

https://blog.csdn.net/weixin_40753536/article/details/81285046

## springboot解决中文乱码：

```java
@RequestMapping(produces = "application/json; charset=utf-8")
```

或

```java
spring.http.encoding.force-response=true #设置响应为utf-8
```

## springboot解决的问题：

**起步依赖：**把具备某种功能的坐标打包到一起，并提供默认的功能。

**自动配置：**自动将一些配置类的bean注册进ioc容器，使用@autowired或@rescource使用它。

只要引入想用功能的包，相关配置不用管，springboot会自动注入这些配置bean，直接使用即可。

## 扩展

![image-20200826160022031](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200826160022031.png)



## 常用注解：

@PathVariable：接收请求路径中占位符的值

![img](https://img-blog.csdnimg.cn/2018111800431828.png)

