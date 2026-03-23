package com.farmerchat.sdk.base

abstract class BaseUseCase {
    protected suspend fun <T> executeApiCall(
        apiName: String,
        block: suspend () -> retrofit2.Response<T>
    ): ApiResult<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(response.code(), "Empty response body", apiName)
                }
            } else {
                ApiResult.Error(response.code(), response.message(), apiName)
            }
        } catch (e: Exception) {
            ApiResult.Error(null, e.localizedMessage, apiName, e)
        }
    }
}
