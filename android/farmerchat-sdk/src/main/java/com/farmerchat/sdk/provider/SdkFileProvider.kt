package com.farmerchat.sdk.provider

import androidx.core.content.FileProvider

/**
 * A dedicated FileProvider subclass for the FarmerChat SDK.
 * Using a subclass avoids manifest merger conflicts with the host app's own FileProvider.
 */
class SdkFileProvider : FileProvider()
