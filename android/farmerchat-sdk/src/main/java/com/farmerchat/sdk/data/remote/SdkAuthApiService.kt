package com.farmerchat.sdk.data.remote

import com.farmerchat.sdk.domain.model.token.RefreshTokenRequest
import com.farmerchat.sdk.domain.model.token.RefreshTokenResponse
import com.farmerchat.sdk.domain.model.token.SendNewTokenRequest
import com.farmerchat.sdk.domain.model.token.SendNewTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface SdkAuthApiService {

    @POST("api/user/get_new_access_token/")
    fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Call<RefreshTokenResponse>

    @POST("api/user/send_tokens/")
    fun sendUserTokens(
        @Header("API-Key") apiKey: String,
        @Body request: SendNewTokenRequest
    ): Call<SendNewTokenResponse>
}
