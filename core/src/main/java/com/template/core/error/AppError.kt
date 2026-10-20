package com.template.core.error

import com.template.core.R
import com.template.core.state.UiMessage
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 全项目唯一的错误类型。
 *
 * 约定：
 * - Repository 只抛 [AppException]（内部包着 [AppError]）；
 * - ViewModel 只做「转换」，不判断错误类型；
 * - UI 只渲染 [UiMessage]，不处理异常。
 */
sealed interface AppError {

    /** 无网络 / DNS 失败 / 连接被拒 */
    data object Network : AppError

    data object Timeout : AppError

    /** 401：需要重新登录 */
    data object Unauthorized : AppError

    /** 非 401 的 HTTP 错误 */
    data class Http(val code: Int) : AppError

    /** 业务错误码（后端 errorinfo.errorcode 非 0） */
    data class Business(val code: Int, val serverMessage: String) : AppError

    data class Unknown(val cause: Throwable? = null) : AppError
}

class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.toString(), cause)

/** HTTP 401：既用于错误映射，也是 TokenAuthenticator 的触发条件 */
const val HTTP_UNAUTHORIZED = 401

fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> error
    is HttpException -> if (code() == HTTP_UNAUTHORIZED) AppError.Unauthorized else AppError.Http(code())
    is SocketTimeoutException -> AppError.Timeout
    is ConnectException, is UnknownHostException -> AppError.Network
    is IOException -> AppError.Network
    else -> AppError.Unknown(this)
}

/**
 * 错误 → 可渲染文案。服务端业务错误优先展示服务端文案。
 */
fun AppError.asUiMessage(): UiMessage = when (this) {
    AppError.Network -> UiMessage.Res(R.string.core_error_network)
    AppError.Timeout -> UiMessage.Res(R.string.core_error_timeout)
    AppError.Unauthorized -> UiMessage.Res(R.string.core_error_unauthorized)
    is AppError.Http -> UiMessage.Res(R.string.core_error_http, listOf(code))
    is AppError.Business -> if (serverMessage.isBlank()) {
        UiMessage.Res(R.string.core_error_unknown)
    } else {
        UiMessage.Text(serverMessage)
    }
    is AppError.Unknown -> UiMessage.Res(R.string.core_error_unknown)
}
