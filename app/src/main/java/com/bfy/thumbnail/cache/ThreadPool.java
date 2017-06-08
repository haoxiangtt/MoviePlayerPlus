package com.bfy.thumbnail.cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
	private static ThreadPool mThreadPool;
	private int num = 3;
	public ExecutorService executorService;
	
	private ThreadPool() {
		executorService = Executors.newFixedThreadPool(num);
	}

	public static synchronized ThreadPool getInstance() {
		if (mThreadPool == null) {
			mThreadPool = new ThreadPool();
		}
		return mThreadPool;
	}

}
