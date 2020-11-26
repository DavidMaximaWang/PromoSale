package com.imooc.miaosha.redis;

public class AccessKey extends BasePrefix {

	public AccessKey(int expireSeconds, String prefix) {
		super(expireSeconds,prefix);
	}

	public static AccessKey withExpire(int seconds) {
		return new AccessKey(seconds, "access");
	}
}
