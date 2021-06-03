[TOC]

## DispatcherServlet继承结构

如下图，HttpServlet时最上层的抽像类，往下依次继承上一层。

![image-20200814095705853](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200814095705853.png)



## doDispatch（）的核心步骤

> DispatcherServlet.doDispatch（）地位相当于spring源码的refresh（）

#### 整体流程：

1）调用getHandler()获取到能够处理当前请求的执行链 HandlerExecutionChain（Handler+拦截
器）但是如何去getHandler的?<span style ="color:red">--得到Handler</span>(细节后面分析)

2）调用getHandlerAdapter()；获取能够执行1）中Handler的适配器
但是如何去getHandlerAdapter的？<span style ="color:red">--得到执行Handler的适配器</span>(细节后面分析)

3）适配器调用Handler执行ha.handle（总会返回⼀个ModelAndView对象）<span style ="color:red">--执行Handler</span>(细节后面分析)

4）调用processDispatchResult()方法完成视图渲染跳转<span style ="color:red">--返回前端</span>

```java
@SuppressWarnings("serial")
public class DispatcherServlet extends FrameworkServlet {   
		protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		boolean multipartRequestParsed = false;

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

		try {
			ModelAndView mv = null;
			Exception dispatchException = null;

			try {
				// 1 检查是否是文件上传的请求
				processedRequest = checkMultipart(request);
				multipartRequestParsed = (processedRequest != request);

				// Determine handler for the current request.
				/*
				 	2 取得处理当前请求的Controller，这里也称为Handler，即处理器
				 	  这里并不是直接返回 Controller，而是返回 HandlerExecutionChain 请求处理链对象
				 	  该对象封装了Handler和Inteceptor
				 */
				mappedHandler = getHandler(processedRequest);
				if (mappedHandler == null) {
					// 如果 handler 为空，则返回404
					noHandlerFound(processedRequest, response);
					return;
				}

				// Determine handler adapter for the current request.
				// 3 获取处理请求的处理器适配器 HandlerAdapter
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

				// Process last-modified header, if supported by the handler.
				// 处理 last-modified 请求头
				String method = request.getMethod();
				boolean isGet = "GET".equals(method);
				if (isGet || "HEAD".equals(method)) {
					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
						return;
					}
				}

				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
					return;
				}

				// Actually invoke the handler.
				// 4 实际处理器处理请求，返回结果视图对象
				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

				if (asyncManager.isConcurrentHandlingStarted()) {
					return;
				}
				// 结果视图对象的处理
				applyDefaultViewName(processedRequest, mv);
				mappedHandler.applyPostHandle(processedRequest, response, mv);
			}
			catch (Exception ex) {
				dispatchException = ex;
			}
			catch (Throwable err) {

				dispatchException = new NestedServletException("Handler dispatch failed", err);
			}
			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
		}
		catch (Exception ex) {
			//最终会调用HandlerInterceptor的afterCompletion 方法
			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
		}
		catch (Throwable err) {
			//最终会调用HandlerInterceptor的afterCompletion 方法
			triggerAfterCompletion(processedRequest, response, mappedHandler,
					new NestedServletException("Handler processing failed", err));
		}
	}
}
```



#### getHandler（）讲解：

主要步骤：

<span style="color:red">通过HttpServletRequest得到请求中handler对应的url，遍历handlerMappings，获取url对应的handler</span>。

> 该方法主要是通过request的url得到相应handler
>
> 触发时机：容器启动的时候，扫描注解就可以建立url和handler的关系，将handler放入handlerMappings

```java
   /**
     * 取得处理当前请求的Controller，这里也称为Handler，即处理器
     * 这里并不是直接返回 Controller，而是返回 HandlerExecutionChain 请求处理链对象
     * 该对象封装了Handler和Inteceptor
     */
   @Nullable
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		if (this.handlerMappings != null) {
            //遍历handlerMappings
            //有两种handlerMappings实现类
            //1.BeanNameUrlHandlerMapping：封装xml配置的handler
            //2.RequestMappingHandlerMapping：封装注解配置的handler（一般都用这个）
			for (HandlerMapping mapping : this.handlerMappings) {
				HandlerExecutionChain handler = mapping.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

public class HandlerExecutionChain {
    
	private static final Log logger = LogFactory.getLog(HandlerExecutionChain.class);

	private final Object handler;

	@Nullable
	private HandlerInterceptor[] interceptors;

	@Nullable
	private List<HandlerInterceptor> interceptorList;

	private int interceptorIndex = -1;
}
```

