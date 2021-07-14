package com.qiniu.qndroidimsdk

import android.text.TextUtils
import com.qiniu.qndroidimsdk.mode.IMGroup
import com.qiniu.qndroidimsdk.mode.IMUser
import com.qiniu.qndroidimsdk.mode.LoginToken
import com.qiniu.qndroidimsdk.mode.UserInfo
import com.qiniu.qndroidimsdk.pubchat.JsonUtils
import com.qiniu.qndroidimsdk.util.SPConstant
import com.qiniu.qndroidimsdk.util.SpUtil


object UserInfoManager {
    var mIMUser: IMUser? = null
    var mIMGroup: IMGroup? = null

    private var uid = ""

    fun init() {
        mIMUser = JsonUtils.parseObject(
            SpUtil.get(SPConstant.User.SpName)?.readString(
                SPConstant.User.KEY_USER_INFO
            ) ?: "",
            IMUser::class.java
        ) as IMUser?
        uid = mIMUser?.im_uid?.toString() ?: ""
    }

    /**
     * 快捷获取　uid
     */
    fun getUserId(): String {
        return uid
    }

    fun updateUserInfo(userInfo: IMUser) {
        uid = mIMUser?.im_uid?.toString() ?: ""
        mIMUser = userInfo
        saveUserInfoToSp()
    }

    //存sp
    private fun saveUserInfoToSp() {
        mIMUser?.let {
            SpUtil.get(SPConstant.User.SpName)
                .saveData(SPConstant.User.KEY_USER_INFO, JsonUtils.toJson(it))
        }
    }

    fun onLogout(toastStr: String = "") {}

    fun clearUser() {
        SpUtil.get(SPConstant.User.SpName).clear()
        uid = ""
        mIMUser = null
    }
}