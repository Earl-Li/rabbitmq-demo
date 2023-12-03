package com.atlisheng.rabbitmq.first;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Producer {
    //设置队列的名称
    private final static String QUEUE_NAME = "mirrior.hello";
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
            channel.queueDeclare(QUEUE_NAME,true,false,false,null);
            //准备消息内容
            String message="hello world";
            /**
             * 信道对象的basicPublish发布发送一个消息
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
