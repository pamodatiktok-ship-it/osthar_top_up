package com.example.util

import androidx.annotation.DrawableRes
import com.example.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object Formatters {
    private val currencyFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun money(amount: Double): String = "LKR ${currencyFormat.format(amount)}"

    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "—"
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsed = parser.parse(isoString)
                if (parsed != null) {
                    val output = SimpleDateFormat("MMM d, yyyy, hh:mm a", Locale.US).apply {
                        timeZone = TimeZone.getDefault()
                    }
                    return output.format(parsed)
                }
            } catch (_: Exception) {
            }
        }
        return isoString.take(16).replace('T', ' ')
    }

    fun isRemoteUrl(path: String?): Boolean {
        val trimmed = path?.trim().orEmpty()
        return trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true)
    }

    @DrawableRes
    fun localAssetRes(path: String?): Int {
        val lower = path?.lowercase().orEmpty()
        return when {
            "free-fire-max" in lower -> R.drawable.img_free_fire_max
            "free-fire" in lower -> R.drawable.img_free_fire
            "battle-hero" in lower -> R.drawable.img_battle_hero
            "osthar-logo" in lower -> R.drawable.img_osthar_logo
            else -> R.drawable.img_diamonds
        }
    }
}
