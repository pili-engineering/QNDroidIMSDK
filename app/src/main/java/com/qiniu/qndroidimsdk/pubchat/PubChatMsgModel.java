package com.qiniu.qndroidimsdk.pubchat;

import com.alibaba.fastjson.annotation.JSONField;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class PubChatMsgModel implements IChatMsg, Serializable {

    public static String action_pubText="pubChatText";
    private String senderId;
    private String senderName;
    private String msgContent;
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public String getMsgContent() {
        return msgContent;
    }
    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    @JSONField(serialize = false)
    @NotNull
    @Override
    public String getMsgHtml() {
        return   "<font color='#ffb83c'>"+  " :"+msgContent+"</font>";
    }
    @JSONField(serialize = false)
    @NotNull
    @Override
    public String getMsgAction() {
        return action_pubText;
    }
}
