package com.ztuo.bc.wallet.interceptor;


/** 
 * @Description: 定义参数CHECK异常
 * @ClassName: ParamsCheckException 
 * @author nz
 * @date 2018年4月30日 下午2:53:34  
 */
public class ParamsCheckException extends RuntimeException {
	private static final long serialVersionUID = -105500584464532362L;
	
	public ParamsCheckException(String errorCode, Throwable message) {
		super(errorCode, message);
	}
	public ParamsCheckException(String message) {
		super(message);
	}
	public ParamsCheckException(String message, String sysId) {
		super(message);
	}
}
