package com.imooc.miaosha.redis;

public class Goodskey extends BasePrefix {

	public Goodskey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	public static Goodskey goodsList = new Goodskey(60, "gl");
	public static KeyPrefix goodsDetail = new Goodskey(60, "gd");
}
