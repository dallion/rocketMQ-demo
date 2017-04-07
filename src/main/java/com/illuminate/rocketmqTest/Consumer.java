package com.illuminate.rocketmqTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;

public class Consumer {

  private static final Logger LOGGER = Logger.getLogger(Consumer.class);

  private static final AtomicInteger COUNT = new AtomicInteger(0);

  private static final Properties PROP = new Properties();
  static {
    ScheduledThreadPoolExecutor schdular = new ScheduledThreadPoolExecutor(1);
    schdular.scheduleAtFixedRate(new Runnable() {

      public void run() {
        LOGGER.info("receive " + COUNT.get() + " message in 60 second ");
        COUNT.set(0);
      }
    }, 0, 60, TimeUnit.SECONDS);

    InputStream is = Consumer.class.getResourceAsStream("/config.properties");
    try {
      PROP.load(is);
    } catch (IOException e) {
      LOGGER.error("Can`t load config.properties");
    }
  }

  public static void main(String[] args) {
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("PushConsumer");
    consumer.setNamesrvAddr("127.0.0.1:9876");
    try {
      // 订阅PushTopic下Tag为push的消息
      consumer.subscribe("topic", "sub_topic");
      // 程序第一次启动从消息队列头取数据
      consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
      consumer.registerMessageListener(new MessageListenerConcurrently() {
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext Context) {
          /*
           * Message msg = list.get(0); System.out.println(msg.toString());
           */
          // MessageExt messageExt = list.get(0);

          // System.out.println("msg:\t" + new String(messageExt.getBody()));


          for (MessageExt messageExt : list) {
            LOGGER.trace("receive message from " + messageExt.getBornHost().toString());
            COUNT.incrementAndGet();
          }

          return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
      });
      consumer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
