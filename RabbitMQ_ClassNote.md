# RabbitMQ简介

> MQ【message queue】，本质是一个队列，遵循 【FIFO】 先入先出原则，只不过队列中存放的内容是message 而已；是一种跨进程的通信机制，用于上下游【消息发送方和消息接收方】传递消息。
>
> MQ 是互联网架构中一种非常常见的上下游“逻辑解耦+物理解耦”的消息通信服务。使用了 MQ 之后，消息发送上游只需要依赖 MQ，不用依赖其他服务。  

1. RabbitMQ是流行的消息队列服务软件，是开源的AMQP（高级消息队列协议）实现。

   + 支持Java、Python、C、PHP、Ruby、JavaScript等多种客户端，

   + 用于在分布式系统中存储转发消息，可以实现异步处理、流量削峰、系统解耦，在易用性、扩展性、高可用等方面表现优异。
2. 课程采用RabbitMQ 3.8.8版本，课程内容包括

   + RabbitMQ的环境搭建、

   + 消息的发送与接收、消息确认、

   + 延迟队列、死信队列、优先队列、惰性队列、

   + 与SpringBoot集成、

   + RabbitMQ集群

## MQ消息队列

1. MQ的引用场景

   + 流量消峰

     > 如果订单系统最多每秒能处理一万次订单，超过这个阈值系统可能崩溃，在高峰期，如果有两万次下单操作系统是处理不了的，只能限制订单超过一万后不允许用户下单。使用消息队列做缓冲，可以取消这个限制，把一秒内下的订单分散成一段时间来处理，这时有些用户可能在下单十几秒后才能收到下单成功的操作，但是比不能下单的体验要好。  

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/04/46839b9b734044b1b1f361cff9121a6bmq流量消峰.png)

   + 应用解耦

     > 电商应用中有订单系统、库存系统、物流系统、支付系统。用户创建下单后，如果订单系统耦合调用库存系统、物流系统、支付系统，任何一个子系统出了故障，都会造成下单操作异常。
     >
     > 当转变成基于消息队列的方式后，下订单任务完整会直接结束，并将订单消息传递给消息队列，由消息队列来调用并监督被调用系统的执行。系统间调用的问题会减少很多，比如物流系统因为发生故障，需要几分钟来修复。在这几分钟的时间里，物流系统要处理的内存被缓存在消息队列中，用户的下单操作可以正常完成。当物流系统恢复后，继续处理订单信息即可，中单用户感受不到物流系统的故障， 提升系统的可用性  

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/04/5a0568232dbf41eda1787830dd791df4应用解耦.png)

   + 异步处理

     > 【服务之间不需要等待，等当前服务执行结束后会自动通知服务调用者获取数据继续下一步的操作】

     > 有些服务间调用是异步的，例如 A 调用 B， B 需要花费很长时间执行，但是 A 需要确认 B执行完成的时间以获取执行结果并继续执行后续操作，以前一般有两种方式，都不是很优雅
     >
     > + A 过一段时间去调用 B 的查询 api 查询。
     > + 或者 A 提供一个 callback api，B 执行完之后调用 api 通知 A 服务。
     >
     > 使用消息总线可以方便地解决这个问题，A 调用 B 服务后，只需要监听 B 处理完成的消息，当 B 处理完成后，会发送一条消息给 MQ， MQ 会将此消息转发给 A 服务。这样 A 服务既不用循环调用 B 的查询 api，也不用提供 callback api。同样 B 服务也不用做这些操作。 A 服务还能及时的得到异步处理成功的消息。

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/04/34ba23dc1b21442ba186a588e19965f6异步处理.png)

     

2. MQ的分类

   + ActiveMQ

     > 很老的MQ，apache开发的

     + 优点：单机吞吐量万级，时效性 ms 级，可用性高，基于主从架构实现高可用性，消息可靠性较高，丢失数据的概率很低
     + 缺点：官方社区现在对 ActiveMQ 5.x 维护越来越少， 高吞吐量场景较少使用。  

   + Kafka  

     > Kafka主要特点是基于 Pull 的模式来处理消息，追求高吞吐量，是大数据领域内的消息传输杀手锏，专为大数据而生的消息中间件，以其百万级 TPS 的吞吐量名声大噪，大数据领域的宠儿，在数据采集、传输、存储的过程中有着举足轻重的作用。已经被 LinkedIn，Uber，Twitter，Netflix 等采纳。

     + 优点: 
       + 卓越的优点就是吞吐量高，单机写入 TPS 约在百万条/秒。时效性 ms 级可用性非常高， 
       + kafka 是分布式的，一个数据多个副本，少数机器宕机，不会丢失数据，不会导致不可用，消费者采用 Pull 方式获取消息, 消息有序, 通过控制能够保证所有消息被消费且仅被消费一次;
       + 有优秀的第三方Kafka Web 管理界面 Kafka-Manager；
       + 日志领域成熟；功能较为简单，主要支持简单的 MQ 功能，在大数据领域的实时计算以及日志采集被大规模使用
     + 缺点：
       +  Kafka 单机超过 64 个队列/分区， Load 会发生明显的飙高现象，队列越多， load 越高，发送消息响应时间变长， 使用短轮询方式，实时性取决于轮询间隔时间， 消费失败不支持重试； 
       + 支持消息顺序，但是一台代理宕机后，就会产生消息乱序，
       +  社区更新较慢；  

   + RocketMQ  

     > RocketMQ 出自阿里巴巴的开源产品，用 Java 语言实现，在设计时参考了 Kafka，并做出了自己的一些改进。被阿里巴巴广泛应用在订单，交易，充值，流计算，消息推送，日志流式处理， binglog 分发等场景。

     + 优点:
       + 单机吞吐量十万级，可用性非常高，分布式架构，消息可以做到 0 丢失，MQ 功能较为完善，还是分布式的，扩展性好,支持 10 亿级别的消息堆积，不会因为堆积导致性能下降，
       + 源码是 java 我们可以自己阅读源码，定制自己公司的 MQ
     + 缺点： 
       + 支持的客户端语言不多，目前是 java 及 c++，其中 c++不成熟；
       + 社区活跃度一般，没有在 MQ核心中去实现 JMS 等接口，有些系统要迁移需要修改大量代码  

   + RabbitMQ  

     > 2007 年发布，是一个在 AMQP【高级消息队列协议】基础上完成的，可复用的企业消息系统，是当前最主流的消息中间件之一
     >
     > https://www.rabbitmq.com/news.html

     + 优点:
       + 由于 erlang 语言的高并发特性，性能较好； 吞吐量到万级， MQ 功能比较完备,健壮、稳定、易用、跨平台、 支持多种语言 如： Python、 Ruby、 .NET、 Java、 JMS、 C、 PHP、 ActionScript、 XMPP、 STOMP等，支持 AJAX 文档齐全；
       + 开源提供的管理界面非常棒，用起来很好用,
       + 社区活跃度高； 更新频率相当高
     + 缺点：商业版需要收费,学习成本较高  

3. MQ的选择

   + Kafka

     > 用于日志收集和传输，适合产生大量数据的互联网服务的数据收集业务。 
     >
     > 大型公司建议可以选用，如果有日志采集功能，肯定是首选 kafka 了。  

   + RocketMQ  

     > 为金融互联网领域而生，对于可靠性要求很高的场景，尤其是电商订单扣款，业务削峰。
     >
     > RoketMQ 在稳定性上更值得信赖，这些业务场景在阿里双 11 已经经历了多次考验，如果你的业务有上述并发场景，建议选择 RocketMQ。  

   + RabbitMQ

     > 结合 erlang 语言本身的并发优势，性能好时效性微秒级， 社区活跃度也比较高，管理界面用起来十分方便， 
     >
     > 如果你的数据量没有那么大， 中小型公司优先选择功能比较完备的 RabbitMQ。  



## RabbitMQ介绍

> RabbitMQ 是一个消息中间件：它接受并转发消息。类比于快递站，消息类比为包裹，RabbitMQ就是快递站，快递站接收，存储和转发消息数据，将数据送到用户手里

![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/812a70d2bbce4143b0b577dffa727ae0消息中间件.png)

1. 四大核心概念

   + 生产者

     > 生产者是产生数据发送消息给消息中间件的程序【服务】

   + 交换机

     > 交换机是 RabbitMQ内部的一个重要部件，一方面接收来自生产者的消息，另一方面将消息推送到队列中。
     >
     > 一个消息中间件可以有多个交换机，每个交换机可以绑定多个队列
     >
     > 交换机必须明确接收到的消息的处理逻辑，是将这些消息推送到特定队列还是推送到多个队列，亦或者是把消息丢弃，这个由交换机的类型决定  

   + 队列

     > 队列是 RabbitMQ 内部使用的一种数据结构， 尽管消息流经在 RabbitMQ 和应用程序之间，但它们只能存储在队列中。队列仅受主机的内存和磁盘限制的约束，本质上是一个大的消息缓冲区。许多生产者可以将消息发送到一个队列，许多消费者可以尝试从一个队列接收数据。这就是该队列的使用方式 
     >
     > 一个消息中间件中可以有多个消息队列，一个交换机与多个消息队列绑定，每个队列对应一个消费者，多个消费者虽然可以对应同一个队列，但是队列中的消息只会发送给这些消费者中的其中一个

   + 消费者

     > 消费者是一个等待接收消息的程序。 注意生产者，消费者和消息中间件很多时候并不在同一机器上。
     >
     > 同一个应用程序既可以是生产者又是可以是消费者。 

2. RabbitMQ的核心部分

   > RabbitMQ的六大模式

   + 简单模式【Hello World!】
   + 工作模式【Work queues】
   + 发布订阅模式【Publish/Subscribe】
   + 路由模式【Routing】
   + 主题模式【Topics】
   + 发布确认模式【Publisher Confirm】

   

3. RabbitMQ的工作原理

   【原理图】

   > + 黄色部分Broker是RabbitMQ的一个实体，Broker意为中间人、经纪人，表示接受和分发消息的应用，可以是RabbitMQ的服务器，也被称为Message Broker【Exchange是一个消息中间件中的多个交换机，Queue是队列】
   >
   > + Virtual host：出于多租户和安全因素设计的，把 AMQP【高级消息队列协议】 的基本组件划分到一个虚拟的分组中，类似
   >   于网络中的 namespace 概念。当多个不同的用户使用同一个 RabbitMQ server 提供的服务时，可以划分出多个 vhost，每个用户在自己的 vhost 创建多个 exchange／ queue 等  
   >   + 多租户：每个Broker中可以包含多个Virtual host，每个Virtual host中可以包含多个交换机和队列
   > + Connection： 生产者或消费者与消息中间间之间的 TCP 连接  
   > + Channel表示信道，每个生产者会与MQ建立连接，建立一个TCP连接的开销非常大，效率低；Channel 是在 connection 内部建立的逻辑连接，TCP连接中可以创建多个Channel，如果应用程序支持多线程，通常每个线程会创建单独的信道进行通讯， AMQP method 包含了 channel id 帮助客户端和消息中间件识别信道，所以信道之间是完全隔离的。channel的设计也是为了减少操作系统建立TCP连接的开支，消费这通过信道直接连接交换机，交换机再连接队列   
   > + Exchange： 消息到达消息队列的第一站，根据分发规则，匹配查询表中的 routing key，分发消息到队列中去。常用的交换机类型有： direct (point-to-point), topic (publish-subscribe) and fanout(multicast)  
   > + Queue： 消息最终被送到这里等待 consumer 取走  
   >
   > + Producer是生产者
   >
   > + Consumer表示消费者，即消息的接收方
   > + Binding：就是交换机与队列间的连线

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/ea6194a5e7de4c82848c789f4bf110a1RabbitMQ的工作原理.png)

   

## RabbitMQ安装

> 官网：https://www.rabbitmq.com/download.html  
>
> RabbitMQ的运行需要Erlang语言的运行环境，RabbitMQ用的最多的是linux系统的，RabbitMQ的版本需要对应linux系统的版本，使用命令`uname -a`查看当前linux系统的版本。el7表示linux7

### 安装步骤

1. 将以下文件上传至`/opt/rabbitmq`目录下

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/2013d6ffc7d84c68a8fb23c738d8917drabbitmq安装包.png)

2. 将以下两个文件移动到/usr/local/rabbitmq目录下

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/49113c81478c488d915595342e748fd5rabbitmq安装目录.png)

3. 使用以下命令安装对应软件

   + 使用命令`rpm -ivh erlang-21.3-1.el7.x86_64.rpm  `安装erlang环境【i表示安装，v表示显示安装进度】

   + 使用命令`yum install socat -y`【安装rabbitmq需要安装rabbitmq的依赖包socat】

     > yum命令需要去互联网联网下载安装包

   + 使用命令`rpm -ivh rabbitmq-server-3.8.8-1.el7.noarch.rpm  `安装rabbitmq



### 安装成功测试

1. 使用命令`chkconfig rabbitmq-server on`设置rab bitmq服务开机启动

2. 使用命令`/sbin/service rabbitmq-server start`手动启动rabbitmq服务

3. 使用命令`/sbin/service rabbitmq-server status`查看rabbitmq服务状态【如果服务是启动状态active会显示running，正在启动会显示activing，inactive表示服务已经关闭】

4. 使用命令`/sbin/service rabbitmq-server stop`停止rabbitmq服务

5. 在rabbitmq服务关闭的状态下使用命令`rabbitmq-plugins enable rabbitmq_management  `安装rabbitmq的web管理插件【执行了该命令才能通过浏览器输入地址`http://主机地址:rabbitmq端口号15672`访问rabbitmq管理界面，访问rabbitmq需要开启防火墙端口通讯，RabbitMQ本身的端口是5672,15672是管理界面的端口】

   > 初始账号和密码默认都是guest，第一次登录会显示没有用户只能通过本地登录，此时需要添加一个账户进行远程登录

   【开放rabbitmq防火墙端口通讯】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/c64e1f726d48432486a5fd84511c5ecd开放rabbitmq端口.png)

   【web控制台】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/0263f00cc0e64946a0808484f42505daweb控制台.png)

6. 使用命令`systemctl status firewalld`查看防火墙状态

7. 使用命令`systemctl stop firewalld`关闭防火墙

8. 使用命令`systemctl disable firewalld.service`可以设置防火墙下次开机也不会自动启动

9. 添加用户并设置超级管理员权限以登录web控制台

   + 使用命令`rabbitmqctl add_user earl 123456`创建账户，账户名earl，密码123456

   + 使用命令`rabbitmqctl set_user_tags earl administrator  `设置用户earl的角色为超级管理员

   + 使用命令`rabbitmqctl set_permissions -p "/" earl ".*" ".*" ".*"  `设置用户权限

     > `[-p <vhostpath>] <user> <conf> <write> <read>  `；-p <vhostpath>表示设置vhost的路径，conf表示可以配置哪些资源，user表示用户，write表示写权限、read表示读权限
     >
     > 上个命令的意思表示对于用户earl设置具有对/vhost1这个virtual host中的所有资源的配置、写、读权限；每个vhost代表一个库，不同vhost中的交换机和队列是不同的
     >
     > guest访问不了就是因为没有设置"/"vhost的路径

   + 使用命令`rabbitmqctl list_users`查看当前rabbitmq server有哪些用户

   

   【MQ的后台管理界面】

   > admin路由中就可以增删改查用户

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/e8a8bbf4bd57430282392a7e9ad1723frabbitmq控制台.png)






# 简单模式

> 以下演示的就是简单队列模式
>
> 【结构图】
>
> ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/5fe26138608d4032898796c7ca09058c业务逻辑.png)
>
> 在RabbitMQ的安装中已经将MQ【消息缓冲区】安装好了，现在使用Java API实现生产者发送单个消息给消息队列，消息队列获取消息转发给消费者并打印消息，实现消息的通信
>
> 创建项目rabbitmq-demo，创建模块01
>
> 使用云服务器的一定要把 5627这个端口号打开，5672端口的作用是用于tcp连接；15672的作用是用于http连接。 同时在建立连接时默认端口号是5627 所以在创建连接时不用指定【服务器必须开启5672端口，本地主机linux系统也需要开启5672端口才能访问，否则会连接超时】
>
> commons-io是apache基金会下的

1. 01模块搭建

   + pom.xml

     ```xml
     <dependencies>
         <!--rabbitmq 依赖客户端-->
         <dependency>
             <groupId>com.rabbitmq</groupId>
             <artifactId>amqp-client</artifactId>
             <version>5.8.0</version>
         </dependency>
         <!--操作文件流的一个依赖,apache基金会下的-->
         <dependency>
             <groupId>commons-io</groupId>
             <artifactId>commons-io</artifactId>
             <version>2.6</version>
         </dependency>
     </dependencies>
     
     <!--指定 jdk 编译版本-->
     <build>
         <plugins>
             <plugin>
                 <groupId>org.apache.maven.plugins</groupId>
                 <artifactId>maven-compiler-plugin</artifactId>
                 <configuration>
                     <source>8</source>
                     <target>8</target>
                 </configuration>
             </plugin>
         </plugins>
     </build>
     ```

   + Producer

     ```java
     public class Producer {
         //设置队列的名称
         private final static String QUEUE_NAME = "hello";
         public static void main(String[] args) throws Exception {
             //创建一个连接工厂
             ConnectionFactory factory = new ConnectionFactory();
             //设置连接工厂创建连接对象的相关属性，不是Spring应用，用不了配置文件配置属性值，但是好像可以用BufferReader读
             factory.setHost("192.168.200.132");
             factory.setUsername("earl");
             factory.setPassword("123456");
             //channel 实现了自动 close 接口 自动关闭 不需要显示关闭，是try()中的连接对象自动关闭把，记得jdbc还是哪儿学过
             try(
                 //通过连接工厂创建连接对象
                 Connection connection = factory.newConnection();
                 //通过连接对象创建信道
                 Channel channel = connection.createChannel()
             ) {
                 /**
                  * 通过信道生成一个消息队列声明
                  * MQ有被动创建队列的功能，只要有消费者监听某个队列后，如果这个队列不存在，MQ就会自动的创建这个队列，交换机也是同理
                  * 参数解释
                  * 1.队列名称
                  * 2.队列里面的消息是否持久化 默认消息存储在内存中，持久化是存在磁盘中，服务重新启动时该队列还会存在
                  * 3.exclusion表示该队列是否只供一个消费者进行消费 该队列是否进行多消费者共享 true表示可只能被一个消费者消费，这个是看源码注释说的，课程讲错了
                  * 4.是否自动删除 最后一个消费者断开连接以后 该队列是否自动删除 true 自动删除
                  * 5.队列的其他参数，不设置其他参数直接传递null就可以，初学没必要设置，如延迟消息，死信消息
                  */
                 channel.queueDeclare(QUEUE_NAME,false,false,false,null);
                 //准备消息内容
                 String message="hello world";
                 /**
                  * 发送一个消息
                  * 1.发送到那个交换机，不考虑交换机问题直接传入空串
                  * 2.路由的 key 是哪个，本次直接写队列名
                  * 3.其他的参数信息，本次没有，直接写null
                  * 4.发送消息的消息体，把字符串转换成byte数组
                  */
                 channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
                 System.out.println("消息发送完毕");
             }
         }
     }
     ```

2. 测试效果

   > 必须开放linux的5627端口和15672端口，web控制台访问只需要开启15672端口

   【生成的队列】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/7138718c0bd54627b0452e02bb7c222aMQ的hello队列.png)

   【消息情况】

   > 一条消息处于就绪状态准备被消费，总消息为1条

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/4e7a69b51a7b4cc987df68f9d36cfcec消息队列消息情况.png)

