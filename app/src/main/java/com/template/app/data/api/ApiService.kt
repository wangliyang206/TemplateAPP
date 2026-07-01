package com.template.app.data.api

import com.template.core.network.BaseResponse
import com.template.app.data.model.UpdateInfo
import com.template.app.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("user/login")
    suspend fun login(@Body params: Map<String, String>): BaseResponse<User>

    @POST("user/register")
    suspend fun register(@Body params: Map<String, String>): BaseResponse<User>

    @GET("system/checkUpdate")
    suspend fun checkUpdate(@Query("versionCode") versionCode: Int): BaseResponse<UpdateInfo>
}
