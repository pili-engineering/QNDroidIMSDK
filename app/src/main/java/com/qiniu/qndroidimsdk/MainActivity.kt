package com.qiniu.qndroidimsdk

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alibaba.fastjson.JSON
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.network.RetrofitManager
import com.qiniu.qndroidimsdk.mode.HttpDate
import com.qiniu.qndroidimsdk.mode.IMGroup
import com.qiniu.qndroidimsdk.pubchat.JsonUtils
import com.qiniu.qndroidimsdk.room.RoomActivity
import com.qiniu.qndroidimsdk.util.PermissionChecker
import com.qiniu.qndroidimsdk.util.Utils
import com.uuzuche.lib_zxing.activity.CaptureActivity
import com.uuzuche.lib_zxing.activity.CodeUtils
import im.floo.BMXDataCallBack
import im.floo.floolib.BMXErrorCode
import im.floo.floolib.BMXGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.wait
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    var token1 =
        "QxZugR8TAhI38AiJ_cptTl3RbzLyca3t-AAiH-Hh:GwzC49F8kBR4uyfSVYNODT2dPGM=:eyJhcHBJZCI6ImQ4ZHJlOHcxcCIsImV4cGlyZUF0IjoxNjI3NjU1MzgyLCJwZXJtaXNzaW9uIjoidXNlciIsInJvb21OYW1lIjoicXdlMTIiLCJ1c2VySWQiOiIxMjMifQ=="
    var token2 =
        "QxZugR8TAhI38AiJ_cptTl3RbzLyca3t-AAiH-Hh:Tj2or5C0y3suQ_WXqnZUB7BtrXU=:eyJhcHBJZCI6ImQ4ZHJlOHcxcCIsImV4cGlyZUF0IjoxNjI3NzQxODE5LCJwZXJtaXNzaW9uIjoidXNlciIsInJvb21OYW1lIjoicXdlMTIiLCJ1c2VySWQiOiJzeGRhc2QifQ=="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.buttonToken1).setOnClickListener { v ->
            room_token_edit_text.setText(token1)
            joinRoom(v)
        }
        findViewById<View>(R.id.buttonToken2).setOnClickListener { v ->
            room_token_edit_text.setText(token2)
            joinRoom(v)
        }
    }

    fun joinRoom(view: View?) {
        // 在进入房间前，必须有相对应的权限，在 Android 6.0 后除了在 Manifest 文件中声明外还需要动态申请权限。
        if (!isPermissionOK) {
            Toast.makeText(this, "Some permissions is not approved !!!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!TextUtils.isEmpty(room_token_edit_text!!.text)) {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val roomToken = room_token_edit_text!!.text
                    val tokens: Array<String> = roomToken.split(":".toRegex()).toTypedArray()
                    if (tokens.size != 3) {
                        return@launch
                    }
                    val roomInfo: String = Utils.base64Decode(tokens[2])

                    val json = JSONObject(roomInfo)
                    val mRoomName = json.optString("roomName")

                    val body =
                        "{\"group_id\":\"${mRoomName}\"}".toRequestBody("application/json".toMediaType())
                    val response = async<Response>(Dispatchers.IO) {
                        val response =
                            RetrofitManager.post("https://im-test.qiniuapi.com/v1/mock/group", body)
                        response
                    }.await()

                    val jsonStr = response.body?.string()
                    Log.d("mRoomName", "   jsonStr  " + jsonStr)


                    if (response.code == 200) {
                        val json = JsonUtils.parseObject(jsonStr, HttpDate::class.java)?.data
                        UserInfoManager.mIMGroup = json
                        Log.d("mRoomName", "   groupInfo.im_group_id  " + json?.group_id)
                        val intent = Intent(this@MainActivity, RoomActivity::class.java)
                        intent.putExtra("roomToken", room_token_edit_text!!.text.toString())
                        startActivity(intent)
                    } else {
                        "创建房间失败".asToast()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                }
            }
        }
    }

    fun clickToScanQRCode(view: View?) {
        // 扫码也用到了相机权限
        if (!isPermissionOK) {
            Toast.makeText(this, "Some permissions is not approved !!!", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, CaptureActivity::class.java)
        startActivityForResult(intent, QRCODE_RESULT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            // 处理扫描结果
            if (null != data) {
                val bundle = data.extras ?: return
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    val result = bundle.getString(CodeUtils.RESULT_STRING)
                    room_token_edit_text!!.setText(result)
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(this, "解析二维码失败", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val isPermissionOK: Boolean
        private get() {
            val checker = PermissionChecker(this)
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission()
        }

    companion object {
        private const val QRCODE_RESULT_REQUEST_CODE = 1
    }
}