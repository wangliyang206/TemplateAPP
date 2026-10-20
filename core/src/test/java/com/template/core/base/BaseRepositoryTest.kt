package com.template.core.base

import com.template.core.error.AppError
import com.template.core.error.AppException
import com.template.core.network.ApiResult
import com.template.core.network.BaseResponse
import com.template.core.network.ErrorInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class BaseRepositoryTest {

    private class TestRepository : BaseRepository() {
        suspend fun <T> call(block: suspend () -> ApiResult<T>): T = apiCall(block)
        suspend fun <T> safe(block: suspend () -> BaseResponse<T>): ApiResult<T> = safeApi(block)
    }

    private val repository = TestRepository()

    @Test
    fun `安全调用成功时返回数据`() = runTest {
        val result = repository.safe { BaseResponse(version = 1, errorInfo = null, data = "ok") }

        assertTrue(result is ApiResult.Success)
        assertEquals("ok", (result as ApiResult.Success).data)
    }

    @Test
    fun `业务错误码转换为 Business 错误`() = runTest {
        val result = repository.safe<String> {
            BaseResponse(version = 1, errorInfo = ErrorInfo(-1001, "参数错误"), data = null)
        }

        assertTrue(result is ApiResult.Failure)
        assertEquals(AppError.Business(-1001, "参数错误"), (result as ApiResult.Failure).error)
    }

    @Test
    fun `网络异常被收敛为 Network 错误而非抛出`() = runTest {
        val result = repository.safe<String> { throw IOException("no network") }

        assertTrue(result is ApiResult.Failure)
        assertEquals(AppError.Network, (result as ApiResult.Failure).error)
    }

    @Test
    fun `apiCall 失败时抛出统一的 AppException`() = runTest {
        try {
            repository.call<String> { ApiResult.Failure(AppError.Timeout) }
            fail("应当抛出 AppException")
        } catch (e: AppException) {
            assertEquals(AppError.Timeout, e.error)
        }
    }
}
