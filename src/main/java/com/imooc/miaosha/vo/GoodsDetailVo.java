package com.imooc.miaosha.vo;

import com.imooc.miaosha.domain.MiaoshaUser;

public class GoodsDetailVo {
	private GoodsVo goods;
	
	private int miaoshaStatus = 0;
	private int remainSeconds = 0;
	private MiaoshaUser user;
	public GoodsVo getGoods() {
		return goods;
	}
	public int getMiaoshaStatus() {
		return miaoshaStatus;
	}
	public int getRemainSeconds() {
		return remainSeconds;
	}
	public MiaoshaUser getUser() {
		return user;
	}
	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}
	
	public void setMiaoshaStatus(int miaoshaStatus) {
		this.miaoshaStatus = miaoshaStatus;
	}
	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}
	public void setUser(MiaoshaUser user) {
		this.user = user;
	}
}
