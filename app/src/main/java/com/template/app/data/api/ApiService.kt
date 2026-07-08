package com.template.app.data.api

import com.template.app.data.model.ArticleListParams
import com.template.app.data.model.ArticleListResponse
import com.template.app.data.model.CheckUpdateParams
import com.template.app.data.model.LoginParams
import com.template.app.data.model.RegisterParams
import com.template.app.data.model.UpdateInfo
import com.template.app.data.model.User
import com.template.core.network.ApiRequest
import com.template.core.network.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API 接口定义
 *
 * 所有接口使用统一请求体 ApiRequest<T>，响应使用统一 BaseResponse<T>
 */
interface ApiService {

    // ===== 用户模块 =====
    @POST("user/login")
    suspend fun login(@Body request: ApiRequest<LoginParams>): BaseResponse<User>

    @POST("user/register")
    suspend fun register(@Body request: ApiRequest<RegisterParams>): BaseResponse<User>

    // ===== 系统模块 =====
    @POST("system/checkUpdate")
    suspend fun checkUpdate(@Body request: ApiRequest<CheckUpdateParams>): BaseResponse<UpdateInfo>

    // ===== 列表分页示例 =====
    @POST("article/list")
    suspend fun getArticleList(@Body request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse>

    // ===== 文件上传示例（Multipart 不走统一包装，token/sign 以 @Part 传递） =====
    // @Multipart
    // @POST("file/upload")
    // suspend fun uploadFile(@Part file: MultipartBody.Part): BaseResponse<String>
}
