# bind（模糊查询）

- 作用：给参数重新赋值
- 场景：模糊查询 | 在原内容前或后添加内容

```xml
<select id="selByLog" parameterType="log" resultType="log">
	select * from log
    <where>
        <!-- 常用语模糊查询(添加%) -->
        <if test="title!=null and title!=''">
            <bind name="title" value="'$'+title+'$'"/>
        	and title like #{title}
        </if>
        <!-- bind:给参数附加字符串 -->
        <if test="money!=null and money!=''">
            <bind name="money" value="'$'+money"/>
			and money = #{money}
        </if>
    </where>
</select>
```