package com.bfy.movieplayerplus.model.biz;

import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.model.base.RequestAction;
import com.bfy.movieplayerplus.model.base.ValidateFilter;
import com.bfy.movieplayerplus.utils.Constant;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/20
 * @modifyDate : 2017/4/20
 * version     : 1.0
 * @desc       :
 * </pre>
 */

public interface MainBiz {

    @ValidateFilter(type = ValidateFilter.Type.NETWORK)
    @RequestAction(action = "102101",noNetWorkStatus = Constant.ResponseCode.CODE_NO_NETWORK,
            noNetWorkPrompt = "哎呀！网络有问题，请求失败，重试看看。")
    void getMV(EventBuilder.Event ev);

    @ValidateFilter(type = ValidateFilter.Type.NETWORK)
    @RequestAction(action = "102102",noNetWorkStatus = Constant.ResponseCode.CODE_NO_NETWORK,
            noNetWorkPrompt = "哎呀！网络有问题，请求失败，重试看看。")
    void getMVUrl(EventBuilder.Event ev);

}
