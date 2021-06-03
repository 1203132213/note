- 属性
  - collection：添加要遍历的集合
  - item：迭代变量，循环内使用#{迭代变量名}来获取内容
  - open：循环后左侧添加的内容
  - close：循环后右侧添加的内容
  - separator：添加每次遍历尾部追加的分割符

```xml
<select id="selIn" parameterType="list" resultType="log">
	select * from log where id in
	<foreach collection="list" item="a" open="(" close=")" separator=",">
		#{a}
	</foreach>
</select>
```