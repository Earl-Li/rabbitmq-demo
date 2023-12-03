package com.atlisheng.rabbitmq.fifth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.io.FileUtils;

import java.io.File;

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
        channel.queueBind(queueName, EXCHANGE_NAME, "123");
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