package com.atlisheng.rabbitmq.nighth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

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