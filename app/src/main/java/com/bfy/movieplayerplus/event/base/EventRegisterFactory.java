package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/2 0002
 * @modifyDate : 2017/6/2 0002
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public interface EventRegisterFactory {

    EventRegisterFactory registRegister(int type, EventRegister register);

    EventRegister getRegister(int type);

    EventRegisterFactory registDispatcher(int type, EventDispatcher dispatcher);

    EventDispatcher getDispatcher(int type);
}
