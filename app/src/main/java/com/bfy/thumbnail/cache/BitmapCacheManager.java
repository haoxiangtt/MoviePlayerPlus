package com.bfy.thumbnail.cache;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

/**
 * 
 * @author zpa
 * 
 */
public class BitmapCacheManager {

	private static final int thumbnailWidth = 320;
	private static final int thumbnailHeight = 240;
	
	private static final boolean DEBUG = true;
	private static final String TAG = "GetBitmapThrunbal";
	
	public static final int BITMAP_SIZE = 1024 * 100;
	public static final int ONE_MB = 1024 * 1024;
	private static final Object lock = new Object();
	private static final HashMap<String, ArrayList<BitmapCallBack>> callbackCache = 
			new HashMap<String, ArrayList<BitmapCallBack>>();
	private static BitmapCacheManager mBCManager = null;
	
	private Handler handler;
	private MemoryCache mMemoryCache;
	private ExecutorService executorService;
	
	

	private BitmapCacheManager(Context context) {
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = ONE_MB * memClass >> 3;
		mMemoryCache = MemoryCache.getInstance(cacheSize);
		executorService = ThreadPool.getInstance().executorService;
		handler = new Handler();

	}
	
	
	public static BitmapCacheManager getInstance(Context context){
		if(mBCManager == null){
			mBCManager = new BitmapCacheManager(context);
		}
		return mBCManager;
	}

	

	public void clear() {
		synchronized (lock) {
			((ThreadPoolExecutor) executorService).getQueue().clear();
			callbackCache.clear();
		}
		mMemoryCache.recycle();
	}



	public Bitmap getBitmap(final String key, BitmapCallBack callback) {
		if (key == null || "".equals(key)) {
			return null;
		}
		final Bitmap bitmap = getBitmapFromCache(key);
		if (bitmap != null && !bitmap.isRecycled()) {
			synchronized (lock) {
				if (callbackCache.containsKey(key)) {
					callbackCache.remove(key);
				}
			}
			return bitmap;
		} else {
			synchronized (lock) {
				if (callbackCache.containsKey(key)) {
					if (!callbackCache.get(key).contains(callback)) {
						callbackCache.get(key).add(callback);
					}
					return null;
				} else {
					ArrayList<BitmapCallBack> calls = new ArrayList<BitmapCallBack>();
					calls.add(callback);
					callbackCache.put(key, calls);
				}
			}
			executorService.execute(new Runnable() {
				@Override
				public void run() {

					final Bitmap map = getBitmapFromKey(key);
				
					handlerBackBitmap(key, map);
				}

			});

		}
		return null;
	}
	
	private Bitmap getBitmapFromCache(String key) {
		Bitmap bitmap = mMemoryCache.getBitmap(key);
		return bitmap;
	}

	private Bitmap getBitmapFromKey(String key) {
		Bitmap bm = getBitmapFromCache(key);
		if (bm == null) {
			bm = compBitmap(key);
		}
		return bm;
	}
	
	
	private Bitmap compBitmap(String path) {
	
		byte[] source=getThumnailByKey(path);
		if(source==null){
			//changed by haoxiangtt 2014.4.2 for handle Thumbnail
			/*try {
				createThumbnail(path);
			} catch (IOException e) {
				Log.w(TAG, "create chumbnail fail");
			}
			source=getThumnailByPath(db,path);
			if(source == null){
				return null;
			}*/
			//end by haoxiangtt
			return null;
		}
		
		ByteArrayInputStream isBm = new ByteArrayInputStream(source);
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//newOpts.inJustDecodeBounds = true;
		newOpts.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return bitmap;
	}


