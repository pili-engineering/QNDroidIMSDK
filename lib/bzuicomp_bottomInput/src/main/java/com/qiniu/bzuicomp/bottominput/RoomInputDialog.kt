package com.qiniu.bzuicomp.bottominput

import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import kotlinx.android.synthetic.main.dialog_room_input.*


/**
 * 底部输入框
 */
class RoomInputDialog(var type: Int = type_text) : FinalDialogFragment() {

    companion object {
        const val type_text = 1
        const val type_danmu = 2
    }

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    /**
     * 发消息拦截回调
     */
    var sendPubCall: ((msg: String) -> Unit)? = null

    // private val faceFragment by lazy { FaceFragment() }
    private val mSoftKeyBoardListener by lazy { SoftKeyBoardListener(requireActivity()) }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_room_input
    }

    private var inputType = 0

    override fun init() {
        //表情暂时没写
        chat_message_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.toString()?.isEmpty() == true) {
                    send_btn.visibility = View.GONE
                } else {
                    send_btn.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        chat_message_input.setOnClickListener {
            hideFace()
        }
        chat_message_input.requestFocus()
        chat_message_input.post {
            SoftInputUtil.showSoftInputView(chat_message_input)
        }
        face_btn.setOnClickListener {
            checkShowFace()
        }
        mSoftKeyBoardListener.setOnSoftKeyBoardChangeListener(object :
            SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
            override fun keyBoardShow(height: Int) {
                hideFace()
            }

            override fun keyBoardHide(height: Int) {
            }
        })

        send_btn.setOnClickListener {
            val msgEdit = chat_message_input.text.toString()
            sendPubCall?.invoke(msgEdit)
            dismiss()
        }
        emojiBoard.setItemClickListener { code ->
            if (code == "/DEL") {
                chat_message_input.dispatchKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL
                    )
                )
            } else {
                chat_message_input.getText()?.insert(chat_message_input.selectionStart, code)
            }
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mSoftKeyBoardListener.detach()
    }

    private fun checkShowFace() {
        if (inputType == 0) {
            showFace()
        } else {
            hideFace()
        }
    }

    private fun hideFace() {
        emojiBoard.visibility = View.GONE
        inputType = 0
        face_btn.isSelected = false
    }

    private fun showFace() {
        SoftInputUtil.hideSoftInputView(chat_message_input)
        emojiBoard.visibility = View.VISIBLE
        inputType = 1
        face_btn.isSelected = true

    }
}