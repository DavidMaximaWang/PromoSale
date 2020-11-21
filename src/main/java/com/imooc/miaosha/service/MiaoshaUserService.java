package com.imooc.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.imooc.miaosha.dao.MiaoshaUserDao;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.exception.GlobalException;
import com.imooc.miaosha.redis.MiaoshaUserkey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.LoginVo;

@Service
public class MiaoshaUserService {
	public static final String COOKI_NAME_TOKEN = "token";

	@Autowired
	MiaoshaUserDao miaoshaUserDao;

	@Autowired
	RedisService redisService;

	public MiaoshaUser getById(long id) {
		//get from cache
		MiaoshaUser user = redisService.get(MiaoshaUserkey.getById, ""+id, MiaoshaUser.class);
		if(user != null) {
			return user;
		}
		
		//Get user from db
		user = miaoshaUserDao.getById(id);
		
		if(user != null) {
			redisService.set(MiaoshaUserkey.getById, ""+ id, user);
		}

		return user;
	}

	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if (StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserkey.token, token, MiaoshaUser.class);
		// refresh expire time
		if (user!= null) {
			addCookie(response, token, user);
		}
		
		return user;
	}
	
	public boolean updatePass(String token, long id, String formPass) {
		//Get user 
		MiaoshaUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		
		//update user pass
		MiaoshaUser toBeUpdate = new MiaoshaUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
		miaoshaUserDao.update(toBeUpdate);
		//handle cache
		
		redisService.delete(MiaoshaUserkey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(MiaoshaUserkey.token, token, user);
		return true;
	}

	public String login(HttpServletResponse response, LoginVo loginVo) {
		if (loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}

		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		// phone number exists?
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if (user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		// verify password;

		String dbPass = user.getPassword();
		String slatDB = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, slatDB);
		if (!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		// generate cookie
		String token = null;
		if(user != null) {
			token = UUIDUtil.uuid();
			addCookie(response, token, user);
		}
		
		return token;
	}

	private void addCookie(HttpServletResponse response, String token,  MiaoshaUser user) {
		redisService.set(MiaoshaUserkey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
		cookie.setMaxAge(MiaoshaUserkey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}

}
