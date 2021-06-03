## 1.package name="映射文件所在包名"

   **必须保证接口名（例如IUserDao）和xml名（IUserDao.xml）相同，还必须在同一个包中**

```java
<package name="com.mybatis.dao"/>
```

##   2.mapper resource=""

   **不用保证同接口同包同名**

```java
<mapper resource="com/mybatis/mappers/EmployeeMapper.xml"/> 
```

##   3.mapper class="接口路径"

  **保证接口名（例如IUserDao）和xml名（IUserDao.xml）相同，还必须在同一个包中**

```java
<mapper class="com.mybatis.dao.EmployeeMapper"/>
```

##   4.mapper url="文件路径名" 不推荐

  **引用网路路径或者磁盘路径下的sql映射文件 file:///var/mappers/AuthorMapper.xml**

```java
<mapper url="file:E:/Study/myeclipse/_03_Test/src/cn/sdut/pojo/PersonMapper.xml"/>
```

