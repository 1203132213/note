[TOC]

### 面对对象的特征：

面向对象的特征主要有以下几个方面：

1. **抽象：**抽象只关注对象有哪些**属性和行为**，并不关注这些行为的细节是什么。

2. **继承：**继承父类的方法，增加代码复用。

3. **封装：**细节使用接口封装起来，对外使用接口访问。

4. **多态：**多态是同一个行为具有多个不同表现形式或形态的能力

   > #### 多态的实现方式：
   >
   > 1. 重写：
   >
   >    外壳不变，核心重写
   >
   > 2. 接口：
   >
   >    生活中的接口最具代表性的就是插座，例如一个三接头的插头都能接在三孔插座中，因为这个是每个国家都有各自规定的接口规则，有可能到国外就不行，那是因为国外自己定义的接口类型。
   >
   > 3. 抽象类和抽象方法
   >
   > #### Java实现多态有三个必要条件：
   >
   > 继承、重写、向上转型
   >
   > -  继承：在多态中必须存在有继承关系的子类和父类。
   > - 重写：子类对父类中某些方法进行重新定义，在调用这些方法时就会调用子类的方法。
   > - 向上转型：在多态中需要将子类的引用赋给父类对象，只有这样该引用才能够具备技能调用父类的方法和子类的方法。

### 重写(Override)与重载(Overload)的区别？

| 区别点   | 重写方法                                       | 重载方法 |
| :------- | :--------------------------------------------- | -------- |
| 参数列表 | 一定不能修改                                   | 必须修改 |
| 返回类型 | 一定不能修改                                   | 可以修改 |
| 异常     | 可以减少或删除，一定不能抛出新的或者更广的异常 | 可以修改 |
| 访问     | 一定不能做更严格的限制（可以降低限制）         | 可以修改 |

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210317205253738.png" alt="image-20210317205253738" style="zoom:80%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210317205313835.png" alt="image-20210317205313835" style="zoom:80%;" />

- #### 为什么不能根据返回类型来区分重载？

  如果可以根据返回值类型来区方法重载，那在仅仅调用方法**不获取返回值的使用场景**，JVM 就不知道调用的是哪个返回值的方法了。

### 8大基本类型：

​	byte（8位）、short（16位）、char（16位）、int（32位）、float（32位）、long（64位）、double（64位）、boolean

- #### 基本类型和包装类型的区别？

  1. ##### 包装类型可以为 null，而基本类型不可以。

  2. ##### 包装类型可用于泛型，而基本类型不可以。

  3. ##### 基本类型比包装类型更高效：

     基本类型在栈中直接存储的具体数值，而包装类型则存储的是堆中的引用。包装类型需要占用更多的内存空间，每次都要通过 new 一个包装类型就显得非常笨重。

  4. ##### 自动装箱和自动拆箱：

     把基本类型转换成包装类型的过程叫做装箱。反之，把包装类型转换成基本类型的过程叫做拆箱。

     在 Java SE5 之前，开发人员要手动进行装拆箱

     ```java
     Integer chenmo = new Integer(10);  // 手动装箱
     int wanger = chenmo.intValue();  // 手动拆箱
     12
     ```

     Java SE5 为了减少开发人员的工作，提供了自动装箱与自动拆箱的功能

     ```java
     Integer chenmo  = 10;  // 自动装箱
     int wanger = chenmo;     // 自动拆箱
     ```

     当需要进行自动装箱时，如果数字在 -128 至 127 之间时，会直接使用缓存中的对象，而不是重新创建一个对象。
  
- #### 数据类型的转换：

  ​	分为自动转换和强制转换，自动转换是程序在执行过程中“悄然”进行的转换，不需要用户提前声明，一般是从位数低的类型向位数高的类型转换；强制类型转换则必须在代码中声明，强制转换有的精度会丢失，而有的会更加精确，转换顺序不受限制。

  ​	自动转换按从低到高的顺序转换。不同类型数据间的优先关系如下：
  ​		 低--------------------------------------------->高
    		byte-> short-> char-> int -> long -> float -> double

  ​	强制转换案例：

  ```java
  public class Demo {
      public static void main(String[] args){
          int x;
          double y;
          x = (int)34.56 + (int)11.2;  // 丢失精度
          y = (double)x + (double)10 + 1;  // 提高精度
          System.out.println("x=" + x);
          System.out.println("y=" + y);
      }
  }
  运行结果：
  x=45
  y=56.0
  ```

### String讲解：

- #### String为什么不是基本类型？

  java 中String 是个对象，是引用类型。**基础类型**与**引用类型**的区别是：

  基础类型只**表示简单的字符或数字**，引用类型可以是**任何复杂的数据结构。**
  
  java虚拟机处理**基础类型**与**引用类型**的**方式是不一样的**，对于基本类型，java虚拟机会为其分配数据类型实际占用的内存空间，对于**引用类型变量，他仅仅是一个指向堆区中某个实例的指针。**
  
  ### String有没有长度限制？
  
  - #### 当String为常量时：
  
    String的构造函数指定的长度是可以支持2147483647(2^31 - 1)的，但是字符串常量池对字符串的长度做了限制字符串在class格式文件中的存储格式为：
    
  
```xml
    CONSTANT_Utf8_info {
        u1 tag;
        u2 length;
        u1 bytes[length];
    }
```

u2是无符号的16位整数(2个字节)，最大值为2^16-1=65535
    
### 当String为变量时：

为变量时，则长度限制为231-1= 2147483647个字符。当然塞不塞得下得看你内存了。

### String的源码：

以主流的 JDK 版本 1.8 来说，String 内部**实际存储结构为 char 数组**，源码如下：

```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    // 用于存储字符串的值
    private final char value[];
    // 缓存字符串的 hash code
    private int hash; // Default to 0
    // ......其他内容
}
```

- ##### String 类型重写了 Object 中的 equals() 方法，比较两个字符串是否相等：

  ```java
  public boolean equals(Object anObject) {
      // 对象引用相同直接返回 true
      if (this == anObject) {
          return true;
      }
      // 判断需要对比的值是否为 String 类型，如果不是则直接返回 false
      if (anObject instanceof String) {
          String anotherString = (String)anObject;
          int n = value.length;
          if (n == anotherString.value.length) {
              // 把两个字符串都转换为 char 数组对比
              char v1[] = value;
              char v2[] = anotherString.value;
              int i = 0;
              // 循环比对两个字符串的每一个字符
              while (n-- != 0) {
                  // 如果其中有一个字符不相等就 true false，否则继续对比
                  if (v1[i] != v2[i])
                      return false;
                  i++;
              }
              return true;
          }
      }
      return false;
  }
  ```

  如上代码，先判断是否是String类型，然后循环比对两个String内部的char 数组是否都相等，如果其中有一个字符不相等就返回false，否则继续对比。

- ##### compareTo() 比较两个字符串

  compareTo() 方法用于比较两个字符串，返回的结果为 int 类型的值，源码如下

  ```java
  public int compareTo(String anotherString) {
      int len1 = value.length;
      int len2 = anotherString.value.length;
      // 获取到两个字符串长度最短的那个 int 值
      int lim = Math.min(len1, len2);
      char v1[] = value;
      char v2[] = anotherString.value;
      int k = 0;
      // 对比每一个字符
      while (k < lim) {
          char c1 = v1[k];
          char c2 = v2[k];
          if (c1 != c2) {
              // 有字符不相等就返回差值
              return c1 - c2;
          }
          k++;
      }
      return len1 - len2;
  }
  ```

  compareTo() 方法和 equals() 方法都是用于比较两个字符串的，但它们有两点不同：

  - equals() 可以接收一个 Object 类型的参数，而 compareTo() 只能接收一个 String 类型的参数；
  - equals() 返回值为 Boolean，而 compareTo() 的返回值则为 int。

  它们都可以用于两个字符串的比较，当 equals() 方法返回 true 时，或者是 compareTo() 方法返回 0 时，则表示两个字符串完全相同。

- ##### String其他重要方法：

  - indexOf()：查询字符串首次出现的下标位置
  - lastIndexOf()：查询字符串最后出现的下标位置
  - contains()：查询字符串中是否包含另一个字符串
  - toLowerCase()：把字符串全部转换成小写
  - toUpperCase()：把字符串全部转换成大写
  - length()：查询字符串的长度
  - trim()：去掉字符串首尾空格
  - replace()：替换字符串中的某些字符
  - split()：把字符串分割并返回字符串数组
  - join()：把字符串数组转为字符串

- ### String使用 final 修饰的好处：

  从 String 类的源码我们可以看出 String 是被 final 修饰的不可继承类，源码如下：

  ```java
  public final class String 
  	implements java.io.Serializable, Comparable<String>, CharSequence { //...... }
  ```

  ##### 那这样设计有什么好处呢？

  **答：**使用 final 修饰的第一个好处是**安全**；第二个好处是**高效**：

  - ##### 安全：

    当你在调用其他方法时，比如调用一些系统级操作指令之前，可能会有一系列校验，如果是可变类的话，

    可能在你校验过后，它的内部的值又被改变了，这样有可能会引起严重的系统崩溃问题。

  - ##### 高效：

    能够缓存结果，当你在传参时不需要考虑谁会修改它的值。

  只有字符串是不可变时，我们才能实现字符串常量池，字符串常量池可以为我们缓存字符串，提高程序的运行效率，如下图所示：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210311202550190.png" alt="image-20210311202550190" style="zoom:80%;" />

  试想一下如果 String 是可变的，那当 s1 的值修改之后，s2 的值也跟着改变了，这样就和我们预期的结果不相符了，因此也就没有办法实现字符串常量池的功能了。

- ### String 和 StringBuilder、StringBuffer 的区别

  > 因为 String 类型是不可变的，所以在字符串拼接的时候如果使用 String 的话需要新建对象，性能会很低，因此我们就需要使用另一个数据类型 StringBuffer、StringBuilder，它提供了 append 和 insert 方法可用于字符串的拼接

  简单说就是String不可变， StringBuilder、StringBuffer 可变。StringBuffer和StringBuilder唯一区别是 StringBuffer在append 和 insert方法使用 synchronized 来修饰，因此线程安全。

  ##### StringBuffer的append 源码：

  ```java
  @Override
  public synchronized StringBuffer append(Object obj) {
      toStringCache = null;
      super.append(String.valueOf(obj));
      return this;
  }
  @Override
  public synchronized StringBuffer append(String str) {
      toStringCache = null;
      super.append(str);
      return this;
  }
  ```

  因为它使用了 synchronized 来保证线程安全，所以性能不是很高，于是在 JDK 1.5 就有了 StringBuilder，它同样提供了 append 和 insert 的拼接方法，但它没有使用 synchronized 来修饰，因此在性能上要优于 StringBuffer，所以在非并发操作的环境下可使用 StringBuilder 来进行字符串拼接。

- ###  String 和 JVM

  String 常见的创建方式有两种，new String() 的方式和直接赋值的方式，直接赋值的方式会先去字符串常量池中查找是否已经有此值，如果有则把引用地址直接指向此值，否则会先在常量池中创建，然后再把引用指向此值；而 new String() 的方式一定会先在堆上创建一个字符串对象，然后再去常量池中查询此字符串的值是否已经存在，如果不存在会先在常量池中创建此字符串，然后把引用的值指向此字符串，如下代码所示：

  ```java
  String s1 = new String("Java");
  String s2 = s1.intern();
  String s3 = "Java";
  System.out.println(s1 == s2); // false
  System.out.println(s2 == s3); // true
  ```

  > ##### intern（）介绍：
