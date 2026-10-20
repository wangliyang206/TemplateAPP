package com.template.app.ui.home

import com.template.app.data.api.ApiService
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
import com.template.app.testing.MainDispatcherRule
import com.template.core.network.ApiRequest
import com.template.core.network.ApiRequestFactory
import com.template.core.network.BaseResponse
import com.template.core.network.ErrorInfo
import com.template.core.network.TokenStore
import com.template.core.state.UiMessage
import com.template.core.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeTokenStore : TokenStore {
        override suspend fun currentToken(): String = "test-token"
        override suspend fun updateToken(token: String) = Unit
        override suspend fun clearToken() = Unit
    }

    /** 只实现首页用到的两个接口，其余调用直接失败，防止测试里误用 */
    private class FakeApiService(
        private val banners: List<Banner> = emptyList(),
        private val categories: List<Category> = emptyList(),
        private val businessError: ErrorInfo? = null,
    ) : ApiService {

        override suspend fun getBannerList(request: ApiRequest<BannerListParams>): BaseResponse<List<Banner>> =
            BaseResponse(1, businessError, if (businessError == null) banners else null)

        override suspend fun getCategoryList(request: ApiRequest<CategoryListParams>): BaseResponse<List<Category>> =
            BaseResponse(1, businessError, if (businessError == null) categories else null)

        override suspend fun login(request: ApiRequest<LoginParams>): BaseResponse<User> = notUsed()
        override suspend fun register(request: ApiRequest<RegisterParams>): BaseResponse<User> = notUsed()
        override suspend fun refreshToken(request: ApiRequest<RefreshTokenParams>): BaseResponse<User> = notUsed()
        override suspend fun checkUpdate(request: ApiRequest<CheckUpdateParams>): BaseResponse<UpdateInfo> = notUsed()
        override suspend fun getArticleList(request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse> =
            notUsed()

        private fun notUsed(): Nothing = error("该接口不应在 HomeViewModel 测试中被调用")
    }

    private fun repository(service: ApiService) = HomeRepository(service, ApiRequestFactory(FakeTokenStore()))

    /** 等状态离开 Idle/Loading（Repository 内部切了真实 IO 线程，不能用虚拟时间推进） */
    private suspend fun awaitSettled(viewModel: HomeViewModel): UiState<HomeData> =
        viewModel.state.first { it !is UiState.Idle && it !is UiState.Loading }

    @Test
    fun `加载成功后进入 Success 并带回数据`() = runTest {
        val viewModel = HomeViewModel(
            repository(
                FakeApiService(
                    banners = listOf(Banner(1, "标题", "https://img", "https://link")),
                    categories = listOf(Category(1, "推荐")),
                ),
            ),
        )

        val state = awaitSettled(viewModel)
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(1, data.banners.size)
        assertEquals(1, data.categories.size)
    }

    @Test
    fun `业务错误时进入 Error 且携带服务端文案`() = runTest {
        val viewModel = HomeViewModel(
            repository(FakeApiService(businessError = ErrorInfo(-1001, "服务开小差了"))),
        )

        val state = awaitSettled(viewModel)
        assertTrue(state is UiState.Error)
        assertEquals(UiMessage.Text("服务开小差了"), (state as UiState.Error).message)
    }

    @Test
    fun `空数据时成功返回空集合，由 UI 决定展示空态`() = runTest {
        val viewModel = HomeViewModel(repository(FakeApiService()))

        val data = (awaitSettled(viewModel) as UiState.Success).data
        assertTrue(data.isEmpty)
    }
}