	private byte[] getThumnailByKey(String key) {
		byte[] thumbByte;
		try {
			thumbByte = LibVLC.getInstance().getThumbnail(key, thumbnailWidth, thumbnailHeight);
			return thumbByte ;
		} catch (LibVlcException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	private void handlerBackBitmap(final String key, final Bitmap bitmap) {
		if (key == null) {
			return;
		}
	
		synchronized (lock) {
			if (bitmap != null)
				mMemoryCache.putBitmap(key, bitmap);
			final ArrayList<BitmapCallBack> calls = callbackCache.get(key);
			if(calls == null){
				return;
			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					for (BitmapCallBack call : calls) {
						if (call != null) {
							call.callBack(key, bitmap);
						}
					}
					callbackCache.remove(key);
				}
	
			});
		}
	}



	// 质量 压缩
	/*private ByteArrayOutputStream compressImage(Bitmap image) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, baos);
		int options = 100;
		while (baos.toByteArray().length > BITMAP_SIZE) {
			baos.reset();
			image.compress(Bitmap.CompressFormat.PNG, options, baos);
			options -= 10;
		}
//		BitmapFactory.Options newOpts = new BitmapFactory.Options();
//		newOpts.inSampleSize =1;
//		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//		image = BitmapFactory.decodeStream(isBm, null, newOpts);
//
//		try {
//			baos.close();
//			isBm.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		return baos;
	}*/
	
	//add by haoxiangtt 2014.4.2 for Thumbnail
	/*private int createThumbnail(String filePath,FileDatabaseHelper db) throws IOException {
		int count = 0;
		if (filePath != null && db != null) {
			File file = new File(filePath);
			Bitmap bitmap = null;
			Bitmap bitmap_thumbnail = null;
			if(file == null || !file.exists()){
				return 0;
			}
			if (FileUtil.static_isPhoto(filePath)) {

				// OutOfMemoryError, ignore file size >15MB
				if (file.length() > ONE_MB * 15 || file.length() <= 0) {
					return 0;
				}

				ThumbnailCursor thumbnailCursor = null;
				try {
					thumbnailCursor = db.VerifiedThumbnailByPath(filePath);
					if (thumbnailCursor != null
							&& thumbnailCursor.moveToFirst()) {
						if (thumbnailCursor.getCount() > 0)
							return 0;
					}
				} finally {
					if (thumbnailCursor != null)
						thumbnailCursor.close();
				}

				bitmap = ThumbnailUtils.createImageThumbnail(filePath,
						Images.Thumbnails.MINI_KIND);
				
			}else if(FileUtil.static_isApk(filePath) ){
				if ( file.length() <= 0 ) {
					return 0;
				}

				ThumbnailCursor thumbnailCursor = null;
				try {
					thumbnailCursor = db.VerifiedThumbnailByPath(filePath);
					if (thumbnailCursor != null
							&& thumbnailCursor.moveToFirst()) {
						if (thumbnailCursor.getCount() > 0)
							return 0;
					}
				} finally {
					if (thumbnailCursor != null)
						thumbnailCursor.close();
				}
				bitmap = FileUtil.static_getApkIcon(mContext, filePath);
				
			}
			
			if(bitmap != null){
				
				bitmap_thumbnail = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight,Config.ARGB_8888);
//				ThumbnailUtils.extractThumbnail(bitmap,
//				bitmapWidth, bitmapHeight);
				BitmapDrawable drawable = new BitmapDrawable(mContext.getResources() , bitmap);
				drawable.setBounds(bitmapOffsetX, bitmapOffsetY,
						bitmapOffsetX + bitmapWidth, bitmapOffsetY + bitmapHeight);
				Canvas canvas = new Canvas(bitmap_thumbnail);
				drawable.draw(canvas);
				
				if(bitmap_thumbnail != null){
					ByteArrayOutputStream os = compressImage(bitmap_thumbnail);
					//bitmap_thumbnail.compress(Bitmap.CompressFormat.PNG,100, os);
					if (db != null) {
						db.insertThumbnail(filePath, os.toByteArray());
						count++;
					}
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(!bitmap_thumbnail.isRecycled()){
						bitmap_thumbnail.recycle();
					}
				}

				if (!bitmap.isRecycled())
					bitmap.recycle();
			}
			
			
			
		}
		return count;
	}*/
	
	//end by haoxiangtt
	
	
	
}








