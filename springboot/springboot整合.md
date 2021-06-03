[TOC]

下图模块参见讲义。

![image-20201223222614644](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201223222614644.png)

## 配置国际化页面：

**第一步：**

编写多语言国际化配置文件

![image-20201223230521295](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201223230521295.png)

![image-20201223230531567](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201223230531567.png)

**第二步：**

自定义区域信息解析器

```java
@Configuration
public class MyLocaleResovel implements LocaleResolver {
 	
    //自定义区域解析方式
	 @Override
 	public Locale resolveLocale(HttpServletRequest httpServletRequest) {
	 	// 1.获取页面手动切换的语言参数
		 String l = httpServletRequest.getParameter("l");
 		 // 2.获取请求头自动传递的语言参数Accept-Language
		 String header = httpServletRequest.getHeader("Accept-Language");
 		 Locale locale=null;
		 // 如果手动切换的语言参数l为空则使用请求头自动传递的参数header
		 if(!StringUtils.isEmpty(l)){
 				String[] split = l.split("_");
				 locale=new Locale(split[0],split[1]);
 		 }else {
 			// Accept-Language: en-US,en;q=0.9;zh-CN;q=0.8,zh;q=0.7
 				String[] splits = header.split(",");
				String[] split = splits[0].split("-");
 				locale=new Locale(split[0],split[1]);
 		 }
 		return locale;
 	}
 @Override
 public void setLocale(HttpServletRequest httpServletRequest, @Nullable
	 HttpServletResponse httpServletResponse, @Nullable Locale locale){
 }
 // 将MyLocalResovl类注册为LocaleResolver的Bean组件
 @Bean
 public LocaleResolver localeResolver(){
 	return new MyLocalResovel();
 	}
}
```

**第三步：**

前端使用

```html
<a class="btn btn-sm" th:href="@{/toLoginPage(l='zh_CN')}">中文</a>
<a class="btn btn-sm" th:href="@{/toLoginPage(l='en_US')}">English</a>
```

## springboot缓存管理：

### 使用默认缓存：

springboot继承了spring的缓存管理功能，通过使用@EnableCaching注解开启缓存支持，springboot就可以启动缓存管理自动化配置。

```java
@EnableCaching //开启spring boot基于注解的缓存管理支持
@SpringBootApplication
public class Springboot05CacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(Springboot05CacheApplication.class, args);
	}

}
```

Service接口层添加@Cacheable注解

```java
public class CommentService {
    //unless：如果返回结果为空，不加入缓存
    @Cacheable(cacheNames = "comment",unless = "#result==null")
    public Comment findCommentById(Integer id){
        Optional<Comment> byId = commentRepository.findById(id);
        if(byId.isPresent()){
            Comment comment = byId.get();
            return  comment;
        }
        return  null;
    }
}
```



#### cache生成key策略：

1. springboot<span style="color:red">默认缓存ConcurrentMapCacheManager就是Map集合</span>（如下）：
   	ConcurrentMap<String, Cache> cacheMap;
2. Cache保存的就是上面的comment对象，每一个Cache有多个k-v键值对，key默认在<span style="color:red">只有一个参数的情况下，如上面只有一个id，那么key的值就是id;
   如果没有参数或者多个参数的情况，使用simpleKeyGenerate对象生成key,多个参数会把参数放进去生成key,如下图1.5;</span>
3. 也可以指定key，如图1.1
4. key的SpEL表达式:如图1.2



![image-20200827175814586](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200827175814586.png)

1.1

常用的SPEL表达式

![image-20200827180021174](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200827180021174.png)

1.2

@Cacheable注解提供多个属性，对缓存存储进行相关配置，可以和SPEL表达式组合使用如图1.1 key=“#result.id”//指定返回结果的id为key

![image-20201224232143065](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201224232143065.png)

1.3

#### cache其他注解介绍：

修改建议使用该注解：

![image-20201224000900122](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201224000900122.png)

删除建议使用该注解：

![image-20201224000925873](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201224000925873.png)



### 基于注解的Redis缓存实现：

**1.导入依赖：**

```xml
<dependency>  
    <groupId>org.springframework.boot</groupId>  
    <artifactId>spring-boot-starter-data-redis</artifactId> 
</dependency>
```

![image-20200827190931324](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200827190931324.png)

RedisCacheManager是spring-boot-starter-data-redis里面的，RedisCacheConfiguration是springboot的。

**2.配置properties：**

![image-20200827191234276](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200827191234276.png)

**3.对CommentService类中的方法进行修改使用@Cacheable、@CachePut、@CacheEvict三个注解定制缓存管理，分别进行缓存存储、缓存更新、缓存删除的演示**

```java
@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    // 查询方法
    @Cacheable(cacheNames = "comment",unless = "#result==null")//当结果为空，不进行缓存
    public Comment findCommentById(Integer id){
        Optional<Comment> byId = commentRepository.findById(id);
        if(byId.isPresent()){
            Comment comment = byId.get();
            return  comment;
        }
        return  null;
    }
    //更新方法
    @CachePut(cacheNames = "comment",key = "#result.id")
    public Comment updateComment(Comment comment){
        commentRepository.updateComment(comment.getAuthor(),comment.getId());
        return comment;
    }
    //删除方法
    @CacheEvict(cacheNames = "comment")
    public void deleteComment(Integer id){
        commentRepository.deleteById(id);
    }
}

```

```java
//Comment需要实现序列化Serializable
@Entity
@Table(name = "t_comment")
public class Comment implements Serializable {}
```

```properties
#设置基于注解的Redis缓存数据统一设置有效期为1分钟，单位毫秒（一般不建议这么做）
spring.cache.redis.time-to-live=60000
```

该方法放进缓存中value是HEX格式存储，不方便可视化管理，使用自定义Redis缓存序列化机制即可

