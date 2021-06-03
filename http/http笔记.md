### HTTPS可使用的协议：

ssl协议
TLS(transport layer security)协议（比ssl协议晚出现）

### HTTPS和HTTP的主要区别：

- HTTPS协议使用时需要到电子商务认证授权机构（CA）申请SSL证书
- HTTP默认使用8080端口，HTTPS默认使用8443端口
- HTTPS则是具有SSL加密的安全性传输协议，对数据的传输进行加密，效果上相当于HTTP的升级
  版
- HTTP的连接是无状态的，不安全的；HTTPS协议是由SSL+HTTP协议构建的可进行加密传输、身
  份认证的网络协议，比HTTP协议安全。

### HTTPS 工作原理：

![image-20200904170331029](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904170331029.png)

**补充：**浏览器保存的是公钥，网站保存的是私钥，可以使用私钥解密公钥，上图7步骤握手信息包含hash值，步骤8判断hash值是不是一致。

**为什么要步骤2：**保证浏览器可以安全地获得各个网站的公钥。

https第一次进行非对称加密（如图2.1），后面进行对称加密（如图2.2）

![image-20200904172327779](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904172327779.png)

2.1

![image-20200904172926402](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20200904172926402.png)

2.2

### URI和URL的区别？

URI：URL是URI的子集，URI表达的是一个资源的唯一标识。

URL：URL是找到资源的路径。

### 常用的HTTP请求方法有哪些？

- GET： 用于请求访问已经被URI（统一资源标识符）识别的资源，可以通过URL传参给服务器
- POST：用于传输信息给服务器，主要功能与GET方法类似，但一般推荐使用POST方式。
- PUT： 传输文件，报文主体中包含文件内容，保存到对应URI位置。
- HEAD： 获得报文首部，与GET方法类似，只是不返回报文主体，一般用于验证URI是否有效。
- DELETE：删除文件，与PUT方法相反，删除对应URI位置的文件。
- OPTIONS：查询相应URI支持的HTTP方法。

### 常见的HTTP相应状态码：

- 200：请求被正常处理
- 204：请求被受理但没有资源可以返回
- 206：客户端只是请求资源的一部分，服务器只对请求的部分资源执行GET方法，相应报文中通过Content-Range指定范围的资源。
- 301：永久性重定向
- 302：临时重定向
- 303：与302状态码有相似功能，只是它希望客户端在请求一个URI的时候，能通过GET方法重定向到另一个URI上
- 304：发送附带条件的请求时，条件不满足时返回，与重定向无关
- 307：临时重定向，与302类似，只是强制要求使用POST方法
- 400：请求报文语法有误，服务器无法识别
- 401：请求需要认证
- 403：请求的对应资源禁止被访问
- 404：服务器无法找到对应资源
- 500：服务器内部错误
- 503：服务器正忙

### HTTP请求报文与响应报文格式：

HTTP 请求报文由请求行、请求头部、空行和请求正文 4 个部分组成：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210316193917941.png" alt="image-20210316193917941" style="zoom:70%;" />

HTTP响应报文主要由状态行、响应头部、空行和响应正文4个部分组成：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210316193955197.png" alt="image-20210316193955197" style="zoom:67%;" />

### HTTP优化方案

我下面就简要概括一下：

- **TCP复用：**TCP连接复用是将多个客户端的HTTP请求复用到一个服务器端TCP连接上，而HTTP复用则是一个客户端的多个HTTP请求通过一个TCP连接进行处理。前者是负载均衡设备的独特功能；而后者是HTTP 1.1协议所支持的新功能，目前被大多数浏览器所支持。
- **内容缓存：**将经常用到的内容进行缓存起来，那么客户端就可以直接在内存中获取相应的数据了。
- **压缩：**将文本数据进行压缩，减少带宽
- **SSL加速（SSL Acceleration）：**使用SSL协议对HTTP协议进行加密，在通道内加密并加速
- **TCP缓冲：**通过采用TCP缓冲技术，可以提高服务器端响应时间和处理效率，减少由于通信链路问题给服务器造成的连接负担。

### HTTP协议与TCP/IP协议的关系：

HTTP的长连接和短连接本质上是TCP长连接和短连接。HTTP属于应用层协议，在传输层使用TCP协议，在网络层使用IP协议。 **IP协议主要解决网络路由和寻址问题**，TCP协议主要解决如何在**IP层之上可靠地传递数据包**，使得**网络上接收端收到发送端所发出的所有包，并且顺序与发送顺序一致**。TCP协议是可靠的、面向连接的。

### 如何理解HTTP协议是无状态的：

