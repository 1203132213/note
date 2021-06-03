[TOC]

# ZooKeeper单机：

### 什么是ZooKeeper？

ZooKeeper是一个开源的**分布式协调服务**，设计目标是**将分布式一致性封装起来，给用户提供简单的接口**。

分布式可以基于它实现诸如数据订阅/发布、负载均衡、命名服务、集群管理、分布式锁、分布式队列等功能。

## ZooKeeper内部结构：

### ZooKeeper三种角色以及请求处理链：

> 注：请求处理链都是使用责任链模式

- #### Leader主要工作：

  1. 事务请求的唯一处理者和调度者。
  2. 保证事务处理的顺序性。

Leader请求处理链：

![image-20201001214304362](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001214304362.png)

PrepRequestProcessor：对事务请求预处理，如创建请求事务头、事务体等。

ProposalRequestProcessor：事务投票处理器，，对于事务请求转发到CommitProcessor处理，对于非事务请求转达到CommitProcessor外。

SyncRequestProcessor：事务日志处理器，将事务请求记录到日志文件中。

AckRequestProcessor：发送Ack反馈，以通知ProposalRequestProcessor完成事务日志记录。

CommitProcessor：对于非事务直接交付给下一级处理，对于事务等待集群投票通过即可提交。

ToBeCommitProcessor：**该处理器有一个toBeApplied队列**，存储CommitProcessor处理过可被提交的Proposal，再将请求交付给FinalRequestProcessor处理，处理完后，将其从toBeApplied队列移除。

FinalRequestProcessor：创建客户端请求响应，还负责将事务应用到内存数据库中。

- #### Follower主要工作：

  1. 处理客户端非事务请求，转发事务给Leader服务器。
  2. 参与事务请求投票
  3. 参与Leader选举投票

Follower请求处理链：

![image-20201001220408605](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001220408605.png)

FollowerRequestProcessor：识别是否为事务请求，是则将请求转发到Leader服务器，不是再往下走。

SendAckRequestProcessor：事务完成日志记录，向Leader服务器发送Ack消息表明完成事务日志记录工作。

- #### Observer主要工作：

  1. 和Follower一样非事务请求自己处理，事务请求发给Leader处理，只是Observer不参与事务和Leader选举投票。
  2. Observer请求处理链：和Follower一样。

### ZooKeeper的节点存储哪些版本：

对于每个ZNode，ZooKeeper都为其维护一个叫stat数据结构，stat记录三个数据版本。

分别是：

- version：当前ZNode版本
- cversion：当前ZNode的子节点版本
- aversion：ACL版本

### 什么是ZooKeeper的ZNode：

ZNode是ZooKeeper最小的数据单位。ZNode下面可以再挂ZNode，形成ZNode树。

- ZNode的管理，如图所示，可通过节点路径（由斜杠/分割）定位到该节点，然后写入数据。

  ![image-20200928014827278](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200928014827278.png)

