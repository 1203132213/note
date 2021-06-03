[TOC]



# springboot自动配置（根据启动流程分析)

```java
@SpringBootApplication//能够扫描Spring组件并自动配置Spring boot
public class Springboot01DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(Springboot01DemoApplication.class, args);
    }
}
```

## @SpringBootApplication内部结构：

```java
@Target(ElementType.TYPE)    //注解的适用范围,Type表示注解可以描述在类、接口、注解或枚举中
@Retention(RetentionPolicy.RUNTIME) ///表示注解的生命周期，Runtime运行时
@Documented ////表示注解可以记录在javadoc中
@Inherited   //表示可以被子类继承该注解 （以上这些了解即可）

@SpringBootConfiguration // 标明该类为配置类
@EnableAutoConfiguration  // 启动自动配置功能
@ComponentScan(excludeFilters = { // 包扫描器 <context:component-scan base-package="com.xxx.xxx"/>
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
	@AliasFor(annotation = EnableAutoConfiguration.class)
	Class<?>[] exclude() default {};

	@AliasFor(annotation = EnableAutoConfiguration.class)
	String[] excludeName() default {};

	@AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
	String[] scanBasePackages() default {};

	@AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
	Class<?>[] scanBasePackageClasses() default {};

}
```

### 1. @SpringBootApplication->@SpringBootConfiguration讲解：

该注解只是对@Configuration的封装，等同于@Configuration。

```java
@Configuration //配置类
public @interface SpringBootConfiguration {
}
```

### 2. @SpringBootApplication->@EnableAutoConfiguration讲解：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

@AutoConfigurationPackage		//2.1讲解
@Import(AutoConfigurationImportSelector.class)  //2.2讲解
public @interface EnableAutoConfiguration {

	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";
  
	Class<?>[] exclude() default {};

	String[] excludeName() default {};

}
```

#### 2.1 @SpringBootApplication->@EnableAutoConfiguration->@AutoConfigurationPackage讲解（含实现细节总结，2.1.1是细节）：

**作用：**会把@springbootApplication注解标注的类所在包名拿到，并且对该包及其子包进行扫描，将组件添加到容器中，这就是为什么把Springboot01DemoApplication放在外层的原因，因为它要扫描dao、service等包。

**实现细节总结：**将@springbootApplication注解标注的类所在包名拿到，并且对该包及其子包进行扫描，将组件添加到容器中。

```java

//spring框架的底层注解，它的作用就是给容器中导入某个组件类，
//例如@Import(AutoConfigurationPackages.Registrar.class)，它就是将Registrar这个组件类导入到容器中
@Import(AutoConfigurationPackages.Registrar.class)  //  默认将主配置类(@SpringBootApplication)所在的包及其子包里面的所有组件扫描到Spring容器中
public @interface AutoConfigurationPackage {
}
```

##### 2.1.1 @SpringBootApplication->@EnableAutoConfiguration->@AutoConfigurationPackage->@Import(AutoConfigurationPackages.Registrar.class)讲解：<span style="color:red">registerBeanDefinition()？？？</span>将什么组件注册？？？

```java
public abstract class AutoConfigurationPackages {
  
	static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

		// 获取的是项目主程序启动类所在的目录
		//metadata:注解标注的元数据信息
		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			//默认将会扫描@SpringBootApplication标注的主配置类所在的包及其子包下所有组件
			register(registry, new PackageImport(metadata).getPackageName());
		}
	}
    
   	   //register(registry, new PackageImport(metadata).getPackageName())讲解
       public static void register(BeanDefinitionRegistry registry, String... packageNames) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(AutoConfigurationPackages.BasePackages.class);
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, packageNames);
            beanDefinition.setRole(2);
            registry.registerBeanDefinition(BEAN, beanDefinition);
    }   
