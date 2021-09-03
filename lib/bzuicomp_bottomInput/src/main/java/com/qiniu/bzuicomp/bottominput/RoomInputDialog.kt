package com.qiniu.bzuicomp.bottominput

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.loader.content.CursorLoader
import com.hapi.mediapicker.ContentUriUtil
import com.hapi.mediapicker.ImagePickCallback
import com.hapi.mediapicker.PicPickHelper
import kotlinx.android.synthetic.main.dialog_room_input.*
import java.io.File
import java.lang.Exception


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
    var sendPubTextCall: ((msg: String) -> Unit)? = null
    var sendImgCall :((url: Uri)->Unit)?=null
    var sendVoiceCall:((url: String)->Unit)?=null

    // private val faceFragment by lazy { FaceFragment() }
    private val mSoftKeyBoardListener by lazy { SoftKeyBoardListener(requireActivity()) }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_room_input
    }

    private var inputType = 0
    private var mVoiceRecorder: VoiceRecorder? = null
    private var lastTime = 0L
    @SuppressLint("ClickableViewAccessibility")
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
            hideMore()
            hideVoice()
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
            sendPubTextCall?.invoke(msgEdit)
            dismiss()
        }

        voice_input_switch.setOnClickListener {
            if (voice_input_switch.isSelected) {
                hideVoice()
            } else {
                SoftInputUtil.hideSoftInputView(chat_message_input)
                hideFace()
                hideMore()
                showVoice()
            }
            voice_input_switch.isSelected = !voice_input_switch.isSelected
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

        more_btn.setOnClickListener {
            if (more_btn.isSelected) {
                hideMore()
            } else {
                SoftInputUtil.hideSoftInputView(chat_message_input)
                hideFace()
                showMore()
            }
            more_btn.isSelected = !more_btn.isSelected
        }


        btnSendImg.setOnClickListener {
            PicPickHelper(activity!!).fromLocal(null, object : ImagePickCallback {
                override fun onSuccess(result: String?, url: Uri?) {
                    url?.let {
                        sendImgCall?.invoke(url)
                    }
                    Log.d("mjl", "${result} ${url.toString()}")
                }
            })
        }

        btnTakePhoto.setOnClickListener {

        }

        chat_voice_input.setOnTouchListener { v, event ->
            fun stop() {
                Log.d("mjl", "stop recorder")
                mVoiceRecorder?.let {
                    if ((System.currentTimeMillis() - lastTime) < 1000) {
                        Toast.makeText(context, "录音时间太短了", Toast.LENGTH_SHORT).show()
                    } else {
                        val path = it.stopRecord()
                        if(path.isNotEmpty()){

                            sendVoiceCall?.invoke( path)
                        }
                        Log.d("mjl", "path ${path}")
                    }
                }
                mVoiceRecorder = null
            }

            fun start() {
                Log.d("mjl", "start recorder")
                mVoiceRecorder = VoiceRecorder()
                mVoiceRecorder?.startRecord(this@RoomInputDialog.context)
                lastTime = System.currentTimeMillis()
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    start()
                MotionEvent.ACTION_UP -> {
                    stop()
                }
                MotionEvent.ACTION_CANCEL -> {
                    stop()
                }
            }
            false
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

    private fun showMore() {
        llMore.visibility = View.VISIBLE
    }

    private fun hideMore() {
        llMore.visibility = View.GONE
    }

    private fun showVoice() {
        chat_message_input.visibility = View.GONE
        chat_voice_input.visibility = View.VISIBLE
        face_btn.visibility = View.GONE
        more_btn.visibility = View.GONE
        send_btn.visibility = View.GONE
    }

    private fun hideVoice() {
        chat_message_input.visibility = View.VISIBLE
        chat_voice_input.visibility = View.GONE
        face_btn.visibility = View.VISIBLE
        more_btn.visibility = View.VISIBLE
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

    fun getAbsoluteImagePath(activity: Activity, contentUri: Uri): String {

        //如果是对媒体文件，在android开机的时候回去扫描，然后把路径添加到数据库中。
        //由打印的contentUri可以看到：2种结构。正常的是：content://那么这种就要去数据库读取path。
        //另外一种是Uri是 file:///那么这种是 Uri.fromFile(File file);得到的
        println(contentUri)
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var urlpath: String?
        val loader = CursorLoader(activity!!, contentUri, projection, null, null, null)
        val cursor = loader.loadInBackground()
        try {
            val column_index = cursor!!.getColumnIndex(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            urlpath = cursor.getString(column_index)
            //如果是正常的查询到数据库。然后返回结构
            return urlpath
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: handle exception
        } finally {
            cursor?.close()
        }

        //如果是文件。Uri.fromFile(File file)生成的uri。那么下面这个方法可以得到结果
        urlpath = contentUri.path
        return urlpath?:""
    }
}