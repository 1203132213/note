[TOC]



### Eureka源码：

#### EurekaServer启动过程详解：

**EurekaServer启动过程步骤总结：**

1. 判断是否加@EnableEurekaServer注解

2. 注入仪表盘接口

3. 注入了一个对等节点注册器，节点同步和注册方法需用到该注册器

4. EurekaServer集群的节点更新

   > 实现细节：
   >
   > 1. 该步骤使用单线程定时任务执行器newSingleThreadScheduledExecutor（默认10min）执行一次更新操作
   > 2. 先新建原节点相同长度的Set集合，然后和新的节点Set集合相加减【使用set.removeAll(newPeerUrls)】，剩余的就是需删除或添加的节点。
   > 3. 通过遍历往节点列表newNodeList 添加或删除节点。
   >
   >  newNodeList 源码： List<PeerEurekaNode> newNodeList = new ArrayList<>(peerEurekaNodes);

5. 注册jersey过滤器，用于Eureka Server服务接口暴露

6. 遍历同步**其他**Eureka Server节点信息（意思是把其他Eureka Server节点信息同步到该Eureka Server）

   > 根据配置文件的重试次数、重试间隔来遍历注册到该Eureka Server，注册方法详解参考：下方AbstractInstanceRegistry#register()讲解。

7. 实例状态改为UP，用于接收请求

   >  applicationInfoManager.setInstanceStatus(InstanceStatus.UP);

8. 使用java自带的Timer定时剔除失效Eureka Client

   > 剔除失效Eureka Client详解：
   >
   > - 服务下线代码
   >
   > ```java
   >  // 1. 自我保护机制能否剔除实例判断，后面会讲解，该方法返回false即代表不可以剔除实例
   >     if (!isLeaseExpirationEnabled()) {
   >         return;
   >     }
   > // 2.过滤出所有过期实例，过期实例放到expiredLeases集合
   >     List<Lease<InstanceInfo>> expiredLeases = new ArrayList<>();
   >     for (Entry<String, Map<String, Lease<InstanceInfo>>> groupEntry : registry.entrySet()) {
   >         Map<String, Lease<InstanceInfo>> leaseMap = groupEntry.getValue();
   >         if (leaseMap != null) {
   >             for (Entry<String, Lease<InstanceInfo>> leaseEntry : leaseMap.entrySet()) {
   >                 Lease<InstanceInfo> lease = leaseEntry.getValue();
   >                 //3.这里判断过期条件，上面已经分析分析了
   >                 if (lease.isExpired(additionalLeaseMs) && lease.getHolder() != null) {
   >                     expiredLeases.add(lease);
   >                 }
   >             }
   >         }
   >     }
   > if (toEvict > 0) {
   >     	//4.随机剔除过期实例
   >         Random random = new Random(System.currentTimeMillis());
   >         for (int i = 0; i < toEvict; i++) {
   >             int next = i + random.nextInt(expiredLeases.size() - i);
   >             Collections.swap(expiredLeases, i, next);
   >             Lease<InstanceInfo> lease = expiredLeases.get(i);
   >  
   >             String appName = lease.getHolder().getAppName();
   >             String id = lease.getHolder().getId();
   >             EXPIRED.increment();
   >             internalCancel(appName, id, false);
   >         }
   >     }
   > ```
   >
   > - 下面涉及的重要参数：
   >
   > ```java
   > private long evictionTimestamp;     		// 第一次服务下线时间戳(不管是事件还是调度触发都会更新这个时间)
   > private long registrationTimestamp; 		// 注册服务时间(每次注册时更新)
   > private long serviceUpTimestamp;    		// 第一次服务上线时间
   > private volatile long lastUpdateTimestamp;      // 最后一次心跳时间
   > private long duration;				// 实例过期时间,默认90s
   > ```
   >
   > - Eureka Client发起续约的代码（续约的目的是为了不会因失效剔除）：
   >
   > ```java
   > public void renew() {
   > 	// 每次续约时会调用这个方法，会更新lastUpdateTimestamp，duration默认是90s
   >     lastUpdateTimestamp = System.currentTimeMillis() + duration;
   > }
   > ```
   >
   > -  自我保护机制能否剔除实例判断
   >
   >   ```java
   >   public boolean isLeaseExpirationEnabled() {
   >   	// 1. 是否启用自我保护机制(eureka.server.enableSelfPreservation,默认true)
   >   	// 如果闭关了，这里直接返回true
   >       if (!isSelfPreservationModeEnabled()) {
   >           return true;
   >       }
   >       // 2. 如果启用自我保护机制，也是有可能剔除过期实例的,只要满足上一分钟续约数量 > 每分钟的续约阈值
   >       return numberOfRenewsPerMinThreshold > 0 && getNumOfRenewsInLastMin() > numberOfRenewsPerMinThreshold;
   >   }
   >   ```
   >
   >   首先校验服务端是否开启了自我保护机制(eureka.server.enableSelfPreservation,默认true),如果没开启，直接返回true,即允许剔除；如果开启了自我保护机制，然后再判断上一分钟续约数是否大于每分钟续约数阈值，大于，返回true，反之，false。
   >
   > - 判断实例过期的代码（Eureka Client下线时会用到该判断）：
   >
   > ```java
   > public boolean isExpired(long additionalLeaseMs) {
   >     return (evictionTimestamp > 0 || System.currentTimeMillis() > (lastUpdateTimestamp + duration + additionalLeaseMs));
   > }
   > ```
   >
   > 如上代码，因为renew()里面lastUpdateTimestamp有加duration，所以duration是判断2、3条件的重要依据，没有两个duration 就说明还没发起续约。
   >
   > 综合以上情况，实例过期条件是：
   >
   > 1. a. evictionTimestamp > 0
   > 2. b. evictionTimestamp <=0 && 当前时间 > 上次真正的续约时间(不包含duration) + duration (注册后还没有发起续约就挂掉了)
   > 3. c. evictionTimestamp <=0 && 当前时间 > 上次真正的续约时间(不包含duration) + 2 * duration (发起续约后挂掉)
   >
   > 如果再计入调度执行间隔时间(60s)，那么服务端在开启自我保护机制下要想剔除一个过期实例，大概需要90s - 240s



