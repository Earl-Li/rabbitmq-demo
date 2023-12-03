package com.atlisheng.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DelayedQueueConfig {
    public static final String DELAYED_QUEUE_NAME = "delayed.queue";
    public static final String DELAYED_EXCHANGE_NAME = "delayed.exchange";
    public static final String DELAYED_ROUTING_KEY = "delayed.routingkey";
    @Bean
    public Queue delayedQueue() {
    return new Queue(DELAYED_QUEUE_NAME);
    }

    /**
     * @return {@link CustomExchange }
     * @描述 自定义交换机 我们在这里定义的是一个延迟交换机；不明白这里为什么key-value是x-delayed-type和direct
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        //自定义交换机的类型,放入自定义交换机的构建参数中
        args.put("x-delayed-type", "direct");
        //自定义延迟交换机需要声明类型为"x-delayed-message"，以及x-delayed-type为direct。延迟交换机的RoutingKey是固定值delayed.routingkey
        //猜测延迟交换机是一个直接交换机
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }
    @Bean
    public Binding bindingDelayedQueue(@Qualifier("delayedQueue") Queue queue,
                                       @Qualifier("delayedExchange") CustomExchange delayedExchange) {
        //自定义交换机的绑定不带参数的构建必须使用noargs方法进行构建
        return BindingBuilder.bind(queue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }
}