package com.qiniu.qndroidimsdk.pubchat


import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.qndroidimsdk.UserInfoManager
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
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.hapi.mediapicker.ContentUriUtil
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
            var id = msg?.msgId()
            Log.d(
                "mjl",
                " onAttachmentStatusChanged ${id}  ${percent}"
            )
            GlobalScope.launch(Dispatchers.Main) {
                PubChatMsgManager.onMsgAttachmentStatusChanged(id.toString(),percent)
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

                    //当前这个群
                    if (it.toId() != UserInfoManager.mIMGroup!!.im_group_id) {
                        return
                    }

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
                        msg?.attachmentProcess=0
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
        imMsg.setSenderName(UserInfoManager.mIMUser?.im_username)
        QNIMClient.sendMessage(imMsg)
        msg.sendImName = UserInfoManager.mIMUser?.im_username
        msg.toImId = UserInfoManager.mIMGroup!!.im_group_id.toString()
        msg.sendImId = UserInfoManager.mIMUser!!.im_uid.toString()
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
            UserInfoManager.mIMUser!!.im_uid,
            UserInfoManager.mIMGroup!!.im_group_id,
            BMXMessage.MessageType.Group,
            UserInfoManager.mIMGroup!!.im_group_id, imageAttachment
        )
        msg.setSenderName(UserInfoManager.mIMUser?.im_username)
        QNIMClient.sendMessage(msg)

        val rtmImgMsg = RtmImgMsg()
        rtmImgMsg.thumbnailUrl = uri
        rtmImgMsg.h = size.mHeight
        rtmImgMsg.w = size.mWidth

        rtmImgMsg.sendImName = UserInfoManager.mIMUser?.im_username
        rtmImgMsg.toImId = UserInfoManager.mIMGroup!!.im_group_id.toString()
        rtmImgMsg.sendImId = UserInfoManager.mIMUser!!.im_uid.toString()
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
            UserInfoManager.mIMUser!!.im_uid,
            UserInfoManager.mIMGroup!!.im_group_id,
            BMXMessage.MessageType.Group,
            UserInfoManager.mIMGroup!!.im_group_id, imageAttachment
        )
        msg.setSenderName(UserInfoManager.mIMUser?.im_username)
        QNIMClient.sendMessage(msg)
        val rtmVoiceMsg = RtmVoiceMsg()
        rtmVoiceMsg.url = uri
        rtmVoiceMsg.duration = duration
        rtmVoiceMsg.sendImName = UserInfoManager.mIMUser?.im_username
        rtmVoiceMsg.toImId = UserInfoManager.mIMGroup!!.im_group_id.toString()
        rtmVoiceMsg.sendImId = UserInfoManager.mIMUser!!.im_uid.toString()
        rtmVoiceMsg.attachmentProcess = 100
        PubChatMsgManager.onNewMsg(rtmVoiceMsg)

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
        imMsg.setSenderName(UserInfoManager.mIMUser?.im_username)
        QNIMClient.sendMessage(imMsg)
        msg.sendImName = UserInfoManager.mIMUser?.im_username
        msg.toImId = UserInfoManager.mIMGroup!!.im_group_id.toString()
        msg.sendImId = UserInfoManager.mIMUser!!.im_uid.toString()
        PubChatMsgManager.onNewMsg(msg)
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
        imMsg.setSenderName(UserInfoManager.mIMUser?.im_username)
        QNIMClient.sendMessage(imMsg)
        msg.sendImName = UserInfoManager.mIMUser?.im_username
        msg.toImId = UserInfoManager.mIMGroup!!.im_group_id.toString()
        msg.sendImId = UserInfoManager.mIMUser!!.im_uid.toString()
        PubChatMsgManager.onNewMsg(msg)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        QNIMClient.getChatManager().removeChatListener(mChatListener)
    }
}