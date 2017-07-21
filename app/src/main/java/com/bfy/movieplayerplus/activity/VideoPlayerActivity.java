package com.bfy.movieplayerplus.activity;

import java.util.ArrayList;
import java.util.Date;

import com.bfy.movieplayerplus.R;
import com.bfy.movieplayerplus.utils.LogUtils;
import com.bfy.movieplayerplus.view.MediaPlayerController;
import com.bfy.movieplayerplus.view.OnChangeListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.AnimationUtils;

public class VideoPlayerActivity extends AppCompatActivity implements OnClickListener,Callback,
						OnSeekBarChangeListener,OnChangeListener{

	protected static final String TAG = "VideoPlayerActivity";
	protected static final boolean DEBUG = LogUtils.isDebug;
	protected static final int RECORDER_DEFAULT = 0;
	protected static final int RECORDER_VLC = 1;

	protected static final int HANDLE_HIDE_CONTROLLER = 0;
	protected static final int HANDLE_SHOW_CONTROLLER = 1;
	protected static final int HANDLE_SET_PROGRESS = 2;

	protected static final int DIALOG_ALERT_PLAY_POSITION = 0;

	protected static final int CONTROLLER_DELAY_HIDE = 5000;
	protected static final long GUESTURE_CLICK_DURATION = 200;
	protected static final int DELTA_TO_TIME = 100;
	protected static final float MIN_DELTA = 50;
	
	public static final String RECORD_POSITION = "RECORD_POSITION";
	public static final String PLAYER_SETTINGS = "PLAYER_SETTINGS";

	protected ViewGroup mPlayerScreen;
	protected MediaPlayerController mPlayer;
	protected ViewGroup mTitleBar;
	protected TextView mTVTitle;
	protected ViewGroup mBufferLoadingView;
	protected TextView mTVBuffer;
	protected TextView mTVMessage;
	protected ViewGroup mMessageView;
	protected ViewGroup mController;
	protected TextView mTVTime;
	protected TextView mTVLength;
	protected SeekBar mSeekBar;
	protected ImageButton mBackBtn;
	protected ImageButton mForwardBtn;
	protected ImageButton mTogglePlayBtn;

	protected Handler mHandler;

	protected int mCurrentRecoder = RECORDER_VLC;
	protected boolean isSeekbarStartTrackingTouch = false;//判定用户正在调整播放进度
	protected boolean isPlayOnce = false;
	protected boolean isRecordPosition = true;//是否记录播放点

	protected long mStartTime = 0;
	protected float mLastX = 0;

	protected boolean readOnce;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.video_player_layout);
		readOnce = true;
		init();
		handleIntent();
	}
	

	@Override
	protected void onStart() {
		if(DEBUG){ Log.i(TAG, "on avtibity start........"); }
		super.onStart();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(DEBUG){ Log.i(TAG, "on avtibity resume........"); }
		if(!mPlayer.isPlaying()){
			if(DEBUG){ Log.i(TAG, "on player start........"); }
			mPlayer.start();
			if(DEBUG){ Log.i(TAG, "on to position........"); }
			long position = readPosition();
			if(position != 0){
                if (readOnce) {
                    showDialog(DIALOG_ALERT_PLAY_POSITION, null);
                } else {
                    mPlayer.setTime(position);
                }
			}
            readOnce = false;
			mHandler.sendEmptyMessage(HANDLE_SET_PROGRESS);
			mHandler.sendEmptyMessage(HANDLE_SHOW_CONTROLLER);
			if(DEBUG){ Log.i(TAG, "on avtibity start........"); }
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
//		ViewGroup.LayoutParams lp = ((View) mPlayer).getLayoutParams();
//		lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//		lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
		super.onConfigurationChanged(newConfig);
		mPlayerScreen.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mPlayer.setVideoScale(MediaPlayerController.SCREEN_FULL,
						MediaPlayerController.SCALE_MODE_DEFAULT);
				if (Build.VERSION.SDK_INT >= 16) {
					mPlayerScreen.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					mPlayerScreen.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});

	}



	@Override
	protected void onPause() {
		if(DEBUG){ Log.i(TAG, "on avtibity pause........"); }
		if(this.mPlayer.isPlaying()){
			if(isRecordPosition){ recordPosition(this.mPlayer.getTime()); }
			this.mPlayer.pause();
		}
		mHandler.removeMessages(HANDLE_SET_PROGRESS);
		super.onPause();
	}

	@Override
	protected void onStop() {
		if(DEBUG){ Log.i(TAG, "on avtibity stop........"); }
		//this.mPlayer.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if(DEBUG){ Log.i(TAG, "on avtibity destroy........"); }
		this.mPlayer.destroy();
		super.onDestroy();
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case DIALOG_ALERT_PLAY_POSITION:{
				AlertDialog d = new AlertDialog.Builder(this).setTitle(R.string.prompt)
					.setMessage(R.string.tip_play_position).setPositiveButton(R.string.yes, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mPlayer.setTime(readPosition());
							
						}
					}).setNegativeButton(R.string.no, null).create();
				return d;
			}
		}
		return null;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		super.onPrepareDialog(id, dialog, args);
	}
	
	
	
	/**
	 * ************************private mathods*****************************
	 */

	protected void init() {
		//初始化组件
		mHandler = new Handler(this);

		//初始化控件
		initView();

		//确定解码器
		comfirmPlayer();

	}

	protected void initView() {
		mTitleBar = (ViewGroup)findViewById(R.id.title_bar);
		mTVTitle = (TextView)findViewById(R.id.tv_title);
		mBufferLoadingView = (ViewGroup)findViewById(R.id.rl_loading);
		mTVBuffer = (TextView)findViewById(R.id.tv_buffer);
		mTVMessage = (TextView)findViewById(R.id.tv_message);
		mMessageView = (ViewGroup)findViewById(R.id.rl_message);
		mController = (ViewGroup)findViewById(R.id.controller);

		mTVTime = (TextView)this.mController.findViewById(R.id.tv_time);
		mTVLength = (TextView)this.mController.findViewById(R.id.tv_length);
		mSeekBar = (SeekBar)this.mController.findViewById(R.id.sb_video);
		mTogglePlayBtn = (ImageButton)this.mController.findViewById(R.id.ib_play);
		mBackBtn = (ImageButton)this.mController.findViewById(R.id.ib_backward);
		mForwardBtn = (ImageButton)this.mController.findViewById(R.id.ib_forward);

		//添加响应事件
		mTogglePlayBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		mForwardBtn.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	@SuppressLint("WrongViewCast")
	protected void comfirmPlayer() {
		mCurrentRecoder = getSharedPreferences(PlayerListActivity.MAIN_SETTINGS, MODE_PRIVATE)
				.getInt(PlayerListActivity.KEY_RECODER, 0);
		if (mCurrentRecoder == RECORDER_DEFAULT) {
			ViewStub wsDefault = (ViewStub) findViewById(R.id.ws_default);
			mPlayerScreen = (ViewGroup)wsDefault.inflate();
			mPlayer = (MediaPlayerController)findViewById(R.id.def_video);
		} else {
			ViewStub wsVlc = (ViewStub) findViewById(R.id.ws_vlc);
			mPlayerScreen = (ViewGroup)wsVlc.inflate();
			mPlayer = (MediaPlayerController) findViewById(R.id.pv_video);
		}
		mPlayer.setOnChangeListener(this);
	}


	protected void handleIntent() {
		Intent intent = getIntent();
		Uri uri = intent.getData();

		String url = "";
		if (uri != null) {
			url = (uri.getHost() != null ? uri.getHost() : "") + (uri.getPort() != -1 ? ":" + uri.getPort() : "")
				+ (uri.getPath() != null ? "/" + uri.getPath() : "");
		}
		if(uri != null && !TextUtils.isEmpty(url)){
			if(DEBUG){ Log.i(TAG, ">>>>>>>>>uri="+uri.toString()+"<<<<<<<<<<<"); }
			this.isPlayOnce = true;
			this.mPlayer.initPlayer(uri.toString().trim());
			//this.mPlayer.start();
			this.mTVTitle.setText(uri.getPath());
			mHandler.sendEmptyMessage(HANDLE_SHOW_CONTROLLER);
		}else{
			this.isPlayOnce = false;
			Bundle b = intent.getBundleExtra("data");
			if(b == null){ showMessage(getResources().getString(R.string.no_video));finish(); }
			ArrayList<String> list = (ArrayList<String>)b.getSerializable("medialist");
			if(list == null){ showMessage(getResources().getString(R.string.no_video));finish(); }
			int index = b.getInt("index", 0);
			this.mPlayer.initPlayer(list, index);
			//this.mPlayer.start();
			this.mTVTitle.setText(list.get(index));
			mHandler.sendEmptyMessage(HANDLE_SHOW_CONTROLLER);
		}
		
	}

	
	
	private String parseTime(long time){
		
		time = time / 1000; //转换成秒
		int second = (int)time % 60;
		time = time / 60;//转换成分
		int minute = (int)time % 60;
		time = time / 60;//转换成小时
		int hour = (int)time;
	
		return String.format("%02d:%02d:%02d", hour,minute,second);
	}

	private void showMessage(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void setProgress(){
		if(this.isSeekbarStartTrackingTouch || !mPlayer.isPlaying()){ return; }
		this.mSeekBar.setMax((int)this.mPlayer.getDuration());
		this.mSeekBar.setProgress((int)this.mPlayer.getTime());
		this.mTVLength.setText(parseTime(this.mPlayer.getDuration()));
		this.mTVTime.setText(parseTime(this.mPlayer.getTime()));
		
	}
	
	

	private void showController(){
		if(this.mController.getVisibility() == View.VISIBLE){ return; }
		this.mController.setVisibility(View.VISIBLE);
		this.mTitleBar.setVisibility(View.VISIBLE);
		this.mController.startAnimation(
				AnimationUtils.loadAnimation(this, R.anim.operate_controler_open));
	}

	private void hideController(){
		if(this.mController.getVisibility() == View.GONE){ return; }
		this.mController.startAnimation(
				AnimationUtils.loadAnimation(this, R.anim.operate_controler_close));
		this.mTitleBar.setVisibility(View.GONE);
		this.mController.setVisibility(View.GONE);
	}

	private void showBuffering(){
		this.mBufferLoadingView.setVisibility(View.VISIBLE);
	}

	private void hideBuffering(){
		this.mBufferLoadingView.setVisibility(View.GONE);
	}

	private void togglePlay(){
		if(this.mPlayer.isPlaying()){
			this.mPlayer.pause();
			recordPosition(this.mPlayer.getTime());
			this.mTogglePlayBtn.setImageLevel(1);
		}else{
			this.mPlayer.play();
			this.mTogglePlayBtn.setImageLevel(0);
		}
	}
	
	private void recordPosition(long position) {
		//Log.i(TAG, "record Position..........:"+position);
        SharedPreferences  pre = getSharedPreferences(RECORD_POSITION, Context.MODE_PRIVATE);
        Editor editor = pre.edit();
        String path = this.mPlayer.getCurrentPlayUrl();
        editor.putLong(path, position);
        editor.commit();
	}
    
    private long readPosition(){
	       SharedPreferences pre = getSharedPreferences(RECORD_POSITION, Context.MODE_PRIVATE);
	       return pre.getLong(this.mPlayer.getCurrentPlayUrl(), 0);
    }
    
    private void changeRecorder(int recorder){
    	//TODO >>>>>>>>>>>>>>no thing<<<<<<<<<<<<<<<<
    }

	private void doEventDown(MotionEvent event) {
		mLastX = event.getRawX();
		mStartTime = new Date().getTime();
		
	}

	private void doEventMove(MotionEvent event) {
		float delta = event.getRawX() - mLastX;
		if(Math.abs(delta) > MIN_DELTA){
			if(mMessageView.getVisibility() != View.VISIBLE){
				mMessageView.setVisibility(View.VISIBLE);
			}
			
			long offsetValue = mPlayer.getTime() + (int)delta * DELTA_TO_TIME;
			
			
			if(offsetValue > mPlayer.getDuration()){
				offsetValue = mPlayer.getDuration();
			}else if(offsetValue < 0){
				offsetValue = 0;
			}
			
			mTVMessage.setText(parseTime(offsetValue));
		}
		
	}

	
	
	private void doEventUp(MotionEvent event) {
		long time = new Date().getTime() - mStartTime;
		
		if(time < GUESTURE_CLICK_DURATION){
			if(mController.getVisibility() == View.VISIBLE){
				
				mHandler.sendEmptyMessage(HANDLE_HIDE_CONTROLLER);
			}else{
				mHandler.sendEmptyMessage(HANDLE_SHOW_CONTROLLER);
			}
		}else{
			float delta = event.getRawX() - mLastX;
			if(mMessageView.getVisibility() == View.VISIBLE){
				mMessageView.setVisibility(View.GONE);
			}
			
			mPlayer.setTime(mPlayer.getTime() + (int)delta * DELTA_TO_TIME);
		}
		
		mStartTime = 0;
		mLastX = 0;
	}
	
	
	
	/**********************************************************************/
	
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				doEventDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				doEventMove(event);
				break;
			default:
				doEventUp(event);
				// updateHeight(height);
				break;
		}
		
		return super.onTouchEvent(event);
		//return this.mGestureDetector.onTouchEvent(event);
	}

	/*******************************************************/
	
	/**
	 * handler message
	 */
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLE_SHOW_CONTROLLER:{
				showController();
				mHandler.removeMessages(HANDLE_HIDE_CONTROLLER);
				mHandler.sendEmptyMessageDelayed(HANDLE_HIDE_CONTROLLER, CONTROLLER_DELAY_HIDE);
				break;
			}
			
			case HANDLE_HIDE_CONTROLLER:{
				hideController();
				break;
			}
			
			case HANDLE_SET_PROGRESS:{
				setProgress();
				mHandler.sendEmptyMessageDelayed(HANDLE_SET_PROGRESS, 100);
				break;
			}
	
		}
		return false;
	}

	/**
	 * controller button events
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ib_play:{
				togglePlay();
				break;
			}
			case R.id.ib_backward:{
				if(isRecordPosition){ recordPosition(this.mPlayer.getTime()); }
				if(!this.mPlayer.playBack()){
					showMessage(getResources().getString(R.string.msg_end));
				}
				break;
			}
			case R.id.ib_forward:{
				if(isRecordPosition){ recordPosition(this.mPlayer.getTime()); }
				if(!this.mPlayer.playNext()){
					showMessage(getResources().getString(R.string.msg_end));
				}
				break;
			}

		}
		
		mHandler.sendEmptyMessage(HANDLE_SHOW_CONTROLLER);
		
	}
	
	/**
	 * Seekbar changed events
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(fromUser && this.mPlayer.canSeekble()){
			this.mPlayer.setTime(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		this.isSeekbarStartTrackingTouch = true;
		mHandler.removeMessages(HANDLE_HIDE_CONTROLLER);
		
	}

	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		this.isSeekbarStartTrackingTouch = false;
		mHandler.sendEmptyMessage(HANDLE_SHOW_CONTROLLER);
	}
	/****************************************************/
	
	
	
	/**
	 * PlayerView onVideochangeListener event
	 */
	@Override
	public void onBufferChanged(float buffer) {
//		if(DEBUG){ Log.i(TAG, ">>>>>>>buffer update:"+buffer); }
		if(buffer >= 100){
			hideBuffering();
		}else{
			showBuffering();
		}
		this.mTVBuffer.setText(getResources().getString(R.string.msg_buffering, (int)buffer+"%"));
	}
	
	@Override
	public void onPositionChanged(long progress) {
		/*if(!this.isSeekbarStartTrackingTouch){
			this.mHandler.sendEmptyMessage(HANDLE_SET_PROGRESS);
		}*/
	}
	
	

	@Override
	public void onLoadComplet() {
		
		
	}

	@Override
	public void onError() {
		showMessage(getResources().getString(R.string.play_error));
		finish();
		
	}

	@Override
	public void onEnd() {
		if(isRecordPosition){ recordPosition(0); }
		if(this.isPlayOnce){
			finish();
		}else{
			if(!this.mPlayer.playNext()){
				finish();
			}
		}
		
	}
	/********************************************************/
	
}
