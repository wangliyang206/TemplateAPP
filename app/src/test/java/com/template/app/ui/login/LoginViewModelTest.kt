package com.template.app.ui.login

import app.cash.turbine.test
import com.template.app.data.api.StubApiService
import com.template.app.data.model.LoginParams
import com.template.app.data.model.User
import com.template.app.data.session.SessionRepository
import com.template.app.testing.MainDispatcherRule
import com.template.core.datastore.DataStoreManager
import com.template.core.datastore.SessionStore
import com.template.core.db.UserDao
import com.template.core.db.UserEntity
import com.template.core.network.ApiRequest
import com.template.core.network.ApiRequestFactory
import com.template.core.network.BaseResponse
import com.template.core.network.ErrorInfo
import com.template.core.network.TokenStore
import com.template.core.state.UiMessage
import com.template.core.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * 登录 ViewModel 的纯 JVM 测试。
 *
 * 与 HomeViewModelTest 同一套路：Fake 掉 ApiService / TokenStore，
 * SessionRepository 用内存版 [FakeSessionStore] + [FakeUserDao] 组装真实链路，
 * 因此验证的不只是 ViewModel，还包括「登录 → 写会话」的完整编排。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val successUser = User(
        userId = "u-1",
        userName = "张三",
        userPhone = "13800138000",
        token = "token-abc",
    )

    private class FakeTokenStore : TokenStore {
        override suspend fun currentToken(): String = ""
        override suspend fun updateToken(token: String) = Unit
        override suspend fun clearToken() = Unit
    }

    /** 内存版会话存储：记录 saveUser/clearUser 的最终状态，供断言 */
    private class FakeSessionStore : SessionStore {
        private val pref = MutableStateFlow(DataStoreManager.UserPref("", "", "", "", ""))
        override val userFlow: Flow<DataStoreManager.UserPref> = pref

        override suspend fun saveUser(
            userId: String,
            token: String,
            userName: String,
            phone: String,
            avatar: String?,
        ) {
            pref.value = DataStoreManager.UserPref(
                userId = userId,
                token = token,
                userName = userName,
                phone = phone,
                avatar = avatar.orEmpty(),
            )
        }

        override suspend fun clearUser() {
            pref.value = DataStoreManager.UserPref("", "", "", "", "")
        }
    }

    private class FakeUserDao : UserDao {
        val rows = mutableListOf<UserEntity>()
        override suspend fun insert(user: UserEntity) {
            rows.removeAll { it.userId == user.userId }
            rows.add(user)
        }
        override suspend fun getById(userId: String): UserEntity? = rows.firstOrNull { it.userId == userId }
        override suspend fun getAll(): List<UserEntity> = rows.toList()
        override suspend fun deleteById(userId: String) {
            rows.removeAll { it.userId == userId }
        }
        override suspend fun clear() = rows.clear()
    }

    /** 只有 login 可用；businessError 非空时返回业务错误，测试中途可改以验证重试。inner 为访问外部的 successUser */
    private inner class FakeApiService(initialError: ErrorInfo? = null) : StubApiService() {
        var businessError: ErrorInfo? = initialError
        override suspend fun login(request: ApiRequest<LoginParams>): BaseResponse<User> =
            BaseResponse(1, businessError, if (businessError == null) successUser else null)
    }

    private class LoginStack(val viewModel: LoginViewModel, val api: FakeApiService, val sessionStore: FakeSessionStore)

    private fun buildStack(initialError: ErrorInfo? = null): LoginStack {
        val sessionStore = FakeSessionStore()
        val api = FakeApiService(initialError)
        val repository = LoginRepository(
            apiService = api,
            requestFactory = ApiRequestFactory(FakeTokenStore()),
            sessionRepository = SessionRepository(sessionStore, FakeUserDao()),
        )
        return LoginStack(LoginViewModel(repository), api, sessionStore)
    }

    /** 等状态离开 Idle/Loading（Repository 内部切了真实 IO 线程，不能用虚拟时间推进） */
    private suspend fun awaitSettled(viewModel: LoginViewModel): UiState<User> =
        viewModel.loginState.first { it !is UiState.Idle && it !is UiState.Loading }

    @Test
    fun `登录成功进入 Success 并写入会话`() = runTest {
        val stack = buildStack()
        stack.viewModel.login("13800138000", "123456")

        val state = awaitSettled(stack.viewModel)
        assertTrue(state is UiState.Success)
        assertEquals(successUser.token, (state as UiState.Success).data.token)
        assertEquals(successUser.token, stack.sessionStore.userFlow.first().token)
    }

    @Test
    fun `业务错误进入 Error 并携带服务端文案，不写会话`() = runTest {
        val stack = buildStack(ErrorInfo(-1001, "账号或密码错误"))
        stack.viewModel.login("13800138000", "wrong")

        val state = awaitSettled(stack.viewModel)
        assertTrue(state is UiState.Error)
        assertEquals(UiMessage.Text("账号或密码错误"), (state as UiState.Error).message)
        assertEquals("", stack.sessionStore.userFlow.first().token)
    }

    @Test
    fun `失败后重试可恢复为 Success`() = runTest {
        val stack = buildStack(ErrorInfo(-1001, "账号或密码错误"))
        stack.viewModel.login("13800138000", "wrong")
        assertTrue(awaitSettled(stack.viewModel) is UiState.Error)

        // 注意不能用 awaitSettled：StateFlow 会立刻命中上一次的 Error 旧值，
        // 这里必须等待「新产生的 Success」
        stack.api.businessError = null
        stack.viewModel.login("13800138000", "123456")
        val state = stack.viewModel.loginState.first { it is UiState.Success }
        assertEquals(successUser.token, (state as UiState.Success).data.token)
    }

    @Test
    fun `状态流转 Idle 到 Loading 到 Success`() = runTest {
        val stack = buildStack()
        stack.viewModel.loginState.test {
            assertEquals(UiState.Idle, awaitItem())
            stack.viewModel.login("13800138000", "123456")
            assertEquals(UiState.Loading, awaitItem())
            assertTrue(awaitItem() is UiState.Success)
        }
    }
}
