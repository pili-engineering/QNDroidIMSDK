package com.qiniu.qndroidimsdk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.network.RetrofitManager
import com.tbruyelle.rxpermissions2.RxPermissions
import im.floo.floolib.BMXErrorCode
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ivAd.setImageResource(R.drawable.spl_bg)
        RxPermissions(this).request(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).doFinally {
            val i = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(i)
        }.subscribe {
        }
    }
}