/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bfy.movieplayerplus.utils;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
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
 * @createDate : 2017/3/28 0023
 * @modifyDate : 2017/3/28 0023
 * @version    : 1.1
 * @desc       : simple thread pool manager
 * </pre>
 */
public abstract class Platform
{
    private static final String TAG = "threadpool";
    private static final boolean DEBUG = true;
    private static final Platform PLATFORM = findPlatform(false);
    private static final Platform ANDROID = findPlatform(true);

    public static final int TYPE_CACHE_THREAD_POOL = 0;
    public static final int TYPE_UI_THREAD_POOL = 1;

    protected Executor mDefaultExecutor;

    protected ConcurrentHashMap<String,List<WeakReference<Future<?>>>> mThreadMap =
            new ConcurrentHashMap<String,List<WeakReference<Future<?>>>>();

    public static <T extends Platform> T getInstance(int type)
    {
//        L.e(PLATFORM.getClass().toString());
        if (type == TYPE_UI_THREAD_POOL) {
            return (T)ANDROID;
        } else if (type == TYPE_CACHE_THREAD_POOL) {
            return (T)PLATFORM;
        } else {
            return null;
        }
    }

    private static Platform findPlatform(boolean isUIThread) {
        try
        {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                if(isUIThread) {
                    return new Android();
                } else {
                    return new CacheThreadPool();
                }
            }
        } catch (ClassNotFoundException ignored) {}
        return new CacheThreadPool();
    }



    public Executor defaultCallbackExecutor(){
        return mDefaultExecutor;
    }


    public void execute(Runnable runnable){
        execute(runnable,defaultCallbackExecutor());
    }


    public void execute(Runnable runnable, Executor executor){
        execute(runnable,executor,null);
    }

    public void execute(Runnable runnable, String sessionId){
        execute(runnable, defaultCallbackExecutor(), sessionId);
    }

    public abstract void execute(Runnable runnable, Executor executor, String sessionId);

    public static class CacheThreadPool extends Platform{

        public CacheThreadPool(){
            mDefaultExecutor = Executors.newCachedThreadPool();
        }

        public void execute(Runnable runnable, Executor executor, String sessionId){
            if(TextUtils.isEmpty(sessionId)) {
                executor.execute(runnable);
                return;
            }

            List<WeakReference<Future<?>>> list = mThreadMap.get(sessionId);
            if (list == null) {
                list = new Vector<WeakReference<Future<?>>>();
                mThreadMap.put(sessionId, list);
            }

            if (executor instanceof ExecutorService) {
//                LogUtils.e(TAG,"ExecutorService class true,add a new task");
                Future<?> task = ((ExecutorService)executor).submit(runnable);
                WeakReference<Future<?>> ref = new WeakReference<Future<?>>(task);
                list.add(ref);
            } else {
                FutureTask<?> task = new FutureTask<Object>(runnable,null);
                WeakReference<Future<?>> ref = new WeakReference<Future<?>>(task);
                list.add(ref);
                executor.execute(task);
            }
        }

        public boolean cancel(String sessionId) {
            if (DEBUG) {
                LogUtils.e(TAG,"Begin cancel the thread pool by sessionId : " + sessionId);
            }
            if (TextUtils.isEmpty(sessionId)) {
                if (DEBUG) {
                    LogUtils.e(TAG,"SessionId is empty!");
                }
                return false;
            }
            List<WeakReference<Future<?>>> list = mThreadMap.get(sessionId);
            int flag = 0;
            if (list != null) {
                if (DEBUG) {
                    LogUtils.e(TAG,"Find " + list.size() + " threads in the list.");
                }
                int i = 0;
                for (WeakReference<Future<?>> ref : list) {
                    if (ref.get() != null) {
                        Future<?> task = ref.get();
                        if (!task.isDone()) {
                            if (task.cancel(true)) {
                                if (DEBUG) {
                                    LogUtils.e(TAG, "Cancel a thread successfully!index = " + i);
                                }
                            } else {
                                flag++;
                                if (DEBUG) {
                                    LogUtils.e(TAG, "Cancel a thread failure!index = " + i);
                                }
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
                if (DEBUG) {
                    LogUtils.e(TAG,"End cancel thread pool!!!!");
                }
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


    public static class Android extends Platform {

        public Android(){
            super();
            mDefaultExecutor = new MainThreadExecutor();
        }

        @Override
        public void execute(Runnable runnable, Executor executor, String sessionId){
            executor.execute(runnable);
        }

        public void executeDelay(Runnable runnable, long delay) {
            ((MainThreadExecutor)defaultCallbackExecutor()).executeDelay(runnable,delay);
        }

        public void cancel(Runnable runnable){
            ((MainThreadExecutor)defaultCallbackExecutor()).cancel(runnable);
        }

        protected static class MainThreadExecutor implements Executor
        {
            private final Handler handler;
            private final ConcurrentHashMap<String, Reference<Runnable>> delayRunnableMap;
            public MainThreadExecutor(){
                handler = new Handler(Looper.getMainLooper());
                delayRunnableMap = new ConcurrentHashMap<>();
            }

            @Override
            public void execute(final Runnable r) {
                Runnable runnable = getRunnable(r);
                handler.post(runnable);

            }

            public void executeDelay(final Runnable r, final long delay){
                Runnable delayRunnable = getRunnable(r);
                handler.postDelayed(delayRunnable, delay);
            }

            @NonNull
            private Runnable getRunnable(final Runnable r) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
//                        AsyncTask.execute(r);
                        r.run();
                        delayRunnableMap.remove(Integer.valueOf(r.hashCode()).toString());
                    }
                };
                Reference<Runnable> ref = new SoftReference<Runnable>(runnable);
                delayRunnableMap.put(Integer.valueOf(r.hashCode()).toString(), ref);
                return runnable;
            }

            public void cancel(Runnable runnable){
                if ( runnable != null) {
                    String key = Integer.valueOf(runnable.hashCode()).toString();
                    Runnable delayRunnable = delayRunnableMap
                        .get(key).get();
                    if (delayRunnable != null) {
                        handler.removeCallbacks(delayRunnable);
                        delayRunnableMap.remove(key);
                    }
                } else  {
                    handler.removeCallbacksAndMessages(null);
                }
            }
        }
    }


}
