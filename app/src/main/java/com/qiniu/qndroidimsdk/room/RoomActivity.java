package com.qiniu.qndroidimsdk.room;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.qiniu.droid.rtc.QNAudioFrame;
import com.qiniu.droid.rtc.QNAudioFrameListener;
import com.qiniu.droid.rtc.QNCameraSwitchResultCallback;
import com.qiniu.droid.rtc.QNCameraVideoTrackConfig;
import com.qiniu.droid.rtc.QNClientEventListener;
import com.qiniu.droid.rtc.QNConnectionState;
import com.qiniu.droid.rtc.QNJoinResultCallback;
import com.qiniu.droid.rtc.QNMicrophoneAudioTrackConfig;
import com.qiniu.droid.rtc.QNPublishResultCallback;
import com.qiniu.droid.rtc.QNRTC;
import com.qiniu.droid.rtc.QNRTCClient;
import com.qiniu.droid.rtc.QNRTCEventListener;
import com.qiniu.droid.rtc.QNRTCSetting;
import com.qiniu.droid.rtc.QNSurfaceView;
import com.qiniu.droid.rtc.QNTrack;
import com.qiniu.droid.rtc.QNVideoFormat;
import com.qiniu.droid.rtc.model.QNAudioDevice;
import com.qiniu.qndroidimsdk.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class RoomActivity extends AppCompatActivity implements QNRTCEventListener, QNClientEventListener {
    private static final String TAG = "RoomActivity";
    private static final String TAG_CAMERA = "camera";
    private static final String TAG_MICROPHONE = "microphone";

    private QNSurfaceView mLocalVideoSurfaceView;
    private QNSurfaceView mRemoteVideoSurfaceView;
    private QNRTCSetting mSetting;
    private QNRTCClient mClient;
    private String mRoomToken;

    private QNTrack mLocalVideoTrack;
    private QNTrack mLocalAudioTrack;

    private boolean mIsVideoUnpublished = false;
    private boolean mIsAudioUnpublished = false;

    private Button mBtnRecord;
    private ClientPcmRecorder mClientPcmRecorder;
    private ChatRoomFragment chatRoomFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLocalVideoSurfaceView = findViewById(R.id.local_video_surface_view);
        mRemoteVideoSurfaceView = findViewById(R.id.remote_video_surface_view);
        mRemoteVideoSurfaceView.setZOrderOnTop(true);// QNSurfaceView 是 SurfaceView 的子类, 会受层级影响

        Intent intent = getIntent();
        mRoomToken = intent.getStringExtra("roomToken");

        mSetting = new QNRTCSetting();

        // 配置默认摄像头 ID，此处配置为前置摄像头
        mSetting.setCameraID(QNRTCSetting.QNCameraFacing.FRONT);

        // 相机预览分辨率、帧率配置为 640x480、20fps
        mSetting.setCameraPreviewFormat(new QNVideoFormat(640, 480, 30));

        // 初始化 QNRTC
        QNRTC.init(getApplicationContext(), mSetting, this);

        // 创建本地 Camera 采集 track
        if (mLocalVideoTrack == null) {
            QNCameraVideoTrackConfig cameraVideoTrackConfig = new QNCameraVideoTrackConfig(TAG_CAMERA)
                    .setVideoEncodeFormat(new QNVideoFormat(640, 480, 30))
                    .setBitrate(1200);
            mLocalVideoTrack = QNRTC.createCameraVideoTrack(cameraVideoTrackConfig);
        }
        // 设置预览窗口
        mLocalVideoTrack.play(mLocalVideoSurfaceView);

        // 创建本地音频采集 track
        if (mLocalAudioTrack == null) {
            QNMicrophoneAudioTrackConfig microphoneAudioTrackConfig = new QNMicrophoneAudioTrackConfig(TAG_MICROPHONE)
                    .setBitrate(100);
            mLocalAudioTrack = QNRTC.createMicrophoneAudioTrack(microphoneAudioTrackConfig);
        }

        // 创建 QNRTCClient
        mClient = QNRTC.createClient(this);
        mClient.join(mRoomToken, new QNJoinResultCallback() {
            @Override
            public void onJoined() {
                Log.i(TAG, "join success");
                // 保证房间内只有2人
                if (mClient.getRemoteUsers().size() > 1) {
                    Toast.makeText(RoomActivity.this, "You can't enter the room.", Toast.LENGTH_SHORT).show();
                    QNRTC.deinit();
                    finish();
                }

                // 加入房间成功后发布音视频数据，发布成功会触发 QNPublishResultCallback#onPublished 回调
                mClient.publish(new QNPublishResultCallback() {
                    @Override
                    public void onPublished(List<QNTrack> list) {
                        Log.i(TAG, "onPublished");
                    }

                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        Log.i(TAG, "publish failed : " + errorCode + " " + errorMessage);
                    }
                }, mLocalVideoTrack, mLocalAudioTrack);
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.i(TAG, "join failed : " + errorCode + " " + errorMessage);
            }
        });

        chatRoomFragment = new ChatRoomFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flCover,chatRoomFragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 需要及时销毁 QNRTCEngine 以释放资源
        QNRTC.deinit();
    }

    /**
     * 房间状态改变时会回调此方法
     * 房间状态回调只需要做提示用户，或者更新相关 UI； 不需要再做加入房间或者重新发布等其他操作！
     * @param state 房间状态，可参考 {@link QNConnectionState}
     */
    @Override
    public void onConnectionStateChanged(QNConnectionState state) {
        switch (state) {
            case IDLE:
                // 初始化状态
                Log.i(TAG, "IDLE");
                break;
            case CONNECTING:
                // 正在连接
                Log.i(TAG, "CONNECTING");
                break;
            case CONNECTED:
                // 连接成功，即加入房间成功
                Log.i(TAG, "CONNECTED");
                break;
            case RECONNECTING:
                // 正在重连，若在通话过程中出现一些网络问题则会触发此状态
                Log.i(TAG, "RECONNECTING");
                break;
            case RECONNECTED:
                // 重连成功
                Log.i(TAG, "RECONNECTED");
                break;
        }
    }

    /**
     * 当退出房间执行完毕后触发该回调，可用于切换房间
     */
    @Override
    public void onLeft() {
        Log.i(TAG, "onLeft");
    }

    /**
     * 远端用户加入房间时会回调此方法
     * @see QNRTCClient#join(String, String, QNJoinResultCallback) 可指定 userData 字段
     *
     * @param remoteUserId 远端用户的 userId
     * @param userData 透传字段，用户自定义内容
     */
    @Override
    public void onUserJoined(String remoteUserId, String userData) {
        Log.i(TAG, "onUserJoined : " + remoteUserId);
    }

    /**
     * 远端用户开始重连时会回调此方法
     *
     * @param remoteUserId 远端用户 ID
     */
    @Override
    public void onUserReconnecting(String remoteUserId) {
        Log.i(TAG, "onUserReconnecting : " + remoteUserId);
    }

    /**
     * 远端用户重连成功时会回调此方法
     *
     * @param remoteUserId 远端用户 ID
     */
    @Override
    public void onUserReconnected(String remoteUserId) {
        Log.i(TAG, "onUserReconnected : " + remoteUserId);
    }

    /**
     * 远端用户离开房间时会回调此方法
     *
     * @param remoteUserId 远端离开用户的 userId
     */
    @Override
    public void onUserLeft(String remoteUserId) {
        Log.i(TAG, "onUserLeft : " + remoteUserId);
    }

    /**
     * 远端用户 tracks 成功发布时会回调此方法
     *
     * @param remoteUserId 远端用户 userId
     * @param trackList 远端用户发布的 tracks 列表
     */
    @Override
    public void onUserPublished(String remoteUserId, List<QNTrack> trackList) {
        Log.i(TAG, "onUserPublished : " + remoteUserId);
    }

    /**
     * 远端用户 tracks 成功取消发布时会回调此方法
     *
     * @param remoteUserId 远端用户 userId
     * @param trackList 远端用户取消发布的 tracks 列表
     */
    @Override
    public void onUserUnpublished(String remoteUserId, List<QNTrack> trackList) {
        Log.i(TAG, "onUserUnpublished : " + remoteUserId);
        for (QNTrack track : trackList) {
            if (TAG_CAMERA.equals(track.getTag())) {
                // 当远端视频取消发布时隐藏远端窗口
                mRemoteVideoSurfaceView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 成功订阅远端用户的 tracks 时会回调此方法
     *
     * @param remoteUserId 远端用户 userId
     * @param trackList 订阅的远端用户 tracks 列表
     */
    @Override
    public void onSubscribed(String remoteUserId, List<QNTrack> trackList) {
        Log.i(TAG, "onSubscribed : " + remoteUserId);
        // 筛选出视频 Track 以渲染到窗口
        for (QNTrack track : trackList) {
            if (TAG_CAMERA.equals(track.getTag())) {
                // 设置渲染窗口
                track.play(mRemoteVideoSurfaceView);
                // 成功订阅后显示远端窗口
                mRemoteVideoSurfaceView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 当音频路由发生变化时会回调此方法
     *
     * @param qnAudioDevice 音频设备, 详情请参考{@link QNAudioDevice}
     */
    @Override
    public void onPlaybackDeviceChanged(QNAudioDevice qnAudioDevice) {
        Log.i(TAG, "onPlaybackDeviceChanged : " + qnAudioDevice.name());
    }

    /**
     * 系统相机出错时会触发此回调
     *
     * @param errorCode 错误码
     * @param errorMessage 错误原因
     */
    @Override
    public void onCameraError(int errorCode, String errorMessage) {
        Log.i(TAG, "onCameraError : " + errorCode + " " + errorMessage);
    }

    public void clickPublishVideo(View view) {
        ImageButton button = (ImageButton) view;

        if (mIsVideoUnpublished) {
            mClient.publish(new QNPublishResultCallback() {
                @Override
                public void onPublished(List<QNTrack> list) {
                    Log.i(TAG, "local video track published");
                }

                @Override
                public void onError(int errorCode, String errorMessage) {
                    Log.i(TAG, "publish failed : " + errorCode + " " + errorMessage);
                }
            }, mLocalVideoTrack);
        } else {
            mClient.unpublish(mLocalVideoTrack);
        }
        mIsVideoUnpublished = !mIsVideoUnpublished;
        button.setImageDrawable(mIsVideoUnpublished ? getResources().getDrawable(R.mipmap.video_close) : getResources().getDrawable(R.mipmap.video_open));
    }

    public void clickPublishAudio(View view) {
        ImageButton button = (ImageButton) view;

        if (mIsAudioUnpublished) {
            mClient.publish(new QNPublishResultCallback() {
                @Override
                public void onPublished(List<QNTrack> list) {
                    Log.i(TAG, "local audio track published");
                }

                @Override
                public void onError(int errorCode, String errorMessage) {
                    Log.i(TAG, "publish failed : " + errorCode + " " + errorMessage);
                }
            }, mLocalAudioTrack);
        } else {
            mClient.unpublish(mLocalAudioTrack);
        }
        mIsAudioUnpublished = !mIsAudioUnpublished;
        button.setImageDrawable(mIsAudioUnpublished ? getResources().getDrawable(R.mipmap.microphone_disable) : getResources().getDrawable(R.mipmap.microphone));
    }

    public void clickSwitchCamera(View view) {
        final ImageButton button = (ImageButton) view;

        // 切换摄像头
        QNRTC.switchCamera(new QNCameraSwitchResultCallback() {
            @Override
            public void onCameraSwitchDone(final boolean isFrontCamera) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setImageDrawable(isFrontCamera ? getResources().getDrawable(R.mipmap.camera_switch_front) : getResources().getDrawable(R.mipmap.camera_switch_end));
                    }
                });
            }

            @Override
            public void onCameraSwitchError(String errorMessage) {

            }
        });
    }

    public void clickHangUp(View view) {
        // 离开房间
        mClient.leave();
        // 释放资源
        QNRTC.deinit();
        finish();
    }

    public synchronized void clickToggleRecordAudio(View view) {
        if (mClientPcmRecorder == null) {
            String pcmRecordingPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/test_record_pcm.pcm";
            mClientPcmRecorder = new ClientPcmRecorder(mClient, pcmRecordingPath);
            if (!mClientPcmRecorder.startRecordAudio()) {
                mClientPcmRecorder = null;
                Toast.makeText(this, "房间录音失败", Toast.LENGTH_SHORT).show();
            } else {
                mBtnRecord.setText("停止录音");
                Toast.makeText(this, "房间录音开始", Toast.LENGTH_SHORT).show();
            }
        } else {
            mClientPcmRecorder.stopAudioRecord();
            mClientPcmRecorder = null;
            Toast.makeText(this, "房间录音结束", Toast.LENGTH_SHORT).show();
            mBtnRecord.setText("开始录音");
        }
    }


    private static class ClientPcmRecorder {

        private QNRTCClient client;
        private String dstPath = null;
        private File pcmFile = null;
        private BufferedOutputStream bos = null;
        private QNAudioFrameListener listener = new QNAudioFrameListener() {
            @Override
            public void onAudioFrameAvailable(QNAudioFrame audioFrame) {
                writeAudioData(audioFrame.buffer, audioFrame.size, audioFrame.format.getBitsPerSample(), audioFrame.format.getSampleRate(), audioFrame.format.getChannels());

            }
        };

        private ClientPcmRecorder(QNRTCClient client, String dstPath) {
            this.client = client;
            this.dstPath = dstPath;
        }

        public boolean startRecordAudio() {
            try {
                pcmFile = new File(dstPath);
                if (pcmFile.exists()) {
                    pcmFile.delete();
                }
                bos = new BufferedOutputStream(new FileOutputStream(pcmFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                bos = null;
                pcmFile = null;
                return false;
            }
            client.addAllAudioDataMixedListener(listener);
            return true;
        }

        public void stopAudioRecord() {
            client.removeAllAudioDataMixedListener(listener);
            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bos = null;
            pcmFile = null;
        }

        private void writeAudioData(ByteBuffer audioData, int size, int bitsPerSample, int sampleRate, int numberOfChannels) {
            try {
                Log.d(TAG, "client pcm recording --> size="+size + ",bitsPerSample="+bitsPerSample + ",sampleRate=" + sampleRate + ",numberOfChannels="+numberOfChannels);
                bos.write(audioData.array(), 0, size);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
