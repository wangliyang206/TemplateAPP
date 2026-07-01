package com.template.core.base

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

    protected suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        block()
    }
}
