package com.template.core.base

import com.template.core.error.AppError
import com.template.core.error.AppException
import com.template.core.error.toAppError
import com.template.core.network.ApiRequest
import com.template.core.network.ApiResult
import com.template.core.network.BaseResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository 基类：统一线程调度与错误归一化。
 *
 * 规范：
 * - Repository 对外只抛 [AppException]，不允许裸抛 IOException / HttpException；
 * - 网络异常（无网、超时、5xx）与业务错误码在这里被区分开；
 * - 所有 IO 切到 [Dispatchers.IO]，调用方无需关心线程。
 */
abstract class BaseRepository {

    /**
     * 调用 API 并直接取业务数据；失败时抛 [AppException]。
     */
    protected suspend fun <T> apiCall(call: suspend () -> ApiResult<T>): T = withContext(Dispatchers.IO) {
        when (val result = call()) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> throw AppException(result.error)
        }
    }

    /**
     * 把统一响应体转换为 [ApiResult]，任何异常都收敛成 [AppError]。
     */
    protected suspend fun <T> safeApi(call: suspend () -> BaseResponse<T>): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            val data = response.data
            when {
                response.isSuccess && data != null -> ApiResult.Success(data)
                response.isSuccess -> ApiResult.Failure(AppError.Unknown())
                else -> ApiResult.Failure(
                    AppError.Business(response.errorCode, response.errorMessage),
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            ApiResult.Failure(e.toAppError())
        }
    }

    /**
     * 构建统一请求体。
     *
     * @param token 需要鉴权时传当前 token；登录/注册等匿名接口传空串
     */
    protected fun <T : Any> buildApiRequest(data: T, token: String = ""): ApiRequest<T> = ApiRequest.build(data, token)

    protected suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block() }
}
