package com.qiniu.qndroidimsdk.pubchat.msg;

import android.net.Uri;

import im.floo.floolib.BMXMessage;

public class RtmImgMsg extends RtmMessage{


    public Uri thumbnailUrl;
    public double w;
    public double h;


    /**
     * 获得消息类型
     */
    @Override
    public MsgType getMsgType() {
        return MsgType.MsgTypeImg;
    }

    /**
     * 获得信令码
     */
    @Override
    public String getAction() {
        return "";
    }


}
