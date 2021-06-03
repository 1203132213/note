<img src="D:\study\学习资料\笔记\mybatis\mybatis链接\未命名文件.png" style="zoom:200%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200804145220573.png" alt="image-20200804145220573" style="zoom:100%;" />

先从初始化开始：

```java
test.java
    
InputStream inputStream = Resources.getResourceAsStream("sqlMapConfig.xml");
//这一行是初始化工作的开始
//该方法的作用：1.将XML文件解析成Configuration; 2.创建DefaultSqlSessionFactory实例，将解析出的Configuration通过构造方法带入该类中。
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

//指定获得的sqlSession、Executor类型和事务的处理
SqlSession sqlSession = sqlSessionFactory.openSession();

```

```java
public class SqlSessionFactoryBuilder {
    public SqlSessionFactory build(InputStream inputStream) {
             //调用重载方法
            return this.build((InputStream)inputStream, (String)null, (Properties)null);
        } 

    public SqlSessionFactory build(InputStream inputStream, String environment,Properties properties) {
            SqlSessionFactory var5;
            try {
                XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
                //调用重载方法，parse.parse()：即将XML成Configuration对象。
                var5 = this.build(parser.parse());
            } catch (Exception var14) {
                throw ExceptionFactory.wrapException("Error building SqlSession.", var14);
            } finally {
                ErrorContext.instance().reset();

                try {
                    inputStream.close();
                } catch (IOException var13) {
                }

            }

            return var5;
        }

	public SqlSessionFactory build(Configuration config) {
    	//触发DefaultSqlSessionFactory构造函数
        return new DefaultSqlSessionFactory(config);
    }
}
```

```java
public class XMLConfigBuilder extends BaseBuilder {
    public Configuration parse() {
        if (this.parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        } else {
            //该字段表示已解析XML文件
            this.parsed = true;
            //解析XML文件的Configuration标签节点
            this.parseConfiguration(this.parser.evalNode("/configuration"));
            return this.configuration;
        }
    }
    private void parseConfiguration(XNode root) {
            try {
                //解析properties标签，后面都是一样
                this.propertiesElement(root.evalNode("properties"));
                this.typeAliasesElement(root.evalNode("typeAliases"));
                this.pluginElement(root.evalNode("plugins"));
                this.objectFactoryElement(root.evalNode("objectFactory"));
                this.objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
                this.reflectionFactoryElement(root.evalNode("reflectionFactory"));
                this.settingsElement(root.evalNode("settings"));
                this.environmentsElement(root.evalNode("environments"));
                this.databaseIdProviderElement(root.evalNode("databaseIdProvider"));
                this.typeHandlerElement(root.evalNode("typeHandlers"));
                //解析<mappers>标签，这里是SQL语句文件
                this.mapperElement(root.evalNode("mappers"));
            } catch (Exception var3) {
                throw new BuilderException("Error parsing SQL Mapper Configuration.Cause: " + var3, var3);
            }
        }
}
```

```java
public interface SqlSessionFactory {
    //默认使用这个
    SqlSession openSession();
	//有boolean的入参都是可以控制是否开启事务，其他默认不开启
    SqlSession openSession(boolean var1);

    SqlSession openSession(Connection var1);
	//TransactionIsolationLevel：事务隔离级别 细节记得百度！！！
    SqlSession openSession(TransactionIsolationLevel var1);
	//根据ExecutorType判断使用哪个Executor
    SqlSession openSession(ExecutorType var1);

    SqlSession openSession(ExecutorType var1, boolean var2);
	
    SqlSession openSession(ExecutorType var1, TransactionIsolationLevel var2);

    SqlSession openSession(ExecutorType var1, Connection var2);

    Configuration getConfiguration();
}
扩展：
ExecutorType类型有三种：
    SimpleExecutor是最简单的执行器，根据对应的sql直接执行即可，不会做一些额外的操作；
    
	BatchExecutor执行器，顾名思义，通过批量操作来优化性能。通常需要注意的是批量更新操作，由于内部有缓存	的实现，使用完成后记得调用flushStatements来清除缓存。
    
	ReuseExecutor 可重用的执行器，重用的对象是Statement，也就是说该执行器会缓存同一个sql的		 	 Statement，省去Statement的重新创建，优化性能。内部的实现是通过一个HashMap来维护Statement对象的。	  由于当前Map只在该session中有效，所以使用完成后记得调用flushStatements来清除Map。
    
这几个Executor的生命周期都是局限于SqlSession范围内。
Executor选择的XML配置 
<settings>
<!--取值范围 SIMPLE, REUSE, BATCH -->
	<setting name="defaultExecutorType" value="SIMPLE"/>
</settings>
    
SimpleExecutor、ReuseExecutor、BatchExecutor都继承BaseExecutor，BaseExecutor实现Executor接口
```



