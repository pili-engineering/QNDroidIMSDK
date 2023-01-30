package com.qiniu.qndroidimsdk

import android.text.TextUtils
import com.qiniu.qndroidimsdk.mode.LoginToken
import com.qiniu.qndroidimsdk.mode.UserInfo
import com.qiniu.qndroidimsdk.pubchat.JsonUtils
import com.qiniu.qndroidimsdk.util.SpUtil

object UserInfoManager {

    private var uid = ""
    private var mUserInfo: UserInfo? = null
    var mLoginToken: LoginToken? = null
        private set

    fun init() {
        mUserInfo = JsonUtils.parseObject(
            SpUtil.get(SpName)?.readString(
                KEY_USER_INFO
            ) ?: "",
            UserInfo::class.java
        ) as UserInfo?
        uid = mUserInfo?.accountId ?: ""

        mLoginToken = JsonUtils.parseObject(
            SpUtil.get(SpName)?.readString(
                KEY_USER_LOGIN_MODEL
            ) ?: "",
            LoginToken::class.java
        ) as LoginToken?
        mLoginToken?.let {
            uid = it.accountId
        }
    }


    fun getUserInfo(): UserInfo? {
        return mUserInfo
    }


    /**
     * 快捷获取　uid
     */
    fun getUserId(): String {
        return uid
    }

    /**
     * 快捷获取　token
     */
    fun getUserToken(): String {
        return mLoginToken?.loginToken ?: ""
    }

    fun updateUserInfo(userInfo: UserInfo) {
        uid = userInfo.accountId
        mUserInfo = userInfo
        saveUserInfoToSp()

    }

    suspend fun updateLoginModel(loginToken: LoginToken) {
        uid = loginToken.accountId
        mLoginToken = loginToken
        saveLoginInfoToSp()

    }

    //存sp
    private fun saveUserInfoToSp() {
        mUserInfo?.let {
            SpUtil.get(SpName)
                .saveData(KEY_USER_INFO, JsonUtils.toJson(it))
        }
    }

    private fun saveLoginInfoToSp() {
        mLoginToken?.let {
            SpUtil.get(SpName)
                .saveData(KEY_USER_LOGIN_MODEL, JsonUtils.toJson(it))
        }
    }

    fun hasLogin(): Boolean {
        return !getUserId().isEmpty() && !TextUtils.isEmpty(
            getUserToken()
        )
    }

    fun clearUser() {
        SpUtil.get(SpName).clear()
        uid = ""
        mUserInfo = null
        mLoginToken = null
    }

    private var SpName = "config:user"
    private val KEY_UID = "uid"
    private val KEY_USER_INFO = "user_info"
    private val KEY_USER_LOGIN_MODEL = "USER_LOGIN_MODEL"
}