![image-20200827201042505](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200827201042505.png)



### 基于API的Redis缓存实现：

```java
@Service
public class ApiCommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    // 使用API方式进行缓存：先去缓存中查找，缓存中有，直接返回，没有，查询数据库
    public Comment findCommentById(Integer id){
        Object o = redisTemplate.opsForValue().get("comment_" + id);
        if(o!=null){
            //查询到了数据，直接返回
            return (Comment) o;
        }else {
            //缓存中没有，从数据库查询
            Optional<Comment> byId = commentRepository.findById(id);
            if(byId.isPresent()){
                Comment comment = byId.get();
                //将查询结果存到缓存中，同时还可以设置有效期为1天
                redisTemplate.opsForValue().set("comment_" + id,comment,1, TimeUnit.DAYS);
                return  comment;
            }
        }

        return  null;
    }

    //更新方法
    public Comment updateComment(Comment comment){
        commentRepository.updateComment(comment.getAuthor(),comment.getId());
        //将更新数据进行缓存更新
        redisTemplate.opsForValue().set("comment_" + comment.getId(),comment);
        return comment;
    }

    //删除方法
    public void deleteComment(Integer id){
        commentRepository.deleteById(id);
        redisTemplate.delete("comment_" + id);
    }
}
RedisTemplate在spring-boot-starter-data-redis里的spring-data-redis.jar包里
```



#### Redis API默认序列化机制：

1. 使用RedisTemplate进行Redis数据缓存操作时，序列化方式为空时默认使用JdkSerializationRedisSerializer方式，所以进行数据缓存的实体类必须实现JDK自带的序列化接口（例如Serializable，这种方式不方便可视化查看和管理，如下图）

   ![image-20201225004230124](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201225004230124.png)

2. 使用RedisTemplate进行Redis数据缓存操作时，如果自定义缓存序列化方式，defaultSerializer那么将使用自定义的序列化方式。（下面会介绍这种方式）

   

RedisSerializer是一个Redis序列化接口，默认有6个实现类，6个实现类代表6种不同的数据序列化方式。开发者可以根据需要选择序列化方式。（例如JSON方式）

![image-20200827203729066](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200827203729066.png)



```java
public class RedisAutoConfiguration {
	@Bean//建立一个名称为方法名redisTemplate，value是返回值template的bean
	@ConditionalOnMissingBean(name = "redisTemplate")//如果有bean的名字是redisTemplate则下面方法不生效，否则默认使用下面的返回值。
	public RedisTemplate<Object, Object> redisTemplate(
			RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}
    ...
}
```

#### Redis API自定义序列化机制：

项目引入Redis依赖后，Spring Boot提供的RedisAutoConﬁguration自动配置会生效，打开RedisAutoConﬁguration源码，里面有关于RedisTemplate的定义方式，使用自定义序列化方式的RedisTemplate进行数据缓存操作，<span style="color:red">需创建一个名为redisTemplate的Bean组件，并在该组件中设置对应的序列化方式即可（会覆盖原有默认序列化方式）。</span>

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 创建JSON格式序列化对象，对缓存数据的key和value进行转换
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);


        // 解决查询缓存转换异常的问题（工具类，不用理解具体方式）
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(om);

        //设置redisTemplate模板API的序列化方式为json
        template.setDefaultSerializer(jackson2JsonRedisSerializer);

        return template;
    }
}
```



#### Redis 注解默认序列化机制：

和api方式类似，只是默认的bean是cacheManager

```java
public class RedisCacheConfiguration {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ResourceLoader resourceLoader) {
        RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(redisConnectionFactory)
                        .cacheDefaults(this.determineConfiguration(resourceLoader.getClassLoader()));
        List<String> cacheNames = this.cacheProperties.getCacheNames();
        if (!cacheNames.isEmpty()) {
            builder.initialCacheNames(new LinkedHashSet(cacheNames));
        }
        return
                (RedisCacheManager) this.customizerInvoker.customize(builder.build());
    }

    private org.springframework.data.redis.cache.RedisCacheConfiguration determineConfiguration(ClassLoader classLoader) {
        if (this.redisCacheConfiguration != null) {
            return this.redisCacheConfiguration;
        } else {
            Redis redisProperties = this.cacheProperties.getRedis();
            org.springframework.data.redis.cache.RedisCacheConfiguration
                    config =
                    org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConf
            ig();
            config = config.serializeValuesWith(SerializationPair.fromSerializer(
                            new JdkSerializationRedisSerializer(classLoader)));
            return config;
        }
    }
}
```



#### Redis 注解自定义序列化机制：

和api方式类似，只是覆盖的bean是cacheManager

```java
//自定义RedisCacheManager（该方式针对基于注解方式Redis缓存实现）
 	@Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 分别创建String和JSON格式序列化对象，对缓存数据key和value进行转换
        RedisSerializer<String> strSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jacksonSeial =
                new Jackson2JsonRedisSerializer(Object.class);

        // 解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSeial.setObjectMapper(om);

        // 定制缓存数据序列化方式及时效
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))//设置缓存时长1天
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(strSerializer))//设置缓存的key值为strSerializer
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jacksonSeial))//设置缓存的value为JSON格式序列化对象
                .disableCachingNullValues();
        RedisCacheManager cacheManager = RedisCacheManager
                .builder(redisConnectionFactory).cacheDefaults(config).build();
        return cacheManager;
    }
```

**自定义Redis注解和api方式的序列化方式总结：**

两个都是有默认的序列化方式，api默认序列化的bean为redisTemplate，注解默认序列化的bean为cacheManager，我们自定义序列化方式只需要和它们创建一样的bean即可覆盖默认序列化方式（默认序列化使用@ConditionalOnMissingBean注解来实现以上的功能）。