- 入口：SpringCloud充分利用了SpringBoot的自动装配的特点 观察eureka-server的jar包，发现在META-INF下面有配置文件spring.factories

![image-20210131111010464](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131111010464.png)

- EurekaServerAutoConfiguration讲解：

```java
//1.详情参考“EurekaServerInitializerConfiguration讲解”
@Import({EurekaServerInitializerConfiguration.class})
//2.对应@EnableEurekaServer的marker，作用是判断添加了@EnableEurekaServer注解，这是成为⼀个EurekaServer的前提
@ConditionalOnBean({Marker.class}) 
public class EurekaServerAutoConfiguration extends WebMvcConfigurerAdapter {
    
    //3.注入对外的接口（仪表盘---就是注册中心PC页面），可以在配置文件加			    eureka.dashboard.enabled=false关闭
    @Bean
    @ConditionalOnProperty(prefix = "eureka.dashboard",name = {"enabled"},
    matchIfMissing = true)
    public EurekaController eurekaController() {
        return new EurekaController(this.applicationInfoManager);
    }
	//4.Eureka集群环境下感知服务注册的方法（Eureka集群各个节点是对等的，没有主从之分）
    @Bean
  public PeerAwareInstanceRegistry peerAwareInstanceRegistry(ServerCodecs serverCodecs) {	
        this.eurekaClient.getApplications();
        return new InstanceRegistry(this.eurekaServerConfig, 		     this.eurekaClientConfig, serverCodecs, this.eurekaClient, this.instanceRegistryProperties.getExpectedNumberOfClientsSendingRenews(), this.instanceRegistryProperties.getDefaultOpenForTrafficCount());
    }
	//5.PeerEurekaNodes：辅助封装节点的相关信息和操作（比如更新Eureka集群的节点，Eureka集群节点可能发生变化，需要实时更新）实现过程参考下方“Eureka节点更新”
    @ConditionalOnMissingBean
    public PeerEurekaNodes peerEurekaNodes(PeerAwareInstanceRegistry registry, ServerCodecs serverCodecs) {
        return new EurekaServerAutoConfiguration.RefreshablePeerEurekaNodes(registry, this.eurekaServerConfig, this.eurekaClientConfig, serverCodecs, this.applicationInfoManager);
    }
	//6.注入EurekaServer上下文对象DefaultEurekaServerContext，其中一个作用是使用@PostConstruct注解构造器执行会触发下面的方法，从而触发4步骤的start方法更新Eureka集群节点，实际上执行了该步骤方法就是在调用4步骤方法（具体见“DefaultEurekaServerContext详解”）
    @Bean
    public EurekaServerContext eurekaServerContext(ServerCodecs serverCodecs, PeerAwareInstanceRegistry registry, PeerEurekaNodes peerEurekaNodes) {
        return new DefaultEurekaServerContext(this.eurekaServerConfig, serverCodecs, registry, peerEurekaNodes, this.applicationInfoManager);
    }
	//7.注入EurekaServerBootstrap类，后续启动要使用该对象
    @Bean
    public EurekaServerBootstrap eurekaServerBootstrap(PeerAwareInstanceRegistry registry, EurekaServerContext serverContext) {
        return new EurekaServerBootstrap(this.applicationInfoManager, this.eurekaClientConfig, this.eurekaServerConfig, registry, serverContext);
    }
	//8.注册jersey过滤器，jersey它是一个rest框架，帮我们发布restful服务接口（类似于springmvc）
    @Bean
    public FilterRegistrationBean jerseyFilterRegistration(Application eurekaJerseyApp) {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new ServletContainer(eurekaJerseyApp));
        bean.setOrder(2147483647);
        bean.setUrlPatterns(Collections.singletonList("/eureka/*"));
        return bean;
    }
}
```

