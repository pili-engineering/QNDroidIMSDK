package com.qiniu.qndroidimsdk.util;


import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;




public class AudioPlayer {

    private static final String TAG = AudioPlayer.class.getSimpleName();
    private static AudioPlayer sInstance = new AudioPlayer();

    private static int MAGIC_NUMBER = 500;
    private static int MIN_RECORD_DURATION = 1000;

    private Callback mPlayCallback;
    private Uri mAudioRecordPath;
    private MediaPlayer mPlayer;

    private Handler mHandler;

    private AudioPlayer() {
        mHandler = new Handler();
    }

    public static AudioPlayer getInstance() {
        return sInstance;
    }



    private void stopInternalRecord() {
        mHandler.removeCallbacksAndMessages(null);

    }

    public void startPlay(Context context,Uri filePath, Callback callback) {
        mAudioRecordPath = filePath;
        mPlayCallback = callback;
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(context,filePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopInternalPlay();
                    onPlayCompleted(true);
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            stopInternalPlay();
            onPlayCompleted(false);
        }
    }

    public void stopPlay() {
        stopInternalPlay();
        onPlayCompleted(false);
        mPlayCallback = null;
    }

    private void stopInternalPlay() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.release();
        mPlayer = null;
    }

    public boolean isPlaying() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    private void onPlayCompleted(boolean success) {
        if (mPlayCallback != null) {
            mPlayCallback.onCompletion(success);
        }
        mPlayer = null;
    }

    public interface Callback {
        void onCompletion(Boolean success);
    }

}
