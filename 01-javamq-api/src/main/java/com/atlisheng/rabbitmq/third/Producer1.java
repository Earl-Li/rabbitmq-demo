package com.atlisheng.rabbitmq.third;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

public class Producer1 {
    private static final String TASK_QUEUE_NAME = "ack_queue";
    public static void main(String[] argv) throws Exception {
        try (Channel channel = RabbitMQUtil.getChannel()) {
            //设置信道确认发布模式，在消息发送前进行设置
            channel.confirmSelect();
            //设置队列名称、不可持久化、可多个工作线程访问、断开连接不自动删除队列、不设置其他参数
            boolean durable=true;
            channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
            Scanner sc = new Scanner(System.in);
            System.out.println("等待输入信息");
            while (sc.hasNext()) {
                String message = sc.nextLine();
                //使用默认交换机、发送消息到指定队列、不设置其他参数、消息转换成byte数组(如果输入有中文要设置转换byte数组的字符集，否则可能出现字符乱码)
                //channel.basicPublish("", TASK_QUEUE_NAME, null, message.getBytes("UTF-8"));
                //设置消息持久化，即消息存入磁盘，使RabbitMQ重启以后消息不丢失
                channel.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
                System.out.println("生产者发出消息" + message);
            }
        }
    }
}