- EurekaServerInitializerConfiguration讲解：


```java
//实现SmartLifecycle接口的start()目的，可以在Spring容器Bean创建完成触发该方法。
@Configuration
public class EurekaServerInitializerConfiguration implements ServletContextAware, SmartLifecycle, Ordered {
    public EurekaServerInitializerConfiguration() {
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void start() {
   (new Thread(new Runnable() {
public void run() {
   try {
        //初始化EurekaServerContext细节（参考下方contextInitialized()）
        eurekaServerBootstrap.contextInitialized(
        EurekaServerInitializerConfiguration.this.servletContext);
                } catch (Exception var2) {
                    EurekaServerInitializerConfiguration.log.error("Could not initialize Eureka servlet context", var2);
                }

            }
        })).start();
    }

}
-----------------------------------------------------------------------------------------
public void contextInitialized(ServletContext context) {
        try {
            //初始化context细节（参考下方initEurekaServerContext()）
            this.initEurekaServerContext();
        } catch (Throwable var3) {
    }
-----------------------------------------------------------------------------------------

protected void initEurekaServerContext() throws Exception {
    	//某个server实例启动，把集群中其他server拷贝注册信息过来（实际上就是同步过程，具体见下方syncUp()）
        int registryCount = this.registry.syncUp();
    	//更改实例状态为UP，对外提供服务（见下方openForTraffic()）
        this.registry.openForTraffic(this.applicationInfoManager, registryCount);
    	//注册统计器
        EurekaMonitors.registerAllStats();
    }
-----------------------------------------------------------------------------------------

    public int syncUp() {
        int count = 0;
//根据配置文件中的eureka.server.registry-sync-retries的配置属性获取注册表同步重试次数,根据重试次数同步其他server节点信息
 for(int i = 0; i < this.serverConfig.getRegistrySyncRetries() && count == 0; ++i) {
            if (i > 0) {
                try {
              		//2.线程等待eureka.server.registry-sync-retry-wait-ms的配置重试间隔时间
                    Thread.sleep(this.serverConfig.getRegistrySyncRetryWaitMs());
                } catch (InterruptedException var10) {
                    logger.warn("Interrupted during registry transfer..");
                    break;
                }
            }
			//3.获取Eureka集群其他节点信息，遍历使用register方法注册到自身注册表中
            Applications apps = this.eurekaClient.getApplications();
            Iterator var4 = apps.getRegisteredApplications().iterator();
            while(var4.hasNext()) {
                Application app = (Application)var4.next();
                Iterator var6 = app.getInstances().iterator();

                while(var6.hasNext()) {
                    InstanceInfo instance = (InstanceInfo)var6.next();
                    try {
                        if (this.isRegisterable(instance)) {
             //4.将Eureka集群其他节点信息注册到自身注册表中（参考下方register()）
             this.register(instance, instance.getLeaseInfo().getDurationInSecs(), true);
                            ++count;
                        }
                    } catch (Throwable var9) {
                        logger.error("During DS init copy", var9);
                    }
                }
            }
        }

        return count;
    }
-----------------------------------------------------------------------------------------
private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry = new ConcurrentHashMap();//注册表

//该方法主要就是判断是否存在当前实例如果不存在则创建一个，并更新续约时间
public void register(InstanceInfo registrant, int leaseDuration, boolean isReplication) {
        //详情见“EurekaServer服务注册接口（接受客户端注册服务）”
}
-----------------------------------------------------------------------------------------
public void openForTraffic(ApplicationInfoManager applicationInfoManager, int count) {
        //实例状态改为UP，此时可以接收请求
        applicationInfoManager.setInstanceStatus(InstanceStatus.UP);
    	//默认每隔60秒（时间可配置）剔除失效Eureka Client（属于心跳检测，实现方法如下）
        super.postInit();
    }

    protected void postInit() {
       //使用java自带的Timer定时剔除失效Eureka Client
       this.evictionTimer.schedule((TimerTask)this.evictionTaskRef.get(), 		 serverConfig.getEvictionIntervalTimerInMs(), serverConfig.getEvictionIntervalTimerInMs());
    }
```



