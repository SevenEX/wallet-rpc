package com.ztuo.bc.wallet.component;

import java.util.ArrayList;
import java.util.List;

public class SysCode {

	// 文本
	public static final String TEXT = "text";

	// 值
	public static final String VALUE = "value";

	public static final int TOKEN_EXPIRES_HOUR = 3600;

	public static final String accessToken = "Authorization";

	public static final String checkTypeKey = "checkType";

	// 短信验证码 验证
	public static final String codeCheckType = "code";

	/**
	 * 存放app 语言的header字段
	 */
	public static final String LANGUAGE = "language";

	/** 
	 * @Description: 删除状态
	 * @ClassName: IS_DELETE 
	 * @author nz
	 * @date 2018年4月16日 上午1:20:16  
	 */
	public static interface IS_DELETE {
		String YES = "1";
		String NO = "0";
	}
	
	/** 
	 * @Description: 是否启用
	 * @ClassName: IS_ENABLE 
	 * @author nz
	 * @date 2018年4月16日 上午1:20:16  
	 */
	public static interface IS_ENABLE {
		String YES = "1";
		String NO = "0";
	}
	   
    /** 
     * @Description: 是否冻结
     * @ClassName: IS_FREEZE 
     * @author nz
     * @date 2018年4月16日 上午1:20:16  
     */
    public static interface IS_FREEZE {
        String YES = "1";
        String NO = "0";
    }
	
	/** 
	 * @Description: 缓存签名前缀
	 * @ClassName: RedisStr 
	 * @author nz
	 * @date 2018年5月6日 上午11:47:40  
	 */
	public static interface RedisStr {
		/** SYSID_PRIVATE:钱包服务签名私钥 */
		String SYSID_PRIVATE = "sys_id_private:";
		/** SYSID_PUBLIC:钱包服务验签公钥 */
		String SYSID_PUBLIC = "sys_id_pubkey:";
		/** ACCOUNT_ADDRESS:账户地址 */
		String ACCOUNT_ADDRESS = "account_address:";

		/** ACCOUNT_ADDRESS:BTC账户地址 */
		String ACCOUNT_BTC_ADDRESS = "account_btc_address:";
		/** NONCE_STR:随机数 */
		String NONCE_STR = "NONCE:";
	    /**存取的最大数**/
        int maxNum=3;
	}
	
	/** 
	 * @Description: 交易类型
	 * @ClassName: PayType 
	 * @author nz
	 * @date 2018年5月6日 上午11:47:59  
	 */
	public static interface PayType {
		/** CHARGE_MONEY:充币  fromAddress为第三方账号，toAddress为本系统地址*/
		String CHARGE_MONEY = "01";
		
		/** WITHDRAW_MONEY:提币 fromAddress 为本系统地址 toAddress为第三方地址 */
		String WITHDRAW_MONEY = "02";
		
		/** TRANSFER_MONEY:平台内转账  资金管理系统的交易请求*/
		String TRANSFER_MONEY = "03";
	}
	
	/** 
	 * @Description: 是否大钱包
	 * @ClassName: MasterFlg 
	 * @author nz
	 * @date 2018年5月6日 上午11:48:07  
	 */
	public static interface MasterFlg {
		/** YES:TODO(用一句话描述这个变量表示什么) */
		String YES = "1";
		String NO = "0";
	}
	
	public static interface RedisNum {
		String TXID_NUM = "TXID_NUM:";

		String STATUS_NUM = "STATUS_NUM:";
	}

	public static List<String> currencyList = new ArrayList<>();

   /** 
     * @Description: 币种
     * @ClassName: CURRENCY 
     * @author nz
     * @date 2018年4月16日 上午1:20:16  
     */
    public static interface CURRENCY {
        String HNB = "HNB";
        String HGS = "HGS";
        String ETH = "ETH";
	    String BTC = "BTC";
	    String USDT = "USDT";
	    String PC = "PC";
	   String GPU = "GPU";
    }
    
    /** 
     * @Description: 交易状态
     * @ClassName: Transf_status 
     * @author zhangniu
     * @date 2018年9月15日 上午4:02:50  
     */
    public static interface TRANSF_STATUS {
        /** CREATING:创建 */
        String CREATING = "01";
        /** DEALING:处理中 */
        String DEALING = "02";
        /** SUCCESS:交易成功 */
        String SUCCESS = "03";
        /** FAILED:交易失败 */
        String FAILED = "04";
    }
}
