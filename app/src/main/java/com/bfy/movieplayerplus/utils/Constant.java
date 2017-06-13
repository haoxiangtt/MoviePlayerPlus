package com.bfy.movieplayerplus.utils;

import java.util.UUID;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/6/12 0012
 * @modifyDate : 2017/6/12 0012
 * @version    : 1.0
 * @desc       : 静态常量池
 * </pre>
 */

public class Constant {

    public static final String KUGOU_MV_SEARCH_URL = "http://mvsearch.kugou.com/mv_search";

    public static final String KUGOU_MV_REAL_URL = "http://trackermv.kugou.com/interface/index/";

    public static final String MAIN_MODEL = "main_model";

    public static final int EVENT_TYPE_MODEL = 0;

    public static final int EVENT_TYPE_CONTEXT = 1;

    /**
     * 随机生成32位字符
     *
     * @return
     * @author licq 2014-4-28
     */
    public static String generateNonce32() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    /**
     * 响应码
     */
    public static final class ResponseCode{

        /**
         * 请求成功
         */
        public static final String CODE_SUCCESSFULLY = "200";

        /**
         * 参数无效
         */
        public static final String CODE_PARAM_INVALID = "102000";

        /**
         * 用户取消认证
         */
        public static final String CODE_USER_CANCEL = "102121";

        /**
         * 数据解析异常
         */
        public static final String CODE_DATA_ERROR = "102223";

        /**
         * 无网络状态
         */
        public static final String CODE_NO_NETWORK = "102501";

        /**
         * 业务未注册
         */
        public static final String CODE_MODEL_UNREGIST = "102505";
    }

}