Eureka节点更新：

```java
public void start() {
 //执行定时任务，默认时间是10min   
 this.taskExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Eureka-PeerNodesUpdater");
                thread.setDaemon(true);
                return thread;
            }
        });

        try {
            this.updatePeerEurekaNodes(this.resolvePeerUrls());
            Runnable peersUpdateTask = new Runnable() {
                public void run() {
                    try {
                     PeerEurekaNodes.this.updatePeerEurekaNodes(PeerEurekaNodes.this.resolvePeerUrls());
                    } catch (Throwable var2) {
                        PeerEurekaNodes.logger.error("Cannot update the replica Nodes", var2);
                    }

                }
            };
    }
    @Bean
protected void updatePeerEurekaNodes(List<String> newPeerUrls) {
    // 计算原peerEurekaNodeUrls - 新newPeerUrls 旧的的差集，就是多余可shutdown节点
    Set<String> toShutdown = new HashSet<>(peerEurekaNodeUrls);
    toShutdown.removeAll(newPeerUrls);
    
    // 计算新newPeerUrls - 原peerEurekaNodeUrls 的差集，就是需要新增节点
    Set<String> toAdd = new HashSet<>(newPeerUrls);
    toAdd.removeAll(peerEurekaNodeUrls);

    if (toShutdown.isEmpty() && toAdd.isEmpty()) { //没有变更
        return;
    }
    List<PeerEurekaNode> newNodeList = new ArrayList<>(peerEurekaNodes);

    // 删除多余节点
    if (!toShutdown.isEmpty()) {
        int i = 0;
        while (i < newNodeList.size()) {
            PeerEurekaNode eurekaNode = newNodeList.get(i);
            if (toShutdown.contains(eurekaNode.getServiceUrl())) {
                newNodeList.remove(i);
                eurekaNode.shutDown();
            } else {
                i++;
            }
        }
    }
    // 添加新的peerEurekaNode - createPeerEurekaNode()
    if (!toAdd.isEmpty()) {
        logger.info("Adding new peer nodes {}", toAdd);
        for (String peerUrl : toAdd) {
            newNodeList.add(createPeerEurekaNode(peerUrl));
        }
    }
}
```

DefaultEurekaServerContext详解：

