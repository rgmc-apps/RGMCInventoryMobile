package com.rgmc.inventory.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import com.rgmc.inventory.R

object ThemeManager {
    const val MODE_MINIMALIST = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    private const val PREF_NAME = "rgmc_theme_prefs"
    private const val KEY_MODE = "theme_mode"

    fun getMode(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_MODE, MODE_MINIMALIST)

    fun setMode(context: Context, mode: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_MODE, mode).apply()
    }

    fun applyTheme(activity: AppCompatActivity) {
        activity.setTheme(getThemeResId(activity))
    }

    fun applySplashTheme(activity: AppCompatActivity) {
        activity.setTheme(getSplashThemeResId(activity))
    }

    private fun getThemeResId(context: Context): Int = when (getMode(context)) {
        MODE_LIGHT -> R.style.Theme_RGMCInventory_Light
        MODE_DARK -> R.style.Theme_RGMCInventory_Dark
        else -> R.style.Theme_RGMCInventory
    }

    private fun getSplashThemeResId(context: Context): Int = when (getMode(context)) {
        MODE_LIGHT -> R.style.Theme_RGMCInventory_Splash_Light
        MODE_DARK -> R.style.Theme_RGMCInventory_Splash_Dark
        else -> R.style.Theme_RGMCInventory_Splash
    }
}

fun Context.resolveAttrColor(@AttrRes attr: Int): Int {
    val tv = TypedValue()
    theme.resolveAttribute(attr, tv, true)
    return if (tv.resourceId != 0) resources.getColor(tv.resourceId, theme) else tv.data
}
