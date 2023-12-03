package com.atlisheng.rabbitmq.second;

import com.atlisheng.rabbitmq.utils.RabbitMQUtil;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 发送大量有序消息的生产者
 * @创建日期 2023/11/05
 * @since 1.0.0
 */
public class Producer1 {
    private static final String QUEUE_NAME="hello";

    public static void main(String[] args)  {
        try(Channel channel= RabbitMQUtil.getChannel()) {
            channel.queueDeclare(QUEUE_NAME,false,false,false,null);
            //从控制台当中接受信息
            Scanner scanner = new Scanner(System.in);
            //如果扫描到下一个输入就进入循环，获取控制台的消息并发送给消息队列
            while (scanner.hasNext()){
                String message = scanner.next();
                channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
                System.out.println("发送消息完成:"+message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
