package com.qiniu.qndroidimsdk.pubchat;

import com.qiniu.qndroidimsdk.pubchat.msg.RtmMessage;

public class PubChatMsgManager {


    protected static IChatMsgCall iChatMsgCall = null;

    public static void onNewMsg(RtmMessage msg) {
        if (iChatMsgCall != null) {
            iChatMsgCall.onNewMsg(msg);
        }
    }
    public static void onMsgAttachmentStatusChanged(String msgId,int percent){
        if (iChatMsgCall != null) {
            iChatMsgCall.onMsgAttachmentStatusChanged(msgId, percent);
        }
    }

    protected interface IChatMsgCall {
        public void onNewMsg(RtmMessage msg);
        public void onMsgAttachmentStatusChanged(String msgId,int percent);
    }
}