```java
	@PostConstruct
    public void initialize() {
        //这里的start方法对应的就是4步骤的start方法
        this.peerEurekaNodes.start();

        try {
            this.registry.init(this.peerEurekaNodes);
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }

        logger.info("Initialized");
    }
```

#### EurekaServer服务接口暴露策略：

**总结：**EurekaServer服务需要用到的接口（例如服务注册、心跳续约等接口）使用类似于springmvc的方式暴露Restful风格接口（@Path类似于@RequestMapping；遍历的方式扫描包的注解；实现写好的接口类似于springmvc的controller）

在Eureka Server启动过程中主配置类注册了Jersey框架（是⼀个发布restful风格接口的框架，类似于我
们的springmvc）

![image-20201108132006426](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108132006426.png)



注入的Jersey细节

![image-20201108132015645](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108132015645.png)

扫描classpath下的那些packages呢？已经定义好了

![image-20201108132035749](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108132035749.png)

对外提供的接口服务，在Jersey中叫做资源

![image-20201108132050631](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108132050631.png)

这些就是使用Jersey发布的供Eureka Client调用的Restful风格服务接口（完成服务注册、心跳续约等接
口）

#### EurekaServer服务注册接口（接受客户端注册服务）：

分三步：

1. 注册必备信息验证，其中一个信息为空返回400，注册成功返回编码204。

   ![image-20210131220347694](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131220347694.png)

2. 调用AbstractInstanceRegistry#register()注册服务

   > 注册过程使用读锁机制控制线程安全
   >
   > 注册过程详解：
   >
   > 1. 通过实例的Lease租约信息判断是否之前注册过
   >    - 注册过则比较租约时间戳，拿最新的时间戳（该时间为租约最近修改时间，判断实例是否修改就是用该时间戳来进行的）。
   >    - 没有注册过，把实例放入map，更新numberOfRenewsPerMinThreshold每分钟续约阀值（默认85%，该阈值在之前的自我保护机制有用到）

3. 信息同步到其他EurekaServer节点

   > 如下图所示，根据该节点的实例动作，对其他节点执行相同实例动作。
   >
   > ![image-20201108140838785](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108140838785.png)



---------

ApplicationResource类的addInstance()方法中代码：registry.register(info,"true".equals(isReplication));

![image-20201108135309680](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108135309680.png)

com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl#register - 注册服务信息并同步到其它Eureka节点

![image-20201108135323125](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108135323125.png)

AbstractInstanceRegistry#register()：注册，实例信息存储到注册表是⼀个ConcurrentHashMap

