package com.farmerchat.sdk.utils

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

/**
 * Detects the user's country code without requiring any permissions.
 * Priority: SIM/network country → device locale → fallback.
 */
internal object CountryDetector {

    fun detect(context: Context, fallback: String = "IN"): String {
        // 1. Try network/SIM country (no permission required)
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val networkCountry = telephonyManager?.networkCountryIso
            ?.uppercase(Locale.ROOT)
            ?.takeIf { it.length == 2 }
        if (networkCountry != null) return networkCountry

        // 2. Try SIM card country
        val simCountry = telephonyManager?.simCountryIso
            ?.uppercase(Locale.ROOT)
            ?.takeIf { it.length == 2 }
        if (simCountry != null) return simCountry

        // 3. Device locale country
        val localeCountry = Locale.getDefault().country
            .uppercase(Locale.ROOT)
            .takeIf { it.length == 2 }
        if (localeCountry != null) return localeCountry

        return fallback
    }
}
