package com.bfy.view;

public interface OnChangeListener {

	public void onBufferChanged(float buffer);
	
	public void onPositionChanged(long progress);

	public void onLoadComplet();

	public void onError();

	public void onEnd();
}
