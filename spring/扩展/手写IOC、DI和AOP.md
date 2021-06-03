#### **第7、8、9讲：**

最原始项目调用以及问题：

![image-20200805205505305](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200805205505305.png)



问题一解决思路以及代码实现：

![new关键字耦合问题分析](D:\study\学习资料\每阶段资料\解压包\spring\Spring课程课堂作图\new关键字耦合问题分析.png)

配置XML：

```java
<beans>
<!--id标识对象，class是类的全限定类名-->
<bean id="accountDao" class="com.lagou.edu.dao.impl.JdbcTemplateDaoImpl">
    </bean>
<bean id="transferService"class="com.lagou.edu.service.impl.TransferServiceImpl">
    </bean>
</beans>
```

创建工厂类BeanFactory：

```java
/**
 * 工厂类，生产对象（使用反射技术）
 */
public class BeanFactory {
    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String,Object> map = new HashMap<>();  // 存储对象
     static {
        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
        // 加载xml
         InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        // 解析xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> beanList = rootElement.selectNodes("//bean");
            for (int i = 0; i < beanList.size(); i++) {
                Element element =  beanList.get(i);
                // 处理每个bean元素，获取到该元素的id 和 class 属性
                String id = element.attributeValue("id");        // accountDao
                String clazz = element.attributeValue("class");  //    		com.lagou.edu.dao.impl.JdbcAccountDaoImpl
                // 通过反射技术实例化对象
                Class<?> aClass = Class.forName(clazz);
                Object o = aClass.newInstance();  // 实例化之后的对象
                // 存储到map中待用
                map.put(id,o);
            }
		} catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static  Object getBean(String id) {
        return map.get(id);
    }
    
}
```

```java
public class TransferServiceImpl implements TransferService {
    //private AccountDao accountDao = new JdbcAccountDaoImpl();
    //改造为：
    private AccountDao accountDao = (AccountDao) BeanFactory.getBean("accountDao");
     @Override
    public void transfer(String fromCardNo, String toCardNo, int money){
        
    }
}
```

```java
public class TransferServlet extends HttpServlet {
   
    //private TransferService transferService = new TransferServiceImpl();
    //改造为：
      private TransferService transferService = (TransferService) BeanFactory.getBean("transferService");
}
```

**继续改造：**

```java
public class TransferServiceImpl implements TransferService {
    private AccountDao accountDao = (AccountDao) BeanFactory.getBean("accountDao");
    //想改为：private AccountDao accountDao;
    //方案：使用构造函数或set方法传值
     @Override
    public void transfer(String fromCardNo, String toCardNo, int money){
        
    }
}
```

**代码实现：**

XML加属性和BeanFactory加属性代码：

```java
<!--id标识对象，class是类的全限定类名-->
    <bean id="accountDao" class="com.lagou.edu.dao.impl.JdbcTemplateDaoImpl">
    </bean>
    <bean id="transferService"
        class="com.lagou.edu.service.impl.TransferServiceImpl">
        <!--set+ name 之后锁定到传值的set方法了，通过反射技术可以调用该方法传入对应的值-->
        <property name="AccountDao" ref="accountDao"></property>
    </bean>
```

```java
public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String,Object> map = new HashMap<>();  // 存储对象


    static {
        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
        // 加载xml
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        // 解析xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> beanList = rootElement.selectNodes("//bean");
            for (int i = 0; i < beanList.size(); i++) {
                Element element =  beanList.get(i);
                // 处理每个bean元素，获取到该元素的id 和 class 属性
                String id = element.attributeValue("id");        // accountDao
                String clazz = element.attributeValue("class");  // com.lagou.edu.dao.impl.JdbcAccountDaoImpl
                // 通过反射技术实例化对象
                Class<?> aClass = Class.forName(clazz);
                Object o = aClass.newInstance();  // 实例化之后的对象

                // 存储到map中待用
                map.put(id,o);

            }
---------------------------------------------------------------------------------
            // 实例化完成之后维护对象的依赖关系，检查哪些对象需要传值进入，根据它的配置，我们传入相应的值
            // 有property子元素的bean就有传值需求
            List<Element> propertyList = rootElement.selectNodes("//property");
            // 解析property，获取父元素
            for (int i = 0; i < propertyList.size(); i++) {
                Element element =  propertyList.get(i);   //<property name="AccountDao" ref="accountDao"></property>
                String name = element.attributeValue("name");
                String ref = element.attributeValue("ref");

                // 找到当前需要被处理依赖关系的bean
                Element parent = element.getParent();

                // 调用父元素对象的反射功能
                String parentId = parent.attributeValue("id");
                Object parentObject = map.get(parentId);
                // 遍历父对象中的所有方法，找到"set" + name
                Method[] methods = parentObject.getClass().getMethods();
                for (int j = 0; j < methods.length; j++) {
                    Method method = methods[j];
                    if(method.getName().equalsIgnoreCase("set" + name)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
                        method.invoke(parentObject,map.get(ref));//parentObject表示该方法调用的是父对象的方法；map.get(ref)表示该方法塞进去的值，这里的值是实例化的accountDao
                        
                    }
                }

                // 把处理之后的parentObject重新放到map中
                map.put(parentId,parentObject);
            }
---------------------------------------------------------------------------------

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static  Object getBean(String id) {
        return map.get(id);
    }

}
```