3. 消费者代码

   > 可以写在同一个包下【同一个服务中】，发送和接收消息都是通过主函数执行的

   + Consumer

     ```java
     public class Consumer {
         private final static String QUEUE_NAME = "hello";
         public static void main(String[] args) throws Exception {
             ConnectionFactory factory = new ConnectionFactory();
             factory.setHost("192.168.200.132");
             factory.setUsername("earl");
             factory.setPassword("123456");
             Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
             System.out.println("等待接收消息....");
             //推送的消息如何进行消费的接口回调，lambda表达式，接收到消息后对消息的处理方法，这里是方法定义，会被basicConsume调用
             //(consumerTag, delivery)->{}是lambda表达式，括号中是实参列表，连参数类型都不需要写，单个参数连小括号都不用写，大括号中写具体的方法，这个会自动实例对应接口的实现了handle方法的接口
             //看到两个函数式接口中都只有一个handle方法
             DeliverCallback deliverCallback=(consumerTag, delivery)->{
                 System.out.println(delivery);//delivery表示一个消息，包含消息头，消息属性，消息体；消息内容放在消息体中
                 String message= new String(delivery.getBody());
                 System.out.println(message);
             };
             //消费消息中断的一个回调接口 如在消费的时候队列被删除掉了
             CancelCallback cancelCallback=(consumerTag)->{
                 System.out.println("消息消费被中断");
             };
             /**
              * 信道对象的basicConsume是消费者消费消息接受消息
              * 1.消费哪个队列
              * 2.消费成功之后是否要自动应答 true 代表自动应答 false 手动应答，后面会讲
              * 3.消费者未成功消费的回调
              *
              * 获取到消息会调用DeliverCallback函数，获取不到消息会调用CancelCallback函数
              * DeliverCallback是一个函数式接口，用注解@FunctionalInterface标注，函数式接口不能实例化，需要使用匿名内部类或者lambda表达式写一个对应接口的实现类
              */
             channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
         }
     }
     ```

     【消费消息】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/b167ff6823dd4660888a23e5aa4cbfec消费者接收消息.png)







# 工作队列

> Work Queues【任务队列】,消息被多个工作线程接收，工作线程采用轮询的策略抢夺消息，一个消息只会被处理一次
>
> 就是生产者发送了大量消息，此时可能存在多个消费者一起来处理这些消息，这些消费者称为工作线程，这些工作线程采用轮询的策略获取竞争这些消息并同时对消息进行处理

1. 工作队列结构图

> 工作线程就是消费者，改了一个名字，多个工作线程
>
> 竞争关系是说其中一个工作线程抢到了某个消息，其他线程将无法抢夺该消息

![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/b5d4c0d9013144eaacc832cf9e7a83f6工作队列.png)