>
  > 在当前类的常量池中查找是否存在与str等值的String，若存在则直接返回常量池中相应Strnig的引用；若不存在，则会在常量池中创建一个等值的String，然后返回这个String在常量池中的引用

  它们在 JVM 存储的位置，如下图所示：

  <img src="https://s0.lgstatic.com/i/image3/M01/0D/BE/Ciqah16RQbaAZ3QkAACUHPvF6fE928.png" alt="img" style="zoom:80%;" />

  代码 "Ja"+"va" 被直接编译成了 "Java" ，因此 s1==s2 的结果才是 true，这就是编译器对字符串优化的结果。

- #### String s=new String(“xyz”);创建了几个字符串对象？

  两个，第一个对象是字符串常量"xyz" 第二个对象是new String()的时候产生的，在堆中分配内存给这个对象，只不过这个对象的内容是指向字符串常量"xyz" 另外还有一个引用s，指向第二个对象。这是一个变量，在栈中分配内存。 

  > #### 变形①：String s = "xyz"创建了几个String对象？
  >
  > 首先看常量池里有没有"xyz"，如果有直接引用，如果没有则创建再引用，这里"xyz"本身就是pool中的一个对象，而在运行时执行new String()时，将pool中的对象复制一份放到heap中，并且把heap中的这个对象的引用交给s持有。ok，这条语句就创建了2个String对象。
  >
  > #### 变形②：String str = “aaa” + new String(“bbb”)创建了几个String对象？
  >
  > 四个， "aa"一个对象 new Sring()一个对象 "bbb"一个对象 “aa” + new String(“bbb”);一个对象

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210322203015386.png" alt="image-20210322203015386" style="zoom:50%;" />

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210322203051552.png" alt="image-20210322203051552" style="zoom:50%;" />

###  == 和 equals 的区别？

== 对于基本数据类型来说，是用于比较 “值”是否相等的；而对于引用类型来说，是用于比较引用地址是否相同的。

> #### Java基本数据类型和引用类型的区别？
>
> ```dart
> int num = 20;
> String str = "java";
> ```
>
> - 基本类型：值就直接保存在变量中
>
> - 引用类型：变量中保存的只是实际对象的地址。一般称这种变量为"引用"，引用指向实际对象，实际对象中保存着内容
>
>   ##### 基本数据类型和引用类型变更值时的过程图：
>
>   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210311200248577.png" alt="image-20210311200248577" style="zoom:70%;" />
>
>   如上图所示，对于基本类型 num ，赋值运算符会直接改变变量的值，原来的值被覆盖掉。对于引用类型 str，赋值运算符会改变引用中所保存的地址，原来的地址被覆盖掉。**但是原来的对象不会被改变（重要）。**如上图所示，"hello" 字符串对象没有被改变。（没有被任何引用所指向的对象是垃圾，会被垃圾回收器回收）。

- #### 总结：

  - ##### 基本类型比较：

    - 使用双等号 == 比较的是值是否相等。
    - 基本数据类型无equals方法（没有意义）。

  - ##### 引用类型比较：

    - 重写了equals方法，比如String【String和Integer重写了equals()】：

      使用==比较的是String的引用是否指向了同一块内存。

      使用equals比较的是String的引用的对象是否相等。

      ```java
  String s1 = new String("java");
      String s2 = new String("java");
       
      System.out.println(s1==s2);            //false
      System.out.println(s1.equals(s2));    //true
      ```
    
    - 没有重写equals方法，比如User等自定义类。
    
      ==和equals比较的都是引用是否指向了同一块内存。
    
    ##### equals和hashCode的关系？
    
    **答：**equals()为true，那么hashCode一定相同，但是hashCode相同，equals不一定相同。
    
    > ##### 原因：
    >
    > hashCode方法就是根据一定的规则将与对象相关的信息（比如对象的存储地址，对象的字段等）映射成一个数值
    >
    > 由上面可知，对象的equals存储地址和对象都相同，所以hashCode一定会相等。
    >
    > hashCode相等，equals不一定相同，有可能hashCode恰好碰到相等的。

### HashMap死循环分析：

JDK1.7之前采用的是头插法，多线程环境下会出现链表的循环引用，于是JDK1.8采用尾插法，问题才得到改善。

那么此时线程 t1 中的 e 指向了 key(3)，而 next 指向了 key(7) ；之后线程 t2 重新 rehash 之后链表的顺序被反转，链表的位置变成了 key(5) → key(7) → key(3)，其中 “→” 用来表示下一个元素。

当 t1 重新获得执行权之后，先执行 newTalbe[i] = e 把 key(3) 的 next 设置为 key(7)，而下次循环时查询到 key(7) 的 next 元素为 key(3)，于是就形成了 key(3) 和 key(7) 的循环引用，因此就导致了死循环的发生，如下图所示：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210315194237250.png" alt="image-20210315194237250" style="zoom:50%;" />

> 有人曾经把这个问题反馈给了 Sun 公司，但 Sun 公司认为这不是一个问题，因为 HashMap 本身就是非线程安全的，如果要在多线程下，建议使用 ConcurrentHashMap 替代，但这个问题在面试中被问到的几率依然很大，所以在这里需要特别说明一下。

### 深克隆和浅克隆：

- #### 它们的区别？

  **答：**深克隆是将值和引用对象的地址+引用对象的值克隆过去，浅克隆是将值和引用对象的地址克隆过去。

  ##### 浅克隆：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210315201748720.png" alt="image-20210315201748720" style="zoom:50%;" />

  #####  深克隆：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210315201820032.png" alt="image-20210315201820032" style="zoom:50%;" />

- #### 在 java.lang.Object 中对 clone() 方法的约定有哪些？

  - 对于所有对象来说，x.clone() !=x 应当返回 true，因为克隆对象与原对象**不是同一个对象**；

  - 对于所有对象来说，x.clone().getClass() == x.getClass() 应当返回 true，因为克隆对象与原对象的**类型是一样的**；

  - 对于所有对象来说，x.clone().equals(x) 应当返回 true，因为使用 equals 比较时，它们的**值都是相同的**。

    **总结：**也就是说可用对象和原对象不是一个对象，但是类型和值是一样的。

- #### Arrays.copyOf() 是深克隆还是浅克隆？

  浅克隆：

  ```java
  People[] o1 = {new People(1, "Java")};
  People[] o2 = Arrays.copyOf(o1, o1.length);
  // 修改原型对象的第一个元素的值
  o1[0].setName("Jdk");
  System.out.println("o1:" + o1[0].getName());
  System.out.println("o2:" + o2[0].getName());
  ```

  以上程序的执行结果为：

  ```xml
  o1:Jdk
  o2:Jdk
  ```

  从结果可以看出，我们在修改克隆对象的第一个元素之后，原型对象的第一个元素也跟着被修改了，这说明 Arrays.copyOf() 其实是一个浅克隆。

  因为数组比较特殊数组本身就是引用类型，因此在使用 Arrays.copyOf() 其实只是把引用地址复制了一份给克隆对象，如果修改了它的引用对象，那么指向它的（引用地址）所有对象都会发生改变，因此看到的结果是，修改了克隆对象的第一个元素，原型对象也跟着被修改了。

- #### 深克隆的实现方式有几种？

  深克隆的实现方式有很多种，大体可以分为以下几类：

  - 所有对象都实现克隆方法；
  - 通过构造方法实现深克隆；
  - 使用 JDK 自带的字节流实现深克隆；
  - 使用第三方工具实现深克隆，比如 Apache Commons Lang；
  - 使用 JSON 工具类实现深克隆，比如 Gson、FastJSON 等。

- #### Java 中的克隆为什么要设计成，既要实现空接口 Cloneable，还要重写 Object 的 clone() 方法？

  #### 实现clone的方式：

  - 在类上新增标识，此标识用于声明某个类拥有克隆的功能，像 final 关键字一样；
  - 使用 Java 中的注解；
  - 实现某个接口；
  - 继承某个类。

  因为Cloneable 接口诞生的比较早，JDK 1.0 就已经存在了，先说第一个，为了一个重要但不常用的克隆功能， 单独新增一个类标识，这显然不合适；再说第二个，因为克隆功能出现的比较早，那时候还没有注解功能，因此也不能使用；第三点基本满足我们的需求，第四点和第一点比较类似，为了一个克隆功能需要牺牲一个基类，并且 Java 只能单继承，因此这个方案也不合适。采用排除法，无疑使用实现接口的方式是那时最合理的方案了，而且在 Java 语言中一个类可以实现多个接口。

  那为什么要在 Object 中添加一个 clone() 方法呢？

  因为 clone() 方法语义的特殊性，因此最好能有 JVM 的直接支持，既然要 JVM 直接支持，就要找一个 API 来把这个方法暴露出来才行，最直接的做法就是把它放入到一个所有类的基类 Object 中，这样所有类就可以很方便地调用到了。

### Native方法：

JDK开放给用户的源码中随处可见Native方法，被Native关键字声明的方法说明该方法不是以Java语言实现的，而是以本地语言实现的，Java可以直接拿来用。这里有一个概念，就是本地语言，本地语言这四个字，个人理解应该就是**可以和操作系统直接交互的语言**。

- #### Native用法

  1.编写带有native声明的方法的Java类（java文件）
  2.使用javac命令编译编写的Java类（class文件）如：javac NativeTest.java
  3.使用javah -jni ****来生成后缀名为.h的头文件（.h的文件） 如：javah -jni NativeTest
  4.使用其他语言（C、C++）实现本地方法
  5.将本地方法编写的文件生成动态链接库（dll文件）

  > 注意：javac NativeTest.java 没有带包名，因为我的NativeTest.java不在任何包（package）中。

  示例如下：

  ##### 1.编写带有native声明的方法的Java类（java文件）

  ```java
  public class NativeTest {
   public native void hello(String name);
   static{
    System.loadLibrary("wittdong");//wittdong和生成动态链接库的 wittdong.dll名字一致
   }
   public static void main(String[] args){
    new NativeTest().hello("jni");
   }
  }
  ```

  ##### 2.使用javac命令编译编写的Java类（class文件）如：javac NativeTest.java

  ##### 3.使用javah -jni ****来生成后缀名为.h的头文件（.h的文件） 如：javah -jni NativeTest

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210315203823362.png" alt="image-20210315203823362" style="zoom:75%;" />

  打开 javah 编译出 后缀名为 .h 的文件图：

  ##### 4、用C语言实现本地方法（hello），生成 NativeTestImpl.c 格式文件

  ```c
  #include <jni.h>
  #include “NativeTest.h”
  #include <stdio.h>
  JNIEXPORT void JNICALL Java_NativeTest_hello(JNIEnv *env,jobject obj, jstring name){
  printf(“hello world”);
  }
  ```

  ##### 5、生成动态链接库

  一种方式：cl -I %java_home%\include -I%java_home%\include\win32 -LD NativeTestImpl.c -Fe wittdong.dll

  另一种方式：用VC++6.0编译一下在debug文件夹中就生成好了dll文件。

  将dll放到生成.h的那一级文件夹中，就可以进行native本地方法调用。在Eclipse执行时，需把dll文件拷贝
  
  到C:\Windows\System32

### static关键字：

static方法不用创建对象就可以调用，通过类名就可以去进行访问。

