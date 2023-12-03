package com.atlisheng.rabbitmq.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 用来测试生产者和消费者能否使用同一个连接，经过测试，生产者和消费者可以使用同一个连接对象
 * 疑惑这应该不是同一个连接对象，每个应用启动都应该使用了新的连接对象，和静态代码块没关系，因为都是单独编译执行的
 * @创建日期 2023/11/05
 * @since 1.0.0
 */
public class RabbitMQUtil {
    public static Connection connection;
    /*静态代码块让连接对象只实例化一次*/
    static {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.200.132");
        factory.setUsername("earl");
        factory.setPassword("123456");
        try {
            Connection mqConnection = factory.newConnection();
            connection=mqConnection;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
    public static Channel getChannel() throws IOException {
        System.out.println("connection对象:"+connection);
        return connection.createChannel();
    }
}
