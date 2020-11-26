package com.imooc.miaosha.redis;

public class OrderKey extends BasePrefix{

	public OrderKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static OrderKey getMiaoshaOrderByUidGid = new OrderKey(0,"moug");
}
