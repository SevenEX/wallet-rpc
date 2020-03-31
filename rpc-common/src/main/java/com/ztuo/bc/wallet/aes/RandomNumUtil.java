package com.ztuo.bc.wallet.aes;

import java.util.UUID;

public class RandomNumUtil {
	
	public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",  
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",  
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",  
        "6", "7", "8", "9"};

	/**
	 * 返回一个32位的随机数，用于返回值参数md5随机校验
	 * 
	 * @return String
	 * */
	public static String getNonceStr() {
		UUID uuid = UUID.randomUUID();
		String random32 = uuid.toString().replace("-", "");
		return random32;
	}
	
	/**
	 * 返回一个32位的随机数，用于返回值参数md5随机校验
	 * 
	 * @return String
	 * */
	public static String getStrByLength(int length) {
		StringBuffer shortBuffer = new StringBuffer();  
		UUID uuid = UUID.randomUUID();
		String random32 = uuid.toString().replace("-", "");
		 for (int i = 0; i < 8; i++) {  
		        String str = random32.substring(i * 2, i * 2 + 2);  
		        int x = Integer.parseInt(str, 16);  
		        shortBuffer.append(chars[x % 36]); 
		    }
		    return shortBuffer.toString();
	}

	public static void main(String[] args) {
		System.out.println(RandomNumUtil.getNonceStr());
	}

	/**
	 * 返回一个16位的随机数，用户分配appid的后缀随机数
	 * 
	 * @return String
	 * */
	public static String getAppidStr() {
		StringBuffer shortBuffer = new StringBuffer();  
	    String uuid = UUID.randomUUID().toString().replace("-", "");  
	    for (int i = 0; i < 16; i++) {  
	        String str = uuid.substring(i * 2, i * 2 + 2);  
	        int x = Integer.parseInt(str, 16);  
	        shortBuffer.append(chars[x % 36]); 
	    }  
	    return shortBuffer.toString();  
	}
}
