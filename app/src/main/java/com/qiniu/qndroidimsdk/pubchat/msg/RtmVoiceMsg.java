package com.qiniu.qndroidimsdk.pubchat.msg;

import android.net.Uri;

import im.floo.floolib.BMXMessage;

public class RtmVoiceMsg extends RtmMessage {


    public Uri url;
    public int duration;

    /**
     * 获得消息类型
     */
    @Override
    public MsgType getMsgType() {
        return MsgType.MsgTypeVoice;
    }

    /**
     * 获得信令码
     */
    @Override
    public String getAction() {
        return "";
    }


}