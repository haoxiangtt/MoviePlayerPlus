package cn.richinfo.player.view;

import android.net.Uri;

import java.util.ArrayList;

public interface MediaPlayerController {

	//flag
	int SCREEN_FULL = 0;
	int SCREEN_DEFAULT = 1;

	//mode
	int SCALE_MODE_DEFAULT = 0;
	int SCALE_MODE_16_9 = 1;
	int SCALE_MODE_4_3 = 2;
	int SCALE_MODE_FULL = 3;

	boolean canSeekble();
	void initPlayer(String url);
	void initPlayer(Uri uri);
	void initPlayer(ArrayList<String> list);
	void initPlayer(ArrayList<String> list,int index);
	void start();
	void play();
	void pause();
	void stop();
	void destroy();
	boolean playNext();
	boolean playBack();
	void setTime(long time);
	long getTime();
	long getDuration();
	boolean isPlaying();
	void seekTo(int delta);
	String getCurrentPlayUrl();
	int getCurrentPlayIndex();
	void setOnChangeListener(OnChangeListener listener);
	void setVideoScale(int flag,int scalMode);

}
