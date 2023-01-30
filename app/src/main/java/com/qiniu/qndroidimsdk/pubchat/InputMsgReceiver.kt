package com.qiniu.qndroidimsdk.pubchat


import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.qndroidimsdk.pubchat.msg.RtmTextMsg
import im.floo.floolib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.qiniu.qndroidimsdk.pubchat.msg.RtmImgMsg
import com.qiniu.qndroidimsdk.pubchat.msg.RtmMessage
import com.qiniu.qndroidimsdk.pubchat.msg.RtmVoiceMsg
import java.lang.Exception
import android.util.Log
import androidx.core.content.FileProvider
import com.hapi.mediapicker.ContentUriUtil
import com.qiniu.qndroidimsdk.UserInfoManager
import java.io.File


class InputMsgReceiver(val context: Context) : LifecycleObserver {


    private val mChatListener: BMXChatServiceListener = object : BMXChatServiceListener() {

        override fun onStatusChanged(msg: BMXMessage?, error: BMXErrorCode?) {
            super.onStatusChanged(msg, error)
        }

        override fun onAttachmentStatusChanged(
            msg: BMXMessage?,
            error: BMXErrorCode?,
            percent: Int
        ) {
            super.onAttachmentStatusChanged(msg, error, percent)
            val id = msg?.msgId()
            Log.d(
                "mjl",
                " onAttachmentStatusChanged ${id}  ${percent}"
            )
            GlobalScope.launch(Dispatchers.Main) {
                PubChatMsgManager.onMsgAttachmentStatusChanged(id.toString(), percent)
            }
        }

        override fun onReceive(list: BMXMessageList) {
            super.onReceive(list)

            //收到消息
            if (list.isEmpty) {
                return
            }
            for (i in 0 until list.size().toInt()) {
                list[i]?.let {
                    var msg: RtmMessage? = null

                    if (it.contentType() == BMXMessage.ContentType.Text) {
                        msg = JsonUtils.parseObject(it.content(), RtmTextMsg::class.java) ?: return
                    }
                    if (it.contentType() == BMXMessage.ContentType.Image) {
                        QNIMClient.getChatManager().downloadAttachment(it)
                        val at: BMXImageAttachment =
                            BMXImageAttachment.dynamic_cast(it.attachment())
                        val rtmImgMsg = RtmImgMsg()
                        val path = at.path()
                        rtmImgMsg.thumbnailUrl = Uri.fromFile(File(path))
                        rtmImgMsg.h = at.size().mHeight
                        rtmImgMsg.w = at.size().mWidth
                        rtmImgMsg.attachmentProcess = 0
                        msg = rtmImgMsg;
                    }

                    if (it.contentType() == BMXMessage.ContentType.Voice) {
                        QNIMClient.getChatManager().downloadAttachment(it)
                        val at: BMXVoiceAttachment =
                            BMXVoiceAttachment.dynamic_cast(it.attachment())
                        val rtmVoiceMsg = RtmVoiceMsg()

                        val path = at.path()
                        val t3 = at.url()
                        val file = File(path)
                        Log.d("mjl", "${it.msgId()} ${path} ${t3} ${path}\n" + file.exists())
                        rtmVoiceMsg.url = Uri.fromFile(File(path))
                        rtmVoiceMsg.duration = at.duration();
                        msg = rtmVoiceMsg
                        msg.attachmentProcess = 0
                    }

                    msg?.msgId = it.msgId().toString()
                    msg?.sendImId = it.fromId().toString()
                    msg?.sendImName = it.senderName()
                    msg?.toImId = it.toId().toString()

                    GlobalScope.launch(Dispatchers.Main) {
                        PubChatMsgManager.onNewMsg(msg)
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
            senderId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
            senderName = UserInfoManager.mLoginToken?.imConfig?.imUsername
            msgContent = msgEdit
        }
        val msg = RtmTextMsg(
            PubChatMsgModel.action_pubText, JsonUtils.toJson(
                pubChatMsgModel
            )
        )
        val imMsg = BMXMessage.createMessage(
            UserInfoManager.mLoginToken!!.imConfig.imUid!!.toLong(),
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            BMXMessage.MessageType.Group,
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            JsonUtils.toJson(msg)
        )
        imMsg.setSenderName(UserInfoManager.mLoginToken?.imConfig?.imUsername)
        QNIMClient.sendMessage(imMsg)
        msg.sendImName = UserInfoManager.mLoginToken?.imConfig?.imUsername
        msg.toImId = UserInfoManager.mLoginToken?.imConfig?.imGroupId.toString()
        msg.sendImId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
        PubChatMsgManager.onNewMsg(msg)
    }

    fun buildImgMsg(uri: Uri) {

        val filePath = ContentUriUtil.getDataFromUri(
            context!!,
            uri,
            ContentUriUtil.ContentType.image
        ) ?: ""
        val bm = BitmapFactory.decodeFile(filePath)
        val size =
            BMXMessageAttachment.Size(bm?.width?.toDouble() ?: 0.0, bm?.height?.toDouble() ?: 0.0)
        val imageAttachment = BMXImageAttachment(filePath, size)

        val msg: BMXMessage = BMXMessage.createMessage(
            UserInfoManager.mLoginToken!!.imConfig.imUid!!.toLong(),
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            BMXMessage.MessageType.Group,
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(), imageAttachment
        )
        msg.setSenderName(UserInfoManager.mLoginToken?.imConfig?.imUsername)
        QNIMClient.sendMessage(msg)

        val rtmImgMsg = RtmImgMsg()
        rtmImgMsg.thumbnailUrl = uri
        rtmImgMsg.h = size.mHeight
        rtmImgMsg.w = size.mWidth

        rtmImgMsg.sendImName = UserInfoManager.mLoginToken?.imConfig?.imUsername
        rtmImgMsg.toImId = UserInfoManager.mLoginToken?.imConfig?.imGroupId.toString()
        rtmImgMsg.sendImId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
        rtmImgMsg.attachmentProcess = 100
        PubChatMsgManager.onNewMsg(rtmImgMsg)
    }

    private fun getRingDuring(mUri: Uri): String {
        var duration: String? = null
        val mmr = MediaMetadataRetriever()
        try {
            if (mUri != null) {
                var headers: HashMap<String?, String?>? = null
                if (headers == null) {
                    headers = HashMap()
                    headers["User-Agent"] =
                        "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1"
                }
                mmr.setDataSource(context, mUri) // videoPath 本地视频的路径
            }
            duration = ((mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()
                ?: 0) / 1000).toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            mmr.release()
        }
        return duration ?: "0"
    }

    fun buildVoiceMsg(filePath: String) {

        val uri = FileProvider.getUriForFile(
            context,
            context.getPackageName().toString() + ".fileProvider",
            File(filePath)
        )
        val duration = getRingDuring(uri).toInt()
        val imageAttachment = BMXVoiceAttachment(filePath, duration)
        val msg: BMXMessage = BMXMessage.createMessage(
            UserInfoManager.mLoginToken!!.imConfig.imUid!!.toLong(),
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            BMXMessage.MessageType.Group,
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(), imageAttachment
        )
        msg.setSenderName(UserInfoManager.mLoginToken?.imConfig?.imUsername)
        QNIMClient.sendMessage(msg)
        val rtmVoiceMsg = RtmVoiceMsg()
        rtmVoiceMsg.url = uri
        rtmVoiceMsg.duration = duration
        rtmVoiceMsg.sendImName = UserInfoManager.mLoginToken?.imConfig?.imUsername
        rtmVoiceMsg.toImId = UserInfoManager.mLoginToken?.imConfig?.imGroupId.toString()
        rtmVoiceMsg.sendImId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
        rtmVoiceMsg.attachmentProcess = 100
        PubChatMsgManager.onNewMsg(rtmVoiceMsg)

    }


    fun sendEnterMsg() {
        val pubMsg = PubChatWelCome().apply {
            senderId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
            senderName = UserInfoManager.mLoginToken?.imConfig?.imUsername
            msgContent = "进入了房间"
        }
        val msg = RtmTextMsg(
            PubChatWelCome.action_welcome, JsonUtils.toJson(
                pubMsg
            )
        )
        val imMsg = BMXMessage.createMessage(
            UserInfoManager.mLoginToken!!.imConfig.imUid!!.toLong(),
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            BMXMessage.MessageType.Group,
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            JsonUtils.toJson(msg)
        )
        imMsg.setSenderName(UserInfoManager.mLoginToken?.imConfig?.imUsername)
        QNIMClient.sendMessage(imMsg)
        msg.sendImName = UserInfoManager.mLoginToken?.imConfig?.imUsername
        msg.toImId = UserInfoManager.mLoginToken?.imConfig?.imGroupId.toString()
        msg.sendImId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
        PubChatMsgManager.onNewMsg(msg)
    }

    fun sendQuitMsg() {
        val pubMsg = PubChatQuitRoom().apply {
            senderId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
            senderName = UserInfoManager.mLoginToken?.imConfig?.imUsername
            msgContent = "退出了房间"
        }
        val msg = RtmTextMsg(
            PubChatQuitRoom.action_quit_room, JsonUtils.toJson(
                pubMsg
            )
        )
        val imMsg = BMXMessage.createMessage(
            UserInfoManager.mLoginToken!!.imConfig.imUid!!.toLong(),
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            BMXMessage.MessageType.Group,
            UserInfoManager.mLoginToken!!.imConfig.imGroupId.toLong(),
            JsonUtils.toJson(msg)
        )
        imMsg.setSenderName(UserInfoManager.mLoginToken?.imConfig?.imUsername)
        QNIMClient.sendMessage(imMsg)
        msg.sendImName = UserInfoManager.mLoginToken?.imConfig?.imUsername
        msg.toImId = UserInfoManager.mLoginToken?.imConfig?.imGroupId.toString()
        msg.sendImId = UserInfoManager.mLoginToken?.imConfig?.imUid.toString()
        PubChatMsgManager.onNewMsg(msg)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        QNIMClient.getChatManager().removeChatListener(mChatListener)
    }
}