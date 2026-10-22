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

/**
 * 测试用 ApiService 空实现。
 *
 * 为什么需要它：`ApiService` 每加一个方法，所有测试里的 fake 都要补实现
 * （否则编译不过）。项目里曾有 3 个测试各自维护一份 `notUsed()` 清单，
 * 加一个接口就要改 3 处。改为继承本类后，只需要在需要的地方 override。
 *
 * 约定：没 override 的方法一旦被调用就会抛异常，这样"测试意外打到别的接口"
 * 能立刻暴露，而不是静默返回空数据。
 */
open class StubApiService : ApiService {

    override suspend fun login(request: ApiRequest<LoginParams>): BaseResponse<User> = notUsed()

    override suspend fun register(request: ApiRequest<RegisterParams>): BaseResponse<User> = notUsed()

    override suspend fun refreshToken(request: ApiRequest<RefreshTokenParams>): BaseResponse<User> = notUsed()

    override suspend fun checkUpdate(request: ApiRequest<CheckUpdateParams>): BaseResponse<UpdateInfo> = notUsed()

    override suspend fun getBannerList(request: ApiRequest<BannerListParams>): BaseResponse<List<Banner>> = notUsed()

    override suspend fun getCategoryList(request: ApiRequest<CategoryListParams>): BaseResponse<List<Category>> =
        notUsed()

    override suspend fun getArticleList(request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse> =
        notUsed()

    override suspend fun getArticleDetail(request: ApiRequest<ArticleDetailParams>): BaseResponse<ArticleDetail> =
        notUsed()

    override suspend fun getRankList(request: ApiRequest<RankListParams>): BaseResponse<List<RankArticle>> = notUsed()

    override suspend fun getMessageList(request: ApiRequest<MessageListParams>): BaseResponse<List<Message>> = notUsed()

    /** 未预期的接口调用：直接失败，避免测试静默通过 */
    protected fun notUsed(): Nothing = error("该接口不应在本次测试中被调用")
}
