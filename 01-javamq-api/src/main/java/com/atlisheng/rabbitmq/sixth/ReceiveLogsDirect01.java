package com.atlisheng.rabbitmq.sixth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.io.FileUtils;

import java.io.File;

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