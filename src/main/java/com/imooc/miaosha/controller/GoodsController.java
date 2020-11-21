package com.imooc.miaosha.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import com.alibaba.druid.util.StringUtils;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.redis.Goodskey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.GoodsDetailVo;
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
	
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver ;
	/*
	 * QPS: 1267
	 * 5000 * 10 = 5000 
	 * QPS 2656
	 */

	@RequestMapping(value = "/to_list", produces="text/html")
	@ResponseBody
	public String toList(HttpServletRequest request,  HttpServletResponse response, Model model, MiaoshaUser user) {
		// get html from cache
		String html = redisService.get(Goodskey.goodsList, "", String.class);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}

		// manual get html 手动渲染
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);		
		IWebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
				model.asMap());
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
		// search in goods list

		if (!StringUtils.isEmpty(html)) {
			redisService.set(Goodskey.goodsList, "", html);
		}
		return html;
	}
	
	@RequestMapping(value="/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> toDetail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
			@PathVariable("goodsId") long goodsId) {

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
				
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoods(goods);
		vo.setMiaoshaStatus(miaoshaStatus);
		vo.setRemainSeconds(remainSeconds);
		vo.setUser(user);
		
		return Result.success(vo);
	}
	@RequestMapping(value="/to_detail1/{goodsId}", produces="text/html")
	@ResponseBody
	public String toDetail1(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
			@PathVariable("goodsId") long goodsId) {
		// get html from cache
		String html = redisService.get(Goodskey.goodsDetail, "" + goodsId, String.class);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}

		// search in goods list
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
		
		IWebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
				model.asMap());
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
		if (!StringUtils.isEmpty(html)) {
			redisService.set(Goodskey.goodsDetail, "" + goodsId, html);
		}
		return html;
		// search in goods list
//		return "goods_detail";
	}

}
