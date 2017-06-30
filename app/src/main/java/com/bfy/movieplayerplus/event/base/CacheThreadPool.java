package com.bfy.movieplayerplus.event.base;


import android.text.TextUtils;

import com.bfy.movieplayerplus.utils.LogUtils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/28 0028
 * @modifyDate : 2017/6/28 0028
 * @version    : 1.0
 * @desc       : 可缓存的线程池
 * </pre>
 */
public class CacheThreadPool extends Platform{

    protected ConcurrentHashMap<String,List<Reference<Future<?>>>> mThreadMap =
            new ConcurrentHashMap<String,List<Reference<Future<?>>>>();

    public CacheThreadPool(){
        mDefaultExecutor = Executors.newCachedThreadPool();
    }

    public void execute(Runnable runnable, Executor executor, String sessionId){
        if(TextUtils.isEmpty(sessionId)) {
            executor.execute(runnable);
            return;
        }

        List<Reference<Future<?>>> list = mThreadMap.get(sessionId);
        if (list == null) {
            list = new Vector<Reference<Future<?>>>();
            mThreadMap.put(sessionId, list);
        }

        if (executor instanceof ExecutorService) {
//                LogUtils.e(TAG,"ExecutorService class true,add a new task");
            Future<?> task = ((ExecutorService)executor).submit(runnable);
            Reference<Future<?>> ref = new WeakReference<Future<?>>(task);
            list.add(ref);
        } else {
            FutureTask<?> task = new FutureTask<Object>(runnable,null);
            Reference<Future<?>> ref = new WeakReference<Future<?>>(task);
            list.add(ref);
            executor.execute(task);
        }
    }

    @Override
    public boolean cancel(String sessionId) {
        LogUtils.e(TAG,"Begin cancel the thread pool by sessionId : " + sessionId);
        if (TextUtils.isEmpty(sessionId)) {
            LogUtils.e(TAG,"SessionId is empty!");
            return false;
        }
        List<Reference<Future<?>>> list = mThreadMap.get(sessionId);
        int flag = 0;
        if (list != null) {
            LogUtils.e(TAG,"Find " + list.size() + " threads in the list.");
            int i = 0;
            for (Reference<Future<?>> ref : list) {
                if (ref.get() != null) {
                    Future<?> task = ref.get();
                    if (!task.isDone()) {
                        if (task.cancel(true)) {
                            LogUtils.e(TAG, "Cancel a thread successfully!index = " + i);
                        } else {
                            flag++;
                            LogUtils.e(TAG, "Cancel a thread failure!index = " + i);
                        }
                    }
                }
                i++;
            }
        }


        try {
            if (flag == 0) {
                mThreadMap.remove(sessionId);
                return true;
            } else {
                return false;
            }
        } finally {
            LogUtils.e(TAG,"End cancel thread pool!!!!");
        }

    }

    public void clearPoolIgnoreAll(){
        mThreadMap.clear();
    }

    public void clearPool(String sessionId){
        if (!TextUtils.isEmpty(sessionId)) {
            mThreadMap.remove(sessionId);
        }
    }
}
