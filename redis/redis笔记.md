[TOC]



## 第一讲：

### Redis使用场景：

- ##### 做session分离：

  传统的session是由tomcat自己进行维护和管理。 

  集群或分布式环境，不同的tomcat管理各自的session。 

  只能在各个tomcat之间，通过网络和io进行session的复制，极大的影响了系统的性能。 

  将登录成功后的Session信息，存放在Redis中，这样多个服务器(Tomcat)可以共享Session信息。

  ![image-20210215095418665](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210215095418665.png)

- ##### 做分布式锁（Redis）

   一般讲锁是多线程的锁，是在一个进程中的多个进程（JVM）在并发时也会产生问题，也要控制时序性可以采用分布式锁。使用Redis实现 sexNX 

- ##### 做乐观锁（Redis） 

  同步锁和数据库中的行锁、表锁都是悲观锁 悲观锁的性能是比较低的，响应性比较差高性能、高响应（秒杀）采用乐观锁 Redis可以实现乐观锁 watch + incr

### 什么是缓存？

缓存原指CPU上的一种**高速存储器**，它先于**内存与CPU交换数据**，速度很快现在泛指存储在计算机上的原始数据的复制集，便于快速访问。 在互联网技术中，缓存是系统**快速响应**的关键技术之一（缓存是以空间换时间的技术，比如访问集群，从机器都有备份主机内容，那么我们直接访问从机时就不用再经过主机）。

### 单机时缓存的使用：

单机时如果性能不够就在各个位置加缓存：

![image-20210215100807721](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210215100807721.png)

### 常见的缓存分类：

- ##### 客户端缓存：

  1. 页面缓存：Cookie、WebStorage（SessionStorage和LocalStorage）、WebSql、indexDB、Application Cache等。

     使用LocalStorage进行本地的数据存储，示例代码：

     ```java
     localStorage.setItem("Name","张飞")
     localStorage.getItem("Name")
     localStorage.removeItem("Name")
     localStorage.clear()
     ```

  2. 浏览器缓存：

     浏览器缓存可分为强制缓存和协商缓存：

     - 强制缓存：直接使用浏览器的缓存数据条件：Cache-Control的max-age没有过期或者Expires的缓存时间没有过期

     ```html
     <meta http-equiv="Cache-Control" content="max-age=7200" />
     <meta http-equiv="Expires" content="Mon, 20 Aug 2010 23:00:00 GMT" />
     ```

     - 协商缓存：服务器资源未修改，使用浏览器的缓存（304）；反之，使用服务器资源（200）

     ```html
     <meta http-equiv="cache-control" content="no-cache">
     ```

  3. APP缓存：原生APP中把数据缓存在内存、文件或本地数据库（SQLite）中。比如图片文件。

- ##### 网络端缓存：

  - **Web代理缓存：**通过代理的方式响应客户端请求，对重复的请求返回缓存中的数据资源，可以缓存原生服务器的静态资源，比如样式、图片等，常见的反向代理服务器比如大名鼎鼎的Nginx。

  # nginx如何实现缓存？？？

  

  - **边缘缓存：**边缘缓存中典型的商业化服务就是CDN了，CDN通过部署在各地的边缘服务器，使用户就近获取所需内容，降低网络拥塞，提高用户访问响应速度 和命中率。 CDN的关键技术主要有内容存储和分发技术。现在一般的公有云服务商都提供CDN服务。

- ##### 服务端缓存：

  服务器端缓存是整个缓存体系的核心。包括数据库级缓存、平台级缓存和应用级缓存。

  - **数据库级缓存：**数据库是用来存储和管理数据的。 MySQL在Server层使用查询缓存机制。将查询后的数据缓存起来。 K-V结构，Key：select语句的hash值，Value：查询结果 InnoDB存储引擎中的buffer-pool用于缓存InnoDB索引及数据块。

  - **平台级缓存 ：**平台级缓存指的是带有缓存特性的应用框架。 比如：GuavaCache 、EhCache、OSCache等。 部署在应用服务器上，也称为服务器本地缓存。
  - **应用级缓存（重点）：**具有缓存功能的中间件：Redis、Memcached、EVCache、Tair等。 采用K-V形式存储。 利用集群支持高可用、高性能、高并发、高扩展。 分布式缓存

### 缓存的优缺点：

- ##### 优点：

  1. 提升用户体验：响应时间变短。
  2. 减轻服务器压力：客户端缓存、网络端缓存减轻应用服务器压力。 服务端缓存减轻数据库服务器的压力。
  3. 提升系统性能：缩短系统的响应时间 、减少网络传输时间和应用延迟时间 、提高系统的吞吐量 、增加系统的并发用户数 、提高了数据库资源的利用率。

- ##### 缺点：

  1. **额外的硬件支出：**缓存是一种软件系统中以空间换时间的技术需要额外的磁盘空间和内存空间来存储数据。
  2. **高并发缓存失效：**在高并发场景下会出现缓存失效（缓存穿透、缓存雪崩、缓存击穿） 造成瞬间数据库访问量增大，甚至崩溃
  3. **缓存与数据库数据同步：**缓存与数据库无法做到数据的实时同步 Redis无法做到主从实时数据同步
  4. **缓存并发竞争：**多个Redis的客户端同时对一个key进行set值得时候由于**执行顺序**引起的并发问题。

### 缓存的几种读写模式：

1. ##### Cache Aside Pattern（常用）

   - ##### 读请求：

     读的时候，先读缓存，缓存没有的话，就读数据库，然后取出数据后放入缓存，同时返回响应。

   - ##### 写请求：

     更新的时候，先更新数据库，然后再删除缓存：

     ![image-20210215111813952](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210215111813952.png)

     ##### 为什么是删除缓存，而不是更新缓存呢？

     **答：**因为缓存的值如果是一个结构：hash、list，更新数据需要遍历，这样性能就很低。

     ##### 为什么是先更新数据库再删除缓存，而不是先删除缓存呢？

     **答：**先删除缓存再更新DB产生脏数据的概率较大，而先更新DB再删除缓存产生脏数据的概率较小，但是会出现一致性的问题，但是不会影响后面的查询（代价较小）。

     高并发脏读三种情况：

     1. 先更新数据库，再更新缓存：

        <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210215113412256.png" alt="image-20210215113412256" style="zoom:80%;" />

     2. 先删除缓存，再更新数据库：

        <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210215113425430.png" alt="image-20210215113425430" style="zoom:80%;" />

     3. 先更新数据库，再删除缓存（推荐）：

        <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210215113435608.png" alt="image-20210215113435608" style="zoom:80%;" />

        2和3可以使用延时双删策略解决数据库一致性问题。

   

2. #### Read/Write Through Pattern：

   应用程序只操作缓存，缓存操作数据库。 

   Read-Through（穿透读模式/直读模式）：应用程序读缓存，缓存没有，由缓存回源到数据库，并写入缓存。 

   Write-Through（穿透写模式/直写模式）：应用程序写缓存，缓存写数据库。 

   该种模式需要提供数据库的handler，开发较为复杂。

3. #### Write Behind Caching Pattern：

   应用程序只更新缓存。 

   缓存通过异步的方式将数据批量或合并后更新到DB中 

   不能实时同步，甚至会丢数据

### 缓存整体设计：

- ##### 总体结构：


![image-20210215214951820](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210215214951820.png)

nginx缓存静态文件（css、js、png...），tomcat缓存和Redis缓存如果其中有一个挂了，另外一个可以顶替。

- ##### 缓存的选择：


简单数据类型采用Memcached。

复杂数据类型hash、set、list、zset，需要存储关系，聚合，计算可采用Redis。

- ##### 缓存集群：


后面讲解

### 缓存的数据结构设计：

有两种方案：

1. 缓存数据与数据库表一致

   数据库表和缓存是一 一对应的 

   缓存的字段会比数据库表少一些 

   缓存的数据是经常访问的

2. 缓存数据与数据库表不一致

   比如查询用户的评论：

   DB结构如下：

   | ID   | UID  | PostTime   | Content    |
   | ---- | ---- | ---------- | ---------- |
   | 1    | 1000 | 1547342000 | xxxxxxxxxx |
   | 2    | 1000 | 1547342000 | xxxxxxxxxx |
   | 3    | 1001 | 1547341030 | xxxxxxxxxx |

   如果以用户为单位存到Redis，需要做以下变形：

   ​	key：UID+PostTime

   ​	value：ID+Content

   还可以设置失效期expire，设置为一天

### 缓存整体设计案例（拉勾网站）：

- 不经常变化的网页使用html，经常变化的网页使用模板。

- 模板技术：每个模块展示数据类型是固定的，但是数据是根据服务端拿到的。

- 固定数据（比如拉勾网站的职位列表），该数据特点，固定数据，一次性读取：

  可以使用Guava Cache，Guava Cache用于存储频繁使用的少量数据，支持高并发访问。也可以使用JDK的CurrentHashMap。

- 经常变动的数据（比如拉勾网站的热门职位），该种数据的特点是不必实时同步，但是一定要有数据，方案如下：

  数据从服务层读取（dubbo），然后放到本地缓存中（Guava），如果出现超时或读取为空，则返回原 来本地缓存的数据。

- 数据回填：

  如果缓存不命中则返回本地缓存，不能直接读取数据库。而是采用异步的形式从数据库刷入到缓存中，再读取缓存。

- 热点数据查询：

  对于热点数据我们采用本地缓存策略，而不采用服务熔断策略，因为首页数据可以不准确，但不能不响应。

## 第二讲：

Redis常用的数据结构：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210510202425592.png" alt="image-20210510202425592" style="zoom:80%;" />

Redis的编码：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210510202312099.png" alt="image-20210510202312099" style="zoom:80%;" />

Redis的底层数据结构：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210510204619121.png" alt="image-20210510204619121" style="zoom:80%;" />

### Redis的value数据类型和应用场景：

key的类型是字符串。

value的数据类型：

- 常用的：string字符串类型、list列表类型、set集合类型、sortedset（zset）有序集合类型、hash类型。
- 不常见的：bitmap位图类型、geo地理位置类型（Redis5.0新增一种：stream类型）。

注意：Redis中**命令**是忽略大小写，（set SET），**key**是不忽略大小写的 （NAME name）



**Redis的Key的设计：**表名：主键值：列名（这样表示明确）

#### Redis常用value数据类型和相关命令：

- ##### String字符串类型：

  Redis的String能表达3种值的类型：字符串、整数、浮点数

  常见操作命令如下表：

  | 命令名称 | 命令格式             | 命令描述                                                     |
  | -------- | -------------------- | ------------------------------------------------------------ |
  | set      | set key value        | 赋值                                                         |
  | get      | get key              | 取值                                                         |
  | getset   | getset key value     | 取值并赋值                                                   |
  | setnx    | setnx key value      | 使用该命令同一个key不能重复赋值（下面会讲解，还有超时情况）<br/>是set if not exists 的缩写，也就是只有不存在的时候才设置 |
  | append   | append key value     | 向尾部追加值                                                 |
  | strlen   | strlen key           | 获取字符串长度                                               |
  | incr     | incr key             | 递增数字                                                     |
  | incrby   | incrby key increment | 增加指定的整数                                               |
  | decr     | decr key             | 递减数字                                                     |
  | decrby   | decrby key decrement | 减少指定的整数                                               |

  以上命令示例：

  **setnx**：

  有两种命令格式：

  - 第一种

    ```yml
    127.0.0.1:6379> setnx name zhangf #如果name不存在赋值
    (integer) 1
    127.0.0.1:6379> setnx name zhaoyun #再次赋值失败
    (integer) 0
    127.0.0.1:6379> get name
    "zhangf"
    ```

  - 第二种

    ```yml
    127.0.0.1:6379> set age 18 NX PX 10000 #如果不存在赋值 有效期10秒
    OK
    127.0.0.1:6379> set age 20 NX #赋值失败
    (nil)
    127.0.0.1:6379> get age #age失效
    (nil)
    127.0.0.1:6379> set age 30 NX PX 10000 #赋值成功
    OK
    127.0.0.1:6379> get age
    "30"
    ```

    ##### String的底层结构（SDS）：
    
    Redis 没有使用C语言的字符数组(C语言的字符数组只有下图中的buf[ ]部分)，而是使用了 SDS(Simple Dynamic String)。用于存储字符串和整型数据。
    
    **SDS源码：**
    
    ```c
    struct sdshdr{
    	//记录buf数组中已使用字节的数量
    	int len;
    	//记录 buf 数组中未使用字节的数量
    	int free;
    	//字符数组，用于保存字符串
    	char buf[];
    }
    ```
    
    ![image-20210216210555743](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216210555743.png)
    
    buf[] 的长度=len+free+1（’\0‘是结束符）
    
    **SDS的优势：**
    
    - 获取字符串长度的复杂度就是O(1),而不是O(n)。
    
      > 相比于C语言的字符串数组，sds增加了free 和 len 字段。
    
    - 杜绝了缓冲区溢出。
    
      >  SDS 由于记录了长度，在可能造成缓冲区溢出时会自动重新分配内存
    
    - 可以存取二进制数据
    
      > C语言的字符数组为什么不可以存取二进制数据？
      >
      > **答：**因为C语言是以 \0（空字符串）为结束符，二进制数据包括空字符串，所以没有办法存取二进制数据
      >
      > SDS是怎么能够存取二进制数据的？
      >
      > **答：**如果是非二进制，SDS以\0（空字符串）为结束符，如果是二进制，那么以字符串长度为结束符，所以可以存取二进制数据。
  
- ##### hash类型（散列表）：

  Redis hash 是一个 string 类型的 field 和 value 的映射表，它提供了字段和字段值的映射。 

  每个 hash 可以存储 2^32 - 1 键值对（40多亿）。

  ![image-20210216164108897](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164108897.png)
  
  常见操作命令如下表：
  
  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164125800.png" alt="image-20210216164125800" style="zoom:80%;" />
  
  应用场景：
  
  对象的存储 ，表数据的映射
  
  举例：
  
  ```yml
  127.0.0.1:6379> hmset user:001 username zhangfei password 111 age 23 sex M
  OK
  127.0.0.1:6379> hgetall user:001
  1) "username"
  2) "zhangfei"
  3) "password"
4) "111"
  5) "age"
6) "23"
  7) "sex"
8) "M"
  127.0.0.1:6379> hget user:001 username
"zhangfei"
  127.0.0.1:6379> hincrby user:001 age 1
(integer) 24
  127.0.0.1:6379> hlen user:001
  (integer) 4
  ```
  
  Jedis客户端操作，安装启动卸载
  
  - ##### hash的底层结构（字典）：
  
    字典其实和hash表的原理是一致的，介绍如下：
  
  1. Hash函数：	
  
     数组下标=hash(key)%数组容量  (hash(key)为hash值)
  
  2. Hash冲突：
  
     出现Hash冲突也是以单链表在相同的下标位置处存储原始key和value
  
  3. 扩容：
  
     字典达到存储上限（阈值 0.75），需要rehash（扩容）
  
     - 扩容流程：
  
       申请新内存 --》重新计算索引  --》将数据迁移到新表
  
     - 迁移过程称为rehash（包括重新计算索引和将数据迁移到新表），rehash的特点：
  
       因为Redis是单线程的，而rehash过程是很缓慢的，所以可以尝试不用一次性全部迁移：
     
       服务器忙，则只对一个节点进行rehash 
     
       服务器闲，可批量rehash(100节点)
  

应用场景：

   1、主数据库的K-V数据存储 

   2、散列表对象（hash） 

 3、哨兵模式中的主从节点管理

- ##### list列表类型

  list的元素个数最多为2^32-1个（40亿）

  常见操作命令如下表：

  | 命令名称   | 命令格式                             | 描述                                                         |
  | ---------- | ------------------------------------ | ------------------------------------------------------------ |
  | lpush      | lpush key v1 v2 v3 ...               | 从左侧插入列表                                               |
  | lpop       | lpop key                             | 从列表左侧取出                                               |
  | rpush      | rpush key v1 v2 v3 ...               | 从右侧插入列表                                               |
  | rpop       | rpop key                             | 从列表右侧取出                                               |
  | lpushx     | lpushx key value                     | 将值插入到列表头部                                           |
  | rpushx     | rpushx key value                     | 将值插入到列表尾部                                           |
  | blpop      | blpop key timeout                    | 从列表左侧取出，当列表为空时阻塞，可以设置最大阻塞时 间，单位为秒 |
  | brpop      | blpop key timeout                    | 从列表右侧取出，当列表为空时阻塞，可以设置最大阻塞时 间，单位为秒 |
  | llen       | llen key                             | 获得列表中元素个数                                           |
  | lindex     | lindex key index                     | 获得列表中下标为index的元素 index从0开始                     |
  | lrange     | lrange key start end                 | 返回列表中指定区间的元素，区间通过start和end指定             |
  | lrem       | lrem key count value                 | 删除列表中与value相等的元素 当count>0时， lrem会从列表左边开始删除;当count<0时， lrem会从列表后边开始删除;当count=0时， lrem删除所有值 为value的元素 |
  | lset       | lset key index value                 | 将列表index位置的元素设置成value的值                         |
  | ltrim      | ltrim key start end                  | 对列表进行修剪，只保留start到end区间                         |
  | rpoplpush  | rpoplpush key1 key2                  | 从key1列表右侧弹出并插入到key2列表左侧                       |
  | brpoplpush | brpoplpush key1 key2                 | 从key1列表右侧弹出并插入到key2列表左侧，会阻塞               |
  | linsert    | linsert key BEFORE/AFTER pivot value | 将value插入到列表，且位于值pivot之前或之后                   |

  应用场景： 

  1、作为栈或队列使用 列表有序可以作为栈和队列使用

  2、可用于各种列表，比如用户列表、商品列表、评论列表等。

  示例：

  ```yml
  127.0.0.1:6379> lpush list:1 1 2 3 4 5 3
  (integer) 6
  127.0.0.1:6379> lrange list:1 0 -1 #末尾-1代表最后一位
  1) "3"
  2) "5"
  3) "4"
  4) "3"
  5) "2"
  6) "1"
  127.0.0.1:6379> lpop list:1 # 从0开始
  "5"
  127.0.0.1:6379> rpop list:1
  "1"
  127.0.0.1:6379> lindex list:1 1
  "3"
  127.0.0.1:6379> lrange list:1 0 -1
  1) "4"
  2) "3"
  3) "2"
  127.0.0.1:6379> lindex list:1 1
  "3"
  127.0.0.1:6379> rpoplpush list:1 list:2
  "2"
  127.0.0.1:6379> lrange list:2 0 -1
  1) "2"
  127.0.0.1:6379> lrange list:1 0 -1
  1) "4"
  2) "3"
  ```

  ##### list的底层结构（压缩列表（ziplist）、双向列表（adlist）、快速列表（quicklist））：

  - ###### 压缩列表（ziplist）：

    > 压缩列表特点：
    >
    > ​	节省存储空间：是由一系列特殊编码的**连续内存块**组成的顺序型数据结构，是一个字节数组，可以包含多个节点（entry）。每个节点可以保存一个字节数组或一个整数。
    >
    > ​	节省内存：是一个字节数组，可以包含多个节点（entry）。每个节点可以保存一个字节数组或一个整数。

    压缩列表的数据结构如下：

    ![image-20210217102801786](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217102801786.png)

    zlbytes：压缩列表的字节长度 

    zltail：压缩列表尾元素相对于压缩列表起始地址的偏移量 

    zllen：压缩列表的元素个数 

    entry1..entryX : 压缩列表的各个节点 

    zlend：压缩列表的结尾，占一个字节，恒为0xFF（255） 

    entry元素的编码结构：

    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217102927332.png" alt="image-20210217102927332" style="zoom:80%;" />

    ​	previous_entry_length：前一个元素的字节长度 

    ​	encoding：表示当前元素的编码 

    ​	content：数据内容

    ziplist源码：

    ```c
    struct ziplist<T>{
      unsigned int zlbytes; // ziplist的长度字节数，包含头部、所有entry和zipend。
      unsigned int zloffset; // 从ziplist的头指针到指向最后一个entry的偏移量，用于快速反向查询
      unsigned short int zllength; // entry元素个数
      T[] entry; // 元素值
      unsigned char zlend; // ziplist结束符，值固定为0xFF
    }
    typedef struct zlentry {
      unsigned int prevrawlensize; //previous_entry_length字段的长度
      unsigned int prevrawlen; //previous_entry_length字段存储的内容
      unsigned int lensize; //encoding字段的长度
      unsigned int len; //数据内容长度
      unsigned int headersize; //当前元素的首部长度，即previous_entry_length字段长度与 encoding字段长度之和。
      unsigned char encoding; //数据类型
      unsigned char *p; //当前元素首地址
    } zlentry;
    ```

    **应用场景：** 

    sorted-set和hash元素个数少且是小整数或短字符串（直接使用） 

    list用快速链表(quicklist)数据结构存储，而快速链表是双向列表与压缩列表的组合。（间接使用）

  - ###### 双向列表（adlist）：

    双向链表优势： 

    ![image-20210217110400754](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217110400754.png)

    1. 双向：链表具有前置节点和后置节点的引用，获取这两个节点时间复杂度都为O(1)。 
    2. 普通链表（单链表）：节点类保留下一节点的引用。链表类只保留头节点的引用，只能从头节点插 入删除 
    3. 无环：表头节点的 prev 指针和表尾节点的 next 指针都指向 NULL,对链表的访问都是以 NULL 结束。 环状：头的前一个节点指向尾节点 
    4. 带链表长度计数器：通过 len 属性获取链表长度的时间复杂度为 O(1)。 
    5. 多态：链表节点使用 void* 指针来保存节点值，可以保存各种不同类型的值。

  - ###### 快速列表（quicklist）：

    quicklist是一个双向链表，链表中的每个节点时一个ziplist结构。quicklist中的每个节点ziplist都能够存储多个数据元素。（快速列表就是双向列表和压缩列表的结合）

    ![image-20210217110459424](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217110459424.png)

    ​	quicklist源码：

    ```c
    typedef struct quicklist {
    	quicklistNode *head; // 指向quicklist的头部
    	quicklistNode *tail; // 指向quicklist的尾部
    	unsigned long count; // 列表中所有数据项的个数总和
    	unsigned int len; // quicklist节点的个数，即ziplist的个数
    	int fill : 16; // ziplist大小限定，由list-max-ziplist-size给定(Redis设定)
    	unsigned int compress : 16; // 节点压缩深度设置，由list-compress-depth给定(Redis设定)
    } quicklist;
    
    ```

    quicklistNode源码（就是图中蓝色部分）：

    ```c
    typedef struct quicklistNode {
    	struct quicklistNode *prev; // 指向上一个ziplist节点
    	struct quicklistNode *next; // 指向下一个ziplist节点
    	unsigned char *zl; // 数据指针，如果没有被压缩，就指向ziplist结构，反之指向 quicklistLZF结构
    	unsigned int sz; // 表示指向ziplist结构的总长度(内存占用长度)
    	unsigned int count : 16; // 表示ziplist中的数据项个数
    	unsigned int encoding : 2; // 编码方式，1--ziplist，2--quicklistLZF
    	unsigned int container : 2; // 预留字段，存放数据的方式，1--NONE，2--ziplist
    	unsigned int recompress : 1; // 解压标记，当查看一个被压缩的数据时，需要暂时解压，标记此参数为 1，之后再重新进行压缩
    	unsigned int attempted_compress : 1; // 测试相关
    	unsigned int extra : 10; // 扩展字段，暂时没用
    } quicklistNode;
    ```

    ##### 应用场景 ：

    列表(List)的底层实现、发布与订阅、慢查询、监视器等功能。

    ##### quicklist对ziplist的改进：

    quicklist每个节点的实际数据存储结构为ziplist，这种结构的优势在于节省存储空间。为了进一步降低 ziplist的存储空间，还可以对ziplist进行压缩，压缩的算法是LZF。

    > LZF：数据与前面重复的记录重复位置及长度，不重复的记录原始数据。

  #### 双链表和单链表的区别？

  - ##### 单链表：

    单链表相对于双链表存储空间要小（每个节点可以少存一个指针）

    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210429091654051.png" alt="image-20210429091654051" style="zoom:80%;" />

  - ##### 双链表：

    双链表删除节点要比单链表快一倍：

    - 删除单链表中的某个结点时，一定要得到待删除结点的前驱，得到该前驱有两种方法，第一种方法是在定位待删除结点的同时一路保存当前结点的前驱。第二种方法是在定位到待删除结点之后，重新从单链表表头开始来定位前驱。尽管通常会采用方法一。但其实这两种方法的效率是一样的，指针的总的移动操作都会有2*i次。而如果用双向链表，则不需要定位前驱结点。因此指针总的移动操作为i次。
    - 查找时也一样，我们可以借用二分法的思路，从head（首节点）向后查找操作和last（尾节点）向前查找操作同步进行，这样双链表的效率可以提高一倍。

    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210429091707276.png" alt="image-20210429091707276" style="zoom:80%;" />

    可是为什么市场上单链表的使用多余双链表呢？

    从存储结构来看，每个双链表的节点要比单链表的节点多一个指针，而长度为n就需要 n*length（这个指针的length在32位系统中是4字节，在64位系统中是8个字节） 的空间，这在一些追求时间效率不高应用下并不适应，因为它占用空间大于单链表所占用的空间；这时设计者就会采用以时间换空间的做法，这时一种工程总体上的衡量。

