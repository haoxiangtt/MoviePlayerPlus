视频播放器控件说明：
    引入方式：compile 'com.bfy:movie-player-plus:1.0.0'
    如果想使用vlc解码器还需导入一下依赖：
    compile 'de.mrmaffen:vlc-android-sdk:3.0.0'

    混淆规则：
    -keep class org.videolan.libvlc.**{*;}

	本项目集成了vlc和系统默认两种视频解码框架，分别进行了封装，主要封装的控件有4种：
	使用vlc解码框架的：
		1、VLCVideoView：使用surfaceView渲染视频；
		2、GlVlcVideoView：使用GlSurfaceView渲染视频；
	使用系统解码框架的：
		1、VideoView：使用surfaceView渲染视频；
		2、GlVideoView：使用GlSurfaceView渲染视频。
	简单集成方式：
	在layout布局文件中直接声明控件：
        <cn.richinfo.player.view.GlVideoView
                android:id="@+id/def_video"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_gravity="center"/>
    在代码中找到控件：
    MediaPlayerController player = (MediaPlayerController)findViewById(R.id.def_video);
    player.initPlayer(url);//初始化播放器
    player.start();//开始播放

作者：ouyangjinfu
注：该项目2019年起已经停止维护
