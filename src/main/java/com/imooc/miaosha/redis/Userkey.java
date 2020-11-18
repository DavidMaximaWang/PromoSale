package com.imooc.miaosha.redis;

public class Userkey extends BasePrefix {

	public Userkey(String prefix) {
		super(prefix);
	}

	public static Userkey getById = new Userkey("id");
	public static Userkey getByName = new Userkey("name");
}
