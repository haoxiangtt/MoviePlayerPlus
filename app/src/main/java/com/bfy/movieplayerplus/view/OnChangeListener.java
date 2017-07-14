package com.bfy.movieplayerplus.view;

public interface OnChangeListener {

	void onBufferChanged(float buffer);
	
	void onPositionChanged(long progress);

	void onLoadComplet();

	void onError();

	void onEnd();
}