```java
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }
public SqlSession openSession() {
    //该测试类传递的是SimpleExecutor
    return this.openSessionFromDataSource(this.configuration.getDefaultExecutorType(), (TransactionIsolationLevel)null, false);
    }

//TransactionIsolationLevel事务隔离级别，autoCommit为是否开启事务
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;

        DefaultSqlSession var8;
        try {
            Environment environment = this.configuration.getEnvironment();
            TransactionFactory transactionFactory = this.getTransactionFactoryFromEnvironment(environment);
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            //根据参数创建指定类型的Executor
            Executor executor = this.configuration.newExecutor(tx, execType);
            //触发DefaultSqlSession构造函数
            var8 = new DefaultSqlSession(this.configuration, executor, autoCommit);
        } catch (Exception var12) {
            this.closeTransaction(tx);
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + var12, var12);
        } finally {
            ErrorContext.instance().reset();
        }
        return var8;
    }
}
```

```java
//sqlSession类型
public interface SqlSession extends Closeable {
    <T> T selectOne(String var1);

    <T> T selectOne(String var1, Object var2);

    <E> List<E> selectList(String var1);

    <E> List<E> selectList(String var1, Object var2);

    <E> List<E> selectList(String var1, Object var2, RowBounds var3);

    <K, V> Map<K, V> selectMap(String var1, String var2);

    <K, V> Map<K, V> selectMap(String var1, Object var2, String var3);

    <K, V> Map<K, V> selectMap(String var1, Object var2, String var3, RowBounds var4);

    void select(String var1, Object var2, ResultHandler var3);

    void select(String var1, ResultHandler var2);

    void select(String var1, Object var2, RowBounds var3, ResultHandler var4);

    int insert(String var1);

    int insert(String var1, Object var2);

    int update(String var1);

    int update(String var1, Object var2);

    int delete(String var1);

    int delete(String var1, Object var2);

    void commit();

    void commit(boolean var1);

    void rollback();

    void rollback(boolean var1);

    List<BatchResult> flushStatements();

    void close();

    void clearCache();

    Configuration getConfiguration();

    <T> T getMapper(Class<T> var1);

    Connection getConnection();
}

```

```java
public class DefaultSqlSession implements SqlSession {
    private Configuration configuration;
    private Executor executor;
    private boolean autoCommit;
    private boolean dirty;
    public DefaultSqlSession(Configuration configuration, Executor executor, boolean 		autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }
     //随便举例一个sqlsession操作
     public <E> List<E> selectList(String statement, Object parameter) {
         //重载方法
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }
	//parameter：sql语句传入参数；rowBounds：逻辑分页字段；
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        List var5;
        try {
            //statement：dao的包名+方法名，从Configuration中的Map中取出MappedStatement对象
            MappedStatement ms = this.configuration.getMappedStatement(statement);
            //调用Executor方法
            var5 = this.executor.query(ms, this.wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
        } catch (Exception var9) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + var9, var9);
        } finally {
            ErrorContext.instance().reset();
        }

        return var5;
    }
}
```

