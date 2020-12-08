package com.imooc.miaosha.controller;

import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.RegisterVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegisterController {

	@Autowired
	MiaoshaUserService userService;
	private static Logger log = LoggerFactory.getLogger(GoodsController.class);

	@RequestMapping("/to_register")
	public String toRegister(RegisterVo registerVo) {
		return "register";
	}

	@RequestMapping("/do_register")
	@ResponseBody
	public Result<Result<MiaoshaUser>> doRegister( @Valid RegisterVo registerVo) {
		log.info(registerVo.toString());
		//登陆
		Result<MiaoshaUser> user=userService.register(registerVo);
		return Result.success(user);
	}

}