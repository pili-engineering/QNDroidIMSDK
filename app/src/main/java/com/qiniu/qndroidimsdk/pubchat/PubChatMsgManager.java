package com.qiniu.qndroidimsdk.pubchat;

public class PubChatMsgManager {


    protected static IChatMsgCall iChatMsgCall = null;

    public static void onNewMsg(IChatMsg msg) {
        if (iChatMsgCall != null) {
            iChatMsgCall.onNewMsg(msg);
        }
    }

    protected interface IChatMsgCall {
        public void onNewMsg(IChatMsg msg);
    }
}
