package com.qiniu.qndroidimsdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.network.RetrofitManager
import com.qiniu.qndroidimsdk.mode.IMUser
import im.floo.BMXDataCallBack
import im.floo.floolib.BMXErrorCode
import im.floo.floolib.BMXUserProfile
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViewData()
    }

    fun signUpNewUser(name: String, pwd: String) {
        val mBMXUserProfile = BMXUserProfile()
        val signCode = QNIMClient.signUpNewUser(name, pwd, mBMXUserProfile)
        if (signCode == BMXErrorCode.NoError) {
            val reLoginCode = QNIMClient.signInByName(name, pwd)
            if (reLoginCode == BMXErrorCode.NoError) {
                UserInfoManager.updateUserInfo(IMUser().apply {
                    im_password = pwd
                    im_username = name
                    Log.d("signUpNewUser", "mBMXUserProfile.userId() " + mBMXUserProfile.userId())
                    im_uid = mBMXUserProfile.userId()
                })
                val i = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(i)
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "im登录失败 ${reLoginCode.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@LoginActivity,
                "im登录失败 ${signCode.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun initViewData() {
        bt_login_login.setOnClickListener {
            var phone = et_login_phone.text.toString() ?: ""
            var pwd = et_login_verification_code.text.toString() ?: ""
            if (phone.isEmpty()) {
                "请输入用户名".asToast()
                return@setOnClickListener
            }
            if (pwd.isEmpty()) {
                "请输入密码".asToast()
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val code = QNIMClient.signInByName(phone, pwd)
                    if (code == BMXErrorCode.NoError) {
                        val mBMXUserProfile = BMXUserProfile()
                        QNIMClient.getUserManager().getProfile(
                            false
                        ) { p0, p1 ->
                            if (p0 == BMXErrorCode.NoError) {
                                UserInfoManager.updateUserInfo(IMUser().apply {
                                    im_password = pwd
                                    im_username = phone
                                    im_uid = p1.userId()
                                })
                            }
                        }
                        val i = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(i)
                    } else {
                        if (BMXErrorCode.UserAuthFailed == code) {
                            signUpNewUser(phone, pwd)
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "im登录失败 ${code.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                }
            }
        }
    }
}