package com.qiniu.qndroidimsdk.room

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.qiniu.bzuicomp.bottominput.RoomInputDialog
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.qndroidimsdk.R
import com.qiniu.qndroidimsdk.UserInfoManager
import com.qiniu.qndroidimsdk.pubchat.InputMsgReceiver
import im.floo.BMXDataCallBack
import im.floo.floolib.BMXConversation
import im.floo.floolib.BMXConversationList
import im.floo.floolib.BMXErrorCode
import im.floo.floolib.BMXMessageList
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatRoomFragment : Fragment() {


    private val mInputMsgReceiver by lazy {
        InputMsgReceiver(requireContext())
    }

//    private val mWelComeReceiver by lazy {
//        WelComeReceiver()
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(mInputMsgReceiver)
        lifecycle.addObserver(pubChatView)
        buttonSend.setOnClickListener {
            RoomInputDialog()
                .apply {
                    sendPubTextCall = { msgEdit ->
                        mInputMsgReceiver.buildMsg(msgEdit)
                    }
                    sendImgCall = {
                        mInputMsgReceiver.buildImgMsg(it)
                    }
                    sendVoiceCall = {
                        mInputMsgReceiver.buildVoiceMsg(it)
                    }
                }
                .show(childFragmentManager, "RoomInputDialog")


//            QNIMClient.getChatManager().getAllConversations(object :
//                BMXDataCallBack<BMXConversationList> {
//                override fun onResult(p0: BMXErrorCode?, p1: BMXConversationList) {
//                    for (i in 0..p1.size()){
//
//                    }
//                }
//            })

            QNIMClient.getChatManager().openConversation(UserInfoManager.mIMGroup!!.im_group_id,
                BMXConversation.Type.Group,
                false,
                object : BMXDataCallBack<BMXConversation> {
                    override fun onResult(p0: BMXErrorCode?, p1: BMXConversation?) {
                        QNIMClient.getChatManager().retrieveHistoryMessages(p1, 0, 100,
                            object : BMXDataCallBack<BMXMessageList> {
                                override fun onResult(p0: BMXErrorCode?, msgs: BMXMessageList?) {
                                    val size =msgs?.size()?:return;
                                    Log.d("mjl","retrieveHistoryMessages"+size)
                                    if(size<=0){
                                        return
                                    }
                                    for (i in 0 until size.toInt()) {
                                        val msg =  msgs[i]
                                        msg.fromId() //来自谁
                                        msg.attachment()
                                        msg.content()
                                    }
                                }
                            })
                    }

                }
            )
        }

        Log.d(
            "mRoomName",
            " 加入聊天  groupInfo.im_group_id  " + UserInfoManager.mIMGroup!!.im_group_id
        )
        QNIMClient.getChatRoomManager().join(
            UserInfoManager.mIMGroup!!.im_group_id
        ) { p0 ->
            if (p0 == BMXErrorCode.NoError || p0 == BMXErrorCode.GroupMemberExist) {
                //  "加入聊天室成功".asToast()
                Log.d("mRoomName", " 加入聊天  加入聊天室成功  " + UserInfoManager.mIMGroup!!.im_group_id)
                lifecycleScope.launch {
                    delay(1000)
                    mInputMsgReceiver.sendEnterMsg()
                }
            } else {
                Log.d("mRoomName", " 加入聊天    加入聊天室失  " + UserInfoManager.mIMGroup!!.im_group_id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mInputMsgReceiver.sendQuitMsg()
        QNIMClient.getChatRoomManager().leave(UserInfoManager.mIMGroup!!.im_group_id) {
        }
    }
}