```java
public void register (InstanceInfo registrant,int leaseDuration, boolean
        isReplication){
            try {
                read.lock(); //读锁
                // registry是保存所有应用实例信息的Map：ConcurrentHashMap<String,Map<String, Lease<InstanceInfo>>>
                // 从registry中获取当前appName的所有实例信息
                Map<String, Lease<InstanceInfo>> gMap =
                        registry.get(registrant.getAppName());
                REGISTER.increment(isReplication); //注册统计+1
                // 如果当前appName实例信息为空，新建Map
                if (gMap == null) {
                    final ConcurrentHashMap<String, Lease<InstanceInfo>> gNewMap = new
                            ConcurrentHashMap<String, Lease<InstanceInfo>>();
                    gMap = registry.putIfAbsent(registrant.getAppName(), gNewMap);
                    if (gMap == null) {
                        gMap = gNewMap;
                    }
                }
				// 获取实例的Lease租约信息
                Lease<InstanceInfo> existingLease = gMap.get(registrant.getId());

				// 如果已经有租约，则保留最后⼀个脏时间戳而不覆盖它
				// （比较当前请求实例租约和已有租约的LastDirtyTimestamp，选择靠后的）
                if (existingLease != null && (existingLease.getHolder() != null)) {
     			 Long existingLastDirtyTimestamp =
          				existingLease.getHolder().getLastDirtyTimestamp();
 	  			Long registrationLastDirtyTimestamp =registrant.getLastDirtyTimestamp();
                    if (existingLastDirtyTimestamp > registrationLastDirtyTimestamp) {
                        registrant = existingLease.getHolder();
                    }
                } else {
				// 如果之前不存在实例的租约，说明是新实例注册
				// expectedNumberOfRenewsPerMin期待的每分钟续约数+2（因为30s⼀个）
				// 并更新numberOfRenewsPerMinThreshold每分钟续约阀值（85%）
                    synchronized (lock) {
                        if (this.expectedNumberOfRenewsPerMin > 0) {
                            this.expectedNumberOfRenewsPerMin =
                                    this.expectedNumberOfRenewsPerMin + 2;
                            this.numberOfRenewsPerMinThreshold =
                                    (int) (this.expectedNumberOfRenewsPerMin *
                                            serverConfig.getRenewalPercentThreshold());
                        }
                    }
                }
                Lease<InstanceInfo> lease = new Lease<InstanceInfo>(registrant,
                        leaseDuration);
                if (existingLease != null) {
                    lease.setServiceUpTimestamp(existingLease.getServiceUpTimestamp());
                }
                gMap.put(registrant.getId(), lease); //当前实例信息放到维护注册信息的Map
				// 同步维护最近注册队列
                synchronized (recentRegisteredQueue) {
                    recentRegisteredQueue.add(new Pair<Long, String>(
                            System.currentTimeMillis(),
                            registrant.getAppName() + "(" + registrant.getId() +
                                    ")"));
                }
				// 如果当前实例已经维护了OverriddenStatus，将其也放到此Eureka Server的		overriddenInstanceStatusMap中
                if (!InstanceStatus.UNKNOWN.equals(registrant.getOverriddenStatus())) {
                    
                    if (!overriddenInstanceStatusMap.containsKey(registrant.getId())) {
                       
                        overriddenInstanceStatusMap.put(registrant.getId(),
                                registrant.getOverriddenStatus());
                    }
                }
                InstanceStatus overriddenStatusFromMap =
                        overriddenInstanceStatusMap.get(registrant.getId());
                if (overriddenStatusFromMap != null) {
                  
                    registrant.setOverriddenStatus(overriddenStatusFromMap);
                }
				// 根据overridden status规则，设置状态
                InstanceStatus overriddenInstanceStatus
                        = getOverriddenInstanceStatus(registrant, existingLease,
                        isReplication);
                registrant.setStatusWithoutDirty(overriddenInstanceStatus);
				// 如果租约以UP状态注册，设置租赁服务时间戳
                if (InstanceStatus.UP.equals(registrant.getStatus())) {
                    lease.serviceUp();
                }
                registrant.setActionType(ActionType.ADDED); //ActionType为 ADD
                recentlyChangedQueue.add(new RecentlyChangedItem(lease)); //维护
                recentlyChangedQueue
                registrant.setLastUpdatedTimestamp(); //更新最后更新时间
				// 使当前应用的ResponseCache失效
                invalidateCache(registrant.getAppName(), registrant.getVIPAddress(),
                        registrant.getSecureVipAddress());
                registrant.getAppName(), registrant.getId(),
                        registrant.getStatus(), isReplication);
            } finally {
                read.unlock(); //读锁
            }
        }
```

PeerAwareInstanceRegistryImpl#replicateToPeers() ：复制到Eureka对等节点

```java
  private void replicateToPeers (Action action, String appName, String id,
                InstanceInfo info /* optional */,
                InstanceStatus newStatus /* optional */,boolean
        isReplication){
            Stopwatch tracer = action.getTimer().start();
            try {
				// 如果是复制操作（针对当前节点，false）
                if (isReplication) {
                    numberOfReplicationsLastMin.increment();
                }
				// 如果它已经是复制，请不要再次复制，直接return
                if (peerEurekaNodes == Collections.EMPTY_LIST || isReplication) {
                    return;
                }
				// 遍历集群所有节点（除当前节点外）
                for (final PeerEurekaNode node : peerEurekaNodes.getPeerEurekaNodes()) {
                    if (peerEurekaNodes.isThisMyUrl(node.getServiceUrl())) {
                        continue;
                    }
					// 复制Instance实例操作到某个node节点
                    replicateInstanceActionsToPeers(action, appName, id, info,
                            newStatus, node);
                }
            }
            finally {
                tracer.stop();
            }
        }
```

