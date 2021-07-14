package com.qiniu.qndroidimsdk.pubchat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.qndroidimsdk.UserInfoManager
import com.qiniu.qndroidimsdk.pubchat.msg.RtmTextMsg
import im.floo.floolib.BMXMessage
import im.floo.floolib.BMXMessageList

import im.floo.floolib.BMXErrorCode

import im.floo.floolib.BMXChatServiceListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class InputMsgReceiver : LifecycleObserver {

    private val mChatListener: BMXChatServiceListener = object : BMXChatServiceListener() {

        override fun onReceive(list: BMXMessageList) {
            super.onReceive(list)

            //收到消息
            if (list.isEmpty) {
                return
            }
            for (i in 0 until list.size().toInt()) {
                list[i]?.let {
                    val msg =
                        JsonUtils.parseObject(it.content(), RtmTextMsg::class.java) ?: return
                    if (msg.action == PubChatMsgModel.action_pubText) {
                        val pubChatMsgModel =
                            JsonUtils.parseObject(msg.msgStr, PubChatMsgModel::class.java)
                                ?: return
                        GlobalScope.launch(Dispatchers.Main) {
                            PubChatMsgManager.onNewMsg(pubChatMsgModel)

                        }
                    }
                }
            }
        }
        //}
    }


    init {
        QNIMClient.getChatManager().addChatListener(mChatListener)
    }


    /**
     * 发公聊消息
     */
    fun buildMsg(msgEdit: String) {
        val pubChatMsgModel = PubChatMsgModel().apply {
            senderId = UserInfoManager.mIMUser?.im_uid.toString()
            senderName = UserInfoManager.mIMUser?.im_username
            msgContent = msgEdit
        }

        val msg = RtmTextMsg(
            PubChatMsgModel.action_pubText, JsonUtils.toJson(
                pubChatMsgModel
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
        PubChatMsgManager.onNewMsg(pubChatMsgModel)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        QNIMClient.getChatManager().removeChatListener(mChatListener)
    }
}