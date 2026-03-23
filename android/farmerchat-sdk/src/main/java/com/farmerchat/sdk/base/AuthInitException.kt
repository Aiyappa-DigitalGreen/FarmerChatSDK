package com.farmerchat.sdk.base

/**
 * Thrown when the SDK cannot initialise a guest session via initialize_user.
 * Carries a user-facing message (e.g. "no internet" vs "device limit reached").
 */
internal class AuthInitException(message: String) : Exception(message)