2. 工作队列的代码实现

   > 生产者大量发送消息，两个工作线程去接收消息，观察两个工作线程的轮询接受消息
   >
   > 注意，消费者一定不能用junit的测试接口写，否则没有监听的效果
   >
   > 为了代码复用，把信道创建的代码封装成一个工具类
   >
   > 两个类的代码相同或者代码基本相同，可以选择EditConfigurations选择Allow parallel run【idea老版本】或者modify option中找到Allow multiple instance【idea新版本】，勾选表示允许一个类启动在不同的进程【？确认是进程还是线程】
   >
   > 显示的效果是生产者发送带序号的消息，会轮询的被两个工作线程接收

   + 工具类

     > 封装获取信道的工具类，本例中的每个工作线程即便使用静态代码块都会使用一个全新的连接，这个怎么弄成一个呢

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 用来测试生产者和消费者能否使用同一个连接，经过测试，生产者和消费者可以使用同一个连接对象
      * 疑惑这应该不是同一个连接对象，每个应用启动都应该使用了新的连接对象，和静态代码块没关系，因为都是单独编译执行的
      * @创建日期 2023/11/05
      * @since 1.0.0
      */
     public class RabbitMQUtil {
         public static Connection connection;
         /*静态代码块让连接对象只实例化一次*/
         static {
             ConnectionFactory factory = new ConnectionFactory();
             factory.setHost("192.168.200.132");
             factory.setUsername("earl");
             factory.setPassword("123456");
             try {
                 Connection mqConnection = factory.newConnection();
                 connection=mqConnection;
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (TimeoutException e) {
                 e.printStackTrace();
             }
         }
         public static Channel getChannel() throws IOException {
             System.out.println("connection对象:"+connection);
             return connection.createChannel();
         }
     }
     ```

   + 消费者

     > 注意<font color=red>消费者的channel对象不能写在try后面的括号中</font>，否则无法获取消息队列中的消息，可以写在try语句块的大括号中；<font color=red>生产者的Channel对象可以写在try的小括号中</font>
     >
     > 回调函数必须定义在方法的大括号中

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 工作线程1号，相当于之前的消费者
      * @创建日期 2023/11/05
      * @since 1.0.0
      */
     public class WorkThread1 {
         /**
          * 对列名称为hello
          */
         public static final String QUEUE_NAME="hello";
     
         public static void main(String[] args){
             DeliverCallback deliverCallback=(consumerTag, delivery)->{
                 String message = new String(delivery.getBody());
                 System.out.println("WT1"+message);
             };
             CancelCallback cancelCallback=consumerTag->{
                 System.out.println(consumerTag+"WT1消费消息失败接口回调逻辑");
             };
             try{
                 Channel channel = RabbitMQUtil.getChannel();
                 System.out.println("WT1等待接收消息");
                 //消息接收
                 channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     ```

     【开启如下配置就可以简单修改参数将该类以另一个类的形式启动】

     > 进行该项配置后就可以根据WorkThread1修改WT1/WT2分别启动实现分别启动两个主函数的效果
     >
     > 效果在测试效果中有展示

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/fbb4ea556f934e1ca408486e01e81e69开启一个类的多个运行进程.png)

   + 生产者

     > 将控制台输入的消息传递给消息队列

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 发送大量有序消息的生产者
      * @创建日期 2023/11/05
      * @since 1.0.0
      */
     public class Producer1 {
         private static final String QUEUE_NAME="hello";
         public static void main(String[] args)  {
             try(Channel channel= RabbitMQUtil.getChannel()) {
                 channel.queueDeclare(QUEUE_NAME,false,false,false,null);
                 //从控制台当中接受信息
                 Scanner scanner = new Scanner(System.in);
                 //如果扫描到下一个输入就进入循环，获取控制台的消息并发送给消息队列
                 while (scanner.hasNext()){
                     String message = scanner.next();
                     channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
                     System.out.println("发送消息完成:"+message);
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (TimeoutException e) {
                 e.printStackTrace();
             }
         }
     }
     ```

   + 测试效果

     > 工作队列采用轮询的策略处理消息

     【web控制台】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/06f0fc234ab349a7a9bc48d33a148d91消息发送面板情况.png)

     【消息发送】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/2e035fddc1024a788255bdca1ff4a17c发送消息到消息队列.png)

     【消息接收】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/c510f97381a34b95b1290e7e734f96f7工作线程按轮询策略接受消息.png)

     【消息接收工作线程2】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/644ace3626154b82838ae08bb3081433工作线程按轮询策略接受消息2.png)



## 消息应答

> 消费者完成一个任务可能需要一段时间，在此期间消费者突然挂掉了，如果RabbitMQ 一旦向消费者传递了一条消息，便立即将该消息标记为删除，我们将丢失正在处理的消息以及后续发送给该消费者的消息。
>
> 为了保证消息在发送过程中不丢失， rabbitmq 引入消息应答机制，消息应答就是消费者在接收到消息并且处理该消息之后，告诉rabbitmq它已经对特定消息处理完成， rabbitmq 可以把该消息删除了。  
>
> 只要工作线程不进行消息应答，队列中的消息是不会删除的

1. 自动应答

   > 这种模式仅适用于消费者可以高效并以一定速率处理这些消息的情况下使用

   + 消息发送后立即被认为已经传送成功【消费者接收到消息就马上进行应答，我怎么感觉讲错了，是消息从消息中间件发送就认为传送成功了，因为后面说连接或者信道关闭，消息就丢失了】，
   + 这种模式在高吞吐量和数据传输安全性方面不是很好，因为该模式下如果消息在接收到之前，消费者那边出现连接或者 channel 关闭，消息就会丢失了
   + 另一方面这种模式下消费者没有对传递的消息数量进行限制，没处理完上一个消息下一个消息就发送过来，可能使得消费者由于接收太多还来不及处理的消息导致这些消息积压，最终使内存耗尽，导致这些消费者线程被操作系统杀死

2. 手动应答

   > 自动应答在数据安全和系统安全方面问题比较大，一般都推荐使用手动应答，以下列举手动应答的相关方法
   >
   > 手动应答的好处是可以批量应答并减少网络拥堵

   + `Channel.basicAck()  `

     > 用于肯定确认，执行该方法RabbitMQ会认为消息被成功处理，可以将该消息丢弃

   + `Channel.basicNack(deliveryTag,true)  `

     > 否定确认，执行这个方法RabbitMQ会认为当前该消息不能进行丢弃

   + `Channel.basicReject()  `

     > 否定确认，和上面方法的区别是缺少一个批量处理的参数Multiple，执行这个方法RabbitMQ会认为该消息处理失败且不再进行处理，可以将该数据进行丢弃
     >
     > 这个讲的不清楚，后面自己研究

3. Mutiple批量处理参数的解释

   + `Channel.basicNack(deliveryTag,true)`的第二个参数就是是否采用批量应答的参数

     + 如果是true，表示批量应答Channel上未应答的消息

       > 信道上的数据并不是一条一条传递的，信道中的数据可能存在好几个独立的消息，头部即当前tag消息才是工作线程下一个要获取的消息，如果批量应答Multiple参数为true，当当前tag对应的消息处理完成后会将信道中所有的消息都做手动确认应答，这种方式如果在处理信道剩余数据过程中消费者宕机，会直接导致信道中剩余的数据丢失【不太确认究竟是处理完再批量应答还是接收到tag对应消息就批量应答，课件就只说tag为8就应答，离谱，课堂上说的是tag对应的已经处理完的消息，就认为是tag对应的消息处理完再批量应答】

     + 如果为false，表示不批量应答信道上未处理的信息，只有当前tag对应的消息处理完后被应答给RabbitMQ

       > 批量应答存在风险，不建议使用批量应答，即第二个参数设置为false；批量应答虽然速度快，减少网络压力，但是存在消息丢失的可能

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/05/aa70328b03234f3a8cdb5260c9b7012b批量应答.png)



4. 消息自动重新入队
   + 如果消费者由于某些原因失去连接(其通道已关闭，连接已关闭或 TCP 连接丢失)， 导致消息中间件无法接收到消费者处理完消息发送的 ACK 确认， RabbitMQ 将发送给该消费者的消息重新排队。如果其他消费者可以处理，它将被重新分发给另一个消费者。
   + 通过这种机制确保消息不会丢失，但是存在消息被重复消费的情况【后面的幂等性会解决重复消费的问题，尚硅谷就这样，逻辑性不连贯，没有老杜讲的好理解，幂等性还是弹幕说的】



5. 消息手动应答代码实现

   > 工作线程消息处理执行完毕执行手动应答
   >
   > 此前案例basicConsume方法第二个参数autoAck都设置的true表示自动应答，手动应答需要将其设置为false，并在deliverCallback方法中对消息处理完之后使用方法channel.basicAck()进行手动应答，该方法的第二个参数是是否批量应答，选择false不使用批量应答，处理一个应答一个
   >
   > <font color=blue>在third包下实现，一个生产者，写两个消费者【<font color=red>为什么这里代码不能改了复用？</font>】，一个消费者接收消息后睡1s，一个消费者接收消息后睡30s，模拟一个工作线程执行时间很长的情况，期间不出问题再手动应答，期间关闭程序不应答断连接检验消息是否丢失【验证消息在手动应答时是不丢失的，会自动放回队列中重新消费】</font>
   >
   > 结论：
   >
   > 1. 在某个工作线程处理消息时间很长的情况下，所有的工作线程仍然遵循轮询消息分发的策略
   > 2. 当某个工作线程接收了一连串消息还没处理完，中途和消息中间件的连接断掉，消息中间件在连接断掉之后会立即将该工作线程还未处理的剩余消息全部重新入队列，再发送给其他建立连接的工作线程

   + 工具类

     【线程睡眠】

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 睡眠工具类
      * @创建日期 2023/11/06
      * @since 1.0.0
      */
     public class SleepUtil {
         /**
          * @param second
          * @描述 传入秒，当前线程睡对应秒，使用Thread.sleep()实现
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/06
          * @since 1.0.0
          */
         public static void sleepInSecond(int second){
             try {
                 //直接通过线程睡，定死了只能用秒作为单位
                 Thread.sleep(1000*second);
             } catch (InterruptedException _ignored) {
                 Thread.currentThread().interrupt();//发生异常通过当前线程的interrupt()方法唤醒当前线程
             }
         }
     
         /**
          * @param milliseconds
          * @描述 传入毫秒数，使用TimeUnit枚举类型的sleep方法实现对应的时间数量级的当前线程睡眠
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/06
          * @since 1.0.0
          */
         public static void sleepInMilliseconds(int milliseconds){
             try{
                 TimeUnit.MILLISECONDS.sleep(milliseconds);
             }catch (InterruptedException e){
                 e.printStackTrace();
             }
         }
     }
     ```

     【信道工具类】

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 用来测试生产者和消费者能否使用同一个连接，经过测试，生产者和消费者可以使用同一个连接对象
      * 疑惑这应该不是同一个连接对象，每个应用启动都应该使用了新的连接对象，和静态代码块没关系，因为都是单独编译执行的
      * @创建日期 2023/11/05
      * @since 1.0.0
      */
     public class RabbitMQUtil {
         public static Connection connection;
         /*静态代码块让连接对象只实例化一次*/
         static {
             ConnectionFactory factory = new ConnectionFactory();
             factory.setHost("192.168.200.132");
             factory.setUsername("earl");
             factory.setPassword("123456");
             try {
                 Connection mqConnection = factory.newConnection();
                 connection=mqConnection;
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (TimeoutException e) {
                 e.printStackTrace();
             }
         }
         public static Channel getChannel() throws IOException {
             System.out.println("connection对象:"+connection);
             return connection.createChannel();
         }
     }
     ```

   + 消息生产者

     > 演示在third包下

     ```java
     public class Producer1 {
         private static final String TASK_QUEUE_NAME = "ack_queue";
         public static void main(String[] argv) throws Exception {
             try (Channel channel = RabbitMQUtil.getChannel()) {
                 //设置队列名称、不可持久化、可多个工作线程访问、断开连接不自动删除队列、不设置其他参数
                 channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
                 Scanner sc = new Scanner(System.in);
                 System.out.println("等待输入信息");
                 while (sc.hasNext()) {
                     String message = sc.nextLine();
                     //使用默认交换机、发送消息到指定队列、不设置其他参数、消息转换成byte数组(如果输入有中文要设置转换byte数组的字符集，否则可能出现字符乱码)
                     channel.basicPublish("", TASK_QUEUE_NAME, null, message.getBytes("UTF-8"));
                     System.out.println("生产者发出消息" + message);
                 }
             }
         }
     }
     ```

   + 工作线程1

     ```java
     public class WorkThread1 {
         private static final String ACK_QUEUE_NAME="ack_queue";
         public static void main(String[] args) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             System.out.println(channel+"WT1 等待接收消息处理时间较短");
             //消息消费的时候如何处理消息
             DeliverCallback deliverCallback=(consumerTag, delivery)->{
                 String message= new String(delivery.getBody());
                 SleepUtil.sleepInSecond(1);
                 System.out.println("接收到消息:"+message);
                 /**
                  * 1.消息标记 tag，在每个消息的头上都被打上一个标识，比如1号标记；这个1并不是消息本身，此时做应答返回当前消息的tag标记，这个标记在消息的envelope属性中
                  * 2.是否批量应答未应答消息
                  */
                 channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
             };
             //采用手动应答
             boolean autoAck=false;
             //basicConsume方法可能封装了等待消息的代码，启动main方法会等待消息队列传递消息过来
             channel.basicConsume(ACK_QUEUE_NAME,autoAck,deliverCallback,(consumerTag)->{
                 System.out.println(consumerTag+"消费者取消消费接口回调逻辑");
             });
         }
     }
     ```

   + 工作线程2

     ```java
     public class WorkThread2 {
         //如果消息中间件中没有这个队列，接收消息启动会报错，在启动前先启动生产者初始化该队列(不需要发送消息就可以初始化)就能避免这种情况
         private static final String ACK_QUEUE_NAME="ack_queue";
         public static void main(String[] args) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             System.out.println(channel+"WT2 等待接收消息处理时间较长");
             //消息消费的时候如何处理消息
             DeliverCallback deliverCallback=(consumerTag, delivery)->{
                 String message= new String(delivery.getBody());
                 SleepUtil.sleepInSecond(30);
                 System.out.println("接收早就接收到了，处理完消息并应答消息队列:"+message);
                 /**
                  * 1.消息标记 tag，在每个消息的头上都被打上一个标识，比如1号标记；这个1并不是消息本身，此时做应答返回当前消息的tag标记，这个标记在消息的envelope属性中
                  * 2.是否批量应答未应答消息
                  * 测试在睡眠过程程序挂掉，不应答消息中间件且连接挂掉情况下，该消息是否被另一个工作线程处理
                  * 测试一个工作队列处理消息较慢，消息发送是否还遵循轮询规则，如果遵循，理论上也会产生消息积压
                  * 经过测试，连接一断消息中间件就会直接将消息重新排队发送给其他工作队列
                  */
                 channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
             };
             //采用手动应答
             boolean autoAck=false;
             //basicConsume方法可能封装了等待消息的代码，启动main方法会等待消息队列传递消息过来
             channel.basicConsume(ACK_QUEUE_NAME,autoAck,deliverCallback,(consumerTag)->{
                 System.out.println(consumerTag+"消费者取消消费接口回调逻辑");
             });
         }
     }
     ```

   + 测试效果

     + 连续发送消息

       【消息生产】

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/21f15487496b47838f74dd3ed43c2178发送消息.png)

       【工作线程】

       > 会发现，处理的很慢的一方仍然是轮询的规则，这不会很低效吗，数据量比较大的情况下仍然如此

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/df5fcb78e7be43f6b792fbe8dfbfcd04仍然轮询.png)

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/83c4e250dd7744d2b966d6cd9aacb4a0轮询.png)

       【消息队列情况】

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/c7f5bd1406354fbbb00ec6ef12c7c57e消息情况.png)

     + 处理时间较长没有消息应答处理过程连接直接断掉的情况

       【消息生产】

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/d4e626bd6599486e9b3b09ffe38a2c39连续发送消息.png)

       【工作线程】

       > 在处理消息18的时候直接断掉程序

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/14ff3fc9fccb43569db2f220615ce300较长响应.png)

       【处理时间较短的工作线程】

       > 当工作线程2挂掉以后，已经发送到2的所有消息全部重新入队发送给了1【即从18开始都是工作线程1处理工作线程2还未处理的消息】，不知道发送给2还未处理的消息都存在信道还是存在哪儿

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/550de0e63cb14d3d8e68af231f3250af较短响应.png)

       【消息队列情况】

       > 断崖掉是关闭了工作线程2导致的

       <img src="https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/cf3c6375d0d84433a25da1d0c88041a1消息情况.png" style="zoom: 80%;" />

       

       

## 消息和队列持久化

> 默认情况下 RabbitMQ 宕机队列和消息就会消失，确保消息不会丢失需要将队列和消息都标记为持久化。  
>
> 持久化的队列在RabbitMQWeb控制台的queue菜单的Features字段会显示大写D

1. 实现消息和队列持久化

   + 队列持久化，必须<font color=red>将原来非持久化的队列删除后</font>再次在生产者声明新建同名的持久化队列，原先队列没删除会报错当前队列非持久化

     > 删除可以在queues中点击对应的队列，在弹出页面点击delete--delete Queue删除原队列
     >
     > 队列持久化在重启RabbitMQ后队列依然存在【<font color=orange>？队列中的消息是否存在</font>】，感觉像只是设置了队列持久化，并没有设置消息持久化，持久化队列中的消息没有设置持久化仍然会丢失

     ```java
     channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
     ```

     【持久化队列】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/e23e52478c854fecbe15d345115b7e12持久化队列.png)

   + 消息持久化

     > 消息实现持久化需要在消息生产者发送消息时在basicPublish方法的其他参数添加属性`MessageProperties.PERSISTENT_TEXT_PLAIN `
     >
     > 尽管这种方式使RabbitMQ 将消息保存到磁盘，但是可能存在消息刚准备存储在磁盘但还没有存储完RabbitMQ就宕机的情况，仍然可能丢数据，但对简单任务队列而言已经够用了。后边会介绍更强有力的"发布确认"持久化策略。  

     ```java
     //设置消息持久化，即消息存入磁盘，使RabbitMQ重启以后消息不丢失
     channel.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
     ```



## 不公平分发

> RabbitMQ默认是轮询分发，在某种场景下这种策略并不好，如有个工作线程1处理任务的速度非常快，而另一个工作线程2处理速度很慢，此时采用轮询分发策略处理速度快工作线程大部分时间处于空闲状态，处理速度慢的工作线程一直在干活，这种情况下轮询策略效率低下。为了避免这种情况，我们可以通过设置参数 `channel.basicQos(1)`;  开启RabbitMQ的不公平分发，使处理速度快的工作线程分配更多的消息，实际默认设置`channel.basicQos(0)`，就是轮询分发
>
> 实际工作场景一般都使用不公平分发，在Channel信道列表能看到信道的Prefetch_count的分发类型

1. 在消费者接收消息之前设置分发方式为不公平分发

   > 实质是设置信道容量的大小，采用轮询的方式往信道放消息，信道满了就跳过！！！！
   >
   > 注意应答方式也要改成手动应答，否则设置的不公平分发不会生效【因为处理完一条数据会应答消息队列，消息队列再回尝试发送数据<font color=red>测试一下一次最多会发送几条</font>】【<font color=blue>经过测试是一条，那岂不是处理完一条再发下一条</font>】
   >
   > + 不设置basicQos的话是一次性平均分发给所有的队列。设置之后限制了一次分发消息的数量，再设置手动确认机制，这样当你还没提交已经处理好的时候他是不会给你消息的，这样才能实现不公平分发。
   > + 同一个消息队列相关的每个信道都要设置

   ```java
   //设置分发类型
   int prefetchCount=1;
   channel.basicQos(prefetchCount);
   //采用手动应答
   boolean autoAck=false;
   //basicConsume方法可能封装了等待消息的代码，启动main方法会等待消息队列传递消息过来
   channel.basicConsume(ACK_QUEUE_NAME,autoAck,deliverCallback,(consumerTag)->{
       System.out.println(consumerTag+"消费者取消消费接口回调逻辑");
   });
   ```

2. 测试效果

   > unacked是尚未确认的意思

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/23309e8ea5fe4d42b041e1e40463f5a2不公平分发.png)

   【生产者】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/9bfa090cebf64197bcda811142081dd6生产者.png)

   【处理速度快的消费者】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/0ec13b90427241db8f133fcd0678b05e消费者1.png)

   【处理慢的消费者】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/4f83071d2fee468184030577c75afb0f消费者2.png)



## 预期值

> 实质就是将信道作为一个未确认消息的消息缓存区，通过限制消息缓冲区的大小【预期值，可以视作滑动窗口的大小】避免缓冲区未确认消息无限制堆积的问题

1. 预取值【perfetchCount】：信道可以一次性获取队列中c条信息

   > 【预期值包含了未处理的和当前正在处理的，视为当前正在处理的在信道的头部】

   + 当为0时不限制，所以队列中的消息可以轮询着一次性发完，
   + 当为1时，只能获取一条，处理完获取下一条

2. 设置预期值的效果

   + 预期值就是信道容纳预期值数量的消息

   + 信道满之前还是按照轮询的规则给每个信道分配直到某个信道堆积到预期值数量的消息，此后接收到应答确认再发消息

     > 【确认一下是信道中的数据处理完了再重发两条还是处理完一条立马将信道补满，经过确认一应答就补，实际预期值就是信道的最大消息堆积数量】

3. 同一个队列的不同信道预取值可以设置成不同的数量

![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/a615f77579e240a7ba090f37aa242d2e信道和预期值的关系.png)



# 发布确认

> 【Publish/Subscribe】
>
> 发布确认的核心是RabbitMQ将消息保存在磁盘上以后向生产者发布确认信息，生产者确实收到消息队列发过来的确认消息已经持久化到硬盘上的信息【这里面暗含了三个前提条件：队列必须设置持久化、队列中的消息必须设置持久化、确认设置了发布确认模式】
>
> + 没有设置队列持久化和队列中消息持久化也是可以设置发布确认模式的，此时消息投递到队列就会向生产者传递确认消息
>
> 生产者将信道设置成发布确认模式后，所有在该信道上面发布的消息都将会被指派一个从1开始的唯一ID
>
> + 没有设置消息和队列是持久化的情况下，当消息被投递到匹配的队列之后，消息队列会发送一个确认给生产者【确认信息中包含了消息的唯一ID】，使得生产者知道消息已经正确到达目的队列
> + 如果消息和队列是可持久化的情况下，确认消息会在消息写入磁盘之后发出，消息队列回传给生产者的确认消息的delivery-tag域中包含了对应消息消息的ID
>   + 此外消息队列也可以设置basic.ack的multiple域【批量应答】，表示到当前消息之前的所有消息都已经得到了处理
> + 确认发布模式最大的好处在于他是异步的，生产者可以在等待信道返回确认的同时继续发送下一条消息
>   + 当消息最终得到确认之后，生产者可以通过回调方法来处理该确认消息
>   + 如果RabbitMQ因为自身内部错误导致消息丢失，就会发送一条nack消息，生产者同样可以在回调方法中处理该nack消息

## 开启发布确认模式

1. 开启发布确认模式

   > 发布确认模式是在发消息前对信道使用confirmSelect方法开启的

   ```java
   //设置信道确认发布模式，在信道【信息通道】获取之后，消息发送前进行设置
   channel.confirmSelect();
   ```





## 三种发布确认模式

> 经过测试，三种发布确认模式发送1000条相同消息的总时间分别为460、58、25毫秒

> 核心是消息中间件确认需要时间，单个发布确认每次都等确认完成再发送下一个；批量发布确认等对方一批确认完成再执行发送下一批；异步发布确认是发送过程不管确认的问题，使用监听线程监听消息确认回调，统一处理后告知发送失败的消息
>
> 企业用的都是异步处理，最好用，速度最快

1. 三种模式的特点

   + 单独发布确认【460ms】

     > 同步等待确认【每发一条确认一条，不缺认下一条发送不了】， 简单，但吞吐量非常有限

   + 批量发布确认【58ms】

     > 批量同步等待确认【一批消息一次确认】，简单，合理的吞吐量， 一旦出现问题但很难推断出是那条消息出现了问题

   + 异步发布确认【25ms】

     > 【发送的时候不管确认】，最佳性能和资源使用，在出现错误的情况下可以很好地控制，但是实现起来稍微难些【多个监听线程和并发跳跃哈希表】  

### 单个发布确认

> 是一种同步发布确认的方式【即发布一个消息后必须等到该消息被确认发布后，下一条消息才能继续发布，`waitForConfirmsOrDie(long)`这个方法只有在消息被确认的时候才返回布尔值，确认成功返回true，如果在指定时间范围内这个消息没有被确认那么它将抛出异常

1. 缺点

   + 发布速度特别的慢

     > 没有确认发布的消息会阻塞所有后续消息的发布，只有等待当前消息发布确认后才发送下一条，这种方式最多提供每秒不超过数百条发布消息的吞吐量。当然对于某些应用程序来说这已经足够了

2. 代码实现

   > 在forth包下进行演示
   >
   > 打印1000条消息发布总共耗时的时间验证不同模式间的性能差异
   >
   > 1000条耗时460ms

   ```java
   /**
    * @author Earl
    * @version 1.0.0
    * @描述 发布确认模式
    * 1. 单个确认
    * 2. 批量确认
    * 3. 异步批量确认
    * 测试各个模式发布确认时间，比对各个模式下的性能
    * @创建日期 2023/11/06
    * @since 1.0.0
    */
   public class PublishConfirm {
   
       /**
        *消息发送数量
        */
       public static final int MESSAGE_COUNT=1000;
       public static void main(String[] args) throws Exception {
           PublishConfirm.singlePublishConfirm();//单个发布确认发布消息1000条耗时460ms(课程每条都有打印行为耗时700多毫秒)
   
       }
   
       /**
        * @描述 单个发布确认
        * @author Earl
        * @version 1.0.0
        * @创建日期 2023/11/06
        * @since 1.0.0
        */
       public static void singlePublishConfirm() throws Exception {
           //UUID的长度是36位长度随机长度的id，中间有4个横线隔开
           try (Channel channel = RabbitMQUtil.getChannel()) {
               String queueName = UUID.randomUUID().toString();
               channel.queueDeclare(queueName, false, false, false, null);
               //开启发布确认
               channel.confirmSelect();
               long begin = System.currentTimeMillis();
               for (int i = 0; i < MESSAGE_COUNT; i++) {
                   String message = i + "";
                   channel.basicPublish("", queueName, null, message.getBytes());
                   //服务端返回 false 或超时时间内未返回，生产者可以消息重发
                   boolean flag = channel.waitForConfirms();
               }
               long end = System.currentTimeMillis();
               System.out.println("单个发布确认发布消息" + MESSAGE_COUNT + "条耗时" + (end - begin) + "ms");
           }
   
       }
   }
   ```

   

### 批量发布确认

> 单个发布确认非常慢，批量发布确认先发布一批消息然后一起确认可以极大地提高吞吐量
>
> `waitForConfirms`方法的逻辑有点类似执行这个方法去获取消息的确认状态，在消息发布确认的过程中，如果有一个消息出现问题【？后面的消息都会出问题吗，不然的话一个状态的确认怎么能代表所有，除非有一个出问题，都会导致状态置为false，表示这一批出现了问题】，`waitForConfirms`的结果就不为true，当执行完这个方法状态会被重新置为true，检验下一批的状态，这意为着`waitForConfirms`方法可以根据设置的位置不同而自主选择消息确认批次中消息数量的多少【如在所有消息发送完成后，是将整个消息作为整体进行发布确认，出了问题只知道本次发送出了问题，也可以设置当发送多少次消息后进行一次发布确认，出了问题可以知道出问题的批次】



1. 缺点

   + 当发生故障导致发布出现问题时，不知道具体是哪个消息出现了问题， 必须将整个批处理消息保存在内存中，记录重要的信息后重新发布消息。当然这种方案仍然是同步的，也一样阻塞消息的发布<font color=red>这句话什么意思</font>。  

2. 代码实现

   > 1000条耗时58ms，相比与单个发布确认，速度快了8倍

   ```java
   public class PublishConfirm {
   
       /**
        *消息发送数量
        */
       public static final int MESSAGE_COUNT=1000;
       public static void main(String[] args) throws Exception {
           //PublishConfirm.singlePublishConfirm();//单个发布确认发布消息1000条耗时460ms(课程每条都有打印行为耗时700多毫秒)
   
           PublishConfirm.batchPublishConfirm();//发布1000个批量确认消息,耗时58ms
       }
   
       /**
        * @描述 单个发布确认发送1000条消息
        * @author Earl
        * @version 1.0.0
        * @创建日期 2023/11/06
        * @since 1.0.0
        */
       public static void batchPublishConfirm() throws Exception {
           //UUID的长度是36位长度随机长度的id
           try (Channel channel = RabbitMQUtil.getChannel()) {
               String queueName = UUID.randomUUID().toString();
               channel.queueDeclare(queueName, true, false, false, null);
               //开启发布确认
               channel.confirmSelect();
               //批量确认消息大小
               int batchSize = 100;
               //未确认消息个数
               int outstandingMessageCount = 0;
               long begin = System.currentTimeMillis();
               for (int i = 0; i < MESSAGE_COUNT; i++) {
                   String message = i + "";
                   channel.basicPublish("", queueName, null, message.getBytes());
                   outstandingMessageCount++;
                   if (outstandingMessageCount == batchSize) {
                       channel.waitForConfirms();
                       outstandingMessageCount = 0;
                   }
               }
               //为了确保还有剩余没有确认消息 再次确认
               if (outstandingMessageCount > 0) {
                   channel.waitForConfirms();
               }
               long end = System.currentTimeMillis();
               System.out.println("发布" + MESSAGE_COUNT + "个批量确认消息,耗时" + (end - begin) + "ms");
           }
   
       }
   }
   ```



### 异步发布确认

> 异步确认虽然编程逻辑比上两个要复杂，但是性价比最高，无论是可靠性还是效率都特别高，他是利用消息队列的回调函数来达到消息可靠性传递的

1. 异步发布确认图解

   > 消息发在信道中，每个消息都以Map集合的方式，在key中保存消息的序号，消息生产者不需要再关注何时去获取发布确认，会由交换机根据消息序号找到哪些消息发送成功，哪些消息没有收到统一地异步返回给生产者，没收到的生产者再重新发送即可
   >
   > 异步发布确认的代码实现比较繁琐

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/06/b359c009d6e241cfaa3690f9735a5074异步发布确认图解.png)

2. 代码实现

   > 在发送消息之前准备一个消息监听器，监听消息中间件通过信道返回给生产者消息发送成功与否的具体情况
   >
   > 监听器的重载方法有单参，有双参；单参是只监听成功的，双参是既监听成功的也监听失败的；监听成功和监听失败的接口都是同一个函数式接口的不同实现
   >
   > 收到确认条数少于发送条数的同学：rabbitmq给的成功回调可能是单条的也可能是批量的，批量时multiple=true，表示该tag及其之前的消息都确认了
   >
   > 注意主线程主方法执行结束，其他线程会自动结束不再进行打印，所以这里让主线程睡10s，让消息队列回调打印完成，【确实打印到1000截止】
   >
   > 回调函数中的第一个参数sequenceNumber是消息的序号，从1开始到1000；第二个参数表示当前返回的序号是批量确认还是单个确认，
   >
   > 异步发布确认再消息发送完成以前就开始批量确认回调通知生产者之前发送的部分消息已经接收到了

   ```java
   public class PublishConfirm {
   
       /**
        *消息发送数量
        */
       public static final int MESSAGE_COUNT=1000;
       public static void main(String[] args) throws Exception {
           //PublishConfirm.singlePublishConfirm();//单个发布确认发布消息1000条耗时460ms(课程每条都有打印行为耗时700多毫秒)
           //PublishConfirm.batchPublishConfirm();//发布1000个批量确认消息,耗时58ms
           PublishConfirm.asyncPublishConfirm();//发布1000个异步确认消息,耗时25ms
       }
   
       /**
        * @描述异步发布确认发送1000条信息
        * @author Earl
        * @version 1.0.0
        * @创建日期 2023/11/07
        * @since 1.0.0
        */
       public static void asyncPublishConfirm() throws Exception{
           try (Channel channel = RabbitMQUtil.getChannel()) {
               String queueName = UUID.randomUUID().toString();
               channel.queueDeclare(queueName, true, false, false, null);
               //开启发布确认
               channel.confirmSelect();
               /**
                * 线程安全有序的一个哈希表，适用于高并发的情况
                * 1.轻松的将序号与消息进行关联
                * 2.轻松批量删除条目 只要给到序列号
                * 3.支持并发访问
                */
               ConcurrentSkipListMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();
               /**
                * 确认收到消息的一个回调
                * 参数1.消息序列号
                * 参数2.true 可以确认小于等于当前序列号的消息[是否为批量确认]
                *      false 只能确认当前序列号消息
                *
                * 这个方法会被多次调用，因为消息队列会多次回调
                */
               ConfirmCallback ackCallback = (sequenceNumber, multiple) -> {
                   //这个if和else的逻辑学完ConcurrentSkipListMap再回来看，这里是根据序号删除已经被确认的数据，所有发送的数据都被记录在这个并发跳跃哈希表中了
                   if (multiple) {
                       //删除已经确认发布的消息，剩余未被确认的消息 是一个 map,这个outstandingConfirms.headMap不太懂是什么意思【删除已确认消息】
                       //headMap类似与获取当前序号前的所有序号,headMap的返回值其实就是一个从第一个Key到传入headMap方法的key所有的组成的一个子跳表
                       //跳表相比于简单的哈希就是跳表的Key是按照插入顺序来的，看一下源码就知道那个headMap方法是得到传进去的Key到第一个Key的所有Key，目的是可以调用clear批量删除。
                       //ConcurrentNavigableMap这是个接口，实现类就有ConcurrentSkipListMap
                       ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(sequenceNumber, true);
                       System.out.println("本次确认子跳表:"+confirmed);
                       //清除该部分未确认消息，卧槽这是怎么识别该清除那个集合中的
                       confirmed.clear();
                   }else{
                       //不是批量确认只删除当前序列号对应的消息
                       outstandingConfirms.remove(sequenceNumber);
                       System.out.println("本次确认子跳表:"+sequenceNumber);
                   }
                   //打印当前确认的消息
                   //System.out.println("确认的消息:"+multiple+" | "+sequenceNumber);
                   //打印当前已发布未被确认的消息
                   //System.out.println(outstandingConfirms);
               };
               /**
                * 消息接收失败的回调
                * */
               ConfirmCallback nackCallback = (sequenceNumber, multiple) -> {
                   String message = outstandingConfirms.get(sequenceNumber);
                   System.out.println("未确认消息:"+message+" | "+sequenceNumber);
               };
               /**
                * 添加一个异步确认的监听器
                * 1.确认收到消息的回调
                * 2.未收到消息的回调
                */
               channel.addConfirmListener(ackCallback, nackCallback);
               long begin = System.currentTimeMillis();
               for (int i = 0; i < MESSAGE_COUNT; i++) {
                   String message = "消息" + i;
                   /**
                    * 将消息
                    * channel.getNextPublishSeqNo()获取下一个消息的序列号，这个map的序号不是人为定的，是调惨获取的
                    * 通过序列号与消息体进行一个关联
                    * 全部都是未确认的消息体
                    */
                   outstandingConfirms.put(channel.getNextPublishSeqNo(), message);
                   channel.basicPublish("", queueName, null, message.getBytes());
               }
               long end = System.currentTimeMillis();
               System.out.println("发布" + MESSAGE_COUNT + "个异步确认消息,耗时" + (end - begin) + "ms");
               SleepUtil.sleepInSecond(10);
           }
       }
   }
   ```
   
   

3. 处理异步未确认消息的方式

   > 思路是将未确认消息重新发送或者将未确认消息保存起来以后再重新发送
   >
   > 解决方案是监听线程把未确认的消息放到一个基于内存的能被发布线程访问的队列，如 ConcurrentLinkedQueue 【并发链式队列，JUC有讲】，这个并发链式队列在监听线程【暂时认为确认回调和未确认回调在一个线程中】与发布线程之间进行消息的传递  

   + 在消息发布的时候生产者就要将所有消息记录在并发链式队列【后改用并发跳越哈希表，因为消息队列只返回序号，需要序号把消息对应起来】中

     > 跳表是有序链表，发布确认模式下消息的编号是从1开始的
     >
     > 选择并发跳跃哈希表的原因是序号和消息对应，轻松添加和删除
     >
     > 支持高并发，可以多线程访问，存操作和删操作可能同时进行，但是同时操作的对象不可能是同一个

     ```java
     for (int i = 0; i < MESSAGE_COUNT; i++) {
         String message = "消息" + i;
         /**
          * 将消息
          * channel.getNextPublishSeqNo()获取下一个消息的序列号，这个map的序号不是人为定的，是调惨获取的
          * 通过序列号与消息体进行一个关联
          * 全部都是未确认的消息体
          */
         outstandingConfirms.put(channel.getNextPublishSeqNo(), message);
         channel.basicPublish("", queueName, null, message.getBytes());
     }
     ```

   + 在确认回调删除已经被确认的消息，剩下的就是未确认的消息

     > 核心是生产者发消息的同时向并发跳跃哈希表添加消息和消息序号的key-value键值对
     >
     > 在确认回调用headMap获取当前确认消息序号到首个元素的子跳表，用子跳表的clear方法删除跳表中的对应子跳表，并发跳跃哈希表中剩余的就是未被确认的【<font color=red>？疑惑，如果之前有确认失败的，后续headMap不会一起给删了吗？是否需要单独在失败回调中将确认失败的消息单独取出存起来？学了JUC来看</font>】

     ```java
     /**
      * 线程安全有序的一个哈希表，适用于高并发的情况
      * 1.轻松的将序号与消息进行关联
      * 2.轻松批量删除条目 只要给到序列号
      * 3.支持并发访问
      */
     ConcurrentSkipListMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();
     /**
      * 确认收到消息的一个回调
      * 参数1.消息序列号
      * 参数2.true 可以确认小于等于当前序列号的消息[是否为批量确认]
      *      false 只能确认当前序列号消息
      *
      * 这个方法会被多次调用，因为消息队列会多次回调
      */
     ConfirmCallback ackCallback = (sequenceNumber, multiple) -> {
         //这个if和else的逻辑学完ConcurrentSkipListMap再回来看，这里是根据序号删除已经被确认的数据，所有发送的数据都被记录在这个并发跳跃哈希表中了
         if (multiple) {
             //删除已经确认发布的消息，剩余未被确认的消息 是一个 map,这个outstandingConfirms.headMap不太懂是什么意思【删除已确认消息】
             //headMap类似与获取当前序号前的所有序号,headMap的返回值其实就是一个从第一个Key到传入headMap方法的key所有的组成的一个子跳表
             //跳表相比于简单的哈希就是跳表的Key是按照插入顺序来的，看一下源码就知道那个headMap方法是得到传进去的Key到第一个Key的所有Key，目的是可以调用clear批量删除。
             //ConcurrentNavigableMap这是个接口，实现类就有ConcurrentSkipListMap
             ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(sequenceNumber, true);
             System.out.println("本次确认子跳表:"+confirmed);
             //清除该部分未确认消息，卧槽这是怎么识别该清除那个集合中的
             confirmed.clear();
         }else{
             //不是批量确认只删除当前序列号对应的消息
             outstandingConfirms.remove(sequenceNumber);
             System.out.println("本次确认子跳表:"+sequenceNumber);
         }
         //打印当前确认的消息
         //System.out.println("确认的消息:"+multiple+" | "+sequenceNumber);
         //打印当前已发布未被确认的消息
         //System.out.println(outstandingConfirms);
     };
     ```

     

   + 批次确认的部分消息

     > 序号从1开始，一直到1000，并发哈希跳表最后剩下的是未被确认的数据，
     >
     > 疑问：这里主线程噶了，其他线程也会噶，即打印会中断，主线程噶了不是守护线程噶吗

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/61d5838442e14aa5b3ad0a5ff00fa4a0批次异步确认.png)

     【守护线程噶了导致监听线程噶】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/235f08169f7544faa5f490deb9534304主线程无了导致监听器线程噶.png)



# 交换机

> 控制台中的Exchanges的AMQP default是默认交换机，发送消息指定交换机为空串就会走默认交换机
>
> 通常生产者生产的消息不会直接送到队列，生产者都不知道这些消息传递到了哪些队列，生产者只负则将消息传递给交换机

1. 交换机介绍

   + 默认情况下，一个消息只能被一个工作线程消费一次；【生产者生产的消息从不会直接发送给队列，这种情况下不需要使用交换机<font color=red>课程说的是错的，这种情况下还是会使用交换机，使用的是默认交换机AMQP default</font>】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/37338b7149214d3aa34e1a6a156e890a简单队列、工作队列结构.png)

   + 可能存在一种工作场景，一个消息需要被多个工作线程消费【这种情况由交换机绑定到多个队列，消息同时发送到多个队列，每个队列中的消息只能被消费一次，从而实现同一个消息被消费多次】

     > 这种模式被称为发布订阅模式

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/7f39c22b7f0e48d7966d0a2054e6792d发布订阅模式结构图.png)

2. 交换机的概念

   > 消息能路由发送到队列是由routingKey【bindingKey】指定的队列，此前指定默认交换机时通过第二个参数指定队列名就是指定的bingdingKey

   + 交换机的工作内容

     +  接收来自生产者的消息，

     + 将消息推入队列

       > 交换机必须确切知道如何处理收到的消息
       >
       > 由交换机的类型决定应该把消息放到特定队列或把消息放到许多队列中又或者丢弃这些消息

   + 交换机的类型

     + 直接类型【direct】

       > 直接类型也叫路由类型

     + 主题类型【topic】

     + 标题类型【headers】

       > 头类型，在企业中已经不常使用了

     + 扇出类型【fanout】

       > 扇出类型就是发布订阅模式

     + 无名类型

       > 无名类型就是默认的交换机类型，通过空字符串进行标识



## 临时队列

> 临时队列是未设置持久化的对列，一旦RabbitMQ打开消费者连接，会被自动删除的队列？【队列不是由生产者声明创建的吗】
>
> <font color=red>这里估计讲错了，是RabbitMQ一旦重启，该队列就会被删除，经过测试，即便队列为空，生产者和消费者都断开连接临时队列依然存在</font>

1. 通过信道指定队列名创建临时队列

   > 在发送消息前声明队列的名称等参数

   ```java
   channel.queueDeclare(QUEUE_NAME,false,false,false,null);
   String message="hello world";
   channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
   ```

2. 通过信道队列声明的getQueue方法创建随机队列名的队列，并返回队列名称

   ```java
   String queueName = channel.queueDeclare().getQueue();
   String message="hello world";
   channel.basicPublish("",queueName,null,message.getBytes());
   ```

   【随机临时队列效果】

   > AD、Excl就是表示临时的意思

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/ff4ebca607f24954bab72114f6998d58创建随机临时队列.png)





## 绑定

> binding指定了交换机和队列之间的对应关系，RountingKey是用户自定义的关键词，认为RountingKey是绑定关系的标识，交换机通过RountingKey将消息路由到对应绑定的队列【一个交换机可以绑定多个队列，生产者可以通过RountingKey指定交换机把消息发送给指定的队列而非所有与交换机绑定的队列】
>
> 通过rountingKey可以实现由生产者随意决定消息的发送方式

![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/7eb38b2f61ae40608ca4c8a5fcbe53d9rountingkey效果演示.png)

1. 绑定实操演示

   【定义交换机】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/d05d565cb45a4e7884b54fee8c16de86创建自定义交换机.png)

   【定义队列】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/14d1a893187a4777928c591df1329f5b用户自定义队列.png)

   【交换机绑定队列】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/e32e238f99ce4d4a98836480dd8ee620交换机绑定队列效果演示.png)

   



## 发布订阅模式

> 扇出类型，翻译成扇出【Fanout】，其实就是发布订阅模式
>
> 将接收到的所有消息广播到对应扇出类型交换机绑定的所有队列中
>
> 系统自带一个发布订阅交换机，名字叫做amq.fanout，除此以外还可以自定义一个发布订阅的交换机而不使用系统自带的
>
> <font color=red>卧槽，大家都说RoutingKey和扇出模式无关，只要交换机是扇出模式，那么其绑定的队列都会收到消息，经过验证，确实如此，即使RountingKey和生产者设定不同，仍然能接收到消息</font>
>
> 发布订阅模式在SpringBoot中的绑定没有设置RoutingKey的方法，因为不需要绑定，原生代码绑定了也没有效果
>
> 【RountingKey】
>
> ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/7c0321935bcc4c4ea6091b80a86684c0扇出模式RoutingKey不同也能接收到消息.png)
>
> 【测试效果】
>
> + 这里将队列2的RountingKey改成了123，注意123是字符串的形式
>
> ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/f406868e4d2d428cb8f0f4dcf39837d3仍然收到消息.png)

1. Fanout实现结构梳理

   > 实现在fifth包下，构建一个简单的日志系统。生产者将发出日志消息，启动两个消费者，一个消费者接收到消息后把日志存储在磁盘， 另外一个消费者接收到消息后把消息打印在屏幕上，以验证一个生产者发出的消息被广播给fanout类型交换机绑定的所有消费者

   【项目结构】

   > 交换机名为logs，绑定两个随机队列，RountingKey两个都设置为空串【即什么都不写】，实现生产者发送的消息同时被消费者接收到并打印

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/8083f97ea8b24258be18753563ff145b扇出实践项目结构.png)

2. 代码实现

   > 要点：
   >
   > + 生产者、消费者都可以对交换机和队列进行声明，且只需要声明一次，在声明一次的情况下，声明的程序必须首先启动，否则即使创建了交换机生产者发送第二条消息的时候也会报错
   > + 生产者发送信息使用了交换机可以不指定队列，此时只有4个参数，第二个参数是routingKey，交换机会自动根据绑定的队列和routingKey将消息发送到指定队列中
   > + 队列的声明最好放在消费者一侧，因为生产者在有交换机和routingKey的情况下，不用关心具体将消息发送给哪一个队列，只需要发送给交换机，交换机根据信息自动裁定；但是消费者需要和队列进行绑定，必须知道队列的名称，如果使用随机临时队列，在消费者一侧声明，`basicConsume`方法接收消息的队列名参数会很方便，同时绑定交换机和队列也很方便

   + 生产者

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 日志生产者，交换机必须同时在生产者和消费者中进行声明，生产者不需要声明队列，队列由交换机决定，消费者必须声明或者创建队列并绑定队列与交换机的关系
      * 那个文件声明了交换机就要先启动，否则即使后续声明交换机的程序启动，仍然无法绑定上交换机，即生产者、消费者都可以声明交换机和队列，但是声明交换机的程序要先启动
      * 否则没有声明交换机的程序后续也无法绑定交换机，稳妥的做法是到处都声明交换机能避免启动报错；由于是消费者接收消息需要与队列绑定，很难实现在生产者声明队列把名字
      * 传递给消费者进行消费者和队列的绑定
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class EmitLog {
         private static final String EXCHANGE_NAME = "logs";
         public static void main(String[] argv) throws Exception {
             try (Channel channel = RabbitMQUtil.getChannel()) {
                 /**
                 * 声明一个 exchange
                 * 1.exchange 的名称
                 * 2.exchange 的类型，实际这里应该写枚举BuildtinExchangeType.FANOUT，也可以直接写英文小写
                 *
                 * 多处声明交换机能避免因为启动顺序报错
                 */
                 //channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                 Scanner sc = new Scanner(System.in);
                 System.out.println("请输入信息");
                 while (sc.hasNext()) {
                     String message = sc.nextLine();
                     //消息发送时要指定RoutingKey，这玩意儿在队列与交换机绑定的时候就进行了声明，生产者发送消息需要使用
                     //UTF-8是避免中文乱码
                     channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
                     System.out.println("生产者发出消息" + message);
                 }
             }
         }
     }
     ```

   + 消费者1打印日志

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 扇出交换机的消息接收
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class ReceiveLogs01 {
         private static final String EXCHANGE_NAME = "logs";
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
     
             //声明交换机
             channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
     
             /**
             * 生成一个临时的队列
             * 队列的名称是随机的
             * 当消费者断开和该队列的连接时 队列自动删除
             */
             String queueName = channel.queueDeclare().getQueue();
     
             //把该临时队列绑定我们的自定义 exchange 其中 routingKey(也称之为bindingKey)为空字符串
             channel.queueBind(queueName, EXCHANGE_NAME, "");
     
             //接收消息
             System.out.println("等待接收消息,把接收到的消息打印在屏幕.....");
             //接收到消息后的处理回调
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 System.out.println("控制台打印接收到的消息"+message);
             };
             //正式接收消息，自动确认
             channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
         }
     }
     ```

   + 消费者2生成覆盖日志文件

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 扇出类型交换机广播消息给消费者2供日志存储文件
      * 弹幕说交换机和队列在消费者和生产者都可以声明，这里为了方便直接在消费者声明队列便于消费者和随机队列的绑定，否则在生产者声明的队列名字都不知道咋传递过来
      * 交换机其实只用声明一次，发布消息和绑定队列的时候出现交换机的名字即可
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class ReceiveLogs02 {
         private static final String EXCHANGE_NAME = "logs";
     
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
             /**
              * 生成一个临时的队列 队列的名称是随机的
              * 当消费者断开和该队列的连接时 队列自动删除
              */
             String queueName = channel.queueDeclare().getQueue();
             //把该临时队列绑定我们的 exchange 其中 routingkey(也称之为 binding key)为空字符串
             channel.queueBind(queueName, EXCHANGE_NAME, "");
             System.out.println("等待接收消息,把接收到的消息写到文件.....");
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 File file = new File("E:\\JavaStudy\\016_RabbitMQ\\rabbitmq-demo\\rabbitmq_info.txt");
                 FileUtils.writeStringToFile(file, message, "UTF-8");
                 System.out.println(message+"数据写入文件成功");
             };
             channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
             });
         }
     }
     ```

   + 执行效果演示

     【生产者】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/501cc36983bc474e81fd97197c3d465b生产者.png)

     【消费者1】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/e467bf0efe5342978d5ce1ac0d68ab28消费者1.png)

     【消费者2】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/6d198add47e2465083b7d25091ea310a消费者2.png)

     【生产者没有声明交换机且启动顺序错误报错】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/88bf1561c669452691a974e81b54136b没有对应交换机报错.png)

   

   

