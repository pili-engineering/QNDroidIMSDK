package com.qiniu.qndroidimsdk.service

import com.qiniu.qndroidimsdk.mode.UserInfo
import okhttp3.MultipartBody
import retrofit2.http.*

interface UserService {

    @GET("/v1/accountInfo/{accountId}")
    suspend fun getUserInfo(@Path("accountId") accountId: String): UserInfo

    @FormUrlEncoded
    @POST("/v1/accountInfo/{accountId}")
    suspend fun editUserInfo(
        @Path("accountId") accountId: String,
        @Field("nickname") nickname: String
    ): Any

    @FormUrlEncoded
    @POST("/v1/accountInfo/{accountId}")
    suspend fun editUserAvatar(
        @Path("accountId") accountId: String,
        @Field("avatar") avatar: String
    ): Any
}