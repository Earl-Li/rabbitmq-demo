package com.atlisheng.rabbitmq.first;

import com.rabbitmq.client.*;

public class Consumer {
    private final static String QUEUE_NAME = "mirrior.hello";
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        //factory.setHost("192.168.200.132");
        factory.setHost("192.168.200.133");
        factory.setUsername("earl");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        System.out.println("等待接收消息....");
        //推送的消息如何进行消费的接口回调，lambda表达式，接收到消息后对消息的处理方法，这里是方法定义，会被basicConsume调用
        //(consumerTag, delivery)->{}是lambda表达式，括号中是实参列表，连参数类型都不需要写，单个参数连小括号都不用写，大括号中写具体的方法，这个会自动实例对应接口的实现了handle方法的接口
        //看到两个函数式接口中都只有一个handle方法
        DeliverCallback deliverCallback=(consumerTag, delivery)->{
            System.out.println(delivery);//delivery表示一个消息，包含消息头，消息属性，消息体；消息内容放在消息体中
            String message= new String(delivery.getBody());
            System.out.println(message);
        };
        //消费消息中断的一个回调接口 如在消费的时候队列被删除掉了
        CancelCallback cancelCallback=(consumerTag)->{
            System.out.println("消息消费被中断");
        };
        /**
         * 信道对象的basicConsume是消费者消费消息接受消息
         * 1.消费哪个队列
         * 2.消费成功之后是否要自动应答 true 代表自动应答 false 手动应答，后面会讲
         * 3.消费者未成功消费的回调
         *
         * 获取到消息会调用DeliverCallback函数，获取不到消息会调用CancelCallback函数
         * DeliverCallback是一个函数式接口，用注解@FunctionalInterface标注，函数式接口不能实例化，需要使用匿名内部类或者lambda表达式写一个对应接口的实现类
         */
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}