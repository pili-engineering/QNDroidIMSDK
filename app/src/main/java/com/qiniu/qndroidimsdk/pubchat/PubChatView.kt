package com.qiniu.qndroidimsdk.pubchat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qiniu.qndroidimsdk.R
import com.qiniu.qndroidimsdk.pubchat.msg.RtmMessage
import kotlinx.android.synthetic.main.view_bzui_pubchat.view.*

/**
 * 公屏
 */
class PubChatView : FrameLayout, LifecycleObserver {

    private var adapter: BaseQuickAdapter<RtmMessage, BaseViewHolder> = PubChatAdapter()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.view_bzui_pubchat, this, false)
        addView(view)
        chatRecy.layoutManager = LinearLayoutManager(context)
        chatRecy.adapter = adapter
    }



    fun onRoomLeaving() {
        adapter.data.clear()
        adapter.notifyDataSetChanged()
    }

    private var mIChatMsgCall = object :  PubChatMsgManager.IChatMsgCall {
        override fun onNewMsg(msg: RtmMessage?) {
            adapter.addData(msg!!)
            chatRecy.smoothScrollToPosition(adapter.data.size - 1)
        }

        override fun onMsgAttachmentStatusChanged( msgId:String, percent: Int) {
            adapter.data.forEachIndexed { index, rtmMessage ->
                if(rtmMessage.msgId==msgId){
                    rtmMessage.attachmentProcess=percent
                    if(percent>=100){
                        adapter.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        //   RtmChannelKit.removeRtmChannelListener(channelListener)
        // RoomManager.removeRoomLifecycleMonitor(roomMonitor)
        PubChatMsgManager.iChatMsgCall = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        //    RtmChannelKit.addRtmChannelListener(channelListener)
        // RoomManager.addRoomLifecycleMonitor(roomMonitor)
        PubChatMsgManager.iChatMsgCall = mIChatMsgCall
    }


}