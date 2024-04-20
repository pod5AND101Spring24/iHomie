package com.example.ihomie

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class NotificationsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notifications_preferences, rootKey)
    }
}