package com.atlisheng.rabbitmq.third;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.atlisheng.rabbitmq.utils.SleepUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

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
        //设置分发类型
        int prefetchCount=1;
        channel.basicQos(prefetchCount);
        //采用手动应答
        boolean autoAck=false;
        //basicConsume方法可能封装了等待消息的代码，启动main方法会等待消息队列传递消息过来
        channel.basicConsume(ACK_QUEUE_NAME,autoAck,deliverCallback,(consumerTag)->{
            System.out.println(consumerTag+"消费者取消消费接口回调逻辑");
        });
    }
}