static修饰的方法一般称作静态方法，由于静态方法**不依赖于任何对象就可以进行访问**，因此对于静态方法来说，是没有this的，因为它不依附于任何对象，既然都没有对象，就谈不上this了。并且由于这个特性，在**静态方法中不能访问类的非静态成员变量和非静态成员方法**，因为非静态成员方法/变量都必须依赖具体的对象才能够被调用。

static修饰的变量和方法，在**程序编译的时候就存在了**，而普通的变量和方法，只有在编译完程序执行的时候才被初始化。一句话，static与程序同生同死。

- #### 静态变量和非静态变量的区别？

  **答：**静态变量被所有的对象所共享，在内存中只有一个副本。非静态变量存在多个副本，各个对象拥有的副本互不影响。

- #### static关键字会改变类中成员的访问权限吗？

  **答：**不会，在Java中能够影响到访问权限的只有private、public、protected（包括包访问权限）这几个关键字。

- #### static能作用于局部变量么？

  **答：**不能，这是java规定。

- #### 能通过this访问静态成员变量吗？

  **答：**可以，静态成员变量虽然独立于对象，但是不代表不可以**通过对象去访问**，所有的静态方法和静态变量都可以通过对象访问（只要访问权限足够）。

- #### static四种用法？

  1. 用来修饰成员变量，将其变为类的成员，从而实现所有对象对于该成员的共享；
  2. 用来修饰成员方法，将其变为类方法，可以直接使用“类名.方法名”的方式调用，常用于工具类；
  3. 静态块用法，将多个类成员放在一起初始化，使得程序更加规整，其中理解对象的初始化过程非常关键；
  4. 静态导包用法，将类的方法直接导入到当前类中，从而直接使用“方法名”即可调用类方法，更加方便。

  > #### static面试题：
  >
  > 1. 下面这段代码的输出结果是什么？
  >
  >    
  >
  >    ```java
  >    public class Test extends Base{
  >        static{
  >            System.out.println("test static");
  >        }
  >        public Test(){
  >            System.out.println("test constructor");
  >        }
  >        public static void main(String[] args) {
  >            new Test();
  >        }
  >    }
  >     
  >    class Base{
  >        static{
  >            System.out.println("base static");
  >        }   
  >        public Base(){
  >            System.out.println("base constructor");
  >        }
  >    }
  >    ```
  >
  >    输出结果：
  >
  >    ```java
  >    base static
  >    test static
  >    base constructor
  >    test constructor
  >    ```
  >
  >    **结论：**先执行父类的方法再执行子类的方法。构造器也是先调用父类的构造器，然后再调用自身的构造器。
  >
  > 2. 这段代码的输出结果是什么？
  >
  >    ```java
  >    public class Test {
  >        Person person = new Person("Test");
  >        static{
  >            System.out.println("test static");
  >        }
  >         
  >        public Test() {
  >            System.out.println("test constructor");
  >        }
  >         
  >        public static void main(String[] args) {
  >            new MyClass();
  >        }
  >    }
  >     
  >    class Person{
  >        static{
  >            System.out.println("person static");
  >        }
  >        public Person(String str) {
  >            System.out.println("person "+str);
  >        }
  >    }
  >     
  >    class MyClass extends Test {
  >        Person person = new Person("MyClass");
  >        static{
  >            System.out.println("myclass static");
  >        }
  >        public MyClass() {
  >            System.out.println("myclass constructor");
  >        }
  >    }
  >    ```
  >
  >    输出结果：
  >
  >    ```java
  >    test static
  >    myclass static
  >    person static
  >    person Test
  >    test constructor
  >    person MyClass
  >    myclass constructor
  >    ```
  >
  >    **结论：**静态代码块父类先执行、自己后执行、调用的最后执行，调用方法也是父类先执行。
  >
  > 3. 这段代码的输出结果是什么？
  >
  >    ```java
  >    public class Test {
  >        static{
  >            System.out.println("test static 1");
  >        }
  >        public static void main(String[] args) {
  >             
  >        }
  >        static{
  >            System.out.println("test static 2");
  >        }
  >    }
  >    ```
  >
  >    输出结果：
  >
  >    ```java
  >    test static 1
  >    test static 2
  >    ```
  >
  >    **结论：**虽然在main方法中没有任何语句，但是还是会输出，原因上面已经讲述过了。另外，static块可以出现类中的任何地方（只要不是方法内部，记住，任何方法内部都不行），并且执行是按照static块的顺序执行的。

- #### 阐述静态变量和实例变量的区别？

  静 态 变 量 是 被static修 饰 符 修 饰 的 变 量 ， 也 称 为 类 变 量 ， 它 属 于 类 ， 不 属 于 类 的任 何 一 个 对 象 ， 一 个 类 不 管 创 建 多 少 个 对 象 ， 静 态 变 量 在 内 存 中 有 且 仅 有 一 个 拷贝；实 例 变 量 必 须 依 存 于 某 一 实 例，需 要 先 创 建 对 象 然 后 通 过 对 象 才 能 访 问 到 它。静 态 变 量 可 以 实 现 让 多 个 对 象 共 享 内 存 。



### public、protected、default、private的作用域：

|           | 类内部 | 本包 | 子类 | 外部包 |
| --------- | ------ | ---- | ---- | ------ |
| public    | √      | √    | √    | √      |
| protected | √      | √    | √    | ×      |
| default   | √      | √    | ×    | ×      |
| private   | √      | ×    | ×    | ×      |

### 泛型：

- #### 泛型介绍：

  **java** SE 1.5的新特性，泛型是在**创建对象或调用方法**时才会明确具体的类型

- #### 泛型的好处：

  1. 代码更加简洁【不用强制转换】
  2. 程序更加健壮【只要编译时期没有警告，那么运行时期就不会出现ClassCastException异常】
  3. 可读性和稳定性【在编写集合的时候，就限定了类型】

- #### 为什么需要泛型：

  ##### 以集合为例：

  ​	集合如果是装载的是Object，那么对集合元素的类型是没有任何限制的，本来我的Collection集合装载的是全部的Dog对象，但是外边把Cat对象存储到集合中，是没有任何语法错误的，这样就涉及强制转换。

  ​	但是如果集合使用泛型，那么创建集合的时候，我们就明确了集合的类型了。

  ```java
  ArrayList<String> list = new ArrayList<>(); //明确装载String类型元素
  ```

- #### 泛型的通配符：

  T，E，K，V，？：

  - ？表示不确定的 java 类型（使用？可以接收任意泛型对象）
  - T (type) 表示具体的一个java类型
  - K V (key value) 分别代表java键值中的Key Value
  - E (element) 代表Element

- #### 项目中运用泛型的例子：

  写工具类的时候要用到，比如map2bean。
  
