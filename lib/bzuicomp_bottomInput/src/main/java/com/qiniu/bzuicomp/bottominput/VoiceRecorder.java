package com.qiniu.bzuicomp.bottominput;

import static android.os.Environment.DIRECTORY_MUSIC;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.format.DateFormat;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.IOException;

public class VoiceRecorder {

    private MediaRecorder mMediaRecorder;
    private String filePath;

    public void startRecord(Context context) {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            filePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC)+ "/" + System.currentTimeMillis() + ".aac";
                    //context.getCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".aac";
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    public String stopRecord() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (RuntimeException e) {
            e.printStackTrace();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            File file = new File(filePath);
            if (file.exists())
                file.delete();
            filePath = "";
        }

        return filePath;
    }

}
