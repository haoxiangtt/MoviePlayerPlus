package com.bfy.view;

import java.util.ArrayList;
import java.util.Locale;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.MediaList;
import org.videolan.vlc.util.WeakHandler;

import com.bfy.movieplayerplus.R;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class PlayerView extends FrameLayout implements IVideoPlayer,MediaPlayerController {

	private static final String TAG = "PlayerView";

	

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private int mCurrentSize = SURFACE_BEST_FIT;
	private static final int NET_WORK_CACHE_TIME = 20000;

	private LibVLC mLibVLC;

	// Whether fallback from HW acceleration to SW decoding was done.
	private boolean mDisabledHardwareAcceleration = false;
	private int mPreviousHardwareAccelerationMode;

	private SurfaceView mSurface;
	//private SurfaceView mSubtitlesSurface;

	private SurfaceHolder mSurfaceHolder;
	//private SurfaceHolder mSubtitlesSurfaceHolder;

	private FrameLayout mSurfaceFrame;

	// size of the video
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;

	private Handler mHandler;
	private OnChangeListener mOnChangeListener;
	private boolean mCanSeek = false;

	private String mCurrentPlayUrl = "";
	private int mCurrentPlayIndex = 0;

	public PlayerView(Context context) {
		super(context);
		initView();
	}

	public PlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	/*public PlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}*/

	private void initView() {
		try {
			mLibVLC = LibVLC.getExistingInstance();
			if (mLibVLC == null) {
				mLibVLC = LibVLC.getInstance();
			}
		} catch (LibVlcException e) {
			throw new RuntimeException("PlayerView Init Failed");
		}

		LayoutInflater.from(getContext()).inflate(R.layout.view_player, this);
		mHandler = new Handler();

		//video view
		mSurface = (SurfaceView) findViewById(R.id.player_surface);
		mSurfaceHolder = mSurface.getHolder();
		mSurfaceHolder.addCallback(mSurfaceCallback);
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);

		//Subtitles view
		//mSubtitlesSurface = (SurfaceView) findViewById(R.id.subtitles_surface);
		//mSubtitlesSurfaceHolder = mSubtitlesSurface.getHolder();
		//mSubtitlesSurface.setZOrderMediaOverlay(true);
		//mSubtitlesSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
		//mSubtitlesSurfaceHolder.addCallback(mSubtitlesSurfaceCallback);

		mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);
	}

	private void handleHardwareAccelerationError() {
		mLibVLC.stop();
		mDisabledHardwareAcceleration = true;
		mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
		mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
		start();
	}

	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (format == PixelFormat.RGBX_8888)
				Log.d(TAG, "Pixel format is RGBX_8888");
			else if (format == PixelFormat.RGB_565)
				Log.d(TAG, "Pixel format is RGB_565");
			else if (format == ImageFormat.YV12)
				Log.d(TAG, "Pixel format is YV12");
			else
				Log.d(TAG, "Pixel format is other/unknown");
			if (mLibVLC != null) {
				mLibVLC.attachSurface(holder.getSurface(), PlayerView.this);
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSurface();
		}
	};

	@SuppressWarnings("unused")
	private final SurfaceHolder.Callback mSubtitlesSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (mLibVLC != null)
				mLibVLC.attachSubtitlesSurface(holder.getSurface());
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSubtitlesSurface();
		}
	};

	private final Handler eventHandler = new VideoPlayerHandler(this);

	@Override
	public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
		if (width * height == 0) {
			return;
		}
		// store video size
		mVideoHeight = height;
		mVideoWidth = width;
		mVideoVisibleHeight = visible_height;
		mVideoVisibleWidth = visible_width;
		mSarNum = sar_num;
		mSarDen = sar_den;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				changeSurfaceSize();
			}
		});
	}

	public void changeSurfaceSize() {
		int sw;
		int sh;
		// get screen size
		sw = getWidth();
		sh = getHeight();
		double dw = sw, dh = sh;
		boolean isPortrait;
		isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (sw > sh && isPortrait || sw < sh && !isPortrait) {
			dw = sh;
			dh = sw;
		}
		// sanity check
		if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}
		// compute the aspect ratio
		double ar, vw;
		if (mSarDen == mSarNum) {
			/* No indication about the density, assuming 1:1 */
			vw = mVideoVisibleWidth;
			ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
		} else {
			/* Use the specified aspect ratio */
			vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
			ar = vw / mVideoVisibleHeight;
		}

		// compute the display aspect ratio
		double dar = dw / dh;

		switch (mCurrentSize) {
		case SURFACE_BEST_FIT:
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_FIT_HORIZONTAL:
			dh = dw / ar;
			break;
		case SURFACE_FIT_VERTICAL:
			dw = dh * ar;
			break;
		case SURFACE_FILL:
			break;
		case SURFACE_16_9:
			ar = 16.0 / 9.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_4_3:
			ar = 4.0 / 3.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_ORIGINAL:
			dh = mVideoVisibleHeight;
			dw = vw;
			break;
		}

		SurfaceView surface;
		//SurfaceView subtitlesSurface;
		SurfaceHolder surfaceHolder;
		//SurfaceHolder subtitlesSurfaceHolder;
		FrameLayout surfaceFrame;

		surface = mSurface;
		//subtitlesSurface = mSubtitlesSurface;
		surfaceHolder = mSurfaceHolder;
		//subtitlesSurfaceHolder = mSubtitlesSurfaceHolder;
		surfaceFrame = mSurfaceFrame;

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		//subtitlesSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		android.view.ViewGroup.LayoutParams lp = surface.getLayoutParams();
		lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
		lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
		surface.setLayoutParams(lp);
		//subtitlesSurface.setLayoutParams(lp);

		// set frame size (crop if necessary)
		lp = surfaceFrame.getLayoutParams();
		lp.width = (int) Math.floor(dw);
		lp.height = (int) Math.floor(dh);
		surfaceFrame.setLayoutParams(lp);

		surface.invalidate();
		//subtitlesSurface.invalidate();

	}

	public void eventHardwareAccelerationError() {
		EventHandler em = EventHandler.getInstance();
		em.callback(EventHandler.HardwareAccelerationError, new Bundle());
	}

	

	/*public PlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}*/
	
	/**
	 * add by haoxiangtt
	 * @param index
	 */
	public void startIndex(int index){
		this.mCurrentPlayIndex = index;
		this.mCurrentPlayUrl = this.mLibVLC.getMediaList().getMRL(index);
		start();
	}
	
	/**
	 * add by haoxiangtt
	 */
	public void playIndex(int index){
		this.mCurrentPlayIndex = index;
		this.mCurrentPlayUrl = this.mLibVLC.getMediaList().getMRL(index);
		mLibVLC.playIndex(index);
		mSurface.setKeepScreenOn(true);
	}
	
	public int getVolume() {
		return mLibVLC.getVolume();
	}

	public void setVolume(int volume) {
		mLibVLC.setVolume(volume);
	}

	public int getPlayerState() {
		return mLibVLC.getPlayerState();
	}

	/**
	 * add by haoxiangtt
	 */
	public int getCurrentPlayIndex(){
		return this.mCurrentPlayIndex;
	}
	
	public String pathToUrl(String path) {
		return LibVLC.PathToURI(path);
	}

	public void setNetWorkCache(int time) {
		mLibVLC.setNetworkCaching(time);
	}
	
	
	
	
	
	
	
	
	
	
	public boolean isSeekable() {
		return mLibVLC.isSeekable();
	}

	
	@Override
	public void setOnChangeListener(OnChangeListener listener) {
		mOnChangeListener = listener;
	}
	
	@Override
	public void initPlayer(String url) {
		try {
			setNetWorkCache(NET_WORK_CACHE_TIME);
			mLibVLC.init(getContext().getApplicationContext());
		} catch (LibVlcException e) {
			throw new RuntimeException("PlayerView Init Failed");
		}
		mLibVLC.getMediaList().clear();
		mLibVLC.getMediaList().add(url);
		this.mCurrentPlayUrl = url;
		this.mCurrentPlayIndex = 0;
	}

	@Override
	public void initPlayer(ArrayList<String> list){
		initPlayer(list,0);
	}

	@Override
	public void initPlayer(ArrayList<String> list,int index){
		try {
			setNetWorkCache(NET_WORK_CACHE_TIME);
			mLibVLC.init(getContext().getApplicationContext());
		} catch (LibVlcException e) {
			throw new RuntimeException("PlayerView Init Failed");
		}
		mLibVLC.getMediaList().clear();
		for(String url : list){
			mLibVLC.getMediaList().add(url);
		}
		this.mCurrentPlayUrl = list.get(index);
		this.mCurrentPlayIndex = index;
	}

	@Override
	public boolean canSeekble() {
		return mCanSeek;
	}
	
	public boolean canSeekable() {
		return mCanSeek;
	}

	/**
	 * add by haoxiangtt
	 */
	public String getCurrentPlayUrl(){
		return this.mCurrentPlayUrl;
	}

	@Override
	public void play() {
		mLibVLC.play();
		mSurface.setKeepScreenOn(true);
	}

	@Override
	public void start() {
		mLibVLC.eventVideoPlayerActivityCreated(true);
		EventHandler.getInstance().addHandler(eventHandler);
		mLibVLC.playIndex(this.mCurrentPlayIndex);
		mSurface.setKeepScreenOn(true);
	
		/*
		 * WARNING: hack to avoid a crash in mediacodec on KitKat. Disable
		 * hardware acceleration if the media has a ts extension.
		 */
		if (LibVlcUtil.isKitKatOrLater()) {
			String locationLC = mCurrentPlayUrl.toLowerCase(Locale.ENGLISH);
			if (locationLC.endsWith(".ts") || locationLC.endsWith(".tts") || locationLC.endsWith(".m2t") || locationLC.endsWith(".mts")
					|| locationLC.endsWith(".m2ts")) {
				Log.i(TAG, ">>>>>>>>>>closed HardwareAcceleration<<<<<<<<<<<<<<");
				mDisabledHardwareAcceleration = true;
				mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
				mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
				return;
			}
		}
		
		//关闭硬件加速
		mDisabledHardwareAcceleration = true;
		mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
		mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
		
		//开启硬件加速
		//mDisabledHardwareAcceleration = false;
		//mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
		//mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
	}

	@Override
	public void pause() {
		mLibVLC.pause();
		mSurface.setKeepScreenOn(false);
	}

	@Override
	public void stop() {
		mLibVLC.stop();
		mSurface.setKeepScreenOn(false);
		EventHandler em = EventHandler.getInstance();
		em.removeHandler(eventHandler);
		// MediaCodec opaque direct rendering should not be used anymore since there is no surface to attach.
		mLibVLC.eventVideoPlayerActivityCreated(false);
		if (mDisabledHardwareAcceleration) {
			mLibVLC.setHardwareAcceleration(mPreviousHardwareAccelerationMode);
		}
		//mLibVLC.destroy();
	}

	@Override
	public void destroy(){
		mLibVLC.destroy();
	}

	/**
	 * add by haoxiangtt
	 */
	@Override
	public boolean playNext(){
		MediaList list = mLibVLC.getMediaList();
		if(list == null || list.size() == 0){ return false; }
		if(this.mCurrentPlayIndex >= list.size() - 1){ return false; }
		this.mCurrentPlayIndex++;
		this.mCurrentPlayUrl = list.getMRL(this.mCurrentPlayIndex);
		this.playIndex(this.mCurrentPlayIndex);
		return true;
	}

	/**
	 * add by haoxiangtt
	 */
	@Override
	public boolean playBack(){
		MediaList list = mLibVLC.getMediaList();
		if(list == null || list.size() == 0){ return false; }
		if(this.mCurrentPlayIndex <= 0){ return false; }
		this.mCurrentPlayIndex--;
		this.mCurrentPlayUrl = list.getMedia(this.mCurrentPlayIndex).getLocation();
		this.playIndex(this.mCurrentPlayIndex);
		return true;
	}

	@Override
	public void setTime(long time) {
		mLibVLC.setTime(time);
	}

	@Override
	public long getTime() {
		return mLibVLC.getTime();
	}

	@Override
	public long getDuration() {
		return mLibVLC.getLength();
	}
	
	public long getLength() {
		return mLibVLC.getLength();
	}

	@Override
	public boolean isPlaying() {
		return mLibVLC.isPlaying();
	}

	@Override
	public void seekTo(int delta) {
		// unseekable stream
		if (mLibVLC.getLength() <= 0 || !mCanSeek)
			return;

		long position = mLibVLC.getTime() + delta;
		if (position < 0)
			position = 0;
		mLibVLC.setTime(position);
	}
	
	public void seek(int delta) {
		// unseekable stream
		if (mLibVLC.getLength() <= 0 || !mCanSeek)
			return;

		long position = mLibVLC.getTime() + delta;
		if (position < 0)
			position = 0;
		mLibVLC.setTime(position);
	}

	
	
	
	
	
	
	
	
	
	
	private static class VideoPlayerHandler extends WeakHandler<PlayerView> {
		public VideoPlayerHandler(PlayerView owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			PlayerView playerView = getOwner();
			if (playerView == null)
				return;

			switch (msg.getData().getInt("event")) {
			case EventHandler.MediaPlayerNothingSpecial:
				break;
			case EventHandler.MediaPlayerOpening:
				break;
			case EventHandler.MediaParsedChanged:
				Log.d(TAG, "MediaParsedChanged");
				break;
			case EventHandler.MediaPlayerPlaying:
				Log.d(TAG, "MediaPlayerPlaying");
				if (playerView.mOnChangeListener != null) {
					playerView.mOnChangeListener.onLoadComplet();
				}
				break;
			case EventHandler.MediaPlayerPaused:
				Log.d(TAG, "MediaPlayerPaused");
				break;
			case EventHandler.MediaPlayerStopped:
				Log.d(TAG, "MediaPlayerStopped");
				break;
			case EventHandler.MediaPlayerEndReached:
				Log.d(TAG, "MediaPlayerEndReached");
				if (playerView.mOnChangeListener != null) {
					playerView.mOnChangeListener.onEnd();
				}
				break;
			case EventHandler.MediaPlayerVout:
				break;
			case EventHandler.MediaPlayerPositionChanged:
				//Log.d(TAG, "MediaPlayerPositionChanged>>>>position="+msg.getData());
				if (!playerView.mCanSeek) {
					playerView.mCanSeek = true;
				}
				if (playerView.mOnChangeListener != null) {
					playerView.mOnChangeListener.onPositionChanged(0);
				}
				break;
			case EventHandler.MediaPlayerEncounteredError:
				Log.d(TAG, "MediaPlayerEncounteredError");
				if (playerView.mOnChangeListener != null) {
					playerView.mOnChangeListener.onError();
				}
				break;
			case EventHandler.HardwareAccelerationError:
				Log.d(TAG, "HardwareAccelerationError");
				if (playerView.mOnChangeListener != null && playerView.mDisabledHardwareAcceleration) {
					playerView.stop();
					playerView.mOnChangeListener.onError();
				} else {
					playerView.handleHardwareAccelerationError();
				}
				break;
			case EventHandler.MediaPlayerTimeChanged:
				// avoid useless error logs
				break;
			case EventHandler.MediaPlayerBuffering:
				Log.d(TAG, "MediaPlayerBuffering");
				if (playerView.mOnChangeListener != null) {
					playerView.mOnChangeListener.onBufferChanged(msg.getData().getFloat("data"));
				}
				break;
			default:
				Log.d(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
				break;
			}
		}
	}

	
}
