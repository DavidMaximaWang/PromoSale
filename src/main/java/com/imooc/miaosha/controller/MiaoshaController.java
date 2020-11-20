package com.imooc.miaosha.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

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

	@RequestMapping("/do_miaosha")
	public String doMiaosha(Model model, MiaoshaUser user, @RequestParam("goodsId")long goodsId) {
		//search in goods list
		if(user == null) {
			return "login";
		}
		
		//check storage
		GoodsVo goods  =  goodsService.getGoodsVoByGoodsId(goodsId);
		int stockCount =  goods.getStockCount();
		if (stockCount <= 0) {
			model.addAttribute("errmsg", CodeMsg.MIAOSHA_OVER.getMsg());
			return "miaosha_fail";
		}
		
		MiaoshaOrder order= orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null) {
			model.addAttribute("errmsg",  CodeMsg.REPEAT_MIAOSHA.getMsg());
			return "miaosha_fail";
		}
		
		//reduce stock, put order, put miaosha order
		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
		model.addAttribute("orderInfo", orderInfo);
		model.addAttribute("goods", goods);

		return "order_detail";
	}
	

}