- ##### set集合类型 Set：

  Set：无序、唯一元素 

  集合中最大的成员数为 2^32 - 1 

  常见操作命令如下表：

  | 命令名称    | 命令格式                | 描述                                   |
  | ----------- | ----------------------- | -------------------------------------- |
  | sadd        | sadd key mem1 mem2 .... | 为集合添加新成员                       |
  | srem        | srem key mem1 mem2 .... | 删除集合中指定成员                     |
  | smembers    | smembers key            | 获得集合中所有元素                     |
  | spop        | spop key                | 返回集合中一个随机元素，并将该元素删除 |
  | srandmember | srandmember key         | 返回集合中一个随机元素，不会删除该元素 |
  | scard       | scard key               | 获得集合中元素的数量                   |
  | sismember   | sismember key member    | 判断元素是否在集合内                   |
  | sinter      | sinter key1 key2 key3   | 求多集合的交集                         |
  | sdiff       | sdiff key1 key2 key3    | 求多集合的差集                         |
  | sunion      | sunion key1 key2 key3   | 求多集合的并集                         |

  集合中最大的成员数为 2^32 - 1 （40亿）

  常见操作命令如下表：

  ​		应用场景： 

  ​		适用于不能重复的且不需要顺序的数据结构 

  ​		比如：关注的用户，还可以通过spop进行随机抽奖

  ​		举例：

  ```c
  127.0.0.1:6379> sadd set:1 a b c d
  (integer) 4
  127.0.0.1:6379> smembers set:1
  1) "d"
  2) "b"
  3) "a"
  4) "c"
  127.0.0.1:6379> srandmember set:1
  "c"
  127.0.0.1:6379> srandmember set:1
  "b"
  127.0.0.1:6379> sadd set:2 b c r f
  (integer) 4
  127.0.0.1:6379> sinter set:1 set:2
  1) "b"
  2) "c"
  127.0.0.1:6379> spop set:1
  "d"
  127.0.0.1:6379> smembers set:1
  1) "b"
  2) "a"
  3) "c"
  ```


  - ###### 整数集合（intset）：

    > 整数集合特点：
    >
    > ​	是一个有序的（整数升序）、存储整数的连续存储结构。
    >
    > ​	当Redis集合类型的元素都是整数并且都处在64位有符号**整数范围内（2^64）**，使用该结构体存储。

    ```c
    127.0.0.1:6379> sadd set:001 1 3 5 6 2
    (integer) 564位有符号**整数
    127.0.0.1:6379> object encoding set:001 //intset只能存储64位有符号整数（2^64）
    "intset"
    127.0.0.1:6379> sadd set:004 1 100000000000000000000000000 9999999999 
    (integer) 3
    127.0.0.1:6379> object encoding set:004
    "hashtable"
    ```

    intset的结构图如下：

    ![image-20210217105446290](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217105446290.png)

    ```c
    typedef struct intset{
    	//编码方式
    	uint32_t encoding;
    	//集合包含的元素数量
    	uint32_t length;
    	//保存元素的数组
    	int8_t contents[];
    }intset;
    ```

    应用场景： 

    可以保存类型为int16_t、int32_t 或者int64_t 的整数值，并且保证集合中不会出现重复元素。

