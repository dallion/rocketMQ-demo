package com.illuminate.rocketmqTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

public class Producer {

  private static final Logger LOGGER = Logger.getLogger(Producer.class);

  private static final AtomicInteger COUNT = new AtomicInteger(0);

  private static final Properties PROP = new Properties();

  static {
    ScheduledThreadPoolExecutor schdular = new ScheduledThreadPoolExecutor(1);
    schdular.scheduleAtFixedRate(new Runnable() {

      public void run() {
        LOGGER.info("send " + COUNT.get() + " message in 60 second ");
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
    Producer p = new Producer();
    int threadNum = Integer.parseInt(PROP.getProperty("producerThreadNumber"));
    ThreadPoolExecutor pool =
        new ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
    for (int i = 0; i < threadNum; i++) {
      Worker task = p.new Worker();
      pool.execute(task);
    }
  }

  class Worker implements Runnable {

    public void run() {
      DefaultMQProducer producer = new DefaultMQProducer(Thread.currentThread().getName());
      producer.setNamesrvAddr("127.0.0.1:9876");
      try {
        producer.start();
        while (true) {
          Message msg = new Message("topic", "sub_topic", "1", ("this is " + COUNT.incrementAndGet() + " message ").getBytes());
          SendResult result = producer.send(msg);

          /*
           * msg = new Message("PushTopic", "push", "2", "Just for test.".getBytes());
           * 
           * result = producer.send(msg); System.out.println("id:" + result.getMsgId() + " result:"
           * + result.getSendStatus());
           */

          msg = new Message("PullTopic", "pull", "1", "Just for test.".getBytes());
          result = producer.send(msg);
          LOGGER.trace("id:" + result.getMsgId() + " result:" + result.getSendStatus());
          // if (i % 10 == 0) {
          // Thread.sleep(20000);
          // }
        }

      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        // producer.shutdown();
      }
    }

  }
}
