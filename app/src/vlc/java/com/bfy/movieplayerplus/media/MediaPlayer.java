/*****************************************************************************
 * MediaPlayer.java
 *****************************************************************************
 * Copyright © 2015 VLC authors and VideoLAN
 *
 * Authors  Jean-Baptiste Kempf <jb@videolan.org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.bfy.movieplayerplus.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaFormat;
import android.media.TimedText;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/9 0009
 * @modifyDate : 2017/6/9 0009
 * @version    : 1.0
 * @desc       : vlc Mediaplayer 封装
 * </pre>
 */
public class MediaPlayer implements org.videolan.libvlc.MediaPlayer.EventListener
{
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;

    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;

    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;

    private static final String TAG = "MediaPlayer";

    private Media mCurrentMedia = null;
    private final LibVLC mLibVLC;
    private org.videolan.libvlc.MediaPlayer mMediaPlayer;

    private OnBufferingUpdateListener mBufferUpdateListener;
    private OnPreparedListener mPrepareListener;
    private OnCompletionListener mCompleteListener;
    private OnSeekCompleteListener mSeekCompleteListener;
    private OnVideoSizeChangedListener mVideoSizeChangedListener;
    private OnTimedTextListener mTimeTextListener;
    private OnErrorListener mErrorListener;
    private OnInfoListener mInfoListener;

    private SurfaceHolder mSurfaceHolder;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;
    private PowerManager.WakeLock mWakeLock = null;

    public MediaPlayer() {
        mLibVLC = new LibVLC();
        mMediaPlayer = new org.videolan.libvlc.MediaPlayer(mLibVLC);
        mMediaPlayer.setEventListener(this);
    }

    public static MediaPlayer create(Uri uri) {
        return create ( uri, null);
    }

    public static MediaPlayer create(Uri uri, SurfaceHolder holder) {
        return create(uri, holder, null, 0);
    }

    public static MediaPlayer create(Uri uri, SurfaceHolder holder,
                                     AudioAttributes audioAttributes, int audioSessionId) {
        MediaPlayer player = new MediaPlayer();
        //player.setDataSource(context, uri); This throws exception, but not this create()
        return player;
    }

    public static MediaPlayer create(int resid) {
        return create(resid, null, 0);
    }

