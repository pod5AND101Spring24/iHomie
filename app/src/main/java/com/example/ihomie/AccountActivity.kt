package com.example.ihomie

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var signOutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        // Retrieve the selected theme preference and apply it

        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_account)

        // Sign-out button
        signOutButton = findViewById(R.id.signOutButton)
        signOutButton.setOnClickListener {
            signOut();
        }

        // Load AccountFragment initially
        loadAccountFragment()

        // Register onBackPressedDispatcher callback
        registerOnBackPressedCallback()

    }

    // Function to load AccountFragment
    private fun loadAccountFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.account_container, AccountFragment())
            .commit()
        // Show sign-out button when AccountFragment is loaded
        signOutButton.visibility = View.VISIBLE
    }

    // Function to hide sign-out button when navigating away from AccountFragment
    private fun hideSignOutButton() {
        signOutButton.visibility = View.GONE
    }

    private fun signOut()
    {
        FirebaseAuth.getInstance().signOut()
        // Navigate to the login pagee
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the shared preference change listener
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }


    // Changing Fragment view
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment.
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader,
            pref.fragment!!
        )
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        // Replace the existing Fragment with the new Fragment.
        supportFragmentManager.beginTransaction()
            .replace(R.id.account_container, fragment)
            .addToBackStack(null)
            .commit()

        // Hide the sign-out button for other fragments
        hideSignOutButton()

        return true
    }

    // Customize back navigation to show sign-out button visible when returning to AccountFragment
    private fun registerOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if the current fragment in the container is the AccountFragment
                val currentFragment = supportFragmentManager.findFragmentById(R.id.account_container)
                if (currentFragment is AccountFragment) {
                    // Make the sign-out button visible
                    signOutButton.visibility = View.VISIBLE
                    finish()
                }
                loadAccountFragment()
            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Handle theme preference change
        if (key == "theme_preference")
        {
            applyTheme()
            recreate()
        }
    }

    private fun applyTheme()
    {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = sharedPreferences.getString("theme_preference", "light_theme")

        when (themePreference) {
            "light_theme" -> setTheme(R.style.AppTheme_Light)
            "dark_theme" -> setTheme(R.style.AppTheme_Dark)
        }

    }
}

