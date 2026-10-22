package com.template.core.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 登录态失效广播的两条不变量（ADR-0006）：
 * 1. `expire()` 必须把 token 清空 —— token 是登录态的唯一权威；
 * 2. `expire()` 必须广播一次 expired —— 否则 Application 层不会跳登录页，用户会卡在"401 但页面还在"。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private class FakeTokenStore(var token: String = "token-1") : TokenStore {
        var clearCount = 0
        override suspend fun currentToken(): String = token
        override suspend fun updateToken(token: String) {
            this.token = token
        }

        override suspend fun clearToken() {
            clearCount++
            token = ""
        }
    }

    @Test
    fun `expire 会清空 token 并广播一次失效事件`() = runTest {
        val store = FakeTokenStore()
        val manager = SessionManager(store)
        val events = mutableListOf<Unit>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.expired.collect { events += it }
        }
        runCurrent()

        manager.expire()
        runCurrent()

        assertEquals("", store.token)
        assertEquals(1, store.clearCount)
        assertEquals(1, events.size)

        job.cancel()
    }

    @Test
    fun `未调用 expire 时不广播`() = runTest {
        val manager = SessionManager(FakeTokenStore())
        val events = mutableListOf<Unit>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.expired.collect { events += it }
        }
        runCurrent()
        runCurrent()

        assertEquals(0, events.size)
        job.cancel()
    }
}
