package com.example.ihomie

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}