HTTP协议是无状态的，指的是协议对于事务处理没有**记忆能力**，服务器不知道客户端是什么状态。也就是说，打开一个服务器上的网页和上一次打开这个服务器上的网页之间没有任何联系。HTTP是一个无状态的面向连接的协议，无状态不代表HTTP不能保持TCP连接，更不能代表HTTP使用的是UDP协议（无连接）。

> HTTPS使用cookie和session解决了无状态问题
>
> #### Cookie 和 Session 的区别？
>
> 1. Cookie 数据存放在客户的浏览器上，Session 数据放在服务器上
>
>    > ##### 为什么把登录信息放在cookie？
>    >
>    > **答：**由于如果用户信息使用session保存的话，用户信息往往会丢失而需要重新登录，使用cookies的话则可以长时间有效比较利于用户体验，那session的情况我们不讨论，下面说说使用cookies保存用户信息的情况。
>
> 2. Cookie 不是很安全，别人可以分析存放在本地的Cookie 并进行Cookie 欺骗，考虑到安全应当使用 Session 
>
>    ##### Cookie欺骗：
>
>    ​	该次登录的cookie信息使用上一个用户登录的信息，从而不用用户名和密码也可以登录。
>
>    ##### Cookie欺骗解决方案：
>
>    ​	验证cookie的登录token时，同时验证token和密码是否匹配。
>
> 3. Session 会在一定时间内保存在服务器上。当访问增多，会比较占用你服务器的性能。考虑到减轻服务器性能方面，应当使用Cookie；
>
> 4. 单个Cookie 在客户端的限制是3K，就是说一个站点在客户端存放的Cookie不能超过3K；
>
> #### localStorage和sessionStorage 介绍：
>
> - localStoragelocalStorage生命周期是永久，意味着除非用户显示在浏览器提供的UI上清除localStorage信息，否则这些信息将永远存在。存放数据大小为一般为5MB,而且它仅在客户端（即浏览器）中保存，不参与和服务器的通信
> - sessionStorage 仅在当前会话下有效，关闭页面或浏览器后被清除。存放数据大小为一般为5MB,而且它仅在客户端（即浏览器）中保存，不参与和服务器的通信。
>   **优点：**
>   （1）增大了传统cookie的容量限制
>   （2）不参与和服务器的通信
>   **缺点：**
>   （1）浏览器的大小不统一，并且在IE8以上的IE版本才支持localStorage这个属性
>   （2）目前所有的浏览器中都把localStorage的值类型限定为String类型，若在存储即要将对象转化为字符串
>   （3）localStorage在浏览器的隐私模式下面是不可读取的
>   （4）localStorage数据不能被爬虫抓取到并且存储内容多的话会消耗内存空间，会导致页面变卡
>   （5）localStorage存储没有时间限制即永不过期，需手动清理缓存信息

### 什么是长连接、短连接？

- ##### 短连接：

  HTTP/1.0中默认使用短连接，客户端和服务器每进行一次HTTP操作，就建立一次连接，任务结束就中断连接。

- ##### 长连接：

  HTTP/1.1起，在使用长连接的情况下，当一个网页打开完成后，客户端和服务器之间用于传输HTTP数据的TCP连接不会关闭，客户端再次访问这个服务器时，会继续使用这一条已经建立的连接。

  ```java
  Connection:keep-alive //在响应头加入这行代码就是使用长连接的HTTP协议
  ```

### TCP为什么要三次握手？

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210316204633549.png" alt="image-20210316204633549" style="zoom:67%;" />

如上图所示，三次握手流程：

1. 客户端发送 SYN，序列号seq=x，此时客户端就变成了 SYN-SENT 状态。

2. 服务端发送SYN、ACK，序列号seq=y，ack=x+1，此时服务器端就变成了 SYN-REVD 状态。

3. 客户端ACK，序列号seq=x+1，ack=y+1（后续根据这个序列号通信），服务端和客户端就变成了 ESTABLISHED（已确认）状态。

   > <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210319005019548.png" alt="image-20210319005019548" style="zoom:70%;" />