```java
//SimpleExecutor、ReuseExecutor、BatchExecutor都继承BaseExecutor，BaseExecutor实现Executor接口
public abstract class BaseExecutor implements Executor {
     public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, 		ResultHandler resultHandler) throws SQLException {
        //传入的参数动态获得sql语句，最后返回BoundSql对象
        BoundSql boundSql = ms.getBoundSql(parameter);
         //创建缓存的key
        CacheKey key = this.createCacheKey(ms, parameter, rowBounds, boundSql);
        return this.query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }
    
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
        if (this.closed) {
            throw new ExecutorException("Executor was closed.");
        } else {
            if (this.queryStack == 0 && ms.isFlushCacheRequired()) {
                this.clearLocalCache();
            }

            List list;
            try {
                ++this.queryStack;
                list = resultHandler == null ? (List)this.localCache.getObject(key) : null;
                if (list != null) {
                    this.handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
                } else {
                    //如果缓存中没有本次查找的值，从数据库中查询
                    list = this.queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
                }
            } finally {
                --this.queryStack;
            }

            if (this.queryStack == 0) {
                Iterator i$ = this.deferredLoads.iterator();

                while(i$.hasNext()) {
                    BaseExecutor.DeferredLoad deferredLoad = (BaseExecutor.DeferredLoad)i$.next();
                    deferredLoad.load();
                }

                this.deferredLoads.clear();
                if (this.configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                    this.clearLocalCache();
                }
            }

            return list;
        }
    }
//从数据库查询
private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        this.localCache.putObject(key, ExecutionPlaceholder.EXECUTION_PLACEHOLDER);

        List list;
        try {
            //查询的方法：有SimpleExcutor、BatchExecutor、ReuseExecutor选择
            list = this.doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            this.localCache.removeObject(key);
        }
		//将查询结果放入缓存
        this.localCache.putObject(key, list);
        if (ms.getStatementType() == StatementType.CALLABLE) {
            this.localOutputParameterCache.putObject(key, parameter);
        }
        return list;
    }
}

```

```java
//以SimpleExcutor为例
public class SimpleExecutor extends BaseExecutor {
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;

        List var9;
        try {
            Configuration configuration = ms.getConfiguration();
            //传入参数创建StatementHandler对象来执行查询
            StatementHandler handler = configuration.newStatementHandler(this.wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            //创建jdbc中的statement对象
            stmt = this.prepareStatement(handler, ms.getStatementLog());
            //StatementHandler进行处理
            var9 = handler.query(stmt, resultHandler);
        } finally {
            this.closeStatement(stmt);
        }

        return var9;
    }
    //创建Statement的方法
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        //这条代码getConnection方法会重重调用，最后调用openConnection，从连接池获取连接
        Connection connection = this.getConnection(statementLog);
        Statement stmt = handler.prepare(connection);
        //使用
        handler.parameterize(stmt);
        return stmt;
    }
}
```

```java
public class JdbcTransaction implements Transaction {
     protected void openConnection() throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Opening JDBC Connection");
        }
		//从连接池获取连接
        this.connection = this.dataSource.getConnection();
        if (this.level != null) {
            this.connection.setTransactionIsolation(this.level.getLevel());
        }
        this.setDesiredAutoCommit(this.autoCommmit);
    }
}
```

```java
public class PreparedStatementHandler extends BaseStatementHandler { 
	public void parameterize(Statement statement) throws SQLException {
        //parameterHandler对象完成对statement的设值
        this.parameterHandler.setParameters((PreparedStatement)statement);
    }
}
```

```java
public class DefaultParameterHandler implements ParameterHandler {
    /**
     * 对某一个Statement进行设置参数
     */
     public void setParameters(PreparedStatement ps) {
        ErrorContext.instance().activity("setting parameters").object(this.mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = this.boundSql.getParameterMappings();
        if (parameterMappings != null) {
            for(int i = 0; i < parameterMappings.size(); ++i) {
                ParameterMapping parameterMapping = (ParameterMapping)parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    String propertyName = parameterMapping.getProperty();
                    Object value;
                    if (this.boundSql.hasAdditionalParameter(propertyName)) {
                        value = this.boundSql.getAdditionalParameter(propertyName);
                    } else if (this.parameterObject == null) {
                        value = null;
                    } else if (this.typeHandlerRegistry.hasTypeHandler(this.parameterObject.getClass())) {
                        value = this.parameterObject;
                    } else {
                        MetaObject metaObject = this.configuration.newMetaObject(this.parameterObject);
                        value = metaObject.getValue(propertyName);
                    }
					//每一个Mapping都有TypeHandler，根据TypeHandler来对PreparedStatement进行设置参数
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) {
                        jdbcType = this.configuration.getJdbcTypeForNull();
                    }

                    try {
                        //设置参数
                        typeHandler.setParameter(ps, i + 1, value, jdbcType);
                    } catch (TypeException var10) {
                        throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + var10, var10);
                    } catch (SQLException var11) {
                        throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + var11, var11);
                    }
                }
            }
        }

    }

}
```

