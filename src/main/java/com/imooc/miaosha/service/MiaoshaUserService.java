package com.imooc.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.miaosha.dao.MiaoshaUserDao;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.exception.GlobalException;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.vo.LoginVo;

@Service
public class MiaoshaUserService {
	
	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	public MiaoshaUser getById(long id) {
		MiaoshaUser miaoshaUser = miaoshaUserDao.getById(id);
		
		return miaoshaUser;
	}
	public boolean login(LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		// phone number exists?
		MiaoshaUser  user = getById(Long.parseLong(mobile));
		if (user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//verify password;
		
		String dbPass = user.getPassword();
		String slatDB = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, slatDB);
		if (!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		
		return true;
	}
}
