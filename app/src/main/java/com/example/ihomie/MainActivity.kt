package com.example.ihomie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.example.ihomie.R.id
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors

private lateinit var accountButton: Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        setSupportActionBar(findViewById(R.id.toolbar))
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        setContentView(R.layout.activity_main)
        val supportFragmentManager = supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // define your fragments here
        val browseFragment: Fragment = BrowseFragment()  // Replace fragment with main screen when implemented
        val savedHomesFragment: Fragment = SavedHomesFragment()
        val statisticsFragment: Fragment = StatisticsFragment()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            lateinit var fragment: Fragment
            when (item.itemId) {
                R.id.action_browse -> replaceFragment(browseFragment)
                R.id.action_saved -> replaceFragment(savedHomesFragment)
                R.id.action_statistics -> replaceFragment(statisticsFragment)
                R.id.action_account -> {
                    // Start the activity for the "action_account" item
                    startActivity(Intent(this, AccountActivity::class.java))
                    // Optionally, you can deselect the item to prevent it from staying selected
                    // after navigating to the activity
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
        // Set default selection
        bottomNavigationView.selectedItemId = R.id.action_browse

        // Replace fragment with main screen when implemented
        fragmentTransaction.replace(id.frame_layout, BrowseFragment(), null).commit()

        // Call helper method to swap the FrameLayout with the fragment
//        replaceFragment(BrowseFragment())
    }

    /*
     * Swap FrameLayout with the fragments when user navigate between screen in bottom nav bar
     */
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}