```

#### 2.2 @SpringBootApplication->@EnableAutoConfiguration->@Import(AutoConfigurationImportSelector.class)讲解（含实现细节总结，2.2.1、2.2.2是细节）：

**作用：**可以帮助springboot应用将所有符合条件的@Configuration配置都加载到当前SpringBoot创建并使用的IoC容器(ApplicationContext)中

**实现细节总结：**见下面

```java
public class AutoConfigurationImportSelector
			implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware,
			BeanFactoryAware, EnvironmentAware, Ordered {
        
		// selectImports（）：这个方法告诉springboot都需要导入那些组件
		@Override
		public String[] selectImports(AnnotationMetadata annotationMetadata) {
			//判断 enableautoconfiguration注解有没有开启，默认开启（是否进行自动装配），没有开启就不运行下面方法
			if (!isEnabled(annotationMetadata)) {
				return NO_IMPORTS;//空数组
			}
			//加载配置文件META-INF/spring-autoconfigure-metadata.properties，从中获取所有支持自动配置类的条件
			//作用：SpringBoot使用一个Annotation的处理器来收集一些自动装配的条件，那么这些条件可以在META-INF/spring-autoconfigure-metadata.properties进行配置。
			// SpringBoot会将收集好的@Configuration进行一次过滤进而剔除不满足条件的配置类
			// 自动配置的类全名.条件=值
			AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
      
			AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(autoConfigurationMetadata, annotationMetadata);
			return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
		}
        
	}
```

##### 2.2.1 @SpringBootApplication->@EnableAutoConfiguration->@Import(AutoConfigurationImportSelector.class)->AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader)讲解：

```java
//该方法properties文件如下图所示，主要是将properties文件的内容遍历封装成AutoConfigurationMetadata对象返回
public final class AutoConfigurationMetadataLoader {
	protected static final String PATH = "META-INF/" + "spring-autoconfigure-metadata.properties"
        
    static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader, String path) {
		try {
			//1.读取spring-boot-autoconfigure.jar包中spring-autoconfigure-metadata.properties的信息生成urls枚举对象
            // 获得 PATH 对应的 URL 们
			Enumeration<URL> urls = (classLoader != null) 
           classLoader.getResources(path) : ClassLoader.getSystemResources(path);
            // 遍历 URL 数组，读取到 properties 中
            Properties properties = new Properties();

			//2.解析urls枚举对象中的信息封装成properties对象并加载
			while (urls.hasMoreElements()) {
				properties.putAll(PropertiesLoaderUtils.loadProperties(new UrlResource(urls.nextElement())));
			}
			// 将 properties 转换成 PropertiesAutoConfigurationMetadata 对象
			// 根据封装好的properties对象生成AutoConfigurationMetadata对象返回
			return loadMetadata(properties);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load @ConditionalOnClass location [" + path + "]", ex);
		}
	}
}
```

![image-20200824195009779](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824195009779.png)

##### 2.2.2 @SpringBootApplication->@EnableAutoConfiguration->@Import(AutoConfigurationImportSelector.class)->getAutoConfigurationEntry(autoConfigurationMetadata, annotationMetadata)讲解：

```java
protected AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata) {
	    // 1. 判断是否开启注解。如未开启，返回空串
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
		// 2. 获得注解的属性
		AnnotationAttributes attributes = getAttributes(annotationMetadata);

		// 3. getCandidateConfigurations()用来获取默认支持的自动配置类名列表
		// spring Boot在启动的时候，使用内部工具类SpringFactoriesLoader，查找classpath上所有jar包中的META-INF/spring.factories，
		// 找出其中key为org.springframework.boot.autoconfigure.EnableAutoConfiguration的属性定义的工厂类名称，
		// 将这些值作为自动配置类导入到容器中，自动配置类就生效了
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);


		// 3.1 //去除重复的配置类，若我们自己写的starter 可能存在重复的
		configurations = removeDuplicates(configurations);
		// 4. 如果项目中某些自动配置类，我们不希望其自动配置，我们可以通过EnableAutoConfiguration的exclude或excludeName属性进行配置，
		// 或者也可以在配置文件里通过配置项“spring.autoconfigure.exclude”进行配置。
		//找到不希望自动配置的配置类（根据EnableAutoConfiguration注解的一个exclusions属性）
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		// 4.1 校验排除类（exclusions指定的类必须是自动配置类，否则抛出异常）
		checkExcludedClasses(configurations, exclusions);
		// 4.2 从 configurations 中，移除所有不希望自动配置的配置类
		configurations.removeAll(exclusions);

		// 5. 对所有候选的自动配置类进行筛选，根据项目pom.xml文件中加入的依赖文件筛选出最终符合当前项目运行环境对应的自动配置类

		//@ConditionalOnClass ： 某个class位于类路径上，才会实例化这个Bean。
		//@ConditionalOnMissingClass ： classpath中不存在该类时起效
		//@ConditionalOnBean ： DI容器中存在该类型Bean时起效
		//@ConditionalOnMissingBean ： DI容器中不存在该类型Bean时起效
		//@ConditionalOnSingleCandidate ： DI容器中该类型Bean只有一个或@Primary的只有一个时起效
		//@ConditionalOnExpression ： SpEL表达式结果为true时
		//@ConditionalOnProperty ： 参数设置或者值一致时起效
		//@ConditionalOnResource ： 指定的文件存在时起效
		//@ConditionalOnJndi ： 指定的JNDI存在时起效
		//@ConditionalOnJava ： 指定的Java版本存在时起效
		//@ConditionalOnWebApplication ： Web应用环境下起效
		//@ConditionalOnNotWebApplication ： 非Web应用环境下起效
    
		//总结一下判断是否要加载某个类的两种方式：
		//根据spring-autoconfigure-metadata.properties进行判断。
		//要判断@Conditional是否满足
		// 如@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })表示需要在类路径中存在SqlSessionFactory.class、SqlSessionFactoryBean.class这两个类才能完成自动注册。
		configurations = filter(configurations, autoConfigurationMetadata);


		// 6. 将自动配置导入事件通知监听器
		//当AutoConfigurationImportSelector过滤完成后会自动加载类路径下Jar包中META-INF/spring.factories文件中 AutoConfigurationImportListener的实现类，
		// 并触发fireAutoConfigurationImportEvents事件。
		fireAutoConfigurationImportEvents(configurations, exclusions);
		// 7. 创建 AutoConfigurationEntry 对象
		return new AutoConfigurationEntry(configurations, exclusions);
	}

