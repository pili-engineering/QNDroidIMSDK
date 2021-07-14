package com.qiniu.qndroidimsdk.pubchat.msg;

import com.alibaba.fastjson.annotation.JSONField;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class RtmTextMsg implements RtmMessage , Serializable {


    private String action="";
    private  String msgStr ="";

    public RtmTextMsg(){}

    public RtmTextMsg(String action, String msgStr) {
        this.action = action;
        this.msgStr = msgStr;
    }



    public void setAction(String action) {
        this.action = action;
    }

    public String getMsgStr() {
        return msgStr;
    }

    public void setMsgStr(String msgStr) {
        this.msgStr = msgStr;
    }

    @JSONField(serialize = false)
    @NotNull
    @Override
    public MsgType getMsgType() {
        return MsgType.MsgTypeText;
    }

    @NotNull
    @Override
    public String getAction() {
        return action;
    }
}
