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
import com.qiniu.qndroidimsdk.pubchat.PubChatAdapter
import com.qiniu.qndroidimsdk.pubchat.WelComeReceiver
import im.floo.floolib.BMXChatServiceListener
import im.floo.floolib.BMXErrorCode
import im.floo.floolib.BMXMessageList
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatRoomFragment : Fragment() {

    private val mInputMsgReceiver by lazy {
        InputMsgReceiver()
    }

    private val mWelComeReceiver by lazy {
        WelComeReceiver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        pubChatView.setAdapter(PubChatAdapter())
        lifecycle.addObserver(mWelComeReceiver)
        lifecycle.addObserver(mInputMsgReceiver)
        lifecycle.addObserver(pubChatView)

        buttonSend.setOnClickListener {
            RoomInputDialog()
                .apply {
                    sendPubCall = { msgEdit ->
                        mInputMsgReceiver.buildMsg(msgEdit)
                    }
                }
                .show(childFragmentManager, "RoomInputDialog")
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
                    mWelComeReceiver.sendEnterMsg()
                }
            } else {
                Log.d("mRoomName", " 加入聊天    加入聊天室失  " + UserInfoManager.mIMGroup!!.im_group_id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mWelComeReceiver.sendQuitMsg()
        QNIMClient.getChatRoomManager().leave(UserInfoManager.mIMGroup!!.im_group_id) {
        }
    }
}