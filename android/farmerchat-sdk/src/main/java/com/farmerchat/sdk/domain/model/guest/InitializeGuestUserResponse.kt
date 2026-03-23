package com.farmerchat.sdk.domain.model.guest

internal data class InitializeGuestUserResponse(
    val access_token: String,
    val refresh_token: String,
    val user_id: String?,
    val created_now: Boolean? = null,   // true = new user, false = existing user returned
    val show_crops_livestocks: Boolean = false,
    val country_code: String? = null,
    val country: String? = null,
    val state: String? = null
)