- #### 泛型上限和下限：

  - ##### 在方法中设置上限：

    设置上限【如下代码：fun(Info<? extends Number> temp），如果泛型符合Number子类，那么进入fun（）】：

    ```java
    class Info<T>{
        private T var ;        // 定义泛型变量
        public void setVar(T var){
            this.var = var ;
        }
        public T getVar(){
            return this.var ;
        }
        public String toString(){    // 直接打印
            return this.var.toString() ;
        }
    };
    public class GenericsDemo17{
        public static void main(String args[]){
            Info<Integer> i1 = new Info<Integer>() ;        // 声明Integer的泛型对象
            Info<Float> i2 = new Info<Float>() ;            // 声明Float的泛型对象
            i1.setVar(30) ;                                    // 设置整数，自动装箱
            i2.setVar(30.1f) ;                                // 设置小数，自动装箱
            fun(i1) ;
            fun(i2) ;
        }
        public static void fun(Info<? extends Number> temp){    // 只能接收Number及其Number的子类
            System.out.print(temp + "、") ;
        }
    };
    ```

    运行成功。但是，如果传人的泛型类型为String的话就不行，因为String不是Number子类。

  - ##### 在类中设置泛型上限：

    ```java
    package Thread1;
    class Info<T extends Number>{    // 此处泛型只能是数字类型
        private T var ;        // 定义泛型变量
        public void setVar(T var){
            this.var = var ;
        }
        public T getVar(){
            return this.var ;
        }
        public String toString(){    // 直接打印
            return this.var.toString() ;
        }
    };
    public class demo1{
        public static void main(String args[]){
            Info<Integer> i1 = new Info<Integer>() ;        // 声明Integer的泛型对象
        }
    };
    ```

  - ##### 在方法中设置下限：

    ```java
    class Info<T>{
        private T var ;        // 定义泛型变量
        public void setVar(T var){
            this.var = var ;
        }
        public T getVar(){
            return this.var ;
        }
        public String toString(){    // 直接打印
            return this.var.toString() ;
        }
    };
    public class GenericsDemo21{
        public static void main(String args[]){
            Info<String> i1 = new Info<String>() ;        // 声明String的泛型对象
            Info<Object> i2 = new Info<Object>() ;        // 声明Object的泛型对象
            i1.setVar("hello") ;
            i2.setVar(new Object()) ;
            fun(i1) ;
            fun(i2) ;
        }
        public static void fun(Info<? super String> temp){    // 只能接收String或Object类型的泛型，String类的父类只有Object类
            System.out.print(temp + "、") ;
        }
    };
    ```

    Object类和String类都是String的父类，所有运行成功，但是如果此时用Integer则会出错，因为integer并不是String父类。

  - ##### 泛型与子类继承的限制：

    一个类的子类可以通过对象多态性，为其父类实例化，但是在泛型操作中，子类的泛型类型是无法使用父类的泛型类型接收的。例如：Info<String>不能使用Info<Object>接收。

    例如，以下肯定出错。

    ```java
    class Info<T>{
        private T var ;        // 定义泛型变量
        public void setVar(T var){
            this.var = var ;
        }
        public T getVar(){
            return this.var ;
        }
        public String toString(){    // 直接打印
            return this.var.toString() ;
        }
    };
    public class GenericsDemo23{
        public static void main(String args[]){
            Info<String> i1 = new Info<String>() ;        // 泛型类型为String
            Info<Object> i2 = null ;
            i2 = i1 ;　　　　　　　　　　　　　　　　　　//这里因为对象泛型类型不同，而出错。
            i1 = i2 ;　　　　　　　　　　　　　　　　　　//也出错。
        }
    };
    ```

    <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210501235757391.png" alt="image-20210501235757391" style="zoom:80%;" />

### java反射：

- #### 反射例子：

  ```java
  //操作apple的set操作
  Apple apple = new Apple(); 
  apple.setPrice(4);
  
  //使用反射实现以上功能
  Class clz = Class.forName("com.chenshuyi.reflect.Apple");
  Method method = clz.getMethod("setPrice", int.class);
  Constructor constructor = clz.getConstructor();
  Object object = constructor.newInstance();
  method.invoke(object, 4);
  ```

- #### 反射介绍：

  反射就是在运行时才知道要操作的类是什么，并且可以在运行时获取类的完整构造，并调用对应的方法

- #### 项目中用的反射：

  比如自定义注解，需要扫描包进行反射调用。



### 面试题：

#### float f=3.4;是否正确？

**答：**不正确。3.4 是双精度数(double1)，这样写属于向下转型，会造成精度丢失。因此需要强制类型转换

float f =(float)3.4; 或者写成 float f =3.4F;。

> ##### 对象的向上转型：
>
> 把子类的对象转化为父类的对象
>
> ##### 基本数据类型向上转型（小转大）：
>
> byte->short
>
> char -> int -> long     
>
> float -> double
> int -> float
> long -> double
> 注意 ： 小可转大，大转小会失去精度！！！

#### short s1 = 1; s1 = s1 + 1;有错吗?short s1 = 1; s1 += 1;有错吗？

**答：**short s1 = 1; s1 = s1 + 1;需要强转。short s1 = 1; s1 += 1;可以正确编译，因为 s1+= 1;相当于 s1 = (short)(s1 + 1);其中有隐含的强制类型转换。

#### goto关键字：

goto关键字，用来改变函数内代码的执行顺序，跳转到函数内指定的标签地方运行，goto不能跨函数代码块跳转：（java目前不支持goto关键字）

```java
// 定义一个标签，这个标签只能被goto使用
RET:
	if i < 5 {
		fmt.Println("循环第", i, "次")
		i++
		// 调转到锚点RET处开始执行
		goto RET
	}

	fmt.Println("end")
}
```

输出结果：

```java
begin
循环第 0 次
循环第 1 次
循环第 2 次
循环第 3 次
循环第 4 次
end
```

#### 拆箱和装箱：

```java
public class AutoUnboxingTest {
	public static void main(String[] args) {
        Integer a = new Integer(3);
        Integer b = 3; // 将 3 自动装箱成 Integer 类型
        int c = 3;
        System.out.println(a == b); // false 两个引用没有引用同一对象
        System.out.println(a == c); // true a 自动拆箱成 int 类型再和 c比较
	} 
}
```

```java
public class Test03 {
	public static void main(String[] args) {
        Integer f1 = 100, f2 = 100, f3 = 150, f4 = 150;
        System.out.println(f1 == f2); //true：整型字面量的值在-128 到 127 之间，那么不会 new 新的 Integer对象
        System.out.println(f3 == f4);//false
	}
}
```

#### &和&&的区别？

##### 相同点：

二者都要求运算符左右两端的布尔值都是true 整个表达式的值才是 true。

##### 不同点：

&&左边的表达式的值是 false，右边的表达式会被直接短路掉，不会进行运算。

&则不会短路。

&还可以做位运算：

> | 符号 | 描述 | 运算规则                                                     |
> | :--- | :--- | :----------------------------------------------------------- |
> | &    | 与   | 两个位都为1时，结果才为1                                     |
> | \|   | 或   | 两个位都为0时，结果才为0                                     |
> | ^    | 异或 | 两个位相同为0，相异为1                                       |
> | ~    | 取反 | 0变1，1变0                                                   |
> | <<   | 左移 | 各二进位全部左移若干位，高位丢弃，低位补0                    |
> | >>   | 右移 | 各二进位全部右移若干位，对无符号数，高位补0，有符号数，各编译器处理方法不一样，有的补符号位（算术右移），有的补0（逻辑右移） |

#### 左移右移：

> A = 0011 1100
>
> |      |                                                            |                                   |
> | ---- | ---------------------------------------------------------- | --------------------------------- |
> | <<   | 二进制左移运算符。左操作数的值向左移动右操作数指定的位数。 | A << 2 将得到 240，即为 1111 0000 |
> | >>   | 二进制右移运算符。左操作数的值向右移动右操作数指定的位数。 | A >> 2 将得到 15，即为 0000 1111  |

```java
int num =0x3241
    num=num>>8;
	num|=0x7010
    System.out.println(Integer.toHexString(num)) //输出是16进制
```

1. 带0x的是16进制，将0x3241转为十进制：

   0x3241=(3x16^3)+(2x16^2)+(4x16^1)+(1x16^0)=12865

2. 12865转为二进制：

   12865=0011 0010 0100 0001

3. 11001001000001进行>>8：

   0011 0010 0100 0001>>8=0000 0000 0011 0010

4. 0x7010同上方法转为2进制：

   0x7010 = 0111 0000 0001 0000

5. 0000 0000 0011 0010| 0111 0000 0001 0000：

   0000 0000 0011 0010|0111 0000 0001 0000=0111 0000 0011 0010

6. 0111 0000 0011 0010转为10进制：

   212562 =7032（输出结果）

#### 二进制、十进制、十六进制之间的转换

- ##### 二进制 转 十进制：

  二进制数：**0101** 转 十进制数： **5**：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210318230213213.png" alt="image-20210318230213213" style="zoom:50%;" />

- ##### 十进制 转 二进制：

  十进制数：**5** 转 二进制数：**0101**：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210318230255309.png" alt="image-20210318230255309" style="zoom:50%;" />

- ##### 十六进制 转 十进制：

  十六进制数：**2AC** 转 十进制数：**684**：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210318230342129.png" alt="image-20210318230342129" style="zoom:50%;" />

- ##### 十进制 转 十六进制：

  十进制数：**684** 转 十六进制数：**2AC**：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210318230450052.png" alt="image-20210318230450052" style="zoom:50%;" />

- ##### 二进制 转 十六进制：

  二进制数：**10101101110** 转 十六进制数：**56E**：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210318230547223.png" alt="image-20210318230547223" style="zoom:50%;" />

- 十六进制 转 二进制：

  十六进制数：**56E** 转 二进制数：**10101101110**：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210318230617407.png" alt="image-20210318230617407" style="zoom:50%;" />

#### Math.round(11.5) 等于多少？Math.round(-11.5)等于多少？

> round函数：四舍五入。

Math.round(11.5)的返回值是 12，Math.round(-11.5)的返回值是-11。

#### switch 是否能作用在 byte 上，是否能作用在 long 上，是否能作用在 String 上？

在 Java 5 以前，switch(expr)中，expr 只能是 byte、short、char、int。从 Java5 开始，Java 中引入了枚举类型，expr 也可以是 enum 类型，从 Java 7 开始，expr 还可以是字符串（String），但是长整型（long）在目前所有的版本中都是不可以的。

#### 用最有效率的方法计算 2 乘以 8？

2 << 3（左移 3 位相当于乘以 2 的 3 次方，右移 3 位相当于除以 2 的 3 次方）。

#### 如何从 100 亿 URL 中找出相同的 URL？

##### 采用分而治之的方式：

对100亿数据进行哈希取余；

然后对每个子文件进行 HashSet 统计。

> ##### 实现如下：
>
> 首先遍历文件 a，对遍历到的 URL 求 `hash(URL) % 1000` ，根据计算结果把遍历到的 URL 存储到 a0, a1, a2, ..., a999，这样每个大小约为 300MB。
>
> 使用同样的方法遍历文件 b，把文件 b 中的 URL 分别存储到文件 b0, b1, b2, ..., b999 中。这样处理过后，所有可能相同的 URL 都在对应的小文件中，即 a0 对应 b0, ..., a999 对应 b999，不对应的小文件不可能有相同的 URL。那么接下来，我们只需要求出这 1000 对小文件中相同的 URL 就好了。
>
> 接着遍历 ai( `i∈[0,999]` )，把 URL 存储到一个 HashSet 集合中。然后遍历 bi 中每个 URL，看在 HashSet 集合中是否存在，若存在，说明这就是共同的 URL，可以把这个 URL 保存到一个单独的文件中。

#### 数组有没有length()方法？String有没有length()方法？

数组没有length()方法，有length的属性。String有length()方法。JavaScript中 ， 获得字符串的长度是通过length属 性得到的 ， 这一 点容易混淆。 

#### 在Java中，如何跳出当前的多重嵌套循环？

在 最 外 层 循 环 前 加 一 个 标 记 如A， 然 后 用breakA;可 以 跳 出 多 重 循 环 。

（Java中支 持 带 标 签 的break和continue语 句 ， 作 用 有 点 类 似 于C和C++中 的goto语句，但 是 就 像 要 避 免 使 用goto一 样，应 该 避 免 使 用 带 标 签 的break和continue，因 为 它 不 会 让 你 的 程 序 变 得 更 优 雅 ， 很 多 时 候 甚 至 有 相 反 的 作 用 ， 所 以 这 种 语 法其 实 不 知 道 更 好 ）

#### 构造器（constructor）是否可被重写（override）？

构 造 器 不 能 被 继 承 ， 因 此 不 能 被 重 写 ， 但 可 以 被 重 载 。

#### 当一个对象被当作参数传递到一个方法后，此方法可改变这个对象的属性，并可返回变化后的结果，那么这里到底是值传递还是引用传递？？？



#### 描述一下JVM加载class文件的原理机制？

参考JVM笔记。

#### char型变量中能不能存贮一个中文汉字，为什么？

char类 型 可 以 存 储 一 个 中 文 汉 字 ， 因 为Java中 使 用 的 编 码 是Unicode（ 不 选 择任 何 特 定 的 编 码 ， 直 接 使 用 字 符 在 字 符 集 中 的 编 号 ， 这 是 统 一 的 唯 一 方 法 ） ， 一个char类 型 占2个 字 节 （16比 特 ） ， 所 以 放 一 个 中 文 是 没 问 题 的 。

> **补 充 ：**使 用Unicode意 味 着 字 符 在JVM内 部 和 外 部 有 不 同 的 表 现 形 式 ， 在JVM内 部 都 是Unicode，当 这 个 字 符 被 从 JVM内 部 转 移 到 外 部 时（例 如 存 入 文 件 系 统中），需 要 进 行 编 码 转 换。所 以Java中 有 字 节 流 和 字 符 流，以 及 在 字 符 流 和 字 节流 之 间 进 行 转 换 的 转 换 流 ， 如InputStreamReader和OutputStreamReader，这 两 个 类 是 字 节 流 和 字 符 流 之 间 的 适 配 器 类 ， 承 担 了 编 码 转 换 的 任 务 ； 对 于C程序 员 来 说 ， 要 完 成 这 样 的 编 码 转 换 恐 怕 要 依 赖 于union（ 联 合 体/共 用 体 ） 共 享 内存 的 特 征 来 实 现 了 。

#### 什么是编码？

​	由于人类的语言有太多，因而表示这些语言的符号太多，无法用计算机中一个基本的存储单元—— byte  来表示，因而必须要经过拆分或一些翻译工作，才能让计算机能理解。我们可以把计算机能够理解的语言假定为英语，其它语言要能够在计算机中使用必须经过一次翻译，把它翻译成英语。这个翻译的过程就是编码。

#### 编码种类：

- ASCII 码   
  - 　学过计算机的人都知道 ASCII 码，总共有 128 个，用一个字节的低 7 位表示，0~31 是控制字符如换行回车删除等；32~126 是打印字符，可以通过键盘输入并且能够显示出来。　

- ISO-8859-1（扩展ASCII编码）   
  - 128 个字符显然是不够用的，于是 ISO 组织在 ASCII 码基础上又制定了一些列标准用来扩展 ASCII 编码，它们是  ISO-8859-1~ISO-8859-15，其中 ISO-8859-1 涵盖了大多数西欧语言字符，所有应用的最广泛。ISO-8859-1  仍然是单字节编码，它总共能表示 256 个字符。　　

- GB2312   
  - 它的全称是《信息交换用汉字编码字符集 基本集》，它是双字节编码，总的编码范围是 A1-F7，其中从 A1-A9 是符号区，总共包含 682 个符号，从 B0-F7 是汉字区，包含 6763 个汉字。　

- GBK（扩展GB2312）   
  - 全称叫《汉字内码扩展规范》，是国家技术监督局为 windows95 所制定的新的汉字内码规范，它的出现是为了扩展 GB2312，加入更多的汉字，它的编码范围是 8140~FEFE（去掉  XX7F）总共有 23940 个码位，它能表示 21003 个汉字，它的编码是和 GB2312 兼容的，也就是说用 GB2312  编码的汉字可以用 GBK 来解码，并且不会有乱码。　　

- GB18030（兼容GB2312）   
  - 全称是《信息交换用汉字编码字符集》，是我国的强制标准，它可能是单字节、双字节或者四字节编码，它的编码与 GB2312 编码兼容，这个虽然是国家标准，但是实际应用系统中使用的并不广泛。　　

- Unicode编码集   
  - ISO 试图想创建一个全新的超语言字典，世界上所有的语言都可以通过这本字典来相互翻译。可想而知这个字典是多么的复杂，关于 Unicode  的详细规范可以参考相应文档。Unicode 是 Java 和 XML 的基础，下面详细介绍 Unicode 在计算机中的存储形式。
  - UTF-16     
    - UTF-16 具体定义了 Unicode 字符在计算机中存取方法。UTF-16 用两个字节来表示 Unicode  转化格式，这个是定长的表示方法，不论什么字符都可以用两个字节表示，两个字节是 16 个 bit，所以叫 UTF-16。UTF-16  表示字符非常方便，每两个字节表示一个字符，这个在字符串操作时就大大简化了操作，这也是 Java 以 UTF-16 作为内存的字符存储格式的一个很重要的原因。
  - UTF-8     
    - UTF-16  统一采用两个字节表示一个字符，虽然在表示上非常简单方便，但是也有其缺点，有很大一部分字符用一个字节就可以表示的现在要两个字节表示，存储空间放大了一倍，在现在的网络带宽还非常有限的今天，这样会增大网络传输的流量，而且也没必要。而 UTF-8 采用了一种变长技术，每个编码区域有不同的字码长度。不同类型的字符可以是由 1~6 个字节组成。
    - UTF-8 有以下编码规则：
      1. 如果一个字节，最高位（第 8 位）为 0，表示这是一个 ASCII 字符（00 - 7F）。可见，所有 ASCII 编码已经是 UTF-8 了。
      2. 如果一个字节，以 11 开头，连续的 1 的个数暗示这个字符的字节数，例如：110xxxxx 代表它是双字节 UTF-8 字符的首字节。
      3. 如果一个字节，以 10 开始，表示它不是首字节，需要向前查找才能得到当前字符的首字节 

#### 抽象类（abstractclass）和接口（interface）有什么异同？

##### 相同：

1. 抽象类和接口都不能够实例化，但可以定义抽象类和接口类型的引用 。
2. 

##### 不同：

1. 抽象类可以有构造方法，接口中不能有构造方法。

2. 抽象类中可以有普通成员变量，接口中没有普通成员变量。

3. 抽象类中可以包含非抽象的普通方法，接口中的所有方法必须都是抽象的，不能有非抽象的普通方法。

4. 抽 象 类 中 的 成 员 可 以 是private、默 认、protected、public的，而 接 口 中 的 成 员 全 都 是public的。

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210322001606082.png" alt="image-20210322001606082" style="zoom:60%;" />

   <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210322001636083.png" alt="image-20210322001636083" style="zoom:60%;" />

5. 抽象类中可以包含静态方法，接口中不能包含静态方法。

6. 抽象类和接口中都可以包含静态成员变量，抽象类中的静态成员变量的访问类型可以任意，但接口中定义的变量只能是public static final类型，并且默认即为public static final类型。

7. 一个类可以实现多个接口，但只能继承一个抽象类。

#### 静态内部类（又叫静态嵌套类）和内部类的区别？

静态内部类不依赖于外部类实例被实例化，而通常的内部类需要在外部类实例化后才能实例化 。

```java
static class Outer {
	class Inner {} //内部类
	static class StaticInner {} //静态内部类（又叫静态嵌套类）
}
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();
Outer.StaticInner inner0 = new Outer.StaticInner();
```

> 从字面的角度解释是这样的：
>
>  什么是嵌套？嵌套就是我跟你没关系，自己可以完全独立存在，但是我就想借你的壳用一下，来隐藏一下我自己。 
>
> 什么是内部？内部就是我是你的一部分，我了解你，我知道你的全部，没有你就没有我。（所以内部类对象是以外部类对象存在为前提的）

##### 总结：

一 静态内部类可以有静态变量和方法，而非静态内部类则不能有静态成员和静态方法。 
二 静态内部类的非静态变量和方法可以访问外部类的静态变量，而不可访问外部类的非静态变量；
三 非静态内部类的非静态成员可以访问外部类的非静态变量和方法。

##### 相关面试题：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210322000937295.png" alt="image-20210322000937295" style="zoom:50%;" />

Java中 非 静 态 内 部 类 对 象 的 创 建 要 依 赖 其 外 部 类 对 象，上 面 的 面 试 题 中foo和main方 法 都 是 静 态 方 法，静 态 方 法 中 没 有this，也 就 是 说 没 有 所 谓 的 外 部 类 对象 ， 因 此 无 法 创 建 内 部 类 对 象 ， 如 果 要 在 静 态 方 法 中 创 建 内 部 类 对 象 ， 可 以 这 样做 ：

```java
new Outer().new Inner();
```

#### 抽象的（abstract）方法是否可同时是静态的（static）,是否可同时是本地方法（native），是否可同时被synchronized修饰？

都 不 能 。 抽 象 方 法 需 要 子 类 重 写 ， 而 静 态 的 方 法 是 无 法 被 重 写 的 ， 因 此 二 者 是 矛盾 的 。 本 地 方 法 是 由 本 地 代 码 （ 如C代 码 ） 实 现 的 方 法 ， 而 抽 象 方 法 是 没 有 实 现的，也 是 矛 盾 的。synchronized和 方 法 的 实 现 细 节 有 关，抽象方法不涉及实现细节 ， 因此也是相互矛盾的 。

#### 接口是否可继承（extends）接口？抽象类是否可实现（implements）接口？抽象类是否可继承具体类（concrete class）

接 口 可 以 继 承 接 口 ， 而 且 支 持 多 重 继 承 。 抽 象 类 可 以 实 现(implements)接 口 ， 抽象 类 可 继 承 具 体 类 也 可 以 继 承 抽 象 类 。

#### 一个”.java”源文件中是否可以包含多个类（不是内部类）？有什么限制？

可 以 ， 但 一 个 源 文 件 中 最 多 只 能 有 一 个 公 开 类 （publicclass） 而 且 文 件 名 必 须 和公 开 类 的 类 名 完 全 保 持 一 致 。

#### AnonymousInnerClass(匿名内部类)是否可以继承其它类？是否可以实现接口？

可 以 继 承 其 他 类 或 实 现 其 他 接 口，在Swing编 程 和Android开 发 中 常 用 此 方 式 来实 现 事 件 监 听 和 回 调 。

#### 内部类可以引用它的包含类（外部类）的成员吗？有没有什么限制？

一 个 内 部 类 对 象 可 以 访 问 **创 建 它 的 外 部 类** 对 象 的 成 员 ， 包 括 私 有 成 员 。

#### 指出下面程序的运行结果

```java
class A{
    static{
        System.out.print("1");
    }
    public A(){
        System.out.print("2");
    }
}
class B extends A { 
    static{
        System.out.print("a");
    }
    public B(){
        System.out.print("b");
    }
}
public class Hello{
    public static void main(String[]args){
        A ab= new B();
        ab=newB();
    }
}
```

执行结果：

```java
1a2b2b
```

#### 数据类型之间的转换：

- ##### 如何将字符串转换为基本数据类型？

  用基本数据类型对应的包装类中的方法parseXXX(String)或valueOf(String)即可返回相应基本类型；

- ##### 如何将基本数据类型转换为字符串？

  一种方法是将基本数据类型与空字符串（”“）连接（+）即可获得其所对应的字符串；另一种方法是调用String类中的valueOf()方法返回相应字符串

#### 如何实现字符串的反转及替换？

方 法 很 多 ， 可 以 自 己 写 实 现 也 可 以 使 用String或StringBuffer/StringBuilder中的 方 法 。 有 一 道 很 常 见 的 面 试 题 是 用 递 归 实 现 字 符 串 反 转 ， 代 码 如 下 所 示 ：

```java
public static String reverse(String originStr){
    if(originStr==null||originStr.length()<=1)
        return originStr;
    return reverse(originStr.substring(1))+originStr.charAt(0);
}
```

#### 怎样将 GB2312 编码的字符串转换为 ISO-8859-1 编码的字符串？

代码如下所示：

```java
String s1 = "你好";
String s2 = new String(s1.getBytes("GB2312"), "ISO-8859-1");
```

#### 日期和时间：

- ##### 如何取得年月日、小时分钟秒？

  创 建java.util.Calendar实 例 ， 调 用 其get()方 法 传 入 不 同 的 参 数 即 可 获得 参 数 所 对 应 的 值。Java8中 可 以 使 用java.time.LocalDateTimel来 获 取，代 码如 下 所 示 。

  ```java
  public class DateTimeTest {
  public static void main(String[] args) {
      Calendar cal = Calendar.getInstance();
      System.out.println(cal.get(Calendar.YEAR));
      System.out.println(cal.get(Calendar.MONTH)); // 0 - 11
      System.out.println(cal.get(Calendar.DATE));
      System.out.println(cal.get(Calendar.HOUR_OF_DAY));
      System.out.println(cal.get(Calendar.MINUTE));
      System.out.println(cal.get(Calendar.SECOND));
      
      // Java 8
      LocalDateTime dt = LocalDateTime.now();
      System.out.println(dt.getYear());
      System.out.println(dt.getMonthValue()); // 1 - 12
      System.out.println(dt.getDayOfMonth());
      System.out.println(dt.getHour());
      System.out.println(dt.getMinute());
  	System.out.println(dt.getSecond());
  	}
  }
  ```

- ##### 如何取得从1970年1月1日0时0分0秒到现在的毫秒数？

  ```java
  Calendar.getInstance().getTimeInMillis();
  System.currentTimeMillis();
  Clock.systemDefaultZone().millis(); // Java 8
  ```

- ##### 如何取得某月的最后一天？

  ```java
  Calendar time = Calendar.getInstance();
  time.getActualMaximum(Calendar.DAY_OF_MONTH);
  ```

- ##### 如何格式化日期？

  ```java
  class DateFormatTest {
  	public static void main(String[] args) {
          SimpleDateFormat oldFormatter = new
          SimpleDateFormat("yyyy/MM/dd");
          Date date1 = new Date();
          System.out.println(oldFormatter.format(date1));
          // Java 8
          DateTimeFormatter newFormatter =
          DateTimeFormatter.ofPattern("yyyy/MM/dd");
          LocalDate date2 = LocalDate.now();
          System.out.println(date2.format(newFormatter));
      }
  }
  ```

  补充：Java 的时间日期API 一直以来都是被诟病的东西，为了解决这一问题，Java8 中引入了新的时间日期API，其中包括LocalDate、LocalTime、LocalDateTime、Clock、Instant 等类，这些的类的设计都使用了不变模式，因此是线程安全的设计。如果不理解这些内容，可以参考我的另一篇文章《关于Java 并发编程的总结和思考》。

- ##### 打印昨天的当前时刻。

  ```java
  class YesterdayCurrent {
  	public static void main(String[] args){
          Calendar cal = Calendar.getInstance();
          cal.add(Calendar.DATE, -1);
          System.out.println(cal.getTime());
  	}
  }
  ```

  在Java 8 中， 可以用下面的代码实现相同的功能。

  ```java
  class YesterdayCurrent {
  	public static void main(String[] args) {
          LocalDateTime today = LocalDateTime.now();
          LocalDateTime yesterday = today.minusDays(1);
          System.out.println(yesterday);
  	}
  }
  ```

#### Error和Exception有什么区别？

​	 Error 表示系统级的错误和程序不必处理的异常，是恢复不是不可能但很困难的情况下的一种严重问题； 比如内存溢出，不可能指望程序能处理这样的情况；
 	Exception 表示需要捕捉或者需要程序进行处理的异常， 是一种设计或实现问题；也就是说，它表示如果程序运行正常，从不会发生的情况。

#### try{}里有一个return 语句，那么紧跟在这个try 后的finally{}里的代码会不会被执行，什么时候被执行，在return前还是后?

```java
	try {
            return "00";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("1111");
        }
```

会执行， 在方法返回调用者前执行。（意思是上面代码先执行finally里的输出语句，再执行return "00"）。

注意：在finally 中改变返回值的做法是不好的，因为如果存在finally 代码块，try中的return 语句不会立马返回调用者，而是记录下返回值待finally 代码块执行完毕之后再向调用者返回其值，然后如果在finally 中修改了返回值， 就会返回修改后的值。显然，在finally 中返回或者修改返回值会对程序造成很大的困扰，C#中
直接用编译错误的方式来阻止程序员干这种龌龊的事情，Java 中也可以通过提升编译器的语法检查级别来产生警告或错误，Eclipse 中可以在如图所示的地方进行设置，强烈建议将此项设置为编译错误。

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210324195726886.png" alt="image-20210324195726886" style="zoom:70%;" />

#### Java 语言如何进行异常处理，关键字：throws、throw、try、catch、finally 分别如何使用？

- try catch 处理:自己将问题处理掉，不会影响到后续代码的继续执行
- throw 抛出：问题自己无法处理，可以通过 throw 关键字，将异常对象抛出给调用者。如果抛出的对象是 RuntimeException 或 Error，则无需在方法上 throws 声明；其他异常，方法上面必须进行 throws 的声明，告知调用者此方法存在异常
- finally 无论为确保一段代码不管发生什么异常状况都要被执行

#### 运行时异常与受检异常有何异同？

- 运行时异常，下图的RunTimeException，表示程序代码在运行时发生的异常，程序代码设计的合理，这类异常不会发生
- 受检异常，跟程序运行的上下文环境有关，即使程序设计无误，仍然可能因使用的问题而引发（就是一些代码需要强制抛异常，不然编译不通过）。

![image-20210407172230777](C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210407172230777.png)

##### 总体上我们根据Javac 对异常的处理要求，将异常类分为二类。

- 非检查异常（ unckecked exception ）： Error 和 RuntimeException 以及他们的子类。javac 在编译时，不会提示和发现这样的异常如除0错误ArithmeticException ，错误的强制类型转换错误ClassCastException ，数组索引越界ArrayIndexOutOfBoundsException ，使用了空对象
  NullPointerException 等等。
  - 受检异常，跟程序运行的上下文环境有关，即使程序设计无误，仍然可能因使用的问题而引发（就是一些代码需要强制抛异常，不然编译不通过）。
- 检查异常（ checked exception ）：除了Error 和 RuntimeException 的其它异常，编译期就会报错。如SQLException , IOException , ClassNotFoundException 等。
  需要明确的是：检查和非检查是对于javac 来说的，这样就很好理解和区分了。

#### 列出一些你常见的运行时异常？

- NullPointerException - 空指针异常
- ClassCastException - 类转换异常
- IndexOutOfBoundsException - 下标越界异常
- ArithmeticException - 计算异常
- IllegalArgumentException - 非法参数异常
- NumberFormatException - 数字格式异常
- UnsupportedOperationException 操作不支持异常
- ArrayStoreException - 数据存储异常，操作数组时类型不一致
- BufferOverflowException - IO 操作时出现的缓冲区上溢异常
- NoSuchElementException - 元素不存在异常
- InputMismatchException - 输入类型不匹配异常

#### 说出下面代码的运行结果：

```java
	try {
            throw new java.io.IOException();
        }catch (java.io.FileNotFoundException ex){
            System.out.println("FileNotFoundException");
        }catch (java.io.IOException ex){
            System.out.println("IOException");
        }catch (java.lang.Exception ex){
            System.out.println("Exception");
        }
```

输出结果：

```
IOException
```

#### 说出下面代码的运行结果：

类ExampleA继承Exception，类ExampleB继承ExampleA。

```java
try {
	throw new ExampleB("b")
} catch（ExampleA e）{
	System.out.println("ExampleA");
} catch（Exception e）{
	System.out.println("Exception");
}
```

输出结果：

```java
ExampleA
```

#### 说出下面代码的运行结果：

```java
class Annoyance extends Exception {}
class Sneeze extends Annoyance {}

class Human {
    public static void main(String[] args) throws Exception {
        try {
            try {
                throw new Sneeze();
            }catch ( Annoyance a ) {
                System.out.println("Caught Annoyance");
                throw a;
            }
        }catch ( Sneeze s ) {
            System.out.println("Caught Sneeze");
            return ;
        }finally {
                System.out.println("Hello World!");
            }
        }
}
```

输出结果：

```java
  Caught Annoyance
  Caught Sneeze
  Hello World!
```

  变形一下：

  ```java
  class Annoyance extends Exception {}
  class Sneeze extends Annoyance {}
  
  public static void main(String[] args) throws Exception {
          try {
              try {
                  throw new Annoyance();
              }catch ( Sneeze s ) {
                  System.out.println("Caught Sneeze");
                  throw s;
              }
          }catch ( Annoyance a ) {
              System.out.println("Caught Annoyance");
              return ;
          }finally {
              System.out.println("Hello World!");
          }
      }
  }
  ```

  输出结果：

  ```
  Caught Annoyance
  Hello World!
  ```

#### 阐述final、finally、finalize的区别？

final：修饰符（关键字）有四种用法：

- 如果一个类被声明为final，意味着它不能再派生出新的子类，即不能被继承。
- 将变量声明为final，可以保证它们在使用中不被改变。
- 被声明为final的变量必须在声明时给定初值，而在以后的引用中只能读取不可修改。
- 被声明为final的方法也同样只能使用，不能在子类中被重写。

finally：通常放在try...catch...的后面构造总是执行代码块，这就意味着程序无论正常执行还是发生异常，这里的代码只要JVM不关闭都能执行，可以将释放外部资源的代码写在finally块中。

finalize：Object类中定义的方法，Java中允许使用finalize()方法在垃圾收集器将对象从内存中清除出去之前做必要的清理工作。这个方法是由垃圾收集器在销毁对象时调用的，通过重写finalize()方法可以整理系统资源或者执行其他清理工作。

#### 请说出与线程同步以及线程调度相关的方法？

-  wait()：使一个线程处于等待（阻塞）状态，并且释放所持有的对象的锁；
- sleep()：使一个正在运行的线程处于睡眠状态，是一个静态方法，调用
  此方法要处理InterruptedException 异常；
- notify()：唤醒一个处于等待状态的线程，当然在调用此方法的时候，并
  不能确切的唤醒某一个等待状态的线程，而是由JVM 确定唤醒哪个线程，而且
  与优先级无关；
- notityAll()：唤醒所有处于等待状态的线程，该方法并不是将对象的锁给
  所有线程，而是让它们竞争，只有获得锁的线程才能进入就绪状态；

#### Java中如何实现序列化，有什么意义？

序列化是为了解决对象流读写操作时可能引发的问题（ 如果不进行序列化可能会
存在数据乱序的问题） 。

实现序列化：让一个类实现Serializable 接口，然后使用writeObject(Object)方法就可以将实现对象写出，反序列化通过readObject 方法从流中读取对象。

编程实现文件拷贝：

```java
public static void fileCopy(String source, String target) throws
IOException {
    try (InputStream in = new FileInputStream(source)) {
            try (OutputStream out = new FileOutputStream(target)) {
                byte[] buffer = new byte[4096];
                int bytesToRead;
                while((bytesToRead = in.read(buffer)) != -1) {
                	out.write(buffer, 0, bytesToRead);
            }
        }
    }
}
```

#### Java 中应该使用什么数据类型来代表价格？

如果不是特别关心内存和性能的话， 使用BigDecimal，否则使用预定义精度的
double 类型。

#### 存在两个类，B 继承A，C 继承B，我们能将B 转换为C 么？如C = (C) B；

可以，面向对象思想源于生活，是对现实生活在程序中的一种抽象。

比如你的ABC分别对应动物，猫，黑猫。

向上转型就是比如

C c = new C();

B b = c;

你把c转型为B，黑猫是猫吗？是啊，所以这是ok的。



但是反过来

B b = new B();

C c = (C)b;

这就不ok了，只知道这个b是一只猫，他不一定是黑猫。



但如果这个b已经确定是一只黑猫了，那就可以转型了

B b = new C();

C c = (C)b;

这里的b本来就是黑猫啊。

#### Java 中++ 操作符是线程安全的吗？

不是线程安全的操作。它涉及到多个指令， 如读取变量值，增加， 然后存储回内存，这个过程可能会出现多个线程交差。

#### a = a + b 与a += b 的区别？

+= 隐式的将加操作的结果类型强制转换为持有结果的类型。

```java
byte a = 127;
byte b = 127;
b = a + b; // error : cannot convert from int to byte
b += a; // ok
```

上面的a+b 操作会将a、b 提升为int 类型，所以将int 类型赋值给byte就会编译出错。

#### 3*0.1 == 0.3 将会返回什么？true 还是false？

false，因为有些浮点数不能完全精确的表示出来。

#### jdk1.6升到1.8 变化？

- **Lambda表达式：**

  能简化代码，没使用Lambda表达式：

  ```java
  //匿名内部类
    Comparator<Integer> cpt = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(o1,o2);
        }
    };
  ```

  使用Lambda表达式：

  ```java
    Comparator<Integer> cpt2 = (x,y) -> Integer.compare(x,y);
  ```

- **函数式接口：**

  常见的四大函数式接口

  - ##### Consumer 《T》：消费型接口，有参无返回值：

    ```java
    @Test
    public void test(){
        changeStr("hello",(str) -> System.out.println(str));
    }
    
    /**
         *  Consumer<T> 消费型接口
         * @param str
         * @param con
         */
    public void changeStr(String str, Consumer<String> con){
        con.accept(str);
    }
    ```

  - ##### Supplier 《T》：供给型接口，无参有返回值

    ```java
    @Test
    public void test2(){
        String value = getValue(() -> "hello");
        System.out.println(value);
    }
    
    /**
         *  Supplier<T> 供给型接口
         * @param sup
         * @return
         */
    public String getValue(Supplier<String> sup){
        return sup.get();
    }
    ```

  - ##### Function 《T,R》：:函数式接口，有参有返回值

    ```java
    @Test
    public void test3(){
        Long result = changeNum(100L, (x) -> x + 200L);
        System.out.println(result);
    }
    
    /**
         *  Function<T,R> 函数式接口
         * @param num
         * @param fun
         * @return
         */
    public Long changeNum(Long num, Function<Long, Long> fun){
        return fun.apply(num);
    }
    ```

  - Predicate《T》： 断言型接口，有参有返回值，返回值是boolean类型

    ```java
    public void test4(){
            boolean result = changeBoolean("hello", (str) -> str.length() > 5);
            System.out.println(result);
        }
    
        /**
         *  Predicate<T> 断言型接口
         * @param str
         * @param pre
         * @return
         */
        public boolean changeBoolean(String str, Predicate<String> pre){
            return pre.test(str);
        }
    ```

    在四大核心函数式接口基础上，还提供了诸如BiFunction、BinaryOperation、toIntFunction等扩展的函数式接口，都是在这四种函数式接口上扩展而来的，不做赘述。

    总结：函数式接口的提出是为了让我们更加方便的使用lambda表达式，不需要自己再手动创建一个函数式接口，直接拿来用就好了。
    

- **方法引用和构造器调用：**

  1. 对象：：实例方法名

     ```java
     public void night() {
             System.out.println("Night study:" + this.toString());
         }
     studys.forEach(Study::night);
     ```

  2. 类：：静态方法名

  3. 类：：实例方法名 （lambda参数列表中第一个参数是实例方法的调用 者，第二个参数是实例方法的参数时可用）

- **Stream API：**

  stream的创建：

  ```java
      // 1，校验通过Collection 系列集合提供的stream()或者paralleStream()
      List<String> list = new ArrayList<>();
      Strean<String> stream1 = list.stream();
  
      // 2.通过Arrays的静态方法stream()获取数组流
      String[] str = new String[10];
      Stream<String> stream2 = Arrays.stream(str);
  
      // 3.通过Stream类中的静态方法of
      Stream<String> stream3 = Stream.of("aa","bb","cc");
  
      // 4.创建无限流
      // 迭代
      Stream<Integer> stream4 = Stream.iterate(0,(x) -> x+2);
  
      //生成
      Stream.generate(() ->Math.random());
  ```

  Stream的中间操作:

  ```java
  /**
     * 筛选 过滤  去重
     */
    emps.stream()
            .filter(e -> e.getAge() > 10)
            .limit(4)
            .skip(4)
            // 需要流中的元素重写hashCode和equals方法
            .distinct()
            .forEach(System.out::println);
  
  
    /**
     *  生成新的流 通过map映射
     */
    emps.stream()
            .map((e) -> e.getAge())
            .forEach(System.out::println);
  
  
    /**
     *  自然排序  定制排序
     */
    emps.stream()
            .sorted((e1 ,e2) -> {
                if (e1.getAge().equals(e2.getAge())){
                    return e1.getName().compareTo(e2.getName());
                } else{
                    return e1.getAge().compareTo(e2.getAge());
                }
            })
            .forEach(System.out::println);
  ```

- **接口中的默认方法和静态方法：**

  ```java
  public interface Interface {
      default  String getName(){
          return "zhangsan";
      }
  
      static String getName2(){
          return "zhangsan";
      }
  }
  ```

  在JDK1.8中很多接口会新增方法，为了保证1.8向下兼容，1.7版本中的接口实现类不用每个都重新实现新添加的接口方法，引入了default默认实现，static的用法是直接用接口名去调方法即可。当一个类继承父类又实现接口时，若后两者方法名相同，则优先继承父类中的同名方法，即“类优先”，如果实现两个同名方法的接口，则要求实现类必须手动声明默认实现哪个接口中的方法。

- **新时间日期API：**

  ##### 新的日期API LocalDate | LocalTime | LocalDateTime

  新的日期API都是不可变的，更使用于多线程的使用环境中

  ```java
      @Test
      public void test(){
          // 从默认时区的系统时钟获取当前的日期时间。不用考虑时区差
          LocalDateTime date = LocalDateTime.now();
          //2018-07-15T14:22:39.759
          System.out.println(date);
  
          System.out.println(date.getYear());
          System.out.println(date.getMonthValue());
          System.out.println(date.getDayOfMonth());
          System.out.println(date.getHour());
          System.out.println(date.getMinute());
          System.out.println(date.getSecond());
          System.out.println(date.getNano());
  
          // 手动创建一个LocalDateTime实例
          LocalDateTime date2 = LocalDateTime.of(2017, 12, 17, 9, 31, 31, 31);
          System.out.println(date2);
          // 进行加操作，得到新的日期实例
          LocalDateTime date3 = date2.plusDays(12);
          System.out.println(date3);
          // 进行减操作，得到新的日期实例
          LocalDateTime date4 = date3.minusYears(2);
          System.out.println(date4);
      }
  ```

#### 怎样判断一个链表是循环链表？怎样判断链表中存在环？

有环的定义：链表的尾结点指向了链表中的某个结点，如下图所示

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210430005021511.png" alt="image-20210430005021511" style="zoom:80%;" />

判断是否有环，两种方法：

方法1：使用p、q两个指针，p总是向前走，但q每次都从头开始走，对于每个节点看p走的步数和q是否一样，如上图所示：当p从6走到3时，共走了6步，此时若q从出发，则q只需要走两步就到达3的位置，因而步数不相等，出现矛盾，存在环。

方法2：快慢指针，定义p、q两个指针，p指针每次向前走一步，q每次向前走两步，若在某个时刻出现 p == q，则存在环。

#### Java中的数据结构

- 常用设计模式，项目场景举例说明和具体怎样用的（要求详细讲，应该是怕背题糊弄），我主要讲了策略模式（营销策略）、享元模式（配置缓存、营销规则缓存、流水号自增实现中对缓存的使用）、单例模式（讲的静态内部类实现单例）、工厂模式（营销策略对象的生产）在项目中的使用。
- Integer缓存的数值范围（-128~127）问题（integer 127 == 127为true，integer 128 == 128为false）
- HashMap、ConcurrentHashMap

#### try catch return之后finally的代码还会执行，为什么？

```java
public class FinallyDemo2 {
    public static void main(String[] args) {
        System.out.println(getInt());
    }
 
    public static int getInt() {
        int a = 10;
        try {
            System.out.println(a / 0);
            a = 20;
        } catch (ArithmeticException e) {
            a = 30;
            return a;
            /*
             * return a 在程序执行到这一步的时候，这里不是return a 而是 return 30；这个返回路径就形成了
             * 但是呢，它发现后面还有finally，所以继续执行finally的内容，a=40
             * 再次回到以前的路径,继续走return 30，形成返回路径之后，这里的a就不是a变量了，而是常量30
             */
        } finally {
            a = 40;
            return a; //如果这样，就又重新形成了一条返回路径，由于只能通过1个return返回，所以这里直接返回40
        }
     }
}

执行结果：40
```

#### 为什么ConcurrentHashMap 底层为什么要红黑树

因为发生hash 冲突的时候，会在链表上新增节点，但是链表过长的话会影响检索效率，引入红黑树可以提高插入和查询的效率。

#### Integer的缓存

我们查看Integer的源码，就会发现里面有个静态内部类。

```java
    public static Integer valueOf(int i) {
        assert IntegerCache.high >= 127;
        //当前值在缓存数组区间段，则直接返回该缓存值
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        //否则创建新的Integer实例
        return new Integer(i);
    }
    
    private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        //IntegerCache初始化时，缓存数值为-128-127的Integer实例(默认是从-128到127)。
        static {
            // high value may be configured by property
            int h = 127;
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                int i = parseInt(integerCacheHighPropValue);
                i = Math.max(i, 127);
                // Maximum array size is Integer.MAX_VALUE
                h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
            }
            high = h;

            cache = new Integer[(high - low) + 1];
            int j = low;
            //填充缓存数组
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);
        }

        private IntegerCache() {}
    }
```

该类的作用是将数值等于-128-127(默认)区间的Integer实例缓存到cache数组中。通过valueOf()方法很明显发现，当再次创建值在-128-127区间的Integer实例时，会复用缓存中的实例，也就是直接指向缓存中的Integer实例。注意，这里的创建不包括用new创建，new创建对象不会复用缓存实例

1. Byte、Short、Integer、Long、Character都是具有缓存机制的类。缓存工作都是在静态块中完成，在类生命周期的初始化阶段执行。

2. 缓存范围？

   Byte，Short，Integer，Long为 -128 到 127

   Character范围为 0 到 127

3. Integer可以通过jvm参数指定缓存范围，其它类都不行。

   Integer的缓存上界high可以通过jvm参数-XX:AutoBoxCacheMax=size指定，取指定值与127的最大值并且不超过Integer表示范围，而下界low不能指定，只能为-128。

#### required和require_new的区别，使用场景是什么？

 1、PROPAGATION_REQUIRED：默认事务类型，如果没有，就新建一个事务；如果有，就加入当前事务。适合绝大多数情况。

  2、PROPAGATION_REQUIRES_NEW：如果没有，就新建一个事务；如果有，就将当前事务挂起。

#### 如果一个POJO类不重写equals和hashCode，你写一个怎么样的代码会让他造成异常？

从数据库查询两条相同的数据出来，但是对象是一致的，导致在JVM里面认为是不相等的。

#### 如果重写equals和hashCode，怎么样能提升他的效率？

如果不重写，则需要使用Hash算法进行判断hashCode是否相等，还会有Hash冲突的问题，重写HashCode方法让他为数据库的索引，则可以减少这一过程。

- ##### 重写equals的场景：

  当学生的姓名、年龄、性别相等时，认为学生对象是相等的（住址和体重不相等都可以）

  比如以下两行数据：

  1. **student1：**姓名：A，性别:女，年龄：18，住址：北京软件路999号，体重：48
  2. **student2：**姓名：A，性别:女，年龄：18，住址：广州暴富路888号，体重：55

  可以认为两行数据属于同一个人的，统计时要使得两行数据相等，就得重写equals：

  ```java
  public class Student {
  	private String name;// 姓名
  	private String sex;// 性别
  	private String age;// 年龄
  	private float weight;// 体重
  	private String addr;// 地址
  	// 重写hashcode方法
  	@Override
  	public int hashCode() {
  		int result = name.hashCode();
  		result = 17 * result + sex.hashCode();
  		result = 17 * result + age.hashCode();
  		return result;
  	}
   
  	// 重写equals方法
  	@Override
  	public boolean equals(Object obj) {
  		if(!(obj instanceof Student)) {
         // instanceof 已经处理了obj = null的情况
  			return false;
  		}
  		Student stuObj = (Student) obj;
  		// 地址相等
  		if (this == stuObj) {
  			return true;
  		}
  		// 如果两个对象姓名、年龄、性别相等，我们认为两个对象相等
  		if (stuObj.name.equals(this.name) && stuObj.sex.equals(this.sex) && stuObj.age.equals(this.age)) {
  			return true;
  		} else {
  			return false;
  		}
  	}
   //省略get和set
   
  }
  ```

- ##### 重写hashCode的场景：

  以上面例子为基础，即student1和student2在重写equals方法后被认为是相等的，把他们放入Map和Set中，如果想要相互覆盖，那就得重写hashCode：

  > hashcode里的代码该怎么理解？该如何写？其实有个相对固定的写法，先整理出你判断对象相等的属性，然后取一个尽可能小的正整数(尽可能小时怕最终得到的结果超出了整型int的取数范围)，这里我取了17，（好像在JDK源码中哪里看过用的是17），然后计算17*属性的hashcode+其他属性的hashcode，重复步骤。

  ```java
  // 重写hashcode方
  	@Override
  	public int hashCode() {
  		int result = name.hashCode();
  		result = 17 * result + sex.hashCode();
  		result = 17 * result + age.hashCode();
  		return result;
  	}
  ```

  > HashMap找数据就是先hashCode计算hash值再使用equals判断内容是否相等。

##### 测试以上重写：

同理，可以测试下放入HashMap中，key为<s1,s1>，<s2,s2>,Map也把两个同样的对象当成了不同的Key（Map的Key是不允许重复的，相同Key会覆盖)那么没有重写的情况下map中也会有2个元素，重写的情况会最后put进的元素会覆盖前面的value