## 路由模式

> 路由模式也称直接交换机，直接模式根据RoutingKey和交换机精确匹配队列；扇出模式忽略RoutingKey，向所有与交换机绑定的队列发送消息【已经验证】
>
> 同样是构建日志系统，希望将日志消息写入磁盘的程序仅接收严重错误(errros)，而不存储哪些警告(warning)或信息(info)日志消息避免浪费磁盘空间。扇出模式不会对队列进行区分，在这种场景下可以使用直接模式让消息只去指定RountingKey对应的队列中去

1. 要点

   + 一个队列可以和一个交换机存在多个绑定关系，每个绑定关系对应1个RoutingKey

     > 像图上这种情况使用任意一个RoutingKey消息都能路由到console队列，<font color=red>组合起来作为新的RoutingKey消息会被丢弃</font>

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/8b5c1d1052c043c28255f67996cc1475多个绑定关系.png)

     【多个RoutingKey结构图】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/af9bcd985ea54cb39d7a748b86f0ac44多个路由key.png)

   + 多重绑定，多个队列相同的RoutingKey

     > 这种情况下，直接模式的表现效果类似扇出模式，会将消息向指定RoutingKey的所有队列传递

     【多重绑定结构图】

     > 生产者指定RoutingKey为black，消息会同时传递给队列Q1和Q2

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/b9161edae0874437aac25c6926c5d47c多重绑定.png)

2. 代码实现

   > sixth包下，交换机名为direct_logs，两个队列console和disk，

   + 生产者

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 生产者发布消息
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class EmitLogDirect {
         private static final String EXCHANGE_NAME = "direct_logs";
         public static void main(String[] argv) throws Exception {
             try (Channel channel = RabbitMQUtil.getChannel()) {
                 //声明交换机
                 channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
                 //创建多个 bindingKey
                 Map<String, String> bindingKeyMap = new HashMap<>();
                 bindingKeyMap.put("info","普通 info 信息");
                 bindingKeyMap.put("warning","警告 warning 信息");
                 bindingKeyMap.put("error","错误 error 信息");
                 //debug 没有消费这接收这个消息 所有就丢失了
                 bindingKeyMap.put("warningerror","测试 RoutingKey混合信息");
                 bindingKeyMap.put("debug","调试 debug 信息");
                 for (Map.Entry<String, String> bindingKeyEntry: bindingKeyMap.entrySet()){
                     String bindingKey = bindingKeyEntry.getKey();
                     String message = bindingKeyEntry.getValue();
                     channel.basicPublish(EXCHANGE_NAME,bindingKey, null, message.getBytes("UTF-8"));
                     System.out.println("生产者发出消息:" + message);
                 }
             }
         }
     }
     ```

   + 消费者1

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 绑定direct类型交换机，设置RoutingKey为error，消息发送者的RoutingKey为error会被该消费者接收并处理
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class ReceiveLogsDirect01 {
         private static final String EXCHANGE_NAME = "direct_logs";
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             //声明交换机名字和类型
             channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
             //声明队列
             String queueName = "disk";
             channel.queueDeclare(queueName, false, false, false, null);
             //绑定交换机和队列
             channel.queueBind(queueName, EXCHANGE_NAME, "error");
             System.out.println("等待接收消息.....");
             //消息接收回调
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 //还可以通过`delivery.getEnvelope().getRoutingKey()`获取消息的RoutingKey
                 message="接收绑定键:"+delivery.getEnvelope().getRoutingKey()+",消息:"+message;
                 File file = new File("E:\\JavaStudy\\016_RabbitMQ\\rabbitmq-demo\\rabbitmq_sixth.txt");
                 FileUtils.writeStringToFile(file,message,"UTF-8");
                 System.out.println("错误日志已经接收"+new String(delivery.getBody()));
             };
             //传递队列名对应消费者准备接收消息
             channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
             });
         }
     }
     ```

   + 消费者2

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 接收RoutingKey为info或者warning的信息输出到控制台
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class ReceiveLogsDirect02 {
         private static final String EXCHANGE_NAME = "direct_logs";
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
             String queueName = "console";
             channel.queueDeclare(queueName, false, false, false, null);
             //交换机和队列间绑定多个RoutingKey
             channel.queueBind(queueName, EXCHANGE_NAME, "info");
             channel.queueBind(queueName, EXCHANGE_NAME, "warning");
             System.out.println("等待接收消息.....");
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 System.out.println("接收绑定键 :"+delivery.getEnvelope().getRoutingKey()+", 消息:"+message);
             };
             channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
             });
         }
     }
     ```

   + 测试效果

     > 结论：RoutingKey为error、warning和info的都分发到对应的队列中去了，其他RoutingKey和组合RoutingKey对应的消息丢弃

     【生产者发送消息】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/1262570ecde74a57bffb539cba24e028生产者.png)

     【消费者1接收到消息】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/c855687d73364c6eac9da122420f0f84错误日志消费者接收消息.png)

     【消费者2接收到消息】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/a93737c69ef344e8871c3e7c3ed00457警告日志消费者接收消息.png)

     【交换机绑定情况】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/81f497a2dceb48e1ba187d6167eadee5交换机绑定情况.png)



## 主题模式

> 【topic】直接交换机不可能同时路由两个RoutingKey不同的队列，如果某天存在这样的需求，只能使用Topic模式

1. 特点

   + topic交换机的 routing_key 必须是一个单词列表，

     + 单词间以点号分隔开，注意经过只有一个单词也可以正常使用
     + 单词列表的长度不能超过 255 个字节
     + 一个队列可以被多个RoutingKey单词列表路由，一个队列的多个RoutingKey都匹配，消息也只会被该队列接收一次
     + 不匹配任何RoutingKey单词列表的消息会被丢弃
     + *(星号)可以代替一个单词
     + \#(井号)可以替代零个或多个单词【一个队列RoutingKey是#,那么这个队列将匹配所有的RoutingKey接收所有数据  】

     > 如"stock.usd.nyse", "nyse.vmw","quick.orange.rabbit"  
     >
     > `*.orange.*`  中间为 orange 长度为3个单词的字符串  
     >
     > `lazy.#` 第一个单词是lazy的单词列表

   + 示例

     > 消息的RoutingKey为`quick.orange.rabbit  `的能同时匹配Q1和Q2队列
     >
     > 消息的RoutingKey为`lazy.pink.rabbit  `的同时满足Q2的两个RoutingKey，但消息只会被接收一次

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/658037badeb64d1ead3c895f6245ebd2主题模式.png)

