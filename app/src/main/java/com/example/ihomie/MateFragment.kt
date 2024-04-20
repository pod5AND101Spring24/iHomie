package com.example.ihomie

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class MateFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.mate_preferences, rootKey)
    }
}