- #### 为什么 TCP 需要三次握手？

  - ##### 原因一：信息对等：

    <table>
    	<tr>
            <td rowspan="2">第N次握手</td>
  		<td colspan="4">A机器确认</td>
    		<td colspan="4">B机器确认</td>
        </tr>
    	<tr>
    		<td >自己发报能力</td>
    		<td >自己收报能力</td>
    		<td >对方发报能力</td>
    		<td >对方收报能力</td>
    		<td >自己发报能力</td>
    		<td >自己收报能力</td>
    		<td >对方发报能力</td>
    		<td >对方收报能力</td>
        </tr>
        <tr>
        	<td>第一次握手后</td>
            <td style="color:red">No</td>
            <td style="color:red">No</td>
            <td style="color:red">No</td>
            <td style="color:red">No</td>
            <td style="color:red">No</td>
            <td>Yes</td>
            <td>Yes</td>
            <td style="color:red">No</td>
        </tr>
         <tr>
        	<td>第二次握手后</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
            <td style="color:red">No</td>
            <td>Yes</td>
            <td>Yes</td>
            <td style="color:red">No</td>
        </tr>
         <tr>
        	<td>第三次握手后</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
            <td>Yes</td>
        </tr>
    </table>
  
    如上表所示，需要三次握手才能确认自己的发报以及对方的收报能力是正常的。
  
  - ##### 原因二：防止超时：
  
    连续三次握手也是为了防止超时导致的脏连接：
  
    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210319004206832.png" alt="image-20210319004206832" style="zoom:50%;" />
  
    如上图，第一的请求超时的又可以后续会建立连接，从而产生脏连接。
  
    三次握手就可以解决这个问题，因为需要 A 服务器确认（也就是最后一步）了才真正的建立了连接。

### TCP三次握手中第三次丢失了怎么办？怎么处理：

​	如果第三次的ACK包丢失了，那么服务端重复第二个步骤等待3秒、6秒、12秒后重新发送SYN+ACK包，以便Client重新发送ACK包，以便Client重新发送ACK包。（Server重发SYN+ACK包的次数，可以通过设置/proc/sys/net/ipv4/tcp_synack_retries修改，默认值为5。）

​	如果重发指定次数后，仍然未收到ACK应答，那么一段时间后，Server自动关闭这个连接。

​     但是**Client认为这个连接已经建立**，如果Client端向Server写数据，Server端将以RST包响应，方能感知到Server的错误。

#### 说一下从url输入到返回请求的过程：

- 首先会进行 url 解析，根据 dns 系统进行 ip 查找

  - 为什么url要解析？

    因为怕出现歧义，比如name1=value1，其中value1的值为“va&lu=e1”，那么实际在传输过程中就会变成这样“name1=va&lu=e1”，使用编码的话在特殊字符的各个字节前加上%，于是变为name1=va%26lu%3De1，这样就不会有歧义。

    ##### URL特殊符号及编码：

    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210330205650574.png" alt="image-20210330205650574" style="zoom:50%;" />

    ##### url编码的规则是什么？

    答：utf-8，中文是使用gb2312。

    ##### 浏览器不是统一用utf-8，怎么保证都是utf-8的编码？

    1. 更改浏览器编码：

       <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210331085703350.png" alt="image-20210331085703350" style="zoom:70%;" />

    2. html的元标签meta设置编码：

       <META HTTP-EQUIV="Content-type" CONTENT="text/html; charset=UTF-8">

  - dns查询规则是什么？

    比如浏览器输入www.baidu.com 域名。

    1. 操作系统会先查hosts件是否有记录，有的话就会把相对应映射的IP返回。

    2. hosts文件没有就去查本地dns解析器有没有缓存，有就返回。

    3. 没有就去其他的dns服务器查找。

    4. 还没有的话就去找根DNS服务器(全球13台，固定ip地址)，然后判断.com域名是哪个服务器管理，如果无法解析，就查找.baidu.com服务器是否能解析，直到查到www.baidu.com的IP地址。

       > dns转发分为2种，全局转发和特定区域转发
       >
       > 全局转发是对非本机所负责解析区域的请求，全部转发给指定的服务器
       >
       > 特定区域转发是仅转发对特定的区域的请求，比全局转发优先级高
       > 而转发又分为2种模式：first和only
       >
       > - first模式：先到本地DNS查找，若本地dns查找不到记录，去其他的dns服务器查找，若其他dns服务器也没有，直接去根服务器查找
       > - only模式：先到本地DNS查找，若本地dns查找不到记录，去其他的dns服务器查找，若其他dns服务器也没有，直接放弃。

  - 前端的dns优化，可以在html页面头部写入dns缓存地址，比如

    ```html
    <meta http-equiv="x-dns-prefetch-control" content="on" />
    <link rel="dns-prefetch" href="http://bdimg.share.baidu.com" />
    ```

- 查找到IP之后，就是http协议的三次握手

  ##### 三次握手的状态变化？

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210316204633549.png" alt="image-20210316204633549" style="zoom:67%;" />

  如上图所示，三次握手流程：

  1. 客户端发送 SYN，序列号seq=x，此时客户端就变成了 SYN-SENT 状态。
  2. 服务端发送SYN、ACK，序列号seq=y，ack=x+1，此时服务器端就变成了 SYN-REVD 状态。
  3. 客户端ACK，序列号seq=x+1，ack=y+1（后续根据这个序列号通信），服务端和客户端就变成了 ESTABLISHED（已确认）状态。

  ##### 两次握手为什么不行？

  参考上方。

  > ##### 扩展：
  >
  > 从网卡把数据包传输出去到服务器发生了什么？