```java
public class PreparedStatementHandler extends BaseStatementHandler {
     public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        //调用PreparedStatement.execute()方法，然后将resultSet交给ResultSetHandler处理
        PreparedStatement ps = (PreparedStatement)statement;
        ps.execute();
        return this.resultSetHandler.handleResultSets(ps);
    }
}
```

```java
public class DefaultResultSetHandler implements ResultSetHandler {
    
     public List<Object> handleResultSets(Statement stmt) throws SQLException {
        ErrorContext.instance().activity("handling results").object(this.mappedStatement.getId());
         //多ResultSet的结果集合，每个ResultSet对应一个Object对象。实际上，每个Object是List<Object>对象。
        //在不存储多个ResultSet 的情况下，实际上就一个ResultSet，也就是说，multipleResults 最多就一个元素。
        List<Object> multipleResults = new ArrayList();
         
        int resultSetCount = 0;
         //获得首个ResultSet对象，并封装成ResultSetWrapper对象
 		ResultSetWrapper rsw = this.getFirstResultSet(stmt);
         //获得首个ResultSet数组
         //在不考虑存储多ResultSet的情况下，普通的查询，实际上就一个ResultSet，也就是说，resultMaps就一个元素
        List<ResultMap> resultMaps = this.mappedStatement.getResultMaps();
        int resultMapCount = resultMaps.size();
        this.validateResultMapsCount(rsw, resultMapCount);//校验

   		 while(rsw != null && resultMapCount > resultSetCount) {
    	    //获取ResultMap对象
 		 	ResultMap resultMap = 
         	(ResultMap)resultMaps.get(resultSetCount);
            //处理ResultSet，将结果添加到multipleResults中
  		 	this.handleResultSet(rsw, resultMap, multipleResults,  (ResultMapping)null);
            //获取下一个ResultSet对象，并封装成ResultSetWrapper对象
            rsw = this.getNextResultSet(stmt);
            //清理
            this.cleanUpAfterHandlingResultSet();
            ++resultSetCount;
        }
		//mappedStatement.getResulSets，该方法只在存储过程中使用！！！
        String[] resultSets = this.mappedStatement.getResulSets();
        if (resultSets != null) {
            while(rsw != null && resultSetCount < resultSets.length) {
                ResultMapping parentMapping = (ResultMapping)this.nextResultMaps.get(resultSets[resultSetCount]);
                if (parentMapping != null) {
                    String nestedResultMapId = parentMapping.getNestedResultMapId();
                    ResultMap resultMap = this.configuration.getResultMap(nestedResultMapId);
                    this.handleResultSet(rsw, resultMap, (List)null, parentMapping);
                }

                rsw = this.getNextResultSet(stmt);
                this.cleanUpAfterHandlingResultSet();
                ++resultSetCount;
            }
        }
		//如果是multipleResults单元素，则取首元素返回
        return this.collapseSingleResultList(multipleResults);
     }
}
```

**Mapper代理方式：**

```java
public static void main(String[] args) { 
    InputStream inputStream = Resources.getResourceAsStream("sqlMapConfig.xml");     
    SqlSessionFactory factory 
     = new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession sqlSession = factory.openSession();      
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);  
    List<User> list = mapper.getUserByName("tom"); 
}
```

```java
public class DefaultSqlSession implements SqlSession {
   //type：传入dao包名
  public <T> T getMapper(Class<T> type) {
        return this.configuration.getMapper(type, this);
    }
}
```

