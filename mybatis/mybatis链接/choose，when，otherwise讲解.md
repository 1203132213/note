# choose，when，otherwise(Java中的switch)

- 只要有一个成立，其他都不执行
- 如果title和content都不为null或都不为""
  - 生成的sql中只有where title=?
- 如果title和content都为null或都为""
  - 生成的sql中只有where owner = “owner1”

```xml
<select id="dynamicChooseTest" parameterType="Blog" resultType="Blog">
	select * from t_blog where 1=1
	<choose>
		<when test="title != null">
			and title = #{title}
		</when>
		<when test="content != null">
			and content = #{content}
		</when>
        <!-- <otherwise>可以不写 -->
		<otherwise>
			and owner = "owner1"
		</otherwise>
	</choose>
</select>
```