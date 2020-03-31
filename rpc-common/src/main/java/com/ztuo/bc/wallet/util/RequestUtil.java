package com.ztuo.bc.wallet.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestUtil {
    public static String remoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    public static Map<String,String> getAreaDetail(String apiUrl,String apiKey,String apiValue){
        JSONObject jsonObject=JSONObject.fromObject(HttpClientUtil.get(apiUrl,apiKey,apiValue));
        log.info("getAreaDetail="+jsonObject.toString());
        Map<String,String> resultMap= (Map<String, String>) JSONObject.toBean(jsonObject,HashMap.class);
        return resultMap;
    }

}
