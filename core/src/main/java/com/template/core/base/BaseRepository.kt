package com.template.core.base

import com.template.core.network.ApiRequest
import com.template.core.network.BaseResponse
import com.template.core.network.ApiException
import com.template.core.network.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository {

    /**
     * 调用 API 并从 ApiResult 中提取成功数据或抛出异常
     */
    protected suspend fun <T> apiCall(
        call: suspend () -> ApiResult<T>
    ): T = withContext(Dispatchers.IO) {
        when (val result = call()) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> throw result.exception
        }
    }

    /**
     * 安全包装 API 调用，将 BaseResponse 转换为 ApiResult
     *
     * 规范：code == 0 表示成功，data 不为 null
     */
    protected suspend fun <T> safeApi(
        call: suspend () -> BaseResponse<T>
    ): ApiResult<T> {
        val resp = call()
        return if (resp.code == 0 && resp.data != null) {
            ApiResult.Success(resp.data)
        } else {
            ApiResult.Error(ApiException(resp.code, resp.message))
        }
    }

    /**
     * 构建统一请求体 ApiRequest
     *
     * @param data 业务参数
     * @param token 用户 token（登录/注册等无需 token 的接口传空字符串）
     */
    protected fun <T : Any> buildApiRequest(data: T, token: String = ""): ApiRequest<T> {
        return ApiRequest.build(data, token)
    }

    protected suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        block()
    }
}
