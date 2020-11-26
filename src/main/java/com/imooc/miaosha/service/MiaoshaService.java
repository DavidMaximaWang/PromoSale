package com.imooc.miaosha.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.GoodsVo;

@Service
public class MiaoshaService {

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;
	
	@Autowired
	RedisService redisService;

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		// reduce stock
		boolean success = goodsService.reduceStock(goods);

		if (success) {
			// put order, orderinfo, miaosha order
			OrderInfo orderInfo = orderService.createOrder(user, goods);
			return orderInfo;
		}
		
		setGoodsOver(goods.getId());
		return null;

	}


	public long getMiaoshaResult(long userId, long goodsId) {

		MiaoshaOrder order = orderService.getOrderByUserIdGoodsId(userId, goodsId);
		if (order !=null) {
			return order.getOrderId();
			
		}
		else if (getGoodsOver(goodsId)) {
			return -1;
		}
		
		return 0;
	}
	
	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, "" + goodsId, true);
	}

	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, "" + goodsId);
	}


	public void reset(List<GoodsVo> goodsList) {
		goodsService.resetStock(goodsList);
		orderService.deleteOrders();
		
	}


	public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
		if (user == null || path == null) {
			return false;
		}
		String pathSaved = redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getId()+"_"+goodsId, String.class);
		return pathSaved.equals(path);
	}


	public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
		String path = MD5Util.md5(UUIDUtil.uuid()+"123456");
		redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getId()+"_"+goodsId, path);
		return path;
	}
}
