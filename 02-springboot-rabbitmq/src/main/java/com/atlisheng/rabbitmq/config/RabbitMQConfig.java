package com.atlisheng.rabbitmq.config;

import org.springframework.amqp.core.*;//AMQP的全称为：Advanced Message Queuing Protocol（高级消息队列协议）
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 RabbitMQ的相关配置
 * 队列转发到死信交换机不需要单独的绑定，只需要声明队列的时候传参死信交换机和RoutingKey
 * @创建日期 2023/11/09
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {
    //普通交换机名字
    public static final String X_EXCHANGE = "X";
    //死信交换机的名字
    public static final String Y_DEAD_LETTER_EXCHANGE = "Y";
    //普通队列名字
    public static final String QUEUE_A = "QA";
    public static final String QUEUE_B = "QB";
    //死信队列名字
    public static final String DEAD_LETTER_QUEUE = "QD";
    //通用队列QC【不设置TTL】
    public static final String QUEUE_C= "QC";

    @Bean
    public Queue queueC(){
        //QC绑定消息转发死信交换机
        Map<String,Object> arguments=new HashMap<>(3);
        arguments.put("x-dead-letter-exchange",Y_DEAD_LETTER_EXCHANGE);
        arguments.put("x-dead-letter-routing-key","YD");
        //构建队列
        return QueueBuilder.durable(QUEUE_C).withArguments(arguments).build();
    }

    @Bean
    public Binding queueCBindingX(){
        return new Binding(QUEUE_C, Binding.DestinationType.QUEUE,X_EXCHANGE,"XC",null);
        //QC绑定普通交换机
        //return BindingBuilder.bind(queueC()).to(xExchange()).with("XC");
    }

    /**
     * @return {@link DirectExchange }
     * @描述  以org.springframework.amqp.core.DirectExchange注入spring容器 声明直接交换机 xExchange
     * 传参交换机的名字
     * 交换机也有ExchangeBuilder
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @Bean("xExchange")
    public DirectExchange xExchange(){
        return new DirectExchange(X_EXCHANGE);
    }
    // 声明 xExchange
    @Bean("yExchange")
    public DirectExchange yExchange(){
        return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
    }

    /**
     * @return {@link Queue }
     * @描述 org.springframework.amqp.core.Queue注入Spring容器声明队列
     * 创建HashMap放入对应死信交换机和RoutingKey，以及队列中消息的有效时间10s
     * 用静态方法QueueBuilder.durable(QUEUE_A).withArguments(args).build()，声明持久化和传递其他参数，为啥队列名要放在durable中
     * 用的QueueBuilder，队列绑定死信交换机只针对当前队列，多个队列绑定同一个死信交换机需要在每个队列中都声明一次
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @Bean("queueA")
    public Queue queueA(){
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YD");
        //声明队列的 TTL,单位ms
        args.put("x-message-ttl", 10000);
        return QueueBuilder.durable(QUEUE_A).withArguments(args).build();
    }

    /**
     * @param queueA
     * @param xExchange
     * @return {@link Binding }
     * @描述 声明队列 A 绑定 X 交换机，绑定也要单独以Binding的形式传递给Spring容器，用的BuildingBuilder
     * 绑定不需要被调用，不用被起名
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @Bean
    public Binding queueABindingX(@Qualifier("queueA") Queue queueA,
                                  @Qualifier("xExchange") DirectExchange xExchange){
        return BindingBuilder.bind(queueA).to(xExchange).with("XA");
    }

    //声明队列 B ttl 为 40s 并绑定到对应的死信交换机
    @Bean("queueB")
    public Queue queueB(){
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YD");
        //声明队列的 TTL
        args.put("x-message-ttl", 40000);
        return QueueBuilder.durable(QUEUE_B).withArguments(args).build();
    }

    //声明队列 B 绑定 X 交换机
    @Bean
    public Binding queueBBindingX(@Qualifier("queueB") Queue queue1B,
                                  @Qualifier("xExchange") DirectExchange xExchange){
        return BindingBuilder.bind(queue1B).to(xExchange).with("XB");
    }

    /**
     * @return {@link Queue }
     * @描述 一般的queue声明如果只需要设置名字不需要设置其他参数可以直接返回Queue对象传参队列名称，不需要使用QueueBuilder
     * 声明死信队列
     * @Qualifier注解是spring中的注解，参数根据类型注入。多个相同类型的bean必须指定ID
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @Bean("queueD")
    public Queue queueD(){
        return new Queue(DEAD_LETTER_QUEUE);
    }
    //声明死信队列 QD 绑定关系
    @Bean
    public Binding deadLetterBindingQAD(@Qualifier("queueD") Queue queueD,
                                        @Qualifier("yExchange") DirectExchange yExchange){
        return BindingBuilder.bind(queueD).to(yExchange).with("YD");
    }
}
