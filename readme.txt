视频播放器控件说明：
	本项目集成了vlc和系统默认两种视频解码框架，分别进行了封装，主要封装的控件有4种：
	使用vlc解码框架的：
		1、VLCVideoView：使用surfaceView渲染视频；
		2、GlVlcVideoView：使用GlSurfaceView渲染视频；
	使用系统解码框架的：
		1、VideoView：使用surfaceView渲染视频；
		2、GlVideoView：使用GlSurfaceView渲染视频。