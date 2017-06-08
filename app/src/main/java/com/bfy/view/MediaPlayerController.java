package com.bfy.view;

import java.util.ArrayList;

public interface MediaPlayerController {
	public boolean canSeekble();
	public void initPlayer(String url);
	public void initPlayer(ArrayList<String> list);
	public void initPlayer(ArrayList<String> list,int index);
	public void start();
	public void play();
	public void pause();
	public void stop();
	public void destroy();
	public boolean playNext();
	public boolean playBack();
	public void setTime(long time);
	public long getTime();
	public long getDuration();
	public boolean isPlaying();
	public void seekTo(int delta);
	public String getCurrentPlayUrl();
	public void setOnChangeListener(OnChangeListener listener);

}
