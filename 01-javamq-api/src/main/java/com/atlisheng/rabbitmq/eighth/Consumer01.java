package com.atlisheng.rabbitmq.eighth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 C1消费者，要先启动C1将两个交换机和两个队列创建出来再关掉C1，模拟C1消费者宕机无法处理消息的情况
 * @创建日期 2023/11/08
 * @since 1.0.0
 */
public class Consumer01 {
    //普通交换机名称
    private static final String NORMAL_EXCHANGE = "normal_exchange";
    //死信交换机名称
    private static final String DEAD_EXCHANGE = "dead_exchange";
    public static void main(String[] argv) throws Exception {
        Channel channel = RabbitMQUtil.getChannel();
        //声明死信和普通交换机 类型为 direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        //声明死信队列，死信队列就当成正常队列声明就行，他的消息接收是有死信交换机解决的，而死信交换机的消息接收由普通队列转发控制的
        String deadQueue = "dead-queue";
        channel.queueDeclare(deadQueue, false, false, false, null);
        //死信队列绑定死信交换机与 routingKey
        channel.queueBind(deadQueue, DEAD_EXCHANGE, "lisi");

        //正常队列绑定死信队列信息,这个绑定将作为参数用在正常队列的声明中。参数包括死信交换机的名字，key为x-dead-letter-exchange是固定的
        //以及绑定死信交换机对应死信队列的RoutingKey
        Map<String, Object> params = new HashMap<>();
        //过期时间，单位默认是毫秒，这个参数可以不设置，因为生产者发送消息可以定制化每个消息的过期时间，企业一般都是生产者发消息的时候设置，好处是可以定制化过期时间
        //在这里设置会导致该队列的所有消息过期时间都是10s
        //params.put("x-message-ttl",10000);
        //正常队列设置死信交换机 参数 key 是固定值
        params.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        //正常队列设置死信 routing-key 参数 key 是固定值
        //下面死信交换机绑定了死信队列的话，这里的x-dead-letter-routing-key就可以不写，不写会走默认绑定的routingKey
        params.put("x-dead-letter-routing-key", "lisi");
        params.put("x-max-length",6);

        //声明普通队列
        String normalQueue = "normal-queue";
        //正常队列的消息成为死信，要将其转发给死信队列必须设置死信队列的交换机和死信交换机对应死信队列的RoutingKey
        //靠其他参数的死信交换机名字和绑定死信队列的RoutingKey设置决定消息成为死信后的转发地址
        channel.queueDeclare(normalQueue, false, false, false, params);
        channel.queueBind(normalQueue, NORMAL_EXCHANGE, "zhangsan");

        //接收消息回调
        System.out.println("等待接收消息.....");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            if ("info3".equals(message)){
                System.out.println("Consumer01 拒绝的消息:"+message);
                //拒绝消息且不放回原队列
                channel.basicReject(delivery.getEnvelope().getDeliveryTag(),false);
            }else {
                System.out.println("Consumer01 接收到消息:"+message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
            }
        };

        //准备接收消息
        //channel.basicConsume(normalQueue, true, deliverCallback, consumerTag -> {
        //改为手动应答测试消息拒绝
        channel.basicConsume(normalQueue, false, deliverCallback, consumerTag -> {
        });
    }
}