TransferServiceImpl修改：

```java
public class TransferServiceImpl implements TransferService {
    //private AccountDao accountDao = (AccountDao) BeanFactory.getBean("accountDao");
    //改为：
    private AccountDao accountDao;
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }
     @Override
    public void transfer(String fromCardNo, String toCardNo, int money){
        
    }
}
```



#### **第10、11讲**：

解决问题二:

添加事务控制：

扩展：

```java
  @Override
    public int updateAccountByCardNo(Account account) throws Exception {
        // 从连接池获取连接
       Connection con = DruidUtils.getInstance().getConnection();
       con.setAutoCommit(false);//数据库之所以自动提交是因为该方法默认为true，改为false即为不自动提交
        String sql = "update account set money=? where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1,account.getMoney());
        preparedStatement.setString(2,account.getCardNo());
        int i = preparedStatement.executeUpdate();
        preparedStatement.close();
        //con.close();
        return i;
    }
```

问题二分析:两次update使用同一个connection

改造：

新建ConnectionUtils.java

```java
public class ConnectionUtils {
	//单例模式保证每次new ConnectionUtils保持ThreadLocal是同一个：具体需了解单例模式
    private static ConnectionUtils connectionUtils = new ConnectionUtils();

    public static ConnectionUtils getInstance() {
        return connectionUtils;
    }


    private ThreadLocal<Connection> threadLocal = new ThreadLocal<>(); // 存储当前线程的连接

    /**
     * 从当前线程获取连接
     */
    public Connection getCurrentThreadConn() throws SQLException {
        /**
         * 判断当前线程中是否已经绑定连接，如果没有绑定，需要从连接池获取一个连接绑定到当前线程
          */
        Connection connection = threadLocal.get();
        if(connection == null) {
            // 从连接池拿连接并绑定到线程
            connection = DruidUtils.getInstance().getConnection();
            // 绑定到当前线程
            threadLocal.set(connection);
        }
        return connection;

    }
}

```



```java
 @Override
    public int updateAccountByCardNo(Account account) throws Exception {

        // 从连接池获取连接
        //Connection con = DruidUtils.getInstance().getConnection();
        // 改造为：从当前线程当中获取绑定的connection连接
        Connection con = connectionUtils.getCurrentThreadConn();
        String sql = "update account set money=? where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1,account.getMoney());
        preparedStatement.setString(2,account.getCardNo());
        int i = preparedStatement.executeUpdate();

        preparedStatement.close();
        //con.close();//不要关闭
        return i;
    }
```

#### 第12讲

把事务控制放到service层

```java
 @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {

        try{
            // 开启事务(关闭事务的自动提交)
            ConnectionUtils.getInstance().getCurrentThreadConn()
           .setAutoCommit(false);
            Account from = accountDao.queryAccountByCardNo(fromCardNo);
            Account to = accountDao.queryAccountByCardNo(toCardNo);

            from.setMoney(from.getMoney()-money);
            to.setMoney(to.getMoney()+money);

            accountDao.updateAccountByCardNo(to);
            int c = 1/0;
            accountDao.updateAccountByCardNo(from);

          // 提交事务
            ConnectionUtils.getInstance().getCurrentThreadConn().commit();
        }catch (Exception e) {
            e.printStackTrace();
            // 回滚事务
            ConnectionUtils.getInstance().getCurrentThreadConn().rollback();
            // 抛出异常便于上层servlet捕获
            throw e;

        }
    }
```

