package com.imooc.miaosha.service;

import com.alibaba.druid.util.StringUtils;
import com.imooc.miaosha.dao.MiaoshaUserDao;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.exception.GlobalException;
import com.imooc.miaosha.redis.MiaoshaUserkey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.LoginVo;
import com.imooc.miaosha.vo.RegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

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

	public Result<MiaoshaUser> register(RegisterVo registerVo) {
		if (registerVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = registerVo.getMobile();
		long id = Long.parseLong(mobile);
		if ( getById(id) != null){
			throw new GlobalException(CodeMsg.REGISTERED_ALREADY);
		}

		String nickname = registerVo.getNickname();
		String password = registerVo.getPassword();
		String repeatedPassward = registerVo.getRepeatedPassword();
		if (!password.equals(repeatedPassward)){
			throw new GlobalException(CodeMsg.PASSWORD_NOT_IDENTICAL);
		}
		MiaoshaUser user = new MiaoshaUser();
		String salt = "1a2b3c";
		String calcPass = MD5Util.formPassToDBPass(password, salt);
		try{
			user.setId(id);
			user.setNickname(nickname);
			user.setPassword(calcPass);
			user.setSalt(salt);
			user.setRegisterDate(new Date());
			user.setLoginCount(0);

			miaoshaUserDao.insert(user);
			return Result.success(user);
		}catch(Exception e){
			e.printStackTrace();
			return Result.error(CodeMsg.REGISTER_FAILED);
		}
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
