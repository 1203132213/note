**前序：**最近开始研究Hadoop平台的搭建，故在本机上安装了VMware workstation pro，并创建了Linux虚拟机（centos系统），为了方便本机和虚拟机间的切换，准备使用Xshell直接连接Linux虚拟机。在此之前，需要对Linux虚拟机的网络进行一定的配置。故有了下面这段记录。

步骤：

1. 检查Linux虚拟机的网络连接模式，确保它是NAT模式。（由于只在本机进行连接，所以没有选择桥接模式。当然，桥接模式的配置会有所不同，在此不做深入分析）

![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813170542499-2065629315.png)

2. 在VMware workstation里，点击菜单栏上的【编辑】-->【虚拟网络编辑器】，打开下方的虚拟网络编辑器。选择VMnet8（NAT模式），取消勾选【使用本地DHCP服务...】（若勾选，会设置动态IP）。 

![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813171106026-2003072125.png)

 

3. 在下图中，点击NAT设置。

![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813171648211-1519052532.png)

 

 ![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813171738212-1581868577.png)

记住上图中的子网IP范围，如上图所示表示虚拟机在192.168.44.0~192.168.44.255范围内。

 注：上图中192.168.44.2为网关地址，192.168.44.255为广播地址，192.168.44.0一般为网段IP，所以0,2,255这三个地址不能设置。

4. 设置虚拟机的IP、DNS和主机名

1）设置IP地址、子网掩码和网关，如下图。

vi /etc/sysconfig/network-scripts/ifcfg-ens33 （*根据实际情况不同，本文为ens33）

![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813173908877-549239084.png)

```
BOOTPROTO=static
IPADDR=192.168.80.3
NETMASK=255.255.255.0
GATEWAY=192.168.80.2
ONBOOT=yes
```

ONBOOT：是指系统启动时是否激活网卡，默认为no，设置为yes，表示开机启动时激活网卡。

`BOOTPROTO`：网络分配方式，静态。（一定记得修改为Static，否则无法连通网络）

`IPPADDR`：手动指定ip地址。

`NETMASK`：子网掩码。

`GATEWAY`：网关ip。

2）设置DNS

\#vi /etc/resolv.conf

nameserver 192.168.80.2

![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813173519373-1492500515.png)

3）设置主机名

\#vi /etc/sysconfig/network

![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813173610127-1038530508.png)

```
NETWORKING=yes
HOSTNAME=master

NETWORKING=yes
HOSTNAME=slave1

NETWORKING=yes
HOSTNAME=slave2

NETWORKING=yes
HOSTNAME=mha
```

#vim /etc/hostname

```
hadoop01
```

4） 上述文件配置成功后，重启虚拟机或使用service network restart命令重启网卡。

\#reboot

5. 设置VMnet8在Windows上的IP属性，如下图。（打开控制面板-->网络和Internet，在右侧点击【更改适配器设置】，进入“网络连接”页面）

![img](https://img2018.cnblogs.com/blog/550771/201901/550771-20190117090729695-1357957998.png)

 

![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813174711386-1324164565.png)

 

6. 配置完上述步骤后就可以成功使用Xshell连接Linux虚拟机了。

 ![img](https://images2018.cnblogs.com/blog/550771/201808/550771-20180813174836736-1276399423.png)

7. 小插曲：vim和ifconfig命令找不到时，使用yum install net-tools/vim安装即可。

\#yum install -y net-tools

\#yum install -y vim



















 systemctl start etcd

 systemctl start kube-apiserver

 systemctl start kube-controller-manager

systemctl start kube-scheduler

 systemctl start kubelet

systemctl start kube-proxy

