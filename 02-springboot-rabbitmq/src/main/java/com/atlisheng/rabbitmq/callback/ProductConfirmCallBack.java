package com.atlisheng.rabbitmq.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 发布确认回调高级
 * 发布确认是生产者和交换机之间的事情，消息应答才是交换机、队列和消费者之间的事情
 * 回调接口的使用还必须在配置文件配置spring.rabbitmq.publisher-confirm-type=correlated
 * 【        属性值none表示禁用确认发布模式，这也是默认设置；
 *          correlated表示发布消息成功到交换器后会触发回调方法；
 *          simple有两个效果，
 *              其一效果和 CORRELATED 值一样会触发回调方法，
 *              其二在发布消息成功后使用 rabbitTemplate 调用 waitForConfirms 或 waitForConfirmsOrDie 方法【特制同步确认消息中的单个确认】，等待 broker 节点返回发送结果，
 *                  根据返回结果来判定下一步的逻辑，要注意的点是waitForConfirmsOrDie方法如果返回false则会关闭channel，则接下来无法发送消息到 broker】
 * @创建日期 2023/11/10
 * @since 1.0.0
 */
@Component
@Slf4j
public class ProductConfirmCallBack implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {
    //由于ProductConfirmCallBack实现的是RabbitTemplate的内部接口，必须将该实现类注入RabbitTemplate，否则即使交给spring容器管理也找不到,
    // 粗略的理解成rabbitTemplate的一个属性，实际是RabbitTemplate在类中设置了一个confirmCallback属性【源码看到的】，通过该属性设置的回调，
    // 不注入就不能通过rabbitTemplate实例找不到这个回调实现类
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @描述 这个方法没有人执行，需要设置@PostConstruct注解让其执行
     *  @postConstruct注解是Spring的一个注解，作用是让该注解修饰的init方法在启动的时候就加载某些数据，
     * 这个注解注释的方法在@Component注解【首先执行】和@Autowired注解【在@Component后执行】后面执行，能够避免rabbitTemplate还没注入就向其注入该接口实现类
     * ？关注一下前置处理器和后置处理器
     * 我擦这个注解是java自带的javax.annotation.PostConstruct
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/10
     * @since 1.0.0
     */
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        //设置配置mandatory属性为true开启回退消息功能，不在这儿设置可以在Spring配置文件设置spring.rabbitmq.publisher.returns=true
        rabbitTemplate.setMandatory(true);
        //设置回退消息交给谁处理
        rabbitTemplate.setReturnCallback(this);
    }
    /**
    * 交换机不管是否收到消息的一个回调方法，实现RabbitTemplate的ConfirmCallback接口，该接口是一个内部接口，且是函数式接口，正常情况下这个类是没有被实现的
    *          CorrelationData   是 消息相关数据ID和相关信息
    *          ack               是 交换机是否收到消息
    *          cause             是 失败的原因
    * 交换机收到或者没有收到消息都会回调这个接口，
    *      correlationData都是消息相关数据
    *      收到boolean ack为true，没有收到boolean ack为false
    *      确认回调cause是null，错误回调cause是失败的原因
    *
     * 生产者要感知到交换机或者队列没有接收到消息，感知就通过这个回调接口完成，交换机没有确认收到消息或者交换机确认收到但失败了【队列接收不到消息】，
     * 认为是信道就会触发这个回调接口，在这个回调接口中尝试把消息返回回来进行保存，收到消息也会回调，用ack标记消息状态
    *
     * 发送消息的是RabbitTemplate,该回调接口也由RabbitTemplate调用
    */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        //尝试取id，id取不到设置id为空串，取得到正常id
        String id=correlationData!=null?correlationData.getId():"";
        //消息成功确认打印信息
        if(ack){
            log.info("交换机已经收到 id 为:{}的消息",id);
        }else{
            //消息失败确认打印信息
            log.info("交换机还未收到 id 为:{}消息,由于原因:{}",id,cause);
        }
    }

    /**
     * @param message the returned message.退回的消息
     * @param replyCode the reply code.
     * @param replyText the reply text.
     * @param exchange the exchange.退回消息的交换机
     * @param routingKey the routing key.路由key
     * @描述
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/10
     * @since 1.0.0
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("消息:{}被服务器退回，退回原因:{}, 交换机是:{}, 路由 key:{}", new String(message.getBody()),replyText, exchange, routingKey);
    }
}