该方法有两个重要部分：
1.getCandidateConfigurations(annotationMetadata, attributes)：
    getCandidateConfigurations()用来获取默认支持的自动配置类名列表（spring Boot在启动的时候，使用内部工具类SpringFactoriesLoader，查找classpath上所有jar包中的META-INF/spring.factories，找出其中key为org.springframework.boot.autoconfigure.EnableAutoConfiguration的属性定义的工厂类名称，将这些值作为自动配置类导入到容器中，自动配置类就生效了）spring.factories见下图。
    
2.filter(configurations, autoConfigurationMetadata)：
    条件类型：
   	    @ConditionalOnClass ： 某个class位于类路径上，才会实例化这个Bean。
		@ConditionalOnMissingClass ： classpath中不存在该类时起效
		@ConditionalOnBean ： DI容器中存在该类型Bean时起效
		@ConditionalOnMissingBean ： DI容器中不存在该类型Bean时起效
		@ConditionalOnSingleCandidate ： DI容器中该类型Bean只有一个或@Primary的只有一个时起效
		@ConditionalOnExpression ： SpEL表达式结果为true时
		@ConditionalOnProperty ： 参数设置或者值一致时起效
		@ConditionalOnResource ： 指定的文件存在时起效
		@ConditionalOnJndi ： 指定的JNDI存在时起效
		@ConditionalOnJava ： 指定的Java版本存在时起效
		@ConditionalOnWebApplication ： Web应用环境下起效
		@ConditionalOnNotWebApplication ： 非Web应用环境下起效
    对自动配置类名列表进行条件判断，比如：1.1、1.2、1.3图，
    要判断org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration类需满足
    @ConditionalOnClass(DSLContext.class)才能完成自动注册。//里面也可是数组
