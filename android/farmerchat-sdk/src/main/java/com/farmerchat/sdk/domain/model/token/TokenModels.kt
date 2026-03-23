package com.farmerchat.sdk.domain.model.token

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refresh_token: String
)

data class RefreshTokenResponse(
    @SerializedName("access_token") val access_token: String?,
    @SerializedName("refresh_token") val refresh_token: String?
)

data class SendNewTokenRequest(
    @SerializedName("device_id") val device_id: String,
    @SerializedName("user_id") val user_id: String
)

data class SendNewTokenResponse(
    @SerializedName("access_token") val access_token: String?,
    @SerializedName("refresh_token") val refresh_token: String?,
    @SerializedName("message") val message: String?
)
