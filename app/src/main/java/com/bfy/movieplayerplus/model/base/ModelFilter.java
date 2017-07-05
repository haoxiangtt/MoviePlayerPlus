package com.bfy.movieplayerplus.model.base;

import android.content.Context;

import org.json.JSONException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.bfy.movieplayerplus.event.EventJsonObject;
import com.bfy.movieplayerplus.event.base.EventBuilder.Event;
import com.bfy.movieplayerplus.utils.NetworkState;


/**
 * 业务模型的过滤器，用于验证某些特殊操作
 * Created by ouyangjinfu on 2016/5/27.
 */
public class ModelFilter implements InvocationHandler {

//    private static final String KEY_CODE = "mamimamihong";
    private Object realModel;
    private Context mContext;
    private NetworkState mNetWorkState;

    private ModelFilter(Object obj, Context context){
        realModel = obj;
        mContext = context;
        mNetWorkState = NetworkState.getInstance(context);
    }

    public static ModelFilter obtain(Object obj, Context context) {
        return new ModelFilter(obj,context);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        //do before....
        if(!doBefore(method,args)){
            return null;
        }

        Object result = method.invoke(realModel,args);
        //do after...
        if(!doAfter()){
            return null;
        }

        return result;
    }

    /**
     * 执行指定方法之前的任务
     * @param method 指定方法的反射
     * @param args 指定方法的参数
     * @return true表示验证通过，false表示拦截，方法将不再执行下去
     */
    private boolean doBefore(Method method, Object[] args){
        ValidateFilter an = method.getAnnotation(ValidateFilter.class);

        if(an != null){
            int type = an.type();
            //可扩展
            /*if(type % 2 == 1){//验证token
                if(!validateToken(method,args)){
                    return false;
                }
            }*/

            if((type) % 2 == 1){//验证网络
                if(!validateNetWork(method,args)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 执行方法完成之后的任务
     * @return true表示通过，false表示拦截返回值
     */
    private boolean doAfter(){
        return true;
    }

    private boolean validateNetWork(Method method, Object[] args) {
        if(!isConnected()){
            for(Object arg : args){
                if(arg == null){ continue; }
                if(arg instanceof Event){
                    String msg = "";
                    String action = "";
                    String status = "";
                    RequestAction requestAction = method.getAnnotation(RequestAction.class);
                    if(requestAction != null){

                        if(requestAction.noNetWorkPrompt() != null) {
                            msg = requestAction.noNetWorkPrompt();//mContext.getString(requestAction.noNetWorkPrompt());
                        }
                        action = requestAction.action();
                        status = requestAction.noNetWorkStatus();
                    }
                    // by ouyangjinfu 已经实现
                    Event ev = (Event)arg;
                    EventJsonObject ejb = new EventJsonObject();
                    try {
                        ejb.putOpt(BaseModel.KEY_RESULT_CODE, status);
                        ejb.putOpt(BaseModel.KEY_DESC, msg);
                    } catch (JSONException e) {
                    }finally {
                        ev.responseData = ejb;
                    }
                    ev.performCallback(ev);
                    break;
                }
            }
            return false;
        }
        return true;
    }


    /**
     * 检测网络
     * @return
     */
    protected boolean isConnected(){
        return mNetWorkState.isConneted(mContext);
    }

}
