package com.qiniu.qndroidimsdk

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import com.qiniu.bzcomp.network.QiniuJsonFactor
import com.qiniu.bzcomp.network.QiniuRequestInterceptor
import com.qiniu.droid.imsdk.QNIMClient
import com.qiniu.network.Form2JsonInterceptor
import com.qiniu.network.NetConfig
import com.qiniu.network.RetrofitManager
import com.qiniu.qndroidimsdk.util.AppCache
import com.uuzuche.lib_zxing.activity.ZXingLibrary
import im.floo.floolib.*
import java.io.File

class App : Application() {

    companion object {
        init {
            System.loadLibrary("floo")
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppCache.setContext(this)
        AppContextUtils.initApp(this)
        val appPath = AppContextUtils.getAppContext().filesDir.path
        val dataPath = File("$appPath/data_dir")
        val cachePath = File("$appPath/cache_dir")
        dataPath.mkdirs()
        cachePath.mkdirs()

        // 配置sdk config
        val config = BMXSDKConfig(
            BMXClientType.Android, "1", dataPath.absolutePath,
            cachePath.absolutePath, "MaxIM"
        )
        config.consoleOutput = true
        config.logLevel = BMXLogLevel.Debug
        config.appID ="cigzypnhoyno"
        config.setEnvironmentType(BMXPushEnvironmentType.Production)

        // 初始化BMXClient
        QNIMClient.init(config)
        UserInfoManager.init()
        QiniuRequestInterceptor.appVersionName = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_CONFIGURATIONS
        ).versionName
        RetrofitManager.resetConfig(NetConfig().apply {
            base = "https://niucube-api.qiniu.com"
            converterFactory = QiniuJsonFactor.create()
            okBuilder.addInterceptor(QiniuRequestInterceptor())
                .addInterceptor(Form2JsonInterceptor())
                .addInterceptor(logInterceptor)
        })

// 初始化使用的第三方二维码扫描库，与 QNRTC 无关，请忽略
        ZXingLibrary.initDisplayOpinion(applicationContext)
    }
}