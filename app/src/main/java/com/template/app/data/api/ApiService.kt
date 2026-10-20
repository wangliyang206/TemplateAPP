package com.template.app.data.api

import com.template.app.data.model.ArticleListParams
import com.template.app.data.model.ArticleListResponse
import com.template.app.data.model.Banner
import com.template.app.data.model.BannerListParams
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.app.data.model.CheckUpdateParams
import com.template.app.data.model.LoginParams
import com.template.app.data.model.RefreshTokenParams
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
    @POST("member/login")
    suspend fun login(@Body request: ApiRequest<LoginParams>): BaseResponse<User>

    @POST("member/register")
    suspend fun register(@Body request: ApiRequest<RegisterParams>): BaseResponse<User>

    /**
     * 刷新 Token：401 时由 TokenAuthenticator 自动调用。
     * 后端若没有该接口，删除此方法并在 TokenRefresher 中改为其他策略（如静默重登）。
     */
    @POST("member/refreshToken")
    suspend fun refreshToken(@Body request: ApiRequest<RefreshTokenParams>): BaseResponse<User>

    // ===== 系统模块 =====
    @POST("system/checkUpdate")
    suspend fun checkUpdate(@Body request: ApiRequest<CheckUpdateParams>): BaseResponse<UpdateInfo>

    // ===== 首页模块 =====
    @POST("banner/list")
    suspend fun getBannerList(@Body request: ApiRequest<BannerListParams>): BaseResponse<List<Banner>>

    @POST("category/list")
    suspend fun getCategoryList(@Body request: ApiRequest<CategoryListParams>): BaseResponse<List<Category>>

    // ===== 列表分页示例 =====
    @POST("article/list")
    suspend fun getArticleList(@Body request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse>
}
