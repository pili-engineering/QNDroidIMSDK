package com.qiniu.qndroidimsdk.pubchat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.qndroidimsdk.UserInfoManager
import com.qiniu.qndroidimsdk.pubchat.msg.RtmTextMsg
import im.floo.floolib.BMXChatServiceListener
import im.floo.floolib.BMXMessage
import im.floo.floolib.BMXMessageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WelComeReceiver : LifecycleObserver {

    private val mChatListener: BMXChatServiceListener = object : BMXChatServiceListener() {

        override fun onReceive(list: BMXMessageList) {
            //收到消息
            for (i in 0 until list.size().toInt()) {
                list[i]?.let {
                    val msg = JsonUtils.parseObject(it.content(), RtmTextMsg::class.java)
                        ?: return
                    if (msg.action == PubChatWelCome.action_welcome) {
                        val pubChatMsgModel =
                            JsonUtils.parseObject(msg.msgStr, PubChatWelCome::class.java)
                                ?: return
                        GlobalScope.launch(Dispatchers.Main) {
                            PubChatMsgManager.onNewMsg(pubChatMsgModel)
                        }
                    }
                    if (msg.action == PubChatQuitRoom.action_quit_room) {
                        val pubChatMsgModel =
                            JsonUtils.parseObject(msg.msgStr, PubChatQuitRoom::class.java)
                                ?: return
                        GlobalScope.launch(Dispatchers.Main) {
                            PubChatMsgManager.onNewMsg(pubChatMsgModel)
                        }
                    }
                }
            }
        }
    }

    fun sendEnterMsg() {
        val pubMsg = PubChatWelCome().apply {
            senderId = UserInfoManager.mIMUser?.im_uid.toString()
            senderName = UserInfoManager.mIMUser?.im_username
            msgContent = "进入了房间"
        }
        val msg = RtmTextMsg(
            PubChatWelCome.action_welcome, JsonUtils.toJson(
                pubMsg
            )
        )
        val imMsg = BMXMessage.createMessage(
            UserInfoManager.mIMUser!!.im_uid,
            UserInfoManager.mIMGroup!!.im_group_id,
            BMXMessage.MessageType.Group,
            UserInfoManager.mIMGroup!!.im_group_id,
            JsonUtils.toJson(msg)
        )
        QNIMClient.sendMessage(imMsg)
        PubChatMsgManager.onNewMsg(pubMsg)
    }

    fun sendQuitMsg() {
        val pubMsg = PubChatQuitRoom().apply {
            senderId = UserInfoManager.mIMUser?.im_uid.toString()
            senderName = UserInfoManager.mIMUser?.im_username
            msgContent = "退出了房间"
        }
        val msg = RtmTextMsg(
            PubChatQuitRoom.action_quit_room, JsonUtils.toJson(
                pubMsg
            )
        )
        val imMsg = BMXMessage.createMessage(
            UserInfoManager.mIMUser!!.im_uid,
            UserInfoManager.mIMGroup!!.im_group_id,
            BMXMessage.MessageType.Group,
            UserInfoManager.mIMGroup!!.im_group_id,
            JsonUtils.toJson(msg)
        )
        QNIMClient.sendMessage(imMsg)
        PubChatMsgManager.onNewMsg(pubMsg)
    }

    init {
        QNIMClient.getChatManager().addChatListener(mChatListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        QNIMClient.getChatManager().removeChatListener(mChatListener)
    }

}