- ##### ZSet有序集合类型：

  ZSet(SortedSet) 有序集合： 元素本身是无序不重复的

  每个元素关联一个分数(score) 

  可按分数排序，分数可重复 

  常见操作命令如下表：

  ![image-20210216163835340](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216163835340.png)

  应用场景： 

  由于可以按照分值排序，所以适用于各种**排行榜**。比如：点击排行榜、销量排行榜、关注排行榜等。 

  举例：

  ```yml
  127.0.0.1:6379> zadd hit:1 100 item1 20 item2 45 item3
  (integer) 3
  127.0.0.1:6379> zcard hit:1
  (integer) 3
  127.0.0.1:6379> zscore hit:1 item3
  "45"
  127.0.0.1:6379> zrevrange hit:1 0 -1
  1) "item1"
  2) "item3"
  3) "item2"
  127.0.0.1:6379>
  ```

  由于可以按照分值排序，所以适用于各种排行榜。比如：点击排行榜、销量排行榜、关注排行榜等。 

  ##### 举例：怎样用redis实现获取一个数组的topN？

  **答：**底层使用ZSet，因为其不重复（排行榜不能有重复），以及使用权重排序的特性，我们可以根据业务分配权重，根据权重排序达到实现排行榜的效果（**注：**因为ZSet默认权重小的排在前面，所以权重使用负数可以达到权重大的排在前面）

  ##### Sort Set的有序Set的底层结构（跳跃表）：

  如下图，每一层的上层个数是下一层个数的1/2，

  - **跳跃表查询：**

    如果访问46，顺序如下：

    L4访问55 --> L3访问21、55  --> L2访问37、55  --> L1访问46

  ![这里写图片描述](https://img-blog.csdn.net/20161205211539787)

  - **跳跃表插入：**

    先查询到位置后再插入，比如L1插入45，那么L2是不是插入45由抛硬币决定（1/2概率），L3也是如此，以此类推。

  - **跳跃表删除 ：**

    找到指定元素并删除每层的该元素即可。

    ##### 跳跃表复杂度：

    查询复杂度是O(logn)。

    插入和删除的时间复杂度就是查询元素插入位置的时间复杂度，这不难理解，所以是O(logn)。

    ##### 应用场景：

    有序集合的实现

    ##### Redis跳跃表的实现：

    ```c
    //跳跃表节点
    typedef struct zskiplistNode {
        sds ele; /* 存储字符串类型数据 Redis3.0版本中使用robj类型表示，
    				但是在Redis4.0.1中直接使用sds类型表示 */
    	double score;//存储排序的分值
    	struct zskiplistNode *backward;//后退指针，指向当前节点最底层的前一个节点
    	/*
    	层，柔性数组，随机生成1-64的值
    	*/
    	struct zskiplistLevel {
    		struct zskiplistNode *forward; //指向本层下一个节点
    		unsigned int span;//本层下个节点到本节点的元素个数
    	} level[];
    } zskiplistNode;
    //链表
    typedef struct zskiplist{
    	//表头节点和表尾节点
    	structz skiplistNode *header, *tail;
    	//表中节点的数量
    	unsigned long length;
    	//表中层数最大的节点的层数
    	int level;
    }zskiplist;
    ```


#### Redis不常用value数据类型和相关命令：

- ##### bitmap位图类型：

  bitmap是进行位操作的 

  通过一个bit位来表示某个元素对应的值或者状态,其中的key就是对应元素本身。 

  bitmap本身会极大的节省储存空间。 

  常见操作命令如下表：

  ![image-20210216164548271](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164548271.png)

  应用场景： 

  1、用户每月签到，用户id为key ， 日期作为偏移量 1表示签到 

  2、统计活跃用户, 日期为key，用户id为偏移量 1表示活跃 

  3、查询用户在线状态， 日期为key，用户id为偏移量 1表示在线 

  举例：

  ```yml
  127.0.0.1:6379> setbit user:sign:1000 20200101 1 #id为1000的用户20200101签到
  (integer) 0
  127.0.0.1:6379> setbit user:sign:1000 20200103 1 #id为1000的用户20200103签到
  (integer) 0
  127.0.0.1:6379> getbit user:sign:1000 20200101 #获得id为1000的用户20200101签到状态
  1 表示签到
  (integer) 1
  127.0.0.1:6379> getbit user:sign:1000 20200102 #获得id为1000的用户20200102签到状态
  0表示未签到
  (integer) 0
  127.0.0.1:6379> bitcount user:sign:1000 # 获得id为1000的用户签到次数
  (integer) 2
  127.0.0.1:6379> bitpos user:sign:1000 1 #id为1000的用户第一次签到的日期
  (integer) 20200101
  127.0.0.1:6379> setbit 20200201 1000 1 #20200201的1000号用户上线
  (integer) 0
  127.0.0.1:6379> setbit 20200202 1001 1 #20200202的1000号用户上线
  (integer) 0
  127.0.0.1:6379> setbit 20200201 1002 1 #20200201的1002号用户上线
  (integer) 0
  127.0.0.1:6379> bitcount 20200201 #20200201的上线用户有2个
  (integer) 2
  127.0.0.1:6379> bitop or desk1 20200201 20200202 #合并20200201的用户和20200202上线
  了的用户
  (integer) 126
  127.0.0.1:6379> bitcount desk1 #统计20200201和20200202都上线的用
  户个数
  (integer) 3
  ```

  

- ##### geo地理位置类型：

  geo是Redis用来处理位置信息的。在Redis3.2中正式使用。主要是利用了Z阶曲线、Base32编码和 geohash算法

  Z阶曲线 在x轴和y轴上将十进制数转化为二进制数，采用x轴和y轴对应的二进制数依次交叉后得到一个六位数编 码。把数字从小到大依次连起来的曲线称为Z阶曲线，Z阶曲线是把多维转换成一维的一种方法。

  ![image-20210216164646514](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164646514.png)

  - ##### Base32编码

    Base32这种数据编码机制，主要用来把二进制数据编码成可见的字符串，其编码规则是：任意给定一 个二进制数据，以5个位(bit)为一组进行切分(base64以6个位(bit)为一组)，对切分而成的每个组进行编 码得到1个可见字符。Base32编码表字符集中的字符总数为32个（0-9、b-z去掉a、i、l、o），这也是 Base32名字的由来。

    ![image-20210216164702930](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164702930.png)

  - ##### geohash算法

    Gustavo在2008年2月上线了geohash.org网站。Geohash是一种地理位置信息编码方法。 经过 geohash映射后，地球上任意位置的经纬度坐标可以表示成一个较短的字符串。可以方便的存储在数据 库中，附在邮件上，以及方便的使用在其他服务中。以北京的坐标举例，[39.928167,116.389550]可以转换成 wx4g0s8q3jf9 。

    Redis中经纬度使用52位的整数进行编码，放进zset中，zset的value元素是key，score是GeoHash的 52位整数值。在使用Redis进行Geo查询时，其内部对应的操作其实只是zset(skiplist)的操作。通过zset 的score进行排序就可以得到坐标附近的其它元素，通过将score还原成坐标值就可以得到元素的原始坐 标。

    常见操作命令如下表：

    ![image-20210216164724207](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164724207.png)

    应用场景： 

    1、记录地理位置 

    2、计算距离 

    3、查找"附近的人" 

    ​	举例：

- ##### stream数据流类型：

  stream是Redis5.0后新增的数据结构，用于可持久化的消息队列。 

  几乎满足了消息队列具备的全部内容，包括： 

  - 消息ID的序列化生成 
  - 消息遍历 
  - 消息的阻塞和非阻塞读取 
  - 消息的分组消费 
  - 未完成消息的处理 
  - 消息队列监控 

  每个Stream都有唯一的名称，它就是Redis的key，首次使用 xadd 指令追加消息时自动创建。 

  常见操作命令如下表：

  ![image-20210216164848448](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164848448.png)

  ![image-20210216164854808](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216164854808.png)

  应用场景： 消息队列的使用

  ```yml
  127.0.0.1:6379> xadd topic:001 * name zhangfei age 23
  "1591151905088-0"
  127.0.0.1:6379> xadd topic:001 * name zhaoyun age 24 name diaochan age 16
  "1591151912113-0"
  127.0.0.1:6379> xrange topic:001 - +
  1) 1) "1591151905088-0"
  2) 1) "name"
  2) "zhangfei"
  3) "age"
  4) "23"
  2) 1) "1591151912113-0"
  2) 1) "name"
  2) "zhaoyun"
  3) "age"
  4) "24"
  5) "name"
  6) "diaochan"
  7) "age"
  8) "16"
  127.0.0.1:6379> xread COUNT 1 streams topic:001 0
  1) 1) "topic:001"
  2) 1) 1) "1591151905088-0"
  2) 1) "name"
  2) "zhangfei"
  3) "age"
  4) "23"
  #创建的group1
  127.0.0.1:6379> xgroup create topic:001 group1 0
  OK
  # 创建cus1加入到group1 消费 没有被消费过的消息 消费第一条
  127.0.0.1:6379> xreadgroup group group1 cus1 count 1 streams topic:001 >
  1) 1) "topic:001"
  2) 1) 1) "1591151905088-0"
  2) 1) "name"
  2) "zhangfei"
  3) "age"
  4) "23"
  #继续消费 第二条
  127.0.0.1:6379> xreadgroup group group1 cus1 count 1 streams topic:001 >
  1) 1) "topic:001"
  2) 1) 1) "1591151912113-0"
  2) 1) "name"
  2) "zhaoyun"
  3) "age"
  4) "24"
  5) "name"
  6) "diaochan"
  7) "age"
  8) "16"
  #没有可消费
  127.0.0.1:6379> xreadgroup group group1 cus1 count 1 streams topic:001 >
  (nil)
  ```

  几乎满足了消息队列具备的全部内容，包括：
  
  - 消息ID的序列化生成 
  - 消息遍历 
  - 消息的阻塞和非阻塞读取 
  - 消息的分组消费 
  - 未完成消息的处理 
  - 消息队列监控
  
  ##### 流对象（stream） 底层结构：
  
  stream主要由：消息、生产者、消费者和消费组构成。
  
  ![image-20210217112041168](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217112041168.png)
  
  Redis Stream的底层主要使用了listpack（紧凑列表）和Rax树（基数树）。
  
  ##### listpack：
  
  listpack表示一个字符串列表的序列化，listpack可用于存储字符串或整数。用于存储stream的消息内 容。
  
  结构如下图：
  
  ![image-20210217112104040](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217112104040.png)
  
  ##### Rax树：
  
  Rax 是一个有序字典树 (基数树 Radix Tree)，按照 key 的字典序排列，支持快速地定位、插入和删除操作。
  
  ![image-20210217163425239](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217163425239.png)
  
  Rax 被用在 Redis Stream 结构里面用于存储消息队列，在 Stream 里面消息 ID 的前缀是时间戳 + 序 号，这样的消息可以理解为时间序列消息。使用 Rax 结构 进行存储就可以快速地根据消息 ID 定位到具 体的消息，然后继续遍历指定消息 之后的所有消息。
  
  应用场景：
  
   stream的底层实现

### Redis底层数据结构：

![image-20210216174707306](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216174707306.png)

如上图所示，Redis中存在“数据库”的概念，该结构由Redis.h中的RedisDb定义，一个Redis实例对应多个RedisDB，一个RedisDB对应多个RedisObject。

当Redis 服务器初始化时，会预先分配 **16** 个数据库

所有数据库保存到结构 RedisServer 的一个成员 RedisServer.db 数组中

RedisClient中存在一个名叫db的指针指向当前使用的数据库

- ##### RedisDB结构体源码：

  ```c
  typedef struct RedisDb {
  	int id; //id是数据库序号，为0-15（默认Redis有16个数据库）
  	long avg_ttl; //存储的数据库对象的平均ttl（time to live），用于统计
  	dict *dict; //存储数据库所有的key-value
  	dict *expires; //存储key的过期时间
  	dict *blocking_keys;//blpop 存储阻塞key和客户端对象
  	dict *ready_keys;//阻塞后push 响应阻塞客户端 存储阻塞后push的key和客户端对象
  	dict *watched_keys;//存储watch监控的的key和客户端对象
  } RedisDb;
  ```

- ##### RedisObject结构：

  ```c
  typedef struct RedisObject {
  	unsigned type:4;//类型：下面详解
  	unsigned encoding:4;//编码：下面详解
  	void *ptr;//指向底层实现数据结构的指针（就是指向五种对象类型的指针）
  	//...
  	int refcount;//引用计数，当对象的refcount>1时，称为共享对象
  	//...
  	unsigned lru:LRU_BITS; //记录最后一次被命令程序访问的时间（ 4.0 版本占 24 位，2.6 版本占 22 位）
  	//...
  }robj;
  ```

  **type**：

  Redis_STRING(字符串)、Redis_LIST (列表)、Redis_HASH(哈希)、Redis_SET(集合)、Redis_ZSET(有 序集合)。

  当我们执行 type 命令时，便是通过读取 RedisObject 的 type 字段获得对象的类型

  ```java
  127.0.0.1:6379> type a1
  string
  ```

  **encoding**：

  每个对象有不同的实现编码

  edis 可以根据不同的使用场景来为对象设置不同的编码，大大提高了 Redis 的灵活性和效率。 通过 

  object encoding 命令，可以查看对象采用的编码方式：

  ```c
  127.0.0.1:6379> object encoding a1
  "int"
  ```

##### 10种encoding：

- ##### set对象：

  - intset ： 元素是64位以内的整数

  - hashtable：元素是64位以外的整数

    ```c
    127.0.0.1:6379> sadd set:001 1 3 5 6 2
    (integer) 5
    127.0.0.1:6379> object encoding set:001
    "intset"
    127.0.0.1:6379> sadd set:004 1 100000000000000000000000000 9999999999
    (integer) 3
    127.0.0.1:6379> object encoding set:004
    "hashtable"
    ```

- ##### string对象：

  - int：整数

    ```c
    127.0.0.1:6379> set n1 123
    OK
    127.0.0.1:6379> object encoding n1
    "int"
    ```

  - embstr：小字符串 长度小于44个字节

    ```c
    127.0.0.1:6379> set name:001 zhangfei
    OK
    127.0.0.1:6379> object encoding name:001
    "embstr
    ```

  - raw：大字符串 长度大于44个字节

    ```c
    127.0.0.1:6379> set address:001
    asdasdasdasdasdasdsadasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdas
    dasdasdas
    OK
    127.0.0.1:6379> object encoding address:001
    "raw"
    ```

- ##### list对象：

  - quicklist：快速列表

    ```c
    127.0.0.1:6379> lpush list:001 1 2 5 4 3
    (integer) 5
    127.0.0.1:6379> object encoding list:001
    "quicklist"
    ```

- ##### hash对象：

  - hashtable：当散列表元素的个数比较多或元素不是小整数或短字符串时：

    ```c
    127.0.0.1:6379> hmset user:003
    username111111111111111111111111111111111111111111111111111111111111111111111111
    11111111111111111111111111111111 zhangfei password 111 num
    2300000000000000000000000000000000000000000000000000
    OK
    127.0.0.1:6379> object encoding user:003
    "hashtable"
    ```

  - ziplist：当散列表元素的个数比较少，且元素都是小整数或短字符串时：

    ```c
    127.0.0.1:6379> hmset user:001 username zhangfei password 111 age 23 sex M
    OK
    127.0.0.1:6379> object encoding user:001
    "ziplist"
    ```

- ##### zset对象：

  有序集合的编码是压缩列表和跳跃表+hashtable

  - ziplist：

    Redis_ENCODING_ZIPLIST（压缩列表） 当元素的个数比较少，且元素都是小整数或短字符串时。

    ```c
    127.0.0.1:6379> zadd hit:1 100 item1 20 item2 45 item3
    (integer) 3
    127.0.0.1:6379> object encoding hit:1
    "ziplist"
    ```

  - skiplist + hashtable：

    Redis_ENCODING_SKIPLIST（跳跃表+字典） 当元素的个数比较多或元素不是小整数或短字符串时。

    ```c
    127.0.0.1:6379> zadd hit:2 100
    item1111111111111111111111111111111111111111111111111111111111111111111111111111
    1111111111111111111111111111111111 20 item2 45 item3
    (integer) 3
    127.0.0.1:6379> object encoding hit:2
    "skiplist"
    ```

### 缓存过期和淘汰策略：

- #### maxmemory设置和不设置的场景：
  - **不设置场景：**作为**DB**使用，不会淘汰，保证了数据的完整性。[缓存淘汰策略maxmemory-policy 为：noeviction（禁止驱逐）]
  - **设置场景：**作为**缓存**使用，当趋近maxmemory时，通过缓存淘汰策略（淘汰策略配置maxmemory-policy 参数），从内存中删除对象。（maxmemory ： 默认为0 不限制）
  
  引申出来的面试题：
  
  #### Redis有哪两种用途？
  
  **答：**一个是作为DB使用，一个是作为缓存使用。作为DB使用不能删除数据，作为缓存使用需设置淘汰策略。
  
  #### expire介绍：
  
  ```c
  127.0.0.1:6379> expire name 2 #2秒失效
  (integer) 1
  127.0.0.1:6379> get name
  (nil)
  127.0.0.1:6379> set name zhangfei
  OK
  127.0.0.1:6379> ttl name #永久有效
  (integer) -1
  127.0.0.1:6379> expire name 30 #30秒失效
  (integer) 1
  127.0.0.1:6379> ttl name #还有24秒失效
  (integer) 24
  127.0.0.1:6379> ttl name #失效
  (integer) -2
  ```
  
  #### expire源码：
  
  ```c
  typedef struct RedisDb {
  dict *dict; -- key Value
  dict *expires; -- key ttl
  dict *blocking_keys;
  dict *ready_keys;
  dict *watched_keys;
  int id;
  } RedisDb;
  ```
  
  #### expire和setex命令设置失效时间区别：
  
  expire命令设置一个key的失效时间实时，Redis 首先到 dict 这个字典表中查找要设置的key是否存在，如果存在就将这个key和失效时间添加到 expires 这个字典表。
  
   setex命令向系统插入数据时，Redis 首先将 Key 和 Value 添加到 dict 这个字典表中，然后将 Key 和失效时间添加到 expires 这个字典表中。
  
   setex命令：
  
  ```cmd
  setex key seconds value
  //例：设置键为aa值为aa过期时间为3秒的数据
  setex aa 3 aa
  ```

#### Redis数据删除策略：

Redis目前采用**惰性删除+主动删除**的方式。

- ##### 定时删除 

  在设置键的过期时间的同时，创建一个定时器，让定时器在键的过期时间来临时，立即执行对键的删除操作。 需要创建定时器，而且消耗CPU，一般不推荐使用。

  > **为什么不用定时删除策略？：**
  >
  > 定时删除，用一个定时器来负责监视key，当这个key过期就自动删除，虽然内存及时释放，但是十分消耗CPU资源，在大并发请求下CPU要尽可能的把时间都用在处理请求，而不是删除key，因此没有采用这一策略

- ##### 惰性删除

  读取数据之前检查有么有失效，如果失效就删除（检查是否失效函数expireIfNeeded）：

  ```c
  int expireIfNeeded(RedisDb *db, robj *key) {
  	//获取主键的失效时间 get当前时间-创建时间>ttl
  	long long when = getExpire(db,key);
  	//假如失效时间为负数，说明该主键未设置失效时间（失效时间默认为-1），直接返回0
  	if (when < 0) return 0;
  	//假如Redis服务器正在从RDB文件中加载数据，暂时不进行失效主键的删除，直接返回0
  	if (server.loading) return 0;
  	...
      //如果以上条件都不满足，就将主键的失效时间与当前时间进行对比，如果发现指定的主键
  	//还未失效就直接返回0
  	if (mstime() <= when) return 0;
  	//如果发现主键确实已经失效了，那么首先更新关于失效主键的统计个数，然后将该主键失
  	//效的信息进行广播，最后将该主键从数据库中删除
  	server.stat_expiredkeys++;
  	propagateExpire(db,key);
  	return dbDelete(db,key);
  }
  ```

  > 惰性删除的缺点是占用内存

- ##### 主动删除 

  Redis的LRU算法：

  1. 访问数据如果链表有则移动到头部，没有则插入到头部
  2. 当链表满的时候，将链表尾部的数据丢弃。

  Redis的LFU算法：

  1. 访问某个key时，该key的freq就要加一
  2. 当容量满了，会对freq最小的删除，如果最小的freq对应多个key，则删除其中最旧的那一个。

  主动删除设置的删除策略：

  1. **volatile-lru** 使用LRU算法删除一个键(只针对设置了过期时间的key）
  2. **allkeys-lru** 使用LRU算法删除一个键
  3. **volatile-lfu** 使用LFU算法删除一个键(只针对设置了过期时间的key)
  4. **allkeys-lfu** 使用LFU算法删除一个键
  5. **volatile-random** 随机删除一个键(只针对设置了过期时间的key)
  6. **allkeys-random** 随机删除一个键
  7. **volatile-ttl**  删除最早过期的一个键
  8. **noeviction** 不删除键，返回错误信息(Redis默认选项)

### Redis客户端访问：

- #### Java程序访问Redis：


  采用jedis API进行访问即可 

  1、关闭RedisServer端的防火墙

  ```xml
  systemctl stop firewalld（默认）
  systemctl disable firewalld.service（设置开启不启动）
  ```

  2、新建maven项目后导入Jedis包 

  pom.xml

  ```xml
  <dependency>
  	<groupId>Redis.clients</groupId>
  	<artifactId>jedis</artifactId>
  	<version>2.9.0</version>
  </dependency>
  ```

  3、写程序

  ```java
  @Test
  public void testConn(){
  	//与Redis建立连接 IP+port
  	Jedis Redis = new Jedis("192.168.127.128", 6379);
  	//在Redis中写字符串 key value
  	Redis.set("jedis:name:1","jd-zhangfei");
  	//获得Redis中字符串的值
  	System.out.println(Redis.get("jedis:name:1"));
  	//在Redis中写list
  	Redis.lpush("jedis:list:1","1","2","3","4","5");
  	//获得list的长度
  	System.out.println(Redis.llen("jedis:list:1"));
  }
  ```

- #### Spring访问Redis：


1. 新建Spring项目 

   新建maven项目

![image-20210216171518102](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216171518102.png)

​		添加Spring依赖：

```xml
<dependencies>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-beans</artifactId>
		<version>5.2.5.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-core</artifactId>
		<version>5.2.5.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-context</artifactId>
		<version>5.2.5.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-test</artifactId>
		<version>5.2.5.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>junit</groupId>
		<artifactId>junit</artifactId>
		<version>4.12</version>
		<scope>test</scope>
	</dependency>
</dependencies>
```

2. 添加Redis依赖

```xml
<dependency>
	<groupId>org.springframework.data</groupId>
	<artifactId>spring-data-Redis</artifactId>
	<version>1.0.3.RELEASE</version>
</dependency>

```

3. 添加Spring配置文件 

   添加Redis.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="propertyConfigurer"
          class="org.springframework.beans.factory.config.PropertyPlaceholderConfigur
er">
        <property name="locations">
            <list>
                <value>classpath:Redis.properties</value>
            </list>
        </property>
    </bean>
    <!-- Redis config -->
    <bean id="jedisPoolConfig" class="Redis.clients.jedis.JedisPoolConfig">
        <property name="maxActive" value="${Redis.pool.maxActive}" />
        <property name="maxIdle" value="${Redis.pool.maxIdle}" />
        <property name="maxWait" value="${Redis.pool.maxWait}" />
        <property name="testOnBorrow" value="${Redis.pool.testOnBorrow}" />
    </bean>
    <bean id="jedisConnectionFactory"
          class="org.springframework.data.Redis.connection.jedis.JedisConnectionFactor
y">
        <property name="hostName" value="${Redis.server}"/>
        <property name="port" value="${Redis.port}"/>
        <property name="timeout" value="${Redis.timeout}" />
        <property name="poolConfig" ref="jedisPoolConfig" />
    </bean>
    <bean id="RedisTemplate"
          class="org.springframework.data.Redis.core.RedisTemplate">
        <property name="connectionFactory" ref="jedisConnectionFactory"/>
        <property name="KeySerializer">
            <bean
                    class="org.springframework.data.Redis.serializer.StringRedisSerializer">
            </bean>
        </property>
        <property name="ValueSerializer">
            <bean
                    class="org.springframework.data.Redis.serializer.StringRedisSerializer">
            </bean>
        </property>
    </bean>
</beans>
```

4. 添加properties文件

   添加Redis.properties

```properties
Redis.pool.maxActive=100
Redis.pool.maxIdle=50
Redis.pool.maxWait=1000
Redis.pool.testOnBorrow=true
Redis.timeout=50000
Redis.server=192.168.72.128
Redis.port=6379
```

5. 编写测试用例

```java
@ContextConfiguration({ "classpath:Redis.xml" })
public class RedisTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    private RedisTemplate<Serializable, Serializable> rt;
    @Test
    public void testConn() {
        rt.opsForValue().set("name","zhangfei");
        System.out.println(rt.opsForValue().get("name"));
    }
}
```

- #### SpringBoot访问Redis：


1. 新建springboot项目

![image-20210216172222897](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216172222897.png)

![image-20210216172231484](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210216172231484.png)

添加Redis依赖包

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-Redis</artifactId>
</dependency>
```

2. 添加配置文件application.yml

```yml
spring:
  Redis:
    host: 192.168.72.128
    port: 6379
    jedis:
	  pool:
		 min-idle: 0
		 max-idle: 8
		 max-active: 80
		 max-wait: 30000
		 timeout: 3000
```

3. 添加配置类RedisConfig

```java
@Configuration
public class RedisConfig {
    @Autowired
    private RedisConnectionFactory factory;
    @Bean
    public RedisTemplate<String, Object> RedisTemplate() {
        RedisTemplate<String, Object> RedisTemplate = new RedisTemplate<>();
        RedisTemplate.setKeySerializer(new StringRedisSerializer());
        RedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        RedisTemplate.setHashValueSerializer(new StringRedisSerializer());
        RedisTemplate.setValueSerializer(new StringRedisSerializer());
        RedisTemplate.setConnectionFactory(factory);
        return RedisTemplate;
    }
}
```

4. 添加RedisController

```java
@RestController
@RequestMapping(value = "/Redis")
public class RedisController {
    @Autowired
    RedisTemplate RedisTemplate;

    @GetMapping("/put")
    public String put(@RequestParam(required = true) String key,
                      @RequestParam(required = true) String value) {
		//设置过期时间为20秒
        RedisTemplate.opsForValue().set(key, value, 20, TimeUnit.SECONDS);
        return "Success";
    }

    @GetMapping("/get")
    public String get(@RequestParam(required = true) String key) {
        return (String) RedisTemplate.opsForValue().get(key);
    }
}
```

5. 修改Application并运行

```java
@SpringBootApplication
@EnableCaching
public class SpringbootRedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootRedisApplication.class, args);
    }
}
```

## 第三讲：

> 该讲中心是讲解Redis客户端和Redis服务端通信机制和过程。

### Redis的请求响应模式（串行和全双工）：

1. 串行的请求响应模式：

   ​	客户端发送请求，服务端响应，客户端收到响应后，再发起第二个请求，服务器端再响应。

2. 全双工的请求响应模式：

   > 全双工、半双工、单工区别：
   >
   > 单工数据传输只支持数据在一个方向上传输。
   >
   > 半双工数据传输允bai许数据在两个方向上传输，但是，在某一时刻，只允许数据在一个方向上传输，它实际上是一种切换方向的单工通信。
   >
   > 全双工数据通信允许数据同时在两个方向上传输。

   批量请求，批量响应，请求响应交叉进行，不会混淆(TCP双工)。

   > 通过pipeline实现批量的操作：
   >
   > pipeline的作用是将一批命令进行打包，然后发送给服务器，服务器执行完按顺序打包返回。

通过Jedis可以很方便的使用pipeline：

```java
    Jedis Redis = new Jedis("192.168.1.111", 6379);
    Redis.auth("12345678");//授权密码 对应Redis.conf的requirepass密码
    Pipeline pipe = jedis.pipelined();
    for (int i = 0; i <50000; i++) {
        pipe.set("key_"+String.valueOf(i),String.valueOf(i));
    }
    //将封装后的PIPE一次性发给Redis
    pipe.sync();
```

### Redis命令处理流程：

> 比如我Redis cli端发了一个命令，Redis server端是怎么处理并返回的？



![image-20210218085603779](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210218085603779.png)

上面流程部分讲解：

- Redis启动：

  - 启动调用 initServer方法：

    创建eventLoop（事件机制） 

    注册时间事件处理器 注册文件事件（socket）处理器 

    **监听 socket** 建立连接

- 建立Client：

  - Redis-cli建立socket 
  - Redis-server为每个连接（socket）创建一个 Client 对象 
  - 创建文件事件监听socket 
  - 指定事件处理函数

- 从socket读数据到输入缓冲区：

  从client中读取客户端的查询缓冲区内容

- 解析获取命令：

  - 将输入缓冲区中的数据解析成对应的命令 
  - 判断是单条命令还是多条命令并调用相应的解析器解析

- 执行命令：

  大致分三个部分： 

  - 调用 lookupCommand 方法获得对应的 RedisCommand 
  - 检测当前是否可以执行该命令 
  - 调用 call 方法真正执行命令

> socket：套接字（socket）是一个抽象层，应用程序可以通过它发送或接收数据。
>
> 缓冲区：主内存中特地预留出的内存。

### Redis的请求、响应格式和解析过程：

- #### Redis cli的请求格式（格式需遵循RESP协议，Redis server进行解析）：

  > 我们写的命令其实要通过转译服务端才能进行接收，转译过程见举例。

  ##### 格式规范：

  1、间隔符号，在Linux下是\r\n，在Windows下是\n 

  2、简单字符串 Simple Strings, 以 "+"加号 开头 

  3、错误 Errors, 以"-"减号 开头 

  4、整数型 Integer， 以 ":" 冒号开头

   5、大字符串类型 Bulk Strings, 以 "$"美元符号开头，长度限制512M 

  6、数组类型 Arrays，以 "*"星号开头

  #### 用SET命令来举例说明RESP协议的格式：

  ```c
  Redis> SET mykey Hello
  "OK"
  ```

  实际发送数据（查看bin目录下的appendonly.aof文件）：

  ```c
  *3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$5\r\nHello\r\n
  ```

  实际收到的响应数据：

  ```c
  +OK\r\n
  ```

  #### Redis cli的响应格式：

  ##### 状态回复 ：

  对于状态，回复的第一个字节是“+”

  ```c
  "+OK"
  ```

  ##### 错误回复：

  对于错误，回复的第一个字节是“ - ”

  ```c
  1. -ERR unknown command 'foobar'
  2. -WRONGTYPE Operation against a key holding the wrong kind of value
  ```

  ##### 整数回复：

  对于整数，回复的第一个字节是“：”

  ```c
  ":6"
  ```

  ##### 批量回复 ：

  对于批量字符串，回复的第一个字节是“$”

  ```c
  "$6 foobar"
  ```

  ##### 多条批量回复：

  对于多条批量回复（数组），回复的第一个字节是“*”

  ```c
  "*3"
  ```

  

#### Redis server请求解析过程：

解析分为三个步骤：

1.  解析命令请求参数数量：

   命令请求参数数量的协议格式为"*N\r\n" ,其中N就是数量，比如：

   ```c
   127.0.0.1:6379> set name:10 zhaoyun
   ```

   我们打开aof文件可以看到协议内容

   ```c
   *3(/r/n)
   $3(/r/n)
   set(/r/n)
   $7(/r/n)
   name:10(/r/n)
   $7(/r/n)
   zhaoyun(/r/n)
   ```

   首字符必须是“*”，使用"/r"定位到行尾，之间的数就是参数数量了。

2. 循环解析请求参数：

   首字符必须是"$"，使用"/r"定位到行尾，之间的数是参数的长度，从/n后到下一个"$"之间就是参数的值了，循环解析直到没有"$"。

3. 协议执行：

   协议的执行包括命令的调用和返回结果。

### Redis server事件处理：

Redis 的事件分为两大类：

- 文件事件：

  文件事件即Socket的读写事件，也就是IO事件。 包括客户端的连接、**命令请求**、数据回复、连接断开。

  Redis server**文件事件**处理的流程图如下：

  ![image-20210219143936586](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219143936586.png)

  如上图所示，整个文件事件采用了用单线程的Reactor模式，属于I/O多路复用的一种常见模式，I/O多路复用模型又有4种选择。

  > Reactor模式介绍：
  >
  > ​	正常多线程模式是采用一个线程管理一个socket。
  >
  > ​	Reactor思想是监听多个socket，一旦socket就绪（读就绪或者写就绪），将请求分发到不同的handler处理：
  >
  > ​	![image-20210219144923152](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219144923152.png)

  ##### 4种I/O多路复用模型（Reactor思想的四种实现）：

  select、poll、epoll对比：

  > **fd：**要监听的文件描述符
  >
  > **用户空间和内核空间：**操作系统的核心是内核控件，独立于普通的应用程序，可以访问受保护的内存空间，也有访问底层硬件设备的**所有权限**。
  >
  > 为了保证用户进程不能直接操作内核，保证内核的安全，操作系统将虚拟空间划分为两部分，一部分为内核空间，一部分为用户空间。
  >
  > **文件描述符：**打开现存文件或新建文件时，**内核**会返回一个文件描述符。读写文件也需要使用文件描述符来指定待读写的文件。
  >
  > **水平触发（LT）：**当事件就绪并通知应用程序时，应用程序可以不立即处理该事件，只要就绪的通知方法epoll_wait被调用时，会再次通知应用程序。
  >
  > **边缘触发（ET）：**当事件就绪并通知应用程序时，应用程序要立即处理该事件，再次调用epoll_wait时，不会再次通知应用程序（也就是说事件状态由未就绪变为就绪时只通知一次而LT只要调用epoll_wait方法就能通知多次）。
  >
  > ET模式很大程度上减少了epoll事件（epoll_wait）的触发次数，因此效率比LT模式下高。

  |            | **select**                                         | **poll**                                           | *epoll*                                                      |
  | ---------- | -------------------------------------------------- | -------------------------------------------------- | ------------------------------------------------------------ |
  | 操作方式   | 遍历                                               | 遍历                                               | 回调                                                         |
  | 底层实现   | 数组                                               | 链表                                               | 红黑树                                                       |
  | IO效率     | 每次调用都进行线性遍历，时间复杂度为O(n)           | 每次调用都进行线性遍历，时间复杂度为O(n)           | 事件通知方式，每当fd就绪，系统注册的回调函数就会被调用，将就绪fd放到readyList里面，时间复杂度O(1) |
  | 最大连接数 | 1024（x86）或2048（x64）可以配置具体大小           | 无上限                                             | 无上限                                                       |
  | fd拷贝     | 每次调用select，都需要把fd集合从用户态拷贝到内核态 | 每次调用select，都需要把fd集合从用户态拷贝到内核态 | 调用epoll_ctl时拷贝进内核并保存，之后每次epoll_wait不拷贝【epoll将活跃的文件描述符的事件存放到内核的一个事件表中，这样在用户空间和内核空间的copy只需一次（将事件表内容从用户空间存到内核空间）】 |
  | IO事件触发 | 只支持水平触发（LT）                               | 只支持水平触发（LT）                               | 支持水平触发（LT）、边缘触发（ET）                           |

- 时间事件：

  时间事件分为定时事件与周期事件，一个时间事件主要由以下三个属性组成：

  - id(全局唯一id) 
  - when (毫秒时间戳，记录了时间事件的到达时间) 
  - timeProc（时间事件处理器，当时间到达时，Redis就会调用相应的处理器来处理事件）

  ##### 定时事件与周期事件的区别？

  **答：**定时事件让一段程序在指定的时间之后执行一次，在达到后删除，之后不会再重复；周期事件：让一段程序每隔指定时间就执行一次，能重复执行。
  
  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210426212832883.png" alt="image-20210426212832883" style="zoom:80%;" />

## 第四讲：

### Redis为什么要持久化？

**答：**Redis是内存数据库，如果宕机后数据会消失，Redis重启后快速恢复数据，要提供持久化机制。

### Redis的两种持久化策略：

- #### RDB：

  ##### RDB存储内容：

  RDB是存储快照**某一时刻的数据**（Redis默认的存储方式）。

  ##### 触发快照的方式 ：

1. 符合自定义配置的快照规则 

2. 执行save或者bgsave命令 

   save 命令示例：

   ```c
   save "" # 不使用RDB存储 不能主从
   save 900 1 # 表示15分钟（900秒钟）内至少1个键被更改则进行快照。
   save 300 10 # 表示5分钟（300秒）内至少10个键被更改则进行快照。
   save 60 10000 # 表示1分钟内至少10000个键被更改则进行快照。
   ```

   bgsave命令示例：

   ```c
   127.0.0.1:6379> bgsave
   Background saving started
   ```

3. 执行flushall命令 

4. 执行主从复制操作 (第一次)

   ##### RDB执行流程：

![image-20210219213607280](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219213607280.png)

​	如上图所示：

1. 生成RDB是通过子进程来生成的

2. 如果已经有子进程则直接返回不能执行fork

3. 父进程执行fork【调用操作系统（OS）函数复制主进程】操作创建子进程

4. 创建后父进程可以响应其他命令。

   ##### RDB文件结构：

   ![image-20210219213954949](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219213954949.png)

   1、头部5字节固定为“Redis”字符串 

   2、4字节“RDB”版本号（不是Redis版本号），当前为9，填充后为0009 

   3、辅助字段，以key-value的形式

   ![image-20210219214103986](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219214103986.png)

   4、存储数据库号码

   5、字典大小

   6、过期key 

   7、主要数据，以key-value的形式存储 

   8、结束标志 

   9、校验和，就是看文件是否损坏，或者是否被修改。

   ##### RDB的优缺点：

   - ##### 优点：

     - RDB是二进制压缩文件，占用空间小，便于传输；
     - 主进程会fork()一个子进程来处理所有保存工作，主进程不需要进行任何磁盘IO操作（主进程不能太大，不然复制过程中主进程阻塞）

   - ##### 缺点：

     - 不保证数据完整性，会丢失最后一次快照**以后更改**的所有数据
     - bgsave 每次运行都要执行fork 操作创建子进程，频繁执行成本过高。

- #### AOF：

  ##### AOF存储内容：

  存储Redis 将所有对数据库进行过写入的命令（及其参数）（RESP）记录， 以此达到记录数据库状态的目的。（当Redis重启后只要按顺序回放这些命令就会恢复到原始状态了）

  ##### AOF的相关配置：

  ```c
  # 可以通过修改Redis.conf配置文件中的appendonly参数开启
  appendonly yes
  # AOF文件的保存位置和RDB文件的位置相同，都是通过dir参数设置的。
  dir ./
  # 默认的文件名是appendonly.aof，可以通过appendfilename参数修改
  appendfilename appendonly.aof
  ```

  ##### AOF执行流程：

  ![image-20210219221229713](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219221229713.png)

  AOF执行流程分为两个部分：AOF主流程和AOF文件重写（缩小文件体积）流程

  ##### AOF主流程：

  AOF文件中存储的是Redis的命令，同步命令到 AOF 文件的整个过程可以分为三个阶段：

1. ##### 命令传播：

   Redis 将**执行完**的命令、命令的参数、命令的参数个数等信息发送到 AOF 程序中。

2. ##### 缓存追加：

   AOF将接收的命令转换为网络通讯协议的格式，然后将协议内容追加到服务器的AOF缓存中。

3. ##### 文件写入和保存：

   AOF 缓存中的内容被写入到 AOF 文件末尾，如果设定的 AOF 保存条件被满足的话， fsync 函数或者 fdatasync 函数会被调用，将写入的内容真正地保存到磁盘中。

   > Redis 目前支持三种 AOF 保存模式：
   >
   > 1. AOF_FSYNC_NO ：不保存。 
   > 2. AOF_FSYNC_EVERYSEC ：每一秒钟保存一次。（默认） 
   > 3. AOF_FSYNC_ALWAYS ：每执行一个命令保存一次。（不推荐）
   
   ##### AOF文件重写（缩小文件体积）流程：
   
   除了以上三个阶段，aof还需要执行fork创建子进程，子进程负责重写来缩小文件体积：
   
   > 重写举例：
   >
   > ​	优化前：
   >
   > ​		set s1 11 
   >
   > ​		set s1 22 
   >
   > ​		set s1 33 
   >
   > ​	优化后：
   >
   > ​		set s1 33
   >
   > ​	优化前：
   >
   > ​		lpush list1 1 2 3 
   >
   > ​		lpush list1 4 5 6 
   >
   > ​	优化后：
   >
   > ​		lpush list1 1 2 3 4 5 6	
   
   重写时为防止主进程有新命令，开辟了aof重写缓存，主进程开辟子进程后产生的新命令会备份一份到aof重写缓存。
   
   ##### aof重写触发方式：
   
   在Redis.conf中配置
   
   ```c
   # 表示当前aof文件大小超过上一次aof文件大小的百分之多少的时候会进行重写。如果之前没有重写过，以
   启动时aof文件大小为准
   auto-aof-rewrite-percentage 100
   # 限制允许重写最小aof文件大小，也就是文件大小小于64mb的时候，不需要进行优化
   auto-aof-rewrite-min-size 64mb
   ```
   
   #### 混合持久化：
   
   Redis 4.0 开始支持 rdb 和 aof 的混合持久化。如果把混合持久化打开，aof rewrite 的时候就直接把 rdb 的内容写到 aof 文件开头。
   
   ##### 开启混合持久化：
   
   ```c
   aof-use-rdb-preamble yes
   ```
   
   ![image-20210219223939233](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219223939233.png)
   
   我们可以看到该AOF文件是rdb文件的头和aof格式的内容，在加载时，首先会识别AOF文件是否以 Redis字符串开头，如果是就按RDB格式加载，加载完RDB后继续按AOF格式加载剩余部分。
   
   ##### AOF文件的载入与数据还原过程：
   
   如下图所示，Redis服务端创建伪客户端从AOF文件循环读取命令并执行，知道所有命令执行完毕。
   
   ##### 为什么要创建伪客户端？
   
   **答：**创建无网络连接的伪客户端是为了达到和带网络连接的客户端执行命令的效果完全一样。
   
   ![image-20210219224011506](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210219224011506.png)

#### RDB与AOF对比：

1. RDB存某个时刻的**数据快照**，采用**二进制压缩**存储，AOF存**操作命令**，采用**文本**存储。

2. RDB性能**高**、AOF性能较**低**。

   ##### AOF性能低的原因：

   为了保证文件的体积，需要开辟子进程重写，自然比直接快照要性能差。

   不过主进程过大时RDB效率没有AOF高，因为AOF可以设置每秒保存一次。

3. RDB会丢失最后一次快照**以后更改**的所有数据，AOF保存数据比较完整。

4. Redis以主服务器模式运行，RDB不会保存过期键值对数据，Redis以从服务器模式运行，RDB会保 存过期键值对，当主服务器向从服务器同步时，再清空过期键值对。

   而AOF执行重写时，会忽略过期key和del命令。

#### RDB与AOF的应用场景：

**内存数据库：**使用rdb+aof ，数据不容易丢

**缓存服务器：**rdb,性能高

**数据还原时：**有rdb+aof 则还原aof，因为RDB会造成文件的丢失，AOF相对数据要完整。

> 拉勾的配置策略：
>
> 追求高性能：都不开 Redis宕机 从数据源恢复 
>
> 字典库 ： 不驱逐，保证数据完整性 
>
> 用作DB 不能主从 数据量小 
>
> 做缓存较高性能： 开rdb ，一般不开aof

### Redis扩展功能：

- #### 发布与订阅：

  ##### 发布与订阅流程：

1. Redis客户端1订阅频道1和频道2

   ```c
   127.0.0.1:6379> subscribe ch1 ch2
   Reading messages... (press Ctrl-C to quit)
   1) "subscribe"
   2) "ch1"
   3) (integer) 1
   1) "subscribe"
   2) "ch2"
   3) (integer) 2
   ```

2. Redis客户端2将消息发布在频道1和频道2上

   ```c
   127.0.0.1:6379> publish ch1 hello
   (integer) 1
   127.0.0.1:6379> publish ch2 world
   (integer) 1
   ```

   Redis客户端1接收到频道1和频道2的消息

   ```c
   1) "message"
   2) "ch1"
   3) "hello"
   1) "message"
   2) "ch2"
   3) "world"
   ```
   
   ##### unsubscribe：退订 channel
   
   Redis客户端1退订频道1，退订后再发布也没用了
   
   ```c
   127.0.0.1:6379> unsubscribe ch1
   1) "unsubscribe"
   2) "ch1"
   3) (integer) 0
   ```
   
   ##### psubscribe ：模式匹配 psubscribe +模式
   
   Redis客户端1订阅所有以ch开头的频道
   
   ```c
   127.0.0.1:6379> psubscribe ch*
   Reading messages... (press Ctrl-C to quit)
   1) "psubscribe"
   2) "ch*"
   3) (integer) 1
   ```
   
   ##### punsubscribe：退订模式匹配
   
   退订所有以ch开头的频道
   
   ```c
   127.0.0.1:6379> punsubscribe ch*
   1) "punsubscribe"
   2) "ch*"
   3) (integer) 0
   ```
   
   ##### 发布与订阅使用场景：
   
   哨兵模式，Redisson框架使用。
   
   > 在Redis哨兵模式中，哨兵通过发布与订阅的方式与Redis主服务器和Redis从服务器进行通信。
   >
   > Redisson是一个分布式锁框架，在Redisson分布式锁释放的时候，是使用发布与订阅的方式通知的

#### Redis事务：

> 事务其实就是批量执行命令,Lua脚本也可以批量执行

- ##### Redis事务执行命令：


1. multi：将后续的命令逐个放入队列中，然后使用exec原子化地执行这个命令队列
2. exec：执行命令队列 
3. discard：清除命令队列 
4. watch：监视key 
5. unwatch：清除监视key

- ##### Redis事务执行命令示例：

  multi：

  ```c
  127.0.0.1:6379> multi #运行该命令后续的命令会放入该队列
  OK
  127.0.0.1:6379> set s1 222
  QUEUED
  127.0.0.1:6379> set s2 333
  QUEUED
  ```

  exec：

  ```c
  127.0.0.1:6379> exec #cli1执行该命令后cli2才能get到刚刚set的s1、s2
  1) OK
  2) (integer) 2
  ```

  discard：

  ```c
  127.0.0.1:6379> discard #清空后再执行会报错
  OK
  127.0.0.1:6379> exec
  (error) ERR EXEC without MULTI
  ```

  watch：

  ```c
  cli1>watch name:1 #当监听的name:1没有exec之前被name:2修改，发生版本变化，再执行exec命令会报错
  OK
  cli1>multi
  OK
  cli2>set name:1 222
  QUEUED
  cli1>set name:4 444
  QUEUED
  cli1>exec
  (nil)
  ```

  

- ##### Redis不支持事务回滚（为什么呢） 

  1、大多数事务失败是因为语法错误或者类型错误，这两种错误，在开发阶段都是可以预见的 ，Redis为了性能方面就忽略了事务回滚。 （回滚记录历史版本）。

  另一种批量执行命令：Lua脚本

#### Lua脚本:

> lua是一种轻量小巧的脚本语言，用标准C语言编写并以源代码形式开放， 其设计目的是为了嵌入应用程序中，从而为应用程序提供灵活的扩展和定制功能。

从Redis2.6.0版本开始，通过**内置**的lua编译/解释器，可以使用EVAL命令对lua脚本进行求值

- ##### Lua脚本使用:
  - ##### EVAL命令：

    - **script参数：**是一段Lua脚本程序，它会被运行在Redis服务器上下文中，这段脚本不必(也不应该) 定义为一个Lua函数。 

    - **numkeys参数：**用于指定键名参数的个数。 

    - **key [key ...]参数：** 从EVAL的第三个参数开始算起，使用了numkeys个键（key），表示在脚本中 所用到的那些Redis键(key)，这些键名参数可以在Lua中通过全局变量KEYS数组，用1为基址的形 式访问( KEYS[1] ， KEYS[2] ，以此类推)。 

    - **arg [arg ...]参数：**可以在Lua中通过全局变量ARGV数组访问，访问的形式和KEYS变量类似( ARGV[1] 、 ARGV[2] ，诸如此类)。

      格式：

      ```c
      EVAL script numkeys key [key ...] arg [arg ...]
      ```

      实际命令：

      ```c
      eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
      ```

      该命令意思为创建key为key1、key2，对应value为first、second的值。

      > 除了上面的还可以使用lua脚本中调用Redis命令
      >
      > Redis.call()： 返回值就是Redis命令执行的返回值 如果出错，则返回错误信息，不继续执行 Redis.pcall()： 返回值就是Redis命令执行的返回值 如果出错，则记录错误信息，继续执行 注意事项 在脚本中，使用return语句将返回值返回给客户端，如果没有return，则返回nil
      >
      > ```c
      > eval "return Redis.call('set',KEYS[1],ARGV[1])" 1 n1 zhaoyun
      > ```
      >
      > 该命令意思为创建key为n1，对应value为zhaoyun的值

  - ##### SCRIPT命令：

    ![image-20210220201353585](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210220201353585.png)

    如上图所示，比如使用一个SCRIPT命令执行了一个语句，然后会返回一个SHA1摘要（就是c686f3***这段字符串），然后执行EVALSHA就可以重复使用该命令创建不同key。

    > EVALSHA 介绍：
    >
    > EVAL 命令要求你在每次执行脚本的时候都发送一次脚本主体(script body)。 
    >
    > Redis 有一个内部的缓存机制，因此它不会每次都重新编译脚本，不过在很多场合，付出无谓的带宽来 传送脚本主体并不是最佳选择。 
    >
    > 为了减少带宽的消耗， Redis 实现了 EVALSHA 命令，它的作用和 EVAL 一样，都用于对脚本求值，但 它接受的第一个参数不是脚本，而是脚本的 SHA1 校验和(sum)

    ##### SCRIPT其它命令：

    - SCRIPT FLUSH ：清除所有脚本缓存 
    - SCRIPT EXISTS ：根据给定的脚本校验和，检查指定的脚本是否存在于脚本缓存 
    - SCRIPT LOAD ：将一个脚本装入脚本缓存，返回SHA1摘要，但并不立即运行它
    - SCRIPT KILL ：杀死当前正在运行的脚本

  - ##### 脚本管理命令：

    ##### 使用脚本管理命令创建key和value：

    创建一个lua脚本：

    ```c
    vim test.lua #创建test.lua脚本
    return Redis.call('set',KEYS[1],ARGV[1]) #将该命令添加到test.lua脚本
    ```

    使用Redis-cli直接执行lua脚本：

    ```c
    ./Redis-cli -h 127.0.0.1 -p 6379 --eval test.lua name:6 , 'caocao' 
    #，的两边要有空格，不然执行错误
    #跑完该命令Redis创建了一个key为name:6，value为caocao的元素
    ```

    ##### 使用脚本管理命令返回Redis的list集合：

    创建一个lua脚本：

    ```c
    local key=KEYS[1]
    local list=Redis.call("lrange",key,0,-1);
    return list;               #将该命令添加到test.lua脚本
    ```

    使用Redis-cli直接执行lua脚本：

    ```c
    ./Redis-cli --eval list.lua list #跑完该命令返回key为lrange的list集合
    ```

  

#### Lua脚本复制：

当执行lua脚本时，Redis 服务器有两种模式：脚本传播模式和命令传播模式（之所以要传播是为了将命令复制到AOF文件和从服务器中）。

- ##### 脚本传播模式（Redis 默认的模式）：

  之前的SCRIPT、EVAL命令执行时就会使用该传播模式

  **注意：**在这一模式下执行的脚本不能有时间、内部状态、随机函数(spop)等。执行相同的脚本以及参数 必须产生相同的效果。在Redis5，也是处于同一个事务中。

- ##### 命令传播模式：

  在命令加Redis.replicate_commands()就会使用该模式，比如：

  ```c
  eval "Redis.replicate_commands();Redis.call('set',KEYS[1],ARGV[1]);Redis.call('set',KEYS[2],ARGV[2])" 2 n1 n2 zhaoyun11 zhaoyun22
  ```

  ##### 脚本传播模式和命令传播模式的区别？

  **答：**有以下两个区别：

     				1. 命令传播模式执行命令需要加Redis.replicate_commands()参数；
                          				2. 命令传播模式，会将执行脚本产生的所有写命令使用事务包裹起来再复制，而脚本传播模式直接复制。
  
  ##### 管道（pipeline）,事务和脚本(lua)三者的区别？
  
  **答：**三者都可以批量执行命令 
  
  - 管道无原子性，命令都是独立的，属于无状态的操作 
  - 事务和脚本是有原子性的，其区别在于脚本可借助Lua语言可在服务器端存储的便利性定制和简化操作 。
  - **脚本的原子性要强于事务**，脚本执行期间，**另外的客户端其它任何脚本或者命令都无法执行**（这点事务无法做到，事务中途可以加命令，会导致报错，比如之前的watch），脚本的执行时间应该尽量短，不能太耗时的脚本。

### Redis慢查询：

我们都知道MySQL有慢查询日志 

Redis也有慢查询日志，可用于监视和优化查询

- #### 慢查询设置：

  在Redis.conf中可以配置和慢查询日志相关的选项：

  ```java
  #执行时间超过多少微秒的命令请求会被记录到日志上 0 :全记录 <0 不记录
  slowlog-log-slower-than 10000
  #slowlog-max-len 存储慢查询日志条数
  slowlog-max-len 128
  ```

  Redis使用列表存储慢查询日志，采用**队列**方式（FIFO）

  ##### 使用命令临时配置慢查询条件：

  > config set的方式可以临时设置，Redis重启后就无效 
  >
  > config set slowlog-log-slower-than 微秒 
  >
  > config set slowlog-max-len 条数

  ```c
  127.0.0.1:6379> config set slowlog-log-slower-than 0 #超过0秒都为慢查询
  OK
  127.0.0.1:6379> config set slowlog-max-len 2 #最多只能存储2条慢查询日志
  OK
  127.0.0.1:6379> set name:001 zhaoyun
  OK
  127.0.0.1:6379> set name:002 zhangfei
  OK
  127.0.0.1:6379> get name:002
  "zhangfei"
  127.0.0.1:6379> slowlog get  #获取慢查询日志命令
  1) 1) (integer) 7 		#日志的唯一标识符(uid)
  2) (integer) 1589774302 #命令执行时的UNIX时间戳
  3) (integer) 65 		#命令执行的时长(微秒)
  4) 1) "get" 			#执行命令及参数
  2) "name:002"
  5) "127.0.0.1:37277"
  6) ""
  2) 1) (integer) 6
  2) (integer) 1589774281
  3) (integer) 7
  4) 1) "set"
  2) "name:002"
  3) "zhangfei"
  5) "127.0.0.1:37277"
  6) ""
  # 由于空间不够第二条和第三条都记录，第一条被移除了。
  ```

- #### 慢查询定位&处理：

  1、key和value尽量精简，能使用int就int。 

  2、减少大key的存取，打散为小key 

  3、避免使用keys *、hgetall等全量操作。 

  4、如果主进程过大将rdb改为aof模式（aof可以设置为每秒保存，这方面效率比rdb高） 

  ​	rdb fork 子进程 主进程阻塞 Redis大幅下降 

  ​	关闭持久化 （这种只适合于数据量较小，有固定数据源，比如拉钩的职位列表） 

  5、想要一次添加多条数据的时候可以使用管道 

  6、尽可能地使用哈希存储 

  7、尽量限制下Redis使用的内存大小，这样可以避免Redis使用swap分区或者出现OOM错误 内存与硬盘的swap

### Redis监控器：

Redis的监控器每当Redis cli发送命令给Redis server，都会给监视器MONITOR发一份

举例：

- 输入开启监听器命令：

  ```c
  127.0.0.1:6379> monitor
  OK
  ```

- Redis客户端输入命令：

  ```c
  127.0.0.1:6379> set name:10 zhaoyun
  OK
  ```

- 监听器收到命令：

  ```c
  127.0.0.1:6379> monitor
  OK
  1589706145.763523 [0 127.0.0.1:42907] "set" "name:10" "zhaoyun"
  ```

一般Redis监听器会使用平台监控：

grafana、prometheus以及Redis_exporter。

Grafana 是一个开箱即用的可视化工具，具有功能齐全的度量仪表盘和图形编辑器，有灵活丰富的图形 化选项，可以混合多种风格，支持多个数据源特点。 

Prometheus是一个开源的服务监控系统，它通过HTTP协议从远程的机器收集数据并存储在本地的时序 数据库上。 

Redis_exporter为Prometheus提供了Redis指标的导出，配合Prometheus以及grafana进行可视化及监 控。

![image-20210220211539566](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210220211539566.png)

## 第五讲：

### Redis主从复制配置：

主Redis无需配置，从Redis修改从服务器上的 Redis.conf 文件：

```xml
# slaveof <masterip> <masterport>
# 表示当前【从服务器】对应的【主服务器】的IP是127.0.0.1，端口是6379。
replicaof 127.0.0.1 6379
```

### Redis主从复制作用：

- **读写分离：**主负责写，从负责读，提升Redis的性能和吞吐量（但是主从的数据一致性问题）

- **数据容灾：**从机是主机的备份，主机宕机，利用哨兵可以实现主从切换，做到高可用。

### Redis主从复制保证最终一致性：

​	Redis主从复制是单向、异步的，主节点只负责将要复制的数据发出，至于从节点是否有接收或者同步完成，主节点不关心。

### Redis主从复制过程：

![image-20210217173205592](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217173205592.png)

- ##### 保存主库信息：

  当客户端向从服务器发送slaveof(replicaof) 主机地址（127.0.0.1） 端口（6379）时：从服务器将主机 ip（127.0.0.1）和端口（6379）保存到RedisServer的masterhost和masterport中。

  ```c
  Struct RedisServer{
  	char *masterhost;//主服务器ip
  	int masterport;//主服务器端口
  } ;
  ```

   从服务器将向发送SLAVEOF命令的客户端返回OK，表示复制指令已经被接收，而实际上复制工作是在 OK返回之后进行。

- ##### 建立socket连接：

  从服务器执行命令（slaveof 127.0.0.1 6379）：

  ![我们在](https://upload-images.jianshu.io/upload_images/1446087-c720e3b085b69053?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

  主服务器查询状态（info Replication）：

  ![在这里插入图片描述](https://upload-images.jianshu.io/upload_images/1446087-0a2a2e70600d8be5?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

  如上图，主服务器显示有一个slave。

- ##### 发送ping命令：

  Slaver向Master发送ping命令，发送ping命令进行首次请求，目的是：**检查socket连接是否可用**，以及主节点当前是否能够处理请求。

  Master的响应： 

  1、发送“pong” , 说明正常 

  2、返回错误，说明Master不正常 

  3、timeout，说明网络超时

![在这里插入图片描述](https://upload-images.jianshu.io/upload_images/1446087-81d4122403b825e2?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- ##### 权限验证：

  如下图所示，主从正常连接后，进行权限验证。

  主未设置密码（requirepass=“”） ，从也不用设置密码（masterauth=“”） 

  主设置密码(requirepass!=""),从需要设置密码(masterauth=主的requirepass的值) 

  或者从通过auth命令向主发送密码

  ![image-20210217172819050](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210217172819050.png)

- ##### 同步数据集：

  ##### Redis 2.8以前的同步操作步骤：

  1. 通过从服务器发送到SYNC命令给主服务器

  2. 主服务器生成RDB文件并发送给从服务器，同时发送保存所有写命令给从服务器

  3. 从服务器**清空**之前数据并执行解释RDB文件 

  4. 主服务器在上面三个步骤之后又有新的写命令会写到缓冲区，将这些新的写命令同步到从服务器执行

     ![image-20210228211738837](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228211738837.png)

  

  ##### Redis 2.8以后的同步操作步骤：

  这个版本的同步分为**全量同步**和**增量同步**：

  - 只有从机第一次连接上主机是全量同步。 

  - 断线重连有可能触发全量同步也有可能是增量同步（ master 判断 runid 是否一致）。

    > runid 和offset在复制过程的变化：
    >
    > - runid ：Redis 服务器只要重启runid就会变化
    > - offset：Redis 服务器执行写命令就会增加偏移量（增加的是执行写命令的字节）

  - 除此之外的情况都是增量同步。

    ##### 全量同步步骤：

    和Redis 2.8以前的同步操作差不多，只不过没有从服务器发送到SYNC命令给主服务器的步骤，后面的步骤是一样的：

    ![image-20210228212746241](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228212746241.png)

    ##### 增量同步步骤：

    - Redis增量同步主要指Slave完成初始化后开始正常工作时， Master 发生的写操作同步到 Slave 的过程。 
    - 通常情况下， Master **每执行一个写命令就会向 Slave 发送相同的写命令**，然后 Slave 接收并执行。

### Redis的心跳检测：

在命令传播阶段，从服务器默认会以每秒一次的频率向主服务器发送命令：

```yml
replconf ack <replication_offset>
#ack :应答
#replication_offset：从服务器当前的复制偏移量
```

- #### 心跳检测的作用：


1. ##### 检测主从的连接状态

2. ##### 辅助实现min-slaves

   主服务器上有两个配置：

   min-slaves-to-write 3 （min-replicas-to-write 3 ） //从服务器的数量少于3个

   min-slaves-max-lag 10 （min-replicas-max-lag 10）//3个从服务器的延迟都不能大于或等于10秒

   这两个有其中一个命中主服务器将**拒绝执行写命令**。

3. ##### 检测命令丢失

   由上面可知，从服务器发送心跳除了ack还会把偏移量（offset）也给主服务器，如果因为网络故障，写命令半路丢失，那么发送心跳时主服务器就能感知到从服务器的**偏移量有问题**，这时主服务器在**复制积压缓冲区**里面找到从服务器**缺少**的数据，并将这些数据重新发送给从服务器。

   > 计算缺少数据的例子：
   >
   > 举个例子，假设有两个处于一致状态的主从服务器，它们的复制偏移量都是200，如下图所示
   >
   > ![image-20210228215548434](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228215548434.png)
   >
   > 如果这时主服务器执行了命令SET key value（协议格式的长度为33字节），将自己的复 制偏移量更新到了233，并尝试向从服务器传播命令SET key value，但这条命令却因为网络 故障而在传播的途中丢失，那么主从服务器之间的复制偏移量就会出现不一致，主服务器的 复制偏移量会被更新为233，而从服务器的复制偏移量仍然为200，如下图所示
   >
   > ![image-20210228215605316](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228215605316.png)
   >
   > 在这之后，当从服务器向主服务器发送REPLCONF ACK命令的时候，主服务器会察觉 从服务器的复制偏移量依然为200，而自己的复制偏移量为233，这说明复制积压缓冲区里面 复制偏移量为201至233的数据（也即是命令SET key value）在传播过程中丢失了，于是主服 务器会再次向从服务器传播命令SET key value，从服务器通过接收并执行这个命令可以将自 己更新至主服务器当前所处的状态，如下图所示
   >
   > ![image-20210228215615833](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228215615833.png)

### 检测命令丢失和增量同步的区别？？？？

**答：**补发缺失数据操作在主从服务器**没有断线**的情况下执行，而增量同步操作则在主从服务器**断线并重连**之后执行。

### Redis哨兵模式：

Redis哨兵介绍：

哨兵（sentinel -[ˈsentɪnl] ）是Redis的高可用性(High Availability)的解决方案： 

由一个或多个sentinel实例组成sentinel集群可以监视一个或多个主服务器和多个从服务器。 

当主服务器进入下线状态时，sentinel可以将该主服务器下的某一从服务器升级为主服务器继续提供服务，从而保证redis的高可用性。

![image-20210228220452959](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228220452959.png)

#### sentinel 配置：

```yml
daemonize yes //开启守护线程（redis将一直运行，除非手动kill该进程）
# 哨兵sentinel监控的redis主节点的 ip port
# master-name 可以自己命名的主节点名字 只能由字母A-z、数字0-9 、这三个字符".-_"组成。
# quorum 当这些quorum个数sentinel哨兵认为master主节点失联 那么这时 客观上认为主节点失联了
# sentinel monitor <master-name> <ip> <redis-port> <quorum>
sentinel monitor mymaster 127.0.0.1 6379 2
# 当在Redis实例中开启了requirepass foobared 授权密码 这样所有连接Redis实例的客户端都要提
供密码
# 设置哨兵sentinel 连接主从的密码 注意必须为主从设置一样的验证密码
# sentinel auth-pass <master-name> <password>
sentinel auth-pass mymaster MySUPER--secret-0123passw0rd
# 指定多少毫秒之后 主节点没有应答哨兵sentinel 此时 哨兵主观上认为主节点下线 默认30秒，改成3秒
# sentinel down-after-milliseconds <master-name> <milliseconds>
sentinel down-after-milliseconds mymaster 3000
# 这个配置项指定了在发生failover主备切换时最多可以有多少个slave同时对新的master进行同步，这个数字越小，完成failover所需的时间就越长，但是如果这个数字越大，就意味着越 多的slave因为replication而不可用。可以通过将这个值设为 1 来保证每次只有一个slave 处于不能处理命令请求的状态。
# sentinel parallel-syncs <master-name> <numslaves>
sentinel parallel-syncs mymaster 1
# 故障转移的超实时间 failover-timeout 可以用在以下这些方面：
#1. 同一个sentinel对同一个master两次failover之间的间隔时间。
#2. 当一个slave从一个错误的master那里同步数据开始计算时间。直到slave被纠正为向正确的master那里同步数据时。
#3.当想要取消一个正在进行的failover所需要的时间。
#4.当进行failover时，配置所有slaves指向新的master所需的最大时间。不过，即使过了这个超时，slaves依然会被正确配置为指向master，但是就不按parallel-syncs所配置的规则来了
# 默认三分钟
# sentinel failover-timeout <master-name> <milliseconds>
sentinel failover-timeout mymaster 180000
```

### sentinel的执行原理：

- #### 启动并初始化Sentinel：

  Sentinel实例启动后，每个Sentinel会创建2个连向主服务器的网络连接

  - 命令连接：用于向主服务器发送命令，并接收响应； 

  - 订阅连接：用于订阅主服务器的—sentinel—:hello频道。

    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228221931010.png" alt="image-20210228221931010" style="zoom:50%;" />

- #### 获取主服务器信息：

  Sentinel默认每**10s**一次，向被监控的主服务器发送info命令，获取**主服务器和其下属从服务器**的信息。

- #### 获取从服务器信息：

  当Sentinel发现主服务器有新的从服务器出现时，Sentinel还会向从服务器建立命令连接和订阅连接。 在命令连接建立之后，Sentinel还是默认10s一次，向从服务器发送info命令，并记录从服务器的信息。

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228222249387.png" alt="image-20210228222249387" style="zoom:50%;" />

- #### 向主服务器和从服务器发送消息(以订阅的方式)：

  默认情况下，Sentinel每2s一次，向所有被监视的主服务器和从服务器所订阅的—sentinel—:hello频道 上发送消息，消息中会携带Sentinel自身的信息和主服务器的信息。

- #### 接收来自主服务器和从服务器的频道信息：

  **Sentinel彼此之间只创建命令连接，而不创建订阅连接**，因为Sentinel通过订阅主服务器或从服务器， 就可以感知到新的Sentinel的加入，而一旦新Sentinel加入后，相互感知的Sentinel通过命令连接来通信就可以了。

- #### sentinel检测服务器下线步骤：

  分为主观下线和客观下线：

  - ##### 检测主观下线状态：

    Sentinel每秒一次向所有与它建立了命令连接的实例(主服务器、从服务器和其他Sentinel)发送PING命令 

    实例在down-after-milliseconds毫秒内返回无效回复(除了+PONG、-LOADING、-MASTERDOWN外) 

    实例在down-after-milliseconds毫秒内无回复（超时） 

    Sentinel就会认为该实例主观下线(SDown)

  - ##### 检查客观下线状态：

    当一个Sentinel将一个主服务器判断为主观下线后 

    Sentinel会向同时监控这个主服务器的所有其他Sentinel发送查询命令

    超过的quorum数量的Sentinel都判断主服务器为主观下线，则该主服务器就会被判定为客观下线(ODown)。

- #### sentinel的leader选举流程：

  Raft算法follower变为leader过程：

  ![image-20210228224616742](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210228224616742.png)

  底层使用Raft算法（参考rpc的笔记），具体流程如下：

  1. 某Sentinel认定master客观下线后，该Sentinel会先看看自己有没有投过票，如果自己已经投过票给其他Sentinel了，在2倍故障转移的超实时间自己就不会成为Leader。相当于它是一个Follower。
  2. 如果该Sentinel还没投过票，那么它就成为Candidate。
  3. Sentinel成为Candidate需要完成几件事情：
     - 更新故障转移状态为start 
     - 当前epoch加1，相当于进入一个新term，在Sentinel中epoch就是Raft协议中的term。 
     - 向其他节点发送 is-master-down-by-addr 命令请求投票。命令会带上自己的epoch。
     -  给自己投一票（leader、leader_epoch）
  4. 当其它哨兵收到此命令时，可以同意或者拒绝它成为领导者；（通过判断epoch）
  5. Candidate会不断的统计自己的票数，直到他发现认同他成为Leader的票数超过一半而且超过它配 置的quorum，这时它就成为了Leader。
  6. 其他Sentinel等待Leader从slave选出master后，**检测到新的master正常工作后，就会去掉客观下线的标识**。

##### sentinel的leader选举的触发条件？

**答：**当redis集群选举一个节点为主节点，首先需要从Sentinel集群中选举一个Sentinel节点作为Leader。

- ####  Sentinel会对下线的主服务器执行故障转移：

  主要有三个步 骤：

  1. 它会将失效 Master 的其中一个 Slave 升级为新的 Master , 并让失效 Master 的其他 Slave 改为复制新的 Master ； 
  2. 当客户端试图连接失效的 Master 时，集群也会向客户端返回新 Master 的地址，使得集群可以使用现在的 Master 替换失效 Master 。 
  3. Master 和 Slave 服务器切换后， Master 的 redis.conf 、 Slave 的 redis.conf 和 sentinel.conf 的配置文件的内容都会发生相应的改变，即， Master 主服务器的 redis.conf 配置文件中会多一行 replicaof 的配置， sentinel.conf 的监控目标会随之调换。

- #### Sentinel主服务器的选择：

  主要有以下规则：

  1. 过滤掉主观下线的节点
  2.  选择slave-priority最高的节点，如果有则返回没有就继续选择（slave-priority：优先级）
  3. 选择出复制偏移量最大的节点，因为复制偏移量越大则数据复制的越完整，如果由就返回了，没有就继续
  4.  选择run_id最小的节点，因为run_id越小说明重启次数越少

### Redis分区：

分区是将数据分布在多个Redis实例（Redis主机）上，以至于每个实例只包含一部分数据。

#### 分区的意义：

- 性能的提升：

  单机Redis的网络I/O能力和计算资源是有限的，将请求分散到多台机器，充分利用多台机器的计算能力可网络带宽，有助于提高Redis总体的服务能力。

- 存储能力的横向扩展：

  即使Redis的服务能力能够满足应用需求，但是随着存储数据的增加，单台机器受限于机器本身的存储 容量，将数据分散到多台机器上存储使得Redis服务可以横向扩展。

#### 分区的方式：

根据分区键（id）进行分区：

- ##### 范围分区：

  根据id数字的范围比如1--10000、100001--20000.....90001-100000，每个范围分到不同的Redis实例中。

  优点：

  - 实现简单，方便迁移和扩展

  缺点：

  - 热点数据分布不均，性能损失
  - 非数字型key，比如uuid无法使用(可采用雪花算法替代)

- ##### hash分区：

  利用简单的hash算法即可： Redis实例=hash(key)%N，

  key:要进行分区的键，比如user_id 

  N:Redis实例个数(Redis主机)

  优点： 

  - 支持任何类型的key 
  - 热点分布较均匀，性能较好

  缺点：

  - 扩容时，迁移复杂，需要重新计算，扩展较差（利用一致性hash环）

### 分区的逻辑位置存放有两种方式：

- #### client端分区：

  如下图，client自己根据算法选择redis服务器

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301005244445.png" alt="image-20210301005244445" style="zoom:50%;" />

  ##### 缺点：

  - **复杂度高：**客户端需要自己处理数据路由、高可用、故障转移等问题。
  - **不易扩展：**一旦**节点的增或者删操作**，都会导致key无法在redis中命中，必须**重新根据节点计算**，并手动迁移全部或部分数据。

- #### proxy端分区：

  如下图所示，client选择redis服务器都要经过代理，而代理（proxy）就是存放分区算法的地方。

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301005603683.png" alt="image-20210301005603683" style="zoom:50%;" />

### client端分区方案-官方cluster分区讲解：

Redis3.0之后，Redis官方提供了完整的集群解决方案。

方案采用去中心化的方式，包括：sharding（分区）、replication（复制）、failover（故障转移）。 称为RedisCluster。

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301231900393.png" alt="image-20210301231900393" style="zoom:75%;" />

- #### 去中心化：

  RedisCluster由多个Redis节点组构成，是一个P2P无中心节点的集群架构，依靠Gossip协议传播的集群。

- #### Gossip协议：

  Gossip协议基本思想：

  一个节点周期性(每秒)随机选择一些节点，并把信息传递给这些节点，然后这些收到信息的节点接下来会做同样的事情（类似于病毒的思想，不断扩散）。

  gossip协议包含多种消息，包括meet、ping、pong、fail、publish等等。

  ![image-20210301232121202](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301232121202.png)

- #### slot：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301232418075.png" alt="image-20210301232418075" style="zoom:70%;" />

  就是上面说的hash槽，slot槽必须在节点上连续分配，如果出现不连续的情况，则RedisCluster不能工作，详见容错。

#### RedisCluster的优势：

- 高性能

   Redis Cluster 的性能与单节点部署是同级别的。 

  多主节点、负载均衡、读写分离。

- 高可用

  Redis Cluster 支持标准的主从复制配置来保障高可用和高可靠。 

  failover 

  Redis Cluster 也实现了一个类似 Raft 的共识方式，来保障整个集群的可用性。

- 易扩展

  向 Redis Cluster 中添加新节点，或者移除节点，都是透明的，不需要停机。

  水平、垂直方向都非常容易扩展。 

  数据分区，海量数据，数据存储

- 原生

  部署 Redis Cluster 不需要其他的代理或者工具，而且 Redis Cluster 和单机 Redis 几乎完全兼 容。

#### RedisCluster的moved重定向和ask重定向：

- #### moved重定向：

  **moved重定向过程：**（其实就是先分配到任意槽，如果计算发现不在该槽范围内，那么给客户端发送moved异常，客户端从moved异常中获取目标节点的信息然后向目标节点发送命令，获取命令执行结果）

  1. 每个节点通过通信都会共享Redis Cluster中槽和集群中对应节点的关系 

  2. 客户端向Redis Cluster的任意节点发送命令，接收命令的节点会根据CRC16规则进行hash运算与 16384取余，计算自己的槽和对应节点 

  3. 如果保存数据的槽被分配给当前节点，则去槽中执行命令，并把命令执行结果返回给客户端 

  4. 如果保存数据的槽不在当前节点的管理范围内，则向客户端返回moved重定向异常

  5. 客户端接收到节点返回的结果，如果是moved异常，则从moved异常中获取目标节点的信息 

  6. 客户端向目标节点发送命令，获取命令执行结果

     <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301233231475.png" alt="image-20210301233231475" style="zoom:60%;" />

- #### ask重定向：

  针对在对集群进行扩容和缩容时，需要对槽及槽中数据进行迁移

  ##### ask重定向过程：

  1. 客户端向目标节点发送命令，目标节点中的槽已经迁移支别的节点上了，此时目标节点会**返回ask**转向给客户端。
  2. 客户端向新的节点发送Asking命令给新的节点，然后再次向新节点发送命令
  3. 新节点执行命令，把命令执行结果返回给客户端

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301233505735.png" alt="image-20210301233505735" style="zoom:50%;" />

##### moved和ask的区别：

- moved：槽已确认转移 
- ask：槽还在转移过程中

#### RedisCluster的智能客户端- JedisCluster：

- JedisCluster是Jedis根据RedisCluster的特性提供的集群智能客户端。
- JedisCluster为每个节点创建连接池，并跟节点建立映射关系缓存（Cluster slots）
- JedisCluster将每个主节点负责的槽位一一与主节点连接池建立映射缓存
- JedisCluster启动时，已经知道key,slot和node之间的关系，可以找到目标节点
- JedisCluster对目标节点发送命令，目标节点直接响应给JedisCluster
- 如果JedisCluster与目标节点连接出错，则JedisCluster会知道连接的节点是一个错误的节点 此时节点返回moved异常给JedisCluster
- JedisCluster会重新初始化slot与node节点的缓存关系，然后向新的目标节点发送命令，目标命令执行 命令并向JedisCluster响应
- 如果命令发送次数超过5次，则抛出异常"Too many cluster redirection!"（也就是最多能有4次重定向）

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301234041472.png" alt="image-20210301234041472" style="zoom:80%;" />

#### RedisCluster数据迁移：

数据迁移场景：

- 新的节点作为master加入
- 某个节点分组需要下线
- 负载不均衡需要调整slot 分布



<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301234420989.png" alt="image-20210301234420989" style="zoom:60%;" />

如上图，A向B数据的过程分为四个步骤：

1. 向节点B**发送状态变更命令**，将B的对应slot 状态置为importing。
2. 向节点A**发送状态变更命令**， 将A对应的slot 状态置为migrating。
3. 向A 发送migrate 命令，告知A将要迁移的slot对应的key 迁移到B。
4. 当所有key 迁移完成后，cluster setslot 重新设置槽位。

#### RedisCluster的容灾（failover）：

- #### 故障检测：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210301235145340.png" alt="image-20210301235145340" style="zoom:50%;" />

  如上图：

  1. 每个节点都彼此发送PING消息，加入A有一段时间没有收到C的PONG回应，那么A就把C标识为pfail。
  2. A在后续发送ping时，会带上B的pfail信息， 通知给其他节点。
  3. 如果B也把C标识为pfail,传入PONG回复给A会带入标识给A，于是C被大于1/2标识为pfail。
  4. A向整个集群 广播，该节点已经下线。
  5. 其他节点收到广播，标记B为fail。

- #### 从节点选举：

  

- #### 变更通知：



## 第六讲：

### Redis性能高的原因：

Redis性能指标：读：110000次/s 写：81000次/s

处理key数量：官方说Redis单例能处理key：2.5亿个 ，一个key或是value大小最大是512M

Redis性能高的原因：

1. redis是基于内存的，内存的读写速度非常快；
2. redis是单线程的，省去了很多上下文切换线程的时间；
3. redis使用多路复用技术，可以处理并发的连接。非阻塞IO 内部实现采用epoll，采用了epoll+自己实现的简单的事件框架。epoll中的读、写、关闭、连接都转化成了事件，然后利用epoll的多路复用特性，绝不在io上浪费一点时间。

### 缓存种类：

包括JVM缓存、文件缓存和Redis缓存等：

- #### JVM缓存：

  JVM缓存就是本地缓存，设计在应用服务器中（tomcat）。 通常可以采用Ehcache和Guava Cache，在互联网应用中，由于要处理高并发，通常选择Guava Cache。

  **适用本地（JVM）缓存的场景：** 1、对性能有非常高的要求。 2、不经常变化 3、占用内存不大 4、有访问整个集合的需求 5、数据允许不实时一致

- #### 文件缓存：

  这里的文件缓存是基于http协议的文件缓存，一般放在nginx中。 因为静态文件（比如css，js， 图片）中，很多都是不经常更新的。nginx使用proxy_cache将用户的请 求缓存到本地一个目录。下一个相同请求可以直接调取缓存文件，就不用去请求服务器了。

  ```properties
  server {
      listen 80 default_server;
      server_name localhost;
      root /mnt/blog/;
      location / {
  	}
  	#要缓存文件的后缀，可以在以下设置
  	location ~ .*\.(gif|jpg|png|css|js)(.*) {
          proxy_pass http://ip地址:90;
          proxy_redirect off;
          proxy_set_header Host $host;
          proxy_cache cache_one;
          proxy_cache_valid 200 302 24h;
          proxy_cache_valid 301 30d;
          proxy_cache_valid any 5m;
          expires 90d;
          add_header wall "hello lagou.";
  	}
  }
  ```

- #### Redis缓存：

  分布式缓存，采用主从+哨兵或RedisCluster的方式缓存数据库的数据。

   在实际开发中 

  作为数据库使用，数据要完整 

  作为缓存使用 

  作为Mybatis的二级缓存使用

### 缓存配置：

- #### 缓存大小设置：

  GuavaCache的缓存设置方式：

  ```java
  CacheBuilder.newBuilder().maximumSize(num) // 超过num会按照LRU算法来移除缓存
  ```

  Nginx的缓存设置方式：

  ```properties
  http {
  	...
  	proxy_cache_path /path/to/cache levels=1:2 keys_zone=my_cache:10m max_size=10g
  	inactive=60m use_temp_path=off;
  	server {
  	proxy_cache mycache;
  	location / {
  	proxy_pass http://localhost:8000;
  	}
    }
  }
  ```

  Redis缓存设置：

  ```yml
  maxmemory=num # 最大缓存量，一般为内存的3/4
  ```

- #### 淘汰策略设置：

  ```yml
  maxmemory-policy=allkeys-lru  #设置缓存淘汰策略
  ```

  缓存淘汰策略的选择：

  1、volatile-lru：只对设置了过期时间的key进行LRU（默认使用该策略） 

  2、allkeys-lru ： 删除lru算法的key  

  3、volatile-random：随机删除即将过期key  

  4、allkeys-random：随机删除  

  5、volatile-ttl ： 删除即将过期的  

  6、noeviction ： 永不过期，返回错误

- #### 查看缓存命中率：

  通过info命令可以监控服务器状态

  ```yml
  127.0.0.1:6379> info
  # Server
  redis_version:5.0.5
  redis_git_sha1:00000000
  redis_git_dirty:0
  redis_build_id:e188a39ce7a16352
  redis_mode:standalone
  os:Linux 3.10.0-229.el7.x86_64 x86_64
  arch_bits:64
  #缓存命中
  keyspace_hits:1000
  #缓存未命中
  keyspace_misses:20
  used_memory:433264648
  expired_keys:1333536
  evicted_keys:1547380
  ```

  如上命令查看命中key为1000，未命中key为20，命中率=1000/（1000+20）=83%

  一个缓存失效机制，和过期时间设计良好的系统，命中率可以做到95%以上。

- #### info命令的各种指标解析：

  ```yml
  connected_clients:68 #连接的客户端数量
  used_memory_rss_human:847.62M #系统给redis分配的内存
  used_memory_peak_human:794.42M #内存使用的峰值大小
  total_connections_received:619104 #服务器已接受的连接请求数量
  instantaneous_ops_per_sec:1159 #服务器每秒钟执行的命令数量 qps
  instantaneous_input_kbps:55.85 #redis网络入口kps
  instantaneous_output_kbps:3553.89 #redis网络出口kps
  rejected_connections:0 #因为最大客户端数量限制而被拒绝的连接请求数量
  expired_keys:0 #因为过期而被自动删除的数据库键数量
  evicted_keys:0 #因为最大内存容量限制而被驱逐（evict）的键数量
  keyspace_hits:0 #查找数据库键成功的次数
  keyspace_misses:0 #查找数据库键失败的次数
  ```

- #### 缓存预热的两种加载缓存思路：

  - 数据量不大，可以在项目启动的时候自动进行加载 
  - 数据量大时，利用定时任务刷新缓存，将数据库的数据刷新到缓存中

### 缓存问题：

- #### 缓存穿透：

  缓存穿透是指在高并发下查询key不存在的数据，会穿过缓存查询数据库。导致数据库压力过大而宕机

  ##### 解决方案：

  1. 对查询结果为空的情况也进行缓存，缓存时间（ttl：生存时间）设置短一点（缓存太多空值占用了更多的空间）

  2. 使用布隆过滤器，在缓存之前在加一层布隆过滤器，在查询的时候先去布隆过滤器查询 key 是否 存在，如果不存在就直接返回，存在再查缓存和DB。

     <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210307175630115.png" alt="image-20210307175630115" style="zoom:50%;" />

     如果一个个遍历性能肯定很差，查询布隆过滤器的key是否存在思想：

     <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210307180545913.png" alt="image-20210307180545913" style="zoom:80%;" />

     如上图：

     - ##### 布隆过滤器放入key过程：

       str1通过几次Hash函数计算分布到数组中并将值设置为1。

     - ##### 布隆过滤器检索key是否存在过程：

       str2也是按几次Hash函数映射到数组的点上看看是不是都是1，如果有任何一个0，则被检元素一定不在；如果都是1，则被检元素**很可能在**（虽然不是一定在，但是能排除一些key，减轻redis和DB压力，且设置Hash函数越多，误差越小）。



- #### 缓存雪崩：

  突然间**大量的key**失效了或redis重启，大量访问数据库，数据库崩溃。

  ##### 解决方案: 

  1、 key的失效期分散开，不同的key设置不同的有效期 

  2、设置二级缓存（要注意出现数据不一致问题） 

  3、使用备机代替失效主机（要注意出现数据不一致问题）

  

- #### 缓存击穿：

  指**某个key**突然被大量请求访问，key如果刚好过期，那么会访问DB加载数据并回设到缓存，这个时候大并发的请求可能会瞬间把后端DB压垮。（和缓存雪崩的区别在于这里针对某一key缓存，前者则是很多key）

  ##### 解决方案：

  1. 用分布式锁控制访问的线程
  2. 不设超实时间，volatile-lru 但会造成写一致问题



- #### 数据不一致：

  为了要保证数据库与redis数据的一致性，redis在分布式环境下追求的是最终一致性，且采用的是延时双删策略。

  - ##### 延时双删介绍：

    1. 先更新数据库同时删除缓存项(key)，等读的时候再填充缓存。
    2. 2秒后再删除一次缓存项(key)。

    如果第二步没有清楚干净还可以采用以下策略删除缓存项；

    1. 设置缓存过期时间 Expired Time 比如 10秒 或1小时
    2. 将缓存删除失败记录到日志中，利用脚本提取失败记录再次删除（缓存失效期过长 7*24）
    3. 通过数据库的binlog来异步淘汰key，利用工具(canal)将binlog日志采集发送到MQ中，然后通过ACK机制确认处理删除缓存。

    ##### 为什么要采用延时双删？

    **答：**因为数据库更新分三步，update数据，然后把缓存删除，再commit操作，如果在commit之前有读取操作，那么会把旧数据刷到缓存，那么后续读的还是旧数据，所有commit后还得再删一次缓存才能确保后续读取的是最新的数据。

- #### 数据并发竞争：

  这里的并发指的是多个redis的client同时set 同一个key引起的并发问题。

  - ##### 第一种方案：分布式锁+时间戳

    这种情况，主要是准备一个分布式锁，大家去抢锁，抢到锁就做set操作。

    时间戳是保证顺序。

  - ##### 第二种方案：利用消息队列

    在并发量过大的情况下,可以通过消息中间件进行处理,把并行读写进行串行化。 把Redis的set操作放在队列中使其串行化,必须的一个一个执行。

- #### Hot Key问题发现和处理：

  ##### 如何发现hot key：

  - 预估hot key 比如秒杀的商品、火爆的新闻等。
  - 在客户端进行统计，实现简单，加一行代码即可 
  - 如果是Proxy，比如Codis，可以在Proxy端收集 
  - 利用Redis自带的命令，monitor、hotkeys。但是执行缓慢（不要用）
  - 利用基于大数据领域的流式计算技术来进行实时数据访问次数的统计，比如 Storm、Spark Streaming、Flink，这些技术都是可以的。发现热点数据后可以写到zookeeper中

  ##### 如何处理热Key：

  - ##### 变分布式缓存为本地缓存 

    发现热key后，把缓存数据取出后，直接加载到本地缓存中。可以采用Ehcache、Guava Cache都可 以，这样系统在访问热key数据时就可以直接访问自己的缓存了。（数据不要求实时一致）

  - ##### 在每个Redis主节点上备份热key数据

    这样在读取时可以采用随机读取的方式，将访问压力负载到 每个Redis上。

  - ##### 利用对热点数据访问的限流熔断保护措施

    每个系统实例每秒最多请求缓存集群读操作不超过 400 次，一超过就可以熔断掉，不让请求缓存集群， 直接返回一个空白信息，然后用户稍后会自行再次重新刷新页面之类的。（首页不行，系统友好性差） 通过系统层自己直接加限流熔断保护措施，可以很好的保护后面的缓存集群。

- #### BIG Key问题发现和处理：

  ##### 如何发现大key：

  - redis-cli --bigkeys命令。可以找到某个实例5种数据类型(String、hash、list、set、zset)的最大
    key。（但如果Redis 的key比较多，执行该命令会比较慢）
  - 获取生产Redis的rdb文件，通过rdbtools分析rdb生成csv文件，再导入MySQL或其他数据库中进行
    分析统计，根据size_in_bytes统计bigkey

  ##### 如何处理大key：

  - String类型的big key，尽量不要存入Redis中，可以使用文档型数据库**MongoDB**或缓存到CDN上。
    如果必须用Redis存储，最好**单独存储**，不要和其他的key一起存储。采用一主一从或多从。

  - 单个简单的key存储的value很大，可以尝试将对象分拆成几个key-value， 使用mget获取值，这样
    分拆的意义在于分拆单次操作的压力，将操作压力平摊到多次操作中，降低对redis的IO影响。

    > mget命令（获取多个key）：
    >
    > ```
    > redis 127.0.0.1:6379> MGET KEY1 KEY2 .. KEYN
    > ```

    ##### 举例：

    hash， set，zset，list 中存储过多的元素，可以将这些元素分拆。（常见）

    ```xml
    以hash类型举例来说，对于field过多的场景，可以根据field进行hash取模，生成一个新的key，例如原
    来的
    hash_key:{filed1:value, filed2:value, filed3:value ...}，可以hash取模后形成如下
    key:value形式
    hash_key:1:{filed1:value}
    hash_key:2:{filed2:value}
    hash_key:3:{filed3:value}
    ...
    取模后，将原先单个key分成多个key，每个key filed个数为原先的1/N
    ```

  - 删除大key时不可以直接删除，会出现阻塞，使用 lazy delete (unlink命令)

    > 该命令会在另一个线程中回收内存，因此它是非阻塞的。 这也是该命令名字的由来：**仅将keys从key空间中删除**，真正的数据删除会在后续异步操作。

    ```yml
    redis> SET key1 "Hello"
    "OK"
    redis> SET key2 "World"
    "OK"
    redis> UNLINK key1 key2 key3
    (integer) 2
    ```

  ### 缓存和数据库一致性：

  - 利用Redis的缓存淘汰策略被动更新 LRU 、LFU

  - 利用TTL被动更新

    ```xml
    TTL介绍：
    
    # 设置key的生存时间 
    redis> EXPIRE key 10086
    (integer) 1
    
    //返回key的剩余过期时间
    redis> TTL key
    (integer) 10084
    ```

  - 在更新数据库时主动更新 （先更数据库再删缓存----延时双删）

  - 异步更新 定时任务 数据不保证实时一致 不穿DB

    ##### 不同策略之间的优缺点：

    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210309000709686.png" alt="image-20210309000709686" style="zoom:75%;" />



**注：**可以使用Redis做Mybatis的二级缓存，在分布式环境下可以使用，详情参考讲义。

### Redis实现分布式锁：

- ##### 利用Watch实现Redis乐观锁：

  使用CAS原理，使用jedis1.watch(redisKey)监控key的变化，Transaction来控制事务，如果执行key自增的事务操作时候，有变化则回滚。

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210310200123275.png" alt="image-20210310200123275" style="zoom:70%;" />

  

- ##### 使用设置key来实现互斥锁：

  ##### 实现原理：

  - 获取锁方法：方法里面设置key和有效期，如果其他线程想设置key成功，必须要等key过期。

    - ##### 使用set命令：

    ```java
    public boolean getLock(String lockKey,String requestId,int expireTime) {
        //NX:保证互斥性
        // hset 原子性操作 只要lockKey有效 则说明有进程在使用分布式锁
        String result = jedis.set(lockKey, requestId, "NX", "EX", expireTime);
        if("OK".equals(result)) {
        	return true;
        }
        return false;
    }
    ```

    - ##### 使用setnx命令：并发会产生问题

    ```java
    public boolean getLock(String lockKey,String requestId,int expireTime) {
        Long result = jedis.setnx(lockKey, requestId);
        if(result == 1) {
            //成功设置 进程down 永久有效 别的进程就无法获得锁
            jedis.expire(lockKey, expireTime);
            return true;
        }
        return false;
    }
    ```

  - 释放锁方法：删除key。

    有两种方式删除key：

    - 方式1（del命令实现） -- 并发

      ```java
      public static void releaseLock(String lockKey,String requestId) {
          if (requestId.equals(jedis.get(lockKey))) {
          	jedis.del(lockKey);
          }
      }
      ```

      方式一缺点：

      调用jedis.del()方法的时候，这把锁已经不属于当前客户端的时候会解除他人加的锁：

      ​	比如客户端A加锁，一段时间之后客户端A解锁，在执行 jedis.del()之前，锁突然过期了，此时客户端B尝试加锁成功，然后客户端A再执行del()方法，则将客户端B的锁给解除了。

    - 方式2（redis+lua脚本实现）--推荐

      ```java
      public static boolean releaseLock(String lockKey, String requestId) {
          String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return
          redis.call('del', KEYS[1]) else return 0 end";
          Object result = jedis.eval(script, Collections.singletonList(lockKey),
          Collections.singletonList(requestId));
          if (result.equals(1L)) {
          	return true;
          }
          return false;
      }
      ```

  ##### 以上两种实现锁存在的问题：

  - 单机 

    无法保证高可用 

  - 主--从 

    无法保证数据的强一致性，在主机宕机时会造成锁的重复获得。

    无法续租，超过expireTime后，不能继续使用

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210310205158936.png" alt="image-20210310205158936" style="zoom:70%;" />

  Redis集群不能保证数据的随时一致性，只能保证数据的最终一致性。 为什么还可以用Redis实现分布式锁？

  **答：**与业务有关，当业务不需要数据强一致性时，比如：社交场景，就可以使用Redis实现分布式锁，当业务必须要数据的强一致性，即不允许重复获得锁，比如金融场景（重复下单，重复转账）就不要使用可以使用CP模型实现，比如：zookeeper和etcd。

  #### 主从切换锁丢失问题？

  使用红锁，Redission就实现了红锁算法，红锁的思想：

  > Redlock思想：
  > 使用N个完全独立，没有主从关系的主redis节点，保证他们大多数情况下不会宕机。
  > 然后对每个主redis节点进行加锁，只有**超过半数**，也就是（N/2 + 1）的主redis节点加锁成功，才算成功，否则一律算失败。
  > 失败的话，还是会到每个主redis节点进行释放锁，**不管之前有没有加锁成功**。

  实现代码：

  ```java
  Config config1 = new Config();
  config1.useSingleServer().setAddress("redis://172.0.0.1:5378").setPassword("a123456").setDatabase(0);
  RedissonClient redissonClient1 = Redisson.create(config1);
  
  Config config2 = new Config();
  config2.useSingleServer().setAddress("redis://172.0.0.1:5379").setPassword("a123456").setDatabase(0);
  RedissonClient redissonClient2 = Redisson.create(config2);
  
  Config config3 = new Config();
  config3.useSingleServer().setAddress("redis://172.0.0.1:5380").setPassword("a123456").setDatabase(0);
  RedissonClient redissonClient3 = Redisson.create(config3);
  
  /**
   * 获取多个 RLock 对象
   */
  RLock lock1 = redissonClient1.getLock(lockKey);
  RLock lock2 = redissonClient2.getLock(lockKey);
  RLock lock3 = redissonClient3.getLock(lockKey);
  
  /**
   * 根据多个 RLock 对象构建 RedissonRedLock （最核心的差别就在这里）
   */
  RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
  
  try {
      /**
       * 4.尝试获取锁
       * waitTimeout 尝试获取锁的最大等待时间，超过这个值，则认为获取锁失败
       * leaseTime   锁的持有时间,超过这个时间锁会自动失效（值应设置为大于业务处理的时间，确保在锁有效期内业务能处理完）
       */
      boolean res = redLock.tryLock((long)waitTimeout, (long)leaseTime, TimeUnit.SECONDS);
      if (res) {
          //成功获得锁，在这里处理业务
      }
  } catch (Exception e) {
      throw new RuntimeException("aquire lock fail");
  }finally{
      //无论如何, 最后都要解锁
      redLock.unlock();
  }
  ```

  ##### 核心源码【redLock.tryLock（）】：

  ```java
  public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
      long newLeaseTime = -1;
      if (leaseTime != -1) {
          newLeaseTime = unit.toMillis(waitTime)*2;
      }
      
      long time = System.currentTimeMillis();
      long remainTime = -1;
      if (waitTime != -1) {
          remainTime = unit.toMillis(waitTime);
      }
      long lockWaitTime = calcLockWaitTime(remainTime);
      /**
       * 1. 允许加锁失败节点个数限制（N-(N/2+1)）
       */
      int failedLocksLimit = failedLocksLimit();
      /**
       * 2. 遍历所有节点通过EVAL命令执行lua加锁
       */
      List<RLock> acquiredLocks = new ArrayList<>(locks.size());
      for (ListIterator<RLock> iterator = locks.listIterator(); iterator.hasNext();) {
          RLock lock = iterator.next();
          boolean lockAcquired;
          /**
           *  3.对节点尝试加锁
           */
          try {
              if (waitTime == -1 && leaseTime == -1) {
                  lockAcquired = lock.tryLock();
              } else {
                  long awaitTime = Math.min(lockWaitTime, remainTime);
                  lockAcquired = lock.tryLock(awaitTime, newLeaseTime, TimeUnit.MILLISECONDS);
              }
          } catch (RedisResponseTimeoutException e) {
              // 如果抛出这类异常，为了防止加锁成功，但是响应失败，需要解锁所有节点
              unlockInner(Arrays.asList(lock));
              lockAcquired = false;
          } catch (Exception e) {
              // 抛出异常表示获取锁失败
              lockAcquired = false;
          }
          
          if (lockAcquired) {
              /**
               *4. 如果获取到锁则添加到已获取锁集合中
               */
              acquiredLocks.add(lock);
          } else {
              /**
               * 5. 计算已经申请锁失败的节点是否已经到达 允许加锁失败节点个数限制 （N-(N/2+1)）
               * 如果已经到达， 就认定最终申请锁失败，则没有必要继续从后面的节点申请了
               * 因为 Redlock 算法要求至少N/2+1 个节点都加锁成功，才算最终的锁申请成功
               */
              if (locks.size() - acquiredLocks.size() == failedLocksLimit()) {
                  break;
              }
  
              if (failedLocksLimit == 0) {
                  unlockInner(acquiredLocks);
                  if (waitTime == -1 && leaseTime == -1) {
                      return false;
                  }
                  failedLocksLimit = failedLocksLimit();
                  acquiredLocks.clear();
                  // reset iterator
                  while (iterator.hasPrevious()) {
                      iterator.previous();
                  }
              } else {
                  failedLocksLimit--;
              }
          }
  
          /**
           * 6.计算 目前从各个节点获取锁已经消耗的总时间，如果已经等于最大等待时间，则认定最终申请锁失败，返回false
           */
          if (remainTime != -1) {
              remainTime -= System.currentTimeMillis() - time;
              time = System.currentTimeMillis();
              if (remainTime <= 0) {
                  unlockInner(acquiredLocks);
                  return false;
              }
          }
      }
  
      if (leaseTime != -1) {
          List<RFuture<Boolean>> futures = new ArrayList<>(acquiredLocks.size());
          for (RLock rLock : acquiredLocks) {
              RFuture<Boolean> future = ((RedissonLock) rLock).expireAsync(unit.toMillis(leaseTime), TimeUnit.MILLISECONDS);
              futures.add(future);
          }
          
          for (RFuture<Boolean> rFuture : futures) {
              rFuture.syncUninterruptibly();
          }
      }
  
      /**
       * 7.如果逻辑正常执行完则认为最终申请锁成功，返回true
       */
      return true;
  }
  ```

- ##### Redission分布式锁的使用：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210310225633675.png" alt="image-20210310225633675" style="zoom:80%;" />

  Redission分布式锁原理（加锁和解锁都是使用lua脚本实现的）：

  - Redission客户端1跟据算法选择某个节点进行加锁，**加锁逻辑**：

    ```lua
    "if (redis.call('exists',KEYS[1])==0) then "+ --看有没有锁
        "redis.call('hset',KEYS[1],ARGV[2],1) ; "+ --无锁 加锁
        "redis.call('pexpire',KEYS[1],ARGV[1]) ; "+
        "return nil; end ;" +
    "if (redis.call('hexists',KEYS[1],ARGV[2]) ==1 ) then "+ --我加的锁
            "redis.call('hincrby',KEYS[1],ARGV[2],1) ; "+ --重入锁
            "redis.call('pexpire',KEYS[1],ARGV[1]) ; "+
    	"return nil; end ;" +
    "return redis.call('pttl',KEYS[1]) ;" --不能加锁，返回锁的时间
    ```

    **加锁逻辑：**就是生成key-value结构的元素。

    **重入锁逻辑：**就是对加锁时生成的key-value结构的元素的value+1。（重入锁前提条件：加锁时的key得存在）

    

  - **释放锁机制逻辑**：

    ```lua
    #如果key已经不存在，说明已经被解锁，直接发布（publish）redis消息
    "if (redis.call('exists', KEYS[1]) == 0) then " +
         "redis.call('publish', KEYS[2], ARGV[1]); " +
         "return 1; " +
       	 "end;" +
    # key和field不匹配，说明当前客户端线程没有持有锁，不能主动解锁。 不是我加的锁 不能解锁
    "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
        "return nil;" +
        "end; " +
    # 将value减1
    "local counter = redis.call('hincrby', KEYS[1], ARGV[3],-1); " +
    # 如果counter>0说明锁在重入，不能删除key
    "if (counter > 0) then " +
    	"redis.call('pexpire', KEYS[1], ARGV[2]); " +
    	"return 0; " +
    # 删除key并且publish 解锁消息
    	"else " +
    	"redis.call('del', KEYS[1]); " + #删除锁
    	"redis.call('publish', KEYS[2], ARGV[1]); " +
    ```

    其实说白了，就是每次都对myLock数据结构中的那个加锁次数减1。 如果发现加锁次数是0了，说明这个客户端已经不再持有锁了，此时就会用： “del myLock”命令，从redis里删除这个key。 然后呢，另外的客户端2就可以尝试完成加锁了。（重入锁得由一层层删，不能跨层删）
  
  #### 如果key过期，但是代码还没跑完，怎么办？
  
  redission会有看门狗机制，会自动续约。

#### 分布式锁特性 

- 互斥性 

  任意时刻，只能有一个客户端获取锁，不能同时有两个客户端获取到锁。 

- 同一性 

  锁只能被持有该锁的客户端删除，不能由其它客户端删除。 

- 可重入性 

  持有某个锁的客户端可继续对该锁加锁，实现锁的续租 

- 容错性 

  锁失效后（超过生命周期）自动释放锁（key失效），其他客户端可以继续获得该锁，防止死锁

#### 分布式锁的应用：

- 数据并发竞争

  利用分布式锁可以将处理串行化，前面已经讲过了。

- 防止库存超卖

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210310232151441.png" alt="image-20210310232151441" style="zoom:80%;" />

订单1和订单2都从Redis中获得分布式锁(setnx)，谁能获得锁谁进行下单操作，这样就把订单系统下单 的顺序串行化了，就不会出现超卖的情况了。伪码如下：

```java
//加锁并设置有效期
if(redis.lock("RDL",200)){
    //判断库存
    if (orderNum<getCount()){
    //加锁成功 ,可以下单
    order(5);
    //释放锁
    redis,unlock("RDL");
    }
}
```

注意此种方法会降低处理效率，这样不适合秒杀的场景，秒杀可以使用CAS和Redis队列的方式。

#### 和Zookeeper分布式锁的对比 

- 基于Redis的set实现分布式锁 
- 基于zookeeper临时节点的分布式锁
- 基于etcd实现

三者的对比，如下表：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210310232428720.png" alt="image-20210310232428720" style="zoom:80%;" />

#### Redis实现分布式架构的session分离：

传统的session是由tomcat自己进行维护和管理，但是对于集群或分布式环境，不同的tomcat管理各自 的session，很难进行session共享，通过传统的模式进行session共享，会造成session对象在各个 tomcat之间，通过网络和Io进行复制，极大的影响了系统的性能。 可以将登录成功后的Session信息，存放在Redis中，这样多个服务器(Tomcat)可以共享Session信息。 利用**spring-session-data-redis**（SpringSession），可以实现**基于redis来实现的session分离**。这个 知识点在讲Spring的时候可以讲过了，这里就不再赘述了。

### 阿里Redis使用手册：

本文主要介绍在使用阿里云Redis的开发规范，从下面几个方面进行说明。 

键值设计 、命令使用 、 客户端使用 、相关工具、删除bigkey

- #### 键值设计：

  1. **key名设计：**

     - **命令规范：**业务名：表名：id

     - **命令不宜过长：**可以使用简称

       ```
       user:{uid}:firends:message:{mid} 简化为 u:{uid}:fr:m:{mid}
       ```

     - **不要包含特殊字符：**不要包含空格、换行、单双引号以及其他转义字符

  2. **value设计：**

     - **拒绝bigkey：**string类型控制在10KB以内，hash、list、set、zset元素个数不要超过5000。

     - **选择适合的数据类型：**实体类型(要合理控制和使用数据结构内存编码优化配置,例如ziplist，但也要注意节省内存和性能之间的平衡)

       <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210309203019487.png" alt="image-20210309203019487" style="zoom:80%;" />

     - **控制key的生命周期 ：**redis不是垃圾桶，建议使用expire设置过期时间，不过期的数据重点关注idletime。

- #### 命令使用：

  1. 例如hgetall、lrange、smembers、zrange、sinter等并非不能使用，但是需要明确N的值。有遍历的需求可以使用hscan、sscan、zscan代替。

     ##### 为什么使用hscan、sscan、zscan比上面的好？

     答：hscan、sscan、zscan使用迭代方式，上面的是遍历方式。

  2. 禁用命令禁止线上使用keys、flushall、flushdb等，通过redis的rename机制禁掉命令，或者使用scan的方式渐进式处理。

     > ##### Redis的危险命令主要有：
     >
     > flushdb,清空数据库
     >
     > flushall,清空所有记录，数据库
     >
     > config,客户端连接后可配置服务器
     >
     > keys,客户端连接后可查看所有存在的键
     >
     > ##### 禁用命令：
     >
     > rename-command KEYS   ""
     > rename-command FLUSHALL ""
     > rename-command FLUSHDB ""
     > rename-command CONFIG  ""
     >
     > ##### 启用命令：
     >
     > rename-command KEYS   "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     > rename-command FLUSHALL "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     > rename-command FLUSHDB "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     > rename-command CONFIG  "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

  3. 使用批量操作提高效率：

     **原生命令：**例如mget、mset。

     **非原生命令：**可以使用pipeline提高效率。（但要注意控制一次批量操作的元素个数(例如500以内，实际也和元素字节数有关)。

     注意两者不同：

     - 原生是原子操作，pipeline是非原子操作。 
     - pipeline可以打包不同的命令，原生做不到 
     - pipeline需要客户端和服务端同时支持。

  4. 不建议过多使用Redis事务功能：Redis的事务功能较弱(不支持回滚)，而且集群版本(自研和官方)要求一次事务操作的key必须在一个slot 上(可以使用hashtag功能解决)

  5. Redis集群版本在使用Lua上有特殊要求

  6. monitor命令：必要情况下使用monitor命令时，要注意不要长时间使用。

- #### 客户端使用：

  1. **避免多个应用使用一个Redis实例：**不相干的业务拆分，公共数据做服务化。

  2. **获取连接使用连接池：**可以有效控制连接，同时提高效率，标准使用方式：

     <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210310121444307.png" alt="image-20210310121444307" style="zoom:80%;" />

  3. **熔断功能：**高并发下建议客户端添加熔断功能(例如netflix hystrix)

  4. **合理的加密：**设置合理的密码，如有必要可以使用SSL加密访问（阿里云Redis支持）

  5. **淘汰策略：**根据自身业务类型，选好maxmemory-policy(最大内存淘汰策略)，设置好过期时间。 默认策略是volatile-lru，即超过最大内存后，在过期键中使用lru算法进行key的剔除，保证不过期数据不被删除，但是可能会出现OOM问题。

     其他策略如下：
     · allkeys-lru：根据LRU算法删除键，不管数据有没有设置超时属性，直到腾出足够空间为止。
     · allkeys-random：随机删除所有键，直到腾出足够空间为止。
     · volatile-random:随机删除过期键，直到腾出足够空间为止。
     · volatile-ttl：根据键值对象的ttl属性，删除最近将要过期数据。如果没有，回退到noeviction策略。
     · noeviction：不会剔除任何数据，拒绝所有写入操作并返回客户端错误信息"(error) OOM
     command not allowed when used memory"，此时Redis只响应读操作。

- #### 相关工具：

  1. 数据同步：redis间数据同步可以使用-redis-port。
  2. big key搜索：redis大key搜索工具。
  3. 热点key寻找：内部实现使用monitor，所以建议短时间使用facebook的redis-faina，阿里云Redis已经在内核层面解决热点key问题。

- #### 删除bigkey：

  下面操作可以使用pipeline加速，redis 4.0已经支持key的异步删除，欢迎使用。
  
  1. Hash删除: hscan + hdel,如下图，使用hscan迭代里面的元素，根据元素长度循环使用hdel删除（下面的删除方法类似）
  
     ```java
     public void delBigHash(String host, int port, String password, String bigHashKey) {
         Jedis jedis = new Jedis(host, port);
         if (password != null && !"".equals(password)) {
             jedis.auth(password);
         }
         ScanParams scanParams = new ScanParams().count(100);
         String cursor = "0";
         do {
             ScanResult<Entry<String, String>> scanResult = jedis.hscan(bigHashKey, cursor, scanParams);
             List<Entry<String, String>> entryList = scanResult.getResult();
             if (entryList != null && !entryList.isEmpty()) {
                 for (Entry<String, String> entry : entryList) {
                     jedis.hdel(bigHashKey, entry.getKey());
                 }
             }
             cursor = scanResult.getStringCursor();
         } while (!"0".equals(cursor));
     
         //删除bigkey
         jedis.del(bigHashKey);
     }
     ```
  
  2. List删除: ltrim
  
     ```java
     public void delBigList(String host, int port, String password, String bigListKey) {
         Jedis jedis = new Jedis(host, port);
         if (password != null && !"".equals(password)) {
             jedis.auth(password);
         }
         long llen = jedis.llen(bigListKey);
         int counter = 0;
         int left = 100;
         while (counter < llen) {
             //每次从左侧截掉100个
             jedis.ltrim(bigListKey, left, llen);
             counter += left;
         }
         //最终删除key
         jedis.del(bigListKey);
     }
     ```
  
  3. Set删除: sscan + srem
  
     ```java
     public void delBigSet(String host, int port, String password, String bigSetKey) {
         Jedis jedis = new Jedis(host, port);
         if (password != null && !"".equals(password)) {
             jedis.auth(password);
         }
         ScanParams scanParams = new ScanParams().count(100);
         String cursor = "0";
         do {
             ScanResult<String> scanResult = jedis.sscan(bigSetKey, cursor, scanParams);
             List<String> memberList = scanResult.getResult();
             if (memberList != null && !memberList.isEmpty()) {
                 for (String member : memberList) {
                     jedis.srem(bigSetKey, member);
                 }
             }
             cursor = scanResult.getStringCursor();
         } while (!"0".equals(cursor));
     
         //删除bigkey
         jedis.del(bigSetKey);
     }
     ```
  
  4. SortedSet删除: zscan + zrem
  
     ```java
     
     public void delBigZset(String host, int port, String password, String bigZsetKey) {
         Jedis jedis = new Jedis(host, port);
         if (password != null && !"".equals(password)) {
             jedis.auth(password);
         }
         ScanParams scanParams = new ScanParams().count(100);
         String cursor = "0";
         do {
             ScanResult<Tuple> scanResult = jedis.zscan(bigZsetKey, cursor, scanParams);
             List<Tuple> tupleList = scanResult.getResult();
             if (tupleList != null && !tupleList.isEmpty()) {
                 for (Tuple tuple : tupleList) {
                     jedis.zrem(bigZsetKey, tuple.getElement());
                 }
             }
             cursor = scanResult.getStringCursor();
         } while (!"0".equals(cursor));
      
         //删除bigkey
         jedis.del(bigZsetKey);
     }
     ```
  
     

## Redis

#### redis集群数据一致性如何保证，如果挂了一台机会怎样？

可以从故障检测和从节点选举说起

- ##### 故障检测

  集群中每个节点都会定期的向集群中的其他节点发送PING信息。

  如果在一定时间内，发送ping的节点A没有收到某节点B的pong回应，那么A将B标识为pfail。

  A在后续发送ping时，会带上B的pfail信息，通知给其他节点。

  如果B被标记为pfail的个数大于集群主节点个数的一半（N/2 + 1）时，B会被标记为fail，A向整个集群广播，该节点已经下线。 其他节点收到广播，标记B为fail。（又是过半原则）

- ##### 从节点选举

  每个从节点，根据自己对master复制数据的offset(偏移量)，来设置一个选举时间，offset越大的从节点，选举时间越靠前，

  优先进行选举。

  所有的Master开始slave选举投票，给要进行选举的slave进行投票，如果大部分master node（N/2 +1）都投票给了某个从节点，那么选举通过，那个从节点可以切换成master。 RedisCluster失效的判定： 1、集群中半数以上的主节点都宕机（无法投票） 2、宕机的主节点的从节点也宕机了（slot槽分配不连续）

  当slave 收到过半的master 同意时，会成为新的master。此时会以最新的Epoch 通过PONG 消息广播自己成为master，让Cluster 的其他节点尽快的更新拓扑结构(node.conf)。

#### 内存耗尽后 Redis 会发生什么？

##### 当内存满了以后，Redis 中提供了 8种淘汰策略：

修改淘汰策略的两种方式：

1. config set maxmemory-policy <策略>来动态修改，
2. 修改redis.conf的`maxmemory-policy`参数。

配置 `Redis` 最大使用内存的两种方式：

1. config set maxmemory 1GB来动态修改，
2. 修改redis.conf的`maxmemory`参数。

| 淘汰策略        | 说明                                                         |
| :-------------- | :----------------------------------------------------------- |
| volatile-lru    | 根据 LRU 算法删除设置了过期时间的键，直到腾出可用空间。如果没有可删除的键对象，且内存还是不够用时，则报错 |
| allkeys-lru     | 根据 LRU 算法删除所有的键，直到腾出可用空间。如果没有可删除的键对象，且内存还是不够用时，则报错 |
| volatile-lfu    | 根据 LFU 算法删除设置了过期时间的键，直到腾出可用空间。如果没有可删除的键对象，且内存还是不够用时，则报错 |
| allkeys-lfu     | 根据 LFU 算法删除所有的键，直到腾出可用空间。如果没有可删除的键对象，且内存还是不够用时，则报错 |
| volatile-random | 随机删除设置了过期时间的键，直到腾出可用空间。如果没有可删除的键对象，且内存还是不够用时，则报错 |
| allkeys-random  | 随机删除所有键，直到腾出可用空间。如果没有可删除的键对象，且内存还是不够用时，则报错 |
| volatile-ttl    | 根据键值对象的 ttl 属性， 删除最近将要过期数据。如果没有，则直接报错 |
| noeviction      | 默认策略，不作任何处理，直接报错                             |

##### 预防内存满的策略：

使用设置有效期以及过期策略将过期的键删除

- 设置有效期：

  Redis中可以通过 4 个独立的命令来给一个键设置过期时间

  - `expire key ttl`：将 `key` 值的过期时间设置为 `ttl` **秒** 。
  - `pexpire key ttl`：将 `key` 值的过期时间设置为 `ttl` **毫秒** 。
  - `expireat key timestamp`：将 `key` 值的过期时间设置为指定的 `timestamp` **秒数** 。
  - `pexpireat key timestamp`：将 `key` 值的过期时间设置为指定的 `timestamp` **毫秒数** 。

  set也可以设置 `key` 的过期时间，设置了有效期后，可以通过 `ttl` 和 `pttl` 两个命令来查询剩余过期时间（如果未设置过期时间则下面两个命令返回 `-1`，如果设置了一个非法的过期时间，则都返回 `-2`）：

  - `ttl key` 返回 `key` 剩余过期秒数。
  - `pttl key` 返回 `key` 剩余过期的毫秒数。

- 过期策略：

  - **定时删除** ：为每个键设置一个定时器，一旦过期时间到了，则将键删除。这种策略对内存很友好，但是对 `CPU` 不友好，因为每个定时器都会占用一定的 `CPU` 资源。

  - **惰性删除** ：不管键有没有过期都不主动删除，等到每次去获取键时再判断是否过期，如果过期就删除该键，否则返回键对应的值。这种策略对内存不够友好，可能会浪费很多内存。

  - **定期扫描** ：系统每隔一段时间就定期扫描一次，发现过期的键就进行删除。这种策略相对来说是上面两种策略的折中方案，需要注意的是这个定期的频率要结合实际情况掌控好，使用这种方案有一个缺陷就是可能会出现已经过期的键也被返回。

    在 `Redis` 当中，其选择的是策略 `2` 和策略 `3` 的综合使用。不过 `Redis`的定期扫描只会扫描设置了过期时间的键，因为设置了过期时间的键`Redis`会单独存储，所以不会出现扫描所有键的情况：

    ```java
    typedef struct redisDb {
            dict *dict; //所有的键值对
            dict *expires; //设置了过期时间的键值对
            dict *blocking_keys; //被阻塞的key,如客户端执行BLPOP等阻塞指令时
            dict *watched_keys; //WATCHED keys
            int id; //Database ID
            //... 省略了其他属性
            } redisDb;
    ```

##### LRU 算法：

最近最长时间未被使用。这个主要针对的是使用时间。

传统的 LRU 算法存在 2 个问题：

- 需要额外的空间进行存储。
- 可能存在某些 key 值使用很频繁，但是最近没被使用，从而被 LRU算法删除。

为了避免以上 `2` 个问题，`Redis` 当中对传统的 `LRU` 算法进行了改造，**通过抽样的方式进行删除** 。

配置文件中提供了一个属性 `maxmemory_samples 5`，默认值就是 `5`，表示随机抽取 `5` 个 `key` 值，然后对这 `5` 个 `key` 值按照 `LRU` 算法进行删除，所以很明显，`key` 值越大，删除的准确度越高。

对抽样 `LRU` 算法和传统的 `LRU` 算法，`Redis` 官网当中有一个对比图：

- 浅灰色带是被删除的对象。
- 灰色带是未被删除的对象。
- 绿色是添加的对象。

![image-20210410095730027](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210410095730027.png)

左上角第一幅图代表的是传统 `LRU` 算法，可以看到，当抽样数达到 `10`个（右上角），已经和传统的 `LRU` 算法非常接近了。

##### LFU 算法：

最近最少频率使用，这个主要针对的是使用频率。这个属性也是记录在`redisObject` 中的 `lru` 属性内。

当我们采用 `LFU` 回收策略时，`lru` 属性的高 `16` 位用来记录访问时间（last decrement time：ldt，单位为分钟），低 `8` 位用来记录访问频率（logistic counter：logc），简称 `counter`。

`LFU` 计数器每个键只有 `8` 位，它能表示的最大值是 `255`，`Redis`使用的是一种基于概率的对数器来实现 `counter` 的递增，当某一个 `key` 一段时间不被访问之后，`counter` 也需要对应减少。

- `counter` 按以下方式递增：

1. 提取 `0` 和 `1` 之间的随机数 `R`。
2. `counter` - 初始值（默认为 `5`），得到一个基础差值，如果这个差值小于 `0`，则直接取 `0`，为了方便计算，把这个差值记为 `baseval`。
3. 概率 `P` 计算公式为：`1/(baseval * lfu_log_factor + 1)`。
4. 如果 `R < P` 时，频次进行递增（`counter++`）。

公式中的 `lfu_log_factor` 称之为对数因子，默认是 `10` ，可以通过参数来进行控制：

```
lfu_log_factor 10
```

下图就是对数因子 `lfu_log_factor` 和频次 `counter` 增长的关系图：

![image-20210410100721268](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210410100721268.png)

可以看到，当对数因子 `lfu_log_factor` 为 `100` 时，大概是 `10M（1000万）` 次访问才会将访问 `counter` 增长到 `255`，而默认的 `10` 也能支持到 `1M（100万）` 次访问 `counter` 才能达到 `255` 上限，这在大部分场景都是足够满足需求的。

- `counter` 按以下方式递减：

  `counter` 的减少速度由参数 `lfu-decay-time` 进行控制，默认是 `1`，单位是分钟。默认值 `1` 表示：`N` 分钟内没有访问，`counter` 就要减 `N`。

  ```
  lfu-decay-time 1
  ```

  具体算法如下：

  1. 获取当前时间戳，转化为**分钟** 后取低 `16` 位（为了方便后续计算，这个值记为 `now`）。
  2. 取出对象内的 `lru` 属性中的高 `16` 位（为了方便后续计算，这个值记为 `ldt`）。
  3. 当 `lru` > `now` 时，默认为过了一个周期（`16` 位，最大 `65535`)，则取差值 `65535-ldt+now`：当 `lru` <= `now` 时，取差值 `now-ldt`（为了方便后续计算，这个差值记为 `idle_time`）。
  4. 取出配置文件中的 `lfu_decay_time` 值，然后计算：`idle_time / lfu_decay_time`（为了方便后续计算，这个值记为`num_periods`）。
  5. 最后将`counter`减少：`counter - num_periods`。

  看起来这么复杂，其实计算公式就是一句话：取出当前的时间戳和对象中的 `lru` 属性进行对比，计算出当前多久没有被访问到，比如计算得到的结果是 `100` 分钟没有被访问，然后再去除配置参数 `lfu_decay_time`，如果这个配置默认为 `1`也即是 `100/1=100`，代表 `100` 分钟没访问，所以 `counter` 就减少 `100`。

Redis遇到的坑：

##### 常见命令有那些坑

![image-20210412200600610](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210412200600610.png)

1. 过期时间意外丢失？

   SET 除了可以设置 key-value 之外，还可以设置 key 的过期时间，就像下面这样：

   ```java
   127.0.0.1:6379> SET testkey val1 EX 60
   OK
   127.0.0.1:6379> TTL testkey
   (integer) 59
   ```

   此时如果你想修改 key 的值，但只是单纯地使用 SET 命令，而没有加上「过期时间」的参数，那这个 key 的过期时间将会被「擦除」。

   ```java
   127.0.0.1:6379> SET testkey val2
   OK
   127.0.0.1:6379> TTL testkey  // key永远不过期了！
   (integer) -1
   ```

   看到了么？testkey 变成永远不过期了！

   **总结：**所以，你在使用 SET 命令时，如果刚开始就设置了过期时间，那么之后修改这个 key，也务必要加上过期时间的参数，避免过期时间丢失问题。

2. DEL 竟然也会阻塞 Redis？

   **删除一个 key 的耗时，与这个 key 的类型有关。**

   删除一个 key，你肯定会用 DEL 命令，不知道你没有思考过它的时间复杂度是多少？Redis 官方文档在介绍 DEL 命令时，是这样描述的：

   - key 是 String 类型，DEL 时间复杂度是 O(1)

   - key 是 List/Hash/Set/ZSet 类型，DEL 时间复杂度是 O(M)，M 为元素数量

     **也就是说，如果你要删除的是一个非 String 类型的 key，这个 key 的元素越多，那么在执行 DEL 时耗时就越久！**

     删除key在讲义有讲

3. RANDOMKEY 竟然也会阻塞 Redis？

   这个命令会从 Redis 中「随机」取出一个 key。

   为什么会出现阻塞？

   如果你对 Redis 的过期策略有所了解，应该知道 Redis 清理过期 key，是采用定时清理 + 懒惰清理 2 种方式结合来做的。

   而 RANDOMKEY 在随机拿出一个 key 后，首先会先检查这个 key 是否已过期。

   如果该 key 已经过期，那么 Redis 会删除它，这个过程就是**懒惰清理**。

   但清理完了还不能结束，Redis 还要找出一个「不过期」的 key，返回给客户端。

   此时，Redis 则会继续随机拿出一个 key，然后再判断是它否过期，直到找出一个未过期的 key 返回给客户端。

   整个流程就是这样的：

   1. master 随机取出一个 key，判断是否已过期
   2. 如果 key 已过期，删除它，继续随机取 key
   3. 以此循环往复，直到找到一个不过期的 key，返回

   但这里就有一个问题了：**如果此时 Redis 中，有大量 key 已经过期，但还未来得及被清理掉，那这个循环就会持续很久才能结束，而且，这个耗时都花费在了清理过期 key + 寻找不过期 key 上。**

   导致的结果就是，RANDOMKEY 执行耗时变长，影响 Redis 性能。

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210410102811195.png" alt="image-20210410102811195" style="zoom:67%;" />

   以上流程，其实是在 master 上执行的。

   如果在 slave 上执行 RANDOMEKY，那么问题会更严重！

   为什么？

   主要原因就在于，slave 自己是不会清理过期 key。

   那 slave 什么时候删除过期 key 呢？

   其实，当一个 key 要过期时，master 会先清理删除它，之后 master 向 slave 发送一个 DEL 命令，告知 slave 也删除这个 key，以此达到主从库的数据一致性。

   还是同样的场景：Redis 中存在大量已过期，但还未被清理的 key，那在 slave 上执行 RANDOMKEY 时，就会发生以下问题：

   1. slave 随机取出一个 key，判断是否已过期
   2. key 已过期，但 slave 不会删除它，而是继续随机寻找不过期的 key
   3. 由于大量 key 都已过期，那 slave 就会寻找不到符合条件的 key，此时就会陷入「**死循环**」！

   **也就是说，在 slave 上执行 RANDOMKEY，有可能会造成整个 Redis 实例卡死！**

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210410102830140.png" alt="image-20210410102830140" style="zoom:67%;" />

   是不是没想到？在 slave 上随机拿一个 key，竟然有可能造成这么严重的后果？

   这其实是 Redis 的一个 Bug，这个 Bug 一直持续到 5.0 才被修复。

   修复的解决方案是，在 slave 上执行 RANDOMKEY 时，会先判断整个实例所有 key 是否都设置了过期时间，如果是，为了避免长时间找不到符合条件的 key，**slave 最多只会在哈希表中寻找 100 次**，无论是否能找到，都会退出循环。

   这个方案就是增加上了一个最大重试次数，这样一来，就避免了陷入死循环。

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210410102842455.png" alt="image-20210410102842455" style="zoom:67%;" />

   虽然这个方案可以避免了 slave 陷入死循环、卡死整个实例的问题，但是，在 master 上执行这个命令时，依旧有概率导致耗时变长。

   所以，你在使用 RANDOMKEY 时，如果发现 Redis 发生了「抖动」，很有可能是因为这个原因导致的！

4. O(1) 复杂度的 SETBIT，竟然会导致 Redis OOM？

   SETBIT使用（SETBIT只能设置0和1）：

   ```java
   127.0.0.1:6379> SETBIT testkey 10 1
   (integer) 1
   127.0.0.1:6379> GETBIT testkey 10
   (integer) 1
   ```

   ![image-20210410110548643](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210410110548643.png)

   当你在使用 SETBIT 时，也一定要注意 offset 的大小，操作过大的 offset 也会引发 Redis 卡顿。

   - ##### SETBIT 使用场景一：用户周活跃

   一周用户登录情况，假设用户ID 1000 1001 1002

   ```java
   127.0.0.1:6379> setbit Monday 1000 0
   (integer) 0
   127.0.0.1:6379> setbit Monday 1001 1
   (integer) 0
   127.0.0.1:6379> setbit Monday 1002 1
   (integer) 0
   127.0.0.1:6379> setbit Tuesday 1000 0
   (integer) 0
   127.0.0.1:6379> setbit Tuesday 1001 0
   (integer) 0
   127.0.0.1:6379> setbit Tuesday 1002 1
   (integer) 0
   127.0.0.1:6379> setbit Wednesday 1000 0
   (integer) 0
   127.0.0.1:6379> setbit Wednesday 1001 1
   (integer) 0
   127.0.0.1:6379> setbit Wednesday 1002 1
   (integer) 0
   127.0.0.1:6379> setbit Thursday 1000 0
   (integer) 0
   127.0.0.1:6379> setbit Thursday 1001 0
   (integer) 0
   127.0.0.1:6379> setbit Thursday 1002 0
   (integer) 0
   127.0.0.1:6379> setbit Friday 1000 0
   (integer) 0
   127.0.0.1:6379> setbit Friday 1001 1
   (integer) 0
   127.0.0.1:6379> setbit Friday 1002 1
   (integer) 0
   127.0.0.1:6379> setbit Saturday 1000 0
   (integer) 0
   127.0.0.1:6379> setbit Saturday 1001 1
   (integer) 0
   127.0.0.1:6379> setbit Saturday 1002 0
   (integer) 0
   127.0.0.1:6379> setbit Sunday 1000 0
   (integer) 0
   127.0.0.1:6379> setbit Sunday 1001 1
   (integer) 0
   127.0.0.1:6379> setbit Sunday 1002 0
   (integer) 0
   ```

   接下来要计算7天内有登录行为的用户，只需要将周一到周五的值做位或运算就可以了
   补充下位与运算符：

   ```java
   按位与运算符（&）
   参加运算的两个数据，按二进制位进行“与”运算。
   运算规则：0&0=0;  0&1=0;   1&0=0;    1&1=1;
       即：两位同时为“1”，结果才为“1”，否则为0
         
   按位或运算符（|）
   参加运算的两个对象，按二进制位进行“或”运算。
   运算规则：0|0=0；  0|1=1；  1|0=1；   1|1=1；
       即 ：参加运算的两个对象只要有一个为1，其值为1。
        
   异或运算符（^）
   参加运算的两个数据，按二进制位进行“异或”运算。
   运算规则：0^0=0；  0^1=1；  1^0=1；   1^1=0；
   即：参加运算的两个对象，如果两个相应位为“异”（值不同），则该位结果为1，否则为0。
   ```

   命令格式：

   ```java
   bitop operation destkey key1 [key2 …]
   ```

   - ##### 解释

   对key1 key2做opecation并将结果保存在destkey上
   opecation可以是AND（与） OR（或） NOT（非） XOR（异或）

   最后计算7天内登录过的活跃用户：

   ```java
   127.0.0.1:6379> bitop OR result Monday Tuesday Wednesday Thursday Friday Saturday Sunday
   ```

   ```java
   1000 0|0|0|0|0|0|0 = 0
   1001 1|0|1|0|1|1|1 = 1
   1002 1|1|1|0|1|0|0 = 1
   ```

   这里计算的结果假设3个用户id都是连续的话就是 110，其实真实的存储位置是…1…1…0…

   ```java
   127.0.0.1:6379> bitcount result
   (integer) 2
   ```

   也就是本周有2个活跃用户登录过。

   还有其他使用场景参见：https://blog.csdn.net/wade1010/article/details/109545263。

   - ##### SETBIT 使用的好处：

     如上例,优点:
     1: 节约空间, 1亿人每天的登陆情况,用1亿bit,约1200WByte,约10M 的字符就能表示
     2: 计算方便

5. 执行 MONITOR 也会导致 Redis OOM？

   这个坑你肯定听说过很多次了。

   当你在执行 MONITOR 命令时，Redis 会把每一条命令写到客户端的「输出缓冲区」中，然后客户端从这个缓冲区读取服务端返回的结果。

   ![image-20210410134714412](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210410134714412.png)

   但是，如果你的 Redis QPS 很高，这将会导致这个输出缓冲区内存持续增长，占用 Redis 大量的内存资源，如果恰好你的机器的内存资源不足，那 Redis 实例就会面临被 OOM 的风险。

   所以，你需要谨慎使用 MONITOR，尤其在 QPS 很高的情况下。

   以上这些问题场景，都是我们在使用常见命令时发生的，而且，很可能都是「无意」就会触发的。

##### 数据持久化有哪些坑？

![image-20210412200646429](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210412200646429.png)

1. Redis没有配置数据持久化，master 宕机，slave 数据也丢失了？

2. AOF的持久化策略 everysec有可能阻塞主线程：

   > 当 Redis 开启 AOF 时，需要配置 AOF 的刷盘策略。
   >
   > 基于性能和数据安全的平衡，你肯定会采用 appendfsync everysec 这种方案。
   >
   > 这种方案的工作模式为，Redis 的后台线程每间隔 1 秒，就把 AOF page cache 的数据，刷到磁盘（fsync）上。
   >
   > 这种方案的优势在于，把 AOF 刷盘的耗时操作，放到了后台线程中去执行，避免了对主线程的影响。
   >
   > 但真的不会影响主线程吗？
   >
   > 答案是否定的。

   appendfsync everysec刷盘流程：

   ![image-20210412193513948](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210412193513948.png)

   当 IO 负载过高导致 fynsc 阻塞，进而导致主线程写 AOF page cache 也发生阻塞。

   **解决方法：**保证磁盘有充足的 IO 资源，避免这个问题。

3. AOF everysec可能会丢2秒数据：

   如果执行到2秒，准备写AOF page cache，这时 Redis 发生了宕机，那么有2秒的数据会丢失。

   ##### 为什么设置2秒不设置1秒呢？

   **答：**因为主线程写 AOF page cache 之前，需要先检查一下距离上一次 fsync 成功的时间；如果 fsync 阻塞，2秒的话也可以留出一点时间等待 fsync 成功。

4. RDB 和 AOF rewrite 时，Redis 发生 OOM？

   ![image-20210412195848925](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210412195848925.png)

   1. 如上图，Redis的rewrite 流程是先创建子进程，使用子进程把实例中的数据持久化到磁盘上。

   2. 子进程，会调用操作系统的 fork 函数。fork 执行完成后，父进程和子进程会同时共享同一份内存数据。

   3. 但此时的主进程依旧是可以接收写请求的，而进来的写请求，会采用 Copy On Write（写时复制）的方式操作内存数据。

      - Copy On Write：主进程一旦有数据需要修改，Redis 并不会直接修改现有内存中的数据，而是先将这块内存数据拷贝出来，再修改这块新内存的数据，这就是所谓的「写时复制」。

        如果这时候写请求过多，就会产生大量的内存拷贝工作面临被 OOM 的风险。

   **解决方法：**给 Redis 机器预留内存

##### 主从复制有哪些坑？

![image-20210412200718638](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210412200718638.png)

#### 高可用方案

哨兵模式

#### key设计问题

举例：

- 原先在mysql里面数据存储：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210426213818866.png" alt="image-20210426213818866" style="zoom:80%;" />

- 如果需要把上面的mysql中表里面的数据存储redis里面，存储的key该如何设计？

  分以下几步操作：

  a、把mysql里面的表名换成redis里面key的前缀（it_user前缀）

  b、把mysql表里面的主键名称放在上面的前缀后面，一般用冒号分割（it_user:id）

  c、对应记录的主键值作为key的第三步（it_user:id:1）

  d、把mysql里面的其他字段作为key的第四部分（it_user:id:1:username）

  把上面的记录保存在redis中：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210426213836415.png" alt="image-20210426213836415" style="zoom:80%;" />

#### 批量命令（mget、mset相关）和不同数据类型如何批量修改值

- #### 字符串 string

  字符串类型是 Redis 中最为基础的数据存储类型，它在 Redis 中是二进制安全的，这便意味着该类型可以接受任何格式的数据，如JPEG图像数据或Json对象描述信息等。在Redis中字符串类型的Value最多可以容纳的数据长度是512M。

  - 设置键值

    > set key value

  - 设置键值及过期时间，以秒为单位

    > setex key seconds value
    >
    > 例：设置键为aa值为aa过期时间为3秒的数据
    >
    > ​			setex aa 3 aa

  - 设置多个键值

    > mset key1 value1 key2 value2 ...

  - 追加值

    > append key value

  - 获取：根据键获取值，如果不存在此键则返回nil

    > get key

  - 根据多个键获取多个值

    > mget key1 key2 ...

    ##### 键命令：

    查找键，参数⽀持正则表达式

    > keys pattern

    例1：查看所有键

    > keys *

    　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713204542235-1531868116.png)

    例2：查看名称中包含a的键

    > 　　keys 'a*'

    　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713204711949-258695828.png)

    判断键是否存在，如果存在返回1，不存在返回0

    > exists key1

    查看键对应的value的类型

    > type key

    删除键及对应的值

    > del key1 key2 ...

    如果没有指定过期时间则⼀直存在，直到使⽤DEL移除

    > expire key seconds

    查看有效时间，以秒为单位

    > ttl key

- #### 哈希 hash

  - hash⽤于存储对象，对象的结构为属性、值
  - 值的类型为string

  - 设置单个属性

    > hset key field value

  - 设置多个属性

    > hmset key field1 value1 field2 value2 ...

  - 获取指定键所有的属性

    > hkeys key
    >
    > 例：获取键u2的所有属性
    >
    > > hkeys u2
    >
    > <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210426221643186.png" alt="image-20210426221643186" style="zoom:80%;" />

  - 获取⼀个属性的值

    > hget key field

  - 删除属性，属性对应的值会被⼀起删除

    > hdel key field1 field2 ...

- #### 列表 list

  - 列表的元素类型为string
  - 按照插⼊顺序排序

  - 在左侧插入数据

    > lpush key value1 value2 ...

  - 例1：从键为'a1'的列表左侧加⼊数据a 、 b 、c

    > lpush a1 a b c

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211427073-1696197158.png)

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211450306-1810876625.png)

  - 在右侧插⼊数据

    > rpush key value1 value2 ...

  - 例2：从键为'a1'的列表右侧加⼊数据0 1

    > rpush a1 0 1

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211532438-511058803.png)

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211543893-511486895.png)

  - 在指定元素的前或后插⼊新元素

    > linsert key before或after 现有元素 新元素

  - 例3：在键为'a1'的列表中元素'b'前加⼊'3'

    > linsert a1 before b 3

  　　 ![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211601361-602340593.png)

  ##### 获取

  - 返回列表⾥指定范围内的元素

    - start、stop为元素的下标索引
    - 索引从左侧开始，第⼀个元素为0
    - 索引可以是负数，表示从尾部开始计数，如-1表示最后⼀个元素

    > lrange key start stop

  - 例4：获取键为'a1'的列表所有元素

    > lrange a1 0 -1

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211622031-501372876.png)

  ##### 设置指定索引位置的元素值

  - 索引从左侧开始，第⼀个元素为0

  - 索引可以是负数，表示尾部开始计数，如-1表示最后⼀个元素

    > lset key index value

  - 例5：修改键为'a1'的列表中下标为1的元素值为'z'

    > lset a 1 z

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211643173-772017135.png)

  ##### 删除

  - 删除指定元素

    - 将列表中前count次出现的值为value的元素移除
    - count > 0: 从头往尾移除
    - count < 0: 从尾往头移除
    - count = 0: 移除所有

    > lrem key count value

  - 例6.1：向列表'a2'中加⼊元素'a'、'b'、'a'、'b'、'a'、'b'

    > lpush a2 a b a b a b

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211705684-1901572451.png)

  - 例6.2：从'a2'列表右侧开始删除2个'b'

    > lrem a2 -2 b

  - 例6.3：查看列表'py12'的所有元素

    > lrange a2 0 -1

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211723284-62841974.png)

