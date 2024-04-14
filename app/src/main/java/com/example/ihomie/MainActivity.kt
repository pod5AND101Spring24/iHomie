package com.example.ihomie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ihomie.R.id

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSupportActionBar(findViewById(R.id.toolbar))
//        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        setContentView(R.layout.activity_main)
        val supportFragmentManager = supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(id.frame_layout, BrowseFragment(), null).commit()

        // Call helper method to swap the FrameLayout with the fragment
//        replaceFragment(BrowseFragment())
    }

    private fun replaceFragment(BrowseFragment: BrowseFragment) {
        val fragmentManager = supportFragmentManager

        // define your fragments here
        val fragment1: Fragment = BrowseFragment()

        fragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, BrowseFragment())
            commit()
        }

        // val fragment2: Fragment =

//        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
//
//        // handle navigation selection
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            lateinit var fragment: Fragment
//            when (item.itemId) {
//                R.id.action_calorie_tracker -> fragment = fragment1
//                R.id.action_dashboard -> fragment = fragment2
//            }
//            fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
//            true
//        }
//        // Set default selection
//        bottomNavigationView.selectedItemId = R.id.action_calorie_tracker
    }
}