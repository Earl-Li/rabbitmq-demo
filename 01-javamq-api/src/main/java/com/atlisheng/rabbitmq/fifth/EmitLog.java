package com.atlisheng.rabbitmq.fifth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.Channel;

import java.util.Scanner;

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
            * 2.exchange 的类型
            *
            * 多处声明交换机能避免因为启动顺序报错
            */
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
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