2. 代码实现

   > 交换机为topic_logs，类型为主题交换机；对列设为Q1、Q2，RoutingKey设置如上图所示
   >
   > 发送消息，验证消息RoutingKey设置为不同列表队列的接收情况

   + 生产者

     > 注意必须消费者完全启动，生产者发送消息才会生效

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 主题模式交换机生产者
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class EmitLogTopic {
         private static final String EXCHANGE_NAME = "topic_logs";
         public static void main(String[] argv) throws Exception {
             try (Channel channel = RabbitMQUtil.getChannel()) {
                 channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
                 /**
                 * Q1-->绑定的是
                 * 中间带 orange 带 3 个单词的字符串(*.orange.*)
                 * Q2-->绑定的是
                 * 最后一个单词是 rabbit 的 3 个单词(*.*.rabbit)
                 * 第一个单词是 lazy 的多个单词(lazy.#)
                 *
                 */
                 Map<String, String> bindingKeyMap = new HashMap<>();
                 bindingKeyMap.put("quick.orange.rabbit","被队列 Q1Q2 接收到");
                 bindingKeyMap.put("lazy.orange.elephant","被队列 Q1Q2 接收到");
                 bindingKeyMap.put("quick.orange.fox","被队列 Q1 接收到");
                 bindingKeyMap.put("lazy.brown.fox","被队列 Q2 接收到");
                 bindingKeyMap.put("lazy.pink.rabbit","虽然满足两个绑定但只被队列 Q2 接收一次");
                 bindingKeyMap.put("quick.brown.fox","不匹配任何绑定不会被任何队列接收到会被丢弃");
                 bindingKeyMap.put("quick.orange.male.rabbit","是四个单词不匹配任何绑定会被丢弃");
                 bindingKeyMap.put("lazy.orange.male.rabbit","是四个单词但匹配 Q2");
                 bindingKeyMap.put("only","一个单词匹配 Q1");
                 bindingKeyMap.put("only.fox","一个单词不匹配Q1消息丢弃 ");
                 for (Map.Entry<String, String> bindingKeyEntry: bindingKeyMap.entrySet()){
                     String bindingKey = bindingKeyEntry.getKey();
                     String message = bindingKeyEntry.getValue();
                     channel.basicPublish(EXCHANGE_NAME,bindingKey, null, message.getBytes("UTF-8"));
                     System.out.println("生产者发出消息" + message);
                 }
             }
         }
     }
     ```

   + 消费者1

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 消费者C1对应队列Q1
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class ReceiveLogsTopic01 {
         private static final String EXCHANGE_NAME = "topic_logs";
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             channel.exchangeDeclare(EXCHANGE_NAME, "topic");
             //声明 Q1 队列与绑定关系
             String queueName="Q1";
             channel.queueDeclare(queueName, false, false, false, null);
             channel.queueBind(queueName, EXCHANGE_NAME, "*.orange.*");
             channel.queueBind(queueName,EXCHANGE_NAME,"only");
             System.out.println("等待接收消息.....");
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 System.out.println(" 接 收 队 列 :"+queueName+" 绑 定 键:"+delivery.getEnvelope().getRoutingKey()+",消息:"+message);
             };
             channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
             });
         }
     }
     ```

   + 消费者2

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 消费者C2对应队列Q2，匹配消息RoutingKey第三个单词为rabbit长度为3个单词的和首字母为lazy的
      * @创建日期 2023/11/07
      * @since 1.0.0
      */
     public class ReceiveLogsTopic02 {
         private static final String EXCHANGE_NAME = "topic_logs";
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             channel.exchangeDeclare(EXCHANGE_NAME, "topic");
             //声明 Q2 队列与绑定关系
             String queueName="Q2";
             channel.queueDeclare(queueName, false, false, false, null);
             channel.queueBind(queueName, EXCHANGE_NAME, "*.*.rabbit");
             channel.queueBind(queueName, EXCHANGE_NAME, "lazy.#");
             System.out.println("等待接收消息.....");
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 System.out.println(" 接 收 队 列 :"+queueName+" 绑定键:"+delivery.getEnvelope().getRoutingKey()+",消息:"+message);
             };
             channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
             });
         }
     }
     ```

   + 测试效果

     【生产者消息发送】

     > 一共发送10条，Q1接收者会显示所有Q1能收到的，有4条；Q2接收者会收到所有Q2能收到的，有5条；有3条丢弃
     >
     > 被两个都收到的有2条【4+5-2+3=10】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/9fbffb59f6b444099e9589d764f6badc主题模式生产者.png)

     【消费者1接收消息】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/6d1b72c243bb4044ac8ce2d5a29a99b7主题模式消费者1.png)

     【消费者2接收消息】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/1be5a38d066f4a068aedcc36c3eb031c主题模式消费者2.png)

     【交换机绑定情况】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/07/23c185e73faf41bb94924b701f9c632dtopic交换机绑定情况.png)



# 死信队列

> 死信：无法被消费的消息，某些时候可能由于特定的原因导致队列中的某些消息无法被消费，这样的消息如果没有即时处理，就变成了死信，<font color=red>？死信队列是有死信的队列还是全是死信的队列</font>【感觉像将无法消费的信息专门放在一个队列方便有条件了再处理，这样的队列称为死信队列】。
>
> 绑定死信交换机的队列的Features字段会显示DLX和DLK，分别表示死信交换机和绑定死信交换机和死信队列的RoutingKey
>
> 应用场景：
>
> + 为了保证订单业务的消息数据不丢失，需要使用到 RabbitMQ 的死信队列机制，当消息消费发生异常时，将消息投入死信队列中，等待环境好转之后再将死信队列中的消息进行消费，防止消息丢失
> + 死信队列可以做一些延迟消息的处理，死信可以在指定的时间内被消费者消费
>   + 如用户在商城下单成功并点击去支付后在指定时间未支付时消息的自动失效  



1. 死信的来源

   + 消息TTL【Time to Live】消息存活时间过期

     > 过期的消息贝能再被消费

   + 队列达到最大长度

     > 队列满了，无法再添加数据到消息队列中

   + 消息被拒，消息在应答的时候进行了拒绝应答【basic.reject】或者否定应答【basic.nack】且`requeue=false`设置了消息不放回队列中

     > 让这种消息不要重新放在队列中进行消费，将其放在死信队列中等后期有条件了再进行后续处理

2. 死信场景搭建

   + 场景架构

     > 一个生产者走直接交换机，正常情况通过RoutingKey=zhangsan被消费者C1消费，消息遇上三种情况之一成为死信，死信会被马上转发到死信交换机【是一个直接交换机dead_exchange】，通过自定义RoutingKey=lisi转发到死信队列被C2消费
     >
     > 消费者包括C1正常队列消费者和C2死信队列消费者
     >
     > 队列包含正常队列和死信队列
     >
     > 交换机包含一个正常交换机和一个死信交换机【都是直接类型，一个和正常队列绑定，一个和死信队列绑定】
     >
     > 生产者一个
     >
     > 要点：
     >
     > + 在C1中要声明两个交换机和两个队列，因为要让正常队列出现死信立刻转发给死信交换机
     > + C2消费者正常写，只负责消费死信队列中的消息【这个逻辑还是不复杂，因为死信队列的消息一过来就被另一个消费者正常消费了】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/05a607981ca946a7a462fb3460c2dcf0死信场景结构.png)

## 消息过期

1. 代码实现

   > 在控制台可以看见普通队列的Message字段的Ready到消息过期时间会递减，死信队列的Ready会递增，注意这里面似乎还有延迟，即在完全确认死信队列收到消息以前，原队列的消息不会立即删除
   >
   > 场景：在消费者中声明2个交换机和两个队列，普通队列声明时设置参数绑定死信交换机；开启消费者1创建对应的交换机和队列后关闭消费者1模拟正常消费者宕机，在生产者中设置消息的过期时间，让普通队列中的消息等待足够时间过期自动进入死信队列【为了观察到消息进入死信队列的渐进效果，设置消息每隔1s发送一次】，死信完全进入死信队列后，启动消费者2消费死信队列中的消息

   + 消费者C1

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 C1消费者，要先启动C1将两个交换机和两个队列创建出来再关掉C1，模拟C1消费者宕机无法处理消息的情况,让消息超过有效时间成为死信，自动转发到死信交换
      * @创建日期 2023/11/08
      * @since 1.0.0
      */
     public class Consumer01 {
         //普通交换机名称
         private static final String NORMAL_EXCHANGE = "normal_exchange";
         //死信交换机名称
         private static final String DEAD_EXCHANGE = "dead_exchange";
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             //声明死信和普通交换机 类型为 direct
             channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
             channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);
     
             //声明死信队列，死信队列就当成正常队列声明就行，他的消息接收是有死信交换机解决的，而死信交换机的消息接收由普通队列转发控制的
             String deadQueue = "dead-queue";
             channel.queueDeclare(deadQueue, false, false, false, null);
             //死信队列绑定死信交换机与 routingkey
             channel.queueBind(deadQueue, DEAD_EXCHANGE, "lisi");
     
             //正常队列绑定死信队列信息,这个绑定将作为参数用在正常队列的声明中。参数包括死信交换机的名字，key为x-dead-letter-exchange是固定的
             //以及绑定死信交换机对应死信队列的RoutingKey
             Map<String, Object> params = new HashMap<>();
             //过期时间，单位默认是毫秒，这个参数可以不设置，因为生产者发送消息可以定制化每个消息的过期时间，企业一般都是生产者发消息的时候设置，好处是可以定制化过期时间
             //在这里设置会导致该队列的所有消息过期时间都是10s
             //params.put("x-message-ttl",10000);
             //正常队列设置死信交换机 参数 key 是固定值
             params.put("x-dead-letter-exchange", DEAD_EXCHANGE);
             //正常队列设置死信 routing-key 参数 key 是固定值
             //下面死信交换机绑定了死信队列的话，这里的x-dead-letter-routing-key就可以不写，不写会走默认绑定的routingKey
             params.put("x-dead-letter-routing-key", "lisi");
     
             //声明普通队列
             String normalQueue = "normal-queue";
             //正常队列的消息成为死信，要将其转发给死信队列必须设置死信队列的交换机和死信交换机对应死信队列的RoutingKey
             //靠其他参数的死信交换机名字和绑定死信队列的RoutingKey设置决定消息成为死信后的转发地址
             channel.queueDeclare(normalQueue, false, false, false, params);
             channel.queueBind(normalQueue, NORMAL_EXCHANGE, "zhangsan");
     
             //接收消息回调
             System.out.println("等待接收消息.....");
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 System.out.println("Consumer01 接收到消息"+message);
             };
     
             //准备接收消息
             channel.basicConsume(normalQueue, true, deliverCallback, consumerTag -> {
             });
         }
     }
     ```

   + 消费者C2

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 处理死信队列的消息
      * 为了解耦合，可以把队列和交换机的声明单独写一个类，也避免启动先后导致的错误问题，当然这种方式还是无法解决临时队列的名字获取问题
      * @创建日期 2023/11/08
      * @since 1.0.0
      */
     public class Consumer02 {
         private static final String DEAD_EXCHANGE = "dead_exchange";
         public static void main(String[] argv) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
             channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);
             String deadQueue = "dead-queue";
             channel.queueDeclare(deadQueue, false, false, false, null);
             channel.queueBind(deadQueue, DEAD_EXCHANGE, "lisi");
             System.out.println("等待接收死信队列消息.....");
             DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                 String message = new String(delivery.getBody(), "UTF-8");
                 System.out.println("Consumer02 接收死信队列的消息" + message);
             };
             channel.basicConsume(deadQueue, true, deliverCallback, consumerTag -> {
             });
         }
     }
     ```

   + 生产者

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 消息生产者【不需要知道消息可能去到死信队列，正常写即可，但是这里添加了设置消息的存活时间】
      * @创建日期 2023/11/08
      * @since 1.0.0
      */
     public class Producer {
         private static final String NORMAL_EXCHANGE = "normal_exchange";
         public static void main(String[] argv) throws Exception {
             try (Channel channel = RabbitMQUtil.getChannel()) {
                 channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
                 //设置消息的 TTL 时间，单位ms，链式编程，10s内消息没有被正常接收就会被转发到死信交换机
                 AMQP.BasicProperties properties = new AMQP.BasicProperties(). builder().expiration("10000").build();
                 //该信息是用作演示队列个数限制
                 for (int i = 1; i <11 ; i++) {
                     String message="info"+i;
                     //发完睡1s，实现Ready字段递减可被观察的效果
                     SleepUtil.sleepInSecond(1);
                     //发送设置参数包括消息的有效时间
                     channel.basicPublish(NORMAL_EXCHANGE, "zhangsan", properties, message.getBytes());
                     System.out.println("生产者发送消息:"+message);
                 }
             }
         }
     }
     ```

   + 测试效果

     【生产者发送消息】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/2a327ca7e0a04b3e991b85cebfa1669f生产者发送消息.png)

     【消息发送到普通队列】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/6f0dd348474946e3a2df80df44042662生产者发送消息到普通队列.png)

     【消息超时进入死信队列】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/41478e397ff84187a5285b4a220042af消息过期被转发到死信队列.png)

     【启动消费者死信被消费】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/0541620867f844adb6a079fcad79dab2死信消息被消费.png)

   

## 队列达到最大长度

> 指普通队列达到最大长度后放不下的消息会立即成为死信



1. 验证流程：

   + 通过在消费者1中普通队列声明其他参数补上`x-max-length,6`设置普通队列的长度仅为6，第二个参数是int类型，

     + 更改队列属性一定要将原队列删掉【可以在声明队列时把autoDelete设置为true，这样就不用每次手动删除队列了，每次断开链接会自动删除】

     + 限制了最大长度的队列会在Features字段显示Lim表示限制了长度【？搜索一下RabbitMQ的队列长度是多少，如何设置】

       > 限制了长度显示lim，其他参数features会显示args，在args中显示对应的参数

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/ffc3feb278da4bd4b8a14ab281019821队列参数展示.png)

     + 未能进入队列的消息将会成为死信被转发到死信交换机；

       ```java
       //设置队列参数
       Map<String, Object> params = new HashMap<>();
       //正常队列设置死信交换机 参数 key 是固定值
       params.put("x-dead-letter-exchange", DEAD_EXCHANGE);
       //正常队列设置死信 routing-key 参数 key 是固定值
       params.put("x-dead-letter-routing-key", "lisi");
       params.put("x-max=length",6);
       
       //声明普通队列
       String normalQueue = "normal-queue";
       //正常队列的消息成为死信，要将其转发给死信队列必须设置死信队列的交换机和死信交换机对应死信队列的RoutingKey
       //靠其他参数的死信交换机名字和绑定死信队列的RoutingKey设置决定消息成为死信后的转发地址
       channel.queueDeclare(normalQueue, false, false, false, params);
       ```

   + 把生产者的消息TTL设置成没有过期时间【为了效果明显】，只启动生产者，让消息在队列中积压，观察两个队列的数据数量

2. 测试

   + 删除正常队列

   + 启动消费者1

     > 生成被限制了长度的队列后关闭消费者1

   + 启动生产者

   + 测试效果

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/9f49e4a2f3a84d0abf235c228da704eb消息数量超过队列长度.png)

     

## 消息被拒

> 一般队列对应的消费者拒绝对应消息，该消息可以被设置重新放回队列，也可以选择立即成为死信转发到死信交换机
>
> 使用消息拒绝必须关闭自动应答，使用手动应答的方式确认消息，自动应答不存在消息拒绝
>
> 在queue界面中能点击get message获取当前队列中有哪些消息

1. 验证流程

   + 将普通队列声明中的自动应答改成手动应答，当消息匹配info3时拒绝该消息，其他消息时手动确认应答

     > 拒绝消息在消息接收回调中，实际拒绝还是接收到了该消息，只是使用拒绝方法以后让消息重新回到队列或者直接转发到死信队列

     ```java
     DeliverCallback deliverCallback = (consumerTag, delivery) -> {
         String message = new String(delivery.getBody(), "UTF-8");
         if ("info3".equals(message)){
             System.out.println("Consumer01 拒绝的消息:"+message);
             //拒绝消息且不放回原队列
             channel.basicReject(delivery.getEnvelope().getDeliveryTag(),false);
         }else {
             System.out.println("Consumer01 接收到消息:"+message);
             channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
         }
     };
     
     //改为手动应答测试消息拒绝
     channel.basicConsume(normalQueue, false, deliverCallback, consumerTag -> {
     });
     ```

   + 删除普通队列并重新生成

   + 生产者发送消息

2. 测试效果

   + 发送消息

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/695e135801d140b1a34889433cd7315a消息生产者.png)

   + 接收消息

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/46661763c46745009eaa306c2c02739e消费者1.png)

   + 死信队列中的消息

     > properties中的queue为一般队列表示从一般队列转发来的，这张图确认是在死信队列中截取的

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/64d016b830e3496db802cc24da10a4bc死信队列中的消息.png)





# 延迟队列

> 延迟队列是死信队列的一种，延迟队列就是TTL过期，没有配置死信队列过期消息会被丢弃，配置了死信队列消息会被发送到死信队列，不设置TTL表示消息永远不会过期，
>
> 通过死信队列消息过期的演示，生产者将带有有效期限的消息发送给绑定一般消费者的直接交换机，一般消费者宕机，消息等待10s后变成死信，死信被转发给死信交换机，发送给死信队列，死信队列发送给死信消费者，这期间消息从生产者到消费者，中间经历的时间是消息的有效时间10s，那么完全可以让消息变成死信后被消费来实现让消息保持一定的时间后再被消费的需求
>
> 核心就是消息超时变成死信+消费者一直消费死信
>
> 总结：
>
> + 两种延迟队列，其中一种是基于死信的，一种是基于插件的；使用RabbitMQ实现延迟队列可以很好的实现RabbitMQ的特性【消息发送和投递的可靠性、死信队列保障消息至少被消费一次以及消息未被正确处理时成为死信不会被丢弃】，通过RabbitMQ集群特性不会让RabbitMQ单个节点挂点导致延时队列不可用或消息丢失
> + 还有其他实现延迟队列的选择，Java中的DelayQueue【消息可能丢失】，Redis的zset，Quartz【定时器】或者Kafka的时间轮，根据特点和场景实现；RabbitMQ更加可靠

1. 延迟队列的应用场景

   > 都是设定消息的有效时长实现在某个事件发生之后或者之前指定时长进行处理，这里面的判断条件都在消息消费时判断吗？【好像是】
   >
   > 数据量较小的情况下，可以使用定时任务每隔几秒查一下条件数据状态，条件成立就执行后续操作【如支付时间一周的账单每晚跑一次定时任务检查一下支付状态】；
   >
   > 数据量比较大且时效性比较强的场景，如十分钟未支付取消订单，活动期间数据量可能达到百万甚至千万，并发量高，对这么多的数据使用定时任务查数据库状态响应时间慢，数据库压力大，性能低下，还可能耗死服务器

   + 订单十分钟未支付自动取消
   + 新创建的店铺十天内没有上传过商品自动发送消息提醒
   + 用户注册三天内没有登录发送短信提醒
   + 用户发起退款三天内没有得到处理则通知运营人员
   + 预定会议后在预定时间点前十分钟通知相关人员参加会议

2. 业务逻辑流程示例

   + 用户下订单后会预定座位，订单超30分钟未支付座位重新回票池【这个就是消息队列触发的，将座位添加会坐席数据库中】，订单取消 

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/cd6dbc08602845f79a7b0daef3802f4a业务流程结构图.png)

## SpringBoot整合RabbitMQ

> 建一个SpringBoot工程

1. pom.xml

   ```xml
   <dependencies>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter</artifactId>
       </dependency>
       <!--RabbitMQ 依赖-->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-amqp</artifactId>
       </dependency>
       <dependency>
       <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-web</artifactId>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-test</artifactId>
           <scope>test</scope>
       </dependency>
       <dependency>
           <groupId>com.alibaba</groupId>
           <artifactId>fastjson</artifactId>
           <version>1.2.47</version>
       </dependency>
       <dependency>
           <groupId>org.projectlombok</groupId>
           <artifactId>lombok</artifactId>
       </dependency>
       <!--swagger-->
       <dependency>
           <groupId>io.springfox</groupId>
           <artifactId>springfox-swagger2</artifactId>
           <version>2.9.2</version>
       </dependency>
       <dependency>
           <groupId>io.springfox</groupId>
           <artifactId>springfox-swagger-ui</artifactId>
           <version>2.9.2</version>
       </dependency>
       <!--RabbitMQ 测试依赖-->
       <dependency>
           <groupId>org.springframework.amqp</groupId>
           <artifactId>spring-rabbit-test</artifactId>
           <scope>test</scope>
       </dependency>
   </dependencies>
   ```

2. application.yml

   ```yml
   #rabbitmq的配置
   spring.rabbitmq.host=192.168.200.132
   spring.rabbitmq.port=5672
   spring.rabbitmq.username=earl
   spring.rabbitmq.password=123456
   ```

3. 启动类

   ```java
   @SpringBootApplication
   public class Application {
       public static void main(String[] args) {
           SpringApplication.run(Application.class, args);
       }
   }
   ```

4. 配置swagger

   ```java
   @Configuration
   @EnableSwagger2
   public class SwaggerConfig {
       @Bean
       public Docket webApiConfig(){
           return new Docket(DocumentationType.SWAGGER_2)
           .groupName("webApi")
           .apiInfo(webApiInfo())
           .select()
           .build();
       }
       private ApiInfo webApiInfo(){
           return new ApiInfoBuilder()
                   .title("rabbitmq 接口文档")
                   .description("本文档描述了 rabbitmq 微服务接口定义")
                   .version("1.0")
                   .contact(new Contact("Earl", "http://concurrent.cn", "2625074321@qq.com"))
                   .build();
       }
   }
   ```



