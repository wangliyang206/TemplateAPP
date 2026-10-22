package com.template.app.data.api

import com.template.app.data.model.ArticleDetail
import com.template.app.data.model.ArticleDetailParams
import com.template.app.data.model.ArticleListParams
import com.template.app.data.model.ArticleListResponse
import com.template.app.data.model.Banner
import com.template.app.data.model.BannerListParams
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.app.data.model.CheckUpdateParams
import com.template.app.data.model.LoginParams
import com.template.app.data.model.Message
import com.template.app.data.model.MessageListParams
import com.template.app.data.model.RankArticle
import com.template.app.data.model.RankListParams
import com.template.app.data.model.RefreshTokenParams
import com.template.app.data.model.RegisterParams
import com.template.app.data.model.UpdateInfo
import com.template.app.data.model.User
import com.template.core.network.ApiRequest
import com.template.core.network.BaseResponse
import com.template.core.network.TokenAuthenticator
import retrofit2.http.Body
import retrofit2.http.Headers
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
     *
     * [TokenAuthenticator.SKIP_AUTH_HEADER] 标记本请求不参与 401 重放：
     * 否则刷新接口自身返回 401 时会再次触发刷新逻辑，造成递归。
     */
    @Headers(TokenAuthenticator.SKIP_AUTH_HEADER + ": true")
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

    /** 文章详情：正文 + 点赞/评论计数 */
    @POST("article/detail")
    suspend fun getArticleDetail(@Body request: ApiRequest<ArticleDetailParams>): BaseResponse<ArticleDetail>

    // ===== 发现模块 =====
    /** 热门排行：一次返回固定条数，不需要分页 */
    @POST("article/rank")
    suspend fun getRankList(@Body request: ApiRequest<RankListParams>): BaseResponse<List<RankArticle>>

    // ===== 消息模块 =====
    @POST("message/list")
    suspend fun getMessageList(@Body request: ApiRequest<MessageListParams>): BaseResponse<List<Message>>
}
