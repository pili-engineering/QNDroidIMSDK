package com.qiniu.qndroidimsdk.pubchat

import android.annotation.SuppressLint
import android.text.Html
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qiniu.qndroidimsdk.R
import kotlinx.android.synthetic.main.bzui_item_pub_chat.view.*

class PubChatAdapter  : BaseQuickAdapter<IChatMsg, BaseViewHolder>(R.layout.bzui_item_pub_chat,ArrayList<IChatMsg>()){

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: IChatMsg) {
       holder.itemView.tvText.text = Html.fromHtml(item.getMsgHtml(), null, null);
    }

}