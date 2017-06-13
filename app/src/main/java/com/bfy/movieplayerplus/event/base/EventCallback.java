package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * version     : 1.0
 * @desc       : 事件回调接口
 * </pre>
 */

public interface EventCallback {
    void call(EventBuilder.Event event);
}
