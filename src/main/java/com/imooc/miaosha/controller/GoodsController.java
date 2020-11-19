package com.imooc.miaosha.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.druid.util.StringUtils;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.service.MiaoshaUserService;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;

	@RequestMapping("/to_list")
	public String toList(HttpServletResponse response, Model model,
			@CookieValue(value=MiaoshaUserService.COOKI_NAME_TOKEN, required=false) String cookieToken,
			@RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN, required=false) String paramToken)
	{
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return "login";
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);
		model.addAttribute("user", user);
		return "goods_list";
	}
}