```

如下图可以看到所有需要配置的类全路径都在文件中，每行一个配置，**多个类名逗号分隔,而\表示忽略换行**

![image-20200824195953923](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824195953923.png)



![image-20200824201306796](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824201306796.png)

1.1

![image-20200824201229712](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824201229712.png)

1.2

![image-20200824201855033](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824201855033.png)

1.3



### 3. @SpringBootApplication->@ComponentScan讲解：

包扫描器， 相当于ssm框架的<context:component-scan base-package="com.xxx.xxx"/>。扫描的包的路径由@EnableAutoConfiguration-@AutoConfigurationPackage 得到，该注解进行扫描。

# 自定义starter

### 自定义starter介绍：

实际相当于导入autoconfigure依赖，该项目就会<span style="color:red">自动加载引入的功能jar包的配置，但是后面会有相应的约束来决定是否需要配置。</span>

![image-20200826084723866](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200826084723866.png)

### 自定义starter搭建：

导入依赖：

```xml
<dependencies>
 	<dependency>
		 <groupId>org.springframework.boot</groupId>
 		<artifactId>spring-boot-autoconfigure</artifactId>
 		<version>2.2.2.RELEASE</version>
 	</dependency>
</dependencies>
```

编写javaBean：

```java
@EnableConfigurationProperties(SimpleBean.class)
@ConfigurationProperties(prefix = "simplebean")
public class SimpleBean {
    private int id;
    private String name;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return "SimpleBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
```

编写配置类MyAutoConfiguration：

```java
@Configuration
@ConditionalOnClass
public class MyAutoConfiguration {
    static {
        System.out.println("MyAutoConfiguration init....");
    }
    @Bean
    public SimpleBean simpleBean() {
        return new SimpleBean();
    }

}
```

resources下创建/META-INF/spring.factories：

![image-20201220184417719](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201220184417719.png)

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.lagou.config.MyAutoConfiguration
```



### 自定义starter使用：

导入自定义starter的依赖：

```xml
<dependency>
 	<groupId>com.lagou</groupId>
 	<artifactId>zdy-spring-boot-starter</artifactId>
 	<version>1.0-SNAPSHOT</version>
</dependency>
```

全局配置文件配置属性值：

```properties
simplebean.id=1
simplebean.name=自定义starter
```

编写测试方法：

```java
@Autowired
private SimpleBean simpleBean;

@Test
public void zdyStarterTest(){
 System.out.println(simpleBean);
}
```

![image-20201220222908317](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201220222908317.png)

# springboot实例化SpringApplication

```java
@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
```

```java
public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
	   //SpringApplication的启动由两部分组成：
		//1. 实例化SpringApplication对象
		//2. run(args)：调用run方法
		return new SpringApplication(primarySources).run(args);
	}

```

### SpringBootApplication->实例化SpringApplication对象：

```java
	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		...
	//1.项目启动类 SpringbootDemoApplication.class设置为属性存储起来
	this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
        
	//2.设置应用类型是SERVLET应用（Spring 5之前的传统MVC应用）还是REACTIVE应用（Spring 5开始出现的WebFlux交互式应用）
	this.webApplicationType = WebApplicationType.deduceFromClasspath();

	//3.设置初始化器(Initializer),最后会调用这些初始化器
	//所谓的初始化器就是org.springframework.context.ApplicationContextInitializer的实现类,在Spring上下文被刷新之前进行初始化的操作
	setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));

	//4.设置监听器(Listener)
	setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));

	//5.初始化 mainApplicationClass 属性:用于推断并设置项目main()方法启动的主程序启动类
	this.mainApplicationClass = deduceMainApplicationClass();
	}
```

### SpringBootApplication->实例化SpringApplication对象->WebApplicationType.deduceFromClasspath()：

```java
static WebApplicationType deduceFromClasspath() {
        // WebApplicationType.REACTIVE 类型  通过类加载器判断REACTIVE相关的Class是否存在
		if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) // 判断REACTIVE相关的Class是否存在
				&& !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
				&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
			return WebApplicationType.REACTIVE;
		}
		// WebApplicationType.NONE 类型
		for (String className : SERVLET_INDICATOR_CLASSES) {
			if (!ClassUtils.isPresent(className, null)) { // 不存在 Servlet 的类
				return WebApplicationType.NONE;
			}
		}
		// WebApplicationType.SERVLET 类型。可以这样的判断的原因是，引入 Spring MVC 时，是内嵌的 Web 应用，会引入 Servlet 类。
		return WebApplicationType.SERVLET;
	}
```

### SpringBootApplication->实例化SpringApplication对象->	getSpringFactoriesInstances(ApplicationContextInitializer.class))：

```java
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type,
			Class<?>[] parameterTypes, Object... args) {
		ClassLoader classLoader = getClassLoader();

    // 加载指定类型对应的，在 `META-INF/spring.factories` 里的类名的数组
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
		//org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
//org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener
		// 根据names来进行实例化
		List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
		// 对实例进行排序
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}
该方法就是通过ApplicationContextInitializer的类路径找到spring.factories对应的指定类型，如下图
```

![image-20200826111847789](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200826111847789.png)



# SpringBootApplication执行run方法：

```java
public ConfigurableApplicationContext run(String... args) {
		//（1）获取并启动监听器
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting();
		try {
		    // 创建  ApplicationArguments 对象 初始化默认应用参数类
			// args是启动Spring应用的命令行参数，该参数可以在Spring应用中被访问。如：--server.port=9000
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);

			//（2）项目运行环境Environment的预配置
			// 创建并配置当前SpringBoot应用将要使用的Environment
			// 并遍历调用所有的SpringApplicationRunListener的environmentPrepared()方法
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);

			configureIgnoreBeanInfo(environment);
			// 准备Banner打印器 - 就是启动Spring Boot的时候打印在console上的ASCII艺术字体
			Banner printedBanner = printBanner(environment);

			// （3）创建Spring容器
			context = createApplicationContext();
			// 获得异常报告器 SpringBootExceptionReporter 数组
			//这一步的逻辑和实例化初始化器和监听器的一样，
			// 都是通过调用 getSpringFactoriesInstances 方法来获取配置的异常类名称并实例化所有的异常处理类。
			exceptionReporters = getSpringFactoriesInstances(
					SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);

			// （4）Spring容器前置处理
			//这一步主要是在容器刷新之前的准备动作。包含一个非常关键的操作：将启动类注入容器，为后续开启自动化配置奠定基础。
			prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);

			// （5）：刷新容器
			refreshContext(context);

			// （6）：Spring容器后置处理
			//扩展接口，设计模式中的模板方法，默认为空实现。
			// 如果有自定义需求，可以重写该方法。比如打印一些启动结束log，或者一些其它后置处理
			afterRefresh(context, applicationArguments);
			// 停止 StopWatch 统计时长
			stopWatch.stop();
			// 打印 Spring Boot 启动的时长日志。
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			// （7）发出结束执行的事件通知
			listeners.started(context);

			// （8）：执行Runners
			//用于调用项目中自定义的执行器XxxRunner类，使得在项目启动完成后立即执行一些特定程序
			//Runner 运行器用于在服务启动时进行一些业务初始化操作，这些操作只在服务启动后执行一次。
			//Spring Boot提供了ApplicationRunner和CommandLineRunner两种服务接口
			callRunners(context, applicationArguments);
		} catch (Throwable ex) {
		    // 如果发生异常，则进行处理，并抛出 IllegalStateException 异常
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

        //   (9)发布应用上下文就绪事件
		//表示在前面一切初始化启动都没有问题的情况下，使用运行监听器SpringApplicationRunListener持续运行配置好的应用上下文ApplicationContext，
		// 这样整个Spring Boot项目就正式启动完成了。
		try {
			listeners.running(context);
		} catch (Throwable ex) {
            // 如果发生异常，则进行处理，并抛出 IllegalStateException 异常
            handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
	 	//返回容器
		return context;
	}

共九步：
    1.获取并启动监听器:
	  依然使用getSpringFactoriesInstances()方法来获取实例，从META-INF/spring.factories中读取Key为org.springframework.boot.SpringApplicationRunListener的Values
      使用SpringApplicationRunListeners封装对象，调用starting()启动监听器。
        
    2.项目运行环境Environment的预配置:
      加载外部化配置资源到environment，包括命令行参数、servletConfigInitParams、
	servletContextInitParams、systemProperties、sytemEnvironment、random、
	application.yml(.yaml/.xml/.properties)等；初始化日志系统。
           
	3.创建Spring容器:
	  先判断有没有指定的实现类(使用Class<? extends ConfigurableApplicationContext>，如果为null抛错)，获取获得 ApplicationContext 类型，反射创建ApplicationContext 对象
	  再使用getSpringFactoriesInstances()获得异常报告器。
            
    4.Spring容器前置处理:
	  这块会对整个上下文进行一个预处理，比如触发监听器的响应事件、加载资源、设置上下文环境等等
	  包含一个非常关键的操作：将启动类注入容器，为后续开启自动化配置奠定基础。
          
    5.刷新容器:
	  开启（刷新）Spring 容器,通过refresh方法对整个IoC容器的初始化（包括Bean资源的定位、解析、注册等等）
	  向JVM运行时注册一个关机钩子,在JVM关机时关闭这个上下文，除非它当时已经关闭(如果JVM关机时，容器也要关闭)
          
    6.Spring容器后置处理:
	  该方法没有实现，可以根据需要做一些定制化的操作。

    7.发出结束执行的事件通知:
      执行所有SpringApplicationRunListener实现的started方法。
          
    8.执行Runners:
	  调用ApplicationRunner、CommandLineRunner实现类的run方法。

    9.发布应用上下文就绪事件:
	  触发所有 SpringApplicationRunListener 监听器的 running 事件方法 。(表示在前面一切初始化启动都没有问题的情况下，使用运行监听器SpringApplicationRunListener持续运行配置好的应用上下文ApplicationContext，这样整个Spring Boot项目就正式启动完成了)
```



### @EnableAutoConfiguration讲解：

