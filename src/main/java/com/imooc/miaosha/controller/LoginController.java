package com.imooc.miaosha.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.UserService;
import com.imooc.miaosha.util.ValidatorUtil;
import com.imooc.miaosha.vo.LoginVo;

@Controller
@RequestMapping("/login")
public class LoginController {

	@Autowired
	MiaoshaUserService userService;
	private static Logger log = LoggerFactory.getLogger(LoginController.class);

	@RequestMapping("/to_login")
	public String toLogin(LoginVo loginVo) {

		return "login";
	}

	@RequestMapping("/do_login")
	@ResponseBody
	public Result<Boolean> doLogin(LoginVo loginVo) {
		log.info(loginVo.toString());
		//参数校验
		String passInput = loginVo.getPassword();
		String mobile = loginVo.getMobile();
		if(StringUtils.isEmpty(passInput)) {
			return Result.error(CodeMsg.PASSWORD_EMPTY);
		}
		if(StringUtils.isEmpty(mobile)) {
			return Result.error(CodeMsg.MOBILE_EMPTY);
		}
		
		if(!ValidatorUtil.isMobile(mobile)) {
			return Result.error(CodeMsg.MOBILE_ERROR);
		}
		
		
		//登陆
		CodeMsg cm = userService.login(loginVo);
		if(cm.getCode() ==0) {
			return Result.success(true);
		}
		
		return Result.error(cm);
	}

}