#### getHandlerAdapter（）讲解:       为什么要使用不同适配器？？？

主要步骤：

<span style="color:red">不同适配器的判断方法不同，只有返回true才返回该类型适配器。</span>

> 该方法也是容器初始化的时候调用的

```java
protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		if (this.handlerAdapters != null) {
			for (HandlerAdapter adapter : this.handlerAdapters) {
                //根据getHandler（）类型，遍历判断该类型是否实现某个接口判断适配器类型
				if (adapter.supports(handler)) {
					return adapter;
				}
			}
		}
		throw new ServletException("No adapter for handler this handler");
	}
```

**HandlerAdapter实现类：**

![image-20201218000357138](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201218000357138.png)



#### handle方法执行方法讲解：

 ha.handle(processedRequest, response, mappedHandler.getHandler());

主要步骤：

1. 解析参数（不同参数使用不同解析器处理）
2. 获取执行方法（getBean（）获取）
3. 反射调用执行invoke方法，执行过程结束。

> 根据session判断执行方法

```java
RequestMappingHandlerAdapter.java
    //该方法时ha.handle执行过程中的方法，主要是判断session的线程问题，具体需进一步了解；
    //核心方法是invokeHandlerMethod(request, response, handlerMethod);下面会讲解。
    @Override
	protected ModelAndView handleInternal(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		ModelAndView mav;
		checkRequest(request);

		// 判断当前是否需要支持在同一个session中只能线性地处理请求
		if (this.synchronizeOnSession) {
			// 获取当前请求的session对象
			HttpSession session = request.getSession(false);
			if (session != null) {
				// 为当前session生成一个唯一的可以用于锁定的key
				Object mutex = WebUtils.getSessionMutex(session);
				synchronized (mutex) {
					// 对HandlerMethod进行参数等的适配处理，并调用目标handler
					mav = invokeHandlerMethod(request, response, handlerMethod);
				}
			}
			else {
				// 如果当前不存在session，则直接对HandlerMethod进行适配
				mav = invokeHandlerMethod(request, response, handlerMethod);
			}
		}
		else {
			// 如果当前不需要对session进行同步处理，则直接对HandlerMethod进行适配
			mav = invokeHandlerMethod(request, response, handlerMethod);
		}

		if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
			if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
				applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
			}
			else {
				prepareResponse(response);
			}
		}

		return mav;
	}
```

#### processDispatchResult()方法完成视图渲染跳转

主要步骤：

1. 视图解析器解析出View视图对象

2. 在解析出View视图对象的过程中会判断是否重定向、是否转发等，不同的情况封装的是不同的
   View实现

3. 解析出View视图对象的过程中，要将逻辑视图名解析为物理视图名

   ![image-20201218231903880](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201218231903880.png)

4. 渲染数据

5. 把modelMap中的数据暴露到request域中（这也是为什么后台model.add之后在jsp中可以从请求
   域取出来的根本原因）

6. 将数据设置到请求域中

![image-20201218232000910](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201218232000910.png)



**详细步骤：**

render方法完成渲染

![image-20200817151302065](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151302065.png)

视图解析器解析出View视图对象

![image-20200817151319778](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151319778.png)

在解析出View视图对象的过程中会判断是否重定向、是否转发等，不同的情况封装的是不同的
View实现

![image-20200817151332199](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151332199.png)

解析出View视图对象的过程中，要将逻辑视图名解析为物理视图名

![image-20200817151346132](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151346132.png)

封装View视图对象之后，调用了view对象的render⽅法

![image-20200817151358785](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151358785.png)

渲染数据

![image-20200817151406837](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151406837.png)

把modelMap中的数据暴露到request域中，这也是为什么后台model.add之后在jsp中可以从请求
域取出来的根本原因

![image-20200817151420769](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151420769.png)

将数据设置到请求域中

![image-20200817151429418](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200817151429418.png)





#### MVC九大组件初始化时机：

