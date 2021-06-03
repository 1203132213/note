导入依赖：

```java
  <dependency>  
  	<groupId>org.mybatis.caches</groupId>   
  	<artifactId>mybatis-redis</artifactId>    
  	<version>1.0.0-beta2</version> 
  </dependency>
```

配置文件：

```java
<?xml version="1.0" encoding="UTF-8"?> <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"  "http://mybatis.org/dtd/mybatis-3-mapper.dtd"> 

<mapper namespace="com.lagou.mapper.IUserMapper">

<cache type="org.mybatis.caches.redis.RedisCache" />//在mapper文件添加该标签

<select id="findAll" resultType="com.lagou.pojo.User" useCache="true"> 
select * from user 
</select>

```

redis.properties：注意命名是不能改的

```java
redis.host=localhost 
redis.port=6379 
redis.connectionTimeout=5000 
redis.password= redis.database=0

```

