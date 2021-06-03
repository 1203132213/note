[TOC]



## 第一部分 MySQL体系架构

![image-20201112085332597](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201112085332597.png)

MySQL Server架构自顶向下大致可以分网络连接层、服务层、存储引擎层和系统文件层。

1. **网络连接层：**

   提供与MySQL服务器建立的支持。目前几乎支持所有主流的服务端编程技术，例如常见的 Java、C、Python、.NET等，它们通过各自API技术与MySQL建立连接。

2. **服务层：**

   MySQL Server的核心，分为六个部分：

   - **连接池：**负责存储和管理客户端与数据库的连接，一个线程负责管理一个连接。

   - **系统管理和控制工具：**例如备份恢复、安全管理、集群管理等

   - **SQL接口：**用于**接受**客户端发送的各种SQL命令，并且**返回**用户需要查询的结果。比如DML、DDL、存储过程、视图、触发器等。

     > ##### DML(Data Manipulation Language)数据操纵语言：
     >
     > 适用范围：对数据库中的数据进行一些简单操作，如insert,delete,update,select等.
     >
     > ##### DDL(Data Definition Language)数据定义语言：
     >
     > 适用范围：对数据库中的某些对象(例如，database,table)进行管理，如Create,Alter和Drop.

   - **解析器：**负责将请求的SQL解析生成一个"解析树"。然后根据一些MySQL规则进一步检查解析树是否合法

   - **查询优化器：**当“解析树”通过解析器语法检查后，将交由优化器将其转化成执行计划，然后与存储引擎交互。

     例：

     ```java
     select uid,name from user where gender=1;
     选取--》投影--》联接 策略
     1）select先根据where语句进行选取，并不是查询出全部数据再过滤
     2）select查询根据uid和name进行属性投影，并不是取出所有字段
     3）将前面选取和投影联接起来最终生成查询结果
     ```

   - **缓存：** 缓存机制是由一系列小缓存组成的。比如表缓存，记录缓存，权限缓存，引擎缓存等。如果查询缓存有命中的查询结果，查询语句就可以直接去查询缓存中取数据。

3. **存储引擎层：**

   负责MySQL中数据的存储与提取，与底层系统文件进行交互。MySQL存储引擎是插件式的，服务器中的查询执行引擎通过接口与存储引擎进行通信，**接口屏蔽了不同存储引擎之间的差异** 。现在有很多种存储引擎，各有各的特点，**最常见的是MyISAM和InnoDB**。

4. **系统文件层：**

   负责将数据库的数据和日志存储在文件系统之上，并完成与存储引擎的交互，是文件的物理存储层。主要包含日志文件，数据文件，配置文件，pid 文件，socket 文件等。

   - **日志文件：**

     - 错误日志：默认开启
     - 通用查询日志：记录一般查询语句
     - 二进制日志：记录了对MySQL数据库执行的更改操作，并且记录了语句的发生时间、执行时长；但是它不 记录select、show等不修改数据库的SQL。主要用于数据库恢复和主从复制。
     - **慢查询日志**：记录所有执行时间超时的查询SQL，默认是10秒。

   - **配置文件：**用于存放MySQL所有的配置信息文件，比如my.cnf、my.ini等。

   - **数据文件：**

     - db.opt 文件：记录库的默认使用的字符集和校验规则。
     - frm 文件：存储与表相关的元数据（meta）信息，包括表结构的定义信息等，每一张表都会 有一个frm 文件。
     - MYD 文件：**MyISAM 存储引擎专用**，存放 MyISAM 表的数据（data)，每一张表都会有一个 .MYD 文件。
     - MYI 文件：**MyISAM 存储引擎专用**，存放 MyISAM 表的索引相关信息，每一张 MyISAM 表对 应一个 .MYI 文件。
     - ibd文件和 IBDATA 文件：**存放 InnoDB 的数据文件（包括索引）**。InnoDB 存储引擎有两种表空间方式：**独享表空间和共享表空间。独享表空间使用 .ibd 文件来存放数据，且每一张 InnoDB 表对应一个 .ibd 文件。共享表空间使用 .ibdata 文件**，所有表共同使用一个（或多 个，自行配置）.ibdata 文件。
     - bdata1 文件：：系统表空间数据文件，存储表元数据、Undo日志等 。
     - ib_logfile0、ib_logfile1 文件：Redo log 日志文件。

   - **pid 文件：**pid 文件是 mysqld 应用程序在 Unix/Linux 环境下的一个进程文件，和许多其他 Unix/Linux 服务 端程序一样，它存放着自己的进程 id。

   - **socket 文件：**socket 文件也是在 Unix/Linux 环境下才有的，用户在 Unix/Linux 环境下客户端连接可以不通过 TCP/IP 网络而直接使用 Unix Socket 来连接 MySQL。

     

### MySQL运行机制

查询语句的执行流程：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210513223825812.png" alt="image-20210513223825812" style="zoom:80%;" />

![image-20201112194849709](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201112194849709.png)

1. **建立连接：**通过客户端/服务器通信协议与MySQL建立连接。MySQL 客户端与服务端的通信方式是 “ 半双工 ”。对于每一个 MySQL 的连接，时刻都有一个线程状态来标识这个连接正在做什么。

   通讯机制：

   -  全双工：能同时发送和接收数据，例如平时打电话。
   - 半双工：指的**某一时刻**，要么发送数据，要么接收数据，不能同时。例如早期对讲机 
   - 单工：只能发送数据或只能接收数据。例如单行道

2. **查询缓存：** **这是MySQL的一个可优化查询的地方，**如果开启了查询缓存且在查询缓存过程中查询到完全相同的SQL语句，则将查询结果直接返回给客户端；如果没有开启查询缓存或者没有查询到完全相同的 SQL 语句则会由解析器进行语法语义解析，并生成“解析树”。

