package com.example.ihomie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
    }
    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun applyTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light_theme")

        when (themePreference) {
            "light_theme" -> setTheme(R.style.AppTheme_Light)
            "dark_theme" -> setTheme(R.style.AppTheme_Dark)
        }
    }
}