```java
DispatcherServlet.java
    //多部件解析器
	@Nullable
	private MultipartResolver multipartResolver;

    //国际化解析器
	@Nullable
	private LocaleResolver localeResolver;

    //主题解析器
	@Nullable
	private ThemeResolver themeResolver;

	//处理映射器
    @Nullable
	private List<HandlerMapping> handlerMappings;

	//处理适配器组件
    @Nullable
	private List<HandlerAdapter> handlerAdapters;

	//异常解析器组件
    @Nullable
	private List<HandlerExceptionResolver> handlerExceptionResolvers;

	//默认视图名转换器组件
    @Nullable
	private RequestToViewNameTranslator viewNameTranslator;

	//flash属性管理舰
    @Nullable
	private FlashMapManager flashMapManager;

	//视图解析器
    @Nullable
	private List<ViewResolver> viewResolvers;

//上述九大组件接口
```

#### 九大组件初始化细节：

```java
    //该方法spring源码的refresh()方法有调用过。
	protected void onRefresh(ApplicationContext context) {
		// 初始化策略
		initStrategies(context);
	}

	/**
	 * 初始化策略
	 */
	protected void initStrategies(ApplicationContext context) {
		// 多文件上传的组件
		initMultipartResolver(context);
		// 初始化本地语言环境
		initLocaleResolver(context);
		// 初始化模板处理器
		initThemeResolver(context);
		// 初始化HandlerMapping
		initHandlerMappings(context);
		// 初始化参数适配器
		initHandlerAdapters(context);
		// 初始化异常拦截器
		initHandlerExceptionResolvers(context);
		// 初始化视图预处理器
		initRequestToViewNameTranslator(context);
		// 初始化视图转换器
		initViewResolvers(context);
		// 初始化 FlashMap 管理器
		initFlashMapManager(context);
	}

//举例initHandlerMappings，其他都差不多，initMultipartResolver除外（单独讲）。
private void initHandlerMappings(ApplicationContext context) {
		this.handlerMappings = null;

		if (this.detectAllHandlerMappings) {
			// 找到所有实现HandlerMapping接口的类
			Map<String, HandlerMapping> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<>(matchingBeans.values());
				AnnotationAwareOrderComparator.sort(this.handlerMappings);
			}
		}
		else {
			try {
				// 否则在ioc中按照固定名称去找
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
			}
		}

		if (this.handlerMappings == null) {
			// 最后还为空则按照默认策略生成
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
		}
	}

private void initMultipartResolver(ApplicationContext context) {
		try {
            //只能从ioc获取，且配置文件的id一定为multipartResolver才可以。
			this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);//其他不用看，MULTIPART_RESOLVER_BEAN_NAME=“multipartResolver”，该初始化方法只能通过配置文件拿bean，所以在配置文件的id一定为multipartResolver才可以。
			if (logger.isTraceEnabled()) {
				logger.trace("Detected " + this.multipartResolver);
			}
			else if (logger.isDebugEnabled()) {
				logger.debug("Detected " + this.multipartResolver.getClass().getSimpleName());
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isTraceEnabled()) {
				logger.trace("No MultipartResolver '" + MULTIPART_RESOLVER_BEAN_NAME + "' declared");
			}
		}
	}
```

![image-20201218004901657](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20201218004901657.png)图1.1

九大组件初始化总结（除了initMultipartResolver）：
    找到实现类；
    如果没找到，在ioc中按照固定名称去找；
    还没找到，如图1.1根据properties文件默认配置去找。

initMultipartResolver初始化：

​    只能从ioc获取，且配置文件的id一定为multipartResolver才可以。



session线程不安全，session、cookie了解？？？

springmvc使用session加锁：

**实现session同步：**给session生成唯一key，对key加锁，里面执行具体逻辑。

```java
		// 判断当前是否需要支持在同一个session中只能线性地处理请求
		if (this.synchronizeOnSession) {
			// 获取当前请求的session对象
			HttpSession session = request.getSession(false);
			if (session != null) {
				// 为当前session生成一个唯一的可以用于锁定的key
				Object mutex = WebUtils.getSessionMutex(session);
				synchronized (mutex) {
					// 对HandlerMethod进行参数等的适配处理，并调用目标handler
					mav = invokeHandlerMethod(request, response, handlerMethod);
				}
			}
			else {
				// No HttpSession available -> no mutex necessary
				// 如果当前不存在session，则直接对HandlerMethod进行适配
				mav = invokeHandlerMethod(request, response, handlerMethod);
			}
		}
```

