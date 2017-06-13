package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/19
 * @modifyDate : 2017/4/19
 * @version     : 1.0
 * @desc       : event对象不能重复发送多次，如果这样做了，就会抛出此异常
 * </pre>
 */

public class EventAlreadySentException extends RuntimeException {

    public EventAlreadySentException(String str){
        super(str);
    }

}
