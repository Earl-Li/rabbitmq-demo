package com.atlisheng.rabbitmq.second;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;

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
