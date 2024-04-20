package com.example.ihomie

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class AccountFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey)
    }
}