```java
public class Configuration { 
 public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return this.mapperRegistry.getMapper(type, sqlSession);
    }
}
```

```java
public class MapperRegistry {
     public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory)this.knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        } else {
            try {
                
                return mapperProxyFactory.newInstance(sqlSession);
            } catch (Exception var5) {
                throw new BindingException("Error getting mapper instance. Cause: " + var5, var5);
            }
        }
    }
}
```

```java
public class MapperProxyFactory<T> {
     public T newInstance(SqlSession sqlSession) {
        MapperProxy<T> mapperProxy = new MapperProxy(sqlSession, this.mapperInterface, this.methodCache);
        return this.newInstance(mapperProxy);
    }
      protected T newInstance(MapperProxy<T> mapperProxy) {
	  //this.mapperInterface.getClassLoader(): 用哪个类加载器去加载代理对象
      //new Class[]{this.mapperInterface}:动态代理类需要实现的接口
      //mapperProxy:动态代理方法在执行时，会调用mapperProxy里面的invoke方法去执行
        return Proxy.newProxyInstance(this.mapperInterface.getClassLoader(), new Class[]{this.mapperInterface}, mapperProxy);
    }
}
```

```java
public class MapperProxy<T> implements InvocationHandler, Serializable {
    
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            try {
                //如果是Object定义的方式，直接调用
                return method.invoke(this, args);
            } catch (Throwable var5) {
                throw ExceptionUtil.unwrapThrowable(var5);
            }
        } else {
            //获取MapperMethod对象
            MapperMethod mapperMethod = this.cachedMapperMethod(method);
            //执行execute方法
            return mapperMethod.execute(this.sqlSession, args);
        }
    }
}
```

```java
//判断是增删改查哪一种走到相应执行方法到sqlsession层
public class MapperMethod {
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object param;
        Object result;
        if (SqlCommandType.INSERT == this.command.getType()) {
            //转换参数
            param = this.method.convertArgsToSqlCommandParam(args);
            //执行insert操作、转换rowCount
            result = this.rowCountResult(sqlSession.insert(this.command.getName(), param));
        } else if (SqlCommandType.UPDATE == this.command.getType()) {
            param = this.method.convertArgsToSqlCommandParam(args);
            result = this.rowCountResult(sqlSession.update(this.command.getName(), param));
        } else if (SqlCommandType.DELETE == this.command.getType()) {
            param = this.method.convertArgsToSqlCommandParam(args);
            result = this.rowCountResult(sqlSession.delete(this.command.getName(), param));
        } else if (SqlCommandType.SELECT == this.command.getType()) {
            if (this.method.returnsVoid() && this.method.hasResultHandler()) {
                this.executeWithResultHandler(sqlSession, args);
                result = null;
            } else if (this.method.returnsMany()) {
                result = this.executeForMany(sqlSession, args);
            } else if (this.method.returnsMap()) {
                result = this.executeForMap(sqlSession, args);
            } else {
                param = this.method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(this.command.getName(), param);
            }
        } else {
            if (SqlCommandType.FLUSH != this.command.getType()) {
                throw new BindingException("Unknown execution method for: " + this.command.getName());
            }

            result = sqlSession.flushStatements();
        }

        if (result == null && this.method.getReturnType().isPrimitive() && !this.method.returnsVoid()) {
            throw new BindingException("Mapper method '" + this.command.getName() + " attempted to return null from a method with a primitive return type (" + this.method.getReturnType() + ").");
        } else {
            return result;
        }
    }
}
```







##### 总结：

1.先初始化：

```java
InputStream inputStream = Resources.getResourceAsStream("sqlMapConfig.xml");
//这一行是初始化工作的开始
//该方法的作用：1.将XML文件解析成Configuration; 2.创建DefaultSqlSessionFactory实例，将解析出的Configuration通过构造方法带入该类中。
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

//指定获得的sqlSession、Executor类型和事务的处理
SqlSession sqlSession = sqlSessionFactory.openSession();
```