```java
Map m = new HashMap();
	m.put(s1, s1);
	m.put(s2, s2);
	System.out.println(m);
	System.out.println(((Student)m.get(s1)).getAddr());

输出结果：
{jianlejun.study.Student@43c2ce69=jianlejun.study.Student@43c2ce69}
222
```

 可以看到最终输出的地址信息为222，222是s2成员变量addr的值，很明天，s2已经替换了map中key为s1的value值，最终的结果是map<s1,s2>。即key为s1value为s2.

##### 原理分析

因为我们没有重写父类（Object）的hashcode方法,Object的hashcode方法会根据两个对象的地址生成对相应的hashcode；

s1和s2是分别new出来的，那么他们的地址肯定是不一样的，自然hashcode值也会不一样。

Set区别对象是不是唯一的标准是，两个对象hashcode是不是一样，再判定两个对象是否equals;

Map 是先根据Key值的hashcode分配和获取对象保存数组下标的，然后再根据equals区分唯一值。

#### Java的反射慢，慢在哪里？

**1. Method#invoke 方法会对参数做封装和解封操作**

我们可以看到，invoke 方法的参数是 Object[] 类型，也就是说，如果方法参数是简单类型的话，需要在此转化成 Object 类型，例如 long ,在 javac compile 的时候 用了Long.valueOf() 转型，也就大量了生成了Long 的 Object, 同时 传入的参数是Object[]数值,那还需要额外封装object数组。

