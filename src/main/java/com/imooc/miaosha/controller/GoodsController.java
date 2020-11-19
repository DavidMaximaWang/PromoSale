package com.imooc.miaosha.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;

	@RequestMapping("/to_list")
	public String toList(Model model, MiaoshaUser user) {
		//search in goods list
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		return "goods_list";
	}
	
	@RequestMapping("/to_detail/{goodsId}")
	public String toDetail(Model model, MiaoshaUser user, @PathVariable("goodsId") long goodsId) {
		//search in goods list
		model.addAttribute("user", user);
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		
		long now = System.currentTimeMillis();
		
		int miaoshaStatus = 0;
		
		int remainSeconds = 0;
		if (now < startAt) {//倒计时
			miaoshaStatus = 0;
			remainSeconds =(int)( (startAt - now) /1000);
		}else if (now > endAt)//结束了
		{
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		return "goods_detail";
	}
}