## 延迟队列实现

> 整合SpringBoot实现向RabbitMQ发送消息，SpringBoot对RabbitMQ的javaApi进行了封装

1. 架构图

   > 三个队列，分别为QA、QB【QA、QB为普通队列】、QD【死信队列】，X为普通交换机，Y为延迟交换机；
   >
   > 设置两个普通队列的延迟时间分别为10s和40s，不同的业务选择不同的RoutingKey就能够匹配不同的延迟时间
   >
   > P发消息，C接收消息

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/9cd05c10527f46d8820102bf3ab1802d代码架构.png)

2. 在原始的RabbitMQ代码中，死信队列绑定在普通队列的其他参数中，整合了SpringBoot后，专门就有一个配置类去配置声明普通交换机，死信交换机...，普通队列、死信队列，不需要消费者或生产者再负责交换机和队列的声明

   > 声明包括两个交换机和三个队列，两个交换机和3个队列的绑定关系，两个普通队列与死信交换机的转发关系

   + 配置类

     > 要点：
     >
     > 1. 交换机、队列和绑定都需要以向Spring容器注入的方式来实现声明和创建，简单的声明只需要使用相应的类传参名字即可，复杂的声明需要使用对应的Builder，如ExchangeBuilder、QueueBuilder和BindingBuilder，这些对象都是org.springframework.amqp.core.包下定义的，用法基本见名知意，按名字设置即可
     > 2. 队列和死信交换机的关系只需要在队列声明中传参死信交换机的名字和RoutingKey，凡是需要转发到死信交换机的队列都要单独进行传参，参数传递仍然使用map，参数名和原来的相同
     
     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 RabbitMQ的相关配置
      * 队列转发到死信交换机不需要单独的绑定，只需要声明队列的时候传参死信交换机和RoutingKey
      * @创建日期 2023/11/09
      * @since 1.0.0
      */
     @Configuration
     public class RabbitMQConfig {
         //普通交换机名字
         public static final String X_EXCHANGE = "X";
         //死信交换机的名字
         public static final String Y_DEAD_LETTER_EXCHANGE = "Y";
         //普通队列名字
         public static final String QUEUE_A = "QA";
         public static final String QUEUE_B = "QB";
         //死信队列名字
         public static final String DEAD_LETTER_QUEUE = "QD";
     
         /**
          * @return {@link DirectExchange }
          * @描述  以org.springframework.amqp.core.DirectExchange注入spring容器 声明直接交换机 xExchange
          * 传参交换机的名字
          * 交换机也有ExchangeBuilder
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/09
          * @since 1.0.0
          */
         @Bean("xExchange")
         public DirectExchange xExchange(){
             return new DirectExchange(X_EXCHANGE);
         }
         // 声明 xExchange
         @Bean("yExchange")
         public DirectExchange yExchange(){
             return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
         }
     
         /**
          * @return {@link Queue }
          * @描述 org.springframework.amqp.core.Queue注入Spring容器声明队列
          * 创建HashMap放入对应死信交换机和RoutingKey，以及队列中消息的有效时间10s
          * 用静态方法QueueBuilder.durable(QUEUE_A).withArguments(args).build()，声明持久化和传递其他参数，为啥队列名要放在durable中
          * 用的QueueBuilder，队列绑定死信交换机只针对当前队列，多个队列绑定同一个死信交换机需要在每个队列中都声明一次
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/09
          * @since 1.0.0
          */
         @Bean("queueA")
         public Queue queueA(){
             Map<String, Object> args = new HashMap<>(3);
             //声明当前队列绑定的死信交换机
             args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
             //声明当前队列的死信路由 key
             args.put("x-dead-letter-routing-key", "YD");
             //声明队列的 TTL,单位ms
             args.put("x-message-ttl", 10000);
             return QueueBuilder.durable(QUEUE_A).withArguments(args).build();
         }
     
         /**
          * @param queueA
          * @param xExchange
          * @return {@link Binding }
          * @描述 声明队列 A 绑定 X 交换机，绑定也要单独以Binding的形式传递给Spring容器，用的BuildingBuilder
          * 绑定不需要被调用，不用被起名
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/09
          * @since 1.0.0
          */
         @Bean
         public Binding queueABindingX(@Qualifier("queueA") Queue queueA,
                                       @Qualifier("xExchange") DirectExchange xExchange){
             return BindingBuilder.bind(queueA).to(xExchange).with("XA");
         }
     
         //声明队列 B ttl 为 40s 并绑定到对应的死信交换机
         @Bean("queueB")
         public Queue queueB(){
             Map<String, Object> args = new HashMap<>(3);
             //声明当前队列绑定的死信交换机
             args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
             //声明当前队列的死信路由 key
             args.put("x-dead-letter-routing-key", "YD");
             //声明队列的 TTL
             args.put("x-message-ttl", 40000);
             return QueueBuilder.durable(QUEUE_B).withArguments(args).build();
         }
     
         //声明队列 B 绑定 X 交换机
         @Bean
         public Binding queueBBindingX(@Qualifier("queueB") Queue queue1B,
                                       @Qualifier("xExchange") DirectExchange xExchange){
             return BindingBuilder.bind(queue1B).to(xExchange).with("XB");
         }
     
         /**
          * @return {@link Queue }
          * @描述 一般的queue声明如果只需要设置名字不需要设置其他参数可以直接返回Queue对象传参队列名称，不需要使用QueueBuilder
          * 声明死信队列
          * @Qualifier注解是spring中的注解，根据ID进行注入。自动是根据参数根据类型注入，多个相同类型的bean必须指定ID
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/09
          * @since 1.0.0
          */
         @Bean("queueD")
         public Queue queueD(){
             return new Queue(DEAD_LETTER_QUEUE);
         }
         //声明死信队列 QD 绑定关系
         @Bean
         public Binding deadLetterBindingQAD(@Qualifier("queueD") Queue queueD,
                                             @Qualifier("yExchange") DirectExchange yExchange){
             return BindingBuilder.bind(queueD).to(yExchange).with("YD");
         }
     }
     ```
     
   + 生产者
   
     > 发送延迟消息的控制器方法
     >
     > 通过控制器方法实现通过请求的方式使用rabbitTemplate传参交换机，RoutingKey和消息本身实现向消息队列传递消息
   
     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 发送订单消息的控制器
      * @创建日期 2023/11/09
      * @since 1.0.0
      */
     @Slf4j
     @RestController
     @RequestMapping("/order")
     public class OrderController {
         //使用rabbitTemplate来实现向交换机发送消息
         @Autowired
         private RabbitTemplate rabbitTemplate;
     
         @GetMapping("/sms/{message}")
         public void sendMessageToMQ(@PathVariable String message){
             log.info("当前时间: {} -- 您有新的未支付订单:{}",new Date(),message);
             //指定交换机名字和RoutingKey，以及消息本身
             rabbitTemplate.convertAndSend("X", "XA", "消息来自有效时长为10S的队列: "+message);
             rabbitTemplate.convertAndSend("X", "XB", "消息来自有效时长为40S的队列: "+message);
         }
     }
     ```
   
   + 消费者
   
     > 使用RabbitListener注解指定监听的队列实现对消息的处理，实际肯定是用了反射，接收到队列QD的消息，获取到消息，调用该方法进行对消息的处理
   
     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 接收的是延迟队列的消息
      * @创建日期 2023/11/09
      * @since 1.0.0
      */
     @Slf4j
     @Component
     public class OrderConsumer {
         //使用RabbitListener注解指定监听的队列实现对消息的处理，实际肯定是用了反射，接收到队列QD的消息，获取到消息，调用该方法进行对消息的处理
         @RabbitListener(queues = "QD")
         public void confirmOrderMessage(Message message, Channel channel) throws IOException {
             String msg = new String(message.getBody(),"UTF-8");
             log.info("当前时间: {} ,收到死信队列信息 {} ", new Date().toString(), msg);
         }
     }
     ```
   
   + 测试效果
   
     > 消息发送以后10s和40s收到延迟消息
   
     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/b1915405863a45e1b453b74935ba0219测试情况.png)
   



## 延迟队列优化

> 上述延迟队列的不足之处
>
> + 每增加一个新的时间需求，就要增加一个新队列，对于预定会议室这种提前通知的场景，这种设计需要增加无数个队列
> + 延迟时间也可能临时改
>
> 为啥不能发送消息的时候指定消息的有效时间，是可以的，这里只是作为讲解加深印象，添加一个没有设置消息有效时间的通用队列，发消息时指定消息的有效时间，通过该队列随意控制消息的延迟时间

1. 优化架构

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/08/28bebef34f8b41b096ad322b000c68ae架构图.png)

2. 实现

   + 在RabbitMQ配置类中添加配置文件类代码，

     > 添加不设置消息过期时间的QC，这种有一种很明显的缺点：官网还专门写了一个警告，消息可能已经过期了但是没有到队列头会被困在队列里。直到轮到该消息到队列头才会被转发到死信队列被消费
     >
     > 重点是生产者如何使用SpringBoot的api发送消息，在convertAndSend方法的第四个参数中设置函数式接口CorrelationData的实现类，设置其中的message.expiration来设置消息的有效时长，传递的是字符串的时间毫秒数

     ```java
     //通用队列QC【不设置TTL】
     public static final String QUEUE_C= "QC";
     
     @Bean
     public Queue queueC(){
         //QC绑定消息转发死信交换机
         Map<String,Object> arguments=new HashMap<>(3);
         arguments.put("x-dead-letter-exchange",Y_DEAD_LETTER_EXCHANGE);
         arguments.put("x-dead-letter-routing-key","YD");
         //构建队列
         return QueueBuilder.durable(QUEUE_C).withArguments(arguments).build();
     }
     
     @Bean
     public Binding queueCBindingX(){
         //经过测试，这个创建绑定关系的写法是完全没问题的，DestinationType.QUEUE就是表示这个目的地是一个队列
         //return new Binding(QUEUE_C, Binding.DestinationType.QUEUE,X_EXCHANGE,"XC",null);
         //QC绑定普通交换机
         return BindingBuilder.bind(queueC()).to(xExchange()).with("XC");
     }
     ```

   + 在生产者发送指定延迟时间的消息

     > 在convertAndSend方法的第四个参数中设置函数式接口CorrelationData的实现类，设置其中的message.expiration来设置消息的有效时长

     ```java
     /**
      * @param message
      * @param ttl
      * @描述 通过在发送消息时设置CorrelationData实现类的messageProperties属性的expiration属性为预期时长实现设置消息的有效时间
      * @author Earl
      * @version 1.0.0
      * @创建日期 2023/11/09
      * @since 1.0.0
      */
     @GetMapping("/custom/{message}/{ttl}")
     public void sendCustomTTLMessage(@PathVariable String message,@PathVariable String ttl){
         log.info("当前时间: {} ,发送一条有效时长为{}s的信息给队列QC:{}",new Date(),Integer.parseInt(ttl)/1000,message);
         rabbitTemplate.convertAndSend("X","XC",message,correlationData->{
             correlationData.getMessageProperties().setExpiration(ttl);
             return correlationData;
         });
     }
     ```

   + 测试效果

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/bb63c773cd8e4070a384289d8a1ffb44自定义延迟队列.png)



## 延迟队列缺陷

> 基于死信存在的问题，即消息可能已经过期了但是还没有到队列头会被困在队列里，直到轮到该消息到队列头才会被转发到死信队列被消费
>
> 就是不设置消息存活时间的队列，可能存在消息到期了但是不在队列头出不去，直到在其前面的所有消息都过期了才能出队列被消费，无法形成一个通用的延时队列，使用过程中基本上必出现消息过期但是被卡的情况

1. 缺陷情况演示

   + 向普通队列QC先后发送请求`http://localhost:8001/order/custom/你好1/2000`和`http://localhost:8001/order/custom/你好2/2000`

     > 使用在消息属性上设置 TTL 的方式，消息可能并不会按时“死亡“，因为 RabbitMQ 只会检查第一个消息是否过期，如果过期则丢到死信队列，如果第一个消息的延时时长很长，而第二个消息的延时时长很短，第二个消息并不会优先得到执行。

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/c646feb7a6044970846a5151abbe0dfa延迟队列缺陷.png)

2. 安装RabbitMQ插件解决延迟队列缺陷

   + 官网下载插件rabbitmq_delayed_message_exchange，放在RabbitMQ的插件目录`/usr/lib/rabbitmq/lib/rabbitmq_server-3.8.8/plugins  `   

     > 这个插件不会实时更新，一直会维持放进去时候的情况

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/955614d903844ade9aac8b739d065258rabbitmq插件.png)

   + 执行命令`  `让插件生效并使用命令`systemctl restart rabbitmq-server`重启RabbitMQ

     > 安装不需要写插件的版本号

   + 弄好之后在前端控制台的exchange列表中点击添加交换机多出来一个`x-delayed-message`类型的交换机，同时也意味着延迟消息不由队列控制，由交换机来控制

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/546e933cc6da49b2b875bb859a17394b安装rabbitmq插件效果.png)

     

## 基于插件的延迟队列

1. 架构图

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/0e139ff0d8674eb4ba9413f80a49b5c3基于插件的延时队列.png)

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/d4f3b2c002734035ad6eaac45ff61ae6基于插件的延迟队列.png)

2. 代码实现

   + 配置类

     > 声明交换机，交换机必须是延迟类型；声明队列；绑定延迟交换机和队列
     >
     > + 核心是自定义类型声明延迟交换机，设置参数`k=v`为`x-delayed-type=direct`，构建交换机传参类型`x-delayed-message`
     > + 构建绑定对象除了传参队列对象，延迟交换机，路由key，还要使用noargs方法构建，注意这个RoutingKey好像是固定的就为`delayed.routingkey`

     ```java
     @Configuration
     public class DelayedQueueConfig {
         public static final String DELAYED_QUEUE_NAME = "delayed.queue";
         public static final String DELAYED_EXCHANGE_NAME = "delayed.exchange";
         public static final String DELAYED_ROUTING_KEY = "delayed.routingkey";
         @Bean
         public Queue delayedQueue() {
         	return new Queue(DELAYED_QUEUE_NAME);
         }
     
         /**
          * @return {@link CustomExchange }
          * @描述 自定义交换机 我们在这里定义的是一个延迟交换机；不明白这里为什么key-value是x-delayed-type和direct
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/09
          * @since 1.0.0
          */
         @Bean
         public CustomExchange delayedExchange() {
             Map<String, Object> args = new HashMap<>();
             //自定义交换机的类型,放入自定义交换机的构建参数中
             args.put("x-delayed-type", "direct");
             //自定义延迟交换机需要声明类型为"x-delayed-message"，以及x-delayed-type为direct。延迟交换机的RoutingKey是固定值delayed.routingkey
             //猜测延迟交换机是一个直接交换机
             return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
         }
         @Bean
         public Binding bindingDelayedQueue(@Qualifier("delayedQueue") Queue queue,
                                            @Qualifier("delayedExchange") CustomExchange delayedExchange) {
             //自定义交换机的绑定不带参数的构建必须使用noargs方法进行构建
             return BindingBuilder.bind(queue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
         }
     }
     ```

   + 生产者

     > 注意correlationData对延迟交换机设置delay属性
     >
     > ?设置延迟是对交换机设置的吗？设置延迟对设置消息在队列中的过期有效果吗

     ```java
     /**
      * @param message
      * @param delayTime
      * @描述 发送延迟消息到使用插件实现的延迟交换机
      * @author Earl
      * @version 1.0.0
      * @创建日期 2023/11/09
      * @since 1.0.0
      */
     @GetMapping("sendDelayMsg/{message}/{delayTime}")
     public void sendMsg(@PathVariable String message,@PathVariable Integer delayTime) {
         rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME,
                 DelayedQueueConfig.DELAYED_ROUTING_KEY,
                 message,
                 //设置延迟是对交换机设置的吗？设置延迟对设置消息在队列中的过期有效果吗
                 correlationData ->{
                     correlationData.getMessageProperties().setDelay(delayTime);
                     return correlationData;
                 });
         log.info(" 当前时间:{},发送一条延迟{}毫秒的信息给队列delayed.queue:{}", new Date(),delayTime, message);
     }
     ```

   + 消费者

     > 正常接收即可

     ```java
     public static final String DELAYED_QUEUE_NAME = "delayed.queue";
     
     /**
      * @param message
      * @描述 接收基于延迟插件的延迟交换机的延时消息的接收
      * @author Earl
      * @version 1.0.0
      * @创建日期 2023/11/09
      * @since 1.0.0
      */
     @RabbitListener(queues = DELAYED_QUEUE_NAME)
     public void receiveDelayedQueue(Message message) throws UnsupportedEncodingException {
         //这里不用转成专门转成UTF-8，不知道为啥会抛异常，以前使用不会抛异常
         String msg = new String(message.getBody(),"UTF-8");
         log.info("当前时间： {},收到延时队列的消息： {}", new Date().toString(), msg);
     }
     ```

   + 测试效果

     > 先后发送链接：`http://localhost:8001/order/delay/订单消息/20000`和`http://localhost:8001/order/delay/订单消息2/2000`

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/09/e38db92daae14958babfceb21c41eaae测试效果.png)



# 发布确认高级

> 生产环境中由于一些不明原因导致 rabbitmq 重启，在 RabbitMQ 重启期间生产者消息投递失败，导致消息丢失，需要手动处理和恢复。如何才能进行 RabbitMQ 的消息可靠投递呢？且RabbitMQ 集群不可用的极端情况下，无法投递的消息该如何处理呢？此时生产者的报错信息为队列不可用异常，提示队列不存在或消息队列不可用
>
> 消息发送后一直得不到确认就会报异常，不能一直等着回应，把消息丢失，然后就引入了发布确认高级模式，等不到交换机和队列确认应答就叫消息放入缓存，使用定时任务发送缓存消息
>
> 发布确认是生产者和交换机之间的事情，消息应答才是交换机、队列和消费者之间的事情

1. RabbitMQ重启期间的两种消息丢失情况

   > 生产者不知道消息队列的情况，只管发送消息，消息发送出去找不到交换机或者队列，消息就没了

   + 队列不可用
   + 消息队列整体挂掉，交换机不可用

2. 解决方案

   + 应该存在一个缓存，当消息经过交换机找不到队列暂时进入缓存，或者消息找不到交换机暂时也进入缓存，通过定时任务对未成功发送的消息重新投递

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/8adb4cac9a7143b0a9f63e35b5302fa2发布确认高级.png)

## 交换机不可用

> 以下代码只能针对交换机不可用的情况，对交换机收到消息，但是队列找不到的情况毫无办法
>
> 就是通过实现rabbitTemplate的一个回调接口，通过一个标志交换机是否接收到消息来分流对消息的处理

