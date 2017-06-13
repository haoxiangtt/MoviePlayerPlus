package com.bfy.movieplayerplus.event;

import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.EventDispatcher;
import com.bfy.movieplayerplus.utils.Platform;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/19
 * @modifyDate : 2017/4/19
 * @version    : 1.0
 * @desc       : Context事件分发器
 * </pre>
 */

public class ContextEventDispatcher implements EventDispatcher {

    public ContextEventDispatcher(){

    }

    @Override
    public void dispatch(final EventBuilder.Event event) {
        Platform.getInstance(Platform.TYPE_UI_THREAD_POOL).execute(new Runnable() {
            @Override
            public void run() {
                ContextReceiver.getReceiverInstance().onReceive(event);
            }
        });
    }

}
