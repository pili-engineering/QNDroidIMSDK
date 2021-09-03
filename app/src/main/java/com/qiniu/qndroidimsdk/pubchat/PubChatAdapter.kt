package com.qiniu.qndroidimsdk.pubchat

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.text.Html
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qiniu.qndroidimsdk.R
import com.qiniu.qndroidimsdk.pubchat.msg.*
import com.qiniu.qndroidimsdk.util.AudioPlayer
import kotlinx.android.synthetic.main.bzui_item_pub_chat.view.*

class PubChatAdapter : BaseQuickAdapter<RtmMessage, BaseViewHolder>(
    R.layout.bzui_item_pub_chat,
    ArrayList<RtmMessage>()
) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: RtmMessage) {
        holder.itemView.tvText.visibility = View.GONE
        holder.itemView.ivImgAttach.visibility = View.GONE
        holder.itemView.llVoice.visibility = View.GONE
        holder.itemView.tvName.text = item.sendImName
        if(item.attachmentProcess>=100 || item.attachmentProcess<0){
            holder.itemView.loading_progress.visibility= View.GONE
        }else{
            holder.itemView.loading_progress.visibility= View.VISIBLE
        }
        when (item.msgType) {
            MsgType.MsgTypeText -> {
                holder.itemView.tvText.visibility = View.VISIBLE
                val msg = item as RtmTextMsg
                val text = when (msg.action) {
                    (PubChatMsgModel.action_pubText) -> {
                        JsonUtils.parseObject(msg.msgStr, PubChatMsgModel::class.java)?.getMsgHtml()
                            ?: ""
                    }
                    PubChatWelCome.action_welcome ->
                        JsonUtils.parseObject(msg.msgStr, PubChatWelCome::class.java)?.getMsgHtml()
                            ?: ""

                    PubChatQuitRoom.action_quit_room ->
                        JsonUtils.parseObject(msg.msgStr, PubChatQuitRoom::class.java)?.getMsgHtml()
                            ?: ""
                    else -> {
                        ""
                    }
                }
                holder.itemView.tvText.text = Html.fromHtml(text, null, null);
            }

            MsgType.MsgTypeVoice -> {
                holder.itemView.ivVoiceAnim.setImageResource(R.drawable.play_voice_message);
                holder.itemView.llVoice.visibility = View.VISIBLE
                holder.itemView.tvVoiceDuration.text = (item as RtmVoiceMsg).duration.toString()+"'"
                holder.itemView.ivVoiceAnim.setOnClickListener {
                    val animationDrawable: AnimationDrawable? =
                        holder.itemView.ivVoiceAnim.getDrawable() as AnimationDrawable?
                    animationDrawable?.start()
                    AudioPlayer.getInstance().startPlay(mContext,(item as RtmVoiceMsg).url) {
                        holder.itemView.ivVoiceAnim.post {
                            animationDrawable?.stop()
                            holder.itemView.ivVoiceAnim.setImageResource(R.drawable.play_voice_message);
                        }
                    }
                }
            }

            MsgType.MsgTypeImg -> {
                holder.itemView.ivImgAttach.visibility = View.VISIBLE
                Glide.with(mContext)
                    .load((item as RtmImgMsg).thumbnailUrl)
                    .into(holder.itemView.ivImgAttach)
            }
        }

    }

}