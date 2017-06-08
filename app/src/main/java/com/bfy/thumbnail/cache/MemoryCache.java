package com.bfy.thumbnail.cache;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

public class MemoryCache {

	private static MemoryCache cache;
	private static Object lock;
	
	private int mMemorySize;
	private LinkedHashMap<String, Bitmap> mHardMemoryCache;
	// Soft cache for bitmap kicked out of hard cache
	private ConcurrentHashMap<String, SoftReference<Bitmap>> mSoftMemoryCache = 
			new ConcurrentHashMap<String, SoftReference<Bitmap>>(20);

	private MemoryCache(int size) {

		this.mMemorySize = size;
		lock = new Object();
		mHardMemoryCache = new LinkedHashMap<String, Bitmap>(20, 0.75f, true) {
			private static final long serialVersionUID = -7190622541619388252L;
			
			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<String, Bitmap> eldest) {
				int currentSize = getHardCacheSize();
				if(currentSize > MemoryCache.this.mMemorySize){
					SoftReference<Bitmap> srf = new SoftReference<Bitmap>(eldest.getValue());
					mSoftMemoryCache.put(eldest.getKey(), srf);
					return true;
				}else{
					return false;
				}
			}
			
		};
	}

	public static synchronized MemoryCache getInstance(int size) {
		if (cache == null) {
			cache = new MemoryCache(size);
		}
		return cache;
	}

	public boolean putBitmap(String key, Bitmap value) {
		if (key != null && value != null && !value.isRecycled()) {
			synchronized (lock) {
				if (mHardMemoryCache.containsKey(key)) {
					// to do-->
				}
				mHardMemoryCache.put(key, value);
//				computerMemorySize();
			}
			return true;
		}
		return false;
	}

	
	@SuppressWarnings("unused")
	private void computerMemorySize() {
		Iterator<String> it = mHardMemoryCache.keySet().iterator();
		String[] keys = new String[mHardMemoryCache.size()];
		int count = 0;
		while (it.hasNext()) {
			keys[count++] = it.next();
		}
		count = 0;
		int currentSize = getHardCacheSize();
		while (currentSize > mMemorySize) {
			Bitmap bitmap = mHardMemoryCache.get(keys[count]);
			currentSize -= getBitmapSize(bitmap);
			SoftReference<Bitmap> srf = new SoftReference<Bitmap>(bitmap);
			mSoftMemoryCache.put(keys[count], srf);
			mHardMemoryCache.remove(keys[count]);
			count++;
		}

		keys = null;
		it = null;
	}

	public Bitmap getBitmap(String key) {
		if (key == null || "".equals(key)) {
			return null;
		}
		Bitmap bitmap = null;
		synchronized (lock) {
			if (mHardMemoryCache.containsKey(key)) {
				bitmap = mHardMemoryCache.get(key);
				mHardMemoryCache.remove(key);
				mHardMemoryCache.put(key, bitmap);
				return bitmap;
			} else if (mSoftMemoryCache.containsKey(key) && mSoftMemoryCache.get(key) != null) {
				bitmap = mSoftMemoryCache.get(key).get();
				if (bitmap != null && !bitmap.isRecycled()) {
					putBitmap(key, bitmap);
					mSoftMemoryCache.remove(key);
					return bitmap;
				}
			}

			return null;
		}
	}
	public int getMemoryCacheSize(){
		return mHardMemoryCache.size();
	}
	public int getsSoftMemoryCacheSize(){
		return mSoftMemoryCache.size();
	}
	

	private int getHardCacheSize() {
		int size = 0;
		for (Entry<String, Bitmap> entry : mHardMemoryCache.entrySet()) {
			size += getBitmapSize(entry.getValue());
		}
		return size;
	}

	private int getBitmapSize(Bitmap bitmap) {
		if(bitmap == null){ return 0; }
		if(bitmap.getConfig() == Bitmap.Config.ALPHA_8){
			return bitmap.getWidth() * bitmap.getHeight();
		}else if(bitmap.getConfig() == Bitmap.Config.ARGB_4444
				|| bitmap.getConfig() == Bitmap.Config.RGB_565){
			return bitmap.getWidth() * bitmap.getHeight() << 1;
		}else if(bitmap.getConfig() == Bitmap.Config.ARGB_8888){
			return bitmap.getWidth() * bitmap.getHeight() << 2;
		}
		return bitmap.getWidth() * bitmap.getHeight();
	}

	public void refushBitmap(String key, Bitmap bitmap) {
		synchronized (lock) {
			if (mHardMemoryCache.containsKey(key)) {
				mHardMemoryCache.remove(key);
				putBitmap(key, bitmap);
			} else if (mSoftMemoryCache.contains(key)) {
				mSoftMemoryCache.remove(key);
				putBitmap(key, bitmap);
			} else {
				putBitmap(key, bitmap);
			}
		}
	}

	public void recycle() {
		synchronized (lock) {
			for(Entry<String, Bitmap> entry : mHardMemoryCache.entrySet()){
				Bitmap bmp = entry.getValue();
				if(bmp != null && !bmp.isRecycled()){
					SoftReference<Bitmap> sr = new SoftReference<Bitmap>(bmp);
					mSoftMemoryCache.put(entry.getKey(), sr);
				}
			}
//			for( Entry<String, SoftReference<Bitmap>> entry : mSoftMemoryCache.entrySet()){
//				SoftReference<Bitmap> sr = entry.getValue();
//				if(sr != null){
//					Bitmap bmp = sr.get();
//					if(bmp != null && !bmp.isRecycled()){ bmp.recycle(); }
//				}
//			}
			mHardMemoryCache.clear();
//			mSoftMemoryCache.clear();
		}
	}

}