把ConnectionUtils里的事务控制放到TransactionManager（面对对象需要）：

```java
/**
 * @author 应癫
 *
 * 事务管理器类：负责手动事务的开启、提交、回滚
 */
public class TransactionManager {

    private ConnectionUtils connectionUtils;

    public void setConnectionUtils(ConnectionUtils connectionUtils) {
        this.connectionUtils = connectionUtils;
    }

 private TransactionManager(){

    }

    private static TransactionManager transactionManager = new TransactionManager();

    public static TransactionManager getInstance() {
        return  transactionManager;
    }



    // 开启手动事务控制
    public void beginTransaction() throws SQLException {
        connectionUtils.getCurrentThreadConn().setAutoCommit(false);
    }


    // 提交事务
    public void commit() throws SQLException {
        connectionUtils.getCurrentThreadConn().commit();
    }


    // 回滚事务
    public void rollback() throws SQLException {
        connectionUtils.getCurrentThreadConn().rollback();
    }
}

```

```java
 @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {

       try{
            // 开启事务(关闭事务的自动提交)
            TransactionManager.getInstance().beginTransaction();
            Account from = accountDao.queryAccountByCardNo(fromCardNo);
            Account to = accountDao.queryAccountByCardNo(toCardNo);

            from.setMoney(from.getMoney()-money);
            to.setMoney(to.getMoney()+money);

            accountDao.updateAccountByCardNo(to);
            int c = 1/0;
            accountDao.updateAccountByCardNo(from);

       // 提交事务

            TransactionManager.getInstance().commit();
        }catch (Exception e) {
            e.printStackTrace();
            // 回滚事务
            TransactionManager.getInstance().rollback();

            // 抛出异常便于上层servlet捕获
            throw e;

        }
    }
```

#### 第13讲：

**动态和静态代理：**

静态代理：缺点是实现类增加方法，代理类也要添加相应方法。耦合度过高

```java
public interface IRentingHouse {
    void rentHosue();
}
public class RentingHouseImpl implements IRentingHouse {
    @Override
    public void rentHosue() {
        System.out.println("我要租用一室一厅的房子");
    }
}
public class RentingHouseProxy implements IRentingHouse {

    private IRentingHouse rentingHouse;

    public RentingHouseProxy(IRentingHouse rentingHouse) {
        this.rentingHouse = rentingHouse;
    }

    @Override
    public void rentHosue() {
        System.out.println("中介（代理）收取服务费3000元");
        rentingHouse.rentHosue();
        System.out.println("客户信息卖了3毛钱");
    }
}
public class Test {

    public static void main(String[] args) {
        IRentingHouse rentingHouse = new RentingHouseImpl();
        // 自己要租用一个一室一厅的房子
        // rentingHouse.rentHosue();
        RentingHouseProxy rentingHouseProxy = new RentingHouseProxy(rentingHouse);
        rentingHouseProxy.rentHosue();
    }
}

```

**动态代理：**

```java
public interface IRentingHouse {
    void rentHosue();
}
/**
 * 委托方（委托对象）
 */
public class RentingHouseImpl implements IRentingHouse {
    @Override
    public void rentHosue() {
        System.out.println("我要租用一室一厅的房子");
    }
}


public class JdkProxy {
    public static void main(String[] args) {
        
        IRentingHouse rentingHouse = new RentingHouseImpl();  // 委托对象---委托方
       
        // 从代理对象工厂获取代理对象
 IRentingHouse proxy =(IRentingHouse)Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;

                        // 写增强逻辑
                        System.out.println("中介（代理）收取服务费3000元");
                        // 调用原有业务逻辑
                        result = method.invoke(obj,args);
                        System.out.println("客户信息卖了3毛钱");
                        return result;
                    }
                });
    proxy.rentHouse();
    }
}
```

**把代理逻辑添加到工厂：**