2.获取dao的代理类，可以获取代理类dao的类路径和方法名拼接成statmentid

```java
IUserDao userDao = sqlSession.getMapper(IUserDao.class);

 @Override
    public <T> T getMapper(Class<?> mapperClass) {
        // 使用JDK动态代理来为Dao接口生成代理对象，并返回
        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 底层都还是去执行JDBC代码
                // 根据不同情况，来调用selctList或者selectOne
                // 准备参数 1：statmentid :sql语句的唯一标识：namespace.id= 接口全限定名.方法名
                // 方法名：findAll
                String methodName = method.getName();
                String className = method.getDeclaringClass().getName();

                String statementId = className+"."+methodName;

                // 准备参数2：params:args
                // 获取被调用方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                // 判断是否进行了 泛型类型参数化
                if(genericReturnType instanceof ParameterizedType){
                    List<Object> objects = selectList(statementId, args);
                    return objects;
                }

                return selectOne(statementId,args);

            }
        });

        return (T) proxyInstance;
    }
```

3.执行dao里面对应方法，代理类就能判断是该执行selectOne还是selectList

```java
 List<User> all = userDao.findAll();
```

以selectList为例，先通过statementid从configuration获取sql语句mappedStatement，最终调用simpleExecutor执行数据库语句

```java
@Override
    public <E> List<E> selectList(String statementid, Object... params) throws Exception {

        //将要去完成对simpleExecutor里的query方法的调用
        simpleExecutor simpleExecutor = new simpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementid);
        List<Object> list = simpleExecutor.query(configuration, mappedStatement, params);

        return (List<E>) list;
    }
```

4.simpleExecutor执行数据库语句过程（会完成对#{}的解析工作）

```java
    @Override                                                                                //user
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws Exception {
        // 1. 注册驱动，获取连接
        Connection connection = configuration.getDataSource().getConnection();

        // 2. 获取sql语句 : select * from user where id = #{id} and username = #{username}
            //转换sql语句： select * from user where id = ? and username = ? ，转换的过程中，还需要对#{}里面的值进行解析存储
        String sql = mappedStatement.getSql();
        BoundSql boundSql = getBoundSql(sql);

        // 3.获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());

        // 4. 设置参数
            //获取到了参数的全路径
         String paramterType = mappedStatement.getParamterType();
         Class<?> paramtertypeClass = getClassType(paramterType);

        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappingList();
        for (int i = 0; i < parameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = parameterMappingList.get(i);
            String content = parameterMapping.getContent();

            //反射
            Field declaredField = paramtertypeClass.getDeclaredField(content);
            //暴力访问
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);

            preparedStatement.setObject(i+1,o);

        }


        // 5. 执行sql
        ResultSet resultSet = preparedStatement.executeQuery();
        String resultType = mappedStatement.getResultType();
        Class<?> resultTypeClass = getClassType(resultType);

        ArrayList<Object> objects = new ArrayList<>();

        // 6. 封装返回结果集
        while (resultSet.next()){
            Object o =resultTypeClass.newInstance();
            //元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {

                // 字段名
                String columnName = metaData.getColumnName(i);
                // 字段的值
                Object value = resultSet.getObject(columnName);

                //使用反射或者内省，根据数据库表和实体的对应关系，完成封装
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultTypeClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(o,value);


            }
            objects.add(o);

        }
            return (List<E>) objects;

    }

    private Class<?> getClassType(String paramterType) throws ClassNotFoundException {
        if(paramterType!=null){
            Class<?> aClass = Class.forName(paramterType);
            return aClass;
        }
         return null;

    }


    /**
     * 完成对#{}的解析工作：1.将#{}使用？进行代替，2.解析出#{}里面的值进行存储
     * @param sql
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        //标记处理类：配置标记解析器来完成对占位符的解析处理工作
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", parameterMappingTokenHandler);
        //解析出来的sql
        String parseSql = genericTokenParser.parse(sql);
        //#{}里面解析出来的参数名称
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();

        BoundSql boundSql = new BoundSql(parseSql,parameterMappings);
         return boundSql;
    }


```

