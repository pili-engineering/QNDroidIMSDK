package com.qiniu.qndroidimsdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.qiniu.bzcomp.network.QiniuRequestInterceptor
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.network.NetBzException
import com.qiniu.network.RetrofitManager
import com.qiniu.qndroidimsdk.service.LoginService
import com.qiniu.qndroidimsdk.service.UserService
import com.qiniu.qndroidimsdk.util.SpUtil
import im.floo.floolib.BMXErrorCode
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import okio.Buffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViewData()
    }

    private fun initViewData() {
        //登陆按钮
        bt_login_login.setOnClickListener {

            val phone = et_login_phone.text.toString() ?: ""
            val code = et_login_verification_code.text.toString() ?: ""
            if (phone.isEmpty()) {
                Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                LoadingDialog.showLoading(supportFragmentManager)
                try {
                    //demo登陆
                    val uinfo = RetrofitManager.create(LoginService::class.java).login(phone, code)
                    QiniuRequestInterceptor.loginToken = uinfo.loginToken
                    //保存用户信息
                    UserInfoManager.updateLoginModel(uinfo)
                    //保存用户信息
                    val info = RetrofitManager.create(UserService::class.java)
                        .getUserInfo(UserInfoManager.getUserId())
                    UserInfoManager.updateUserInfo(info)
                    SpUtil.get("login").saveData("phone", phone)

                    loginIMSuspend(
                        UserInfoManager.getUserId(),
                        UserInfoManager.mLoginToken!!.imConfig.imUid,
                        UserInfoManager.mLoginToken!!.imConfig.imUsername,
                        UserInfoManager.mLoginToken!!.imConfig.imPassword
                    )
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                } finally {
                    LoadingDialog.cancelLoadingDialog()
                }
            }
        }
        tvSmsTime.setOnClickListener {
            val phone = et_login_phone.text.toString() ?: ""
            if (phone.isEmpty()) {
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    RetrofitManager.create(LoginService::class.java).sendSmsCode(phone)
                    et_login_verification_code.requestFocus()
                    timeJob()
                } catch (e: Exception) {
                    e.message?.asToast()
                    e.printStackTrace()
                }
            }
        }

        val lastPhone = SpUtil.get("login").readString("phone", "")
        if (!TextUtils.isEmpty(lastPhone)) {
            et_login_phone.setText(lastPhone)
        }
    }

    private suspend fun loginIMSuspend(uid: String, loginImUid: String, name: String, pwd: String) =
        suspendCoroutine<BMXErrorCode> { continuation ->
            QNIMClient.getUserManager().signInByName(name, pwd) { p0 ->
                if (p0 == BMXErrorCode.NoError) {
                    continuation.resume(p0)
                } else {
                    continuation.resumeWithException(NetBzException(p0.name))
                }
            }
        }

    private fun timeJob() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                tvSmsTime.isClickable = false
                repeat(60) {
                    tvSmsTime.text = (60 - it).toString()
                    delay(1000)
                }
                tvSmsTime.text = "获取验证码"
                tvSmsTime.isClickable = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}