```java
/**
 *
 * 代理对象工厂：生成代理对象的
 */

public class ProxyFactory {
    //设置单例工厂
    private ProxyFactory(){

    }

    private static ProxyFactory proxyFactory = new ProxyFactory();

    public static ProxyFactory getInstance() {
        return proxyFactory;
    }
/**
     * Jdk动态代理
     * @param obj  委托对象
     * @return   代理对象
     */
    public Object getJdkProxy(Object obj) {
        // 获取代理对象
        return  Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;

                        // 写增强逻辑
                        System.out.println("中介（代理）收取服务费3000元");
                        // 调用原有业务逻辑
                        result = method.invoke(obj,args);

                        System.out.println("客户信息卖了3毛钱");

                        return result;
                    }
                });

    }
}
```

**JdkProxy改造成：**

```java
/**
 * @author 应癫
 */
public class JdkProxy {

    public static void main(String[] args) {

        IRentingHouse rentingHouse = new RentingHouseImpl();  // 委托对象---委托方

        // 从代理对象工厂获取代理对象
        IRentingHouse jdkProxy = (IRentingHouse) ProxyFactory.getInstance().getJdkProxy(rentingHouse);

        jdkProxy.rentHosue();


    }
}

```

#### **第14讲：**

cglib动态代理（和jdk动态代理区别在于不需强制被代理类是接口实现类型）

```java
 /**
     * 使用cglib动态代理生成代理对象
     * @param obj 委托对象
     * @return
     */
    public Object getCglibProxy(Object obj) {
        return  Enhancer.create(obj.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Object result = null;
                System.out.println("中介（代理）收取服务费3000元");
                result = method.invoke(obj,objects);
                System.out.println("客户信息卖了3毛钱");
                return result;
            }
        });
    }
```

```java
public class CglibProxy {

    public static void main(String[] args) {
        RentingHouseImpl rentingHouse = new RentingHouseImpl();  // 委托对象
        // 获取rentingHouse对象的代理对象，
        // Enhancer类似于JDK动态代理中的Proxy
        // 通过实现接口MethodInterceptor能够对各个方法进行拦截增强，类似于JDK动态代理中的InvocationHandler
        // 使用工厂来获取代理对象
        RentingHouseImpl cglibProxy = (RentingHouseImpl) ProxyFactory.getInstance().getCglibProxy(rentingHouse);

        cglibProxy.rentHosue();
    }
}
```

#### 第15讲

动态代理改造service事务控制：service只需实现业务逻辑，事务控制放在代理类就行



```java
/**
  * 代理对象工厂：生成代理对象的
  */

public class ProxyFactory {
   /**
     * 使用cglib动态代理生成代理对象
     * @param obj 委托对象
     * @return
     */
    public Object getCglibProxy(Object obj) {
        return  Enhancer.create(obj.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Object result = null;
                try{
                    // 开启事务(关闭事务的自动提交)
                    transactionManager.beginTransaction();

                    result = method.invoke(obj,objects);

                    // 提交事务

                    transactionManager.commit();
                }catch (Exception e) {
                    e.printStackTrace();
                    // 回滚事务
                    transactionManager.rollback();

                    // 抛出异常便于上层servlet捕获
                    throw e;

                }
                return result;
            }
        });
    }
}
```

```java
public class TransferServiceImpl implements TransferService {
    // 最佳状态
    private AccountDao accountDao;

    // 构造函数传值/set方法传值
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }
   @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {
            Account from = accountDao.queryAccountByCardNo(fromCardNo);
            Account to = accountDao.queryAccountByCardNo(toCardNo);

            from.setMoney(from.getMoney()-money);
            to.setMoney(to.getMoney()+money);

            accountDao.updateAccountByCardNo(to);
            int c = 1/0;
            accountDao.updateAccountByCardNo(from);

    }
}
```

```java
public class TransferServlet extends HttpServlet {
    // 1. 实例化service层对象
    //private TransferService transferService = new TransferServiceImpl();
    //private TransferService transferService = (TransferService) BeanFactory.getBean("transferService");
改为：
    // 从工厂获取委托对象（委托对象是增强了事务控制的功能）
    // 首先从BeanFactory获取到proxyFactory代理工厂的实例化对象
    private ProxyFactory proxyFactory =    (ProxyFactory)BeanFactory.getBean("proxyFactory");
    private TransferService transferService = (TransferService) proxyFactory.getJdkProxy(BeanFactory.getBean("transferService")) ;
}
```

