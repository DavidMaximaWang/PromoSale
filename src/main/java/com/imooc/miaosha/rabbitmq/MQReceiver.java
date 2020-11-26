package com.imooc.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;

import org.springframework.beans.factory.annotation.Autowired;


@Service
public class MQReceiver {
	@Autowired
	OrderService orderService;
	@Autowired
	GoodsService goodsService;
	@Autowired
	MiaoshaService miaoshaService;
	@Autowired
	RedisService redisService;

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
	
	@RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
	public void receive(String message) {
		log.info("received message: " + message);
		MiaoshaMessage mm = RedisService.stringToBean(message, MiaoshaMessage.class);
		MiaoshaUser user = mm.getUser();
		long goodsId = mm.getGoodsId();
		
		
		////////////
		GoodsVo goods  =  goodsService.getGoodsVoByGoodsId(goodsId);
		int stockCount =  goods.getStockCount();
		if (stockCount <= 0) {
			return ;
		}
		
		
		MiaoshaOrder order= orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null) {
			return ;
		}
		
		
		//reduce stock, put order, put miaosha order
		miaoshaService.miaosha(user, goods);
	}

//	@RabbitListener(queues = MQConfig.QUEUE)
//	public void receive(String message) {
//		log.info("received message: " + message);
//
//	}
//
//	@RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
//	public void receiveTopic1(String message) {
//		log.info("topic queue1 message: " + message);
//
//	}
//
//	@RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
//	public void receiveTopic2(String message) {
//		log.info("topic queue2 message: " + message);
//
//	}
//
//	@RabbitListener(queues = MQConfig.HEADERS_QUEUE)
//	public void receiveHeaders(byte[] message) {
//		log.info("header queue message: " + new String(message));
//
//	}

}
