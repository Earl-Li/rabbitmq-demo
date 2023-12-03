package com.atlisheng.rabbitmq.controller;

import com.atlisheng.rabbitmq.callback.ProductConfirmCallBack;
import com.atlisheng.rabbitmq.config.DelayedQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 发送订单消息的控制器
 * @创建日期 2023/11/09
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    public static final String CONFIRM_EXCHANGE_NAME = "confirm.exchange";
    //再设置rabbitTemplate的回调对象，也可以在控制器方法中对rabbitTemplate注入回调实现类
    /*@Autowired
    private ProductConfirmCallBack productConfirmCallBack;

    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(productConfirmCallBack);
    }*/
    //使用rabbitTemplate来实现向交换机发送消息
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sms/{message}")
    public void sendMessageToMQ(@PathVariable String message){
        log.info("当前时间: {} -- 您有新的未支付订单:{}",new Date(),message);
        //指定交换机名字和RoutingKey，以及消息本身
        rabbitTemplate.convertAndSend("X", "XA", "消息来自有效时长为10S的队列: "+message);
        rabbitTemplate.convertAndSend("X", "XB", "消息来自有效时长为40S的队列: "+message);
    }

    /**
     * @param message
     * @param ttl
     * @描述 通过在发送消息时设置CorrelationData实现类的messageProperties属性的expiration属性为预期时长实现设置消息的有效时间
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @GetMapping("/custom/{message}/{ttl}")
    public void sendCustomTTLMessage(@PathVariable String message,@PathVariable String ttl){
        log.info("当前时间: {} ,发送一条有效时长为{}s的信息给队列QC:{}",new Date(),Integer.parseInt(ttl)/1000,message);
        rabbitTemplate.convertAndSend("X","XC",message,correlationData->{
            correlationData.getMessageProperties().setExpiration(ttl);
            return correlationData;
        });
    }

    /**
     * @param message
     * @param delayTime
     * @描述 发送延迟消息到使用插件实现的延迟交换机
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/09
     * @since 1.0.0
     */
    @GetMapping("delay/{message}/{delayTime}")
    public void sendMsg(@PathVariable String message,@PathVariable Integer delayTime) {
        rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME,
                DelayedQueueConfig.DELAYED_ROUTING_KEY,
                message,
                //设置延迟是对交换机设置的吗？设置延迟对设置消息在队列中的过期有效果吗
                correlationData ->{
                    correlationData.getMessageProperties().setDelay(delayTime);
                    return correlationData;
                });
        log.info(" 当前时间:{},发送一条延迟{}秒的信息给队列delayed.queue:{}", new Date(),delayTime/1000, message);
    }

    /**
     * @param message
     * @描述 确认发布高级发送消息
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/10
     * @since 1.0.0
     */
    @GetMapping("confirm/{message}")
    public void sendMessage(@PathVariable String message){
        //指定消息 id 为 1
        CorrelationData correlationData1=new CorrelationData("1");
        String routingKey="key1";
        //发消息相较于普通方法多了一个CorrelationData参数，传参指定消息的id，这里面还有一个message类型的returnedMessage属性，会自动将发送的消息存入该属性
        rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME,routingKey,message+",id:1",correlationData1);
        log.info("发送消息内容:{}",message+",id:1");

        //睡一秒观察信道关闭会不会影响后续消息的发送，经过测试不会影响
        try{
            TimeUnit.SECONDS.sleep(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        //演示交换机名字不正确找不到交换机回调接口缓存数据
        CorrelationData correlationData2=new CorrelationData("2");
        routingKey="key1";
        rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME+2,routingKey,message+",id:2",correlationData2);
        log.info("发送消息内容:{}",message+",id:2");

        //睡一秒观察信道关闭会不会影响后续消息的发送
        try{
            TimeUnit.SECONDS.sleep(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        //演示RoutingKey不正确找不到队列交换机回调正常执行，但是消费者接收不到消息，这种情况消息仍然丢失，需要再写一个回调接口实现类
        CorrelationData correlationData3=new CorrelationData("3");
        routingKey="key3";
        rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME,routingKey,message+",id:3",correlationData3);
        log.info("发送消息内容:{}",message+",id:3");
    }
}