    public static MediaPlayer create(int resid,
            AudioAttributes audioAttributes, int audioSessionId) {
        return null;
    }

    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, null);
    }

    // FIXME, this is INCORRECT, @headers are ignored
    public void setDataSource(Context context, Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mCurrentMedia = new Media(mLibVLC, uri);
        mMediaPlayer.setMedia(mCurrentMedia);
    }

    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mCurrentMedia = new Media(mLibVLC, path);
        mMediaPlayer.setMedia(mCurrentMedia);
    }

    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        mCurrentMedia = new Media(mLibVLC, fd);
        mMediaPlayer.setMedia(mCurrentMedia);
    }

    // FIXME, this is INCORRECT, @offset and @length are ignored
    public void setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fd);
    }

    public void prepare(){
        mCurrentMedia.addOption(":video-paused");
        if (mPrepareListener != null) {
            mPrepareListener.onPrepared(this);
        }
//        start();
    }

    public void prepareAsync() {
        mCurrentMedia.addOption(":video-paused");
        if (mPrepareListener != null) {
            mPrepareListener.onPrepared(this);
        }
//        start();
    }

    public void setDisplay(SurfaceHolder sh) {
        mSurfaceHolder = sh;
        mMediaPlayer.getVLCVout().setVideoSurface(sh.getSurface(), sh);
        mMediaPlayer.getVLCVout().attachViews();
        updateSurfaceScreenOn();
    }

    public void setSurface(Surface surface, SurfaceHolder holder) {
        if (mScreenOnWhilePlaying && surface != null) {
            Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        mMediaPlayer.getVLCVout().detachViews();
        mSurfaceHolder = holder;
        mMediaPlayer.getVLCVout().setVideoSurface(surface, holder);
        mMediaPlayer.getVLCVout().attachViews();
        updateSurfaceScreenOn();
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceHolder = surfaceView.getHolder();
        mMediaPlayer.getVLCVout().setVideoView(surfaceView);
        mMediaPlayer.getVLCVout().attachViews();
        updateSurfaceScreenOn();
    }

    public void setVideoScalingMode(int mode) {
    }

    public void start() throws IllegalStateException {
        if (!isPlaying()) {
            stayAwake(true);
            mMediaPlayer.play();
        }
    }

    public void stop() throws IllegalStateException {
        if (isPlaying()) {
            stayAwake(false);
            mMediaPlayer.stop();
        }
    }

    public void pause() throws IllegalStateException {
        if (isPlaying()) {
            stayAwake(false);
            mMediaPlayer.pause();
        }
    }

    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;

        try {
            Class cls = Class.forName("android.os.SystemProperties");
            Method method = cls.getDeclaredMethod("getBoolean", String.class, boolean.class);
            Object obj = method.invoke(null, "audio.offload.ignore_setawake", false);
            if ((boolean)obj == true) {
                return;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
//        if (SystemProperties.getBoolean("audio.offload.ignore_setawake", false) == true) {
//            Log.w(TAG, "IGNORING setWakeMode " + mode);
//            return;
//        }


        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode|PowerManager.ON_AFTER_RELEASE, android.media.MediaPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    public int getVideoWidth() {
        return -1;
    }

    public int getVideoHeight() {
        return -1;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void seekTo(int msec) throws IllegalStateException {
        if (mMediaPlayer.isSeekable()) {
            mMediaPlayer.setTime(msec);
        }
    }

    public boolean isSeekavble(){
        return mMediaPlayer.isSeekable();
    }

    // This is of course, less precise than VLC
    public int getCurrentPosition() {
        return (int)mMediaPlayer.getTime();
    }

    // This is of course, less precise than VLC
    public int getDuration() {
        return (int)mMediaPlayer.getLength();
    }

    public void setNextMediaPlayer(MediaPlayer next) {
        //todo by ouyangjinfu
    }

    public void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        mPrepareListener = null;
        mBufferUpdateListener = null;
        mCompleteListener = null;
        mSeekCompleteListener = null;
        mErrorListener = null;
        mInfoListener = null;
        mVideoSizeChangedListener = null;
        mTimeTextListener = null;
        mMediaPlayer.release();
    }

    public void reset() {
        //todo by ouyangjinfu
    }

    public void setAudioStreamType(int streamtype) {
        //todo by ouyangjinfu
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
    }

    public void setLooping(boolean looping) {
    }

    public boolean isLooping() {
        return false;
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume( (int)((leftVolume + rightVolume) * 100/2));
    }

    public void setAudioSessionId(int sessionId)  throws IllegalArgumentException, IllegalStateException {
    }

    public int getAudioSessionId() {
        return 0;
    }

    public void attachAuxEffect(int effectId) {
    }

    public void setAuxEffectSendLevel(float level) {
    }




    static public class TrackInfo implements Parcelable {

        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;

        TrackInfo(Parcel in) {
        }

        public static final Creator<TrackInfo> CREATOR = new Creator<TrackInfo>() {
            @Override
            public TrackInfo createFromParcel(Parcel in) {
                return new TrackInfo(in);
            }

            @Override
            public TrackInfo[] newArray(int size) {
                return new TrackInfo[size];
            }
        };

        public int getTrackType() {
            return MEDIA_TRACK_TYPE_UNKNOWN;
        }

        public String getLanguage() {
            return  "und";
        }

        public MediaFormat getFormat() {
            return null;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        @Override
        public String toString() {
            return "";
        }
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        //FIXME
        TrackInfo trackInfo[] = new TrackInfo[1];
        return trackInfo;
    }

    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";

    public void addTimedTextSource(String path) {
//        mMediaPlayer.addSlave(Media.Slave.Type.Subtitle, path, false);
        mMediaPlayer.setSubtitleFile(path);
    }

    public void addTimedTextSource(Uri uri) {
//        mMediaPlayer.addSlave(Media.Slave.Type.Subtitle, uri, false);
        mMediaPlayer.setSubtitleFile(uri.getPath());
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType)
            throws IllegalArgumentException, IllegalStateException {
    }

    public void addTimedTextSource(FileDescriptor fd, long offset, long length, String mime)
            throws IllegalArgumentException, IllegalStateException {
    }

    public int getSelectedTrack(int trackType) throws IllegalStateException {
        return 0;
    }

    public void selectTrack(int index) throws IllegalStateException {
    }

    public void deselectTrack(int index) throws IllegalStateException {
    }

    @Override
    protected void finalize() {}

    public interface OnPreparedListener
    {
        void onPrepared(MediaPlayer mp);
    }
    public interface OnCompletionListener
    {
        void onCompletion(MediaPlayer mp);
    }
    public interface OnBufferingUpdateListener
    {
        void onBufferingUpdate(MediaPlayer mp, int percent);
    }

    public interface OnSeekCompleteListener
    {
        public void onSeekComplete(MediaPlayer mp);
    }

    public interface OnVideoSizeChangedListener
    {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height);
    }

    public interface OnTimedTextListener
    {
        public void onTimedText(MediaPlayer mp, TimedText text);
    }

    public interface OnErrorListener
    {
        boolean onError(MediaPlayer mp, int what, int extra);
    }
    public interface OnInfoListener
    {
        boolean onInfo(MediaPlayer mp, int what, int extra);
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mPrepareListener = listener;
    }



    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mCompleteListener = listener;
    }



    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mBufferUpdateListener = listener;
    }



    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mSeekCompleteListener = listener;
    }


    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mVideoSizeChangedListener = listener;
    }


    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mTimeTextListener = listener;
    }



    public void setOnErrorListener(OnErrorListener listener) {
        this.mErrorListener = listener;
    }


    public void setOnInfoListener(OnInfoListener listener) {
        this.mInfoListener = listener;
    }

    public LibVLC getLibVLC() {
        return mLibVLC;
    }

    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }

    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    @Override
    public void onEvent(org.videolan.libvlc.MediaPlayer.Event event) {

        switch (event.type) {
//            case org.videolan.libvlc.MediaPlayer.Event.MediaChanged:
            case org.videolan.libvlc.MediaPlayer.Event.Stopped:
            case org.videolan.libvlc.MediaPlayer.Event.EndReached:{
                Log.i(TAG, ">>>>receive Event , action: stoped / end;registerType = " + event.type
                    + "; arg1 = " +event.getTimeChanged() + "; arg2 = " + event.getPositionChanged());
                stayAwake(false);
                if ( mCompleteListener != null) {
                    mCompleteListener.onCompletion(this);
                }
                break;
            }
            case org.videolan.libvlc.MediaPlayer.Event.EncounteredError:{
                Log.i(TAG, ">>>>receive Event , action: EncounteredError;registerType = " + event.type
                    + "; arg1 = " +event.getTimeChanged() + "; arg2 = " + event.getPositionChanged());
                stayAwake(false);
                if (mErrorListener != null) {
                    mErrorListener.onError(this, event.getEsChangedType(), event.getVoutCount());
                }
                break;
            }
            case org.videolan.libvlc.MediaPlayer.Event.Opening:{
                Log.i(TAG, ">>>>receive Event , action: Opening;registerType = " + event.type
                    + "; arg1 = " +event.getTimeChanged() + "; arg2 = " + event.getPositionChanged());
                /*if (mPrepareListener != null) {
                    mPrepareListener.onPrepared(this);
                }*/
                break;
            }
            /*case org.videolan.libvlc.MediaPlayer.Event.Buffering: {
                if (mBufferUpdateListener != null) {
                    mBufferUpdateListener.onBufferingUpdate(this, (int)(event.getBuffering() * 100));
                }
                break;
            }*/
            case org.videolan.libvlc.MediaPlayer.Event.Playing:
            case org.videolan.libvlc.MediaPlayer.Event.Paused: {
                Log.i(TAG, ">>>>receive Event , action: Playing / Paused;registerType = " + event.type
                    + ";arg1 = " +event.getTimeChanged() + ";arg2 = " + event.getPositionChanged());
                break;
            }
            case org.videolan.libvlc.MediaPlayer.Event.TimeChanged: {
                Log.i(TAG, ">>>>receive Event , action: TimeChanged;registerType = " + event.type
                    + ";arg1 = " +event.getTimeChanged() + ";arg2 = " + event.getPositionChanged());
                break;
            }
            case org.videolan.libvlc.MediaPlayer.Event.PositionChanged: {
                Log.i(TAG, ">>>>receive Event , action: PositionChanged;registerType = " + event.type
                    + ";arg1 = " +event.getTimeChanged() + ";arg2 = " + event.getPositionChanged());
                break;
            }
            case org.videolan.libvlc.MediaPlayer.Event.Vout: {
                Log.i(TAG, ">>>>receive Event; action: Vout; registerType = " + event.type
                        + "; arg1 = " +event.getTimeChanged() + "; arg2 = " + event.getPositionChanged());
                break;
            }
            case org.videolan.libvlc.MediaPlayer.Event.ESAdded:
            case org.videolan.libvlc.MediaPlayer.Event.ESDeleted:
            case org.videolan.libvlc.MediaPlayer.Event.SeekableChanged:
            case org.videolan.libvlc.MediaPlayer.Event.PausableChanged: {
                Log.i(TAG, ">>>>receive Event , action: ESAdded / ESDeleted / SeekableChanged / PausableChanged; registerType = "
                    + event.type + "; arg1 = " +event.getTimeChanged() + "; arg2 = " + event.getPositionChanged());
                break;
            }
            default: {
                Log.i(TAG, ">>>>receive Event , action: unknown;registerType = " + event.type
                    + "; arg1 = " +event.getTimeChanged() + "; arg2 = " + event.getPositionChanged());
            }
        }
    }

}
