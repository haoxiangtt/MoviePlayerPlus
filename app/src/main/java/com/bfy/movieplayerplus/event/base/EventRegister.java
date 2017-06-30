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
 * @desc       : 注册者(注册器)，通常一个注册者管理多个接收者，需要通过一个标识来查找到对应的接收者；
 *              有一种特殊情况就是如果一个注册者只管理一个接收者，那么EventRegister和EventReceiver接口可以在同一个类中实现
 * </pre>
 */

public interface EventRegister {
    /**
     * 获取接收者
     * @param key 注册者通过这个key来获取对应的接收者，这里的key对应event中的receiverKey参数
     * @return
     */
    EventReceiver getReceiver(String key);
}