- 3次握手之后，后续流程是什么？

  3次握手后，建立完连接，请求html文件，如果html文件在缓存里面浏览器直接返回，如果没有，就去后台拿。

  ##### 浏览器缓存步骤：

  - 浏览器首次加载资源成功时，服务器返回200，此时浏览器不仅将资源下载下来，而且把response的header(里面的date属性非常重要，用来计算第二次相同资源时当前时间和date的时间差)一并缓存;
  - 下一次加载资源时，首先要经过强缓存的处理，cache-control的优先级最高，比如cache-control：no-cache,就直接进入到协商缓存的步骤了，如果cache-control：max-age=xxx,就会先比较当前时间和上一次返回200时的时间差，如果没有超过max-age，命中强缓存，不发请求直接从本地缓存读取该文件（这里需要注意，如果没有cache-control，会取expires的值，来对比是否过期），过期的话会进入下一个阶段，协商缓存
  - 协商缓存阶段，则向服务器发送header带有If-None-Match和If-Modified-Since的请求，服务器会比较Etag，如果相同，命中协商缓存，返回304；如果不一致则有改动，直接返回新的资源文件带上新的Etag值并返回200;
  - 协商缓存第二个重要的字段是，If-Modified-Since，如果客户端发送的If-Modified-Since的值跟服务器端获取的文件最近改动的时间，一致则命中协商缓存，返回304；不一致则返回新的last-modified和文件并返回200;

  ##### 什么是from disk cache和from memory cache吗，什么时候会触发？

  ##### 什么是启发式缓存？

  如果响应中未显示Expires，Cache-Control：max-age或Cache-Control：s-maxage，并且响应中不包含其他有关缓存的限制，缓存可以使用启发式方法计算新鲜度寿命。通常会根据响应头中的2个时间字段 Date 减去 Last-Modified 值的 10% 作为缓存时间。

  ```java
  // Date 减去 Last-Modified 值的 10% 作为缓存时间。
  // Date：创建报文的日期时间, Last-Modified 服务器声明文档最后被修改时间  response_is_fresh =  max(0,（Date -  Last-Modified)) % 10
  ```

  **接着回答，我说返回html之后，会解析html,这部分知识我提前准备过，但是答的不是很详细，大概意思就是cssom + domTree = html,然后布局和绘制**

  - 构建DOM树(DOM tree)：从上到下解析HTML文档生成DOM节点树（DOM tree），也叫内容树（content tree）；
  - 构建CSSOM(CSS Object Model)树：加载解析样式生成CSSOM树；
  - 执行JavaScript：加载并执行JavaScript代码（包括内联代码或外联JavaScript文件）；
  - 构建渲染树(render tree)：根据DOM树和CSSOM树,生成渲染树(render tree)；
  - 渲染树：按顺序展示在屏幕上的一系列矩形，这些矩形带有字体，颜色和尺寸等视觉属性。
  - 布局（layout）：根据渲染树将节点树的每一个节点布局在屏幕上的正确位置；
  - 绘制（painting）：遍历渲染树绘制所有节点，为每一个节点适用对应的样式，这一过程是通过UI后端模块完成；

  ##### 页面渲染优化：

  - HTML文档结构层次尽量少，最好不深于六层；
  - 脚本尽量后放，放在前即可；
  - 少量首屏样式内联放在标签内；
  - 样式结构层次尽量简单；
  - 在脚本中尽量减少DOM操作，尽量缓存访问DOM的样式信息，避免过度触发回流；
  - 减少通过JavaScript代码修改元素样式，尽量使用修改class名方式操作样式或动画；
  - 动画尽量使用在绝对定位或固定定位的元素上；
  - 隐藏在屏幕外，或在页面滚动时，尽量停止动画；
  - 尽量缓存DOM查找，查找器尽量简洁；
  - 涉及多域名的网站，可以开启域名预解析

### HTTP1.1版本新特性：

### UDP：

#### 四次挥手：

#### nginx是如何实现高并发的？

   一个主进程，多个工作进程，每个工作进程可以处理多个请求，每进来一个request，会有一个worker进程去处理。
   但不是全程的处理，处理到可能发生阻塞的地方，比如向上游（后端）服务器转发request，并等待请求返回。那么，
   这个处理的worker继续处理其他请求，而一旦上游服务器返回了，就会触发这个事件，worker才会来接手，这个request才会接着往下走。
   由于web server的工作性质决定了每个request的大部份生命都是在网络传输中，实际上花费在server机器上的时间片不多。
   这是几个进程就解决高并发的秘密所在。即@skoo所说的webserver刚好属于网络io密集型应用，不算是计算密集型。