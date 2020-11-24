package com.imooc.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.redis.RedisService;

import org.springframework.beans.factory.annotation.Autowired;


@Service
public class MQReceiver {
	
	@Autowired
	RedisService redisService;

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

	@RabbitListener(queues = MQConfig.QUEUE)
	public void receive(String message) {
		log.info("received message: " + message);

	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String message) {
		log.info("topic queue1 message: " + message);

	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String message) {
		log.info("topic queue2 message: " + message);

	}

	@RabbitListener(queues = MQConfig.HEADERS_QUEUE)
	public void receiveHeaders(byte[] message) {
		log.info("header queue message: " + new String(message));

	}
//	@RabbitListener(queues=MQConfig.HEADERS_QUEUE)
//	public void receiveHeaderQueue(byte[] message) {
//		log.info(" header  queue message:"+new String(message));
//	}
}