1. 代码实现

   + 配置类

     > 一个直接交换机，一个队列，一个绑定关系
     >
     > 正常绑定

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 发布确认高级的架构配置类
      * @创建日期 2023/11/10
      * @since 1.0.0
      */
     @Configuration
     public class ConfirmConfig {
         //交换机名字和队列名字
         public static final String CONFIRM_EXCHANGE_NAME = "confirm.exchange";
         public static final String CONFIRM_QUEUE_NAME = "confirm.queue";
     
         //声明业务 Exchange，直接交换机
         @Bean("confirmExchange")
         public DirectExchange confirmExchange(){
             return new DirectExchange(CONFIRM_EXCHANGE_NAME);
         }
     
         // 声明确认队列
         @Bean("confirmQueue")
         public Queue confirmQueue(){
             return QueueBuilder.durable(CONFIRM_QUEUE_NAME).build();
         }
     
         // 声明确认队列绑定关系，RoutingKey为key1
         @Bean
         public Binding queueBinding(@Qualifier("confirmQueue") Queue queue,
                                     @Qualifier("confirmExchange") DirectExchange exchange){
             //自定义交换机这儿还额外需要加一个noargs
             return BindingBuilder.bind(queue).to(exchange).with("key1");
         }
     }
     ```

   + 生产者

     > 情况1：正常模拟消息发送被交换机和队列接收被消费回调函数执行情况
     >
     > 情况2：把交换机的名字写错模拟找不到交换机的情况，观察回调函数的执行情况和消费者消费情况
     >
     > 情况3：把RoutingKey写错，让交换机找不着队列，观察观察回调函数的执行情况和消费者消费情况
     >
     > 回调的消息是发送的时候创建CorrelationData对象，设置消息的id，消息会被自动放入该对象在回调的时候传入，区别于以前的消息发送是不带该参数的重载方法

     ```java
     /**
      * @param message
      * @描述 确认发布高级发送消息
      * @author Earl
      * @version 1.0.0
      * @创建日期 2023/11/10
      * @since 1.0.0
      */
     @GetMapping("confirm/{message}")
     public void sendMessage(@PathVariable String message){
         //指定消息 id 为 1
         CorrelationData correlationData1=new CorrelationData("1");
         String routingKey="key1";
         //发消息相较于普通方法多了一个CorrelationData参数，传参指定消息的id，这里面还有一个message类型的returnedMessage属性，会自动将发送的消息存入该属性
         rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME,routingKey,message+",id:1",correlationData1);
         log.info("发送消息内容:{}",message+",id:1");
     
         //睡一秒观察信道关闭会不会影响后续消息的发送，经过测试不会影响
         try{
             TimeUnit.SECONDS.sleep(1);
         }catch (InterruptedException e){
             e.printStackTrace();
         }
         //演示交换机名字不正确找不到交换机回调接口缓存数据
         CorrelationData correlationData2=new CorrelationData("2");
         routingKey="key1";
         rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME+2,routingKey,message+",id:2",correlationData2);
         log.info("发送消息内容:{}",message+",id:2");
     
         //睡一秒观察信道关闭会不会影响后续消息的发送
         try{
             TimeUnit.SECONDS.sleep(1);
         }catch (InterruptedException e){
             e.printStackTrace();
         }
         //演示RoutingKey不正确找不到队列交换机回调正常执行，但是消费者接收不到消息，这种情况消息仍然丢失，需要再写一个回调接口实现类
         CorrelationData correlationData3=new CorrelationData("3");
         routingKey="key3";
         rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME,routingKey,message+",id:3",correlationData3);
         log.info("发送消息内容:{}",message+",id:3");
     }
     ```

   + 回调实现类

     > 核心是交换机【注意这里不涉及队列是否接收到】不管是否接到消息都会回调，用ack标志接收状态来区分回调函数对数据的处理，但是处理不了队列找不到的情况
     >
     > 回调接口RabbitTemplate.ConfirmCallback的实现类必须通过标注了@PostConstruct的init方法注入rabbitTemplate实例的confirmCallback属性，否则实现类即使注入到Spring容器，消息发送者rabbitTemplate也找不到

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 发布确认回调高级
      * 发布确认是生产者和交换机之间的事情，消息应答才是交换机、队列和消费者之间的事情
      * 回调接口的使用还必须在配置文件配置spring.rabbitmq.publisher-confirm-type=correlated
      * 【        属性值none表示禁用确认发布模式，这也是默认设置；
      *          correlated表示发布消息成功到交换器后会触发回调方法；
      *          simple有两个效果，
      *              其一效果和 CORRELATED 值一样会触发回调方法，
      *              其二在发布消息成功后使用 rabbitTemplate 调用 waitForConfirms 或 waitForConfirmsOrDie 方法【特制同步确认消息中的单个确认】，等待 broker 节点返回发送结果，
      *                  根据返回结果来判定下一步的逻辑，要注意的点是waitForConfirmsOrDie方法如果返回false则会关闭channel，则接下来无法发送消息到 broker】
      * @创建日期 2023/11/10
      * @since 1.0.0
      */
     @Component
     @Slf4j
     public class ProductConfirmCallBack implements RabbitTemplate.ConfirmCallback {
         //由于ProductConfirmCallBack实现的是RabbitTemplate的内部接口，必须将该实现类注入RabbitTemplate，否则即使交给spring容器管理也找不到,
         // 粗略的理解成rabbitTemplate的一个属性，实际是RabbitTemplate在类中设置了一个confirmCallback属性【源码看到的】，通过该属性设置的回调，
         // 不注入就不能通过rabbitTemplate实例找不到这个回调实现类
         @Autowired
         private RabbitTemplate rabbitTemplate;
     
         /**
          * @描述 这个方法没有人执行，需要设置@PostConstruct注解让其执行
          * @postConstruct注解是Spring的一个注解，作用是让该注解修饰的init方法在启动的时候就加载某些数据，
          * 这个注解注释的方法在@Component注解【首先执行】和@Autowired注解【在@Component后执行】后面执行，能够避免rabbitTemplate还没注入就向其注入该接口实现类
          * ？关注一下前置处理器和后置处理器
          * 我擦这个注解是java自带的javax.annotation.PostConstruct
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/10
          * @since 1.0.0
          */
         @PostConstruct
         public void init(){
             rabbitTemplate.setConfirmCallback(this);
         }
         /**
         * 交换机不管是否收到消息的一个回调方法，实现RabbitTemplate的ConfirmCallback接口，该接口是一个内部接口，且是函数式接口，正常情况下这个类是没有被实现的
         *          CorrelationData   是 消息相关数据ID和相关信息
         *          ack               是 交换机是否收到消息
         *          cause             是 失败的原因
         * 交换机收到或者没有收到消息都会回调这个接口，
         *      correlationData都是消息相关数据
         *      收到boolean ack为true，没有收到boolean ack为false
         *      确认回调cause是null，错误回调cause是失败的原因
         *
          * 生产者要感知到交换机或者队列没有接收到消息，感知就通过这个回调接口完成，交换机没有确认收到消息或者交换机确认收到但失败了【队列接收不到消息】，
          * 认为是信道就会触发这个回调接口，在这个回调接口中尝试把消息返回回来进行保存，收到消息也会回调，用ack标记消息状态
         *
          * 发送消息的是RabbitTemplate,该回调接口也由RabbitTemplate调用
         */
         @Override
         public void confirm(CorrelationData correlationData, boolean ack, String cause) {
             //尝试取id，id取不到设置id为空串，取得到正常id
             String id=correlationData!=null?correlationData.getId():"";
             //消息成功确认打印信息
             if(ack){
                 log.info("交换机已经收到 id 为:{}的消息",id);
             }else{
                 //消息失败确认打印信息
                 log.info("交换机还未收到 id 为:{}消息,由于原因:{}",id,cause);
             }
         }
     }
     ```

   + 消费者

     > 正常写法消费消息

     ```java
     /**
      * @param message
      * @描述 发布确认高级接收消息
      * @author Earl
      * @version 1.0.0
      * @创建日期 2023/11/10
      * @since 1.0.0
      */
     @RabbitListener(queues =CONFIRM_QUEUE_NAME)
     public void receiveConfirmMsg(Message message){
         String msg=new String(message.getBody());
         log.info("接收到队列confirm.queue 消息:{}",msg);
     }
     ```

   + Spring配置类开启发布确认功能

     ```properties
     spring.rabbitmq.publisher-confirm-type=correlated
     ```

     > 回调接口的使用还必须在配置文件配置spring.rabbitmq.publisher-confirm-type=correlated
     >
     > + 属性值none表示禁用确认发布模式，这也是默认设置；
     > + correlated表示发布消息成功到交换器后会触发回调方法；
     > + simple有两个效果
     >   + 其一效果和 CORRELATED 值一样会触发回调方法，
     >   + 其二在发布消息成功后使用 rabbitTemplate 调用 waitForConfirms 或 waitForConfirmsOrDie 方法【特制同步确认消息中的单个确认，效率很低，不咋用】，等待 broker 节点返回发送结果，根据返回结果来判定下一步的逻辑，要注意的点是waitForConfirmsOrDie方法如果返回false则会关闭channel，则接下来无法发送消息到 broker

   + 测试效果

     > 核心：
     >
     > + 情况1：交换机接收消息成功回调进行通知，消息成功被消费
     > + 情况2：交换机找不到成功回调对未接收消息进行后续处理【可能缓存起来用定时任务处理】，消息未被消费
     > + 情况3：交换机接收消息但是找不到队列，发现回调只是调用交换机成功接收到消息的回调，队列仍然没有接收到消息，消息没有被消费，消息丢失，至此，方案还不完善

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/1e3c96b094314178ba9a212986725ac9测试效果.png)





## 队列不可用

> 仅开启了生产者确认机制的情况下，交换机接收到消息后，会直接给消息生产者发送确认消息， 如果发现该消息不可路由，那么消息会被直接丢弃，此时生产者是不知道消息被丢弃这个事件的。 通过消息回退可以在当消息传递过程中不可达目的地时将消息返回给生产者。  

1. 代码实现

   + 消费者

     > 要点
     >
     > + 配置类实现RabbitTemplate.ReturnCallback接口，在returnedMessage中对回退消息进行处理
     > + `rabbitTemplate.setMandatory(true);`配置mandatory属性为true开启回退消息功能，也可以在Spring配置文件配置spring.rabbitmq.publisher.returns=true开启

     ```java
     @Component
     @Slf4j
     public class ProductConfirmCallBack implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {
         @Autowired
         private RabbitTemplate rabbitTemplate;
     
         @PostConstruct
         public void init(){
             //设置配置mandatory属性为true开启回退消息功能，不在这儿设置可以在Spring配置文件设置spring.rabbitmq.publisher.returns=true
             rabbitTemplate.setMandatory(true);
             //设置回退消息交给谁处理
             rabbitTemplate.setReturnCallback(this);
         }
     
         /**
          * @param message the returned message.退回的消息
          * @param replyCode the reply code.
          * @param replyText the reply text.
          * @param exchange the exchange.退回消息的交换机
          * @param routingKey the routing key.路由key
          * @描述
          * @author Earl
          * @version 1.0.0
          * @创建日期 2023/11/10
          * @since 1.0.0
          */
         @Override
         public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
             log.info("消息:{}被服务器退回，退回原因:{}, 交换机是:{}, 路由 key:{}", new String(message.getBody()),replyText, exchange, routingKey);
         }
     }
     ```

   + 测试效果

     > 消息还是原来的消息【生产者、消费者、队列，绑定关系】，注意：
     >
     > 情况2：交换机找不到没有走该接口，仍然走的RabbitTemplate.ConfirmCallback接口的实现类
     >
     > 情况3：只有队列没接收到才走的RabbitTemplate.ReturnCallback接口的实现类

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/a179205ad1b1491093196460328b3248消息回退测试效果.png)



## 交换机备份

1. 项目架构图

   > 核心思想：消息无法被确认交换机接收自动转发给备份交换机【扇出类型】，备份交换机将消息一方面转发给备份队列进行消息备份，另一方面将消息转发给警告队列进行预警
   >
   > 当mandatory 参数【消息回退】与备份交换机一起使用的时候，如果两者同时开启，消息究竟何去何从？谁优先级高，经过测试显示答案是备份交换机优先级高【即优先备份交换机，不走回退】  
   >
   > 需要声明：
   >
   > + 确认交换机和确认队列【这俩已实现】，备份交换机，确认队列、备份队列、警告队列
   > + 正常声明交换机、队列和组件；在确认交换机中使用代码绑定备份交换机为确认交换机的消息转发交换机，用withArgument传递参数

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/efd790122b7349818778ffc2464d2348交换机备份.png)

2. 代码实现

   + 配置类

     > 注意更改了确认交换机的绑定关系，让其接收不到消息转发到备份交换机

     ```java
     public static final String BACKUP_EXCHANGE_NAME = "backup.exchange";
     public static final String BACKUP_QUEUE_NAME = "backup.queue";
     public static final String WARNING_QUEUE_NAME = "warning.queue";
     
     //声明业务Exchange，直接交换机，在直接交换机中声明其备份交换机
     @Bean("confirmExchange")
     public DirectExchange confirmExchange(){
         //return new DirectExchange(CONFIRM_EXCHANGE_NAME);
         //设置确认交换机的备份交换机
         return ExchangeBuilder.directExchange(CONFIRM_EXCHANGE_NAME).withArgument("alternate-exchange",BACKUP_EXCHANGE_NAME).build();
     }
     
     //声明备份 Exchange，此处就是测试 交换机无法路由到队列 而转发给备份交换机的过程，正常情况用于备份数据的交换机肯定在备份服务器，
      * 这样测试不满意的话可以自己搭建集群测试。
     @Bean("backupExchange")
     public FanoutExchange backupExchange(){
         return new FanoutExchange(BACKUP_EXCHANGE_NAME);
     }
     
     // 声明警告队列
     @Bean("warningQueue")
     public Queue warningQueue(){
         return QueueBuilder.durable(WARNING_QUEUE_NAME).build();
     }
     // 声明报警队列绑定关系
     @Bean
     public Binding warningBinding(@Qualifier("warningQueue") Queue queue,
                                   @Qualifier("backupExchange") FanoutExchange backupExchange){
         return BindingBuilder.bind(queue).to(backupExchange);
     }
     // 声明备份队列
     @Bean("backQueue")
     public Queue backQueue(){
         return QueueBuilder.durable(BACKUP_QUEUE_NAME).build();
     }
     // 声明备份队列绑定关系
     @Bean
     public Binding backupBinding(@Qualifier("backQueue") Queue queue,
                                  @Qualifier("backupExchange") FanoutExchange backupExchange){
         return BindingBuilder.bind(queue).to(backupExchange);
     }
     ```

   + 警告消费者

     ```java
     /**
      * @param message
      * @描述 消费警告队列的消息发出警告
      * @author Earl
      * @version 1.0.0
      * @创建日期 2023/11/10
      * @since 1.0.0
      */
     @RabbitListener(queues = WARNING_QUEUE_NAME)
     public void receiveWarningMsg(Message message) {
         String msg = new String(message.getBody());
         log.error("报警发现不可路由消息: {}", msg);
     }
     ```

     

   + 测试效果

     > 仍然是使用消息回退的生产者发送消息，注意删除原来的确认交换机，改了其消息流向
     >
     > 要点：
     >
     > + 情况1：走确认发布机--确认队列--正常消费者
     > + 情况2：找不到确认交换机，调用交换机不可用的回调接口实现类让生产者对数据进行处理
     > + 情况3：在消息回退和交换机备份同时开启的情况下，不再走消息回退，转而走备份交换机，备份交换机的优先级更高

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/f3baa94d319842b888963690bfd5a1c8备份交换机测试效果.png)

     【数据保存在备份队列中】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/cc017357748c425e9cd0071df7d969f2数据保存在备份队列中.png)



# 其他知识

## 幂等性问题

> 重复提交，比如用户购买商品后点击支付，支付扣款成功，但返回结果时网络异常，此时用户多次点击，发生多次扣款并生成多条扣款记录，以往的单系统应用，将数据操作放入事务中，发生错误立刻回滚，但再响应客户端的时候也可能发生网络中断或者异常
>
> 幂等性就是为了让用户的同一个操作发起一次或者多次请求的结果是一致的，不会因为多次点击产生副作用
>
> 幂等性问题就是消息队列应答ack网络中断导致的消息重复消费的问题

1. 消息重复消费的可能性

   + 消费者在消费消息返回ack【应答】时网络中断，MQ无法收到应答消息，会把已经消费的消息发给其他消费者重复消费，造成重复消费

2. 幂等性问题的解决思路

   + 加一个验证消息是否消费过的流程，在消息生成时一同生成一个全局唯一的id，每次消费消息前先对消息进行判断是否消费过

3. 消费端幂等性问题的保障

   > 海量订单生成的业务高峰期，生产端可能重复产生消息，通过消费端实现幂等性，让即使收到一样的消息也永远不会被消费多次，

   + 业界主流的幂等性有两种操作

     + 唯一ID+指纹码机制，利用数据库主键去重

       > 指纹码：按一些规则或时间戳加别的服务拼接出的唯一信息码，利用id查询是否已经处理过，优势是信息拼接简单，信息基本由业务规则拼接而来；劣势是高并发场景下，单个数据库有写入性能瓶颈，可以采用分库分表提升性能，但是不建议
       >
       > 这个方式不是最佳的，最佳的方式是下一个利用redis的原子性解决

     + 利用redis原子性实现

       > 用redis的setnx命令，天然就具有幂等性，实现不重复消费
       >
       > 这个很常用，但是怎么用没说



## 优先级队列

> 场景：淘宝订单催付功能，客户在天猫下单，淘宝会将订单推送给客户，但当客户没有即时付款，淘宝会给用户发一条短信提示，但是一般能创造很大利润的大商家的订单会先处理，这种大商家会先发短信；后端用redis做消息队列不能实现有优先级的场景，订单量大了以后用RabbitMQ进行改造和优化，发现是大客户就给一个相对较高的优先级，否则就用默认的优先级
>
> RabbitMQ中就有对优先级队列的实现，为每个消息分配一个优先级，每次发送消息前对消息进行优先级排序，优先级大的即便在队列的尾部也是排到队列的前面

1. 优先级队列原理说明

   + 队列消息正常情况

     > `/`前面是消息，后面是消息的优先级，出队列到消费者对消息根据优先级排序，优先级大的先出队列

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/e2f09873ca4a42429d66674e83191832优先级队列.png)

   + 排序后的优先级队列

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/ba94f701c48c4671827d14a065faa5f1按优先级顺序消息排序.png)

2. 优先级队列的控制台操作

   > 企业一般都用代码操作

   + 点击队列--添加队列--Maximum priority--在arguments一栏设置最大优先级【表示只能设置0-设置值之间的优先级，用太大对cpu和内存浪费性能，因为有对优先级的排序】

