package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/14 0014
 * @modifyDate : 2017/4/14 0014
 * @version    : 1.0
 * @desc       : 事件接收器
 * </pre>
 */

public interface EventReceiver {
    void onReceive(EventBuilder.Event event);
}
