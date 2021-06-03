#### Dao接口里的方法，参数不同时，方法能重载吗？

Dao接口里的方法，是不能重载的，因为是全限名+方法名的保存和寻找策略，重载方法时将导致矛盾。对于Mapper接口，Mybatis禁止方法重载（overLoad）。

#### 从以下几个方面谈谈对mybatis的一级缓存 

- ##### mybaits中如何维护一级缓存 

  答： BaseExecutor成员变量之一的PerpetualCache，是对Cache接口最基本的实现， 其实现非常简单，内部持有HashMap，对一级缓存的操作实则是对HashMap的操作。 

- ##### 一级缓存的生命周期 

  答：

  MyBatis一级缓存的生命周期和SqlSession一致;

  MyBatis的一级缓存最大范围是SqlSession内部，有多个SqlSession或者分布式的环境下，数据库写操作会引起脏数据;

   MyBatis一级缓存内部设计简单，只是一个没有容量限定的HashMap，在缓存的功能性上有所欠缺

- ##### mybatis 一级缓存何时失效 

  答：
  a. MyBatis在开启一个数据库会话时，会创建一个新的SqlSession对象，SqlSession对象中会有一个新的Executor对象，Executor对象中持有一个新的PerpetualCache对象；当会话结束时，SqlSession对象及其内部的Executor对象还有PerpetualCache对象也一并释放掉。
  b. 如果SqlSession调用了close()方法，会释放掉一级缓存PerpetualCache对象，一级缓存将不可用；
  c. 如果SqlSession调用了clearCache()，会清空PerpetualCache对象中的数据，但是该对象仍可使用；
  d.SqlSession中执行了任何一个update操作update()、delete()、insert() ，都会清空PerpetualCache对象的数据 

- ##### 一级缓存的工作流程？

  答：
  a.对于某个查询，根据statementId,params,rowBounds来构建一个key值，根据这个key值去缓存Cache中取出对应的key值存储的缓存结果；
  b. 判断从Cache中根据特定的key值取的数据数据是否为空，即是否命中；
  c. 如果命中，则直接将缓存结果返回；
  d. 如果没命中：去数据库中查询数据，得到查询结果；将key和查询到的结果分别作为key,value对存储到Cache中；将查询结果返回.

#### 调用dao方法底层流程（从启动到执行sql）



#### 插件原理

#### mybatis分页插件的原理



#### 什么是SOAP

简单对象访问协议是一种数据交换协议规范，是一种轻量的、简单的、基于XML的协议的规范。SOAP协议和HTTP协议一样，都是底层的通信协议，只是请求包的格式不同而已，SOAP包是XML格式的。SOAP的消息是基于xml并封装成了符合http协议，因此，它符合任何路由器、防火墙或代理服务器的要求。SOAP可以使用任何语言来完成，只要发送正确的SOAP请求即可，基于SOAP的服务可以在任何平台无需修改即可正常使用。



#### #{} 和${}的区别？

- #{}生成的sql，将#{user_ids}替换成?占位符，然后在执行时替换成实际传入的user_id值，并在两边加上单引号，以字符串方式处理

```sql
select user_id,user_name from t_user where user_id = #{user_id}
```

执行出的日志

```sql
10:27:20.247 [main] DEBUG william.mybatis.quickstart.mapper.UserMapper.selectById - ==>  Preparing: select id, user_name from t_user where id = ? 
10:27:20.285 [main] DEBUG william.mybatis.quickstart.mapper.UserMapper.selectById - ==> Parameters: 1(Long)
```

- ${}生成的sql，即将传入的值直接拼接到SQL语句中，且不会自动加单引号

```java
select user_id,user_name from t_user where user_id = ${user_id}
```

执行出的日志

```sql
10:27:20.247 [main] DEBUG william.mybatis.quickstart.mapper.UserMapper.selectById - ==>  Preparing: select id, user_name from t_user where id = 1
```

可以看到，参数是直接替换的，且没有单引号处理，这样就有SQL注入的风险。

但是在一些特殊情况下，使用${}是更适合的方式，如表名、orderby等。见下面这个例子：

```sql
select user_id,user_name from ${table_name} where user_id = ${user_id}
这里如果想要动态处理表名，就只能使用"${}"，因为如果使用"#{}"，就会在表名字段两边加上单引号，变成下面这样：
```

```sql
select user_id,user_name from 't_user' where user_id = ${user_id}
```

这样SQL语句就会报错。

#### 占位符为什么可以防止sql注入？

以下为例：

String sql = "select * from administrator where adminname=?";
psm = con.prepareStatement(sql);

String s_name ="zhangsan' or '1'='1";
psm.setString(1, s_name);

如果zhangsan' or '1'='1直接拼接到sql会查询所有数据，而有占位符会转义：

```sqlite
转义后的sql为'zhangsan\' or \'1\'=\'1';这个时候是查不出来的。
```

#### MySQL预编译：

##### MySQL执行预编译分为如三步：

第一步：执行预编译语句，例如：prepare myperson from 'select * from t_person where name=?'
第二步：设置变量，例如：set @name='Jim'
第三步：执行语句，例如：execute myperson using @name

如果需要再次执行myperson，那么就不再需要第一步，即不需要再编译语句了：

设置变量，例如：set @name='Tom'
执行语句，例如：execute myperson using @name

##### 预编译的好处

##### 1.1、预编译能避免SQL注入

预编译功能可以避免SQL注入，因为SQL已经编译完成，其结构已经固定，用户的输入只能当做参数传入进去，不能再破坏SQL的结果，无法造成曲解SQL原本意思的破坏。

##### 1.2、预编译能提高SQL执行效率

预编译功能除了避免SQL注入，还能提高SQL执行效率。当客户发送一条SQL语句给服务器后，服务器首先需要校验SQL语句的语法格式是否正确，然后把SQL语句编译成可执行的函数，最后才是执行SQL语句。其中校验语法，和编译所花的时间可能比执行SQL语句花的时间还要多。

如果我们需要执行多次insert语句，但只是每次插入的值不同，MySQL服务器也是需要每次都去校验SQL语句的语法格式以及编译，这就浪费了太多的时间。如果使用预编译功能，那么只对SQL语句进行一次语法校验和编译，所以效率要高。

##### JDBC的预编译用法

相信每个人都应该了解JDBC中的PreparedStatement接口，它是用来实现SQL预编译的功能。其用法是这样的：

```java
Class.forName("com.mysql.jdbc.Driver");
String url = "jdbc:mysql://127.0.0.1:3306/mybatis";
String user = "root";
String password = "123456";
//建立数据库连接
Connection conn = DriverManager.getConnection(url, user, password);

String sql = "insert into user(username, sex, address) values(?,?,?)";
PreparedStatement ps = conn.preparedStatement(sql);
ps.setString(1, "张三");  //为第一个问号赋值  
ps.setInt(2, 2);    //为第二个问号赋值
ps.setString(3, "北京");    //为第三个问号赋值
ps.executeUpdate();
conn.close();
```