PeerAwareInstanceRegistryImpl#replicateInstanceActionsToPeers

![image-20201108140838785](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108140838785.png)



#### Eureka Server服务续约接口（接受客户端续约，上面已经有介绍）：

总结：**服务续约就是心跳检测**，续约接口其实就是使用时间戳更新续约的最新时间。

InstanceResource的renewLease方法中完成客户端的心跳（续约）处理，关键代码：
registry.renew(app.getName(), id, isFromReplicaNode);

![image-20201108150555146](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108150555146.png)

![image-20201108150601112](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108150601112.png)

com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl#renew

![image-20201108150612178](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108150612178.png)

replicateInstanceActionsToPeers() 复制Instance实例操作到其它节点

![image-20201108150645629](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108150645629.png)



#### Eureka Client启动过程：

启动过程步骤：

1. 读取配置文件

   > 读取yml或properties配置文件参数，并注入容器

2. 启动时从EurekaServer获取服务实例信息

   > 先拿本地缓存，分为全量获取和增量获取服务实例信息
   >
   > - EurekaClient 启动时，首先执行一次全量获取进行本地缓存注册信息。后面执行增量获取，但是如果本地缓存为空或禁用增量获取等情况则全量获取。

3. 注册自己到EurekaServer（addInstance）

   > 底层使用Jersey客户端发送请求，调用EurekaServer注册接口的AbstractInstanceRegistry#register()

4. 开启⼀些定时任务（心跳续约，刷新本地服务缓存列表）

   > 心跳续约：定时任务使用Jersey客户端发送请求，调用EurekaServer的renew()

启动过程：Eureka客户端在启动时也会装载很多配置类，我们通过spring-cloud-netflix-eureka-client-
2.1.0.RELEASE.jar下的spring.factories⽂件可以看到加载的配置类

![image-20201108162440865](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108162440865.png)

引⼊jar就会被自动装配，分析EurekaClientAutoConfiguration类头

![image-20201108162411084](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108162411084.png)

如果不想作为客户端，可以设置eureka.client.enabled=false

![image-20201108162345781](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108162345781.png)

1）读取配置文件

![image-20201108162519677](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108162519677.png)



2）启动时从EurekaServer获取服务实例信息（先拿本地缓存，分为全量获取和增量获取服务实例信息）

![image-20201108162547152](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108162547152.png)

3）注册自己到EurekaServer（addInstance）

DiscoveryClient#register（底层使用Jersey客户端进行远程请求，等于说请求接口在EurekaServer端）

![image-20201108164005663](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108164005663.png)

4）开启⼀些定时任务（心跳续约，刷新本地服务缓存列表）

刷新本地缓存定时任务

![image-20201108165437800](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108165437800.png)

心跳

![image-20201108165501268](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108165501268.png)

![image-20201108165508476](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108165508476.png)

![image-20201108165540533](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108165540533.png)



#### Eureka Client服务下架：

> @PreDestroy：在对象销毁之前这个方法就会被调用。
>
> 底层使用Jersey客户端发送请求下线，请求下线步骤：
>
> 1. 设置实例为DELETE状态
>
>    ```java
>    instanceInfo.setActionType(ActionType.DELETED);
>    ```
>
> 2. 清除缓存
>
>    ```java
>    invalidateCache(appName, vip, svip);
>    ```

我们看com.netflix.discovery.DiscoveryClient#shutdown

![image-20201108170624816](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108170624816.png)

![image-20201108170632625](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108170632625.png)

![image-20201108170642051](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201108170642051.png)

### Ribbon源码：

![image-20210131231808853](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131231808853.png)

Ribbon的核心组件：

1. IRule：装载多种不同负载均衡方案的接口。
2. IPing：是用来向服务发起心跳检测的，通过心跳检测来判断该服务是否可用 。
3. ServerListFilter：根据⼀些规则过滤传入的服务实例列表 。
4. ServerListUpdater：定义了⼀系列的对服务列表的更新操作。

Ribbon源码实现步骤：

