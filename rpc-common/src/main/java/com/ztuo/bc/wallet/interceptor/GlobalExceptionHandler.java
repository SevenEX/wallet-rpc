package com.ztuo.bc.wallet.interceptor;

import com.ztuo.bc.wallet.util.CodeMsg;
import com.ztuo.bc.wallet.util.MessageResult;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author y
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public MessageResult<String> exceptionHandler(Exception e){
        //绑定异常是需要明确提示给用户的
        if(e instanceof BindException){
            BindException exception=(BindException) e;
            List<ObjectError> errors=exception.getAllErrors();
            //获取自错误信息
            String msg=errors.get(0).getDefaultMessage();
            return MessageResult.error(CodeMsg.SERVER_BIND_ERROR.fillArgs(msg).getMsg());
            //将具体错误信息设置到CodeMsg中返回
        }
        if(e instanceof SignCheckException){
            SignCheckException exception=(SignCheckException) e;
            //获取自错误信息
            String msg=exception.getMessage();
            return MessageResult.error(CodeMsg.SERVER_SIGN_ERROR.fillArgs(msg).getMsg());
            //将具体错误信息设置到CodeMsg中返回
        }
        if(e instanceof ParamsCheckException){
            ParamsCheckException exception=(ParamsCheckException) e;
            //获取自错误信息
            String msg=exception.getMessage();
            return MessageResult.error(CodeMsg.PARAM_CHECK_ERROR.fillArgs(msg).getMsg());
            //将具体错误信息设置到CodeMsg中返回
        }
// 其余异常简单返回为服务器异常
        return MessageResult.error(e.getMessage());

    }
}