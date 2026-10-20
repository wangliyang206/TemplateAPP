package com.template.core.error

import com.template.core.state.UiMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AppErrorTest {

    @Test
    fun `连接异常映射为 Network`() {
        val error = ConnectException("refused").toAppError()
        assertEquals(AppError.Network, error)
    }

    @Test
    fun `DNS 失败映射为 Network`() {
        val error = UnknownHostException("unknown").toAppError()
        assertEquals(AppError.Network, error)
    }

    @Test
    fun `超时映射为 Timeout`() {
        val error = SocketTimeoutException("timeout").toAppError()
        assertEquals(AppError.Timeout, error)
    }

    @Test
    fun `普通 IO 异常映射为 Network`() {
        val error = IOException("broken pipe").toAppError()
        assertEquals(AppError.Network, error)
    }

    @Test
    fun `AppException 内的业务错误原样保留`() {
        val original = AppError.Business(-1001, "手机号或密码错误")
        val error = AppException(original).toAppError()
        assertEquals(original, error)
    }

    @Test
    fun `未知异常映射为 Unknown 并保留原因`() {
        val cause = IllegalStateException("boom")
        val error = cause.toAppError()
        assertTrue(error is AppError.Unknown)
        assertEquals(cause, (error as AppError.Unknown).cause)
    }

    @Test
    fun `业务错误文案优先展示服务端返回`() {
        val message = AppError.Business(-1, "库存不足").asUiMessage()
        assertEquals(UiMessage.Text("库存不足"), message)
    }

    @Test
    fun `业务错误文案为空时降级为本地通用文案`() {
        val message = AppError.Business(-1, "").asUiMessage()
        assertTrue(message is UiMessage.Res)
    }
}