   使用Spring Boot时，我们只需引入对应的Starters，Spring Boot启动时便会自动加载相关依赖，配置相应的初始化参数，以最快捷、简单的形式对第三方软件进行集成，这便是Spring Boot的自动配置功能。我们先从整体上看一下Spring Boot实现该运作机制涉及的核心部分，如下图所示。
![ ](https://img-blog.csdnimg.cn/20200816204755423.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0ODk2NzMw,size_16,color_FFFFFF,t_70#pic_center)
   上图描述了Spring Boot自动配置功能运作过程中涉及的几个核心功能及其相互之间的关系包括@EnableAutoConfiguration、spring.factories、各组件对应的AutoConfiguration类、@Conditional注解以及各种Starters。
   可以用一句话来描述整个过程：Spring Boot通过@EnableAutoConfiguration注解开启自动配置，加载spring.factories中注册的各种AutoConfiguration类，当某个AutoConfiguration类满足其注解@Conditional指定的生效条件（Starters提供的依赖、配置或Spring容器中是否存在某个Bean等）时，实例化该AutoConfiguration类中定义的Bean（组件等），并注入Spring容器，就可以完成依赖框架的自动配置。
**概念：**

- @EnableAutoConfiguration：该注解由组合注解@SpringBootApplication引入，完成自动配置开启，扫描各个jar包下的spring.factories文件，并加载文件中注册的AutoConfiguration类等。
- spring.factories：配置文件，位于jar包的META-INF目录下，按照指定格式注册了自动配置的AutoConfiguration类。spring.factories也可以包含其他类型待注册的类。该配置文件不仅存在于Spring Boot项目中，也可以存在于自定义的自动配置（或Starter）项目中。
- AutoConfiguration类：自动配置类，代表了Spring Boot中一类以XXAutoConfiguration命名的自动配置类。其中定义了三方组件集成Spring所需初始化的Bean和条件。
- @Conditional：条件注解及其衍生注解，在AutoConfiguration类上使用，当满足该条件注解时才会实例化AutoConfiguration类。
- Starters：三方组件的依赖及配置，Spring Boot已经预置的组件。Spring Boot默认的Starters项目往往只包含了一个pom依赖的项目。如果是自定义的starter，该项目还需包含spring.factories文件、AutoConfiguration类和其他配置类。

总结：

```java
@SpringBootApplication
  |-@SpringBootConfiguration
       |- @Configuration //通过javaConfig的方式添加组件到IOC容器中
  |-@EnableAutoConfiguration
       |-@AutoConfigurationPackage //自动配置包，与@ComponentScan扫描到的添加到IOC
       |-@Import(AutoConfigurationImportSelector.class)//到METAINF/spring.factories中定义的bean添加到IOC容器中
  |- @ComponentScan //包扫描
```



# dependencyManagement使用简介

在dependencyManagement元素中声明所依赖的jar包的版本号等信息，那么所有子项目再次引入此依赖jar包时则无需显式的列出版本号。Maven会沿着父子层级向上寻找拥有dependencyManagement 元素的项目，然后使用它指定的版本号。

举例：

在父项目的POM.xml中配置：

```xml
<dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
                <version>1.2.3.RELEASE</version>
            </dependency>
        </dependencies>
</dependencyManagement>
```

此配置即生命了spring-boot的版本信息。

子项目则无需指定版本信息：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

# springboot源码总结：

### springboot源码分两部分：

1. @SpringBootApplication讲解。
2.  SpringApplication.run(Springboot01DemoApplication.class, args)讲解。

```java
@SpringBootApplication//能够扫描Spring组件并自动配置Spring boot
public class Springboot01DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(Springboot01DemoApplication.class, args);//1. 实例化SpringApplication对象；run(args)：调用run方法
    }
}
```

### @SpringBootApplication讲解：

```java
@SpringBootApplication
  |-@SpringBootConfiguration
       |- @Configuration //通过javaConfig的方式添加组件到IOC容器中
  |-@EnableAutoConfiguration
       |-@AutoConfigurationPackage //自动配置包，与@ComponentScan扫描到的添加到IOC
       |-@Import(AutoConfigurationImportSelector.class)//到METAINF/spring.factories中定义的bean添加到IOC容器中
  |- @ComponentScan //包扫描
```

springboot实现自动配置@EnableAutoConfiguration：

1. 通过registry.registerBeanDefinition(BEAN, beanDefinition)将ComponentScan扫描到的bean添加到IOC

2. 过滤METAINF/spring.factories中定义的bean添加到IOC容器中：

   **实现细节：**

   - 加载配置文件META-INF/spring-autoconfigure-metadata.properties，从中获取所有支持自动配置类的条件（将properties文件的内容遍历封装成AutoConfigurationMetadata对象返回）。
   
     ![image-20200824201306796](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824201306796.png)
   
   - springboot在启动的时候，使用内部工具类SpringFactoriesLoader，查找classpath上所有jar包中的META-INF/spring.factories，找出其中key为org.springframework.boot.autoconfigure.EnableAutoConfiguration的属性定义的工厂类名称，将这些值作为自动配置类导入到容器中，自动配置类就生效了
   ```java
     List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
   ```
   
   - filter(configurations, autoConfigurationMetadata)：过滤自动配置类
   
     **例：**如下图，只有JooqAutoConfiguration类上有@ConditionalOnClass({DSLContext.class})时才会自动加载，其他也差不多这个意思。

![image-20200824201306796](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824201306796.png)

![image-20200824201229712](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824201229712.png)

![image-20200824201855033](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200824201855033.png)



### SpringApplication.run(SpringBootDemoApplication.class, args)讲解：

```java
public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
	   //SpringApplication的启动由两部分组成：
		//1. 实例化SpringApplication对象
		//2. run(args)：调用run方法
		return new SpringApplication(primarySources).run(args);
	}
```

