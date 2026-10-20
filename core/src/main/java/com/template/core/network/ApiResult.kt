package com.template.core.network

import com.template.core.error.AppError

/**
 * Repository 层的统一返回类型。
 *
 * 只承载两种结果：拿到数据，或拿到一个已归一化的 [AppError]。
 */
sealed interface ApiResult<out T> {

    data class Success<T>(val data: T) : ApiResult<T>

    data class Failure(val error: AppError) : ApiResult<Nothing>
}
