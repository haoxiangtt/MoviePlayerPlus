package cn.richinfo.player.view;

public interface OnChangeListener {

	void onBufferChanged(float buffer);
	
	void onPositionChanged(long progress);

	void onLoadComplet();

	void onError();

	void onEnd();
}