1. ##### 装载Eureka Client到RibbonLoadBalancerClient

   > 添加了@LoadBalanced的注解都会被添加一个LoadBalancerInterceptor，该拦截器执行内容如下图：
   >
   > ![image-20210131232910872](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131232910872.png)
   >
   > 

2. ##### ServerList定时更新：

   通过延时定时任务，隔一段时间获取最新的Eureka Client缓存中新的服务实例信息（Eureka Client也会定时从Eureka Server更新服务信息）。

   ![image-20210131234511048](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131234511048.png)

   定时更新逻辑[就是定时调用updateAction.doUpdate()，该方法是获取本地缓存的服务实例信息]：

   ![image-20210131235124721](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131235124721.png)

3. ##### 选择轮询策略

   ![image-20210131234118956](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131234118956.png)

4. ##### 过滤传入的服务实例列表 ，并根据轮询策略选择其中一个Eureka Client。

   ![image-20210131233941407](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210131233941407.png)

   轮询策略选择其中一个Eureka Client步骤：

   - RoundRobinRule轮询策略举例：

     如下代码：

     > 1. 获取轮询对象索引方法ncrementAndGetModulo（）：
     >
     >    取上一次索引位置，加1后对列表长度取模
     >
     >    ```java
     >    // 取出上次的计数
     >    current = this.nextServerCyclicCounter.get();
     >    // 因为是轮询，计数+1之后对总数取模
     >    next = (current + 1) % modulo;
     >    ```
     >
     > 2. 根据索引取出实例对象，判断是否可用，可用的话返回
     >
     >    ```java
     >    // 获得⼀个轮询索引
     >    int nextServerIndex =
     >    this.incrementAndGetModulo(serverCount);
     >    // 根据索引取出服务实例对象
     >    server = (Server)allServers.get(nextServerIndex);
     >    // 判断服务可用后返回
     >    if (server.isAlive() && server.isReadyToServe()) {
     >     	return server;
     >    }
     >    ```

     ```java
     // 负载均衡策略类核心方法
         public Server choose(ILoadBalancer lb, Object key) {
             if (lb == null) {
                 log.warn("no load balancer");
                 return null;
             } else {
                 Server server = null;
                 int count = 0;
                 while(true) {
                     if (server == null && count++ < 10) {
                         // 所有可用服务实例列表
                         List<Server> reachableServers = lb.getReachableServers();
                         // 所有服务实例列表
                         List<Server> allServers = lb.getAllServers();
                         int upCount = reachableServers.size();
                         int serverCount = allServers.size();
                         if (upCount != 0 && serverCount != 0) {
                             // 获得⼀个轮询索引
                             int nextServerIndex =
                                     this.incrementAndGetModulo(serverCount);
                             // 根据索引取出服务实例对象
                             server = (Server)allServers.get(nextServerIndex);
                             if (server == null) {
                                 Thread.yield();
                             } else {
                                 // 判断服务可用后返回
                                 if (server.isAlive() && server.isReadyToServe()) {
                                     return server;
                                 }
                                 server = null;
                             }
                             continue;
                         }
     
                         return null;
                     }
                     if (count >= 10) {
                         log.warn("No available alive servers after 10 tries from load balancer: " + lb);
                     }
                     return server;
                 }
             }
         }
     ```

     ```java
      private int incrementAndGetModulo(int modulo) {
             int current;
             int next;
             do {
                 // 取出上次的计数
                 current = this.nextServerCyclicCounter.get();
                 // 因为是轮询，计数+1之后对总数取模
                 next = (current + 1) % modulo;
             } while(!this.nextServerCyclicCounter.compareAndSet(current, next));
             return next;
         }
     ```

     ```java
      private int incrementAndGetModulo(int modulo) {
             int current;
             int next;
             do {
                 // 取出上次的计数
                 current = this.nextServerCyclicCounter.get();
                 // 因为是轮询，计数+1之后对总数取模
                 next = (current + 1) % modulo;
             } while(!this.nextServerCyclicCounter.compareAndSet(current, next));
             return next;
         }
     ```

   - RandomRule轮询策略举例：

     ![image-20210201002126176](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210201002126176.png)

     ![image-20210201002136074](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210201002136074.png)