- ZNode类型：

  1. 持久节点：ZooKeeper最常见的节点，节点创建后一直存在服务器，直到删除操作主动清除。

     ```java
     create /hadoop "123456" //创建持久化节点并写入数据
     ```

  2. 持久顺序节点：指有顺序的持久节点，创建该节点时，节点名后加一个数字后缀表示顺序。

     ```java
     create -s /hadoop "123456"//创建持久化顺序节点并写入数据
     ```

  3. 临时节点：生命周期与会话绑在一起，会话结束，节点也会删掉。与持久性节点不同的是，**临时节点不能创建子节点。**

     ```java
     create -e /hadoop "123456" //创建临时节点并写入数据
     ```

  4. 临时顺序节点：就是有顺序的临时节点，和持久顺序节点类似。

     ```java
     create -s -e /hadoop "123456" //创建临时顺序节点并写入数据
     ```

  ##### 节点的内部结构：

  输入命令stat /XXX就可以看到节点内部结构，如下图：

  ![image (8).png](https://s0.lgstatic.com/i/image/M00/02/DA/Ciqc1F6yL-yAKn9QAABsJSpQkFI688.png)

  参数对应说明：

  ![表.png](https://s0.lgstatic.com/i/image/M00/03/C1/Ciqc1F6zbwWAVkt5AAC_yMQVCFo712.png)

  ##### 数据节点的版本：

  如上图，每个节点有三个版本信息，对数据节点的任何更新操作都会引起版本号的变化，ZooKeeper 的版本信息表示的是对节点数据内容、子节点信息或者是 ACL 信息的**修改次数**。

- 事务ID：

  该事务和数据库事务有区别，该事务指能改变ZooKeeper服务器状态的操作（又称事务操作或更新操作），一般包括数据节点的创建、删除、更新等操作。**每一个事务请求，ZooKeeper都会分配唯一事务ID**，用ZXID表示，通常是64位数字，每一次ZXID对应一次更新操作，从ZXID间接识别ZooKeeper处理更新操作请求的全局顺序。

  ##### 面试题：为什么 ZooKeeper 不能采用相对路径查找节点呢？
  
  **答：**因为像这种查找与给定值相等的记录问题最适合用散列来解决， 因此ZooKeeper 在底层实现的时候，使用了一个 hashtable，即  hashtableConcurrentHashMap<String, DataNode>nodes ，用节点的完整路径来作为 key 存储节点数据。这样就大大提高了 ZooKeeper 的性能。

### 什么是ZooKeeper的ACL：

**为什么ZooKeeper要引入ACL？**

ZooKeeper内部存储了分布式系统运行状态的元数据，元数据会直接影响分布式运行状态。因此引入ACL保证数据安全。

**ACL由什么组成？**

ACL由三部分组成：权限模式（Scheme）、授权对象（ID）、权限（Permission），**通常使用“Scheme：ID：Permission”标识有效的ACL信息。**（通俗的讲ACL这三部分，前两部分是判断ZooKeeper的选择，最后一部分是控制该ZooKeeper的操作)

权限模式（Scheme）和授权对象（ID）对应关系：

| 权限模式 | 授权对象                                                     |
| -------- | ------------------------------------------------------------ |
| IP       | IP地址进行权限控制，也可使用网段控制，如：“ip:192.168.0.1/24” 表示针对192.168.0.*进行权限控制 |
| Digest   | 最常见的权限控制模式，BASE64(SHA-1(username:password))，将自定义的用户名和密码：username:password进行SHA-1加密和BASE64编码 |
| World    | 只有一个ID：anyone                                           |
| Super    | 超级用户，可以对任意ZooKeeper进行任何操作                    |

数据操作权限：

- CREATE：创建子节点权限

- READ：获取节点数据和节点列表权限

- WRITE：更细腻节点数据权限

- DELETE：删除子节点权限

- ADMIN：设置节点ACL权限

  需注意的是CREATE和DELETE都是针对子节点的权限控制。

**创建Acl  demo：**

```java
//下面的语句代表ip为128.0.0.1的，可以对/test2节点进行crwda权限
setAcl /test2 ip:128.0.0.1:crwda  //crwda对应CREATE、READ、WRITE、DELETE、ADMIN
```



**Acl相关命令：**

getAcl    getAcl <path>   读取ACL权限
setAcl    setAcl <path> <acl>   设置ACL权限
addauth   addauth <scheme> <auth>   添加认证用户

### 什么是ZooKeeper的ZAB协议？

是专门为ZooKeeper设计的一致性算法，思想如下。

![image-20201001154641411](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001154641411.png)

如上图，所有事务请求由Leader服务器协调处理，Leader服务器将客户端事务转化为Proposal（提议），将Proposal分发到所有Follower服务器，之后Leader等待Follower反馈（Ack），一旦超半数正确反馈后，再向所有Follower分发Commit消息。

ZAB协议还包括两种基本模式：崩溃恢复和消息广播。

**消息广播模式：**

消息广播模式介绍：

相比2PC，移除了中断逻辑，**过半的Follower服务器反馈Ack之后就可以提交事务Proposal而不是等待所有的Follower服务器都响应，**这种方式会因为Leader服务器崩溃带来数据1不一致问题，ZAB采用崩溃恢复模式解决该问题。**此外消息广播协议是基于FIFO特性的TCP协议进行网络通信的**。

消息广播模式场景：

**当集群过半Follower服务器和Leader服务器数据同步后，整个服务框架进入消息广播模式**，当一台同样遵守ZAB协议的服务器加入到集群中，Leader服务器进行消息广播。

消息广播模式过程：

前言：Leader服务器会每个事务分配全局单调递增的唯一ID（又称ZXID），每个事务Proposal按照ZXID先后顺序进行排序和处理。

1. Leader服务器为每一个Follower服务器分配单独队列，然后将需广播的事务Proposal放入队列，根据FIFO策略进行消息发送。

2. 每个Follower服务器接收到Proposal事务，将事务日志写入本地磁盘，返回Leader服务器Ack响应。

3. Leader服务器接收到半数的Ack响应后，发送Commit消息给所有Follow完成事务提交，同时Leader自身也完成事务提交。

   

**崩溃恢复模式：**

崩溃恢复模式介绍：

如果Leader服务器出现网络中断或崩溃等情况，ZAB进入崩溃恢复模式，同时选举产生新的Leader服务器，当集群中有过半的Leader服务器数据同步之后机会退出崩溃恢复模式。

崩溃恢复模式过程：

崩溃恢复模式需要解决两种问题，一是Commit消息还没发送给所有Follower机器就中途挂了；二是崩溃恢复时出现丢弃的事务，那么崩溃恢复结束后需跳过该事务。

解决方式：重新选举Leader时，该Leader拥有集群中所有机器最高编号事务（即ZXID），这样可以保证新选举的Leader一定具有所有已提交的提案，也省去检查Proposal的提交和丢弃工作。



### ZAB协议的三种状态？

LOOKING：Leader选举阶段

FOLLOWING：Follower和Leader保持同步状态

LEADING：Leader服务器作为主进程状态

所有进程初始状态LOOKING，此时选举出Leader，选举完后切换到FOLLOWING为Follower，切换到LEADING为Leader，如果Leader崩溃，Follower又切换到LOOKING状态。

Leader和所有Follower通过心跳机制感知彼此，如果指定时间内Leader无法从过半Follower接收到心跳检测或TCP断开，那么Leader和Follower会切换到LOOKING状态，开始新一轮选举。

### ZAB和Paxos的联系和区别？

待定。

## ZooKeeper实现分类

### ZooKeeper实现数据发布/订阅功能：

采用推拉模式结合方式，服务端主动将数据更新发送给所有订阅客户端（推，服务端数据变更，向相应客户端发送Watcher事件通知），客户端主动发起请求获取最新数据（拉），通常**拉动作客户端采用定时轮询方式获取。**



### 分布式锁的意义以及ZooKeeper实现的分布式锁：

分布式系统访问一组资源时，为保持一致性，需使用分布式锁防止彼此之间的干扰。

### ZooKeeper实现分布式系统的排他锁（悲观锁）：

排他锁概念举例：A上厕所，上厕所期间其他人不能上，等A上完再通知其他**所有**人去上。

**具体实现：**

1. 定义锁：创建一个锁节点，如图：

   ![image-20201001000051718](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001000051718.png)

2. 获取锁：所有客户端只有一个能创建锁节点成功，可以认为该客户端获取了锁，同时该客户端需到/exclusive_lock 节点注册一个子节点Watcher监听，以便实时监听lock节点的变化。

3. 释放锁：两种情况会释放，一是正常执行完业务逻辑，二是lock节点宕机。无论是哪个，ZooKeeper都会通知所有在/exclusive_lock节点注册子节点变更Watcher监听的客户端，这些客户端收到通知，重新发起锁获取。即重复获取锁过程。


### ZooKeeper实现分布式系统的共享锁（乐观锁）：

![image-20201001102252013](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001102252013.png)

共享锁两个阶段：

1. 初始阶段：

   - 定义锁：所有客户端都到/share_lock这个节点创建一个临时节点，一个节点代表一个锁，节点名称：Hostname+请求类型-序号。

   - 获取锁：读请求，若自己是序号最小或比自己小的都是读请求，那么获取到共享锁，并每个节点注册Watcher；对于写请求只有自己是序号最小的才能获取到共享锁，并每个节点注册Watcher

   - 释放锁：和排它锁一样，释放后节点自动删除。

     问题：一旦某节点删除**，所有节点**都需收到Watcher通知和重新排序，会造成**羊群效应**（短时间收到大量事件通知），影响ZooKeeper服务器性能。

2. 改进阶段：

   - 获取阶段改进：比自己小的节点注册Watcher即可。



### ZooKeeper实现分布式系统队列：

实现的分布式系统队列有两种：

![image-20201001150757854](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001150757854.png)

1. FIFO队列（和共享锁思想类似）：

   - 所有客户端都会到/queue_fifo节点创建临时节点，然后获取获取所有子节点。
   - 确定自己的节点在所有子节点的顺序。
   - 自己序号不是最小，继续等待，同时比自己序号小的最后一个节点注册Watcher监听。
   - 当接收到Watcher通知后，重复步骤1

   ![image-20201001151717372](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001151717372.png)

2. Barrier（分布式屏障）：该思想是在FIFO队列的基础上进行了增强。

   - 创建临时节点：首先设置/queue_barrier一个数字n=10（例）代表Barrier值，当子节点个数达到10后，所有客户端会到queue_barrier节点下创建一个临时节点，例如host1。
   - 获取所有子节点，并注册Watcher监听。
   - 统计子节点个数，如果子节点个数不足10个，继续等待。
   - 接收到Watcher通知后，重复步骤2



### ZooKeeper命名服务：

调用create()方法创建顺序节点，根据路径不同就能得到唯一ID，之所以不用UUID生成原因是数字太长且命名不直观。

![image-20200930034229927](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200930034229927.png)

### ZooKeeper时如何进行集群管理：

![image-20200930041519580](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200930041519580.png)

如上图拿日志收集系统举例：

1. 创建收集器节点（host1、host2...）。

2. 根据个数，将日志分为几份分发到节点host1、host2...。

3. host1、host2等每个节点下创建status(状态)节点，收集器节点定期向status节点写入自己的状态信息，日志系统根据状态节点最后更新时间判断机器是否存活。

4. 一旦收集器挂掉或扩容，有两种解决办法：

   - 全局分配：

     对所有节点重新分配

   - 局部分配：

     对当前收集器任务执行评估，有一个收集器挂了，将其任务分配到负载较低机器上，有新的收集器加入，再将负载高的机器部分任务转移过去。

**扩展：**

1.状态节点的创建，使得即使机器挂掉，如果重新恢复后，依然能将分配任务还原。

2.该日志系统如果采用Watcher机制，网络开销较大，可采用轮询收集器策略，会存在延迟，但是能节省流量。

### ZooKeeper实现文件系统： 

ZooKeeper提供一个多层级的节点命名空间（节点称为 znode）。与文件系统不同的是，这些节点都可以设置关联的数据，而文件系统中只有文件节点可以存放数据而目录节点不行。 ZooKeeper为了保证高吞吐和低延迟，在内存中维护了这个树状的目录结构，这 种特性使得 ZooKeeper不能用于存放大量的数据，每个节点的存放数据上限为 1M。

### ZooKeeper的序列化方式：

ZooKeeper内部使用jute实现序列化，jute实现序列化示例（实现 Record 接口，并在对应的 serialize 序列化方法和 deserialize 反序列化方法中编辑具体的实现逻辑。）：

```java
class test_jute implements Record{
  private long ids；
  private String name;
  ...
  public void serialize(OutpurArchive a_,String tag){
    ...
  }
  public void deserialize(INputArchive a_,String tag){
    ...
  }
}
```

```java
public void serialize(OutpurArchive a_,String tag) throws ...{
  a_.startRecord(this.tag);
  a_.writeLong(ids,"ids");
  a_.writeString(type,"name");
  a_.endRecord(this,tag);
}
```

```java
public void deserialize(INputArchive a_,String tag) throws {
  a_.startRecord(tag);
  ids = a_.readLong("ids");
  name = a_.readString("name");
  a_.endRecord(tag);
}
```

到这里我们就介绍完了如何在 ZooKeeper 中使用 Jute 实现序列化，需要注意的是，**在实现了Record 接口后，具体的序列化和反序列化逻辑要我们自己在 serialize 和 deserialize 函数中完成。**





# ZooKeeper使用：

### ZooKeeper的单机、集群、伪集群模式环境搭建：

![image-20200928005442627](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200928005442627.png)

#### 修改zoo.cfg文件：

**单机模式：**

ZooKeeper目录下创建data文件夹，修改conf文件夹下的zoo.cfg的dataDir属性指向data文件夹：

```xml
dataDir=/root/ZooKeeper-3.4.14/data
```

进入bin目录：

```xml
//启动ZooKeeper服务
./zkServer.sh start
//关闭服务
./zkServer.sh stop
//查看状态
./zkServer.sh status
```

**伪集群模式：（集群和伪集群区别：集群是不同ZooKeeper部署到不同机器；伪集群是部署在一台机器，一般是测试用的）**

1. ##### clientPort端口：

   每个ZooKeeper实例设置不同clientPort端口

2. ##### dataDir（存放数据文件）和dataLogDir（存放日志文件）：

   以上单机配置dataDir，每个ZooKeeper实例另外还要配置不同dataLogDir的路径参数

3. ##### server.X和myid：

   server.X中的X对应的是data/myid中的数字，比如3个server的myid文件中分别写入了1,2,3，那么每个server的zoo.cfg都配server.1 server.2 server.3就行。同一台机器后面连着2个端口（服务器之间通信端口，投票选举端口），3个server端口都不要一样，否则端口冲突。

1、2步骤：前面就是ZooKeeper复制改名、设置clientPort不同端口、dataDir和dataLogDir存放路径

3步骤：每个ZooKeeper的data目录创建myid文件，内容为1、2、3（就是记录每个服务器ID），在zoo.cfg配置客户端访问端口（clientPort）和集群服务器IP列表

```xml
server.1=10.211.55.4:2881:3881
server.2=10.211.55.4:2882:3882
server.3=10.211.55.4:2883:3883
#server.服务器ID=服务器IP地址：服务器之间通信端口：服务器之间投票选举端口
```



### ZooKeeper的常用命令行有哪些？

连接ZooKeeper服务器命令（进入bin目录输入以下命令）：

```
./zkcli.sh  //连接本地ZooKeeper服务器
./zkCli.sh -server ip:port  //连接指定的ZooKeeper服务器
```

创建节点：

```
create [-s][-e] path data acl
-s或-e分别代表创建顺序或临时节点，若不指定，则默认创建持久节点；acl用来进行权限控制
例：创建顺序节点--》create -s /zk-temp 123
```

读取节点：

```
//path代表指定数据节点的路径
ls path //列出指定节点的所有子节点
get path //获取ZooKeeper指定节点的数据内容和属性信息
ls2 path //除了可以获取ZooKeeper指定节点的数据内容和属性信息，还可以看到直系子节点列表
```

删除节点：

```
delete path [version]
```

若删除节点存在子节点，那么必须删除子节点，再删除父节点。

### Curator和ZKClient有什么区别？什么是fluent风格？

Curator使用fluent风格。

fluent风格：

```java
//fluent相对本对象加入build方法，调用build.setA.setB,对A、B对象赋值。
public class Student {
    private String name;
    private String address;
    public Student setName(String name){
        this.name = name;
        return this;
    }
    public Student setAddress(String address){
        this.address = address;
        return this;
    }
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    // 返回学生实体，可以做成单例
    public static Student build(){
        return new Student();
    }
}

public class Test {
    public static void main(String[] args) {
        Student student = Student.build().setName("李四").setAddress("广东广州");
        System.out.println(student.getName());
    }

```



# ZooKeeper特性

### ZooKeeper保证了哪些分布式一致性特性？

1. 顺序一致性 
2. 原子性
3. 单一视图 
4. 可靠性
5. 实时性（最终一致性）

客户端的读请求可以被集群中的任意一台机器处理，如果读请求在节点上注册了 监听器，这个监听器也是由所连接的 ZooKeeper机器来处理。对于写请求，这些请求会同时发给其他 ZooKeeper机器并且达成一致后，请求才会返回成功。因此， 随着 ZooKeeper的集群机器增多，读请求的吞吐会提高但是写请求的吞吐会下降。 

有序性是 ZooKeeper中非常重要的一个特性，所有的更新都是全局有序的，每个更新都有一个唯一的时间戳，这个时间戳称为 zxid（ZooKeeper Transaction Id）。 而读请求只会相对于更新有序，也就是读请求的返回结果中会带有这个 ZooKeeper最新的 zxid。

### ZooKeeper Watcher讲解：

- #### ZooKeeper Watcher使用（默认watcher和getChildren监听示例）：

  ![image-20210222201910970](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210222201910970.png)

  ```java
  /**
   * 测试默认watcher
   */
  public class DefaultWatcher implements Watcher {
  
      @Override
      public void process(WatchedEvent event) {
  
          System.out.println("==========DefaultWatcher start==============");
  
          System.out.println("DefaultWatcher state: " + event.getState().name());
  
          System.out.println("DefaultWatcher type: " + event.getType().name());
  
          System.out.println("DefaultWatcher path: " + event.getPath());
  
          System.out.println("==========DefaultWatcher end==============");
      }
  }
  ```

  ```java
  /**
   * 用于监听子节点变化的watcher
   */
  public class ChildrenWatcher implements Watcher {
      @Override
      public void process(WatchedEvent event) {
          System.out.println("==========ChildrenWatcher start==============");
          System.out.println("ChildrenWatcher state: " + event.getState().name());
          System.out.println("ChildrenWatcher type: " + event.getType().name());
          System.out.println("ChildrenWatcher path: " + event.getPath());
          System.out.println("==========ChildrenWatcher end==============");
      }
  }
  ```

  ```java
  public class WatcherTest {
  
      /**
       * 链接zk服务端的地址
       */
      private static final String CONNECT_STRING = "192.168.0.113:2181";
  
      public static void main(String[] args) {
          // 监听子节点变化的watcher
          ChildrenWatcher childrenWatcher = new ChildrenWatcher();
          try {
              // 创建zk客户端，并注册默认watcher
              ZooKeeper zooKeeper = new ZooKeeper(CONNECT_STRING, 100000, defaultWatcher);
              // 让childrenWatcher监听 /GetChildren 节点的子节点变化(默认watcher不再监听该节点子节点变化)
              zooKeeper.getChildren("/GetChildren", childrenWatcher);
              
              TimeUnit.SECONDS.sleep(1000000);
          } catch (Exception ex) {
              ex.printStackTrace();
          }
      }
  }
  ```

  首先在命令行客户端创建节点 /GetChildren

  ```shell
  [zk: localhost:2181(CONNECTED) 133] create /GetChildren GetChildrenData
  Created /GetChildren12
  ```

  运行测试代码`WatcherTest`，输出如下内容：

  ```shell
  ==========DefaultWatcher start==============
  DefaultWatcher state: SyncConnected
  DefaultWatcher type: None
  DefaultWatcher path: null
  ==========DefaultWatcher end==============12345
  ```

  可以看出在客户端第一次链接zk服务端时触发了链接成功的事件通知，该事件由默认watcher接收，导致默认watcher相关代码得到执行

  接着在命令行客户端创建子节点：

  ```shell
  [zk: localhost:2181(CONNECTED) 134] create /GetChildren/ChildNode ChildNodeData
  Created /GetChildren/ChildNode
  ```

  ChildrenWatcher收到通知，/GetChildren的子节点发生变化，因此输出如下内容：

  ```shell
  ==========ChildrenWatcher start==============
  ChildrenWatcher state: SyncConnected
  ChildrenWatcher type: NodeChildrenChanged
  ChildrenWatcher path: /GetChildren
  ==========ChildrenWatcher end==============
  ```

  > 当子节点再变动时不会再输出了！

  

由上代码可知，实现watcher机制客户端只需要三步：

1. 创建zk客户端，并注册默认watcher[ZooKeeper zooKeeper = new ZooKeeper(xxx xxx, xxx);]。
2. 注册其他watcher。
3. 类实现Watcher接口，节点变化后该类就会触发输出。

##### 除了默认watcher，注册其他watcher有以下几种，以及它们的区别：

- exits： 节点被创建、节点被删除、节点数据和状态变动时，会触发watch。
- getChildren：当**子节点**列表发生变动时，会触发watch。
- getData：当节点发生变动（包含状态和数据变动）时，会触发watch。

- #### ZooKeeper Watcher 底层实现：

  zookeeper 的 watcher 机制，可以分为四个过程：

  ![image (1).png](https://s0.lgstatic.com/i/image/M00/05/28/Ciqc1F61IL-AEQuUAABdpaAsy2k628.png)

  - 客户端注册 watcher。
  - 服务端处理 watcher。
  - 服务端触发 watcher 事件。
  - 客户端回调 watcher。

### ZooKeeper对节点的 watch监听通知是永久的吗？

**答：**不是永久的，当节点再次变更时，需要重新注册watcher才能感知变更。

> 为什么不是永久的，举个例子，如果服务端变动频繁，而监听的客户端很多情况下，每次变动都要通知到所有的客户端，**给网络和服务器造成很大压力**。 一般是客户端执行 getData(“/节点 A”,true)，如果节点 A 发生了变更或删除， 客户端会得到它的 watch 事件，但是在之后节点 A 又发生了变更，而客户端又没有设置 watch 事件，就不再给客户端发送。 在实际应用中，很多情况下，我们的客户端不需要知道服务端的每一次变动，我只要最新的数据即可。
>



### Chroot 特性 

3.2.0 版本后，添加了 Chroot 特性，该特性允许每个客户端为自己设置一个命名空间。如果一个客户端设置了 Chroot，那么该客户端对服务器的任何操作，都将会被限制在其自己的命名空间下。

 通过设置 Chroot，能够将一个客户端应用于 ZooKeeper服务端的一颗子树相对应，在那些多个应用公用一个 ZooKeeper集群的场景下，对实现不同应用间的相互隔离非常有帮助。

客户端可以通过在connectString中添加后缀的方式来设置Chroot，如下所示：

```xml
192.168.0.1:2181,192.168.0.2:2181,192.168.0.3:2181/apps/X
```

这个client的chrootPath就是/apps/X
将这样一个connectString传入客户端的ConnectStringParser后就能够解析出Chroot并保存在chrootPath属性中。



### 会话管理 

Zookeeper为了保证请求会话的全局唯一性，在SessionTracker初始化时，调用initializeNextSession方法生成一个sessionID，之后在Zookeeper运行过程中，会在该sessionID的基础上为每个会话进行分配，初始化算法如下：

```java
public static long initializeNextSession(long id) {
    long nextSid = 0;
    // 无符号右移8位使为了避免左移24后，再右移8位出现负数而无法通过高8位确定sid值
    nextSid = (System.currentTimeMillis() << 24) >>> 8;
    nextSid = nextSid | (id << 56);
    return nextSid;
}
```

**分桶策略**：将类似的会话放在同一区块中进行管理，以便于 ZooKeeper对会话进行不同区块的隔离处理以及同一区块的统一处理。

![image-20201018163626563](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201018163626563.png)

 **分配原则：**每个会话的“下次超时时间点”（ExpirationTime） 

计算公式：

```
ExpirationTime_ = currentTime + sessionTimeout ExpirationTime = (ExpirationTime_ / ExpirationInrerval + 1) * ExpirationInterval 

ExpirationInterval 是指 Zookeeper 会话超时检查时间 间隔，默认 tickTime=2000
```

为了保持客户端会话的有效性，**客户端会在会话超时时间过期范围内向服务端发送PING请求来保持会话的有效性（心跳检测）**。同时，服务端需要不断地接收来自客户端的心跳检测，并且需要重新激活对应的客户端会话，这个重新激活过程称为TouchSession。会话激活不仅能够使服务端检测到对应客户端的存货性，同时也能让客户端自己保持连接状态，其流程如下　　

![img](https://images2015.cnblogs.com/blog/616953/201611/616953-20161126171119628-1511238863.png)
如上图所示，整个流程分为四步

　　1. **检查该会话是否已经被关闭**。若已经被关闭，则直接返回即可。

　　2. **计算该会话新的超时时间ExpirationTime_New**。使用上面提到的公式计算下一次超时时间点。

　　3. **获取该会话上次超时时间ExpirationTime_Old**。计算该值是为了定位其所在的区块。

　　3. **迁移会话**。将该会话从老的区块中取出，放入ExpirationTime_New对应的新区块中。

![img](https://images2015.cnblogs.com/blog/616953/201611/616953-20161126171348878-1972526661.png)

　　在上面会话激活过程中，只要客户端发送心跳检测，服务端就会进行一次会话激活，心跳检测由客户端主动发起，以PING请求形式向服务端发送，在Zookeeper的实际设计中，**只要客户端有请求发送到服务端，那么就会触发一次会话激活**，以下两种情况都会触发会话激活。

　　1. 客户端向服务端发送请求，包括读写请求，就会触发会话激活。

　　2. 客户端发现在sessionTimeout/3时间内尚未和服务端进行任何通信，那么就会主动发起PING请求，服务端收到该请求后，就会触发会话激活。

　　对于会话的超时检查而言，Zookeeper使用SessionTracker来负责，SessionTracker使用单独的线程（超时检查线程）专门进行会话超时检查，即逐个一次地对会话桶中剩下的会话进行清理。如果一个会话被激活，那么Zookeeper就会将其从上一个会话桶迁移到下一个会话桶中，如ExpirationTime 1 的session n 迁移到ExpirationTime n 中，此时ExpirationTime 1中留下的所有会话都是尚未被激活的，超时检查线程就定时检查这个会话桶中所有剩下的未被迁移的会话，超时检查线程只需要在这些指定时间点（ExpirationTime 1、ExpirationTime 2...）上进行检查即可，这样提高了检查的效率，性能也非常好。

**会话清理：**

　　当SessionTracker的会话超时线程检查出已经过期的会话后，就开始进行会话清理工作，大致可以分为如下七步。

　　1. **标记会话状态为已关闭**。由于会话清理过程需要一段时间，为了保证在此期间不再处理来自该客户端的请求，SessionTracker会首先将该会话的isClosing标记为true，这样在会话清理期间接收到该客户端的心情求也无法继续处理了。

　　2. **发起会话关闭请求**。为了使对该会话的关闭操作在整个服务端集群都生效，Zookeeper使用了提交会话关闭请求的方式，并立即交付给PreRequestProcessor进行处理。

　　3. **收集需要清理的临时节点**。一旦某个会话失效后，那么和该会话相关的临时节点都需要被清理，因此，在清理之前，首先需要将服务器上所有和该会话相关的临时节点都整理出来。Zookeeper在内存数据库中会为每个会话都单独保存了一份由该会话维护的所有临时节点集合，在Zookeeper处理会话关闭请求之前，若正好有以下两类请求到达了服务端并正在处理中。

　　　　· 节点删除请求，删除的目标节点正好是上述临时节点中的一个。

　　　　· 临时节点创建请求，创建的目标节点正好是上述临时节点中的一个。

　　对于第一类请求，需要将所有请求对应的数据节点路径从当前临时节点列表中移出，以避免重复删除，对于第二类请求，需要将所有这些请求对应的数据节点路径添加到当前临时节点列表中，以删除这些即将被创建但是尚未保存到内存数据库中的临时节点。

　　4. **添加节点删除事务变更**。完成该会话相关的临时节点收集后，Zookeeper会逐个将这些临时节点转换成"节点删除"请求，并放入事务变更队列outstandingChanges中。

　　5. **删除临时节点**。FinalRequestProcessor会触发内存数据库，删除该会话对应的所有临时节点。

　　6. **移除会话**。完成节点删除后，需要将会话从SessionTracker中删除。

　　7. **关闭NIOServerCnxn**。最后，从NIOServerCnxnFactory找到该会话对应的NIOServerCnxn，将其关闭。

　　2.5 重连

　　当客户端与服务端之间的网络连接断开时，Zookeeper客户端会自动进行反复的重连，直到最终成功连接上Zookeeper集群中的一台机器。此时，再次连接上服务端的客户端有可能处于以下两种状态之一

　　1. **CONNECTED**。如果在会话超时时间内重新连接上集群中一台服务器 。

　　2. **EXPIRED**。如果在会话超时时间以外重新连接上，那么服务端其实已经对该会话进行了会话清理操作，此时会话被视为非法会话。

　　在客户端与服务端之间维持的是一个长连接，在sessionTimeout时间内，服务端会不断地检测该客户端是否还处于正常连接，服务端会将客户端的每次操作视为一次有效的心跳检测来反复地进行会话激活。因此，在正常情况下，客户端会话时一直有效的。然而，当客户端与服务端之间的连接断开后，用户在客户端可能主要看到两类异常：**CONNECTION_LOSS（连接断开）和SESSION_EXPIRED（会话过期）**。

　　1. **CONNECTION_LOSS**。此时，客户端会自动从地址列表中重新逐个选取新的地址并尝试进行重新连接，直到最终成功连接上服务器。若客户端在setData时出现了CONNECTION_LOSS现象，此时客户端会收到None-Disconnected通知，同时会抛出异常。应用程序需要捕捉异常并且等待Zookeeper客户端自动完成重连，一旦重连成功，那么客户端会收到None-SyncConnected通知，之后就可以重试setData操作。

　　2. **SESSION_EXPIRED**。客户端与服务端断开连接后，重连时间耗时太长，超过了会话超时时间限制后没有成功连上服务器，服务器会进行会话清理，此时，客户端不知道会话已经失效，状态还是DISCONNECTED，如果客户端重新连上了服务器，此时状态为SESSION_EXPIRED，用于需要重新实例化Zookeeper对象，并且看应用的复杂情况，重新恢复临时数据。

　　3. **SESSION_MOVED**。客户端会话从一台服务器转移到另一台服务器，即客户端与服务端S1断开连接后，重连上了服务端S2，此时会话就从S1转移到了S2。当多个客户端使用相同的sessionId/sess



# ZooKeeper集群

### ZooKeeper如何保持数据同步

整个集群完成 Leader 选举之后，Learner（Follower 和 Observer 的统称）回向 Leader 服务器进行注册。当 Learner 服务器想 Leader 服务器完成注册后，进入数据同步环节。 

数据同步流程：（均以消息传递的方式进行） 

Learner 向 Leader 注册 

数据同步 

同步确认 

**Zookeeper 的数据同步通常分为四类：** 

1、直接差异化同步（DIFF 同步） 

2、先回滚再差异化同步（TRUNC+DIFF 同步） 

3、仅回滚同步（TRUNC 同步） 

4、全量同步（SNAP 同步） 

**在进行数据同步前，Leader 服务器会完成数据同步初始化：** 

peerLastZxid： 

-  从 learner 服务器注册时发送的 ACKEPOCH 消息中提取 lastZxid（该 Learner 服务器最后处理的 ZXID） 

minCommittedLog： 

- Leader 服务器 Proposal 缓存队列 committedLog 中最小 ZXID 

maxCommittedLog： 

- Leader 服务器 Proposal 缓存队列 committedLog 中最大 ZXID 

**直接差异化同步（DIFF 同步）** 

场景：peerLastZxid 介于 minCommittedLog 和 maxCommittedLog 之间 

**先回滚再差异化同步（TRUNC+DIFF 同步）** 

场景：当新的 Leader 服务器发现某个 Learner 服务器包含了一条自己没有的事务记录，那么就需要让该 Learner 服务器进行事务回滚--回滚到 Leader 服务器上存在的，同时也是最接近于 peerLastZxid 的 ZXID 

**仅回滚同步（TRUNC 同步）** 

场景：peerLastZxid 大于 maxCommittedLog 

**全量同步（SNAP 同步）** 

场景一：peerLastZxid 小于 minCommittedLog 

场景二：Leader 服务器上没有 Proposal 缓存队列且 peerLastZxid 不等 于 lastProcessZxid



### ZooKeeper负载均衡和 nginx 负载均衡区别 

zk 的负载均衡是可以调控，nginx 只是能调权重，其他需要可控的都需要自己写插件；但是 nginx 的吞吐量比 zk 大很多，应该说按业务选择用哪种方式。

### ZooKeeper集群最少要几台机器，集群规则是怎样的? 

集群规则为 2N+1 台，N>0，即 3 台。

集群支持动态添加机器吗？ 

其实就是水平扩容了，ZooKeeper在这方面不太好。两种方式：

**全部重启：**关闭所有 ZooKeeper服务，修改配置之后启动。不影响之前客户端的 会话。

**逐个重启：**在过半存活即可用的原则下，一台机器重启不影响整个集群对外提供 服务。这是比较常用的方式。

3.5 版本开始支持动态扩容。

### ZooKeeper的 java 客户端都有哪些？

java 客户端：zk 自带的 zkclient 及 Apache 开源的 Curator。

### chubby 是什么，和 ZooKeeper比你怎么看？ 

chubby 是 google 的，完全实现 paxos 算法，不开源。ZooKeeper是 chubby 的开源实现，使用 zab 协议，paxos 算法的变种。

### ZooKeeper如何实现高可用？

https://blog.csdn.net/u012661248/article/details/84371889



# ZooKeeper源码

### 单机ZooKeeper启动流程？

可分为预启动和初始化：

![image-20201001221811017](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001221811017.png)

预启动：

![image-20201001221850863](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001221850863.png)

初始化：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001221951213.png" alt="image-20201001221951213" style="zoom:155%;" />

### 集群ZooKeeper启动流程？

可分为预启动、初始化、Leader选举、Leader和Follower启动器交互、Leader与Follower启动等过程。

![image-20201001222646799](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001222646799.png)

!<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001222745557.png" alt="image-20201001222745557" style="zoom:200%;" />

![image-20201001222941072](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001222941072.png)

![image-20201001223246006](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001223246006.png)

![image-20201001223302963](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001223302963.png)

![image-20201001223316501](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001223316501.png)

### ZooKeeper集群和节点的关系

![image-20201019105555310](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201019105555310.png)

### ZooKeeper的Leader选举过程？（zk集群选举）

分为启动和运行选举（过程都一样）：

![image-20201001224931257](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201001224931257.png)

如图：

1. server1和server2投票给自己，再把投票信息发给其他机器。
2. 将接收到的选票信息和本身PK，PK规则是先比较ZXID再比较MyId；如果投票信息比自己大时机器更新为大的投票信息，再次发送投票信息给其他机器。
3. server2收到了（2,0）两个投票（一次是自己），超过3的半数，于是成为Leader。

**选举的状态变更：**

Leader挂后或者还没有，余下的非Observer服务器都将自己的服务器状态变更为LOOKING，然后进入Leader选举过程。

### ZooKeeper的Master选举的意义是什么？（zk内部选举出主节点）

Master主要是协调集群的其他系统单元，具有对分布式系统状态变更的决定权，Master主要负责处理一些复杂的逻辑（有些逻辑一个客户端计算，其他复制结果就可以了，不用每个都计算）。

![image-20200930232631219](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200930232631219.png)

如上图：Client集群每天定时通过ZooKeeper实现选举，Master进行一系列海量数据处理，将结果存储到数据库，Master再通知其他Client共享计算结果。

### ZooKeeper的Master选举的过程是什么？

选举Master的需求是集群中所有机器选出一台机器作为Master，使用关系型数据库主键特性，插入成功数据的客户端就是Master，但是Master宕机无法处理。于是使用ZooKeeper节点唯一性，客户端集群每天定时往ZooKeeper创建临时节点例如：/master_election/2020-11- 11/binding，如果有客户端创建成功则为Master节点，其他客户端在节点/master_election/2020-11-11注册子节点变更Watcher，监控Master是否存活，一旦挂了，重新选举（往ZooKeeper创建临时节点）。

### ZooKeeper怎么处理客户端发送读写请求？

如下图当读写请求发送到follower:

- 读请求：

  直接返回结果；

- 写请求

  1. Follower 会将写请求转发给Leader 服务器。
  2. Leader 将事务请求放入**待提交队列中**，然后**发起Proposal 投票** 。
  3. Follower 处理提议，响应ACK。
  4. 如果leader统计到投票过半，发送commit请求给Follower，Follower提交事务，由刚刚接收客户端请求的follower响应给客户端。

![image-20200928002111424](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200928002111424.png)

但是ZooKeeper颠覆了Master/Slave模式概念，引用Leader、Follower、Observer三种角色。

Leader：通过所有机器选举得出，Leader服务器为客户端提供读写服务。

Follower、Observer：都能接受和读写服务和提供读服务，唯一区别是Observer不参与选举（能接收到proposal投票请求，但是不参与），也不提供事务服务（只提供读服务），Observer存在意义是防止所有follower投票导致系统性能不足。

### 分布式集群中为什么会有 Master？

 在分布式环境中，有些业务逻辑只需要集群中的某一台机器进行执行，其他的机器可以共享这个结果，这样可以大大减少重复计算，提高性能，于是就需要进行 leader 选举。

zk的observer宕机怎么办

zk集群有一个宕机怎么处理

![image-20201019111451750](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201019111451750.png)