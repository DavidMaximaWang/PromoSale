package com.imooc.miaosha.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {

	@Autowired
	MiaoshaUserService userService;
	private static Logger log = LoggerFactory.getLogger(GoodsController.class);

	@RequestMapping("/to_login")
	public String toLogin(LoginVo loginVo) {

		return "login";
	}

	@RequestMapping("/do_login")
	@ResponseBody
	public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
		log.info(loginVo.toString());
		//登陆
		userService.login(response, loginVo);
		
		return Result.success(true);
	}

}