package com.qiniu.qndroidimsdk.service

import com.qiniu.qndroidimsdk.mode.LoginToken
import retrofit2.http.*

interface LoginService {

    @FormUrlEncoded
    @POST("/v1/getSmsCode")
    suspend fun sendSmsCode(
        @Field("phone") phone: String
    )

    @FormUrlEncoded
    @POST("/v1/signUpOrIn")
    suspend fun login(@Field("phone") phone: String, @Field("smsCode") smsCode: String): LoginToken


    @FormUrlEncoded
    @POST("/v1/signInWithToken")
    suspend fun signInWithToken(@Field("phone") phone: String): LoginToken


    @POST("/v1/signOut")
    suspend fun signOut()
}