package com.ztuo.bc.wallet.util;


import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 通用返回对象
 *
 * @author macro
 * @date 2019/4/19
 */
public class MessageResult<T> implements Serializable {

	@ApiModelProperty(value = "返回数据")
	private T data;
	@ApiModelProperty(value = "返回码")
	private int code;
	@ApiModelProperty(value = "返回信息")
	private String message;
	private Long total;

	public MessageResult(int code , String msg){
		this.code = code;
		this.message = msg;
	}
	public MessageResult(int code , String msg, T object){
		this.code = code;
		this.message = msg;
		this.data = object;
	}
	public MessageResult(int code , String msg,T object,Long total){
		this.code = code;
		this.message = msg;
		this.total = total;
		this.data = object;
	}
	public MessageResult() {
		// TODO Auto-generated constructor stub
	}
	
	public static <T> MessageResult<T> success(){
		return new MessageResult<>(0,"SUCCESS");
	}
	public static <T> MessageResult<T> success(String msg){
		return new MessageResult<>(0,msg);
	}
	
	public static <T> MessageResult<T> success(String msg,T data){
		return new MessageResult<>(0, msg,data);
	}
	public static <T> MessageResult<T> success(T data){
		return new MessageResult<>(0, "SUCCESS",data);
	}
	public static <T> MessageResult<T> error(int code,String msg){
		return new MessageResult<>(code,msg);
	}
	public static <T> MessageResult<T> error(String msg){
		return new MessageResult<>(500,msg);
	}
	public static <T> MessageResult<T> error(){
		return new MessageResult<>(500,"fail");
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}


	@Override
	public String toString(){
		return JSONObject.toJSONString(this);
		//return "{\"code\":"+code+",\"message\":\""+message+"\"}";
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}
}
