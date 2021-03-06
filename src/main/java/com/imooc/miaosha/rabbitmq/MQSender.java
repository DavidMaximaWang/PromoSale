package com.imooc.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.redis.RedisService;

@Service
public class MQSender {

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
	@Autowired
	AmqpTemplate amqpTemplate;
	public void sendMiaoshaMessage(MiaoshaMessage mm) {
		String msg = RedisService.beanToString(mm);
		log.info("sent message: " + msg);
		amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
	}
	
//	public void send(Object message) {
//
//		String msg = RedisService.beanToString(message);
//		log.info("sent message: " + msg);
//		amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
//	}
//
//	public void sendTopic(Object message) {
//
//		String msg = RedisService.beanToString(message);
//		log.info("sent message: " + msg);
//		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCJAMGE, MQConfig.ROUTING_KEY1, msg + "1");
//		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCJAMGE, MQConfig.ROUTING_KEY2, msg + "2");
//	}
//	
//	public void sendFanout(Object message) {
//
//		String msg = RedisService.beanToString(message);
//		log.info("sent message: " + msg);
//		amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg + ".fanout");
//	}
//	
//	public void sendHeader(Object message) {
//
//		String msg = RedisService.beanToString(message);
//		log.info("sent header message: " + msg);
//		MessageProperties properties = new MessageProperties();
//		properties.setHeader("header1", "value1");
//		properties.setHeader("header2", "value2");
//		Message obj = new Message(msg.getBytes(),properties);
//		
//		amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
//	}



	
//	public void sendHeader(Object message) {
//	String msg = RedisService.beanToString(message);
//	log.info("send fanout message:"+msg);
//	MessageProperties properties = new MessageProperties();
//	properties.setHeader("header1", "value1");
//	properties.setHeader("header2", "value2");
//	Message obj = new Message(msg.getBytes(), properties);
//	amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
//}

}
