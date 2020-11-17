package com.imooc.miaosha.result;

public class CodeMsg {
	private int code;
	private String msg;
	
	
	//generic error
	public static CodeMsg SUCCESS =  new CodeMsg(0, "success");
	public static CodeMsg SERVER_ERROR =  new CodeMsg(500100, "SERVER ERROR");
	
	
	//login 模块
	
	//商品模块
	
	//订单模块
	
	//
	
	private CodeMsg(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	

}
