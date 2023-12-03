package com.atlisheng.rabbitmq.third;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.atlisheng.rabbitmq.utils.SleepUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

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
