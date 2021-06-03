# trim(截断 添加)

- prefix 在前面添加内容
- suffix 在后面添加内容
- prefixOverrides 去掉前面内容
- suffixOverrides 去掉后面内容

```xml
<update id="upd" parameterType="log">
	update log
	<!-- 去掉了后面的内容 -->
	<!-- 覆盖了标签后的逗号 -->
	<!-- 适用于存在符号和关键字的参数(例如金钱符号$) -->
	<trim prefix="set" suffixOverrides>
		a=a,
	</trim>
	where id=100
</update>
```