3. 优先级队列的代码实现

   > 注意这种排序是基于队列中有一定数据量情况下的排序，否则发一个就被马上消费，可能观察不到排序的现象，演示为了简单，直接在一堆消息发送完毕的情况下再启动消费者进行消费，实际情况很复杂，因为动态添加数据，出数据的时候又在进数据，很好奇实际是怎么实现的
   >
   > 实现在first包下

   + 代码中设置队列为优先级队列、设置优先级范围并设置被发送消息的优先级

     > 要点：
     >
     > + 使用`params.put("x-max-priority", 10);channel.queueDeclare(QUEUE_NAME, true, false, false, params);`设置声明优先级队列
     > + 使用`AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();`把properties设置为消息发送的其他参数设置消息的优先级

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 优先级队列,向优先级队列添加10条有优先级区别的数据
      * 使用`params.put("x-max-priority", 10);channel.queueDeclare(QUEUE_NAME, true, false, false, params);`设置声明优先级队列,
      * 使用`AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();`把properties设置为消息发送的其他参数设置消息的优先级
      * @创建日期 2023/11/10
      * @since 1.0.0
      */
     public class Producer {
     private static final String QUEUE_NAME="priority.queue";
     public static void main(String[] args) throws Exception {
         try (Channel channel = RabbitMQUtil.getChannel()) {
             //设置队列的最大优先级 最大可以设置到 255 官网推荐 1-10 如果设置太高比较吃内存和 CPU
             Map<String, Object> params = new HashMap();
             params.put("x-max-priority", 10);
             channel.queueDeclare(QUEUE_NAME, true, false, false, params);
     
             //给消息赋予一个 priority 属性
             AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();
     
             for (int i = 1; i <11; i++) {
                 String message = "info"+i;
                 //把五的倍数发送的消息设置成优先级更高的
                 if(i%5==0){
                     //properties是AMQP.BasicProperties类型的
                     channel.basicPublish("", QUEUE_NAME, properties, message.getBytes());
                 }else{
                     channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                 }
                 System.out.println("发送消息完成:" + message);
             }
         }
     }
     ```

   + 消费者

     ```java
     /**
      * @author Earl
      * @version 1.0.0
      * @描述 优先级队列消费者，正常消费
      * @创建日期 2023/11/10
      * @since 1.0.0
      */
     public class Consumer {
         private static final String QUEUE_NAME="priority.queue";
         public static void main(String[] args) throws Exception {
             Channel channel = RabbitMQUtil.getChannel();
     
             System.out.println("消费者启动等待消费......");
             DeliverCallback deliverCallback=(consumerTag, delivery)->{
                 String receivedMessage = new String(delivery.getBody());
                 System.out.println("接收到消息:"+receivedMessage);
             };
             channel.basicConsume(QUEUE_NAME,true,deliverCallback,(consumerTag)->{
                 System.out.println("消费者无法消费消息时调用，如队列被删除");
             });
         }
     }
     ```

   + 测试效果

     【优先级队列属性标记】

     > 用Pri标记优先级队列

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/70306762f1a1434c82dc0de44251b978优先级队列的状态栏.png)

     【消息发送】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/cfe81c3202d848d49bf42d38dcbf7799带有优先级的消息发送.png)

     【消息接收】

     > 优先级高先接收，默认优先级是0吗？

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/10/b8652082aef349cc8aa8ae05caa53144带有优先级的消息接收.png)



## 惰性队列

> 判断惰性队列的标准是消息是保存在内存还是磁盘上，正常情况下消息保存在内存中，惰性队列消息保存在磁盘中，每次接收消息都会把消息写入磁盘，速度很慢，一般不采用惰性队列，只有在大量的消息堆积但是暂时没有消费者，防止大量消息占用内存需要使用队形队列
>
> RabbitMQ 从 3.6.0 版本开始引入了惰性队列的概念。惰性队列将消息存入磁盘，在消费者消费到相应的消息时才会被加载到内存中，
>
> 它的一个重要的设计目标是能够支持更长的队列，即支持更多的消息存储。当消费者由于各种各样的原因(比如消费者下线、宕机亦或者是由于维护而关闭等)而致使长时间内不能消费消息造成堆积时，惰性队列就很有必要了。  
>
> 即使是持久化的消息，在被写入磁盘的同时也会在内存中驻留一份备份【下一页待处理的消息】。当 RabbitMQ 需要释放内存的时候，会将内存中的消息换页至磁盘中【类比于分页查询】，这个操作会耗费较长的时间，也会阻塞队列的操作，进而无法接收新的消息。虽然 RabbitMQ 的开发者们一直在升级相关的算法，但是效果始终不太理想，尤其是在消息量特别大的时候。  

1. 队列的两种模式

   + defalut模式

     + 默认是default模式，

   + lazy模式

     + lazy模式是惰性队列的模式，通过方法channel.queueDeclare方法进行设置，也可以通过Policy策略方式设置【通过控制台设置Queue--add a queue--lazy mode】，一个队列同时使用这两种方式设置，Policy的方式具有更高的优先级

     + 声明惰性队列的代码

       ```
       Map<String,Object> args=new HashMap<String,Object>();
       args.put("x-queue-mode","lazy");
       channel.queueDeclare("myqueue",false,false,false,args);
       ```

2. 惰性队列的性能

   + 内存开销

     > 在发送 1 百万条消息并积压的情况下，每条消息大概占 1KB 的情况下，普通队列占用内存是 1.2GB，而惰性队列仅仅占用 1.5MB  

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/a4d980d6b5c64617b35595b06470973f性能开销.png)

     

# RabbitMQ集群

> 添加其他RabbitMQ服务器，将其加入1号节点服务器就可以形成集群，比如2加入1号，4加入2号和4加入1号效果是一样的，类似于redis集群

1. 集群架构

   > 添加两台新机器，都加入RabbitMQ节点1号

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/a426be98a29949828057b4971e9c031cRabbitMQ集群搭建的架构.png)

2. 集群搭建实操

   + 将当前机器克隆三份并修改三台机器的ip地址，不要使其冲突【电脑好，扛得住】，使用xshell对三台机器进行远程连接

   + 使用命令`vim /etc/hostname`修改3台机器的主机名称为目标名称`node1、node2、node3`并使用命令`shutdown -r now`重启机器，使用命令`hostname`查看当前机器的主机名

   + 使用命令`vim /etc/hosts`添加各机器节点的ip和hostname配置各个虚拟机节点并重启机器，让各个节点能识别对方

     ```shell
     192.168.200.132 node1
     192.168.200.133 node2
     192.168.200.134 node3
     ```

   + 要确保各个节点的cookie文件使用的是同一个值，在node1节点上执行远程操作命令`scp /var/lib/rabbitmq/.erlang.cookie root@node2:/var/lib/rabbitmq/.erlang.cookie`和`scp /var/lib/rabbitmq/.erlang.cookie root@node3:/var/lib/rabbitmq/.erlang.cookie`将第一台机器的cookie复制给第二台和第三台机器

   + 三台机器使用命令`rabbitmq-server -detached`重启RabbitMQ服务、顺带重启Erlang虚拟机和RabbitMQ的应用服务

   + 以node1为集群将node2和node3加入进去，分别在node2和node3节点执行以下命令

     > 关闭RabbitMQ服务，将rabbitmq重置，将node2和node3节点分别加入node1节点【这里将node2节点加入node3节点观察后续移除node2节点后node3的效果，凉了手速过快，一起连上了】

     ```shell
     rabbitmqctl stop_app
     #(rabbitmqctl stop 会将 Erlang 虚拟机关闭， rabbitmqctl stop_app 只关闭 RabbitMQ 服务，就是rabbitmq本身)
     rabbitmqctl reset
     rabbitmqctl join_cluster rabbit@node1
     rabbitmqctl start_app
     #(只启动应用服务)
     ```

     > 执行命令`rabbitmqctl join_cluster rabbit@node1`必须开放node1的4369和25672端口，否则会报错；网上一堆操作猛如虎，没一个讲到点上的；克隆的系统相关端口也是开放的
     >
     > 我靠，血泪教训，最多只能有一个机器不开放4369和25672端口，其他所有机器都必须开放这俩接口，否则严重点会直接导致所有的RabbitMQ没有一台机器能启动，一直显示正在启动中，启动命令一直卡在运行中，其他的rabbitmq命令报错消息还很傻逼，只会提醒应用没启动，网上还没啥解决方案【fuck】，最后只启动node1发现突然能启动，且能进后台，然后启动node2突然能启动了，node3死活启动不了，终于开放node2的两个端口后node3就能自动启动了，为了方便以后不出问题，建议所有机器节点都开放这俩端口，连带5672端口和15672端口

     【没开放端口的情况】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/a4230a6432f74167bbaf1cde0bf4838c报错信息1.png)

     【开放4369端口的情况和开放了25672端口的情况】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/e4b2b620346441e7975d42b15fcd6b7a报错信息2.png)

   + 使用命令`rabbitmqctl cluster_status`查看集群状态

     > 2号节点一直在启动，不知道为啥

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/6562fdaa1d3e4d92a0061a17ba19e130集群状态.png)

   + 只需要在一台机器上使用以下命令重新设置用户

     ```shell
     rabbitmqctl add_user earl 123456
     #创建账户，账户名earl，密码123456
     rabbitmqctl set_user_tags earl administrator  
     #设置用户earl的角色为超级管理员
     rabbitmqctl set_permissions -p "/" earl ".*" ".*" ".*"  
     #设置用户权限
     ```

3. 搭建成功标志

   + 进入网页服务界面能看到3个RabbitMQ节点【状态都是绿色就表示非常健康】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/00e0e14fbaf048c1961265c67822d8c6启动成功的效果.png)

4. 解除集群节点的命令【node2和node3分别执行以脱离，最后测试一下2号机脱离通过2号机联机集群的3号机的状态，手快了全绑在node1下了】

   【脱离机器node2或node3分别执行】

   ```java
   rabbitmqctl stop_app
   rabbitmqctl reset
   rabbitmqctl start_app
   rabbitmqctl cluster_status
   ```

   【node1执行命令忘记脱离的节点】

   ```shell
   rabbitmqctl forget_cluster_node rabbit@node2
   ```

   

## 镜像队列

> 目前每个节点上的队列不可复用，某个节点突然宕机，队列会直接不可用，队列中的消息会丢失，即使是持久化的消息也会存在在持久化的过程中时间不够消息丢失

1. 节点队列不可复用演示

   + 在node1上创建hello队列，使用命令`rabbitmqctl stop_app`关闭node1服务，观察控制台node1队列的状态

     【node1关闭】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/e582b560dfe6473b8899617baaac00e5关闭node1.png)

     【关闭后队列情况】

     > 和课堂演示不同，压根连队列都直接不显示了，那是因为队列没有持久化

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/e9cbc652d32c48a48253f2444566c444队列显示情况.png)

     【持久化以后】

     > NaN表示不是一个数字，非法值

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/1fe0f07de5794c658d920c4133a07e3f节点挂掉.png)

     【使用其他节点访问该队列会报错并提示队列down了】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/c530cb85ed7949f789676400936bf1df队列down了.png)

   + 重启以后队列以后发现队列中的消息没了

     > 也没有被消费

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/ef84616ad1c0438ca4b76ba6d3486fd5消息丢失.png)

     

2. 镜像队列

   > 镜像队列就是对其他节点队列的备份，引入镜像队列(Mirror Queue)的机制，可以将队列镜像到集群中的其他节点之上，如果集群中的一个节点失效了，队列能自动地切换到镜像中的另一个节点上以保证服务的可用性。  
   >
   > 可以将节点队列在其他节点上备份一份，也可以每个节点上都备份一份，但是要根据情况，如果全都备份会很浪费资源，这样不好

   + 镜像队列备份策略搭建【通过控制台】

     > 在admin菜单下users下点击Policies--添加新的策略，表示给`/`添加策略
     >
     > name是随便起的，只是表示这个镜像对列的名字
     >
     > pattern是镜像匹配的队列，是一个正则表达式，`^mirrior`表示给以mirrior为前缀的队列或交换机整个镜像【名为hello的队列就不能被镜像，mirrior_hello这个队列就可以】
     >
     > Apply to表示应用于交换机和队列
     >
     > Definition表示设置一些参数
     >
     > + ha-mode：HA 全称high available 高可用，mode表示高可用的备份模式，exactly表示指定具体备份几份
     > + ha-params：表示指定备份的具体份数，这里表示备份两份【这个两份包括主机在内一共两份】
     > + ha-sync-mode：表示自动同步数据，自动设置为automatic，表示自动进行同步，也可以设置成手动，但是手动同步比较麻烦

     <img src="https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/3fce865d78d14643b39dc3077663306d策略设置.png" style="zoom:150%;" />

   + 备份效果

     + admin中显示当前的备份策略

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/d1c384342102493ab62f7295713d2635显示备份策略.png)

     + 此时在node1创建匹配策略名字的队列，会再多备份1份，备份的一份队列可能在node2节点，也可能在node3节点上，具体由服务器决定【有备份的队列会在Node上显示+1，即额外备份的数量】

       > 点进具体的队列会在mirrors显示具体备份的节点

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/de6c36251ba04b23aa41dcc2e862baad备份队列.png)

       【队列详情】

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/8bc19617c5954653a17ac245ac6c9361队列详情.png)

     + 关闭节点1，备份的镜像队列会自动在Node属性栏显示正在node3节点运行，同时还会再备份一份在其他节点node2上【牛皮】

       > 没有备份的队列都噶了【down】
       >
       > 能够达到就算整个集群只剩一台机器也能处理之前宕机的节点中的队列和数据，宕机会自动再备份到其他服务器上

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/b11a1baafc8644359d4282ac8a66df32宕机后备份队列反应.png)

       【再次备份】

       > 一台宕机以后，在其他节点上再次备份一份维持备份策略要求的2份策略

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/b11a1baafc8644359d4282ac8a66df32宕机后备份队列反应.png)

     + 启动消费者

       > 发现消息仍然被消费了
       >
       > 注意，这时候消费者对应的节点地址也必须跟着变才能接收到消息，使用宕机节点的地址仍然会报错消息队列不可用【经过测试确实如此】
       >
       > 不足：没有介绍消费者针对集群的连接设置，因为消费者要自己判断机器是否宕机和切换节点地址，生产者此时也有相同的问题【发送消息】，写死了ip；这已经不能由RabbitMQ自己解决，需要借助软件Haproxy实现负载均衡，Twitter、Reddit、StackOverflow、GitHub等都在用，类似与这种负载均衡软件还有nginx、lvs，软件区别：http://www.ha97.com/5646.html  

       ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/f4e6a4225cdf42b9806e485c0f9144a1接收消息.png)

       

       

## Haproxy实现负载均衡

> HAProxy 提供高可用性、负载均衡及基于 TCPHTTP 应用的代理，支持虚拟主机，它是免费、快速并且可靠的一种解决方案，包括 Twitter,Reddit,StackOverflow,GitHub 在内的多家知名互联网公司在使用。HAProxy 实现了一种事件驱动、单一进程模型，此模型支持非常大的井发连接数。  
>
> 高可用：某个机器宕机了，有备机接替他的工作，系统能正常运行

1. 整体架构

   + 生产者发送的消息通过ip找到haproxy主机【有haproxy备机】，由主机负责转发消息到不同的消息队列节点，只需要将消息发送到http://10.211.55.71:8888/stats【具体看文件或者博客】
   + 主机宕机会被keepalive软件发现将ip漂移到备机上，备机再负责消息向消息队列转发【备机也会定期监测主机是否还还活着，收不到主机的消息就会自动启动】
   + keepalive主要是为了高可用，能够通过自身健康检查、资源接管功能做高可用(双机热备)，实现故障转移.
   + haproxy+keepalive方案就能解决rabbitmq集群的转发和负载均衡问题  【具体操作看文档或者博客，没讲】

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/277c97d8e0e04d5db3c29ebf4deb17c7负载均衡.png)

   

## Federation Exchange  

> 联合交换机，相距很远的机房之间存在网络延迟，消息队列可能被设置在相隔很远的机房，机房周边区域使用特定的机房来访问降低延迟，但是存在数据不一致的问题，使用联合交换机解决数据不一致的问题
>
> 这部分讲的太水了，有相同的应用场景再回来看

1. 搭建步骤

   + 在每台机器上开启federation相关插件【自带的】
     + 使用命令`rabbitmq-plugins enable rabbitmq_federation`开启对应插件
     + 使用命令`rabbitmq-plugins enable rabbitmq_federation_management`
   + 安装好以后控制台admin菜单能看见多出来`Federation Status` 和`Federation Upstreams`菜单【一般是一个机器固定同步数据给另一台机器】

2. 联合交换机原理

   + node1理解为北京，node2理解为深圳；1号节点算上游【数据由上游同步到下游，水流类比】，2号节点算下游；

     > 1号节点的交换机【在上游配置2号节点地址】要配置2号节点的地址，1号节点的交换机在同步数据之前2号节点交换机必须有和对应1号节点交换机同名的交换机，没有会报错
     >
     > 文档又说下游配置上游节点地址，服了

   <img src="https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/3439fccd134a43eab1370bdef22e5fcf联合交换机原理.png"  />

3. 步骤演示

   + 在node2上创建node1上需要同步数据的交换机fed_exchange，绑定node2上的队列

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/0d4df02ed0094bf8b7cd7065177e8468联合交换机node2声明交换机和队列.png)

   + 在客户端配置上游地址

     > 注意是在下游节点配置上游节点的地址，在node2配置node1的地址，把node1的数据同步到下游node2

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/0cfceac521de4d5786cd77f6506d61d8配置上游地址.png)

     【设置效果】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/189c82f4b8df43918ab7a31d40aa7b64设置效果.png)

   + 设置策略

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/ccad7fcbc390465da11bf16f2b3a8227设置交换策略.png)

     【设置效果】

     > 这个只是表示node2能不能连接上node1

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/4135074a301949bda44525a63234f8a5联合状态.png)



## Federation Queue  

> 联邦队列，联邦交换机和联邦队列都可以实现两地间数据的交换
>
> 一个联邦队列可以连接一个或者多个上游队列(upstream queue)，并从这些上游队列中获取消息以满足本地消费者消费消息
> 的需求。
>
> 联邦队列和联邦交换机都没有演示效果  

1. 原理

   > node1的fed.queue想将数据同步到node2进行消费【不影响node1对同步数据的消费，应用场景就是深圳对北京的数据同步】，需要先将node2的fed.queue队列联合到node1的fed.queue队列
   >
   > node2配置node1的地址已经在联和交换机中配置好了，这里只需要配置策略

   ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/8c38e647b83241d5b4d0615d78063239联和队列.png)

   

2. 步骤演示

   + 在node2创建对应node1的fed.queue

   + 在node2配置node1的地址【同上面的联合交换机，upstream表示上游的意思】

   + 添加联合策略

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/d03197c67ea5447583898289f436a5f2联合队列策略配置.png)

     【联合策略】

     > 一个是联合交换机策略，一个是联合队列策略，上游都是node1

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/4db95a4c003c4008aaefb55c85b4e90e联合策略.png)

     

     



## Shovel

> 还是做数据备份或者转发的，和联合交换机、队列的作用差不多，将一个节点的数据【作为源端】拉取转发到另一个节点【目的端】
>
> shovel可以翻译为铲子

1. 架构图

   + Q1是源端、Q2是目的端；

     > 发送数据给Q1，Q1会将数据同步到Q2中

![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/aac5e615429f4bb9950eec6b8dce9fefshovel.png)

2. 搭建

   + 像federation一样shovel安装插件并在控制台可以看见shovel status和shovel upstream

     ```shell
     rabbitmq-plugins enable rabbitmq_shovel
     rabbitmq-plugins enable rabbitmq_shovel_management
     ```

   + 配置shovel策略

     > 配置以后，node1节点的Q1中的消息都会同步到node2节点的Q2，解决跨地区数据同步的问题
     >
     > name是自定义的，和联合队列是一样的，最好见名知意

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/da8333b3366b4ce3b6ef833fa74a273cshovel策略.png)

     【配置状态】

     ![](https://vpc-ol-edu.oss-cn-chengdu.aliyuncs.com/2023/11/11/eb4ef30dc2224c489dc33147d778f5beshovel配置.png)





# 附录

1. QPS

   > `Queries Per Second` 是每秒查询率 ，是一台服务器每秒能够相应的查询次数，是一台特定的查询服务器每秒能够相应的查询次数，即每秒的响应请求数，也即是最大吞吐能力。

2. TPS

   > `Transactions Per Second` 也就是事务数/秒。一个事务是指一个客户机向服务器发送请求然后服务器做出反应的过程。客户机在发送请求时开始计时，收到服务器响应后结束计时，以此来计算使用的时间和完成的事务个数；这不就是每秒响应的请求数吗【一个页面可能有多个请求，以响应为主，10个请求，一个响应；收到一个响应算一个TPS【理解成一个客户机同一时间发出请求并接受到响应的过程算一个事务，期间可能涉及多个请求】，发送诗词请求是10RPS，如果请求都是查询请求，就是10QPS】

3. 并发数【并发度】

   > 指系统同时能处理的请求数量，同样反应了系统的负载能力。这个数值可以分析机器1s内的访问日志数量来得到
   >
   > QPS(TPS)=并发数/平均响应时间【QPS(TPS)=并发数/平均响应时间；并发数：系统同时处理的request/事务数；响应时间：一般取平均响应时间】

4. 吞吐量

   > 指系统在单位时间内处理请求的数量，一个系统的吞吐量（承压能力）与request（请求）对cpu的消耗，外部接口，IO等等紧密关联。
   >
   > 一个系统吞吐量通常有QPS(TPS)，并发数两个因素决定，每套系统这个两个值都有一个相对极限值，在应用场景访问压力下，只要某一项达到系统最高值，系统吞吐量就上不去了，如果压力继续增大，系统的吞吐量反而会下降，原因是系统超负荷工作，上下文切换，内存等等其他消耗导致系统性能下降。

5. PV【页面访问量】

   > 【Page View】，即页面浏览量或点击量，用户每次刷新即被计算一次。可以统计服务一天的访问日志得到

6. UV【独立访客】

   > 【Unique Visitor】统计1天内访问某站点的用户数。可以统计服务一天的访问日志并根据用户的唯一标识去重得到。

7. RT【响应时间】

   > 响应时间是指系统对请求作出响应的时间，一般取平均响应时间。可以通过Nginx、Apache之类的Web Server得到。

8. DAU【日活跃用户数量】

   > 【Daily Active User】常用于反映网站、互联网应用或网络游戏的运营情况。DAU通常统计一日（统计日）之内，登录或使用了某个产品的用户数（去除重复登录的用户），与UV概念相似

9. MAU【月活跃用户数量】

   > 【Month Active User】指网站、app等去重后的月活跃用户数量

10. typora快捷键

   + [ ] 任务列表：`SHIFT+CTRL+X`

+ F11：全屏和退出全屏

11. 查询一下消息大小限制和队列长度限制方面相关的博文
11. linux命令：`ip addr`效果和`ipconfig`类似，都显示ip地址



























