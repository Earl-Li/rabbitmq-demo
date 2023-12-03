package com.atlisheng.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 接收的是延迟队列的消息
 * @创建日期 2023/11/09
 * @since 1.0.0
 */
@Slf4j
@Component
public class OrderConsumer {
    public static final String DELAYED_QUEUE_NAME = "delayed.queue";

    //使用RabbitListener注解指定监听的队列实现对消息的处理，实际肯定是用了反射，接收到队列QD的消息，获取到消息，调用该方法进行对消息的处理
    @RabbitListener(queues = "QD")
    public void confirmOrderMessage(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody(),"UTF-8");
        log.info("当前时间: {} ,收到死信队列信息 {} ", new Date().toString(), msg);
    }

    /**
     * @param message
     * @描述 接收基于延迟插件的延迟交换机的延时消息的接收
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @RabbitListener(queues = DELAYED_QUEUE_NAME)
    public void receiveDelayedQueue(Message message) throws UnsupportedEncodingException {
        //这里不用转成专门转成UTF-8，不知道为啥会抛异常，以前使用不会抛异常
        String msg = new String(message.getBody(),"UTF-8");
        log.info("当前时间： {},收到延时队列的消息： {}", new Date().toString(), msg);
    }

    public static final String CONFIRM_QUEUE_NAME = "confirm.queue";

    /**
     * @param message
     * @描述 发布确认高级接收消息
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/10
     * @since 1.0.0
     */
    @RabbitListener(queues =CONFIRM_QUEUE_NAME)
    public void receiveConfirmMsg(Message message){
        String msg=new String(message.getBody());
        log.info("接收到队列confirm.queue 消息:{}",msg);
    }

    public static final String WARNING_QUEUE_NAME = "warning.queue";

    /**
     * @param message
     * @描述 此处就是测试 交换机无法路由到队列 而转发给备份交换机的过程，正常情况用于备份数据的交换机肯定在备份服务器，
     * 这样测试不满意的话可以自己搭建集群测试。
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/10
     * @since 1.0.0
     */
    @RabbitListener(queues = WARNING_QUEUE_NAME)
    public void receiveWarningMsg(Message message) {
        String msg = new String(message.getBody());
        log.error("报警发现不可路由消息: {}", msg);
    }
}