package com.atlisheng.rabbitmq.fifth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

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