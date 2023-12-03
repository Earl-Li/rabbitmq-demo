package com.atlisheng.rabbitmq.eighth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.atlisheng.rabbitmq.utils.SleepUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;


/**
 * @author Earl
 * @version 1.0.0
 * @描述 消息生产者【不需要知道消息可能去到死信队列，正常写即可，但是这里添加了设置消息的存活时间】
 * @创建日期 2023/11/08
 * @since 1.0.0
 */
public class Producer {
    private static final String NORMAL_EXCHANGE = "normal_exchange";
    public static void main(String[] argv) throws Exception {
        try (Channel channel = RabbitMQUtil.getChannel()) {
            channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
            //设置消息的 TTL 时间，单位ms，链式编程，10s内消息没有被正常接收就会被转发到死信交换机
            //AMQP.BasicProperties properties = new AMQP.BasicProperties(). builder().expiration("10000").build();
            //该信息是用作演示队列个数限制
            for (int i = 1; i <11 ; i++) {
                String message="info"+i;
                //发完睡1s，实现Ready字段递减可被观察的效果
                SleepUtil.sleepInSecond(1);
                //发送设置参数包括消息的有效时间
                //channel.basicPublish(NORMAL_EXCHANGE, "zhangsan", properties, message.getBytes());
                channel.basicPublish(NORMAL_EXCHANGE, "zhangsan", null, message.getBytes());
                System.out.println("生产者发送消息:"+message);
            }
        }
    }
}