- #### 集合 set

  - ⽆序集合
  - 元素为string类型
  - 元素具有唯⼀性，不重复
  - 说明：对于集合没有修改操作

  ##### 增加

  - 添加元素

    > sadd key member1 member2 ...

  - 例1：向键'a3'的集合中添加元素'zhangsan'、'lisi'、'wangwu'

    > sadd a3 zhangsan sili wangwu

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211915558-53975270.png)

  ##### 获取

  - 返回所有的元素

    > smembers key

  - 例2：获取键'a3'的集合中所有元素

    > smembers a3

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211934965-1120357142.png)

  ##### 删除

  - 删除指定元素

    > srem key

  - 例3：删除键'a3'的集合中元素'wangwu'

    > srem a3 wangwu

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713211952419-1529936276.png)

- #### 有序集合 zset

  - sorted set，有序集合
  - 元素为string类型
  - 元素具有唯⼀性，不重复
  - **每个元素都会关联⼀个double类型的score，表示权重，通过权重将元素从小到大排序**
  - 说明：没有修改操作

  ##### 增加

  - 添加

    > zadd key score1 member1 score2 member2 ...

  - 例1：向键'a4'的集合中添加元素'lisi'、'wangwu'、'zhaoliu'、'zhangsan'，权重分别为4、5、6、3

    > zadd a4 4 lisi 5 wangwu 6 zhaoliu 3 zhangsan

  ##### 获取

  - 返回指定范围内的元素

  - start、stop为元素的下标索引

  - 索引从左侧开始，第⼀个元素为0

  - 索引可以是负数，表示从尾部开始计数，如-1表示最后⼀个元素

    > zrange key start stop

  - 例2：获取键'a4'的集合中所有元素

    > zrange a4 0 -1

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713212041838-1679288952.png)

  - 返回score值在min和max之间的成员

    > zrangebyscore key min max

  - 例3：获取键'a4'的集合中权限值在5和6之间的成员

    > zrangebyscore a4 5 6

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713212103456-206059435.png)

  - 返回成员member的score值

    > zscore key member

  - 例4：获取键'a4'的集合中元素'zhangsan'的权重

    > zscore a4 zhangsan

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713212122044-1643922957.png)

  ##### 删除

  - 删除指定元素

    > zrem key member1 member2 ...

  - 例5：删除集合'a4'中元素'zhangsan'

    > zrem a4 zhangsan

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713212142807-130010064.png)

  - 删除权重在指定范围的元素

    > zremrangebyscore key min max

  - 例6：删除集合'a4'中权限在5、6之间的元素

    > zremrangebyscore a4 5 6

  　　![img](https://images2018.cnblogs.com/blog/1396321/201807/1396321-20180713212201087-1750772244.png)

#### Redis数据结构，如果设计一个延时队列用哪种

使用zset，因为有唯一有序的特性，且zrangebyscore 命令能筛选满足条件的成员，举例：

我们现在有这么一个需求，从上游接到运输包裹的时候会绑定一个车牌号，现在要做一个需求，接到的运输包裹按照配送区域数量满足十个合并绑定到一个车牌上，当同配送区域数量不满足条件并且等待时间超过四分钟时则直接绑定到对应车牌并通知到对应的分拣人员。（也就是说要么满足10个，要么超时4分钟，满足一个条件就会执行）

伪代码：

```java
 		//存入代码
		//zset结构来存放接到第一个同区域包裹的时间戳。
		if   box == area:
			redis.sadd('set_'+area,box)
			if  None == redis.zscore('zset',area):
				redis.zadd('zset_','推送的时间戳',area)
		if	redis.scard('set_'+area)>=10:
			//执行绑定
			redis.smembers('set_'+area)
			redis.del('set_'+area)
			redis.zrem('zset_',area)
	  //获取满足要求的包裹
		areas = redis.zrangebyscore('zset_',0,'当前时间-需延迟时间的时间戳')
		for area in areas:
			//执行绑定
			boxs=redis.smembers('set_'+area)
			redis.del('set_'+area)
			redis.zrem('zset_',area)
```

上面这段代码主要是分为两个步骤,第一在接到第一个包裹的时候按照区域埋入当前时间的时间戳，第二步是通过zrangebyscore这个命令来扫描已经满足时间已经达到约定的区域信息，通过这样一种方式，我们简易的延时队列就算是实现了。（但是在并发场景没有保证更新与删除的原子性，可以使用lua脚本进行改进）

```lua
	KEYS[1]: 区域包裹信息key,
	KEYS[2]:区域时间戳的集合,
	ARGV[1]: 包裹信息
	AGRV[2]:当前时间的时间戳
	AGRV[3]:区域信息
	//之前存放的代码：
	if redis.call('zscore',KEYS[2],ARGV[3]) then
	else
		redis.call('zadd',KEYS[2],ARGV[2],ARGV[3])
	end
	redis.call('sadd',KEYS[1],ARGV[1])
	if redis.call('scard',KEYS[1])>=4 then
		local ll= redis.call('smembers',KEYS[1])
		redis.call('del',KEYS[1])
		redis.call('zrem',KEYS[2],ARGV[3])
		return ll
	end
	return nil
------------------我是脚本分割线----------------------------------------------------	
KEYS[1]: 区域包裹信息key,
KEYS[2]:区域时间戳的集合,
AGRV[1]:0，
AGRV[2]:当前时间-延迟时间的时间戳
// 之前获取的代码
local time=redis.call('zrangebyscore',KEYS[2],ARGV[1],ARGV[2])
local sch={}
if next(time)~=nil then
  for key,value in pairs(time) do
	local tempkey=KEYS[1]..value
	local templist=redis.call('SMEMBERS',tempkey)
	sch[key]=templist
	redis.pcall('zrem',KEYS[2],value)
	redis.pcall('del',tempkey)
  end
  return sch
else
  return nil
end
```

#### 有用过redis的二级缓存吗？

- ##### mybatis使用redis实现二级缓存：

  ​	一级缓存是SqlSession级别的缓存。在操作数据库时需要构造 sqlSession对象，在对象中有一个(内存区域)数据结构（HashMap）用于存储缓存数据。不同的sqlSession之间的缓存数据区域（HashMap）是互相不影响的。

  ​	二级缓存是mapper级别的缓存，多个SqlSession去操作同一个Mapper的sql语句，多个SqlSession去操作数据库得到数据会存在二级缓存。

- ##### Mybatis的二级缓存的实现：

  1. 配置文件中开启二级缓存：

     ```xml
     <setting name="cacheEnabled" value="true"/>
     ```

     默认二级缓存是开启的。

  2. 实现Mybatis的Cache接口：
     Mybatis提供了第三方Cache实现的接口，我们自定义MybatisRedisCache实现Cache接口，代码如下：

     ```java
     /**
      * 创建时间：2016年1月7日 上午11:40:00
      * 
      * Mybatis二级缓存实现类
      * 
      * @author andy
      * @version 2.2
      */
      
     public class MybatisRedisCache implements Cache {
     
     	private static final Logger LOG = Logger.getLogger(MybatisRedisCache.class); 
     	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
     	
     	private RedisTemplate<Serializable, Serializable> redisTemplate =  (RedisTemplate<Serializable, Serializable>) SpringContextHolder.getBean("redisTemplate"); 
     	
     	private String id;
     	
     	private JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();
     	
     	public MybatisRedisCache(final String id){
     		if(id == null){
     			throw new IllegalArgumentException("Cache instances require an ID");
     		}
     		LOG.info("Redis Cache id " + id);
     		this.id = id;
     	}
     	
     	@Override
     	public String getId() {
     		return this.id;
     	}
      
     	@Override
     	public void putObject(Object key, Object value) {
     		if(value != null){
     			redisTemplate.opsForValue().set(key.toString(), jdkSerializer.serialize(value), 2, TimeUnit.DAYS);
     		}
     	}
      
     	@Override
     	public Object getObject(Object key) {
     		try {
     			if(key != null){
     				Object obj = redisTemplate.opsForValue().get(key.toString());
     				return jdkSerializer.deserialize((byte[])obj); 
     			}
     		} catch (Exception e) {
     			LOG.error("redis ");
     		}
     		return null;
     	}
      
     	@Override
     	public Object removeObject(Object key) {
     		try {
     			if(key != null){
     				redisTemplate.expire(key.toString(), 1, TimeUnit.SECONDS);
     			}
     		} catch (Exception e) {
     		}
     		return null;
     	}
      
     	@Override
     	public void clear() {
     		//jedis nonsupport
     	}
      
     	@Override
     	public int getSize() {
     		Long size = redisTemplate.getMasterRedisTemplate().execute(new RedisCallback<Long>(){
     			@Override
     			public Long doInRedis(RedisConnection connection)
     					throws DataAccessException {
     				return connection.dbSize();
     			}
     		});
     		return size.intValue();
     	}
      
     	@Override
     	public ReadWriteLock getReadWriteLock() {
     		return this.readWriteLock;
     	}
     }
     ```

  3. 二级缓存的实用
     我们需要将所有的实体类进行序列化，然后在Mapper中添加自定义cache功能。

     ```xml
     <cache
         type="org.andy.shop.cache.MybatisRedisCache"
         eviction="LRU"
         flushInterval="6000000"
         size="1024"
         readOnly="false"
         />
     ```

  4. Redis中的存储
          redis会自动的将Sql+条件+Hash等当做key值，而将查询结果作为value，只有请求中的所有参数都符合，那么就会使用redis中的二级缓存。其查询结果如下：	![image-20210428220414588](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210428220414588.png)

吞吐量（TPS）、QPS、并发数、响应时间（RT）

1. **吞吐量（TPS）：** 吞吐量是指系统在**单位时间内**处理请求的数量。

2. **QPS每秒查询率(Query Per Second) ：**每秒请求数，就是说服务器在一秒的时间内处理了多少个请求。

3. **并发数：**并发用户数是指系统可以同时承载的正常使用系统功能的用户的数量。

4. **响应时间(RT) ：**响应时间是指系统对请求作出响应的时间。

   计算关系：
   QPS = 并发量 / 平均响应时间
   并发量 = QPS * 平均响应时间

#### 单机的qps是多少？

单机的qps能达到十万+。

#### 怎么能够突破峰值qps？

##### 使用集群：

单机或者主从方式 qps 8 万左右
redis Cluster 集群方式 qps 上千万以上
redis 分布式方式使用 不常见 需要自己建立分发规则 较难！ qps 上千万以上

#### 怎么用单线程处理的请求？

使用多路复用机制处理并发。

#### 如果一直用get，你觉得达到qps的顶点，它的限制点在哪里？

#### 如何解决单机qps达到峰值的问题？

#### Redis集群是怎么获取值的？

#### 常用操作命令

#### 整个redis集群挂掉，业务服务应对方案（按redis作用讲）

#### 生产禁用命令以及原因，scan、unlink命令考察

#### 为什么java的位图比redis性能好？

#### 如果数据量很大java机器放不下呢？（分治法）