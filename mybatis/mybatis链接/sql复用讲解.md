# sql (复用)

- 某些SQL片段如果需要复用，可以使用<sql>这个标签

```xml
<sql id="mysql">
	id,accin,accout,money
</sql>
123
```

- 在<select>或<upfate>或<insert>中使用<include>标签进行复用引用

```java
<select id="">
	select <include refid="mysql"></include> from log
</select>
```

