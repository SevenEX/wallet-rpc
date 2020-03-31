package com.ztuo.bc.wallet.interceptor;


/** 
 * @Description: 定义签名异常
 * @ClassName: SignFailException
 */
public class SignFailException extends RuntimeException {
	public SignFailException(String errorCode, Throwable message) {
		super(errorCode, message);
	}
	public SignFailException(String message) {
		super(message);
	}
	public SignFailException(String message, String sysId) {
		super(message);
	}
}
