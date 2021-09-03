package com.qiniu.qndroidimsdk.pubchat.msg;

import im.floo.floolib.BMXMessage;

abstract public class RtmMessage {

    /**
     * 获得消息类型
     */
    abstract public MsgType getMsgType();

    /**
     * 获得信令码
     */
    abstract public String getAction();

    public String msgId;
    public String sendImId;
    public String sendImName;
    public String toImId;
    public int attachmentProcess=-1;

}