而在上面 MethodAccessorGenerator#emitInvoke 方法里我们看到，生成的字节码时，会把参数数组拆解开来，把参数恢复到没有被 Object[] 包装前的样子，同时还要对参数做校验，这里就涉及到了解封操作。

因此，在反射调用的时候，因为封装和解封，产生了额外的不必要的内存浪费，当调用次数达到一定量的时候，还会导致 GC。

**2. 需要检查方法可见性**

通过上面的源码分析，我们会发现，反射时每次调用都必须检查方法的可见性（在 Method.invoke 里）

**3. 需要校验参数**

反射时也必须检查每个实际参数与形式参数的类型匹配性（在NativeMethodAccessorImpl.invoke0 里或者生成的 Java 版 MethodAccessor.invoke 里）；

**4. 反射方法难以内联**

Method#invoke 就像是个独木桥一样，各处的反射调用都要挤过去，在调用点上收集到的类型信息就会很乱，影响内联程序的判断，使得 Method.invoke() 自身难以被内联到调用方。参见 [http://www.iteye.com/blog/rednax](https://link.zhihu.com/?target=http%3A//www.iteye.com/blog/rednax)…

**5. JIT 无法优化**

在 JavaDoc 中提到：

> Because reflection involves types that are dynamically resolved, certain Java virtual machine optimizations can not be performed. Consequently, reflective operations have slower performance than their non-reflective counterparts, and should be avoided in sections of code which are called frequently in performance-sensitive applications.

