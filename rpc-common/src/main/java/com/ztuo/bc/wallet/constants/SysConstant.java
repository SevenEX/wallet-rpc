package com.ztuo.bc.wallet.constants;

/**
 * 系统常量
 *
 * @author GuoShuai
 * @date 2017年12月18日
 */
public class SysConstant {
    /**
     * session常量
     */
    public static final String SESSION_ADMIN = "ADMIN_MEMBER";

    public static final String SESSION_MEMBER = "API_MEMBER";

    /**
     * 验证码
     */
    public static final String PHONE_WITHDRAW_MONEY_CODE_PREFIX = "PHONE_WITHDRAW_MONEY_CODE_PREFIX_";

    public static final String PHONE_trade_CODE_PREFIX = "PHONE_trade_CODE_PREFIX_";

    public static final String PHONE_REG_CODE_PREFIX = "PHONE_REG_CODE_";

    public static final String PHONE_RESET_TRANS_CODE_PREFIX = "PHONE_RESET_TRANS_CODE_";

    public static final String PHONE_BIND_CODE_PREFIX = "PHONE_BIND_CODE_";

    public static final String PHONE_UPDATE_PASSWORD_PREFIX = "PHONE_UPDATE_PASSWORD_";

    public static final String PHONE_ADD_ADDRESS_PREFIX = "PHONE_ADD_ADDRESS_";

    public static final String EMAIL_BIND_CODE_PREFIX = "EMAIL_BIND_CODE_";

    public static final String API_BIND_CODE_PREFIX = "API_BIND_CODE_PREFIX_";
    /**
     * 解绑邮箱验证码
     */
    public static final String EMAIL_UNTIE_CODE_PREFIX = "EMAIL_UNTIE_CODE_";
    /**
     * 换绑邮箱验证码
     */
    public static final String EMAIL_UPDATE_CODE_PREFIX = "EMAIL_UPDATE_CODE_";

    public static final String ADD_ADDRESS_CODE_PREFIX = "ADD_ADDRESS_CODE_";
    public static final String RESET_PASSWORD_CODE_PREFIX = "RESET_PASSWORD_CODE_";
    public static final String PHONE_CHANGE_CODE_PREFIX = "PHONE_CHANGE_CODE_";

    public static final String RESET_GOOGLE_CODE_PREFIX = "RESET_PASSWORD_CODE_";
    public static final String ADMIN_LOGIN_PHONE_PREFIX = "ADMIN_LOGIN_PHONE_PREFIX_";

    public static final String ADMIN_COIN_REVISE_PHONE_PREFIX = "ADMIN_COIN_REVISE_PHONE_PREFIX_";
    public static final String ADMIN_COIN_TRANSFER_COLD_PREFIX = "ADMIN_COIN_TRANSFER_COLD_PREFIX_";
    public static final String ADMIN_EXCHANGE_COIN_SET_PREFIX = "ADMIN_EXCHANGE_COIN_SET_PREFIX_";

    /** 防攻击验证 */
    public static final String ANTI_ATTACK_ = "ANTI_ATTACK_";
    /**
     * 防止注册机器人
     */
    public static final String ANTI_ROBOT_REGISTER ="ANTI_ROBOT_REGISTER_";
    /**
     * 60亿BHB累计(过期时间为15分钟)
     */
    public static final String BHB_AMOUNT="BHB_AMOUNT";
    public static final int BHB_AMOUNT_EXPIRE_TIME=900;


    /**
     * 公告页缓存
     */
    public static final String NOTICE_DETAIL = "notice_detail_";
    public static final int NOTICE_DETAIL_EXPIRE_TIME=300;

    /**
     * 帮助页缓存(首页)
     */
    public static final String SYS_HELP = "SYS_HELP";
    public static final int SYS_HELP_EXPIRE_TIME=300;


    /**
     * 帮助页缓存(类别页)
     */
    public static final String SYS_HELP_CATE = "SYS_HELP_CATE_";
    public static final int SYS_HELP_CATE_EXPIRE_TIME=300;

    /**
     * 帮助页缓存(详情页)
     */
    public static final String SYS_HELP_DETAIL = "SYS_HELP_DETAIL_";
    public static final int SYS_HELP_DETAIL_EXPIRE_TIME=300;

    /**
     * 帮助页缓存(该分类置顶文章)
     */
    public static final String SYS_HELP_TOP = "SYS_HELP_TOP_";
    public static final int SYS_HELP_TOP_EXPIRE_TIME=300;


    //字典表数据缓存
    public static final String DATA_DICTIONARY_BOUND_KEY= "data_dictionary_bound_key_";
    public static final int DATA_DICTIONARY_BOUND_EXPIRE_TIME= 604800;

    //盘口数据
    public static final String EXCHANGE_INIT_PLATE_SYMBOL_KEY="EXCHANGE_INIT_PLATE_SYMBOL_KEY_";
    public static final int EXCHANGE_INIT_PLATE_SYMBOL_EXPIRE_TIME= 18000;

    /**
     * 盘口数据所有交易对
     */
    public static final String EXCHANGE_INIT_PLATE_ALL_SYMBOLS = "EXCHANGE_INIT_PLATE_ALL_SYMBOLS";



    /**
     * 用户币币交易订单时间限制
     */
    public static final String USER_ADD_EXCHANGE_ORDER_TIME_LIMIT= "USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_";
    public static final int USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_EXPIRE_TIME= 20;

    /**
     * 空投锁
     */
    public static final String HANDLE_AIRDROP_LOCK="HANDLE_AIRDROP_LOCK_";
    /**
     * 登录锁，连续账号密码错误时启用
     */
    public static final String LOGIN_LOCK="LOGIN_LOCK_";
    /**
     * 实名用户一级推荐实名用户赠送积分
     */
    public static final String INTEGRATION_GIVING_ONE_INVITE = "integration_giving_one_invite";
    /**
     * 实名用户二级推荐实名用户赠送积分
     */
    public static final String INTEGRATION_GIVING_TWO_INVITE = "integration_giving_two_invite";
    /**
     * 法币交易人民币对积分比例
     */
    public static final String INTEGRATION_GIVING_OTC_BUY_CNY_RATE = "integration_giving_otc_buy_cny_rate";
    /**
     * 币币充值USDT对积分比例
     */
    public static final String INTEGRATION_GIVING_EXCHANGE_RECHARGE_USDT_RATE = "integration_giving_exchange_recharge_usdt_rate";
    /**
     * 用户每日提币笔数
     */
    public static final String CUSTOMER_DAY_WITHDRAW_TOTAL_COUNT = "CUSTOMER_DAY_WITHDRAW_TOTAL_COUNT_";
    /**
     * 用户每日提币数量折合USDT
     */
    public static final String CUSTOMER_DAY_WITHDRAW_COVER_USD_AMOUNT ="CUSTOMER_DAY_WITHDRAW_COVER_USD_AMOUNT_";
    /**
     * 等级缓存
     */
    public static final String CUSTOMER_INTEGRATION_GRADE="CUSTOMER_INTEGRATION_GRADE_";

    /**
     * 默认交易对儿缓存
     */
    public static final String DEFAULT_SYMBOL = "DEFAULT_SYMBOL";




}
