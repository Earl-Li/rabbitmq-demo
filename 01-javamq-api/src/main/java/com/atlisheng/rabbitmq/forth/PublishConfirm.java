package com.atlisheng.rabbitmq.forth;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.atlisheng.rabbitmq.utils.SleepUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 发布确认模式
 * 1. 单个确认
 * 2. 批量确认
 * 3. 异步批量确认
 * 测试各个模式发布确认时间，比对各个模式下的性能
 * @创建日期 2023/11/06
 * @since 1.0.0
 */
public class PublishConfirm {

    /**
     *消息发送数量
     */
    public static final int MESSAGE_COUNT=1000;
    public static void main(String[] args) throws Exception {
        //PublishConfirm.singlePublishConfirm();//单个发布确认发布消息1000条耗时460ms(课程每条都有打印行为耗时700多毫秒)
        //PublishConfirm.batchPublishConfirm();//发布1000个批量确认消息,耗时58ms
        PublishConfirm.asyncPublishConfirm();//发布1000个异步确认消息,耗时25ms
    }

    /**
     * @描述 单个发布确认发送1000条消息
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/06
     * @since 1.0.0
     */
    public static void singlePublishConfirm() throws Exception {
        //UUID的长度是36位长度随机长度的id
        try (Channel channel = RabbitMQUtil.getChannel()) {
            String queueName = UUID.randomUUID().toString();
            channel.queueDeclare(queueName, false, false, false, null);
            //开启发布确认
            channel.confirmSelect();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String message = i + "";
                channel.basicPublish("", queueName, null, message.getBytes());
                //服务端返回 false 或超时时间内未返回，生产者可以消息重发
                boolean flag = channel.waitForConfirms();
            }
            long end = System.currentTimeMillis();
            System.out.println("单个发布确认发布消息" + MESSAGE_COUNT + "条耗时" + (end - begin) + "ms");
        }

    }

    /**
     * @描述 单个发布确认发送1000条消息
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/06
     * @since 1.0.0
     */
    public static void batchPublishConfirm() throws Exception {
        //UUID的长度是36位长度随机长度的id
        try (Channel channel = RabbitMQUtil.getChannel()) {
            String queueName = UUID.randomUUID().toString();
            channel.queueDeclare(queueName, true, false, false, null);
            //开启发布确认
            channel.confirmSelect();
            //批量确认消息大小
            int batchSize = 100;
            //未确认消息个数
            int outstandingMessageCount = 0;
            long begin = System.currentTimeMillis();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String message = i + "";
                channel.basicPublish("", queueName, null, message.getBytes());
                outstandingMessageCount++;
                if (outstandingMessageCount == batchSize) {
                    channel.waitForConfirms();
                    outstandingMessageCount = 0;
                }
            }
            //为了确保还有剩余没有确认消息 再次确认
            if (outstandingMessageCount > 0) {
                channel.waitForConfirms();
            }
            long end = System.currentTimeMillis();
            System.out.println("发布" + MESSAGE_COUNT + "个批量确认消息,耗时" + (end - begin) + "ms");
        }
    }

    /**
     * @描述异步发布确认发送1000条信息
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/07
     * @since 1.0.0
     */
    public static void asyncPublishConfirm() throws Exception{
        try (Channel channel = RabbitMQUtil.getChannel()) {
            String queueName = UUID.randomUUID().toString();
            channel.queueDeclare(queueName, true, false, false, null);
            //开启发布确认
            channel.confirmSelect();
            /**
             * 线程安全有序的一个哈希表，适用于高并发的情况
             * 1.轻松的将序号与消息进行关联
             * 2.轻松批量删除条目 只要给到序列号
             * 3.支持并发访问
             */
            ConcurrentSkipListMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();
            /**
             * 确认收到消息的一个回调
             * 参数1.消息序列号
             * 参数2.true 可以确认小于等于当前序列号的消息[是否为批量确认]
             *      false 只能确认当前序列号消息
             *
             * 这个方法会被多次调用，因为消息队列会多次回调
             */
            ConfirmCallback ackCallback = (sequenceNumber, multiple) -> {
                //这个if和else的逻辑学完ConcurrentSkipListMap再回来看，这里是根据序号删除已经被确认的数据，所有发送的数据都被记录在这个并发跳跃哈希表中了
                if (multiple) {
                    //删除已经确认发布的消息，剩余未被确认的消息 是一个 map,这个outstandingConfirms.headMap不太懂是什么意思【删除已确认消息】
                    //headMap类似与获取当前序号前的所有序号,headMap的返回值其实就是一个从第一个Key到传入headMap方法的key所有的组成的一个子跳表
                    //跳表相比于简单的哈希就是跳表的Key是按照插入顺序来的，看一下源码就知道那个headMap方法是得到传进去的Key到第一个Key的所有Key，目的是可以调用clear批量删除。
                    //ConcurrentNavigableMap这是个接口，实现类就有ConcurrentSkipListMap
                    ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(sequenceNumber, true);
                    System.out.println("本次确认子跳表:"+confirmed);
                    //清除该部分未确认消息，卧槽这是怎么识别该清除那个集合中的
                    confirmed.clear();
                }else{
                    //不是批量确认只删除当前序列号对应的消息
                    outstandingConfirms.remove(sequenceNumber);
                    System.out.println("本次确认子跳表:"+sequenceNumber);
                }
                //打印当前确认的消息
                //System.out.println("确认的消息:"+multiple+" | "+sequenceNumber);
                //打印当前已发布未被确认的消息
                //System.out.println(outstandingConfirms);
            };
            /**
             * 消息接收失败的回调
             * */
            ConfirmCallback nackCallback = (sequenceNumber, multiple) -> {
                String message = outstandingConfirms.get(sequenceNumber);
                System.out.println("未确认消息:"+message+" | "+sequenceNumber);
            };
            /**
             * 添加一个异步确认的监听器
             * 1.确认收到消息的回调
             * 2.未收到消息的回调
             */
            channel.addConfirmListener(ackCallback, nackCallback);
            long begin = System.currentTimeMillis();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String message = "消息" + i;
                /**
                 * 将消息
                 * channel.getNextPublishSeqNo()获取下一个消息的序列号，这个map的序号不是人为定的，是调惨获取的
                 * 通过序列号与消息体进行一个关联
                 * 全部都是未确认的消息体
                 */
                outstandingConfirms.put(channel.getNextPublishSeqNo(), message);
                channel.basicPublish("", queueName, null, message.getBytes());
            }
            long end = System.currentTimeMillis();
            System.out.println("发布" + MESSAGE_COUNT + "个异步确认消息,耗时" + (end - begin) + "ms");
            //主线程不能提前噶，噶了是不是信道就没了，信道没了就无法确认？为什么视频中没有噶
            SleepUtil.sleepInSecond(10);
        }
    }
}