因为反射涉及到动态加载的类型，所以无法进行优化。

如下图，反射导致的性能问题是否严重跟使用的次数有关系。如果控制在100次以内，基本上没什么差别；如果调用次数超过了100次，性能差异会很明显；

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210507203054234.png" alt="image-20210507203054234" style="zoom:80%;" />

#### 如何优雅的写代码？什么代码算做优雅？什么代码是规范？你们代码规范是什么样的？



#### 为什么要用stream？并发场景使用stream有做压测吗？

stream使用：

```java
List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
//过滤用法
count = strings.stream().filter(string->string.isEmpty()).count();
System.out.println("空字符串数量为: " + count);
        
count = strings.stream().filter(string -> string.length() == 3).count();
System.out.println("字符串长度为 3 的数量为: " + count);
//遍历用法
 intList.stream().forEach(i -> {
            i.intValue();
        });
//stream的方法：
			stream()；为集合创建串行流。
			parallelStream(),为集合创建并行流。是流并行处理程序的代替方法。
			forEach(),Stream提供的新的方法来迭代流中的每个数据。
			map(),方法用于映射每个元素到对应的结果。map(i -> i*i)集合中的每个元素变为平方
			filter(),方法用于通过设置的条件过滤出元素，filter(string -> string.isEmpty()) 过滤出空字符串。
			limit(),方法用于获取指定数量的流。limit(10) 获取10条数据
			sorted(),方法用于对流进行排序。
			collect(Collectors.toList()),用于返回列表或字符串，Collectors.joining(",");将集合转换成逗号隔开的字符串
```

