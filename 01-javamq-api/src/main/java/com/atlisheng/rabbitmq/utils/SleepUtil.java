package com.atlisheng.rabbitmq.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author Earl
 * @version 1.0.0
 * @描述 睡眠工具类
 * @创建日期 2023/11/06
 * @since 1.0.0
 */
public class SleepUtil {
    /**
     * @param second
     * @描述 传入秒，当前线程睡对应秒，使用Thread.sleep()实现
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/06
     * @since 1.0.0
     */
    public static void sleepInSecond(int second){
        try {
            //直接通过线程睡，定死了只能用秒作为单位
            Thread.sleep(1000*second);
        } catch (InterruptedException _ignored) {
            Thread.currentThread().interrupt();//发生异常通过当前线程的interrupt()方法唤醒当前线程
        }
    }

    /**
     * @param milliseconds
     * @描述 传入毫秒数，使用TimeUnit枚举类型的sleep方法实现对应的时间数量级的当前线程睡眠
     * @author Earl
     * @version 1.0.0
     * @创建日期 2023/11/06
     * @since 1.0.0
     */
    public static void sleepInMilliseconds(int milliseconds){
        try{
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
