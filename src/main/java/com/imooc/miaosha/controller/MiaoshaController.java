package com.imooc.miaosha.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.access.AccessLimit;
import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.rabbitmq.MiaoshaMessage;
import com.imooc.miaosha.redis.Goodskey;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.OrderKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	MQSender sender;
	
	private HashMap<Long, Boolean> localOverMap = new HashMap<Long,Boolean>();
	/**
	 * initialize
	 **/
	@Override
	public void afterPropertiesSet() throws Exception {
		
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if (goodsList == null) {
			return;
		}
		for (GoodsVo goods : goodsList) {
			int stock =  goods.getStockCount();
			redisService.set(Goodskey.getMiaoshaGoodsStock, goods.getId() + "", stock);
			localOverMap.put(goods.getId(), false);
		}
	}
	
	@RequestMapping(value="/reset", method=RequestMethod.GET)
	@ResponseBody
	public Result<Boolean> reset(Model model){
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for(GoodsVo goods: goodsList) {
			goods.setStockCount(10);
			redisService.set(Goodskey.getMiaoshaGoodsStock, "" + goods.getId(), 10);
			localOverMap.put(goods.getId(), false);
		}
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
		redisService.delete(MiaoshaKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}
	
	/**
	 * QPS: 1372
	 * 5000 *10
	 * GET 幂等 无论从服务端多少次 都一样 不能对服务器端产生变化
	 * POST
	 * 
	 * After
	 * 2202
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */

	@RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> miaosha(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId, @PathVariable("path")String path) {
		//search in goods list
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//verify path
		boolean check =miaoshaService.checkPath(user, goodsId, path);
		if(!check) {
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		
		//check stock and decrease stock first
		long stock = redisService.decr(Goodskey.getMiaoshaGoodsStock, goodsId + "" );;
		if (stock < 0) {
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		
		//check miaosha over locally, reduce redis request
		if (localOverMap.get(goodsId)) {
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		
		//check repeat order
	   	MiaoshaOrder order = orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		localOverMap.put(goodsId, true);
    		return Result.error(CodeMsg.REPEAT_MIAOSHA);
    	}
		
		// put to rabbitmq queue;
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(mm);
		
		
		return Result.success(0);//in rabbitmq queue
		
		/*
		//check storage
		GoodsVo goods  =  goodsService.getGoodsVoByGoodsId(goodsId);
		int stockCount =  goods.getStockCount();
		if (stockCount <= 0) {
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		
		MiaoshaOrder order= orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null) {
			return Result.error(CodeMsg.REPEAT_MIAOSHA);
		}
		
		//reduce stock, put order, put miaosha order
		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
		model.addAttribute("orderInfo", orderInfo);
		model.addAttribute("goods", goods);

		return Result.success(orderInfo);
		*/
	}
	/**
	 * orderId: success
	 * -1 :failed
	 * 0: in queue
	 **/
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> getMiaoshaResult(
			Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId) {
		//search in goods list
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}
	
	
	@AccessLimit(seconds=5, maxCount=5, needLogin=true)
	@RequestMapping(value="/path", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaPath(Model model,
			MiaoshaUser user, @RequestParam("goodsId")long goodsId,
			@RequestParam(value="verifyCode") int verifyCode
			) {
		//search in goods list
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		boolean check  = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
		if(!check) {
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
		String path = miaoshaService.createMiaoshaPath(user, goodsId);

		return Result.success(path);
	}
	
	@RequestMapping(value="/verifyCode", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, Model model,
			MiaoshaUser user, @RequestParam("goodsId")long goodsId) {
		//search in goods list
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
	   	try {
    		BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return Result.error(CodeMsg.MIAOSHA_FAIL);
    	}
    }


}