##### Stream的适合场景

- **集合操作超过两个步骤**
  比如先filter再for each
  这时Stream显得优雅简洁，效率也高
- **任务较重，注重效能，希望并发执行**
  很容易的就隐式利用了多线程技术。非常好的使用时机。
- **函数式编程的编码风格里**
  Stream的设计初衷之一



#### 简述一下你了解的设计模式：



#### 如何进行code review？

#### .java文件都包含什么？.class文件都包括什么？

**查看.class文件内容命令：**javap -verbose

原.java文件：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210507210348154.png" alt="image-20210507210348154" style="zoom:80%;" />

查看.class文件：

<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210507210259133.png" alt="image-20210507210259133" style="zoom:80%;" />

#### 你对java什么感受

#### git的merge和rebase命令的区别

#### hashmap负载因子为什么是0.75

Hashtable 和HashMap扩容对比：

- **Hashtable 初始容量是11 ，扩容 方式为2N+1;**

- **HashMap 初始容量是16,扩容方式为2N;**　　

  提高空间利用率和减少查询成本的**折中**，主要是泊松分布，0.75的话碰撞最小。

  加载因子过高，例如为1，虽然减少了空间开销，提高了空间利用率，但同时也增加了查询时间成本（因为Hash碰撞的深度越来越大）；

  加载因子过低，例如0.5，虽然可以减少查询时间成本，但是空间利用率很低，同时提高了rehash操作的次数。





- 属性转换推荐直接直接定义get/set转换类+IDEA 插件自动填充 get / set 函数。

-  commons 包的 BeanUtils 进行属性拷贝性能较差。

- Spring 的 BeanUtils 性能好很多，但是存在问题：

  接下来我们看 Spring 的 BeanUtils 的属性拷贝会存在啥问题

  ```java
  import lombok.Data;
  import java.util.List;
  
  @Data
  public class A {
      private String name;
  
      private List<Integer> ids;
  }
  ```

  ```java
  @Data
  public class B {
      private String name;
  
      private List<String> ids;
  }
  ```

  ```java
  import org.springframework.beans.BeanUtils;
  
  import java.util.Arrays;
  
  public class BeanUtilDemo {
      public static void main(String[] args) {
          A first = new A();
          first.setName("demo");
          first.setIds(Arrays.asList(1, 2, 3));
  
          B second = new B();
          BeanUtils.copyProperties(first, second);
          for (String each : second.getIds()) {// 类型转换异常
              System.out.println(each);
          }
      }
  }
  ```

  大家运行上述示例时，会发生类型转换异常。

  打断点可以看到，属性拷贝之后 B 类型的 second 对象中 ids 仍然为 Integer 类型：

  

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210517172935451.png" alt="image-20210517172935451" style="zoom:80%;" />

  

  如果不转换为字符串，直接进行打印，并不会报错。

- 接下来我们看下 mapstruct：

  ```java
  import org.mapstruct.Mapper;
  import org.mapstruct.factory.Mappers;
  
  @Mapper
  public interface Converter {
      Converter INSTANCE = Mappers.getMapper(Converter.class);
  
      B aToB(A car);
  }
  ```

  ```java
  import java.util.Arrays;
  
  public class BeanUtilDemo {
      public static void main(String[] args) {
          A first = new A();
          first.setName("demo");
          first.setIds(Arrays.asList(1, 2, 3));
  
          B second = Converter.INSTANCE.aToB(first);
          for (String each : second.getIds()) {// 正常
              System.out.println(each);
          }
      }
  }
  ```

  可以成功的将 A 中 List<Integer> 转为 B 中的 List<String> 类型。

  我们看下编译生成的 Converter 实现类：

  ```java
  import java.util.ArrayList;
  import java.util.List;
  import javax.annotation.Generated;
  import org.springframework.stereotype.Component;
  
  @Generated(
      value = "org.mapstruct.ap.MappingProcessor",
      comments = "version: 1.3.1.Final, compiler: javac, environment: Java 1.8.0_202 (Oracle Corporation)"
  )
  @Component
  public class ConverterImpl implements Converter {
  
      @Override
      public B aToB(A car) {
          if ( car == null ) {
              return null;
          }
  
          B b = new B();
  
          b.setName( car.getName() );
          b.setIds( integerListToStringList( car.getIds() ) );
  
          return b;
      }
  
      protected List<String> integerListToStringList(List<Integer> list) {
          if ( list == null ) {
              return null;
          }
  
          List<String> list1 = new ArrayList<String>( list.size() );
          for ( Integer integer : list ) {
              list1.add( String.valueOf( integer ) );
          }
  
          return list1;
      }
  }
  
  ```

  自动帮我们进行了转换，我们可能没有意识到类型并不一致。

  **但是：**

  如果我们在 A 类中添加一个 String number 属性，在 B 类中添加一个 Long number 属性，使用 mapstruect 当 number 设置为非数字类型时就会报 NumberFormatException。

  ```java
  
  @Override
  public B aToB(A car) {
      if ( car == null ) {
          return null;
      }
  
      B b = new B();
  
      b.setName( car.getName() );
      if ( car.getNumber() != null ) { // 问题出在这里
          b.setNumber( Long.parseLong( car.getNumber() ) );
      }
      b.setIds( integerListToStringList( car.getIds() ) );
  
      return b;
  }
  ```

  使用 cglib 默认则不会映射 number 属性，B 中的 number 为 null。

- 如果手动定义转换器，使用 IDEA 插件(如 generateO2O)自动转换：

  ```java
  public final class A2BConverter {
  
      public static B from(A first) {
          B b = new B();
          b.setName(first.getName());
          b.setIds(first.getIds());
          return b;
      }
  }
  ```

  在编码阶段就可以非常明确地发现这个问题：

  <img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210517173830003.png" alt="image-20210517173830003" style="zoom:80%;" />

  ##### 总结：

  -  commons 包的 BeanUtils 进行属性拷贝性能较差。
  - Spring 的 BeanUtils 性能好很多，但是private List<Integer> ids;转为private List<String> ids;只有运行才会报错。
  -  mapstruct会自动将Integer转换为String，但问题是如果将String转为Number，String如果是字母的话就会报错（而手动定义转换器在编译期就会报错）。



<img src="C:\Users\12031\AppData\Roaming\Typora\typora-user-images\image-20210517172259108.png" alt="image-20210517172259108" style="zoom:80%;" />