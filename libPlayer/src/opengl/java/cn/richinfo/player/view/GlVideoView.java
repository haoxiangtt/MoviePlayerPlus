package cn.richinfo.player.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.MediaController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.richinfo.player.BuildConfig;
import cn.richinfo.player.utils.DirectDrawer;
import cn.richinfo.player.utils.GlUtil;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/9 0009
 * @modifyDate : 2017/6/9 0009
 * @version    : 1.0
 * @desc       : GlVideoView class使用OpenGL渲染视频的播放控件
 * 				can load images from various sources (such as resources or content
 * 				providers), takes care of computing its measurement from the video so that
 * 				it can be used in any layout manager, and provides various display options
 * 				such as scaling and tinting.
 * </pre>
 */
public class GlVideoView extends GLSurfaceView implements MediaPlayerController
		, GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{

	private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "VideoView";

    private Context mContext;
	private Handler mHandler;
	private ArrayList<String> mMediaList;
    private Uri         mCurrentUri;
    private int         mDuration;
    private int			mCurrentIndex;

    private MediaPlayer mMediaPlayer = null;
    private MediaController mMediaController = null;

    private boolean     mIsPrepared;
    private int         mVideoWidth;
    private int         mVideoHeight;
    private int         mSurfaceWidth;
    private int         mSurfaceHeight;
    private boolean     mStartWhenPrepared;
    private int         mSeekWhenPrepared;

    private OnChangeListener mOnChangeListener;
	private int mTextureID;
	private SurfaceTexture mSurface;
	private DirectDrawer mDirectDrawer;
	private boolean mUpdateSurface = false;

	public GlVideoView(Context context) {
	    this(context,null);
	}

	public GlVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mHandler = new Handler();
		initVideoView();
	}

    
    
    private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
	    new MediaPlayer.OnVideoSizeChangedListener() {
	        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
	        	if(DEBUG) Log.i(TAG, "OnVideoSizeChanged.......................");
	            mVideoWidth = mp.getVideoWidth();
	            mVideoHeight = mp.getVideoHeight();
	        }
	};
	
	private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
	        public void onPrepared(MediaPlayer mp) {
	            // briefly show the mediacontroller
	        	if(DEBUG) Log.i(TAG, "begin prepare....................");
	            mIsPrepared = true;
	           
	            if (mMediaController != null) {
	                mMediaController.setEnabled(true);
	            }
	            mVideoWidth = mp.getVideoWidth();
	            mVideoHeight = mp.getVideoHeight();
	            if (mVideoWidth != 0 && mVideoHeight != 0) {
	                if(DEBUG) Log.i(TAG, "video size: " + mVideoWidth +"/"+ mVideoHeight);
	                setVideoScale(SCREEN_FULL, SCALE_MODE_DEFAULT);
//	                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
	                    // We didn't actually change the size (it was already at the size
	                    // we need), so we won't get a "surface changed" callback, so
	                    // start the video here instead of in the callback.
//	                	if(DEBUG) Log.i(TAG, " if mStartWhenPrepared : " + mStartWhenPrepared);

				   if (mStartWhenPrepared) {
						mMediaPlayer.start();
						mStartWhenPrepared = false;
					   stayAwake(true);
					}

					if (mSeekWhenPrepared != 0) {
						mMediaPlayer.seekTo(mSeekWhenPrepared);
						mSeekWhenPrepared = 0;
					}


					if (mMediaController != null) {
						 mMediaController.show();
					}

	            } else {
	                // We don't know the video size yet, but should start anyway.
	                // The video size might be reported to us later.

	                if (mStartWhenPrepared) {
	                    mMediaPlayer.start();
	                    mStartWhenPrepared = false;
						stayAwake(true);
	                }

					if(DEBUG) Log.i(TAG, " else mStartWhenPrepared : " + mStartWhenPrepared);
					if (mSeekWhenPrepared != 0) {
						mMediaPlayer.seekTo(mSeekWhenPrepared);
						mSeekWhenPrepared = 0;
					}

					if (mMediaController != null) {
						mMediaController.show();
					}

	            }

	        }
	    };
	    
	private MediaPlayer.OnCompletionListener mCompletionListener =
	  new MediaPlayer.OnCompletionListener() {
	    public void onCompletion(MediaPlayer mp) {
			if (DEBUG) {
				Log.i(TAG, "playing complete!!!!!!!!!!!!");
			}
	        if (mMediaController != null) {
	            mMediaController.hide();
	        }
			stop();
			//recordPosition(0);
			if (mOnChangeListener != null) {
				mOnChangeListener.onEnd();;
			}
		}
	
	};
	
	private MediaPlayer.OnErrorListener mErrorListener =
	        new MediaPlayer.OnErrorListener() {
	        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
	            if(DEBUG) Log.d(TAG, "Error: " + framework_err + "," + impl_err);
	            if (mMediaController != null) {
	                mMediaController.hide();
	            }
	
	            /* If an error handler has been supplied, use it and finish. */
	            if (mOnChangeListener != null) {
	            	mOnChangeListener.onError();
	            }
				stayAwake(false);
	            return true;
	        }
	    };
	
	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
	  new MediaPlayer.OnBufferingUpdateListener() {
	    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//	        mCurrentBufferPercentage = percent;
	        if(mOnChangeListener != null){
	        	mOnChangeListener.onBufferChanged(percent);;
	        }
	    }
	};
	
	/*private MediaPlayer.OnTimedTextListener mTimeChangedListener = new MediaPlayer.OnTimedTextListener() {
			
			@Override
			public void onTimedText(MediaPlayer mp, TimedText text) {
				if(mOnChangeListener != null){
					mOnChangeListener.onPositionChanged(mMediaPlayer.getCurrentPosition());
				}
				
			}
	};*/




	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;

		//一定要设置版本
		setEGLContextClientVersion(2);
		// 设置与当前GLSurfaceView绑定的Renderer
		setRenderer(this);
		// 设置渲染的模式
		setRenderMode(RENDERMODE_WHEN_DIRTY);

		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
	}

	private void initPlayer(){
		 openVideo();
//		 requestLayout();
//		 invalidate();
	}

	private void openVideo() {
	
	        if (mCurrentUri == null || mSurface == null) {
	            return;
	        }
	        if(DEBUG) Log.i(TAG, "Uri Scheme : " + mCurrentUri.getScheme()
	        				+ "      ParentPath : " + new File(mCurrentUri.getPath()).getParent());
	        
	        // Tell the music playback service to pause
	        Intent i = new Intent("com.android.music.musicservicecommand");
	        i.putExtra("command", "pause");
	        mContext.sendBroadcast(i);
	        if (mMediaPlayer != null) {
	            mMediaPlayer.reset();
	            mMediaPlayer.release();
	            mMediaPlayer = null;
	        }
	        try {
//	        	begin playing video.........................................
	        	mDuration = -1;
	        	mIsPrepared = false;
	            mMediaPlayer = new MediaPlayer();
	            mMediaPlayer.setOnPreparedListener(mPreparedListener);
	            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
	            mMediaPlayer.setOnCompletionListener(mCompletionListener);
	            mMediaPlayer.setOnErrorListener(mErrorListener);
	            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
	            //mMediaPlayer.setOnTimedTextListener(null);
				Surface surface = new Surface(mSurface);
				mMediaPlayer.setSurface(surface);
				surface.release();
				mMediaPlayer.setDataSource(mContext, mCurrentUri);
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	            //mMediaPlayer.setVolume(1f, 1f);

				/**
				 * see{@link #stayAwake}
				 * 使用Glsurfaceview渲染视频后这个方法不起作用
				 */
	            mMediaPlayer.setScreenOnWhilePlaying(true);

	            mMediaPlayer.prepareAsync();
	        } catch (IOException ex) {
	            Log.w(TAG, "Unable to open content: " + mCurrentUri, ex);
	            return;
	        }
	    }

	private void setScale(int width , int height){
		LayoutParams lp = getLayoutParams();
		lp.height = height;
		lp.width = width;
		setLayoutParams(lp);
	}
	
	private int[] adjustScale(int cw,int ch,int vw,int vh){
    	int[] scale = new int[]{cw,ch};
    	if(vh <= 0 || vw <= 0) return scale;
    	if(ch * vw > cw * vh){
    		//Log.i(TAG, "video too wide, correcting");
    		scale[1] = cw * vh / vw;
    	}else if(ch * vw < cw * vh){
    		//Log.i(TAG, "video too tall, correcting");
    		scale[0] = ch * vw / vh;
    	}
    	return scale;
    	
    }

    private void stayAwake (boolean flag) {
		if (flag) {
			((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			((Activity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	/*private void toggleMediaControlsVisiblity() {
	    if (mMediaController.isShowing()) {
	        mMediaController.hide();
	    } else {
	        mMediaController.show();
	    }
	}*/

	/*private void attachMediaController() {
	     if (mMediaPlayer != null && mMediaController != null) {
	          mMediaController.setMediaPlayer(this);
	          View anchorView = this.getParent() instanceof View ?
	                (View)this.getParent() : this;
	          mMediaController.setAnchorView(anchorView);
	          mMediaController.setEnabled(mIsPrepared);
	     }
	}*/

	/*public void setMediaController(MediaController controller) {
	     if (mMediaController != null) {
	         mMediaController.hide();
	     }
	     mMediaController = controller;
	     attachMediaController();
	}*/

	public int getVideoWidth(){
    	return mVideoWidth;
    }
    
    public int getVideoHeight(){
    	return mVideoHeight;
    }
    
    @Override
    public void setVideoScale(int flag,int scalMode){
    	if(!(mContext instanceof Activity)) return;
    	switch(flag){
    		case SCREEN_FULL: {
				((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.GONE);
//				setScale(r.width(), r.height());
				break;
			}
    		case SCREEN_DEFAULT: {
				//end by haoxiangtt
				((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

				break;
			}
    	}
		Rect r = new Rect();
		View p = ((View)getParent());
		if(p != null) p.getGlobalVisibleRect(r);

		int mWidth = r.width();
		int mHeight = r.height();
		if (DEBUG) Log.i(TAG, "the parent screen width/height =(" + mWidth + ":" + mHeight + ")");
		if (DEBUG) Log.i(TAG, "the video width/height =(" + mVideoWidth + ":" + mVideoHeight + ")");
		//add by haoxiangtt 2014.4.2 for change scale
		int[] scale = null;
		switch (scalMode) {
			case SCALE_MODE_DEFAULT: {
				//原比例
				scale = adjustScale(mWidth, mHeight, mVideoWidth, mVideoHeight);
				break;
			}
			case SCALE_MODE_16_9: {
				//16:9
				scale = adjustScale(mWidth, mHeight, 16, 9);
				break;
			}
			case SCALE_MODE_4_3: {
				//4:3
				scale = adjustScale(mWidth, mHeight, 4, 3);
				break;
			}
			case SCALE_MODE_FULL: {
				scale = adjustScale(mWidth, mHeight, 0, 0);
			}
		}
		if (DEBUG) {
			Log.i(TAG, "the scale =(" + scale[0] + ":" + scale[1] + ")");
		}
		setScale(scale[0], scale[1]);
    }

    
	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize =  MeasureSpec.getSize(measureSpec);
		switch (specMode) {
		    case MeasureSpec.UNSPECIFIED:
		       /* Parent says we can be as big as we want. Just don't be larger
		        * than max size imposed on ourselves.*/
		      result = desiredSize;
		      break;
		   case MeasureSpec.AT_MOST:
		       /* Parent says we can be as big as we want, up to specSize.
		        * Don't be larger than specSize, and don't be larger than
		        * the max size imposed on ourselves.*/
		       result = Math.min(desiredSize, specSize);
		        break;
		
		   case MeasureSpec.EXACTLY:
		       // No choice. Do what we are told.
		      result = specSize;
		      break;
		}
		return result;
	}

    
    public boolean takeScreenShot(){
    	return false;
    }

	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
            //toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (mIsPrepared &&
                keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL &&
                mMediaPlayer != null && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    && mMediaPlayer.isPlaying()) {
                pause();
                mMediaController.show();
            } else {
               // toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setOnChangeListener(OnChangeListener listener) {
    	mOnChangeListener = listener;
    }

	@Override
    public void initPlayer(Uri uri) {
		mMediaList = new ArrayList<String>();
		mCurrentUri = uri;
		mCurrentIndex = 0;
		mMediaList.clear();
		mMediaList.add(uri.toString());
		initPlayer();
	    
	}
    
   
    
    @Override
	public void initPlayer(String path) {
		if(path != null &&  !path.equals("")){
			initPlayer(Uri.parse(path));
		}
	}

	
	@Override
	public void initPlayer(ArrayList<String> list){
		initPlayer(list, 0);
	}
	
	@Override
	public void initPlayer(ArrayList<String> list,int index){
		mMediaList = list;
		mCurrentUri = Uri.parse(mMediaList.get(index));
		mCurrentIndex = index;
		initPlayer();
	}
	
	@Override
    public void start() {
//    	Log.i(TAG, "start: mMediaplayer = " + mMediaPlayer);
        if (mMediaPlayer != null && mIsPrepared) {
        	//Log.i(TAG, " out start le..................");
			mMediaPlayer.start();
			mStartWhenPrepared = false;
			stayAwake(true);
        } else {
            mStartWhenPrepared = true;
        }
    }
	
	@Override
	public void play() {
		if (mMediaPlayer != null && mIsPrepared) {
			mMediaPlayer.start();
			stayAwake(true);
		}
	}
	
    @Override
    public void pause() {
//    	Log.i(TAG, "pause: mMediaplayer = " + mMediaPlayer);
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
				stayAwake(false);
            }
        }
//        mStartWhenPrepared = false;
    }
    
    @Override
    public void stop() {
		if(DEBUG) Log.i(TAG, "enter Stop method:"+mMediaPlayer);
	    if (mMediaPlayer != null && mIsPrepared) {
	    	if(DEBUG) Log.i(TAG, "stop media play................");
	    	//long t = getTime();
	    	//recordPosition(t);
	        mMediaPlayer.stop();
			stayAwake(false);
	    }
	}
    
    @Override
    public void destroy(){
    	if(mMediaPlayer != null){
			if (isPlaying()) {
				mMediaPlayer.stop();
			}
	    	mMediaPlayer.reset();
	    	mMediaPlayer.release();
		    //this.invalidate();
		    mMediaPlayer = null;
    	}
    }

	@Override
    public long getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }
	
    @Override
    public long getTime() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    
    @Override
	public void setTime(long time) {
		if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo((int)time);
        } else {
            mSeekWhenPrepared = (int)time;
        }
		
	}
    
    @Override
    public void seekTo(int delta) {
    	int seek = mMediaPlayer.getCurrentPosition() + delta;
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(seek);
        } else {
            mSeekWhenPrepared = seek;
        }
    }
    
    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

	@Override
	public boolean canSeekble() {
		return true;
	}

	

	@Override
	public boolean playNext() {
		if(mMediaList == null || mMediaList.size() == 0){ return false; }
		if(this.mCurrentIndex >= mMediaList.size() - 1){ return false; }
		mCurrentIndex++;
		mCurrentUri = Uri.parse(mMediaList.get(mCurrentIndex));
		try {
			if(mMediaPlayer.isPlaying()){ mMediaPlayer.stop(); }
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(mContext, mCurrentUri);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		}catch (IOException e) {
			if(DEBUG){ e.printStackTrace(); }
			return false;
		}
		return true;
	}

	@Override
	public boolean playBack() {
		if(mMediaList == null || mMediaList.size() == 0){ return false; }
		if(this.mCurrentIndex <= 0){ return false; }
		mCurrentIndex--;
		mCurrentUri = Uri.parse(mMediaList.get(mCurrentIndex));
		try {
			if(mMediaPlayer.isPlaying()){ mMediaPlayer.stop(); }
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(mContext, mCurrentUri);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		}catch (IOException e) {
			if(DEBUG){ e.printStackTrace(); }
			return false;
		}
		return true;
	}


	@Override
	public String getCurrentPlayUrl() {
		return mCurrentUri != null ? mCurrentUri.toString() : "";
	}

	@Override
	public int getCurrentPlayIndex() {
		return mCurrentIndex;
	}



	@Override
	public void onPause() {
		super.onPause();
//		CameraCapture.get().doStopCamera();
	}


	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		if (DEBUG) {
			Log.i(TAG, "onFrameAvailable...");
		}
		mUpdateSurface = true;
		requestRender();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		if (DEBUG) {
			Log.i(TAG, "onSurfaceCreated...");
		}
		mTextureID = GlUtil.createTextureID();
		mSurface = new SurfaceTexture(mTextureID);
		mSurface.setOnFrameAvailableListener(this);
		mDirectDrawer = new DirectDrawer(mContext, mTextureID);
//		CameraCapture.get().openBackCamera();
		//尽量还是在ui线程中执行，GlThread线程总会出些毛病
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				openVideo();
			}
		});
	}


	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (DEBUG) {
			Log.i(TAG, "onSurfaceChanged...");
		}
		mSurfaceWidth = width;
		mSurfaceHeight = height;
		// 设置OpenGL场景的大小,(0,0)表示窗口内部视口的左下角，(w,h)指定了视口的大小
//		if (!CameraCapture.get().isPreviewing()) {
//			CameraCapture.get().doStartPreview(mSurface);
//		}
		//下列代码意义为使绘制区域适应视频尺寸, 我们这里统一设置成占满整个控件, 尺寸的调整交给setVideoScale方法
		float screenRatio=width*1f/height;//屏幕宽高比
		float videoRatio=width*1f/height;//视频宽高比
		if (videoRatio>screenRatio){
			Matrix.orthoM(mDirectDrawer.mMVP,0,-1f,1f,-videoRatio/screenRatio,videoRatio/screenRatio,-1f,1f);
		}else Matrix.orthoM(mDirectDrawer.mMVP,0,-screenRatio/videoRatio,screenRatio/videoRatio,-1f,1f,-1f,1f);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if (DEBUG) {
			Log.i(TAG, "onDrawFrame...");
		}
		// 设置白色为清屏
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		// 清除屏幕和深度缓存
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		// 更新纹理
		synchronized (this) {
			if (mUpdateSurface) {
				mSurface.updateTexImage();
				mSurface.getTransformMatrix(mDirectDrawer.mSTMatrix);
				mUpdateSurface = false;
			}
		}
		mDirectDrawer.draw();
	}



}