3. **解析器：**将客户端发送的SQL进行语法解析，生成"解析树"。预处理器根据一些MySQL 规则进一步检查“解析树”是否合法，例如这里将检查数据表和数据列是否存在，还会解析名字和别名，看看它们是否有歧义，最后生成新的“解析树”。

   > #### 解析过程：
   >
   > ​	　将请求的sql生存一颗语法树。如：
   >
   > 　　select username from userinfo
   >
   > 　　先通过**词法分析：**
   >
   > 从左到右一个字符、一个字符地输入，然后根据构词规则识别单词。你将会生成4个Token,如下所示。"
   >
   >   ![面试官：说说一条查询sql的执行流程和底层原理？](http://p1.pstatp.com/large/pgc-image/a36c8c45d95b4cbb899bd2d40e95fba5)
   >
   > 　　接下来，进行**语法解析**，判断输入的这个 SQL 语句是否满足 MySQL 语法。然后生成下面这样一颗语法树：
   >
   > ![img](https://img2018.cnblogs.com/blog/785056/201904/785056-20190422222055065-1162512049.png)
   >
   > 　　如果语法不对，会报错。
   >
   
4. ##### 预处理器：

   根据一些mysql规则进一步检查解析树是否合法。如检查查询的表名、列名是否正确，是否有表的权限等。

5. **查询优化器：**根据“解析树”生成最优的执行计划。MySQL使用很多优化策略生成最优的执行计划，可以分为两类：静态优化（编译时优化）、动态优化（运行时优化）。 

   - 等价变换策略 
     - 5=5 and a>5 改成 a > 5 
     
     - a < b and a=5 改成b>5 and a=5 
     
     - 基于联合索引，调整条件位置等 
     
       如下表数据，id1、id2的索引是id1_key、id2_key：
     
       ![这里写图片描述](https://img-blog.csdn.net/20160528145202615)
     
       查询语句是：select * from t8 where id1=1 and id2=0;
     
       优化器会先分析数据表，得知有索引id1_key与id2_key，如果先判断id1_key的话，然后需要从**4行数据中排除3行**数据；如果先判断id2_key的话，然后需要从**2行中排除1行**。对人来说，这两种方式没有什么区别，但是对于程序而言，先判断id2_key需要较少的计算和磁盘输入输出。因此，查询优化器会规定程序，先去检验id2_key索引，然后在从中挑出id2为0的数据行。
   - 优化count、min、max等函数 
     - InnoDB引擎min函数只需要找索引最左边 
     - InnoDB引擎max函数只需要找索引最右边 
     - MyISAM引擎count(*)，不需要计算，直接返回 
   - 提前终止查询 
     
     - 使用了limit查询，获取limit所需的数据，就不在继续遍历后面数据 
   - in的优化 
     
     - MySQL对in查询，**会先进行排序，再采用二分法查找数据**。比如where id in (2,1,3)，变成 in (1,2,3) 

6. **执行引擎：**负责执行 SQL 语句，此时查询执行引擎会根据 SQL 语句中表的存储引擎类型，以及对应的API接口与底层存储引擎缓存或者物理文件的交互，得到查询结果并返回给客户端。若开启用查询缓存，这时会将SQL 语句和结果完整地保存到查询缓存（Cache&Buffer）中，以后若有相同的 SQL 语句执行则直接返回结果。 

   - 如果开启了查询缓存，先将查询结果做缓存操作 
   - 返回结果过多，采用增量模式返回


### 执行update语句时的内部流程： 

```sql
update T set c=c+1 where ID=2;
```

1. 执行器先找引擎取 ID=2 这一行。ID 是主键，引擎直接用树搜索找到这一行。如果 ID=2这一行所在的数据页本来就在内存中，就直接返回给执行器；否则，**需要先从磁盘读入内存，然后再返回**。

2. 执行器拿到引擎给的行数据，把这个值加上 1，比如原来是 N，现在就是 N+1，得到新的一行数据，再调用引擎接口写入这行新数据。

3. 引擎将这行新数据更新到内存中，同时将这个更新操作记录到 redo log 里面，此时 redolog 处于 prepare 状态。然后告知执行器执行完成了，随时可以提交事务。

4. 执行器生成这个操作的 binlog，并把 binlog 写入磁盘。

5. 执行器调用引擎的提交事务接口，引擎把刚刚写入的 redo log 改成提交（commit）状态，更新完成。这里我给出这个 update 语句的执行流程图，图中浅色框表示是在 InnoDB 内部执行的，深色框表示是在执行器中执行的。

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210324095922398.png" alt="image-20210324095922398" style="zoom:50%;" />

   图中数据页不在内存中，还有可能走change buffer，不过仅限于非聚集非唯一索引（因为其他索引需要判断，比如唯一性判断）。



### MySQL存储引擎

#### 存储引擎简介和种类：

存储引擎在MySQL的体系架构中位于第三层，**负责MySQL中的数据的存储和提取**，是与文件打交道的子系统，它是根据MySQL提供的文件访问层抽象接口定制的一种文件访问机制，这种机制就叫作存储引擎。

存储引擎种类：（主要是使用InnoDB和MyISAM）

- **InnoDB：支持事务，具有提交，回滚和崩溃恢复能力，事务安全** 
- **MyISAM：不支持事务和外键，访问速度快** 
- Memory：利用内存创建表，访问速度非常快，因为数据在内存，而且默认使用Hash索引，但是 一旦关闭，数据就会丢失 
- Archive：归档类型引擎，仅能支持insert和select语句 
- Csv：以CSV文件进行数据存储，由于文件限制，所有列必须强制指定not null，另外CSV引擎也不支持索引和分区，适合做数据交换的中间表 
- BlackHole: 黑洞，只进不出，进来消失，所有插入数据都不会保存 
- Federated：可以访问远端MySQL数据库中的表。一个本地表，不保存数据，访问远程表内容。 
- MRG_MyISAM：一组MyISAM表的组合，这些MyISAM表必须结构相同，Merge表本身没有数据， 对Merge操作可以对一组MyISAM表进行操作。

####  InnoDB和MyISAM对比：

- 事务和外键

  InnoDB支持事务和外键，具有安全性和完整性，**适合大量insert或update操作**

  MyISAM不支持事务和外键，它提供高速存储和检索，**适合大量的select查询操作**

- 锁机制

  InnoDB**支持行级锁**，锁定指定记录。基于索引来加锁实现。 

  MyISAM**支持表级锁**，锁定整张表。

- 索引结构

  InnoDB使用聚集索引（聚簇索引），索引和记录在一起存储，**既缓存索引，也缓存记录**。 

  MyISAM使用非聚集索引（非聚簇索引），**索引和记录分开。**

- 并发处理能力

  InnoDB**读写阻塞可以与隔离级别有关，可以采用多版本并发控制（MVCC）来支持高并发。**

  MyISAM**使用表锁，会导致写操作并发率低，读之间并不阻塞，读写阻塞**。 

- 存储文件

  InnoDB表对应两个文件，一个.frm表结构文件，一个.ibd数据文件。InnoDB表最大支持64TB； 

  MyISAM表对应三个文件，一个.frm表结构文件，一个MYD表数据文件，一个.MYI索引文件。从 MySQL5.0开始默认限制是256TB。

**InnoDB和MyISAM适用场景：** 

MyISAM 

- 不需要事务支持（不支持） 
- 并发相对较低（锁定机制问题） 
- 数据修改相对较少，以读为主 
- 数据一致性要求不高 

InnoDB 

- 需要事务支持（具有较好的事务特性） 
- 行级锁定对高并发有很好的适应能力 
- 数据更新较为频繁的场景 
- 数据一致性要求较高 
- 硬件设备内存较大，可以利用InnoDB较好的缓存能力来提高内存利用率，减少磁盘IO 

**总结：** 

两种引擎该如何选择？ 

- 是否需要事务？有，InnoDB 
- 是否存在并发修改？有，InnoDB 
- 是否追求快速查询，且数据修改少？是，MyISAM 
- 在绝大多数情况下，推荐使用InnoDB

**Innodb使用的是哪种隔离级别呢?**

InnoDB默认使用的是可重复读隔离级别。

###  InnoDB存储结构

#### InnoDB存储结构介绍和结构图：

从MySQL 5.5版本开始默认使用InnoDB作为引擎，它擅长处理事务，具有自动崩溃恢复的特性，下面是官方的InnoDB引擎架构图，主要分为内存结构和磁盘结构两大部分。

![image-20201113110736443](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201113110736443.png)

#### InnoDB存储结构-内存结构：

1. **Adaptive Hash Index：**自适应哈希索引，用于优化对BP数据的查询。<span style="color:#4169E1;">**InnoDB存储引擎会监控对表索引的查找，如果观察到建立哈希索引可以带来速度的提升，则建立哈希索引**，</span>所以称之为自适应。<span style="color:#4169E1;">**InnoDB存储引擎会自动根据访问的频率和模式来为某些页建立哈希索引。**</span>

2. **Buffer Pool：**缓冲池，简称BP。BP以Page页为单位，默认大小16K，BP的底层采用**链表数据结构**管理Page。**在InnoDB访问表记录和索引时会在Page页中缓存**，以后使用可以减少磁盘IO操作，提升效率。

   - ##### Page讲解：

     ##### Page根据状态可以分为三种类型： 

     - free page ： 空闲page，未被使用 
     - clean page：被使用page，数据没有被修改过 
     - dirty page：脏页，被使用page，数据被修改过，页中数据和磁盘的数据产生了不一致 

   - **针对上述三种page类型，InnoDB通过三种链表结构来维护和管理** 
     
     - free list ：表示空闲缓冲区，管理free page 
     - flush list：表示需要刷新到磁盘的缓冲区，管理dirty page，内部page按修改时间排序。脏页即存在于flush链表，也在LRU链表中，但是两种互不影响，**LRU链表负责管理page的可用性和释放，而flush链表负责管理脏页的刷盘操作。** 
     - lru list（内部使用：LRU--内存淘汰算法）：表示正在使用的缓冲区，**管理clean page和dirty page**，缓冲区以 midpoint为基点，前面链表称为new列表区，存放经常访问的数据，占63%；后面的链表称为old列表区，存放使用较少数据，占37%。
     
     ​	每当有新的page数据读取到buffer pool时，InnoDb引擎会判断是否有空闲页，是否足够，如果有就将空闲页从free list列表删除，放入到LRU列表中。没有空闲页，就会根据LRU算法淘汰LRU链表默认的页，将内存空间释放分配给新的页。
     
     ![image-20210420202317621](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210420202317621.png)
     
   - **Buffer Pool配置参数** 

     show variables like '%innodb_page_size%'; //查看page页大小 

     show variables like '%innodb_old%'; //查看lru list中old列表参数 

     show variables like '%innodb_buffer%'; //查看buffer pool参数 

     **建议：**

     将innodb_buffer_pool_size设置为总内存大小的60%-80%，
   
      innodb_buffer_pool_instances可以设置为多个，这样可以避免缓存争夺。
   
3. **Change Buffer：**写缓冲区，简称CB。当更新一条记录时，该记录在BufferPool存在，直接在BufferPool修改，一次内存操作。如果该记录在BufferPool不存在（没有命中），会直接在ChangeBuffer进行一次内存操作，不用再去磁盘查询数据，避免一次磁盘IO。当下次查询记录时，会先进行磁盘读取，然后再从 ChangeBuffer中读取信息合并，最终载入BufferPool中。

   ​	ChangeBuffer占用BufferPool空间，默认占25%，最大允许占50%，可以根据读写业务量来进行调整。参数innodb_change_buffer_max_size;

   ## Change Buffer详解：

   #### 简介：

   Change Buffer是一种特殊的缓存结构，用来缓存不在Buffer Pool中的辅助索引页， 支持insert, update,delete(DML)操作的缓存(注意，这个在MySQL5.5之前叫做Insert Buffer，仅支持insert操作的缓存)。当这些数据页被其他查询加载到Buffer Pool后，则会将数据进行merge到索引数据叶中。

   ![img](https:////upload-images.jianshu.io/upload_images/24613101-5aa1ff81e8f18eed.png?imageMogr2/auto-orient/strip|imageView2/2/w/527/format/webp)

   InnoDB在进行DML操作非聚集非唯一索引时，会先判断要操作的数据页是不是在Buffer Pool中，如果不在就会先放到Change Buffer进行操作，然后再以一定的频率将数据和辅助索引数据页进行merge。这时候通常都能将多个操作合并到一次操作，减少了IO操作，尤其是辅助索引的操作大部分都是IO操作，可以大大提高DML性能。

   如果Change Buffer中存储了大量的数据，那么可能merge操作会需要消耗大量时间。

   #### Change Buffer的工作流程：

   ​	当发生DML操作时一条记录时，该记录在BufferPool存在，直接在BufferPool修改，一次内存操作。如果该记录在BufferPool不存在（没有命中），会直接在ChangeBuffer进行一次内存操作，不用再去磁盘查询数据，避免一次磁盘IO。当下次查询记录时，会先进行磁盘读取，然后再从 ChangeBuffer中读取信息合并，最终载入BufferPool中（还有另外两种情况会更新到磁盘，详情参考下面）。

   ### 什么时候将Change buffer中的数据更新到磁盘中？

   1. 当下一次查询命中这个数据页的时候，会先从磁盘中读取数据页到内存中，然后先执行Change buffer的merge操作，保证数据逻辑的正确性。
   2. 除了查询操作外，系统有后台线程会定期merge。
   
   3. 数据库正常关闭(shutdown)的时候，也会进行merge操作。
   
   #### 为什么Change Buffer只能针对非聚集非唯一索引：
   
   因为如果是主键索引或者唯一索引，需要唯一性校验，而唯一性校验需要操作查询磁盘，做一次io操作，会直接将记录查询到BufferPool中，然后在缓冲池修改，不会在 ChangeBuffer操作。
   
   #### 以下几种情况开启 Change Buffer，会使得 MySQL 数据库明显提升：
   
   1、数据库大部分是非唯一索引
   
   2、业务是写多读少
   
   3、写入数据之后并不会立即读取它
4. **Log Buffer：**日志缓冲区，用来保存要写入磁盘上log文件（Redo/Undo）的数据，**日志缓冲区的内容定期刷新到磁盘log文件中。日志缓冲区满时会自动将其刷新到磁盘**，当遇到BLOB 或多行更新的大事务操作时，增加日志缓冲区可以节省磁盘I/O（通过将innodb_log_buffer_size参数调大）。



#### InnoDB磁盘结构：

![image-20201113110736443](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201113110736443.png)

InnoDB磁盘主要包含表空间、数据字典、双写缓冲区、重做日志 、撤销日志。

- 表空间（Tablespaces）：用于存储表结构和数据。表空间又分为系统表空间、独立表空间、 通用表空间、临时表空间、Undo表空间等多种类型；

  - 系统表空间（The System Tablespace）：包含InnoDB Data Dictionary、Doublewrite Buffer、Change Buffer、Undo Logs的存储区域。也默认包含任何用户在系统表空间创建的表数据和索引数据。
  - 独立表空间（File-Per-Table Tablespaces）：独立表空间是一个单表表空间，该表创建于自己的数据文件，而非创建于系统表空间中。
  - 通用表空间（General Tablespaces）：通用表空间为通过create tablespace语法创建的共享表空间。通用表空间可以创建于 mysql数据目录外的其他表空间，其可以容纳多张表，且其支持所有的行格式。
  - 撤销表空间（Undo Tablespaces）：间由一个或多个包含Undo日志文件组成。在MySQL 5.7版本之前Undo占用的 是System Tablespace共享区，从5.7开始将Undo从System Tablespace分离了出来。
  - 临时表空间（Temporary Tablespaces）：存储的是用户创建的临时表和磁盘内部的临时表，mysql服务 器正常关闭或异常终止时，临时表空间将被移除，每次启动时会被重新创建。

- 数据字典（InnoDB Data Dictionary）：

  InnoDB数据字典由内部系统表组成，这些表包含用于查找表、索引和表字段等对象的元数据。由于历史原因，数据字典元数据在一定程度上 与InnoDB表元数据文件（.frm文件）中存储的信息重叠。

- 双写缓冲区（Doublewrite Buffer）：

  位于系统表空间，是一个存储区域。<span style="color:#4169E1;">**在BufferPool的page页刷新到磁盘真正的位置前，会先将数据存在Doublewrite 缓冲区**。**如果在page页写入过程中出现操作系统、存储子系统或 mysqld进程崩溃，InnoDB可以在崩溃恢复期间从Doublewrite 缓冲区中找到页面的备份。**</span>在大多数情况下，默认情况下启用双写缓冲区，要禁用Doublewrite 缓冲区，可以将 innodb_doublewrite设置为0。使用Doublewrite 缓冲区时建议将innodb_flush_method设 置为O_DIRECT。

- 撤销日志（Undo Logs）：

  **Undo Logs介绍：**

  数据库事务开始之前，<span style="color:#4169E1;">**会将要修改的记录存放到 Undo 日志里**</span>，当事务回滚时或者数据库崩溃时，可以利用 Undo 日志，返回指定某个状态操作，<span style="color:#4169E1;">**撤销未提交事务对数据库产生的影响**</span>（相当于git的revert --hard）。

  **Undo Logs产生和销毁：**

  Undo Log在事务开始前产生；事务在提交时，并不会立刻删除undo log，innodb会将该事务对应的Undo log放入到删除列表中，后面会通过后台线程purge thread进行回收处理。

  **Undo Logs记录的内容：**

  <span style="color:#4169E1;">**Undo Logs属于逻辑日志，记录一个变化过程，例如执行一个delete，undolog会记 录一个insert；执行一个update，undolog会记录一个相反的update。**</span>（记录的是反向操作，但是它的记录格式远比一条逆向SQL复杂得多！！要处理的问题也要复杂，例如insert时候生成的索引，delete和update时候的索引重排。）

  **Undo Logs作用：**

  1. 实现事务回滚操作：如果出现了错误或者用户执行了 ROLLBACK 语句，MySQL 可以利用 Undo Log 中的备份将数据恢复到事务开始之前的状态。（保证事务的原子性）
2. 实现多版本并发控制（MVCC）：保存了未提交之前的版本数据，Undo Log 中的数据可作为数据旧版本快照供其他并发事务进行快照读。

- Redo Log和Binlog：

  Redo Log：（InnoDB引擎所特有的日志）
  
  - **简介：**重做日志，在数据库发生意外时重现操作
  - **生成时机：**事务操作执行时生成，事务提交时写入，先写入到Log Buffer，而不是立刻写入磁盘。
  - **Redo Log覆盖写入时机：**等事务操作的脏页写入到磁盘之后，Redo Log 的使命也就完成了，<span style="color:#4169E1;">**Redo Log占用的空间就可以重用。**</span>
  - **Redo Log工作原理：** Redo Log 是为了实现事务的持久性而出现的产物。防止在发生故障的时间点，尚有脏页未写入表 的 IBD 文件中，在重启 MySQL 服务的时候，根据 Redo Log 进行重做，从而达到事务的未入磁盘 数据进行持久化这一特性。
  - **Redo Log写入机制：**Redo Log 文件内容是以顺序循环的方式写入文件，写满时则回溯到第一个文件，进行覆盖写。
  - **删除机制：**见binlog、redolog、undolog详解。
  
  BinLog：（MySQL Server自己的日志）
  
  - **简介：**Binlog是记录所有数据库表结构变更以及表数据修改的二进制日志，不会记录SELECT和SHOW这类操作。Binlog日志是以事件形式记录，还包含语句所执行的消耗时间。
  
  - **使用场景：**
  
    1. 主从复制：在主库中开启Binlog功能，这样主库就可以把Binlog传递给从库，从库拿到 Binlog后实现数据恢复达到主从数据一致性。 
    2. 数据恢复：通过mysqlbinlog工具来恢复数据。
  
  - **文件记录模式：**
  
    1. ROW（row-based replication, RBR）：日志中会记录每一行数据被修改的情况，然后在 slave端对相同的数据进行修改。 
  
       优点：能清楚记录每一个行数据的修改细节，能完全实现主从数据同步和数据的恢复。 
  
       缺点：批量操作，会产生大量的日志，尤其是alter table会让日志暴涨。
  
    2. STATMENT（statement-based replication, SBR）：每一条被修改数据的SQL都会记录到 master的Binlog中，slave在复制的时候SQL进程会解析成和原来master端执行过的相同的 SQL再次执行。简称SQL语句复制。 
  
       优点：日志量小，减少磁盘IO，提升存储和恢复速度 
  
       缺点：在某些情况下会导致主从数据不一致，比如last_insert_id()、now()等函数
  
    3. MIXED（mixed-based replication, MBR）：以上两种模式的混合使用，一般会使用 STATEMENT模式保存binlog，对于STATEMENT模式无法复制的操作使用ROW模式保存 binlog，MySQL会根据执行的SQL语句选择写入模式。
  
  - **写入机制：**
  
    1. 根据记录模式和操作触发event事件生成log event（事件触发执行机制） 
    2. 将事务执行过程中产生log event写入缓冲区，每个事务线程都有一个缓冲区 Log Event保存在一个binlog_cache_mngr数据结构中，在该结构中有两个缓冲区，一个是 stmt_cache，用于存放不支持事务的信息；另一个是trx_cache，用于存放支持事务的信息。 
    3. 事务在提交阶段会将产生的log event写入到外部binlog文件中。<span style="color:#4169E1;"> **不同事务以串行方式将log event写入binlog文件中，所以一个事务包含的log event信息在 binlog文件中是连续的，中间不会插入其他事务的log event。**</span>
  
  - **删除机制：**可以通过设置expire_logs_days参数来启动自动清理功能。默认值为0表示没启用。设置为1表示超 出1天binlog文件会自动删除掉。
  
- Redo Log和Binlog区别

  1. Redo Log是属于InnoDB引擎功能，Binlog是属于MySQL Server自带功能，并且是以二进制文件记录。
  2. Redo Log属于物理日志，记录该数据页更新状态内容，Binlog是逻辑日志，记录更新过程。
  3. Redo Log日志是循环写，日志空间大小是固定，Binlog是追加写入，写完一个写下一个，不会覆盖使用。
  4. Redo Log作为服务器异常宕机后事务数据自动恢复使用，Binlog可以作为主从复制和数据恢复使用。Binlog没有自动crash-safe能力。（crash-safe：主要体现在事务执行过程中突然奔溃，重启后能保证事务完整性，已提交的数据不会丢失，未提交完整的数据会自动进行回滚。这个能力依赖的就是<span style="color:#4169E1;"> **redo log和undo log两个日志。**</span>）

####  InnoDB线程模型：

1. IO Thread

   在InnoDB中使用了大量的AIO（Async IO）来做读写处理，这样可以极大提高数据库的性能。在 InnoDB1.0版本之前共有4个IO Thread，分别是write，read，insert buffer和log thread，后来 版本将read thread和write thread分别增大到了4个，一共有10个了。

   - read thread ： 负责读取操作，将数据从磁盘加载到缓存page页。4个
   - write thread：负责写操作，将缓存脏页刷新到磁盘。4个
   - log thread：负责将日志缓冲区内容刷新到磁盘。1个
   - insert buffer thread ：负责将写缓冲内容刷新到磁盘。1个

2. Purge Thread

   事务提交之后，其使用的undo日志将不再需要，因此需要Purge Thread回收已经分配的undo 页。

3. Page Cleaner Thread

   作用是将脏数据刷新到磁盘，脏数据刷盘后相应的redo log也就可以覆盖，即可以同步数据，又能 达到redo log循环使用的目的。会调用write thread线程处理。

4. Master Thread

   Master thread是InnoDB的主线程，负责调度其他各线程，优先级最高。作用是将缓冲池中的数据异步刷新到磁盘 ，保证数据的一致性。包含：脏页的刷新（page cleaner thread）、undo页 回收（purge thread）、redo日志刷新（log thread）、合并写缓冲等。内部有两个主处理，分别是每隔1秒和10秒处理。

   每1秒的操作：

   - 刷新日志缓冲区，刷到磁盘 
   - 合并写缓冲区数据，根据IO读写压力来决定是否操作 
   - 刷新脏页数据到磁盘，根据脏页比例达到75%才操作（innodb_max_dirty_pages_pct， innodb_io_capacity）

   每10秒的操作：

   - 刷新脏页数据到磁盘 
   - 合并写缓冲区数据 
   - 刷新日志缓冲区 
   - 删除无用的undo页

### 新版本结构演变：

#### MySQL 5.7 版本

- 将 Undo日志表空间从共享表空间 ibdata 文件中分离出来，可以在安装 MySQL 时由用户自行指定文件大小和数量。 
- 增加了 temporary 临时表空间，里面存储着临时表或临时查询结果集的数据。 
- Buffer Pool 大小可以**动态修改**，无需重启数据库实例。 

> **temporary 临时表：**
>
> 临时表只在当前连接可见，当关闭连接时，Mysql会自动删除表并释放所有空间
>
> **创建临时表命令：** 
>
> ```java
> CREATE TEMPORARY TABLE XXX
> ```
>
> **临时表使用场景：**
>
> 1. 将两个子查询的结果进行合并：记录不在临时表中则插入，记录已存在则取下一条记录。
> 2. 分组并统计每组记录个数：如果记录不存在则插入临时表，否则累计计数。

##### 1) 随机 root 密码

MySQL 5.7 数据库初始化完成后，会自动生成一个 root@localhost 用户，root 用户的密码不为空，而是随机产生一个密码。

##### 2) 自定义 test 数据库

MySQL 5.7 默认安装完成后没有 test 数据库。用户可以自行创建 test 数据库并对其进行权限控制。

##### 3) 默认 SSL 加密

MySQL 5.7 采用了更加简单的 SSL 安全访问机制，默认连接使用 SSL 的加密方式。

##### 4) 密码过期策略

MySQL 5.7 支持用户设置密码过期策略，要求用户在一定时间过后必须修改密码。

##### 5) 用户锁

MySQL 5.7 为管理员提供了暂时禁用某个用户的功能，使被锁定的用户无法访问和使用数据库。

##### 6) 全面支持JSON

随着非结构化数据存储需求的持续增长，各种非结构化数据存储的数据库应运而生（如 [MongoDB](http://c.biancheng.net/mongodb/)），各大关系型数据库也不甘示弱，纷纷提供对 JSON 的支持，以应对非结构化数据库的挑战。

MySQL 5.7 也提供了对 JSON 的支持，在服务器端提供了一组便于操作 JSON 的函数。存储的方法是将 JSON 编码成 BLOB 后再由存储引擎进行处理。这样，MySQL 就同时拥有了关系型数据库和非关系型数据库的优点，并且可以提供完整的事务支持。

##### 7) 支持两类生成列（generated column）

生成列是通过数据库中的其他列计算得到的一列。当为生成列创建索引时，可以便捷地加快查询速度。MySQL 5.7 支持虚拟生成列和存储生成列。虚拟生成列仅将数据保存在表的元数据中，作为缺省的生成列类型；存储生成列则是将数据永久保存在磁盘上，需要更多的磁盘空间。

##### 8) 引入系统库（sys schema）

系统库中包含一系列视图、函数和存储过程，通过多线程、多进程、组合事务提交和基于行的优化方式将复制功能提高 5 倍以上，用户向外扩充其跨商品系统的工作负载时，得以大幅提升复制的效能和效率。

#### MySQL 8.0 版本

- 将InnoDB表的数据字典和Undo都从共享表空间ibdata中彻底分离出来了，以前需要 ibdata中数据字典与独立表空间ibd文件中数据字典一致才行，8.0版本就不需要了。 

- temporary 临时表空间也可以配置多个物理文件，而且均为 InnoDB 存储引擎并能创建索引，这样加快了处理的速度。 

- 用户可以像 Oracle 数据库那样设置一些表空间，每个表空间对应多个物理文件，每个表空间可以给多个表使用，但一个表只能存储在一个表空间中。 

- 将Doublewrite Buffer从共享表空间ibdata中也分离出来了。

- 账户与安全：

  - ##### 第一个变更：

    mysql5.7创建用户和授权使用一个语句就可以完成：

    ```java
    grant all privileges on *.* to 'tony'@'%' identified by 'Tony@2019';
    ```

    mysq8.0将创建用户和授权分开（为了不产生歧义，语句更明确）：

    1. 创建用户：

       ```java
       create user 'tony'@'%' identified by 'Tony@2019';
       ```

    2. 用户授权

       ```java
       grant all privileges on *.* to 'tony'@'%';
       ```

  - ##### 第二个变更：

    认证插件的更新，由5.7版本的mysql_native_password插件改为caching_sha2_password，caching_sha2_password更加安全，性能也更好。

    mysql8.0想修改成以前的插件mysql_native_password的办法：

    1. 修改配置文件：

       修改/etc/my.cnf文件，把下图注释去掉即可

       ![image-20210214161829144](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210214161829144.png)

    2. 使用命令修改：

       ```java
       alter user 'tony'@'%' identified with mysql_native_password by 'Tony@2019'
       ```

  - ##### 第三个变更：

    新增密码管理：

    ​	password_history：修改密码不允许与最近几次使用或的密码重复，默认是0，即不限制         
    ​	password_reuse_interval ：修改密码不允许与最近多少天的使用过的密码重复，默认是0,即不限制
    ​	password_require_current： 修改密码是否需要提供当前的登录密码，默认是OFF,即不需要；如果需要，则设置成ON

  - ##### 第四个变更：

    一组权限赋予某个角色，再把某个角色赋予某个用户，那用户就拥有角色对应的权限

- 优化器索引：

- 通用表表达式：

- 窗口函数：

- InnoDB 增强：

- JSON 增强：

  ##### 8.0使用内联路径操作符 ：

  - 使用前后的语句对比：

    ```java
    使用后：
    JSON_UNQUOTE(column -> path)
    使用前：
    JSON_UNQUOTE(JSON_EXTRACT(column,path))
    ```

  


## 第二部分 MySQL索引原理

### 第1节 索引类型

索引可以提升查询速度，会影响where查询，以及order by排序。MySQL索引类型如下：

- 从索引存储结构划分：B Tree索引、Hash索引、FULLTEXT全文索引、R Tree索引 
- 从应用层次划分：普通索引、唯一索引、主键索引、复合索引 
- 从索引键值类型划分：主键索引、辅助索引（二级索引） 
- 从数据存储和索引键值逻辑关系划分：聚集索引（聚簇索引、覆盖索引）、非聚集索引（非聚簇索引、非覆盖索引）

#### 1.1 普通索引

这是最基本的索引类型，基于普通字段建立的索引，<span style="color:#4169E1;">**没有任何限制**。</span> 创建普通索引的方法如下：

- CREATE INDEX <索引的名字> ON tablename (字段名); 
- ALTER TABLE tablename ADD INDEX [索引的名字] (字段名);
- CREATE TABLE tablename ( [...], INDEX [索引的名字] (字段名) );

#### 1.2 唯一索引

与"普通索引"类似，不同的就是：索引字段的值必须唯一，<span style="color:#4169E1;">**但允许有空值 （普通索引也可以）**</span>。在创建或修改表时追加唯一约束，就会**自动**创建对应的唯一索引。

创建唯一索引的方法如下：

- CREATE UNIQUE INDEX <索引的名字> ON tablename (字段名);
- ALTER TABLE tablename ADD UNIQUE INDEX [索引的名字] (字段名);
- CREATE TABLE tablename ( [...], UNIQUE [索引的名字] (字段名) ;

#### 1.3 主键索引

它是一种特殊的唯一索引，不允许有空值。在创建或修改表时追加主键约束即可，每个表只能有一个主键。 创建主键索引的方法如下：

- CREATE TABLE tablename ( [...], PRIMARY KEY (字段名) ); 
- ALTER TABLE tablename ADD PRIMARY KEY (字段名);

#### 1.4 复合索引

单一索引是指索引列为一列的情况，即新建索引的语句只实施在一列上；<span style="color:#4169E1;">**用户可以在多个列上建立索引，这种索引叫做组复合索引（组合索引）**</span>。复合索引可以代替多个单一索引，相比多个单一索引复合索引所需的开销更小。

索引同时有两个概念叫做窄索引和宽索引，窄索引是指索引列为**1-2列**的索引，宽索引也就是索引列超过2列的索引，设计索引的一个重要原则就是**能用窄索引不用宽索引**，因为窄索引往往比组合索引更有效。

创建组合索引的方法如下：

- CREATE INDEX <索引的名字> ON tablename (字段名1，字段名2...); 
- ALTER TABLE tablename ADD INDEX [索引的名字] (字段名1，字段名2...); 
- CREATE TABLE tablename ( [...], INDEX [索引的名字] (字段名1，字段名2...) );

复合索引使用注意事项： 

- 何时使用复合索引，要根据where条件建索引，**注意不要过多使用索引，过多使用会对更新操作效率有很大影响**。
-  如果表已经建立了(col1，col2)，就没有必要再单独建立（col1）；如果现在有(col1)索引，如果查询需要col1和col2条件，可以建立(col1,col2)复合索引，对于查询有一定提高。

复合索引使用场景：

比如select * from lagou where a==1 and b==3 and c ==4;

这种查询如果a、b、c一起建立了复合索引查询起来就会很快。

#### 1.5 全文索引

​	查询操作在数据量比较少时，可以使用like模糊查询，但是对于大量的文本数据检索，效率很低。如果使用全文索引，查询速度会比like快很多倍。在MySQL 5.6 以前的版本，只有MyISAM存储引擎支持全文索引，从MySQL 5.6开始MyISAM和InnoDB存储引擎均支持。

创建全文索引的方法如下： 

- CREATE <span style="color:red">FULLTEXT</span> INDEX <索引的名字> ON tablename (字段名); 
- ALTER TABLE tablename ADD FULLTEXT [索引的名字] (字段名); 
- CREATE TABLE tablename ( [...], FULLTEXT KEY [索引的名字] (字段名) ;

和常用的like模糊查询不同，全文索引有自己的语法格式，使用 match 和 against 关键字，比如

```java
select * from user where match(name) against('aaa');
```

全文索引使用注意事项： 

- 全文索引必须在字符串、文本字段上建立。 

- 全文索引字段值必须在最小字符和最大字符之间的才会有效。（innodb：3-84；myisam：4- 84具体范围可以在配置文件修改，命令行配置会报只可读） 

- 全文索引字段值要进行切词处理，按syntax字符进行切割，例如b+aaa，切分成b和aaa

  syntax字符：

  ```xml
  ft_boolean_syntax =+ -><()~*:""&|'
  ```

比如：baaa、caaa你直接搜索select * from user where match(name) against('aaa');是搜不到的，但是你用syntax字符'+',比如b+aaa、c+aaa,再使用上面的查询语句就能搜索到。

- 全文索引匹配查询，默认使用的是等值匹配，例如a匹配a，不会匹配ab,ac。如果想匹配可以在布尔模式下搜索a*

```java
select * from user where match(name) against('a*' in boolean mode);
```

如下图，查询aaa不能把aaaa查出来，而使用in boolean mode可以把所有aaa的模糊结果都查出来。

![image-20210209004016597](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210209004016597.png)

#### 全文索引和普通索引有什么区别？

- 普通索引的结构主要以B+树和哈希索引为主，用于实现对字段中数据的精确查找，比如查找某个字段值等于给定值的记录，A=10这种查询，因此适合数值型字段和短文本字段。
- 全文索引是用于检索字段中是否包含或不包含指定的关键字，有点像搜索引擎的功能，其内部的索引结构采用的是与搜索引擎相同的**倒排索引**结构，其原理是对字段中的**文本进行分词**，然后为每一个出现的单词记录一个索引项，这个索引项中保存了所有出现过该单词的记录的信息，也就是说在索引中找到这个单词后，就知道哪些记录的字段中包含这个单词了。因此适合用大文本字段的查找（**比如模糊查询**）。

#### 什么是倒排索引？

对于模糊查询传统索引就是通过一个个查询。

倒排索引模糊查询的方法是：

![image-20210420110736591](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210420110736591.png)

如上图所示，纵坐标代表词汇分类，横坐标代表文档分类，表中的数字记录词汇在文档出现的个数（如果为0次不记录，不然记的数量太多了），然后模糊查询，直接查询词汇，然后遍历相应的文档进行查询。

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210514100207242.png" alt="image-20210514100207242" style="zoom:80%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210514104211758.png" alt="image-20210514104211758" style="zoom:80%;" />

###  第2节 索引原理

MySQL官方对索引定义：是存储引擎用于快速查找记录的一种数据结构。<span style="color:#4169E1;">**需要额外开辟空间和数据维护工作。**</span>

- <span style="color:#4169E1;">**索引是物理数据页存储，在数据文件中（InnoDB，ibd文件），利用数据页(page)存储。**</span>
-  索引可以加快检索速度，但是同时也会降低增删改操作速度，索引维护需要代价。

<span style="color:#4169E1;">**索引涉及的理论知识：二分查找法、Hash和B+Tree。**</span>

#### 2.1 二分查找法

二分查找法也叫作折半查找法，它是在有序数组中查找指定数据的搜索算法。它的优点是等值查询、范围查询性能优秀，<span style="color:#4169E1;">**缺点是更新数据、新增数据、删除数据维护成本高。**</span>

- 首先定位left和right两个指针
-  计算(left+right)/2 
- 判断除2后索引位置值与目标值的大小比对 
- 索引位置值大于目标值就-1，right移动；如果小于目标值就+1，left移动

举个例子，下面的有序数组有17 个值，查找的目标值是7，过程如下：

![image-20201126150432540](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126150432540.png)





![image-20201126150442972](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126150442972.png)



![image-20201126150504309](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126150504309.png)



#### 2.2 Hash结构

Hash底层实现是由Hash表来实现的，是根据键值存储数据的结构。非常适合根据key查找value值，也就是单个key查询，或者说等值查询。其结构如下所示：

![image-20201126150529024](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126150529024.png)

从上面结构可以看出，Hash索引可以方便的提供等值查询，但是对于范围查询就需要全表扫描了。 Hash索引在MySQL 中Hash结构主要应用在Memory原生的<span style="color:#4169E1;">**Hash索引 、InnoDB 自适应哈希索引。**</span>

InnoDB提供的自适应哈希索引功能强大，接下来重点描述下InnoDB 自适应哈希索引。

InnoDB自适应哈希索引是为了提升查询效率，InnoDB存储引擎会监控表上各个索引页的查询，<span style="color:#4169E1;">**当 InnoDB注意到某些索引值访问非常频繁时，会在内存中基于B+Tree索引再创建一个哈希索引，使得内存中的 B+Tree 索引具备哈希索引的功能，即能够快速定值访问频繁访问的索引页。**</span>

InnoDB自适应哈希索引：在使用Hash索引访问时，一次性查找就能定位数据，<span style="color:#4169E1;">**等值查询效率要优于 B+Tree**。</span>

自适应哈希索引的建立使得InnoDB存储引擎能自动根据索引页访问的频率和模式自动地为某些热点页建立哈希索引来加速访问。另外InnoDB自适应哈希索引的功能，用户只能选择开启或关闭功能，无法进行人工干涉。

```java
show engine innodb status \G;
show variables like '%innodb_adaptive%';
```

- ##### Hash查找的时间复杂度：

  **答：**理想的状态是o（1），最坏的是o（n）（因为hash冲突要遍历链表或B+Tree）。

#### 2.3 B+Tree结构

MySQL数据库索引采用的是B+Tree结构，在B-Tree结构上做了优化改造。

- B-Tree结构

  - 索引值和data数据分布在整棵树结构中 
  - 每个节点可以存放多个索引值及对应的data数据 
  - 树节点中的多个索引值从左到右升序排列

  ![image-20201126164627263](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126164627263.png)

  B树的搜索：从根节点开始，对节点内的索引值序列采用二分法查找，如果命中就结束查找。没有命中会进入子节点重复查找过程，直到所对应的的节点指针为空，或已经是叶子节点了才结束。

- B+Tree结构

  - <span style="color:#4169E1;">**非叶子节点不存储data数据，只存储索引值，这样便于存储更多的索引值** </span>
  - 叶子节点包含了所有的索引值和data数据 
  - 叶子节点用指针连接，提高区间的访问性能

  ![image-20201126164720776](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126164720776.png)

  相比B树，B+树进行范围查找时，只需要查找定位两个节点的索引值，然后利用叶子节点的指针进行遍历即可。而B树需要遍历范围内所有的节点和数据，显然B+Tree效率高。

  #### B+Tree对比BTree的优点：

  1. 磁盘读写代价更低。

  2. 查询速度更稳定：

     由于B+Tree非叶子节点不存储数据（data），因此所有的数据都要查询至叶子节点，而叶子节点的高度都是相同的，因此所有数据的查询速度都是一样的。

     很多存储引擎在B+Tree的基础上进行了优化，**添加了指向相邻叶节点的指针**，形成了带有顺序访问指针的B+Tree，这样做是为了提高区间查找的效率，只要找到第一个值那么就可以顺序的查找后面的值。
  
  #### 2.4 聚簇索引和辅助索引
  
  <span style="color:red">**要点：**</span>
  
  聚簇索引和非聚簇索引：B+Tree的叶子节点存放主键**索引值和行记录**就属于聚簇索引；如果索引值和行记录分开存放就属于非聚簇索引（下面的辅助索引就属于非聚簇索引）。
  
  主键索引和辅助索引：B+Tree的叶子节点存放的是主键字段值就属于主键索引；如果存放的是非主键值就属于辅助索引（二级索引）。
  
  > 如下图所示，辅助索引查询行记录需要回表查询（后面也有讲解）：辅助索引查询表数据时需要遍历两次B+tree，先是通过Ellison获取id为14（主键值），然后再通过14找到数据；而主键索引只需要通过14查一次就能定位到数据。
  
  > 所以主键索引和辅助键索引的区别：
  >
  > 1. 主键索引查询效率高
  > 2. 辅助索引存储空间较小（主键索引需要存储完整的行记录数据）
  > 3. 主键索引只局限于主键来查，辅助索引可以创建任意一个非主键字段作为辅助索引来查。
  >
  > ![image-20201126165328843](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126165328843.png)
-----------------------------------------------------

  在InnoDB引擎中，主键索引采用的就是聚簇索引结构存储。

  - 聚簇索引（聚集索引、主键索引）

    聚簇索引是一种数据存储方式，InnoDB的聚簇索引就是按照主键顺序构建 B+Tree结构。B+Tree 的叶子节点就是行记录，行记录和主键值紧凑地存储在一起。 这也意味着 InnoDB 的主键索引就是数据表本身，它按主键顺序存放了整张表的数据，占用的空间就是整个表数据量的大小。通常说的主键索引就是聚集索引。

    <span style="color:red">**要点：**</span>

    InnoDB的表要求必须要有聚簇索引：
  
    - 如果表定义了主键，则主键索引就是聚簇索引
    - 如果表没有定义主键，则第一个非空unique列作为聚簇索引 
    - 否则InnoDB会从建一个隐藏的row-id作为聚簇索引

------------------------------------------------

  - 辅助索引（二级索引）
  
    InnoDB辅助索引，是根据索引列构建 B+Tree结构。但在 B+Tree 的叶子节点中只存了索引列和主键的信息。二级索引占用的空间会比聚簇索引小很多， 通常创建辅助索引就是为了提升查询效率。一个表InnoDB只能创建一个聚簇索引，但可以创建多个辅助索引。（其实辅助索引之所以属于非聚簇索引是因为索引值和部分数据放在一起而不是**完整**的数据）
  
    ![image-20201126165328843](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126165328843.png)
  
  - 非聚簇索引
  
    与InnoDB表存储不同，MyISAM数据表的索引文件和数据文件是分开的，被称为非聚簇索引结构。

  ![image-20201126165500918](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126165500918.png)





### 第3节 索引分析与优化

#### 3.1 EXPLAIN

MySQL 提供了一个 EXPLAIN 命令，它可以对 SELECT 语句进行分析，并输出 SELECT 执行的详细信息，供开发人员有针对性的优化。例如：

```java
EXPLAIN SELECT * from user WHERE id < 3;
```

EXPLAIN 命令的输出内容大致如下：

![image-20201126165911944](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126165911944.png)

- select_type

  表示查询的类型。常用的值如下：

  - SIMPLE ： 表示查询语句不包含子查询或union 
  - PRIMARY：表示此查询是最外层的查询 
  - UNION：表示此查询是UNION的第二个或后续的查询 EXPLAIN SELECT * from user WHERE id < 3; 
  - DEPENDENT UNION：UNION中的第二个或后续的查询语句，使用了外面查询结果 
  - UNION RESULT：UNION的结果 
  - SUBQUERY：SELECT子查询语句 
  - DEPENDENT SUBQUERY：SELECT子查询语句依赖外层查询的结果。

- type

  表示存储引擎查询数据时采用的方式。比较重要的一个属性，通过它可以判断出查询是全表扫描还是基于索引的部分扫描。常用属性值如下，从上至下效率依次增强。

  type的种类（从上至下，性能由差到好）：

  1. **ALL：** 扫描全表
  2. **index：** 扫描全部索引树
  3. **range：** 扫描部分索引，索引范围扫描，对索引的扫描开始于某一点，返回匹配值域的行，常见于between、<、>等的查询
  4. **ref：** 使用非唯一索引或非唯一索引前缀进行的查找
  5. **eq_ref：**唯一性索引扫描，对于每个索引键，表中只有一条记录与之匹配。常见于主键或唯一索引扫描
  6. **const, system：** 单表中最多有一个匹配行，查询起来非常迅速，例如根据主键或唯一索引查询。system是const类型的特例，当查询的表只有一行的情况下， 使用system。
  7. **NULL：** 不用访问表或者索引，直接就能得到结果，如select 1 from test where 1

- possible_keys

  表示查询时能够使用到的索引。注意并不一定会真正使用，显示的是索引名称。

- key

  表示查询时真正使用到的索引，显示的是索引名称。

- rows

  MySQL查询优化器会根据统计信息，估算SQL要查询到结果需要扫描多少行记录。原则上rows是 越少效率越高，可以直观的了解到SQL效率高低。

- key_len

  表示查询使用了索引的字节数量。可以判断是否全部使用了组合索引。 key_len的计算规则如下：

  - 字符串类型 字符串长度跟字符集有关：latin1=1、gbk=2、utf8=3、utf8mb4=4 

    char(n)：n*字符集长度 

    varchar(n)：n * 字符集长度 + 2字节

  - 数值类型

    TINYINT：1个字节

     SMALLINT：2个字节 

    MEDIUMINT：3个字节 

    INT、FLOAT：4个字节 

    BIGINT、DOUBLE：8个字节

  - 时间类型

    DATE：3个字节

    TIMESTAMP：4个字节

    DATETIME：8个字节

  - 字段属性

    NULL属性占用1个字节，如果一个字段设置了NOT NULL，则没有此项。

- Extra

  Extra表示很多额外的信息，各种操作会在Extra提示相关信息，常见几种如下：

  - Using where

    表示查询需要通过索引回表查询数据。

  - Using index

    表示查询需要通过索引，索引就可以满足所需数据。

  - Using filesort

    表示查询出来的结果需要额外排序，数据量小在内存，大的话在磁盘，因此有Using filesort 建议优化。

  - Using temprorary

    查询使用到了临时表，一般出现于去重、分组等操作。

#### 3.2 回表查询

> 当前树查询不到行记录，需要通过另外一个索引才能查询到这种模式叫回表查询，就比如辅助索引查询行记录就是最典型的回表查询

在之前介绍过，InnoDB索引有聚簇索引和辅助索引。聚簇索引的叶子节点存储行记录，InnoDB必须要有，且只有一个。辅助索引的叶子节点存储的是主键值和索引字段值，通过辅助索引无法直接定位行记录，通常情况下，需要扫码两遍索引树。先通过辅助索引定位主键值，然后再通过聚簇索引定位行记录，这就叫做回表查询，它的性能比扫一遍索引树低。 

**总结：**通过索引查询主键值，然后再去聚簇索引查询记录信息

#### 3.3 覆盖索引

> 和回表查询相反，只需要在一棵索引树上就能获取SQL所需的所有列数据就叫做索引覆盖，聚簇索引查询行记录就是最典型的覆盖索引

在MySQL官网，类似的说法出现在explain查询计划优化章节，即explain的输出结果Extra字段为Using index时，能够触发索引覆盖。

不管是SQL-Server官网，还是MySQL官网，都表达了：只需要在一棵索引树上就能获取SQL所需的所有列数据，无需回表，速度更快，这就叫做索引覆盖。

实现索引覆盖最常见的方法就是：将被查询的字段，建立到组合索引。

如何知道是索引覆盖：

![image-20210421204643403](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210421204643403.png)

如上图，Extra：Using index，就是使用索引覆盖。

#### 3.4 最左前缀原则

> 总结：复合索引只有遵循最左前缀原则才不会失效

复合索引使用时遵循最左前缀原则，最左前缀顾名思义，就是最左优先，即查询中使用到最左边的列， 那么查询就会使用到索引，如果从索引的第二列开始查找，索引将失效。

![image-20201126172433570](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126172433570.png)

#### 3.5 LIKE查询

**面试题：MySQL在使用like模糊查询时，索引能不能起作用？**

**答：**LIKE查询只有%字符写在后面索引才起作用

 回答：MySQL在使用Like模糊查询时，索引是可以被使用的，只有把%字符写在**后面**才会使用到索引。 

select * from user where name like '%o%'; //不起作用 

select * from user where name like 'o%'; //起作用 

select * from user where name like '%o'; //不起作用

#### 3.6 NULL查询

**面试题：如果MySQL表的某一列含有NULL值，那么包含该列的索引是否有效？**

**答：**某一列含有NULL值时索引和复合索引都是有效的，但是不建议列中有NULL，因为有以下原因：

1. 不能使用=，<，>这样的运算符。
2. 对NULL做算术运算的结果都是NULL。
3. count时不会包括NULL行。
4. NULL比空字符串需要更多的存储空间等（NULL列需要增加额外空间来记录其值是否为NULL）。

**怎么解决列中不能有NULL？**

**答：**列中设置NOT NULL；为空时设置默认值（比如0和 ‘’ 空字符串等，如果是datetime类型，也可以设置系统当前时间或某个固定的特殊值，例如'1970-01-01 00:00:00'。）。

对MySQL来说，NULL是一个特殊的值，从概念上讲，NULL意味着“一个未知值”，它的处理方式与其他值有些不同。比如：不能使用=，<，>这样的运算符，对NULL做算术运算的结果都是NULL，count时不会包括NULL行等，NULL比空字符串需要更多的存储空间等。

```
“NULL columns require additional space in the row to record whether their values
are NULL. For MyISAM tables, each NULL column takes one bit extra, rounded up to
the nearest byte.”
```

NULL列需要增加额外空间来记录其值是否为NULL。对于MyISAM表，每一个空列额外占用一位，四舍五入到最接近的字节。

![image-20201126172551748](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126172551748.png)

虽然MySQL可以在含有NULL的列上使用索引，但NULL和其他数据还是有区别的，不建议列上允许为 NULL。最好设置NOT NULL，并给一个默认值，比如0和 ‘’ 空字符串等，如果是datetime类型，也可以设置系统当前时间或某个固定的特殊值，例如'1970-01-01 00:00:00'。

#### 3.7 索引与排序

使用order by语句时MySQL有两种排序方式：

> 1. index：
>
>    索引已经排好序了，直接利用索引自动实现排序。
>
> 2. filesort：
>
>    filesort又有两种排序算法：单路排序和双路排序
>
>    - 单路排序：
>
>      一次性全部查出并排序，如果查询数据超出缓存 sort_buffer，会导致多次磁盘读取操作，并创建临时表，最后产生了多次IO，效率反而不如双路排序。
>
>    - 双路排序：
>
>      分两次读取并排序，这样可以防止数据太多，查询不过来。
>
> 分析语句到底是使用index还是filesort排序方式是通过Explain分析，如下图：
>
> index模式：
>
> ![image-20210221181645737](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210221181645737.png)
>
> filesort模式：
>
> ![image-20210221181721062](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210221181721062.png)

MySQL查询支持filesort和index两种方式的排序，filesort是先把结果查出，然后在缓存或磁盘进行排序操作，效率较低。使用index是指利用索引自动实现排序，不需另做排序操作，效率会比较高。

filesort有两种排序算法：双路排序和单路排序。

双路排序：需要两次磁盘扫描读取，最终得到用户数据。第一次将排序字段读取出来，然后排序；第二次去读取其他字段数据。

单路排序：从磁盘查询所需的所有列数据，然后在内存排序将结果返回。如果查询数据超出缓存 sort_buffer，会导致多次磁盘读取操作，并创建临时表，最后产生了多次IO，反而会增加负担。

> 解决方案：少使用select *；增加sort_buffer_size容量和max_length_for_sort_data容量。

如果我们Explain分析SQL，结果中Extra属性显示Using filesort，表示使用了filesort排序方式，需要优化。如果Extra属性显示Using index时，表示覆盖索引，也表示所有操作在索引上完成，也可以使用 index排序方式，建议大家尽可能采用覆盖索引。

- 以下几种情况，会使用index方式的排序：

  - ORDER BY 子句索引列组合满足索引最左前列

    ```java
    explain select id from user order by id; //对应(id)、(id,name)索引有效
    ```

  - WHERE子句+ORDER BY子句索引列组合满足索引最左前列

    ```java
    explain select id from user where age=18 order by name; //对应(age,name)索引
    ```
  
- 以下几种情况，会使用filesort方式的排序。

  - 对索引列同时使用了ASC和DESC

    ```java
    explain select id from user order by age asc,name desc; //对应(age,name)索引
    ```

  - WHERE子句和ORDER BY子句满足最左前缀，但where子句使用了范围查询（例如>、<、in 等）

    ```java
    explain select id from user where age>10 order by name; //对应(age,name)索引
    ```

  - ORDER BY或者WHERE+ORDER BY索引列没有满足索引最左前列

    ```java
    explain select id from user order by name; //对应(age,name)索引
    ```

  - 使用了不同的索引，MySQL每次只采用一个索引，ORDER BY涉及了两个索引

    ```java
    explain select id from user order by name,age; //对应(name)、(age)两个索引
    ```

  - WHERE子句与ORDER BY子句，使用了不同的索引

    ```java
    explain select id from user where name='tom' order by age; //对应(name)、(age)索引
    ```

  - WHERE子句或者ORDER BY子句中索引列使用了表达式，包括函数表达式

    ```java
    explain select id from user order by abs(age); //对应(age)索引
    ```

### 第4节 查询优化

#### 4.1 慢查询定位

- **开启慢查询日志**

  查看 MySQL 数据库是否开启了慢查询日志和慢查询日志文件的存储位置的命令如下：

  ```java
  SHOW VARIABLES LIKE 'slow_query_log%'
  ```

  通过如下命令开启慢查询日志：

  ```java
  SET global slow_query_log = ON;
  SET global slow_query_log_file = 'OAK-slow.log';
  SET global log_queries_not_using_indexes = ON;
  SET long_query_time = 10;
  ```

  long_query_time：指定慢查询的阀值，单位秒。如果SQL执行时间超过阀值，就属于慢查询记录到日志文件中。

  log_queries_not_using_indexes：表示会记录没有使用索引的查询SQL。前提是slow_query_log 的值为ON，否则不会奏效。

- **查看慢查询日志**

  - 文本方式查看

    直接使用文本编辑器打开slow.log日志即可。

    ![image-20201126173356562](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126173356562.png)

    - time：日志记录的时间 
    - User@Host：执行的用户及主机 
    - Query_time：执行的时间 
    - Lock_time：锁表时间 
    - Rows_sent：发送给请求方的记录数，结果数量 
    - Rows_examined：语句扫描的记录条数 
    - SET timestamp：语句执行的时间点 
    - select....：执行的具体的SQL语句

  - 使用mysqldumpslow查看

    MySQL 提供了一个慢查询日志分析工具mysqldumpslow，可以通过该工具分析慢查询日志内容。

    在 MySQL bin目录下执行下面命令可以查看该使用格式。

    ```java
    perl mysqldumpslow.pl --help
    ```

    运行如下命令查看慢查询日志信息：

    ```java
    perl mysqldumpslow.pl -t 5 -s at C:\ProgramData\MySQL\Data\OAK-slow.log
    ```

    除了使用mysqldumpslow工具，也可以使用第三方分析工具，比如pt-query-digest、 mysqlsla等。

#### 4.2 慢查询优化

- **索引和慢查询**

  - **如何判断是否为慢查询？**

    MySQL判断一条语句是否为慢查询语句，主要依据SQL语句的执行时间，它把当前语句的执行时间跟 long_query_time 参数做比较，如果语句的执行时间 > long_query_time，就会把这条执行语句记录到慢查询日志里面。long_query_time 参数的默认值是10s，该参数值可以根据自己的业务需要进行调整。

  - **如何判断是否应用了索引？**

    SQL语句是否使用了索引，可根据SQL语句执行过程中有没有用到表的索引，可通过 explain 命令分析查看，检查结果中的 key 值，是否为NULL。

    ![image-20210221182208094](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210221182208094.png)

  MySQL中explain的type类型
  
  |  ALL        |  全表扫描
  
  |  index       |  索引全扫描
  
  |  range       |  索引范围扫描，常用语<,<=,>=,between等操作
  
  |  ref         |  使用非唯一索引扫描或唯一索引前缀扫描，返回单条记录，常出现在关联查询中
  
  |  eq_ref      |  类似ref，区别在于使用的是唯一索引，使用主键的关联查询
  
  |  const/system  |  单条记录，系统会把匹配行中的其他列作为常数处理，如主键或唯一索引查询
  
  |  null         |  MySQL不访问任何表或索引，直接返回结果
  
  MySQL中explain的extra类型

  **extra** 的信息非常丰富，常见的有：

  1. Using index 使用覆盖索引
  2. Using where 使用了用where子句来过滤结果集
  3. Using filesort 使用文件排序，使用非索引列进行排序时出现，非常消耗性能，尽量优化。
4. Using temporary 使用了临时表 sql优化的目标可以参考阿里开发手册

- **应用了索引是否一定快？**
  
  下面我们来看看下面语句的 explain 的结果，你觉得这条语句有用上索引吗？比如
  
  ```java
    select * from user where id>0;
  ```
  
    虽然使用了索引，但是还是从主键索引的最左边的叶节点开始向右扫描整个索引树，进行了全表扫描，此时**索引就失去了意义**。 
  
    而像 select * from user where id = 2; 这样的语句，才是我们平时说的使用了索引。它表示的意思是，我们使用了索引的快速搜索功能，并且有效地减少了扫描行数。
  
    查询是否使用索引，只是表示一个SQL语句的执行过程；而是否为慢查询，是由它执行的时间决定的，也就是说是否使用了索引和是否是慢查询两者之间没有必然的联系。
  
  我们在使用索引时，不要只关注是否起作用，应该关心索引是否减少了查询扫描的数据行数，如果扫描行数减少了，效率才会得到提升。对于一个大表，不止要创建索引，还要考虑索引过滤性，过滤性好，执行速度才会快。
  
  - **提高索引过滤性**
  
    假如有一个5000万记录的用户表，通过sex='男'索引过滤后，还需要定位3000万，SQL执行速度也不会很快。其实这个问题涉及到索引的过滤性，比如1万条记录利用索引过滤后定位10条、100 条、1000条，那他们过滤性是不同的。索引过滤性与索引字段、表的数据量、表设计结构都有关系。
  
  - 下面我们看一个案例：
  
      ```java
      表：student
      字段：id,name,sex,age
      造数据：insert into student (name,sex,age) select name,sex,age from student;
    SQL案例：select * from student where age=18 and name like '张%';（全表扫描）
    ```
  
  - 优化
  
      ```java
      - alter table student add index(name); //追加name索引 
      ```
  
      优化2
  
      ```java
      alter table student add index(age,name); //追加age,name索引
      ```
  
  - 优化3
  
    ```java
    可以看到，index condition pushdown 优化的效果还是很不错的。再进一步优化，我们可以把名字的第一个字和年龄做一个联合索引，这里可以使用 MySQL 5.7 引入的虚拟列来实现。
    ```
  
    ```java
    //为user表添加first_name虚拟列，以及联合索引(first_name,age)
    alter table student add first_name varchar(2) generated always as
    (left(name, 1)), add index(first_name, age);
    explain select * from student where first_name='张' and age=18;
    ```
  
  - 慢查询原因总结
  
    - 全表扫描：explain分析type属性all 
    - 全索引扫描：explain分析type属性index 
    - 索引过滤性不好：靠索引字段选型、数据量和状态、表设计 
    - 频繁的回表查询开销：尽量少用select *，使用覆盖索引
    
    ![image-20201126165911944](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201126165911944.png)

#### 4.3 分页查询优化

- 一般性分页

  般的分页查询使用简单的 limit 子句就可以实现。limit格式如下：

  ```java
  SELECT * FROM 表名 LIMIT [offset,rows] 
  ```

  - 第一个参数指定第一个返回记录行的偏移量，注意从0开始； 
  - 第二个参数指定返回记录行的最大数目； 
  - 如果只给定一个参数，它表示返回最大的记录行数目；

  思考1：如果偏移量固定，返回记录量对执行时间有什么影响？

  ```java
  select * from user limit 10000,1;
  select * from user limit 10000,10;
  select * from user limit 10000,100;
  select * from user limit 10000,1000;
  select * from user limit 10000,10000;
  ```

  结果：在查询记录时，返回记录量低于100条，查询时间基本没有变化，差距不大。随着查询记录量越大，所花费的时间也会越来越多。

  思考2：如果查询偏移量变化，返回记录数固定对执行时间有什么影响？

  ```java
  select * from user limit 1,100;
  select * from user limit 10,100;
  select * from user limit 100,100;
  select * from user limit 1000,100;
  select * from user limit 10000,100;
  ```

  结果：在查询记录时，如果查询记录量相同，偏移量超过100后就开始随着偏移量增大，查询时间急剧的增加。（这种分页查询机制，每次都会从数据库**第一条记录开始扫描**，越往后查询越慢，而且查询的数据越多，也会拖慢总查询速度。）

- 分页优化方案

  第一步：利用覆盖索引优化

  ```java
  select * from user limit 10000,100;
  select id from user limit 10000,100;
  ```

  第二步：利用子查询优化

  ```java
  select * from user limit 10000,100;
  select * from user where id>= (select id from user limit 10000,1) limit 100;
  ```

  原因：使用了id做主键比较(id>=)，并且子查询使用了覆盖索引进行优化。

#### 使用合理的分页方式以提高分页的效率

使用不同的分页语句效果：

> ##### 方法1-直接使用limit 关键字查询：
>
> select * from tbiguser limit 9999998, 2；
>
> - 缺点：该语句会进行**全表扫描**,速度会很慢且有的数据库结果集返回不稳定(如某次返回1,2,3,另外的一次返回2,1,3)，Limit限制的是从结果集的M位置处取出N条输出,**其余抛弃**。
>
> ##### 方法2-改进语句：
>
> select * from tbiguser where id>9999998 limit 2;
>
> - 虽然比直接使用limit快，如下图，但是如果查询的列不是有序的会有遗漏的情况，只能方法3或者使用有序的列（比如主键）
>
> ##### 方法3：
>
> select * from tbiguser where id > (pageNum*10) order by id asc limit 2;
>
> - 修复了方法2的缺点，但是比它要慢。
>
> ```java
> mysql> select * from tbiguser limit 9999998, 2;
> +----------+------------+-----------------+------+------+--------+---------+
> | id | nickname | loginname | age | sex | status | address |
> +----------+------------+-----------------+------+------+--------+---------+
> | 9999999 | zy9999999 | zhaoyun9999999 | 23 | 1 | 1 | beijing |
> | 10000000 | zy10000000 | zhaoyun10000000 | 23 | 1 | 1 | beijing |
> +----------+------------+-----------------+------+------+--------+---------+
> 2 rows in set (4.72 sec)
> mysql> select * from tbiguser where id>9999998 limit 2;
> +----------+------------+-----------------+------+------+--------+---------+
> | id | nickname | loginname | age | sex | status | address |
> +----------+------------+-----------------+------+------+--------+---------+
> | 9999999 | zy9999999 | zhaoyun9999999 | 23 | 1 | 1 | beijing |
> | 10000000 | zy10000000 | zhaoyun10000000 | 23 | 1 | 1 | beijing |
> +----------+------------+-----------------+------+------+--------+---------+
> 2 rows in set (0.00 sec)
> ```

#### 跨库分页怎么做？

待定。

#### 索引失效的场景：

- ##### 怎么判断sql语句有没有使用到索引？

  **答：**通过explain查看，如下图圆圈部分：

  ​		<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210223214111189.png" alt="image-20210223214111189" style="zoom:60%;" />

##### 索引查询失效的几个情况（索引失效，行锁变为表锁）：

- LIKE查询只有%字符写在后面索引才起作用，其他都失效（比如“%a”、“%a%”）

- 组合索引，不是使用第一列索引，索引失效

  ![img](https://img2018.cnblogs.com/blog/1663681/201907/1663681-20190714222554215-53685899.png)

- 数据类型出现隐式转化。如varchar不加单引号的话可能会自动转换为int型，使索引无效，产生全表扫描

  ![img](https://img2018.cnblogs.com/blog/1663681/201907/1663681-20190714222027129-1935581315.png)

- 使用IS NULL 或 IS NOT NULL操作，索引不一定失效，但是最好不要用：

  ![img](https://img2018.cnblogs.com/blog/1663681/201907/1663681-20190714225235656-1936309040.png)

  ![img](https://img2020.cnblogs.com/blog/1663681/202008/1663681-20200826110937381-1904645425.png)

- 在索引字段上使用not，<>，!=。**不等于操作符是永远不会用到索引的**，因此对它的处理只会产生全表扫描。 优化方法： key<>0 改为 key>0 or key<0。

  ![img](https://img2018.cnblogs.com/blog/1663681/201907/1663681-20190714231600024-1094042713.png)

- 对索引字段进行计算操作、字段上使用函数。（索引为 emp(ename,empno,sal)）

  ![img](https://img2018.cnblogs.com/blog/1663681/201907/1663681-20190714230958372-956162917.png)

- 当全表扫描速度比索引速度快时，mysql会使用全表扫描，此时索引失效。


##### 为什么没有索引或者索引失效时，InnoDB 的行锁会变表锁？

**答：**Mysql 的行锁是通过索引实现的。（InnoDB只有通过索引条件检索数据才使用行级锁，否则，InnoDB将使用表锁）

## 第三部分 MySQL事务和锁

```java
//一个正常事务的流程：
mysql>begin; 			//BEGIN 开始一个事务
mysql>select * from emp;
...                     //进行其他操作
mysql>commit; 			//COMMIT 事务确认
mysql>rollback;		    //ROLLBACK 事务回滚
```

#### MVCC介绍：

核心思想：他的主要实现思想是通过数据多版本来做到读写分离。从而实现不加锁读进而做到读写并行（读不加锁是读写并行的关键，写会加锁）。

> 在 MVCC 并发控制中，读操作可以分为两类：快照读（Snapshot Read）与当前读 （Current Read）
>
> **快照读：**读取的是记录的快照版本（有可能是历史版本），不用加锁。（比如select语句就是快照读） 
>
> **当前读：**读取的是记录的最新版本，并且当前读返回的记录，都会加锁，保证其他事务不会再并发修改这条记录。[ 比如排它锁和共享锁读：select... for update 、lock in share mode，事务操作（操作之前会先查询）：insert/delete/update都会使用当前读 ]

#### 修改过程讲解：

先讲解修改的数据结构：

![image-20210221232456563](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210221232456563.png)

如上图，我们要修改上面的数据，设 F1～F6 是表中字段的名字，1～6 是其对应的数据，三个隐含字段分别对应该行的隐含ID、事务ID和回滚指针

> 事务号每更改一次就会加1，回滚指针是指向上一个版本

修改过程如下图：

![image-20210221235849364](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210221235849364.png)

> 上图过程讲解：
>
> 1. 将修改操作内容放入redo log
> 2. 将修改前版本放入undo log （回滚指针指向该版本）
> 3. 修改表中数据 

修改后的三个不同版本：

![image-20210221232415332](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210221232415332.png)



##### MVCC实现读写并行原理总结：

**答：**MVCC首先每次写会保留之前版本，当事务开始写操作的时候，读操作在RR和RC隔离级别下会快照生成Read View视图，然后通过Read View视图的参数和最新版本的事务ID按特定规则比较，通过即可以访问，不通过则访问上一版本，用同样方式校验是否可读，若是到了最后一个版本，该版本的数据仍对当前事务不可见，那么就表明该条记录对该事务完全不可见，查询结果就不会包含该条记录。（之所以读要经过规则校验是防止幻读、不可重复读发生）

##### MVCC能否完全解决幻读：

**答：**不能，特殊情况还是会出现幻读：

> 假设有如下场景：
>
> ```java
> # 事务T1，REPEATABLE READ隔离级别下
> mysql> BEGIN;
> Query OK, 0 rows affected (0.00 sec)
>  
> mysql> SELECT * FROM t_test WHERE id = 2;
> Empty set (0.01 sec)
>     
> # 此时事务T2执行了：INSERT INTO t_test VALUES(2, '呵呵'); 并提交
> mysql> UPDATE t_test SET name = '哈哈' WHERE id = 2;
> Query OK, 1 row affected (0.01 sec)
> Rows matched: 1  Changed: 1  Warnings: 0
> 
> mysql> SELECT * FROM t_test WHERE id = 2;
> +--------+---------+
> | id  |  name    |
> +--------+---------+
> |     2 |   哈哈   |
> +--------+---------
> 1 row in set (0.01 sec)
> ```
>
> 在REPEATABLE READ隔离级别下，T1第一次执行普通的SELECT语句时生成了一个ReadView，之后T2向表中新插入了一条记录便提交了，ReadView并不能阻止T1执行UPDATE或者DELETE语句来对改动这个新插入的记录（因为T2已经提交，改动该记录并不会造成阻塞），但是这样一来这条新记录的`trx_id`隐藏列就变成了T1的事务id
>
> 之后T1中再使用普通的SELECT语句去查询这条记录时就可以看到这条记录了，也就把这条记录返回给客户端了。因为这个特殊现象的存在，可以认为InnoDB中的MVCC并不能完完全全的禁止幻读

#### MVVC底层数据结构：

- ##### MVCC有两个基本的数据结构：

  1. ##### 行记录：

     Innodb引擎会为每一行添加3个字段实现的，DATA_TRX_ID、DATA_ROLL_PTR与DELETED_BIT：

     ​	DATA _TRX_ID表示产生当前记录项的事务ID（每开启一个新的事务，其对应的事务id会自动递增）；
     ​	DATA_ROLL_PTR一个指向此条记录项的undo信息的指针，undo信息是指此条记录被修改前的信息；
     ​	DELETED_BIT位，用于标识该记录是否被删除。

  2. ##### read view：

     read view的初始化变量：

     ```java
     read_view->creator_trx_id = current-trx;                       		当前的事务id
     read_view->up_limit_id = trx1;                                      当前活跃事务的最小id
     read_view->low_limit_id = trx7;                                     当前活跃事务的最大id
     read_view->trx_ids = [trx1, trx3, trx5, trx7];                   	当前活跃的事务的id列表
     read_view->m_trx_ids = 4;                                           当前活跃的事务id列表长度
     ```

     快照根据上面的变量进行判断：

     ```java
     ow_limit_id，即当时活跃事务的最大id，如果读到row的data_trx_id>=low_limit_id，说明这些数据在当前事务开始时都还没有提交，如注释中的描述，这些数据都不可见。
     
     up_limit_id，即当时活跃事务列表的最小事务id，如果row的data_trx_id<up_limit_id,说明这些数据在当前事务开始时都已经提交，如注释中的描述，这些数据均可见。
     
     data_trx_id在up_limit_id和low_limit_id之间的row，如果这个data_trx_id在trx_ids的集合中，就说明开启当前的事务的时候，这个data_trx_id还处于活跃状态，即还未提交，那么这个row是不可见的；如果这个data_trx_id不在trx_ids的集合中，就说明开启当前的事务的时候，这个data_trx_id已经提交，那么这个row是可见的。
     ```

     这样我们在要在事务中获取数据行，我们就能根据数据行的data_trx_id 和当前事务的read_view来判断此版本的数据在事务中是否可见。可见包括两层含义：

     ```java
     记录可见，且Deleted bit = 0；当前记录是可见的有效记录。
     记录可见，且Deleted bit = 1；当前记录是可见的删除记录。此记录在本事务开始之前，已经删除。
     ```

     如果数据不可见我们需要去哪里找上一个版本的数据呢？

     通过数据行的DB_ROLL_PTR字段去undo log信息中找到上一个版本的记录，再判断这个版本的数据是否可见，以此类推。

     到这里，大概已经清楚了快照读中的"快照"是怎么生成的。

#### MVCC 是如何操作的：

- Select
  InnoDB只查找版本小于或等于当前事务版本的数据行。确保事务读取到的行，要么是事务开始前就存在的，要么是事务自身插入或者修改的。
  行的删除版本要么未定义，要么大于当前事务的版本。确保事务读取到的行，在事务开始之前未被删除。 
- Insert
  InnoDB为新插入的每一行保存当前系统版本号作为行版本号
- Delete 
  InnoDB为删除的每一行保存当前系统版本号作为行删除标识
- Update
  InnoDB会插入一行新记录保存当前系统版本号作为行版本号，同时保存当前系统的版本号到原来的行作为行删除标识


##### 面试题：快照读在RC（读已提交）和RR（可重复读）有什么不同？

**答：**通过undo log实现记录快照存储，第一次读的是时候RR隔离级别会生成ReadView视图，下一次读的话还是查该ReadView视图，而RC隔离级别每次查询都会生成一个新的ReadView视图（所以第二次查的结果可能和第一次不一样）。

##### RC、RR区别：

- RR支持语句级别binlog，事务级创建read_view，支持gap锁。

- RC不支持语句级别binlog，语句级创建read_view，不支持gap锁。

  mysql binlog共有三种日志 statement row mixed

  1. Statement：每一条会修改数据的sql都会记录在binlog中。

     **缺点：**可能出现数据不一致问题。

  2. ROW：记录每一行记录的变化。

     **缺点：**记录的日志文件比较大。

  3. MIXED： 是以上两种level的混合使用，一般的语句修改使用statment格式保存binlog。

     ​				一些函数，statement无法完成主从复制的操作，则采用row格式保存binlog。

     ​				MySQL会根据执行的每一条具体的sql语句来区分对待记录的日志形式。

     

#### 排他锁和共享锁读取和修改语句并发问题：

- ##### 共享锁事务之间的读取和修改语句：

  ```java
  start transaction;
  select * from test where id = 1 lock in share mode;#读取要加lock in share mode
  update test set name = 'kkkk' where id = 1; #update语句和之前一致
  ```

- ##### 排他锁读取和修改语句：

  ```java
  start transaction;
  select * from test where id = 1 for update; #读取要加for update
  update test set name = 'kkkk' where id = 1;  #update语句和之前一致
  ```

  事务之间共享锁和排他锁对于读取和修改的区别：

  - 事务1和事务2都持有共享锁，允许一起读取。
  - 事务1和事务2都持有共享锁，事务1执行读取，事务2不允许执行修改（会出现死锁或者锁超时），需要事务1提交完才能执行修改。
  - 事务1持有共享锁、事务2持有排他锁，不管是读取还是修改，都得等事务1提交后才能执行下一事务操作。
  - 事务1和事务2持有排他锁，不管是读取还是修改，都得等事务1提交后才能执行下一事务操作。

#### 事务隔离级别和锁的关系：

1. 事务隔离级别本质上是对锁和MVCC使用的封装，隐藏了底层细节。
2. 当选用的隔离级别不能解决并发问题或需求时，可以手动加锁控制。

#### 隔离级别与锁的关系

在Read Uncommitted级别下，读取数据不需要加共享锁，这样就不会跟被修改的数据上的排他锁冲突

在Read Committed级别下，读操作需要加共享锁，但是在语句执行完以后释放共享锁；

在Repeatable Read级别下，读操作需要加共享锁，但是在事务提交之前并不释放共享锁，也就是必须等待事务执行完毕以后才释放共享锁。

SERIALIZABLE 是限制性最强的隔离级别，因为该级别**锁定整个范围的键**，并一直持有锁，直到事务完成。

#### mysql锁分类：

- ##### 操作的粒度分类：
  - **表级锁：**每次操作锁住整张表。锁定粒度大，发生锁冲突的概率最高，并发度最低。应用在 MyISAM、InnoDB、BDB 等存储引擎中。 

    ##### 表锁相关操作：

    - 手动增加表锁：

      ```java
    lock table 表名称 read|write,表名称2 read|write;
      ```

    - 查看表上加过的锁：
  
      ```java
      show open tables;
      ```
  
    - 删除表锁：
  
      ```java
      unlock tables;
      ```
  
      总结：表级读锁会阻塞写操作，但是不会阻塞读操作。而写锁则会把读和写操作都阻塞。
  
  - **行级锁：**每次操作锁住一行数据。锁定粒度最小，发生锁冲突的概率最低，并发度最高。应用在InnoDB 存储引擎中。 
  
  - **页级锁：**每次锁定相邻的一组记录，锁定粒度界于表锁和行锁之间，开销和加锁时间界于表锁和行锁之间，并发度一般。应用在BDB 存储引擎中。（了解即可）
  
    mysql存储引擎支持的锁：
  
    <p style="color:red">InnoDB只有通过索引条件检索数据才使用行级锁，否则，InnoDB将使用表锁</p>
    ![image-20210223002155313](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210223002155313.png)
    
    > #### 行锁介绍：
    >
    > InnoDB行锁是通过对**索引数据页上的记录加锁实现的**，主要实现算法有 3 种：Record Lock、Gap Lock 和 Next-key Lock：
    >
    > - Record Lock锁：锁定单个行记录的锁。（记录锁，RC、RR隔离级别都支持） 
    > - Gap Lock锁：间隙锁，锁定索引记录间隙，确保索引记录的间隙不变。（范围锁，RR隔离级别支持） 
    > - Next-key Lock 锁：记录锁和间隙锁组合，同时锁住数据，并且锁住数据前后范围。（记录锁+范围锁，RR隔离级别支持）
    >
    > Gap Lock锁介绍：
    >
    > 1. 间隙锁阻止其他事务对间隙数据的并发插入，这样可有有效的**解决幻读问题**(Phantom Problem)。正因为如此，**并不是所有事务隔离级别都使用间隙锁**，MySQL InnoDB引擎只有在Repeatable Read（默认）隔离级别才使用间隙锁。
    > 2. 间隙锁的作用只是用来阻止其他事务在间隙中插入数据，他不会阻止其他事务拥有同样的的间隙锁。这就意味着，**除了insert语句，允许其他SQL语句可以对同样的行加间隙锁而不会被阻塞**。
    > 3. **对于唯一索引的加锁行为，间隙锁就会失效，此时只有记录锁起作用**。
    >
    > Next-key Lock锁介绍：
    >
    > **答：**Next-key Lock锁就是其他两个算法的集合体，RecordLock锁很好理解，我读这行数据时，其他事务不能修改该行数据。而间隙锁讲解如下例子：
    >
    > 事务1查t1的数据：
    >
    > ![image-20210223004026731](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210223004026731.png)
    >
    > 事务2插入2可以（因为不在5前后范围内），但插入4就不可以：
    >
    > ![image-20210223004119215](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210223004119215.png)
    >
    > ##### 什么sql语句会使用到Next-key Lock？
    >
    > **答：**select ... from lock in share mode、select ... from for update、update ... where、delete ... where（insert语句使用RecordLock锁，普通查询语句select ... from采用MVCC机制实现非阻塞读）
    >
    > ##### Next-key Lock什么时候会降级？
    >
    > **答：**InnoDB对于**记录加锁**行为都是**先采用**Next-Key Lock，但比如碰到SQL操作含有唯一索引时，Innodb会对Next-Key Lock进行优化，降级为RecordLock（因为间隙锁会失效，干脆就不要），仅锁住索引本身而非范围。
  
- ##### 从操作的类型可分为读锁和写锁：

  - 读锁（S锁）：共享锁，针对同一份数据，多个读操作可以同时进行而不会互相影响。 

    > 添加了S锁，可以对记录读操作，不能修改操作，其他事务可以对该记录**追加S锁**，需要追加X锁，需要等记录的S锁全部释放。

  - 写锁（X锁）：排他锁，当前写操作没有完成前，它会阻断其他写锁和读锁。

    > 事务A对记录添加了X锁，可以对记录进行读和修改操作，其他事务不能对记录做读和修改操作。

  ##### IS锁、IX锁介绍：

  IS锁、IX锁：意向读锁、意向写锁，属于**表级锁**，S和X主要针对行级锁。

  在对表记录添加S或X锁之前，会先对表添加IS或IX锁

  

- ##### 从操作的性能可分为乐观锁和悲观锁：

  - 乐观锁：一般的实现方式是对记录数据版本进行比对，在数据更新提交的时候才会进行冲突检测，如果发现冲突了，则提示错误信息。 
  
  - 悲观锁：在对一条数据修改的时候，为了避免同时被其他人修改，在修改数据之前先锁定， 再修改的控制方式。共享锁和排他锁是悲观锁的不同实现，但都属于悲观锁范畴。
  
    ##### mysql实现悲观锁：
  
    行锁、表锁、读锁、写锁、共享锁、排他锁等都属于悲观锁。
  
    ##### mysql实现乐观锁：
  
    乐观锁实现原理 
  
    - 使用版本字段（version） 先给数据表增加一个版本(version) 字段，每操作一次，将那条记录的版本号加 1。version 是用来查看被读的记录有无变化，作用是**防止记录在业务处理期间被其他事务修改**。
    - 使用时间戳（Timestamp） 与使用version版本字段相似，同样需要给在数据表增加一个字段，字段类型使用timestamp 时间戳。也是在**更新提交的时候**检查当前数据库中数据**的时间戳**和自己更新前取到的时间戳进行对比，如果一致则提交更新，否则就是版本冲突，取消操作。

**共享锁（行级锁-读锁）：**

​	总结：事务使用了共享锁，只能读取，不能修改，修改操作被阻塞。（又称为读锁，获得共享锁之后，可以查看但无法修改和删除数据。）

**排他锁（行级锁-写锁）：**

​	总结：事务使用了排他锁（写锁），当前事务可以读取和修改，其他事务不能修改，也不能获取记录锁（select... for update）。如果查询没有使用到索引，将会锁住整个表记录。（又称为写锁、独占锁，获得排他锁之后，既能读数据，又能修改数据。）

共享锁：`SELECT ... LOCK IN SHARE MODE;`

排他锁：`SELECT ... FOR UPDATE;`

###  mysql死锁介绍：

#### 几种常见的死锁现象和解决方案：

1. ##### 表锁死锁：

   用户A访问表A（锁住了表A），然后又访问表B；另一个用户B访问表B（锁住了表B），然后企图访问表A；这时用户A由于用户B已经锁住表B，它必须等待用户B释放表B才能继续，同样用户B要等用户A释放表A才能继续，这就死锁就产生了。

   用户A--》A表（表锁）--》B表（表锁） 

   用户B--》B表（表锁）--》A表（表锁）

   **解决方案：**

   ​	修改业务逻辑，尽量按照相同的顺序进行处理。

2. ##### 行级锁死锁：

   **场景一：**

   ​	**没有索引条件的查询**，引发全表扫描，把行级锁上升为全表记录锁定，多个这样的事务执行后，就很容易产生死锁和阻塞，最终应用系统会越来越慢，发生阻塞或死锁。

   ​	**解决方案1：** 

   ​		SQL语句中不要使用太复杂的关联多表的查询；使用explain“执行计划"对SQL语句进行分析，对于有全表扫描和全表锁定的SQL语句，建立相应的索引进行优化。

   **场景二：**

   ​	和表死锁类似，两个事务分别想拿到对方持有的锁，互相等待，于是产生死锁。

   ​	**解决方案：**

   ​		修改业务逻辑，尽量按照相同的顺序进行处理。

3. ##### 共享锁转换为排他锁：

   举例说明：

   ​	如下图，事务1查询时，事务2更新操作需等待事务1提交，而事务1又有一个提交操作，这时导致事务2等待的修改产生死锁，至于第三步的修改不会死锁是因为mysql优化了（前面有死锁后面不会继续等待）。

   ​	![image-20210223231744413](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210223231744413.png)

   **解决方案：**

   1. 使用乐观锁，但是注意外部系统的操作不能使用乐观锁，因为更新操作不受我们系统的控制。
   2. 对于按钮等控件，点击立刻失效，不让用户重复点击，避免引发同时对同一条记录多次操作；（点击一个按钮触发查询和更新语句时，重复点击就会出现以上问题）

#### 死锁排查：

MySQL提供了几个与锁有关的参数和命令，可以辅助我们优化锁操作，减少死锁发生。 

- 查看死锁日志 

  通过**show engine innodb status\G**命令查看近期死锁日志信息。 

  使用方法：1、查看近期死锁日志信息；2、使用explain查看下SQL执行计划 

- 查看锁状态变量 

  通过**show status like'innodb_row_lock%‘**命令检查状态变量，分析系统中的行锁的争夺情况 

  - Innodb_row_lock_current_waits：当前正在等待锁的数量 
  - Innodb_row_lock_time：从系统启动到现在锁定总时间长度 
  - Innodb_row_lock_time_avg： 每次等待锁的平均时间 
  - Innodb_row_lock_time_max：从系统启动到现在等待最长的一次锁的时间 
  - Innodb_row_lock_waits：系统启动后到现在总共等待的次数 

  如果等待次数高，而且每次等待时间长，需要分析系统中为什么会有如此多的等待，然后着手定制优化。

![image-20210422095321222](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422095321222.png)

![image-20210422095344947](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422095344947.png)

![image-20210422095358813](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422095358813.png)

## 第四部分 mysql集群

#### 在集群架构设计时，主要遵从下面三个维度：

- ##### 可用性设计：

  **保证高可用的方法是冗余**，但是数据冗余带来的问题是数据一致性问题。

  > ##### 冗余的示例：
  >
  > 比如经常关联查询，还不如把关联字段也添加到该表，但是会出现表1更新了该列字段，另外一张表没更新到，可以将**不怎么改动的字段**使用冗余来取代关联查询。

  实现高可用的方案有以下几种架构模式： 

  主从模式 

  - 简单灵活，能满足多种需求。比较主流的用法，但是写操作高可用需要自行处理。 

  双主模式 

  - 互为主从，有双主双写、双主单写两种方式，建议使用双主单写

- ##### 扩展性设计：

  - 如何扩展以提高读性能 

    - 加从库 

      简单易操作，方案成熟。 从库过多会引发主库性能损耗。建议不要作为长期的扩充方案，应该设法用良好的设计避免持续加从库来缓解读性能问题。 

    - 分库分表 

      可以分为垂直拆分和水平拆分，垂直拆分可以缓解部分压力，水平拆分理论上可以无限扩展。 

  - 如何扩展以提高写性能 

    ​	分库分表

- ##### 一致性设计：

  - 不使用从库 

    扩展读性能问题需要单独考虑，否则容易出现系统瓶颈。 

  - 增加访问路由层 

    可以先得到主从同步最长时间t，在数据发生修改后的t时间内，先访问主库。

#### mysql主从复制原理图：

![image-20210225194827905](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210225194827905.png)

主从复制整体分为以下三个步骤：

- 主库将数据库的变更操作记录到Binlog日志文件中 
- 从库读取主库中的Binlog日志文件信息写入到从库的Relay Log中继日志中 
- 从库读取中继日志信息在从库中进行Replay,更新从库数据信息

过程涉及三个线程，Master的BinlogDump Thread和Slave的I/O Thread、SQL Thread：

- Master服务器对数据库更改操作记录在Binlog中，BinlogDump Thread接到写入请求后，读取 Binlog信息推送给Slave的I/O Thread。 
- Slave的I/O Thread将读取到的Binlog信息写入到本地Relay Log中。 
- Slave的SQL Thread检测到Relay Log的变更请求，解析Relay log中内容在从库上执行。

##### 异步复制的时序图：

![image-20210225195113184](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210225195113184.png)

由上图可知，主库同步到从库后就commit，后面有没有同步成功主库也不知道，该时序图存在的问题：

- 主库宕机后，数据可能丢失
- 从库只有一个SQL Thread，主库写压力大，复制很可能延时

解决方法： 

- 半同步复制---解决数据丢失的问题 
- 并行复制----解决从库复制延迟的问题

##### 半同步复制：

- 当Master需要在第三步等待Slave返回ACK时，即为 after-commit（从库commit后才返回ACK），半同步复制（MySQL 5.5引入）。
-  当Master需要在第二步等待 Slave 返回 ACK 时，即为 after-sync（从库commit之前就返回ACK），增强半同步（MySQL 5.7引入）。

##### 并行复制：

- MySQL从5.6版本开始追加了库级别的并行复制，其实和半同步复制的区别就是SQL Thread采用多线程（IO Thread多线程意义不大）。

- MySQL5.7版本并行复制改进，MySQL 5.7才可称为真正的并行复制，它是基于**组提交**的并行复制，不是一次修改就复制到从库。

  ##### 组提交是如何实现的？

  **答：**所有已经处于prepare阶段的事务，都是可以并行提交的。

  > InnoDB事务提交采用的是两阶段提交模式。一个阶段是prepare，另一个是commit。prepare阶段的sql已经跑过了，所以不会有冲突
  >
  > ![image-20210225204440715](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210225204440715.png)
  >
  > 可以发现MySQL 5.7二进制日志较之原来的二进制日志内容多了last_committed和 sequence_number，last_committed表示事务提交的时候，上次事务提交的编号，如果事务具有相同 的last_committed，表示这些事务都在一组内，可以进行并行的回放。

- MySQL8.0版本并行复制改进，MySQL5.7有个问题是读语句也会复制过去，8.0版本改为使用集合存储修改事务信息，所有已经提交的事务所修改的主键值经过hash后都会与那个变量的集合进行对比，来判断该行是否与其冲突，并以此来确定依赖关系，没有冲突即可并行。这样的粒度，就到了 row级别了，此时并行的粒度更加精细，并行的速度会更快。

  > MySQL8.0判断是不是能并行执行的方法：
  >
  > 1. writeset：如果两个事物修改不同行数据，由于主键不同，就可以并行。
  > 2. writeset_session：判断是否是同一个session，如果是，则顺序执行。

#### 读写分离：

读写分离中间件：

- MySQL Proxy：是官方提供的MySQL中间件产品可以实现负载平衡、读写分离等。 
- MyCat：MyCat是一款基于阿里开源产品Cobar而研发的，基于 Java 语言编写的开源数据库中间 件。 
- ShardingSphere：ShardingSphere是一套开源的分布式数据库中间件解决方案，它由ShardingJDBC、Sharding-Proxy和Sharding-Sidecar（计划中）这3款相互独立的产品组成。已经在2020 年4月16日从Apache孵化器毕业，成为Apache顶级项目。

#### 双主方案：

- 双主单写：

  建议用双主单写，再引入高可用组件，例如 Keepalived和MMM等工具，实现主库故障自动切换。

- 双主双写：

  不推荐，存在以下问题：

  -  ID冲突：

    在A主库写入，当A数据未同步到B主库时，对B主库写入，如果采用自动递增容易发生ID主键的冲突。 

    解决方案：可以采用MySQL自身的自动增长步长来解决，例如A的主键为1,3,5,7...，B的主键为2,4,6,8... ，但是对数据库运维、扩展都不友好。

  - 更新丢失：

    同一条记录在两个主库中进行更新，会发生前面覆盖后面的更新丢失。

### MMM架构和MHA架构介绍：

- #### 双主单写MMM架构：

  ![image-20210225230612969](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210225230612969.png)

  

  如上图，MMM架构故障处理包含writer和reader两类角色：

  - 当 writer节点出现故障，程序会自动移除该节点上的VIP
  - 写操作切换到 Master2，并将Master2设置为writer
  - 将所有Slave节点会指向Master2
  - Slave节点如果出现异常，MMM 会移除该节点的 VIP，直到节点恢复正常。

  ##### MMM监控机制

  MMM 包含monitor和agent两类程序，功能如下：

  - monitor：**监控**集群内数据库的**状态**，在出现异常时发布切换命令，一般和数据库**分开部署**。
  - agent：monitor 命令的**执行者**，完成监控的探针工作和具体服务设置，例如设置 VIP（虚拟IP）、指向新同步节点。

- #### MHA架构：

  MHA（Master High Availability）是一套比较成熟的 MySQL 高可用方案，也是一款优秀的故障切换和主从提升的高可用软件。在MySQL故障切换过程中，MHA能做到在**30秒之内自动完成数据库的故障切换操作**，并且在进行故障切换的过程中，MHA能在最大程度上保证数据的一致性，以达到真正意义上的高可用。MHA还支持在线快速将**Master切换到其他主机，通常只需0.5－2秒**。

  目前MHA主要支持一主多从的架构，要搭建MHA，要求一个复制集群中必须最少有三台数据库服务器。

  ![image-20210225231225493](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210225231225493.png)

  ##### MHA Manager功能：

  - 负责检测master是否宕机、控制故障转移、检查MySQL复制状况等
  - MHA Manager会定时探测集群中的master节点，当master出现故障时，它可以自动将最新数据的 slave提升为新的master，然后将所有其他的slave重新指向新的master，整个故障转移过程对应用程序完全透明。

  ##### MHA故障处理机制：

  - 把宕机master的binlog保存下来 
  - 根据binlog位置点找到最新的slave 
  - 用最新slave的relay log修复其它slave 
  - 将保存下来的binlog在最新的slave上恢复 
  - 将最新的slave提升为master 
  - 将其它slave重新指向新提升的master，并开启主从复制

  ##### MHA优点：

  - 自动故障转移快 
  - 主库崩溃不存在数据一致性问题 
  - 性能优秀，支持半同步复制和异步复制 
  - 一个Manager监控节点可以监控多个集群

### 数据库主备切换策略：

##### 主备同步延迟问题：

主备延迟是由主从数据同步延迟导致的，步骤如下：

1. 主库 A 执行完成一个事务，写入 binlog，我们把这个时刻记为 T1; 
2. 之后将binlog传给备库 B，我们把备库 B 接收完 binlog 的时刻记为 T2; 
3. 备库 B 执行完成这个binlog复制，我们把这个时刻记为 T3。（这个阶段最耗时长）

在备库上执行show slave status命令，它可以返回结果信息，seconds_behind_master表示当前备库延迟了多少秒。

主备切换是指将备库变为主库，主库变为备库，有**可靠性优先**和**可用性优先**两种策略。

- 可靠性优先：

  ![image-20210225232436410](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210225232436410.png)

  主库由A切换到B，切换的具体流程如下：

  - 判断从库B的seconds_behind_master值，当小于某个值才继续下一步 
  - 把主库A改为只读状态（readonly=true） 等待从库B的seconds_behind_master值降为 0 
  - 把从库B改为可读写状态（readonly=false） 
  - 把业务请求切换至从库B

- 可用性优先：

  不等主从同步完成， 直接把业务请求切换至从库B，可能数据会不一致。

  > 如下图，切换的时候B又插入数据，并且比A的binglog写表要快于是导致A、B的数据顺序不一致

  ![image-20210225232704054](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210225232704054.png)

  上图过程如下：

  - 主库A执行完 INSERT c=4 ，得到 (4,4) ，然后开始执行主从切换 
  - 主从之间有5S的同步延迟，从库B会先执行 INSERT c=5 ，得到 (4,5) 
  - 从库B执行主库A传过来的binlog日志 INSERT c=4 ，得到 (5,4) 
  - 主库A执行从库B传过来的binlog日志 INSERT c=5 ，得到 (5,5) 
  - 此时主库A和从库B会有两行不一致的数据

通过上面介绍了解到，主备切换采用可用性优先策略，由于可能会导致数据不一致，所以大多数情况下，优先选择**可靠性优先策略**。在满足数据可靠性的前提下，MySQL的可用性依赖于同步延时的大小，同步延时越小，可用性就越高。

### 数据库分片策略：

##### 分片常用规则：

- 基于范围分片：

  根据特定字段的范围进行拆分，比如用户ID、订单时间、产品价格等。例如： {[1 - 100] => Cluster A, [101 - 199] => Cluster B} 

  - 优点：新的数据可以落在新的存储节点上，如果集群扩容，数据无需迁移。

  -  缺点：**数据热点分布不均**，数据冷热不均匀，导致节点负荷不均。

- 哈希取模分片：

  整型的Key可直接对设备数量取模，其他类型的字段可以先计算Key的哈希值，然后再对设备数量取模。假设有n台设备，编号为0 ~ n-1，通过Hash(Key) % n就可以确定数据所在的设备编号。该模式也称为离散分片。

  - 优点：实现简单，数据分配比较均匀，不容易出现冷热不均，负荷不均的情况。 
  - 缺点：**扩容时会产生大量的数据迁移**，比如从n台设备扩容到n+1，绝大部分数据需要重新分配和迁移。

- 一致性哈希分片：

  采用Hash取模的方式进行拆分，后期集群扩容需要迁移旧的数据。使用一致性Hash算法能够很大程度的避免这个问题，所以很多中间件的集群分片都会采用一致性Hash算法。 

  一致性Hash是将数据按照特征值映射到一个首尾相接的Hash环上，同时也将节点（按照IP地址或者机器名Hash）映射到这个环上。对于数据，从数据在环上的位置开始，顺时针找到的第一个节点即为数据的存储节点。Hash环示意图与数据的分布如下：

  ![image-20210226175224064](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210226175224064.png)

  一致性Hash在增加或者删除节点的时候，受到影响的数据是比较有限的，只会影响到Hash环相邻的节点，不会发生大规模的数据迁移。

  [一致性Hash讲解]: D:\study\学习资料\笔记\第四阶段\一致性Hash.md

- #### 分表根据用户id哈希，会出现数据分布不均匀的问题，怎样处理？

  更改分片策略，有以上方案。

### 分库分表：

##### 垂直拆分：

​	分为两种：

- 一种是单库表过多，将表拆分到多个库，

- 另一种是表有些列不经常使用，拆分到其他表（主键也要迁移过去）。

  ##### 垂直拆分优点：

  - 拆分后业务清晰，拆分规则明确； 
  - 易于数据的维护和扩展； 
  - 可以使得行数据变小，一个数据块 (Block) 就能存放更多的数据，在查询时就会减少 I/O 次数； 
  - 可以达到最大化利用 Cache 的目的，具体在垂直拆分的时候可以将不常变的字段放一起，将经常改变的放一起；
  -  便于实现冷热分离的数据表设计模式。

  ##### 垂直拆分缺点： 

  - 主键出现冗余，需要管理冗余列； 
  - 会引起表连接 JOIN 操作，可以通过在业务服务器上进行 JOIN来减少数据库压力，提高了系统的复杂度； 
  - 依然存在单表数据量过大的问题； 
  - 事务处理复杂。

##### 水平拆分：

表记录过多，拆分到不同数据库中

拆分规则：例如范围、时间或Hash算法等。

水平拆分优点： 

- 拆分规则设计好，join 操作基本可以数据库做；
-  不存在单库大数据，高并发的性能瓶颈； 
- 切分的表的结构相同，应用层改造较少，只需要增加路由规则即可； 
- 提高了系统的稳定性和负载能力。 

水平拆分缺点： 

- 拆分规则难以抽象；
-  跨库Join性能较差； 
- 分片事务的一致性难以解决； 
- 数据扩容的难度和维护量极大。

### 分布式数据库主键策略：

- ##### UUID：

  优点：

  - 可以在本地生成，没有网络消耗，所以生成性能高。

  缺点：

  - **数据长**，耗费空间
  - UUID的**无序性**可能会引起数据位置频繁变动（比如索引树会频繁变动），影响性能。

- ##### COMB（UUID变种）：

  将时间信息与UniqueIdentifier的组合，特点是有序性，解决了UUID的2缺点。

- ##### SNOWFLAKE（推荐使用）：

  SnowFlake是Twitter开源的分布式ID生成算法，结果是一个**long型的ID**，long型是8 个字节，64-bit。雪花算法是将符号位、时间、机器ID、序列号组合而成，具体如下图：

  ![image-20210227003614642](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210227003614642.png)

  优点：

  - 效率高，经测试SnowFlake每秒能够产生26万个ID。
  - 有序性，生成的ID整体上按照时间自增排序。
  - 不会产生ID重复。

  缺点：
  - 强依赖机器时钟，如果多台机器环境时钟没同步，或时钟回拨，会导致发号重复或者服务会处于不可用状态。

    > 因此一些互联网公司也基于上述的方案做了封装，例如百度的uidgenerator（基于SnowFlake）和美团的leaf（基于数据库和 SnowFlake）等。

- 数据库ID表：

  单据新建一张存储ID的表，其他表添加字段需要先从该表获取ID。

  缺点：

  - 性能不好，每次插入都要查一遍该表获取分布式ID。
  - 可靠性都不好，万一该存储数据库挂了，就无法获取分布式ID了。

- Redis生成ID：

  可以采用单机或集群

  集群策略：

  - 举例：

    ```java
    A：1,6,11,16,21  //A存储1开始的步长都是5的ID
    B：2,7,12,17,22	//B存储2开始的步长都是5的ID
    C：3,8,13,18,23	...
    D：4,9,14,19,24
    E：5,10,15,20,25
    ```

**为什么官方建议使用自增长主键作为索引？**

结合B+Tree的特点，自增主键是连续的，在插入过程中尽量减少页分裂，即使要进行页分裂，也只会分裂很少一部分。并且能减少数据的移动，每次插入都是插入到最后。总之就是减少分裂和移动的频率。(这也是为什么主要尽量要有序的原因)

### 数据扩容方案：

- ##### 停机扩容：

  ##### 停机扩容的具体步骤如下： 

  - 站点发布一个公告，例如：“为了为广大用户提供更好的服务，本站点将在今晚00:00-2:00之间升级，给您带来不便抱歉"； 
  - 时间到了，停止所有对外服务； 
  - 新增n个数据库，然后写一个数据迁移程序，将原有x个库的数据导入到最新的y个库中。比如分片规则由%x变为%y； 
  - 数据迁移完成，修改数据库服务配置，原来x个库的配置升级为y个库的配置 
  - 重启服务，连接新库重新对外提供服务

  ##### 优点：

  简单 

  ##### 缺点：

  - 停止服务，缺乏高可用 
  - 程序员压力山大，需要在指定时间完成 
  - 如果有问题没有及时测试出来启动了服务，运行后发现问题，数据会丢失一部分，难以回滚。

   适用场景：

  - 小型网站 
  - 大部分游戏 
  - 对高可用要求不高的服务

- ##### 平滑扩容：

  平滑扩容就是将数据库数量扩容成原来的2倍，比如：由2个数据库扩容到4个数据库，具体步骤如下：

  - 新增2个数据库

  - 配置双主进行数据同步（先测试、后上线）

    ![image-20210227005131535](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210227005131535.png)

  - 数据同步完成之后，配置双主双写（同步因为有延迟，如果时时刻刻都有写和更新操作，会存在不准确问题）

    ![image-20210227005122415](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210227005122415.png)

  - 数据同步完成后，删除双主同步，修改数据库配置，并重启；

    ![image-20210227005115262](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210227005115262.png)

  - 此时已经扩容完成，但此时的数据并没有减少，新增的数据库跟旧的数据库一样多的数据，此时还需要写一个程序，清空数据库中多余的数据，如：

    User1去除 uid % 4 = 2的数据；

     User3去除 uid % 4 = 0的数据；

     User2去除 uid % 4 = 3的数据； 

    User4去除 uid % 4 = 1的数据；

  优点： 

  - 扩容期间，服务正常进行，保证高可用 
  - 相对停机扩容，时间长，项目组压力没那么大，出错率低 
  - 扩容期间遇到问题，随时解决，不怕影响线上服务 
  - 可以将每个数据库数据量减少一半 

  缺点： 

  - 程序复杂、配置双主同步、双主双写、检测数据同步等 
  - 后期数据库扩容，比如成千上万，代价比较高 

  适用场景： 

  - 大型网站 
  - 对高可用要求高的服务



## 第五部分：MySQL性能优化

#### 数据库优化维度有四个： 

硬件升级、系统配置、表结构设计、SQL语句及索引。

#### 优化选择： 

- ##### 优化成本：

  硬件升级>系统配置>表结构设计>SQL语句及索引。 

- ##### 优化效果：

  SQL语句及索引>表结构设计>系统配置>硬件升级

### 1、系统配置优化：

#### 1.1调整BufferPool大小：

​	查看bufferpool大小命令：

```xml
mysql> show global status like 'innodb_buffer_pool_pages_%';
+----------------------------------+-------+
| Variable_name | Value |
+----------------------------------+-------+
| Innodb_buffer_pool_pages_data | 8190 |
| Innodb_buffer_pool_pages_dirty | 0 |
| Innodb_buffer_pool_pages_flushed | 12646 |
| Innodb_buffer_pool_pages_free | 0 | 0 表示已经被用光
| Innodb_buffer_pool_pages_misc | 1 |
| Innodb_buffer_pool_pages_total | 8191 |
+----------------------------------+-------+
```

innodb_buffer_pool_size默认为128M，理论上可以扩大到内存的3/4或4/5。

##### 修改innodb_buffer_pool_size方法：

修改 my.cnf文件的参数： innodb_buffer_pool_size = 750M

如果是专用的MySQL Server可以禁用SWAP

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210302224302317.png" alt="image-20210302224302317" style="zoom:70%;" />

#### 1.2数据预热：

默认情况，仅仅有某条数据被读取一次，才会缓存在 innodb_buffer_pool。 

所以，数据库刚刚启动，须要进行数据预热，将磁盘上的全部数据缓存到内存中。 

数据预热能够提高读取速度（不然数据库刚启动，突然执行大量查询语句，这时会直接访问硬盘，会很慢甚至卡死）。

##### 数据预热解决实战：

对于InnoDB数据库，进行数据预热的脚本是:

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210302224740136.png" alt="image-20210302224740136" style="zoom:80%;" />

将该脚本保存为：loadtomem.sql，然后通过以下命令执行脚本：

```xml
mysql -uroot -proot -AN < /root/loadtomem.sql > /root/loadtomem.sql
```

在需要数据预热时，比如重启数据库

```xml
mysql -uroot < /root/loadtomem.sql > /dev/null 2>&1
```

#### 1.3降低磁盘写入次数：

- 增大redolog，减少落盘次数

  innodb_log_file_size 设置为 **0.25 *** innodb_buffer_pool_size

- 通用查询日志、慢查询日志可以不开 ，bin-log开

  生产中**不开通用查询日志**，遇到**性能问题开慢查询日志**

- 写redolog策略 innodb_flush_log_at_trx_commit设置为0或2

  如果不涉及非常高的安全性 (金融系统)，或者基础架构足够安全，或者事务都非常小，都能够用 0 或者 2 来减少磁盘操作。

> innodb_flush_log_at_trx_commit的参数0、1、2讲解：
>
> 值为0 : 提交事务的时候，不立即把 redo log buffer 里的数据刷入磁盘文件的，而是依靠 InnoDB 的主线程每秒执行一次刷新到磁盘。此时可能你提交事务了，结果 mysql 宕机了，然后此时内存里的数据全部丢失。
>
> 值为1 : 提交事务的时候，就必须把 redo log 从内存刷入到磁盘文件里去，只要事务提交成功，那么 redo log 就必然在磁盘里了。注意，因为操作系统的“延迟写”特性，此时的刷入只是写到了操作系统的缓冲区中，因此执行同步操作才能保证一定持久化到了硬盘中。
>
> 值为2 : 提交事务的时候，把 redo 日志写入磁盘文件对应的 os cache 缓存里去，而不是直接进入磁盘文件，可能 1 秒后才会把 os cache 里的数据写入到磁盘文件里去。
>
> 0、1、2的优缺点：
>
> 当设置为0，该模式速度最快，但不太安全，mysqld进程的崩溃会导致上一秒钟所有事务数据的丢失。
>
> 当设置为1，该模式是最安全的， 但也是最慢的一种方式。在mysqld 服务崩溃或者服务器主机crash的情况下，binary log 只有可能丢失最多一个语句或者一个事务。
>
> 当设置为2，该模式速度较快，也比0安全，只有在操作系统崩溃或者系统断电的情况下，上一秒钟所有事务数据才可能丢失。

#### 1.4提高磁盘读写性能 

使用SSD或者内存磁盘

### 2、表结构设计优化：

#### 2.1 设计中间表

设计中间表：对于**实时性不高（因为合到中间表需要一定时间）**的需求，如下图，比如要统计几个表的某些数据，那么我们把每个表统计的数据合成一个中间表，这样我们要查统计数据直接查中间表就可以了。

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210302230128591.png" alt="image-20210302230128591" style="zoom:50%;" />

#### 2.2 设计冗余字段 

为减少关联查询，创建合理的冗余字段（创建冗余字段还需要注意**数据一致性问题**）

#### 2.3 拆表

对于字段太多的大表，考虑拆表（比如一个表有100多个字段） 

对于表中**经常不被使用的字段**或者**存储数据比较多的字段**，考虑拆表

#### 2.4 主键优化

每张表建议都要有一个主键（主键索引），而且主键类型最好是int类型，建议自增主键（不考虑分布式系统的情况下雪花算法）。

#### 2.5 字段的设计

- 数据库中的表越小，在它上面执行的查询也就会越快。因此，在创建表的时候，为了获得更好的性能，我们可以将表中字段的宽度设得尽可能小。
- 尽量把字段设置为NOT NULL，这样在将来执行查询的时候，数据库不用去比较NULL值。
- 对于某些文本字段，例如“省份”或者“性别”，我们可以将它们定义为ENUM类型。因为在MySQL中，ENUM类型被当作数值型数据来处理，而数值型数据被处理起来的速度要比文本类型快得多。这样，我们又可以提高数据库的性能。
- 能用数字的就用数值类型

### 3、SQL语句及索引优化

#### 3.1 SQL语句中IN包含的值不应过多

MySQL对于IN做了相应的优化，即将IN中的常量全部**存储在一个数组**里面，而且这个数组是**排好序的**。但是如果数值较多，产生的消耗也是比较大的。

> ##### 原始表：
>
> ![image-20210303193158313](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210303193158313.png)
>
> ##### sql语句：
>
> ```
> SELECT * FROM Persons WHERE LastName IN ('Adams','Carter')//其实就是查询该列存在该字段的结果
> ```
>
> ![image-20210303193235637](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210303193235637.png)

#### 3.2 SELECT语句务必指明字段名称

尽量不要使用select * 而是要把字段都列出来。

> 使用select * 的缺点：
>
> 1. 增加查询分析器解析成本
> 2. 减少了使用覆盖索引的可能性；
> 3. 无用字段增加网络消耗，尤其是 text 类型的字段。
> 4. **增减字段**容易与 resultMap 配置不一致。

#### 3.3 当只需要一条数据的时候，使用limit 1

limit 是可以停止全表扫描的。

#### 3.4 排序字段加索引

经常使用排序语句时，构建索引能快很多order by id

> 因为直接就查索引B+tree的最下面一排，已经是排好序的。

#### 3.5 如果限制条件中其他字段没有索引，尽量少用or

or两边的字段中，如果有一个不是索引字段，会造成该查询不走索引的情况。

#### 3.6 尽量用union all代替union

Union：对两个结果集进行并集操作，不包括重复行，同时进行默认规则的排序，排序会增加大量的CPU运算，加大资源消耗及延迟；

Union All：对两个结果集进行并集操作，包括重复行，不进行排序；

> union 和join的区别？
>
> 答：union 是将多个语句结合起来，join是将多张表连接起来查询。

![image-20210423154103574](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423154103574.png)

##### union查询：

列出所有在中国和美国的不同的雇员名：

```sql
SELECT E_Name FROM Employees_China
UNION
SELECT E_Name FROM Employees_USA
```

![image-20210423154134695](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423154134695.png)

##### union all查询：

列出在中国和美国的所有的雇员：

```sql
SELECT E_Name FROM Employees_China
UNION ALL
SELECT E_Name FROM Employees_USA
```

![image-20210423154215168](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423154215168.png)

#### 3.7 不使用ORDER BY RAND()

ORDER BY RAND() **不走索引**。

> ##### rand()介绍：
>
> rand()是一个函数，调用它能生成0和1之间的一个数，如果直接调用rand()，每次生成的结果都不一样，如果是rand(1)或rand(2)这种括号有数字的，每次生成都是唯一的，不会变。
>
> rand()用法：
>
> select * from tbiguser order by rand() limit 10，如这个语句，我想搜索10条语句，但是每次都要获取结果不一样，就可以使用rand（）函数实现。

要想使用rand（）可以使用select * from tbiguser t1 join (select rand()*(select max(id) from tbiguser) nid ) t2 on t1.id>t2.nid limit 10;代替。

#### 3.8 区分in和exists、not in和not exists

##### in和exists关联表查询用法：

```sql
SELECT * FROM `user` WHERE `user`.id IN (SELECT `order`.user_id FROM `order`)
SELECT `user`.* FROM `user` WHERE EXISTS (SELECT `order`.user_id FROM `order` WHERE `user`.id = `order`.user_id)
```

##### in和exists的区别？

**答：**如上面的sql语句，in关联表方式是内表驱动外表（适合内表小外表大的），exists是外表驱动内表（适合内表大外表小的）。

> in 和 not in 也要慎用，因为IN会使系统无法使用索引,而只能直接搜索表中的数据。如：
> 	select id from t where num in(1,2,3)
> 	对于连续的数值，能用 between 就不要用 in 了：
> 	select id from t where num between 1 and 3

##### not in和not exists的区别？

关于not in和not exists，推荐使用not exists，不仅仅是效率问题，not in可能存在逻辑问题。如果查询语句使用了not in 那么内外表都进行全表扫描，没有用到索引；而not extsts 的子查询依然能用到表上的索引。

如何高效的写出一个替-代not exists的SQL语句？

原SQL语句：

```sql
select colname … from A表 where a.id not in (select b.id from B表)
```

高效的SQL语句：

```sql
select colname … from A表 Left join B表 on where a.id = b.id where b.id is null
```



#### 3.9使用合理的分页方式以提高分页的效率

使用不同的分页语句效果：

> ##### 方法1-直接使用limit 关键字查询：
>
> select * from tbiguser limit 9999998, 2；
>
> - 缺点：该语句会进行**全表扫描**,速度会很慢且有的数据库结果集返回不稳定(如某次返回1,2,3,另外的一次返回2,1,3)，Limit限制的是从结果集的M位置处取出N条输出,**其余抛弃**。
>
> ##### 方法2-改进语句：
>
> select * from tbiguser where id>9999998 limit 2;
>
> - 虽然比直接使用limit快，如下图，但是如果查询的列不是有序的会有遗漏的情况，只能方法3或者使用有序的列（比如主键）
>
> ##### 方法3：
>
> select * from tbiguser where id > (pageNum*10) order by id asc limit 2;
>
> - 修复了方法2的缺点，但是比它要慢。
>
> ```java
> mysql> select * from tbiguser limit 9999998, 2;
> +----------+------------+-----------------+------+------+--------+---------+
> | id | nickname | loginname | age | sex | status | address |
> +----------+------------+-----------------+------+------+--------+---------+
> | 9999999 | zy9999999 | zhaoyun9999999 | 23 | 1 | 1 | beijing |
> | 10000000 | zy10000000 | zhaoyun10000000 | 23 | 1 | 1 | beijing |
> +----------+------------+-----------------+------+------+--------+---------+
> 2 rows in set (4.72 sec)
> mysql> select * from tbiguser where id>9999998 limit 2;
> +----------+------------+-----------------+------+------+--------+---------+
> | id | nickname | loginname | age | sex | status | address |
> +----------+------------+-----------------+------+------+--------+---------+
> | 9999999 | zy9999999 | zhaoyun9999999 | 23 | 1 | 1 | beijing |
> | 10000000 | zy10000000 | zhaoyun10000000 | 23 | 1 | 1 | beijing |
> +----------+------------+-----------------+------+------+--------+---------+
> 2 rows in set (0.00 sec)
> ```

#### 3.10 分段查询

一些用户选择页面中，可能一些用户选择的范围过大，造成查询缓慢。主要的原因是扫描行数过多。这个时候可以通过程序，**分段进行查询，循环遍历，将结果合并**处理进行展示。

#### 3.11 不建议使用%前缀模糊查询 

例如LIKE“%name”或者LIKE“%name%”，这种查询会导致索引失效而进行全表扫描。但是可以使用LIKE “name%”。 

那么如何解决这个问题呢，

答案：如果一定要使用LIKE“%name%”查询的话，可以使用全文索引或ES全文检索

#### 3.12 避免在where子句中对字段进行表达式操作

```
select user_id,user_project from user_base where age*2=36;
```

上面这句不走索引，建议改成：

```
select user_id,user_project from user_base where age=36/2;
```

#### 3.13 避免隐式类型转换

where子句中出现column字段的类型和传入的参数类型不一致的时候发生的类型转换，建议先确定 where中的参数类型。 where age='18'

#### 3.14 对于联合索引来说，要遵守最左前缀法则

举列来说索引含有字段id、name、school，可以直接用id字段，也可以id、name这样的顺序，但是 name;school都无法使用这个索引。所以在创建联合索引的时候一定要注意索引字段顺序，常用的查询字段放在最前面。

#### 3.15 必要时可以使用force index来强制查询走某个索引

有的时候MySQL优化器采取它认为合适的索引来检索SQL语句，但是可能它所采用的索引并不是我们想要的。这时就可以采用force index来强制优化器使用我们制定的索引。

原来的语句：

```sql
SELECT * FROM user u where u.id=100 order by u.update_time
```

强制索引语句：

```sql
SELECT * FROM user u force index(idx_user_id_update_time) where u.id=100 order by u.update_time
```



#### 3.17 注意范围查询语句

对于联合索引来说，如果存在范围查询，比如between、>、<等条件时，会造成后面的索引字段失效。

#### 3.18 使用JOIN优化

尽量使用数据量小的作为驱动表：

> ##### 为什么要使用数据量小的作为驱动表？
>
> **答：**驱动表的操作是循环遍历，关联的另外一张表是匹配，而B+树匹配要比循环遍历快得多。所以推荐使用小表驱动大表。

LEFT JOIN A表为驱动表，INNER JOIN MySQL会自动找出那个数据少的表作用驱动表，RIGHT JOIN B 表为驱动表。

MySQL中没有full join，可以用以下方式来解决：

```sql
select * from A left join B on B.name = A.namewhere B.name is null union all
select * from B;
```

##### join优化策略：

1. 尽量使用inner join（因为它会自动找出那个数据少的表作用驱动表）。
2. 合理利用索引：被驱动表的索引字段作为on的限制字段。

> ### 数据库JOIN用法：
>
> 对数据库中的两张或两张以上表进行连接操作：
>
> ##### Persons：
>
> ![image-20210423152302361](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423152302361.png)
>
> ##### Orders：
>
> ![image-20210423152315985](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423152315985.png)
>
> **Join** 分为：
>
> - 内连接(inner join)
> - 外连接(outer join)
>
> 其中**外连接**分为：
>
> - 左外连接(left outer join)
> - 右外连接(right outer join)
> - 全外连接(full outer join)
>
> ## 内连接——inner join
>
> 内连接查询返回满足条件的所有记录，**默认情况下没有指定任何连接则为内连接**。
>
> ```sql
> SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo
> FROM Persons
> INNER JOIN Orders
> ON Persons.Id_P=Orders.Id_P
> ORDER BY Persons.LastName
> ```
>
> **查询结果**
>
> ![image-20210423153401945](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423153401945.png)
>
> ## 左外连接——left join
>
> 左外连接查询不仅返回满足条件的所有记录，而且还会返回不满足连接条件的连接操作符左边表的其他行。
>
> ```sql
> SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo
> FROM Persons
> LEFT JOIN Orders
> ON Persons.Id_P=Orders.Id_P
> ORDER BY Persons.LastName
> ```
>
> **查询结果**
>
> ![image-20210423153726148](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423153726148.png)
>
> ## 右外连接——right join
>
> 右外连接查询不仅返回满足条件的所有记录，而且还会返回不满足连接条件的连接操作符右边表的其他行。
>
> ```java
> SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo
> FROM Persons
> RIGHT JOIN Orders
> ON Persons.Id_P=Orders.Id_P
> ORDER BY Persons.LastName
> ```
>
> **查询结果**
>
> ![image-20210423153756785](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423153756785.png)
>
> 
>
> ## 全连接——full join
>
> full join顾名思义，不满足连接条件的左右都会查询来。
>
> ```sql
> SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo
> FROM Persons
> FULL JOIN Orders
> ON Persons.Id_P=Orders.Id_P
> ORDER BY Persons.LastName
> ```
>
> 查询结果：
>
> ![image-20210423153909272](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423153909272.png)
>
> **注：**mysql默认不支持full join。
>
> ## 数据库JOIN出现的笛卡尔积现象：
>
> 我们以下面两张表举例：
>
> **学生表（Student）**
>
> | ID   | StudentName | StudentClassID |
> | ---- | ----------- | -------------- |
> | 1    | 小明        | 1              |
> | 2    | 小红        | 2              |
> | 3    | 小兰        | 3              |
> | 4    | 小吕        | 2              |
> | 5    | 小梓        | 1              |
>
> **班级表（Class）**
>
> | ClassID | ClassName |
> | ------- | --------- |
> | 1       | 软件一班  |
> | 2       | 软件二班  |
> | 3       | 软件三班  |
>
> 当我们进行查询操作的时候：
>
> > select * from Student,Class;
>
> ![查询](https://img2018.cnblogs.com/blog/1629488/201906/1629488-20190622122511883-746628420.png)
>
> 就会出现上面的情况，也就是笛卡尔现象，表Student中有5条记录，表Class中有3条记录，那么对于表Student而言有5种选择，对于表Class来说有3种选择。所以一共有 5 * 3 = 15种选择了，也就是**笛卡尔积**。

##### sql语句in和on的区别？

in指的是某字段的值在某个集合当中。也就是该字段的取值范围。
比如：select * from name where name in ('a','s','d')

on主要是在表之间进行连接时指明连接条件的，有内连接，外连接等

比如：select * from apps inner join altapp on apps.app_name = altapp.source_name

##### 在使用left jion时，on和where条件的区别？

1. where条件是在临时表生成好后，再对临时表进行过滤的条件。这时已经没有left join的含义（必须返回左边表的记录）了，条件不为真的就全部过滤掉。
2. on条件是在生成临时表时使用的条件，它不管on中的条件是否为真，都会返回左边表中的记录。

假设有两张表：

表1：tab2

| id   | size |
| ---- | ---- |
| 1    | 10   |
| 2    | 20   |
| 3    | 30   |

表2：tab2

| size | name |
| ---- | ---- |
| 10   | AAA  |
| 20   | BBB  |
| 20   | CCC  |

两条SQL:
1、select * form tab1 left join tab2 on (tab1.size = tab2.size) where tab2.name=’AAA’
2、select * form tab1 left join tab2 on (tab1.size = tab2.size and tab2.name=’AAA’)

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210304201932090.png" alt="image-20210304201932090" style="zoom:50%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210304201952101.png" alt="image-20210304201952101" style="zoom:50%;" />

如上图，**不管on上的条件是否为真都会返回left或right表中的记录**， 而inner jion没这个特殊性，则条件放在on中和where中，返回的结果集是相同的。

> #### SQL SELECT DISTINCT 语句：
>
> 就是查询结果不能有重复值
>
> | Company  | OrderNumber |
> | :------- | :---------- |
> | IBM      | 3532        |
> | W3School | 2356        |
> | Apple    | 4698        |
> | W3School | 6953        |
>
> ```sql
> SELECT Company FROM Orders
> ```
>
> | Company  |
> | :------- |
> | IBM      |
> | W3School |
> | Apple    |
> | W3School |
>
> ```sql
> SELECT DISTINCT Company FROM Orders 
> ```
>
> | Company  |
> | :------- |
> | IBM      |
> | W3School |
> | Apple    |

### 4.阿里的MYSQL开发规约：

#### 4.1 建表规约：



#### 4.2 索引规约：

【强制性的】：

1. 有唯一性的字段也必须建成唯一索引。

   **说明：**不要以为唯一索引影响了 insert 速度，这个速度损耗可以忽略，但提高查找速度是明显的；另外，即 使在应用层做了非常完善的校验控制，只要没有唯一索引，根据墨菲定律，必然有脏数据产生。

2. 三个表以上禁止 join，join涉及到循环遍历和匹配，性能消耗较大。

3. 在 varchar 字段上建立索引时，必须指定索引长度，没必要对全字段建立索引，根据实际文本区分度决定索引长度即可。

   **说明：**varchar 类型的有些长度不适合做索引，可以使用 count(distinct left(列名, 索引长度))/count(*)公式设置一个合适的长度。

4. 页面搜索严禁左模糊或者全模糊，如果需要请走搜索引擎来解决。

   就是之前说的%%不走索引问题。



### 复杂SQL实战：

需优化SQL：

```java
mysql> select count(id) num , address from tbiguser where address in (select
distinct address from tuser1) group by address union select count(id) num ,
address from tbiguser where address in (select distinct address from tuser2)
group by address ;
+-----+----------+
| num | address |
+-----+----------+
| 105 | tianjin |
| 100 | shanghai |
+-----+----------+
2 rows in set (14.43 sec)
```

通过explain可以看到：

```java
mysql> explain select count(id) num , address from tbiguser where address in
(select distinct address from tuser1) group by address union select
count(id) num , address from tbiguser where address in (select distinct
address from tuser2) group by address ;
+----+--------------+-------------+------+---------------+------+---------+-----
-+---------+----------------------------------------------------+
| id | select_type | table | type | possible_keys | key | key_len | ref
| rows | Extra |
+----+--------------+-------------+------+---------------+------+---------+-----
-+---------+----------------------------------------------------+
| 1 | PRIMARY | <subquery2> | ALL | NULL | NULL | NULL | NULL| NULL | Using temporary; Using filesort |
| 1 | PRIMARY | tbiguser | ALL | NULL | NULL | NULL | NULL| 9754360 | Using where; Using join buffer (Block Nested Loop) |
| 2 | MATERIALIZED | tuser1 | ALL | NULL | NULL | NULL | NULL| 20 | NULL |
| 3 | UNION | <subquery4> | ALL | NULL | NULL | NULL | NULL| NULL | Using temporary; Using filesort |
| 3 | UNION | tbiguser | ALL | NULL | NULL | NULL | NULL| 9754360 | Using where; Using join buffer (Block Nested Loop) |
| 4 | MATERIALIZED | tuser2 | ALL | NULL | NULL | NULL | NULL| 20 | NULL|
| NULL | UNION RESULT | <union1,3> | ALL | NULL | NULL | NULL |NULL | NULL | Using temporary |
+----+--------------+-------------+------+---------------+------+---------+-----
-+---------+----------------------------------------------------+
7 rows in set (0.00 sec)
```

type:为ALL 说明没有索引，全表扫描
Using temporary：说明使用了临时表
Using filesort ：说明使用了文件排序
Using where：没有索引下推，在Server层进行了全表扫描和过滤
Using join buffer(Block Nested Loop)：关联没有索引，有关联优化

1. ##### 第一次优化：

   给address加索引

   ```sql
   --给address加索引
   alter table tbiguser add index idx_addr(address);
   alter table tuser1 add index idx_addr(address);
   alter table tuser2 add index idx_addr(address);
   
   --再次Explain
   mysql> explain select count(id) num , address from tbiguser where address in (select distinct address from tuser1) group by address union select count(id) num , address from tbiguser where address in (select distinct address from tuser2) group by address;
   +----+--------------+-------------+--------+---------------+------------+-------
   --+-----------------------+---------+--------------------------+
   | id | select_type | table | type | possible_keys | key |key_len | ref | rows | Extra |
   +----+--------------+-------------+--------+---------------+------------+-------
   --+-----------------------+---------+--------------------------+
   | 1 | PRIMARY | tbiguser | index | idx_addr | idx_addr | 768| NULL | 9754360 | Using where; Using index |
   | 1 | PRIMARY | <subquery2> | eq_ref | <auto_key> | <auto_key> | 768| demo.tbiguser.address | 1 | NULL |
   | 2 | MATERIALIZED | tuser1 | index | idx_addr | idx_addr | 768| NULL | 20 | Using index |
   | 3 | UNION | tbiguser | index | idx_addr | idx_addr | 768| NULL | 9754360 | Using where; Using index |
   | 3 | UNION | <subquery4> | eq_ref | <auto_key> | <auto_key> | 768| demo.tbiguser.address | 1 | NULL |
   | 4 | MATERIALIZED | tuser2 | index | idx_addr | idx_addr | 768| NULL | 20 | Using index |
   | NULL | UNION RESULT | <union1,3> | ALL | NULL | NULL | NULL| NULL | NULL | Using temporary |
   +----+--------------+-------------+--------+---------------+------------+-------
   --+-----------------------+---------+--------------------------+
   ```

   type：index ，说明用到了索引 ： 覆盖索引
   Using temporary ：有临时表
   Using where ：没有索引下推，在Server层进行了全表扫描和过滤

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210519233657529.png" alt="image-20210519233657529" style="zoom:80%;" />

2. ##### 第二次优化：

   ```sql
   --将or取代union(这样就没有临时表了)，虽然使用or是导致索引失效，但是这里两个语句有用到索引
   select count(id) num , address from tbiguser where address in (select distinct address from tuser1) or address in (select distinct address from tuser2) group by address order by address;
   
   --运行执行计划
   explain select count(id) num , address from tbiguser where address in (select
   distinct address from tuser1) or address in (select distinct address from
   tuser2) group by address order by address;
   +----+-------------+----------+-------+---------------+----------+---------+----
   --+---------+--------------------------+
   | id | select_type | table | type | possible_keys | key | key_len | ref
   | rows | Extra |
   +----+-------------+----------+-------+---------------+----------+---------+----
   --+---------+--------------------------+
   | 1 | PRIMARY | tbiguser | index | idx_addr | idx_addr | 768 |NULL | 9754360 | Using where; Using index |
   | 3 | SUBQUERY | tuser2 | index | idx_addr | idx_addr | 768 |NULL | 20 | Using index |
   | 2 | SUBQUERY | tuser1 | index | idx_addr | idx_addr | 768 |NULL | 20 | Using index |
   +----+-------------+----------+-------+---------------+----------+---------+----
   --+---------+--------------------------+
   3 rows in set (0.00 sec)
   ```

   没有了临时表

   但是还有using where。

3. ##### 第三次优化：

   将两个主查询sql的type由index变为ref，然后使用union all关联

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210519235819212.png" alt="image-20210519235819212" style="zoom:80%;" />

   ```sql
   select count(x.id),x.address from(select distinct b.* from tuser1 a,tbiguser b where a.address=b.address union
   all select distinct b.* from tuser2 a,tbiguser b where a.address=b.address) x group by x.address;、
   
   --查看执行计划
   mysql> explain select count(x.id),x.address
   -> from
   -> (select distinct b.* from tuser1 a,tbiguser b where a.address=b.address
   union all select distinct b.* from tuser2 a,tbiguser b where
   a.address=b.address) x group by x.address;
   +----+--------------+------------+-------+---------------+----------+---------+-
   ---------------+----------+-------------------------------------------+
   | id | select_type | table | type | possible_keys | key | key_len |ref | rows | Extra |
   +----+--------------+------------+-------+---------------+----------+---------+-
   ---------------+----------+-------------------------------------------+
   | 1 | PRIMARY | <derived2> | ALL | NULL | NULL | NULL |NULL | 97543600 | Using temporary; Using filesort |
   | 2 | DERIVED | a | index | idx_addr | idx_addr | 768 |NULL | 20 | Using where; Using index; Using temporary |
   | 2 | DERIVED | b | ref | idx_addr | idx_addr | 768 |demo.a.address | 2438590 | Distinct |
   | 3 | UNION | a | index | idx_addr | idx_addr | 768 |NULL | 20 | Using where; Using index; Using temporary |
   | 3 | UNION | b | ref | idx_addr | idx_addr | 768 |demo.a.address | 2438590 | Distinct |
   | NULL | UNION RESULT | <union2,3> | ALL | NULL | NULL | NULL| NULL | NULL | Using temporary |
   +----+--------------+------------+-------+---------------+----------+---------+-
   ---------------+----------+-------------------------------------------+
   6 rows in set (0.00 sec)
   ```

   4.最后，还可以将复杂sql生成视图，**mysql有对视图进行优化。**

   ##### 优化总结：

   开启慢查询日志，定位运行慢的SQL语句
   利用explain执行计划，查看SQL执行情况
   关注索引使用情况：type（加索引后，查看索引使用情况，index只是覆盖索引，并不算很好的使用索引，如果有关联尽量将索引用到eq_ref或ref级别）
   关注Rows：行扫描(行数越少越好)
   关注Extra：没有信息最好
   复杂SQL可以做成视图，视图在MySQL内部有优化，而且开发也比较友好
   对于复杂的SQL要逐一分析，找到比较费时的SQL语句片段进行优化

#### 外键关联的优缺点：

优点：保持数据一致性（保证数据的引用完整性），子表有主表的外键数据，那么删除表A就会失败。

缺点：更新子表或者删除子表数据都会去主表判断一下，这是个隐式操作，拖累系统，性能很差。

#### 存储过程：

存储过程说白了就是一堆SQL的合并，在比较复杂的业务时比较实用。

- ##### 存储过程的优点：

  1. **不需要重新编译**，一般 SQL 语句每执行一次就编译一次，存储过程只在创造时进行编译，以后每次执行存储过程都不需再重新编译，可提高数据库执行速度。
  2. **可减少数据库连接**，当对数据库进行复杂操作时(如对多个表进行 Update,Insert,Query,Delete 时），可将此复杂操作用存储过程封装起来与数据库提供的事务处理结合一起使用。这些操作，如果用程序来完成，就变成了一条条的 SQL 语句，可能要多次连接数据库。而换成存储，只需要连接一次数据库就可以了。
  3. 存储过程可以重复使用,可减少数据库开发人员的工作量。
  4. 安全性高,可设定只有某此用户才具有对指定存储过程的使用权。

- ##### 存储过程的缺点：

  1. 大多数高级的数据库系统都有statement  cache的，所以编译sql的花费没什么影响。但是执行存储过程要比直接执行sql花费更多（检查权限等），所以对于很简单的sql，存储过程没有什么优势。
  2. 代码可读性差,相当难维护。
  3. 可移植性差。

### 全局临时表：

全局临时表的生命周期一直持续到创建会话(不是创建级别)才终止。

> #### 本地临时表和全局临时表的区别？
>
> 本地临时表只对创建这个表的用户的SESSION可见，对其他进程是不可见的。当创建它的进程消失时，这个临时表就会自动删除。
> 全局临时表对整个SQL Server示例都可见，只有当所有访问它的SESSION都消失的时候，它才会自动删除

- ##### 创建本地临时表语句：

  ```sql
  CREATE TEMPORARY TABLE #tmp_table (
   name VARCHAR(10) NOT NULL,
   value INTEGER NOT NULL
  )
  ```

- ##### 创建全局临时表语句：

  ```sql
  CREATE TABLE dbo.##Globals
  (
      ID INT NOT NULL,
      VALUE NVARCHAR(50) NOT NULL,
      CONSTRAINT PK_Globals PRIMARY KEY(ID)
  );
  ```

## 面试题

#### 普通LRU和改性LRU的区别

1. 普通LRU：末尾淘汰法，新数据从链表头部加入，释放空间时从末尾淘汰

   ![image-20201113151254862](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201113151254862.png)

2. 改性LRU：

   （1）整个LRU长度是10；

   （2）前70%是新生代；

   （3）后30%是老生代；

   （4）新老生代首尾相连

   假如有一个页号为50的新页被预读加入缓冲池，50先加入老生代，将7淘汰，假如50这一页立刻被读取到，它会被立刻加入到新生代的头部，新生代的页被挤到老生代

   ![img](https://mmbiz.qpic.cn/mmbiz_png/YrezxckhYOy291iaib2osRbSOicYG30MaK9XOiaLNoiaibboSMgGxdofIItGUuicibteYc0SPVJHEc3zUq5Ed24wPxYHibQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

   ![img](https://mmbiz.qpic.cn/mmbiz_png/YrezxckhYOy291iaib2osRbSOicYG30MaK91motSYgHJCN97doViabdM0dgr1iae1cxSENEWsvjML3opibQMMKRPR7cA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

   ![img](https://mmbiz.qpic.cn/mmbiz_png/YrezxckhYOy291iaib2osRbSOicYG30MaK9dohrUhEGPmiaDxHmFzial6iaqwU3Gn4AIVv85IApUsFSLK6ianlSZficIFA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

3. 为了改善缓存污染，进一步优化改性LRU，具体方式如下：

   加入以下约束：

   （1）假设T=老生代停留时间窗口；

   （2）插入老生代头部的页，即使立刻被访问，并不会立刻放入新生代头部；

   （3）**只有满足“被访问”并且“在老生代停留时间”大于T，才会被放入新生代头部**

#### 什么是缓存污染和预读失效？

**预读失效：**

由于预读(Read-Ahead)，提前把页放入了缓冲池，<span style="color:#4169E1;">**但最终MySQL并没有从页中读取数据**</span>，称为预读失效。

**什么是MySQL缓冲池污染：**

当某一个SQL语句，要批量扫描大量数据时，可能导致把缓冲池的所有页都替换出去，导致大量热数据被换出，MySQL性能急剧下降，这种情况叫缓冲池污染。（意思是在LRU场景中，如果被读了一次就立即放入新生代头部，有可能只读这一次，后面很少读了，但是把其他经常读的给挤到老生代）

#### 更改缓冲区是否支持其他类型的索引？

不可以。更改缓冲区仅支持二级索引。不支持聚簇索引，全文索引和空间索引。全文索引有自己的缓存机制。

#### 有关Change buffer的哪些类型的操作会修改二级索引并导致更改缓冲？

INSERT、UPDATE和DELETE操作可以修改二级索引。如果受影响的索引页不在缓冲池中，则可以在更改缓冲区中缓冲更改。

**刷新更换缓冲区的时间是什么时候？**

更新的页面由刷新占用缓冲池的其他页面的相同刷新机制刷新。

**应该何时使用更改缓冲区？**

更改缓冲区是一种功能，旨在随着索引变大并且不再适合InnoDB缓冲池而减少对二级索引的随机I / O. 通常，当整个数据集不适合缓冲池，存在修改二级索引页的大量DML活动时，或者存在大量由DML活动定期更改的二级索引时，应使用更改缓冲区。

**应该何时不使用更改缓冲区？**

如果整个数据集适合InnoDB缓冲池，如果您的二级索引相对较少，或者您使用的是固态存储，则可以考虑禁用更改缓冲区，其中随机读取的速度与顺序读取速度一样快。在进行配置更改之前，建议您使用代表性工作负载运行测试，以确定禁用更改缓冲区是否提供任何好处。

#### 数据库自增ID用完了会怎么样

- ##### 有主键的情况：

  一般主键的类型是int是4个字节，如果有符号位的话最大值是2147483647，无符号位的话最大值就是4294967295。
  
  21亿不够用的话，使用bigint类型，bigint是8个字节。
  
- ##### 没主键的情况：

  没有设置主键的话，InnoDB则会自动帮你创建一个6个字节的row_id，由于row_id是无符号的，所以最大长度是2^48-1也就是，281474976710655（281万亿）

- ##### 有主键和没主键的id用完的情况区别：

  有主键：再添加会报错

  没主键：再添加会覆盖以前的数据

- ##### 经常DML操作对索引的影响：

  当表里的数据发生DML操作时，oracle会自动维护索引树。但是在索引树中没有更新操作，只有删除和插入操作。例如在某列上创建索引，将某列上的一个值“1”更新为“2”时，oracle会同时更新索引树，但是oracle是先将索引树中的“1”标示为删除，然后再将“2”写到索引树中。所以如果表更新比较频繁，那么在索引中删除的标示会越来越多，这时索引的查询效率必然降低，所以我们应该定期重建索引。

- ##### 索引查询复杂度：

  索引有助于提升数据的查询性能。对于B+树，查询数据的IO平均次数为O(log n)；如果没有索引需要逐条匹配，IO平均次数为O(n)。

#### 数据量超过100万表的负载，怎样扩充新字段

- ##### 方案一、扩展新表方案（建新表，每次查询关联查就行）

  1、扩展一张新的表，t_order_goods_ext ，添加关联t_order_goods表的字段good_id, 是否虚拟商品标示字段:isVirtual
  2、将涉及到本次业务的虚拟商品goodId初始化到扩展表中，不用迁移t_order_goods老表的数据，这样数据量瞬间很小了
  3、调整应用层的程序调用代码，将涉及到操作t_order_goods表的地方改为左关联扩展表t_order_goods_ext

- ##### 方案二、老表数据迁移四部曲方案（建立备份表进行迁移）

  1、新建老表t_order_goods的备份表t_order_goods_bak，同时加一个字段：isVirtual 并给默认值
  2、迁移老表t_order_goods数据到备份表t_order_goods_bak中
  3、删除老表t_order_goods
  4、新命名备份表t_order_goods_bak表名为t_order_goods
  以上的操作步骤2~4建议是在脱机的情况下执行，避免在执行迁移数据过程中有新数据进来，导致新表数据流失不完整

- ##### 方案三、升级MySQL的服务器版本（5.7版本加字段很慢，8.0.12版本的加字段算法很快，几乎是毫秒级的操作）

  1、将现有MySQL版本5.7升级到8.0.12之后的版本
  2、然后再执行添加字段操作

  以上几个方案优势劣势对比

  ##### 方案一：

  优势：开发人员可以快速的定位相关影响点
  劣势: 一旦新表的数据量一样的会耗时很长

  ##### 方案二：

  优势：不用再调整业务层的应用程序代码，只需要DBA迁移表即可
  劣势：新表可能会跟老表数据不一致，数据不完整；**脱机**操作过长可能会影响其他业务的正常运行

  ##### 方案三：

  优势：不影响业务层的应用程序代码，也不会导致数据丢失
  劣势：升级过程，必然要**脱机**，此过程时间过程一样会影响业务的正常运行

  > 个人建议在实际情况允许的情况下，如果大家所在的公司也出现类似的问题时，尽可能的还是采用方案三：升级服务器版本
  > 毕竟长远考虑，后续在业务的发展不确定情况下，原始表拓展加新的字段是很正常的一件事，升级到高版本后 因为引入了新的算法：即时算法 所以会毫秒级别的加字段 不会对业务发布上线造成影响。

#### 假设索引的某个叶子节点存放了100条记录（id，name，age）怎样快速找出id为60的记录

**答：**如果是有序的二分查找，无序的阐述各种排序的优缺点。

#### 主键索引与唯一索引哪个性能好？为什么？

**答：**回表的话主键更快，不回表唯一快，因为非聚簇索引的索引树更小。

#### 普通索引与唯一索引哪个更好？

- 使用查询时，普通索引与唯一索引性能差异可以忽略。

- 但是更新语言时，普通索引性能比唯一索引要好。

  > 那么为什么说"更新语言时，普通索引性能比唯一索引要好"呢？
  > 原因是:
  >         对于唯一索引来说，需要将数据页读入内存，判断到没有冲突，插入这个值，语句执行结束；
  >         对于普通索引来说，则是将更新记录在 change buffer，语句执行就结束了；
  >         将数据从磁盘读入内存涉及随机 IO 的访问，是数据库里面成本最高的操作之一。change buffer 因为减少了随机磁盘访问，所以对更新性能的提升是会很明显的。
  > 		普通索引更新语句时会将更新操作都会先缓存到change buffer中，且会一直累积，直到使用select语句查询该数据。才会调用merge，将change buffer中的数据写入磁盘，这样的操作减少了数据写入磁盘的次数。
  >         而mysql也会定时调用merge方法(在数据库正常关闭（shutdown）的过程中，也会执行 merge 操作)。

#### SQL应该建立几个索引？索引排序？

- 建立索引的几个原则：

  1、较频繁地作为查询条件的字段
  2、唯一性太差的字段不适合建立索引
  3、更新太频繁地字段不适合创建索引
  4、不会出现在where条件中的字段不该建立索引

- mysql支持两种方式的排序，filesort和index，**index效率高**，它指mysql扫描索引本身完成排序，filesort方式效率低。

  order by满足两下情况会使用index方式排序：
  
  1. order by语句使用索引最左前列。
  2. 使用where子句与order by子句条件列组合满足索引最左前列。
  
  如果不在索引列上，filesort有两种算法：双路排序和单路排序。
  
  - **双路排序过程:**
    MySQL 4.1 之前使用的双路排序，通过两次扫描磁盘得到数据。读取主键id 和 order by 列并对其进行排序，扫描排序好的列表，按照列表中的值重新从列表中读取对应的数据输出。
    1. 从索引 name 找到第一个满足 name = ‘自由的辣条’ 的主键id。
    2. 根据主键 id 取出整行，把排序字段 age 和主键 id 这两个字段放到 sort buffer(排序缓存) 。
    3. 从索引 name 取下一个满足 name = ‘自由的辣条’ 记录的主键 id。
    4. 重复 3、4 直到不满足 name = ‘自由的辣条’。
    5. 对 sort_buffer 中的字段 age 和主键 id 按照字段 age进行排序。
    6. 遍历排序好的 id 和字段 age ，按照 id 的值回到原表中取出 所有字段的值返回给客户端。
  
  - **单路排序过程：**
  
    从磁盘中读取查询需要的所有列，按照 order by 列在 sort_buffer(排序缓存) 缓冲区对他们进行排序，然后扫描排序后的列表输出。
  
    1. 从索引name找到第一个满足 name = ‘自由的辣条’ 条件的主键 id。
    2. 根据主键 id 取出整行，取出所有字段的值，存入 sort_buffer(排序缓存)中。
    3. 从索引name找到下一个满足 name = ‘自由的辣条’ 条件的主键 id。
    4. 重复步骤 2、3 直到不满足 name = ‘自由的辣条’。
    5. 对 sort_buffer 中的数据按照字段 age 进行排序。
    6. 返回结果给客户端。
  
    **总结：**单路排序效率更快，避免了二次读取数据，把随机IO变成了顺序IO，但是会使用更多的空间。

#### ABC三列字段，让你来建立索引，你会怎么建？为什么？

什么时候要索引？

　　1、表的主键、外键必须有索引

　　2、数据量超过300必须有索引

　　3、经常与其他表进行连接的表，在连接字段上建立索引

　　4、经常出现在where子句的字段，特别是大表字段，必须建索引

　　5、索引应建立在小字段上，对于大文本字段甚至超长字段，不要建索引

什么时候不需要索引？

　　1、建立组合索引，但查询谓词并未使用组合索引第一列，此时索引也是无效的

　　2、在包含有null值的table列上建索引，使用select count(*) from table时不会使用索引

　　3、在索引列上使用函数不会使用索引，除非新建函数索引

　　4、被索引的列进行隐式类型转换时不会使用索引

　　5、当查询数据量占整个表比重大时，full scan table采用多块读查询更快

#### 什么字段适合加索引?什么不适合加索引?

索引的过滤性,过滤性弱的不适合加索引（比如性别或者订单状态这种列）。
频繁增删改但是很少查询的字段不适合加索引
两个字段绑定在一起的用联合索引

#### 谈下数据库，大in的查询怎么处理？

**答：**oracle sql 语句in子句中（where id in (1, 2, ..., 1000, 1001)）， 如果子句中超过1000项就会报错。  这主要是oracle考虑性能问题做的限制。 

​		如果要解决问题，可以用 where id (1, 2, ..., 1000) or id... 。

> 意思是in过多可以使用or关键字来分段。

#### 跨库聚合怎么做，查询怎么做？

- ##### 同服务器的跨库查询：

  服务的跨库查询只需要在关联查询的时候带上数据名，SQL的写法是这样的：SELECT * FROM 数据库1.table1 x JOIN 数据库2.table2 y ON x.field1=y.field2；

  如：

  ![image-20210422112017363](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422112017363.png)

- ##### 不同服务的跨库查询：

  > ##### 要关联的表是：机器A上的数据库A中的表A && 机器B上的数据库B中的表B

  1. 先查看MySQL数据库是否安装了FEDERATED引擎，通过命令show engines;如下图：

     ![image-20210422112100757](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422112100757.png)

     如果有FEDERATED引擎，但Support是NO，说明你的mysql安装了这个引擎，但没启用，去my.cnf文件末添加一行  federated  ，重启mysql即可；

  2. 在机器A上的数据库A中建一个表B：

     ![image-20210422112900430](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422112900430.png)

     如上图，建表语句示例：

     **CREATE TABLE table_name(......) ENGINE =FEDERATED CONNECTION='mysql://[username]:[password]@[location]:[port]/[db-name]/[table-name]'**

     如：

     **CREATE TABLE app() ENGINE=FEDERATED DEFAULT CHARSET=utf8 CONNECTION='mysql://root:123456@127.0.0.1:3306/test/app1';**

     > app相当于数据库X的表B，app1相当于数据库Y的表B

     这种跨服务查询需要注意的几点：

     1. 本地的表结构必须与远程的完全一样。
     2. 远程数据库目前仅限MySQL
     3. 不支持事务
     4. 不支持表结构修改

#### 子查询为什么不推荐使用？

执行子查询时，MYSQL需要创建临时表，查询完毕后再删除这些临时表，所以，子查询的速度会受到一定的影响，这里多了一个创建和销毁临时表的过程。

#### update t1 set name=‘XX’ where id=10会锁几行数据？

以RR隔离级别为例（RC其实也一样，只是没有间隙锁）：

- 主键加锁：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422155326919.png" alt="image-20210422155326919" style="zoom:60%;" />

  加锁行为：仅在id=10的主键索引记录上加X锁。

- 唯一键加锁：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422155417997.png" alt="image-20210422155417997" style="zoom:60%;" />

  加锁行为：先在唯一索引id上加X锁，然后在id=10的主键索引记录上加X锁。

- 非唯一键加锁：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422155541235.png" alt="image-20210422155541235" style="zoom:60%;" />

  加锁行为：对满足id=10条件的记录和主键分别加X锁，然后在(6,c)-(10,b)、(10,b)-(10,d)、(10,d)- (11,f)范围分别加Gap Lock。

- 无索引加锁：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422155601003.png" alt="image-20210422155601003" style="zoom:60%;" />

  加锁行为：表里所有行和间隙都会加X锁。（当没有索引时，会导致全表锁定，因为InnoDB引擎锁机制是基于索引实现的记录锁定）。

#### 索引失效，除了语法层面，怎么让它重新走索引?

同样的sql如果在之前能够使用到索引，那么现在使用不到索引，以下几种主要情况：

1. 随着表的增长，where条件出来的数据太多，大于15%，使得索引失效（会导致CBO计算走索引花费大于走全表）。

2. 统计信息失效   需要重新搜集统计信息。

3. 索引本身失效   需要重建索引。

#### mysql表设计需要注意的点？

字符类型、是否为空、索引设置。

#### SQL题，查询某天考试所有科目的成绩最高的学生

![image-20210422210034483](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422210034483.png)

SELECT c.id,c.kemu,c.name,MAX(c.chenji) chenji FROM course c GROUP BY c.kemu **where DateDiff(dd,datetime,getdate())=0**；(其中datetime就是数据存的时间)。

DateDiff使用：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210422205803147.png" alt="image-20210422205803147" style="zoom:67%;" />

#### 什么是索引分裂？

![image-20210423143009073](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423143009073.png)

#### 数据库三大范式：

- 第一范式：每个列都不可以再拆分。

  ![image-20210423223908034](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423223908034.png)

  ​	比如上方的表，地址拆分为省份、城市、详细地址，已经不能够再拆分。

- 第二范式：在第一范式的基础上，非主键列完全依赖于主键，而不能是依赖于主键的一部分。**（确保表中的每列都和主键相关）**

  ​	第二范式需要确保数据库表中的每一列都和主键相关，而不能只与主键的某一部分相关（主要针对联合主键而言），**也就是说在一个数据库表中，一个表中只能保存一种数据，不可以把多种数据保存在同一张数据库表中。**

  ​	比如要设计一个订单信息表，因为订单中可能会有多种商品，所以要将订单编号和商品编号作为数据库表的联合主键，如下表所示。

   	**订单信息表**

  ​		![image-20210423225022560](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423225022560.png)

  ​	这样就产生一个问题：这个表中是以订单编号和商品编号作为联合主键。这样在该表中商品名称、单位、商品价格等信息不与该表的主键相关，而仅仅是与商品编号相关。所以在这里违反了第二范式的设计原则。

  而如果把这个订单信息表进行拆分，把商品信息分离到另一个表中，把订单项目表也分离到另一个表中，就非常完美了。如下所示：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423225135563.png" alt="image-20210423225135563" style="zoom:80%;" />

  这样设计，在很大程度上减小了数据库的冗余。如果要获取订单的商品信息，使用商品编号到商品信息表中查询即可。

- 第三范式：在第二范式的基础上，非主键列只依赖于主键，不依赖于其他非主键。（**确保每列都和主键列直接相关,而不是间接相关**）

  第三范式需要确保数据表中的每一列数据都和主键直接相关，而不能间接相关。

  比如在设计一个订单数据表的时候，可以将客户编号作为一个外键和订单表建立相应的关系。而不可以在订单表中添加关于客户其它信息（比如姓名、所属公司等）的字段。如下面这两个表所示的设计就是一个满足第三范式的数据库表。

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423225231443.png" alt="image-20210423225231443" style="zoom:80%;" />

  这样在查询订单信息的时候，就可以使用客户编号来引用客户信息表中的记录，也不必在订单信息表中多次输入客户信息的内容，减小了数据冗余。

在设计数据库结构的时候，要尽量遵守三范式，如果不遵守，必须有足够的理由。比如性能。事实上我们经常会为了性能而妥协数据库的设计。（）

#### mysql有关权限的表都有哪几个

MySQL服务器通过权限表来控制用户对数据库的访问，权限表存放在mysql数据库里，由mysql_install_db脚本初始化。这些权限表分别user，db，table_priv，columns_priv和host。下面分别介绍一下这些表的结构和内容：

- user权限表：记录允许连接到服务器的用户帐号信息，里面的权限是全局级的。
- db权限表：记录各个帐号在各个数据库上的操作权限。
- table_priv权限表：记录数据表级的操作权限。
- columns_priv权限表：记录数据列级的操作权限。
- host权限表：配合db权限表对给定主机上数据库级操作权限作更细致的控制。这个权限表不受GRANT和REVOKE语句的影响。

#### mysql有哪些数据类型

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423230254461.png" alt="image-20210423230254461" style="zoom:80%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210423230315549.png" alt="image-20210423230315549" style="zoom:80%;" />

- 1、**整数类型**，包括TINYINT、SMALLINT、MEDIUMINT、INT、BIGINT，分别表示1字节、2字节、3字节、4字节、8字节整数。任何整数类型都可以加上UNSIGNED属性，表示数据是无符号的，即非负整数。
  `长度`：整数类型可以被指定长度，例如：INT(11)表示长度为11的INT类型。长度在大多数场景是没有意义的，它不会限制值的合法范围，只会影响显示字符的个数，而且需要和UNSIGNED ZEROFILL属性配合使用才有意义。
  `例子`，假定类型设定为INT(5)，属性为UNSIGNED ZEROFILL，如果用户插入的数据为12的话，那么数据库实际存储数据为00012。

- 2、**实数类型**，包括FLOAT、DOUBLE、DECIMAL。
  DECIMAL可以用于存储比BIGINT还大的整型，能存储精确的小数。
  而FLOAT和DOUBLE是有取值范围的，并支持使用标准的浮点进行近似计算。
  计算时FLOAT和DOUBLE相比DECIMAL效率更高一些，DECIMAL你可以理解成是用字符串进行处理。

- 3、**字符串类型**，包括VARCHAR、CHAR、TEXT、BLOB
  VARCHAR用于存储可变长字符串，**它比定长类型更节省空间**。
  VARCHAR使用额外1或2个字节存储字符串长度。列长度小于255字节时，使用1字节表示，否则使用2字节表示。
  VARCHAR存储的内容超出设置的长度时，内容会被截断。
  CHAR是定长的，根据定义的字符串长度分配足够的空间。
  CHAR会根据需要使用空格进行填充方便比较。
  CHAR适合存储很短的字符串，或者所有值都接近同一个长度。
  CHAR存储的内容超出设置的长度时，内容同样会被截断。

  **使用策略：**
  对于经常变更的数据来说，CHAR比VARCHAR更好，因为CHAR不容易产生碎片。
  对于非常短的列，CHAR比VARCHAR在存储空间上更有效率。
  使用时要注意只分配需要的空间，更长的列排序时会消耗更多内存。
  尽量避免使用TEXT/BLOB类型，查询时会使用临时表，导致严重的性能开销。

  > ### varchar与char的区别
  >
  > **char的特点**
  >
  > - char表示定长字符串，长度是固定的；
  > - 如果插入数据的长度小于char的固定长度时，则用空格填充；
  > - 因为长度固定，所以存取速度要比varchar快很多，甚至能快50%，但正因为其长度固定，所以会占据多余的空间，是空间换时间的做法；
  > - 对于char来说，最多能存放的字符个数为255，和编码无关
  >
  > **varchar的特点**
  >
  > - varchar表示可变长字符串，长度是可变的；
  > - 插入的数据是多长，就按照多长来存储；
  > - varchar在存取方面与char相反，它存取慢，因为长度不固定，但正因如此，不占据多余的空间，是时间换空间的做法；
  > - 对于varchar来说，最多能存放的字符个数为65532
  >
  > 总之，结合性能角度（char更快）和节省磁盘空间角度（varchar更小），具体情况还需具体来设计数据库才是妥当的做法。
  >
  > ### varchar(50)中50的涵义
  >
  > 最多存放50个字符，varchar(50)和(200)存储hello所占空间一样，但后者在排序时会消耗更多内存，因为order by col采用fixed_length计算col长度(memory引擎也一样)。在早期 MySQL 版本中， 50 代表字节数，现在代表字符数。
  >
  > ### int(20)中20的涵义
  >
  > 是指显示字符的长度。20表示最大显示宽度为20，但仍占4字节存储，存储范围不变；
  >
  > 不影响内部存储，只是影响带 zerofill 定义的 int 时，前面补多少个 0，易于报表展示
  >
  > ### mysql为什么这么设计
  >
  > 对大多数应用没有意义，只是规定一些工具用来显示字符的个数；int(1)和int(20)存储和计算均一样；
  >
  > ### mysql中int(10)和char(10)以及varchar(10)的区别
  >
  > - int(10)的10表示显示的数据的长度，不是存储数据的大小；chart(10)和varchar(10)的10表示存储数据的大小，即表示存储多少个字符。
  >
  >   int(10) 10位的数据长度 9999999999，占32个字节，int型4位
  >   char(10) 10位固定字符串，不足补空格 最多10个字符
  >   varchar(10) 10位可变字符串，不足补空格 最多10个字符
  >
  > - char(10)表示存储定长的10个字符，不足10个就用空格补齐，占用更多的存储空间
  >
  > - varchar(10)表示存储10个变长的字符，存储多少个就是多少个，空格也按一个字符存储，这一点是和char(10)的空格不同的，char(10)的空格表示占位不算一个字符
  
- 4、**枚举类型（ENUM）**，把不重复的数据存储为一个预定义的集合。
  有时可以使用ENUM代替常用的字符串类型。
  ENUM存储非常紧凑，会把列表值压缩到一个或两个字节。
  ENUM在内部存储时，其实存的是整数。
  尽量避免使用数字作为ENUM枚举的常量，因为容易混乱。
  排序是按照内部存储的整数

- 5、**日期和时间类型**，尽量使用timestamp，空间效率高于datetime，
  用整数保存时间戳通常不方便处理。
  如果需要存储微妙，可以使用bigint存储。
  看到这里，这道真题是不是就比较容易回答了。

#### 索引使用场景

where

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210424132309924.png" alt="image-20210424132309924" style="zoom:80%;" />

上图中，根据`id`查询记录，因为`id`字段仅建立了主键索引，因此此SQL执行可选的索引只有主键索引，如果有多个，最终会选一个**较优的作为检索**的依据。

#### 哈希索引和Btree索引的比较

1.在精确查找的情况下：hash索引要高于btree索引，因为hash索引查找数据基本上能一次定位数据（大量hash值相等的情况下性能会有所降低，也可能低于btree）,而btree索引基于节点上查找，所以在精确查找方面hash索引一般会高于btree索引。

2.在范围性查找情况下：比如 'like'等范围性查找hash索引无效，因为hash算法是基于等值计算的。

3.btree支持的联合索引的最优前缀；hash是无法支持的，hash联合索引要么全用，要么全不用。

4.hash是不支持索引排序的，索引值和hash计算出来的hash值大小并不一定一致。（因为hash 索引指向的数据是无序的，所以order by使用不到索引排序）

5.hash索引任何时候都避免不了回表查询数据，而B+树在符合某些条件(聚簇索引，覆盖索引等)的时候可以只通过索引完成查询。

![image-20210424133748988](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210424133748988.png)

#### 创建索引的三种方式，删除索引

第一种方式：在执行CREATE TABLE时创建索引

```sql
CREATE TABLE user_index2 (
	id INT auto_increment PRIMARY KEY,
	first_name VARCHAR (16),
	last_name VARCHAR (16),
	id_card VARCHAR (18),
	information text,
	KEY name (first_name, last_name),
	FULLTEXT KEY (information),
	UNIQUE KEY (id_card)
);
```

第二种方式：使用ALTER TABLE命令去增加索引

```sql
ALTER TABLE table_name ADD INDEX index_name (column_list);
```

第三种方式：使用CREATE INDEX命令创建

```sql
CREATE INDEX index_name ON table_name (column_list);
```

删除索引：

根据索引名删除普通索引、唯一索引、全文索引：`alter table 表名 drop KEY 索引名`

```sql
alter table user_index drop KEY name;
alter table user_index drop KEY id_card;
alter table user_index drop KEY information;
```

删除主键索引：`alter table 表名 drop primary key`（因为主键只有一个）。这里值得注意的是，如果主键自增长，那么不能直接执行此操作（自增长依赖于主键索引）:

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210424134753761.png" alt="image-20210424134753761" style="zoom:80%;" />

需要需要取消自增长再行删除：

```sql
alter table user_index
-- 重新定义字段
MODIFY id int,
drop PRIMARY KEY
```

但通常不会删除主键，因为设计主键一定与业务逻辑无关。

#### 创建索引时需要注意什么？

- 非空字段：应该指定列为NOT NULL，除非你想存储NULL。在mysql中，含有空值的列很难进行查询优化，因为它们使得索引、索引的统计信息以及比较运算更加复杂。你应该用0、一个特殊的值或者一个空串代替空值；

- 取值离散大的字段：（变量各个取值之间的差异程度）的列放到联合索引的前面，可以通过count()函数查看字段的差异值，返回值越大说明字段的唯一值越多字段的离散程度高；
- 索引字段越小越好：数据库的数据存储以页为单位一页存储的数据越多一次IO操作获取的数据越大效率越高。

#### 什么是触发器？触发器的使用场景有哪些？

触发器是用户定义在关系表上的一类由事件驱动的特殊的存储过程。触发器是指一段代码，当触发某个事件时，自动执行这些代码。

使用场景

- 可以通过数据库中的相关表实现级联更改。
- 实时监控某张表中的某个字段的更改而需要做出相应的处理。
- 例如可以生成某些业务的编号。
- 注意不要滥用，否则会造成数据库及应用程序的维护困难。

#### MySQL中都有哪些触发器？

在MySQL数据库中有如下六种触发器：

- Before Insert
- After Insert
- Before Update
- After Update
- Before Delete
- After Delete

#### SQL 约束有哪几种？

- NOT NULL: 用于控制字段的内容一定不能为空（NULL）。

- UNIQUE: 控件字段内容不能重复，一个表允许有多个 Unique 约束。
- PRIMARY KEY: 也是用于控件字段内容不能重复，但它在一个表只允许出现一个。
- FOREIGN KEY: 用于预防破坏表之间连接的动作，也能防止非法数据插入外键列，因为它必须是它指向的那个表中的值之一。
- CHECK: 用于控制字段的值范围。

#### 六种关联查询

- 交叉连接（CROSS JOIN）
- 内连接（INNER JOIN）
- 外连接（LEFT JOIN/RIGHT JOIN）
- 联合查询（UNION与UNION ALL）
- 全连接（FULL JOIN）
- 交叉连接（CROSS JOIN）

#### drop、delete与truncate的区别

三者都表示删除，但是三者有一些差别：

|          | Delete                                   | Truncate                       | Drop                                                 |
| -------- | ---------------------------------------- | ------------------------------ | ---------------------------------------------------- |
| 类型     | 属于DML                                  | 属于DDL                        | 属于DDL                                              |
| 回滚     | 可回滚                                   | 不可回滚                       | 不可回滚                                             |
| 删除内容 | 表结构还在，删除表的全部或者一部分数据行 | 表结构还在，删除表中的所有数据 | 从数据库中删除表，所有的数据行，索引和权限也会被删除 |
| 删除速度 | 删除速度慢，需要逐行删除                 | 删除速度快                     | 删除速度最快                                         |

因此，在不再需要一张表的时候，用drop；在想删除部分数据行时候，用delete；在保留表而删除所有数据的时候用truncate。

- ##### DML(Data Manipulation Language)数据操纵语言：

  适用范围：对数据库中的数据进行一些简单操作，如insert,delete,update,select等.

- ##### DDL(Data Definition Language)数据定义语言：

  适用范围：对数据库中的某些对象(例如，database,table)进行管理，如Create,Alter和Drop.

#### 什么是数据库视图	

视图其实就是把一些复杂的sql封装成函数，然后可以重复调用函数实现数据库操作。

- ##### 创建视图（②是使用）：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210425002120015.png" alt="image-20210425002120015" style="zoom:80%;" />

- ##### 修改视图：

  - ##### 方式一

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210425002347859.png" alt="image-20210425002347859" style="zoom:80%;" />

  - ##### 方式二

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210425002423911.png" alt="image-20210425002423911" style="zoom:80%;" />

- 删除和查看视图：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210425002642605.png" alt="image-20210425002642605" style="zoom:80%;" />

- ##### 数据库表和视图的区别：

  1、视图是已经编译好的sql语句，而表不是；
  2、视图没有实际的物理记录，而表有；
  3、表是内容，视图是窗口；
  4、表占用物理空间而视图不占用物理空间，视图只是逻辑概念的存在，表可以及时对它进行修改，但视图只能用创建的语句来修改；
  5、表是三级模式结构中的概念模式，试图是外模式；

  6、视图是查看数据表的一种方法，可以查询数据表中某些字段构成的数据，只是一些SQL语句的集合，从安全的角度说，视图可以不给用户接触数据表，从而不知道表结构；
  7、表属于全局模式中的表，是实表，视图属于局部模式的表，是虚表；
  8、视图的建立和删除只影响视图本身，不影响对应的基本表；
  9、不能对视图进行update或者insert into操作。

- ##### 数据库表和视图的联系：

  1、视图（view）是在基本表之上建立的表，它的结构（即所定义的列）和内容（即所有数据行）都来自基本表，它依据基本表存在而存在；
  2、一个视图可以对应一个基本表，也可以对应多个基本表；
  3、视图是基本表的抽象和在逻辑意义上建立的新关系。
  **总结：**视图是一个子查询，性能肯定会比直接查询要低（尽管sql内部有优化），所以使用视图时有一个必须要注意的，就是不要嵌套使用查询，尤其是复杂查询。

- ##### 视图有什么用

  1、当一个查询需要频频的作为子查询使用时，视图可以简化代码，直接调用而不是每次都去重复写这个东西。
  2、系统的数据库管理员需要给他人提供一张表的某两列数据，而不希望他可以看到其他任何数据，这时可以建一个只有这两列数据的视图，然后把视图公布给他。

- ##### 性能损失解决方案

  对视图的查询语句进行优化。
  通常来说直接查询和查询视图是没有什么区别的（sql 本身会进行优化），除非是视图嵌套了视图，或者子查询很复杂要计算。
  **特别说明：**每次SELECT视图的时候，视图都会重新计算创建它的规则（sql算法），如果算法复杂，数据量大，就会比较慢，那样每次就很慢了。
  而且，表的索引对于视图view来说是无效的，它是全表扫描的。

#### 大表数据查询，怎么优化

1. 优化shema、sql语句+索引；
2. 第二加缓存，memcached, redis；
3. 主从复制，读写分离；
4. 垂直拆分，根据你模块的耦合度，将一个大的系统分为多个小的系统，也就是分布式系统；
5. 水平切分，针对数据量大的表，这一步最麻烦，最能考验技术水平，要选择一个合理的sharding key, 为了有好的查询效率，表结构也要改动，做一定的冗余，应用也要改，sql中尽量带sharding key，将数据定位到限定的表上去查，而不是扫描全部的表；

> #### 什么是shema？
>
> 在数据库中，schema（发音 “skee-muh” 或者“skee-mah”，中文叫模式）是数据库的组织和结构，*schemas* 和*schemata*都可以作为复数形式。模式中包含了schema对象，可以是**表**(table)、**列**(column)、**数据类型**(data type)、**视图**(view)、**存储过程**(stored procedures)、**关系**(relationships)、**主键**(primary key)、**外键(**foreign key)等。数据库模式可以用一个可视化的图来表示，它显示了数据库对象及其相互之间的关系。

#### MySQL数据库cpu飙升到500%的话他怎么处理？

当 cpu 飙升到 500%时，先用操作系统命令 top 命令观察是不是 mysqld 占用导致的，如果不是，找出占用高的进程，并进行相关处理。

如果是 mysqld 造成的， show processlist，看看里面跑的 session 情况，是不是有消耗资源的 sql 在运行。找出消耗高的 sql，看看执行计划是否准确， index 是否缺失，或者实在是数据量太大造成。

一般来说，肯定要 kill 掉这些线程(同时观察 cpu 使用率是否下降)，等进行相应的调整(比如说加索引、改 sql、改内存参数)之后，再重新跑这些 SQL。

也有可能是每个 sql 消耗资源并不多，但是突然之间，有大量的 session 连进来导致 cpu 飙升，这种情况就需要跟应用一起来分析为何连接数会激增，再做出相应的调整，比如说限制连接数等

#### 什么是having、游标

- ##### MySQL中的where和having的区别：

  where子句：是在**分组之前**使用（之后的话会报错），表示从所有数据中筛选出部分数据，以完成分组的要求，

  ​            在where子句中不允许使用统计函数，没有group by子句也可以使用。

  having子句：是在**分组之后**使用的（之前的话会报错），表示对分组统计后的数据执行再次过滤，可以使用

  ​            统计函数，有group  by子句之后才可以出现having子句。

  ##### where和having都可以使用的场景

  ```sql
  select goods_price,goods_name from sw_goods where goods_price > 100
  ```

  ```sql
  select goods_price,goods_name from sw_goods having goods_price > 100
  ```

  ##### 只可以用where，不可以用having的情况

  ```sql
  select goods_name,goods_number from sw_goods where goods_price > 100
  ```

  ```sql
  select goods_name,goods_number from sw_goods having goods_price > 100 //报错！！！因为前面并没有筛选出goods_price 字段
  ```

  ##### 只可以用having，不可以用where情况

  ```sql
  select goods_category_id , avg(goods_price) as ag from sw_goods group by goods_category having ag > 1000
  ```

  ```sql
  select goods_category_id , avg(goods_price) as ag from sw_goods where ag > 1000 group by goods_category //报错！！因为from sw_goods 这张数据表里面没有ag这个字段
  ```

- ##### 游标：

#### MySQL数据库cpu飙升到500%的话他怎么处理？

当 cpu 飙升到 500%时，先用操作系统命令 top 命令观察是不是 mysqld 占用导致的，如果不是，找出占用高的进程，并进行相关处理。

输入top -c命令：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210425145123744.png" alt="image-20210425145123744" style="zoom:80%;" />

如果是 mysqld 造成的， show processlist，看看里面跑的 session 情况，是不是有消耗资源的 sql 在运行。找出消耗高的 sql，看看执行计划是否准确， index 是否缺失，或者实在是数据量太大造成。

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210425164202466.png" alt="image-20210425164202466" style="zoom:80%;" />

一般来说，肯定要 kill 掉这些线程(同时观察 cpu 使用率是否下降)，等进行相应的调整(比如说加索引、改 sql、改内存参数)之后，再重新跑这些 SQL。

也有可能是每个 sql 消耗资源并不多，但是突然之间，有大量的 session 连进来导致 cpu 飙升，这种情况就需要跟应用一起来分析为何连接数会激增，再做出相应的调整，比如说限制连接数等

#### explain的索引和实际使用的索引完全不一样怎么办？

先分析具体原因，实在不行强制走索引。

#### 项目遇到什么问题？

导出时查询数据够多很慢；分页慢；

#### 怎么排查sql是否存在问题，或者有没有遇到相关问题？

查询慢的问题排查，根据讲义的去说

#### 为什么存储引擎不采用红黑树？

1. b+树有更低的树高，查询速度更快。

#### 有没有遇到MySQL主从的数据一致性问题，怎么解决的？

先上Master库：

```cmd
mysql>show processlist;
```

查看下进程是否Sleep太多。发现很正常。

```cmd
show master status;
```

也正常。

```cmd
mysql> show master status;
+-------------------+----------+--------------+-------------------------------+
| File              | Position | Binlog_Do_DB | Binlog_Ignore_DB              |
+-------------------+----------+--------------+-------------------------------+
| mysqld-bin.000001 |     3260 |              | mysql,test,information_schema |
+-------------------+----------+--------------+-------------------------------+
1 row in set (0.00 sec)
```

再到Slave上查看

```cmd
mysql> show slave status\G                                                
 
Slave_IO_Running: Yes
Slave_SQL_Running: No
```

可见是Slave不同步

- #### 解决方案

  下面介绍两种解决方法

  - **方法一：忽略错误后，继续同步**

    该方法适用于主从库数据相差不大，或者要求数据可以不完全统一的情况，数据要求不严格的情况

    解决：

    ```cmd
    stop slave;
     
    #表示跳过一步错误，后面的数字可变
    set global sql_slave_skip_counter =1;
    start slave;
    ```

    之后再用mysql> show slave status\G 查看

    ```cmd
    mysql> show slave status\G
    Slave_IO_Running: Yes
    Slave_SQL_Running: Yes
    ```

    ok，现在主从同步状态正常了。。。

  - **方式二：重新做主从，完全同步**

    该方法适用于主从库数据相差较大，或者要求数据完全统一的情况

    解决步骤如下：

    1.先进入主库，进行锁表，防止数据写入

    使用命令：

    ```cmd
    mysql> flush tables with read lock;
    ```

    注意：该处是锁定为只读状态，语句不区分大小写

    2.进行数据备份

    \#把数据备份到mysql.bak.sql文件

    ```cmd
    mysqldump -uroot -p -hlocalhost > mysql.bak.sql
    ```

    这里注意一点：数据库备份一定要定期进行，可以用shell脚本或者python脚本，都比较方便，确保数据万无一失。

    3.查看master 状态

    ```cmd
    mysql> show master status;
    +-------------------+----------+--------------+-------------------------------+
    | File              | Position | Binlog_Do_DB | Binlog_Ignore_DB              |
    +-------------------+----------+--------------+-------------------------------+
    | mysqld-bin.000001 |     3260 |              | mysql,test,information_schema |
    +-------------------+----------+--------------+-------------------------------+
    1 row in set (0.00 sec)
    ```

    4.把mysql备份文件传到从库机器，进行数据恢复

    ```cmd
    scp mysql.bak.sql root@192.168.128.101:/tmp/
    ```

    5.停止从库的状态

    ```cmd
    mysql> stop slave;
    ```

    6.然后到从库执行mysql命令，导入数据备份

    ```cmd
    mysql> source /tmp/mysql.bak.sql
    ```

    7.设置从库同步，注意该处的同步点，就是主库show master status信息里的| File| Position两项

    ```cmd
    change master to master_host = '192.168.128.100', master_user = 'rsync', master_port=3306, master_password='', master_log_file = 'mysqld-bin.000001', master_log_pos=3260;
    ```

    8.重新开启从同步

    ```cmd
    mysql> start slave;
    ```

    9.查看同步状态

    ```cmd
    mysql> show slave status\G  
    
    Slave_IO_Running: Yes
    Slave_SQL_Running: Yes
    ```

    10.回到主库并执行如下命令解除表锁定。

    ```cmd
    UNLOCK TABLES
    ```



#### count(*) 慢该如何解决？

MyISAM能实时存储行数，Innodb由于MVCC存在，只能全表扫描得到行数。

解决方案：

- 使用redis存储行数：但是会有丢失风险和数据不一致问题。

  数据不一致问题：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210512204312278.png" alt="image-20210512204312278" style="zoom:80%;" />

  如上图，由于没有隔离性，会话B在T3阶段会读到已插入数据的行数，但实际数据库还没提交，如果提交阶段失败，那么就会出现数据不一致问题。

- 使用数据库存储行数：

  能解决上面缓存储存的两个问题（因为会话B会有事务隔离性，读不到未提交的会话A）。

#### 怎么给字符串对象加索引？

跟据场景选择前缀索引和全字段匹配索引

前缀索引：

```java
mysql> alter table SUser add index index1(email);//全字段匹配索引
或
mysql> alter table SUser add index index2(email(6)); //这就是前缀索引
```

全字段匹配索引存储结构：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210512205837927.png" alt="image-20210512205837927" style="zoom:80%;" />

前缀索引存储结构：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210512205916618.png" alt="image-20210512205916618" style="zoom:80%;" />

如上图，前缀索引只存储字符串前6位，但是有可能查的时候索引都差不多，结果每个还要再单独查全字段是否对应，这样效率反而很低。

所以前缀索引适合在前缀字段区分度高的时候使用，还有使用辅助索引可能还要取回表再查一遍。

#### 对于千万级的大表，MySQL 要怎么优化？

很多人第一反应是各种切分；我给的顺序是:
	第一：优化你的sql和索引；

​	第二：加缓存，memcached,redis；

​	第三：以上都做了后，还是慢，就做主从复制或主主复制，读写分离，可以在应用层做，效率高，也可以用三方工具，第三方工具推荐360的atlas,其它的要么效率不高，要么没人维护；

​	第四：如果以上都做了还是慢，不要想着去做切分，mysql自带分区表，先试试这个，对你的应用是透明的，无需更改代码,但是sql语句是需要针对分区表做优化的，sql条件中要带上分区条件的列，从而使查询定位到少量的分区上，否则就会扫描全部分区，另外分区表还有一些坑，在这里就不多说了；

​	第五：如果以上都做了，那就先做垂直拆分，其实就是根据你模块的耦合度，将一个大的系统分为多个小的系统，也就是分布式系统；

​	第六：才是水平切分，针对数据量大的表，这一步最麻烦，最能考验技术水平，要选择一个合理的sharding key,为了有好的查询效率，表结构也要改动，做一定的冗余，应用也要改，sql中尽量带sharding key，将数据定位到限定的表上去查，而不是扫描全部的表；

​	mysql数据库一般都是按照这个步骤去演化的，成本也是由低到高；

#### 为什么临时表可以重名？

建表语法：create temporary

因为每个线程维护自己的临时表，临时表只能被创建的session访问，所以在session结束的时候，会自动删除表，不同session的临时表是可以重名的，如果有多个session同时执行join优化，不需要担心表名重复到时建表失败的问题。

#### 什么时候使用内部临时表？

使用left join和right join、union、group by都是用到了临时表。

临时表使用的场景定义：

1. 如果语句执行过程可以一边读数据，一边直接得到结果，是不需要额外内存的，否则就需要额外的内存，来保存中间结果。
2.  join_buffer是无序数组，sort_buffer是有序数组，临时表是二维表结构。
3. 如果执行逻辑需要用到二维表特性，就会优先考虑使用临时表，比如我们的例子中，union需要用到唯一索引约束，group by还需要用到另外一个字段来存累计计数。

#### mysql和oracle的区别？

1. ##### 对事务的提交：

   MySQL默认是自动提交，而Oracle默认不自动提交，需要用户手动提交，需要在写commit;指令或者点击commit按钮

2. ##### 分页查询语句不同：

    MySQL是直接在SQL语句中写"select... from ...where...limit x, y",有limit就可以实现分页;而Oracle则是需要用到伪列ROWNUM和嵌套查询，如下代码：

   ```java
   SELECT * FROM  
   (  
   SELECT A.*, ROWNUM RN  
   FROM (SELECT * FROM TABLE_NAME) A  
   WHERE ROWNUM <= 40  
   )  
   WHERE RN >= 21
   ```

3. ##### 事务隔离级别：

   MySQL是read commited的隔离级别，而Oracle是repeatable read的隔离级别。

4. ##### 对事务的支持：

      MySQL在innodb存储引擎的行级锁的情况下才可支持事务，而Oracle则完全支持事务。

5. ##### 保存数据的持久性：

      MySQL是在数据库更新或者重启，则会丢失数据，Oracle把提交的sql操作线写入了在线联机日志文件中，保持到了磁盘上，可以随时恢复。

6. ##### 并发性：

      MySQL以表级锁为主，对资源锁定的粒度很大，如果一个session对一个表加锁时间过长，会让其他session无法更新此表中的数据。
    虽然InnoDB引擎的表可以用行级锁，但这个行级锁的机制依赖于表的索引，如果表没有索引，或者sql语句没有使用索引，那么仍然使用表级锁。
    Oracle使用行级锁，对资源锁定的粒度要小很多，只是锁定sql需要的资源，并且加锁是在数据库中的数据行上，不依赖与索引。所以Oracle对并发性的支持要好很多。

7. ##### 性能诊断：

      MySQL的诊断调优方法较少，主要有慢查询日志。
      Oracle有各种成熟的性能诊断调优工具，能实现很多自动分析、诊断功能。比如awr、addm、sqltrace、tkproof等   

8. ##### 权限与安全：

      MySQL的用户与主机有关，感觉没有什么意义，另外更容易被仿冒主机及ip有可乘之机。
      Oracle的权限与安全概念比较传统，中规中矩。

9. ##### 分区表和分区索引：

      MySQL的分区表还不太成熟稳定。
      Oracle的分区表和分区索引功能很成熟，可以提高用户访问db的体验。

10. ##### 复制：

       MySQL:复制服务器配置简单，但主库出问题时，丛库有可能丢失一定的数据。且需要手工切换丛库到主库。
       Oracle:既有推或拉式的传统数据复制，也有dataguard的双机或多机容灾机制，主库出现问题是，可以自动切换备库到主库，但配置管理较复杂。

11. ##### 最重要的区别：

       MySQL是轻量型数据库，并且免费，没有服务恢复数据。
       Oracle是重量型数据库，收费，Oracle公司对Oracle数据库有任何服务。

#### order by是怎么工作的？





#### 其他：

- ##### buffer刷盘策略

- ##### 更新表前10万行的数据，怎样做？

- ##### 断电如何保持数据一致

- ##### 冷热数据怎么分离？

#### SQL 使用 Join 好，还是多次 Select 好？

straight join和left join区别：

> 使用left join，数据库会自己选择加载表的顺序，而straight join是按照指定表对的顺序加载的，
> 可以看一下left join使用了过多的操作，explain分析一下就行（A表有1000条数据，B表有2000条数据1000^2000和2000^1000次方的区别）

```java
select * from t1 straight_join t2 on (t1.a=t2.a);
```

如上代码，在Mysql的实现中，Nested-Loop Join有3种实现的算法：

Simple Nested-Loop Join：SNLJ，简单嵌套循环连接
Index Nested-Loop Join：INLJ，索引嵌套循环连接
Block Nested-Loop Join：BNLJ，缓存块嵌套循环连接