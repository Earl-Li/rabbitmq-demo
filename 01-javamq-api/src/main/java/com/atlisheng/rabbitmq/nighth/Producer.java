package com.atlisheng.rabbitmq.nighth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 优先级队列,向优先级队列添加10条有优先级区别的数据
 * 使用`params.put("x-max-priority", 10);channel.queueDeclare(QUEUE_NAME, true, false, false, params);`设置声明优先级队列,
 * 使用`AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();`把properties设置为消息发送的其他参数设置消息的优先级
 * @创建日期 2023/11/10
 * @since 1.0.0
 */
public class Producer {
    private static final String QUEUE_NAME="priority.queue";
    public static void main(String[] args) throws Exception {
        try (Channel channel = RabbitMQUtil.getChannel()) {
            //设置队列的最大优先级 最大可以设置到 255 官网推荐 1-10 如果设置太高比较吃内存和 CPU
            Map<String, Object> params = new HashMap();
            params.put("x-max-priority", 10);
            channel.queueDeclare(QUEUE_NAME, true, false, false, params);

            //给消息赋予一个 priority 属性
            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();

            for (int i = 1; i <11; i++) {
                String message = "info"+i;
                //把五的倍数发送的消息设置成优先级更高的
                if(i%5==0){
                    //properties是AMQP.BasicProperties类型的
                    channel.basicPublish("", QUEUE_NAME, properties, message.getBytes());
                }else{
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                }
                System.out.println("发送消息完成:" + message);
            }
        }
    }
}