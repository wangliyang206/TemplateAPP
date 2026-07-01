package com.template.core.base

import com.template.core.network.BaseResponse
import com.template.core.network.ApiException
import com.template.core.network.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository {

    protected suspend fun <T> apiCall(
        call: suspend () -> ApiResult<T>
    ): T = withContext(Dispatchers.IO) {
        when (val result = call()) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> throw result.exception
        }
    }

    protected suspend fun <T> safeApi(
        call: suspend () -> BaseResponse<T>
    ): ApiResult<T> {
        val resp = call()
        return if (resp.code == 200 && resp.data != null) {
            ApiResult.Success(resp.data)
        } else {
            ApiResult.Error(ApiException(resp.code, resp.message))
        }
    }

    protected suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        block()
    }
}
