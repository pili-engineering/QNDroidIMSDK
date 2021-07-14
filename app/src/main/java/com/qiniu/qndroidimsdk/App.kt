package com.qiniu.qndroidimsdk

import android.app.Application
import android.util.Log
import com.qiniu.droid.imsdk.QNIMClient
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

    val mBMXGroupServiceListener = object  : BMXGroupServiceListener() {
        override fun onMemberJoined(group: BMXGroup?, memberId: Long, inviter: Long) {
            super.onMemberJoined(group, memberId, inviter)
        }
    }
    val mBMXChatServiceListener = object : BMXChatServiceListener() {
        override fun onReceive(list: BMXMessageList?) {
            super.onReceive(list)
        }

        override fun onStatusChanged(msg: BMXMessage, error: BMXErrorCode) {
            super.onStatusChanged(msg, error)
            Log.d("mjl","error code"+error.name+"  "+error.swigValue())
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
        config.appID ="dxdjbunzmxiu"
        config.setEnvironmentType(BMXPushEnvironmentType.Production)

        // 初始化BMXClient
        QNIMClient.init(config)
        UserInfoManager.init()
        RetrofitManager.resetConfig(NetConfig().apply {
            base = "https://niucube-api.qiniu.com/"
        })

        QNIMClient.getGroupManager().addGroupListener(mBMXGroupServiceListener)
        QNIMClient.getChatManager().addChatListener(mBMXChatServiceListener)


// 初始化使用的第三方二维码扫描库，与 QNRTC 无关，请忽略
        ZXingLibrary.initDisplayOpinion(applicationContext)
    }
}