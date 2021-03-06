package com.imooc.miaosha.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.Userkey;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.UserService;

@Controller
@RequestMapping("/demo")
public class DemoController {
	
	
	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	MQSender sender;

	@RequestMapping("/")
	@ResponseBody
	public String sayHello() {
		return "Hello World" + LocalDateTime.now();
	}
	
//	@RequestMapping("/mq/header")
//	@ResponseBody
//	public Result<String> header() {
//		sender.sendHeader("hello topic, imooc");
//		return Result.success("hello imooc");
//	}
//	
//	
//	@RequestMapping("/mq/fanout")
//	@ResponseBody
//	public Result<String> fanout() {
//		sender.sendFanout("hello topic, imooc");
//		return Result.success("hello imooc");
//	}
//	
//	@RequestMapping("/mq/topic")
//	@ResponseBody
//	public Result<String> mqTopic() {
//		sender.sendTopic("hello topic, imooc");
//		return Result.success("hello imooc");
//	}
//	
//	@RequestMapping("/mq")
//	@ResponseBody
//	public Result<String> mq() {
//		sender.send("hello imooc");
//		return Result.success("hello imooc");
//	}
	
	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> hello() {
		return Result.success("hello imooc");
//		return new Result(0, "success", "hello");
	}
	
	@RequestMapping("/helloError")
	@ResponseBody
	public Result<String> helloError() {
		return Result.error(CodeMsg.SERVER_ERROR);
	}
	
	@RequestMapping("/thymeleaf")
	
	public String thymeleaf(Model model) {
		model.addAttribute("name", "Joshua");
		return "hello";
	}
	
	@RequestMapping("/db/get")
	@ResponseBody
	public Result<User> dbGet() {
		User user = userService.getById(1);
		
		return Result.success(user);
	}
	
	@RequestMapping("/db/tx")
	@ResponseBody
	public Result<Boolean> dbTx() {
		boolean inserted = userService.tx();
		
		return Result.success(inserted);
	}
	
	@RequestMapping("/redis/get")
	@ResponseBody
	public Result<User> redisGet() {
		User v1 = redisService.get(Userkey.getById, "" + 1, User.class);
		return Result.success(v1);
	}
	
	@RequestMapping("/redis/set")
	@ResponseBody
	public Result<Boolean> redisSet() {
		User user = new User();
		user.setId(1);
		user.setName("1111");
		boolean ret = redisService.set(Userkey.getById, "" + 1, user);
//		String str = redisService.get("key2", String.class);
		return Result.success(ret);
	}

}
