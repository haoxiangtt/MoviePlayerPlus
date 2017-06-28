package com.bfy.movieplayerplus.event.base;


import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/28 0028
 * @modifyDate : 2017/6/28 0028
 * @version    : 1.0
 * @desc       : Android UI线程池
 * </pre>
 */
public class AndroidThreadPool extends Platform {

    private final ConcurrentHashMap<String, Reference<Runnable>> delayRunnableMap;
    public AndroidThreadPool(){
        super();
        mDefaultExecutor = new MainThreadExecutor();
        delayRunnableMap = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(Runnable runnable, Executor executor, String sessionId){
        Runnable wrapRunnable = getWrapRunnable(runnable, sessionId);
        executor.execute(wrapRunnable);
    }

    public void executeDelay(Runnable runnable, long delay, String sessionId) {
        Runnable wrapRunnable = getWrapRunnable(runnable, sessionId);
        ((MainThreadExecutor)defaultCallbackExecutor()).executeDelay(wrapRunnable,delay);
    }
    @Override
    public boolean cancel(String sessionId){
        if (TextUtils.isEmpty(sessionId)) {
            return ((MainThreadExecutor) defaultCallbackExecutor()).cancel(null);
        }
        Reference<Runnable> reference = delayRunnableMap.get(sessionId);
        if (reference != null) {
            Runnable runnable = reference.get();
            if (runnable != null) {
                return ((MainThreadExecutor) defaultCallbackExecutor()).cancel(runnable);
            }
        }

        return false;
    }

    @NonNull
    private Runnable getWrapRunnable(final Runnable r, final String sessionId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                  AsyncTask.execute(r);
                r.run();
                if (!TextUtils.isEmpty(sessionId)) {
                    delayRunnableMap.remove(sessionId);
                }
            }
        };
        Reference<Runnable> ref = new SoftReference<Runnable>(runnable);
        if (!TextUtils.isEmpty(sessionId)) {
            delayRunnableMap.put(sessionId, ref);
        }
        return runnable;
    }

    protected static class MainThreadExecutor implements Executor
    {
        private final Handler handler;
        public MainThreadExecutor(){
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(final Runnable r) {
//                Runnable runnable = getWrapRunnable(r);
            handler.post(r);

        }

        public void executeDelay(final Runnable r, final long delay){
//                Runnable delayRunnable = getWrapRunnable(r);
            handler.postDelayed(r, delay);
        }

        public boolean cancel(Runnable runnable){
            if ( runnable != null) {
                handler.removeCallbacks(runnable);
            } else  {
                handler.removeCallbacksAndMessages(null);
            }
            return true;
        }
    }
}
