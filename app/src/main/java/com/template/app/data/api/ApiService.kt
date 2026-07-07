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

    // ===== 列表分页示例 =====
    @GET("article/list")
    suspend fun getArticleList(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResponse<ArticleListResponse>

    // ===== 文件上传示例 =====
    @Multipart
    @POST("file/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): BaseResponse<String>
}
