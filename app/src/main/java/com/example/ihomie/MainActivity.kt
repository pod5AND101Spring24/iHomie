package com.example.ihomie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ihomie.R.id
import com.google.android.material.elevation.SurfaceColors

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSupportActionBar(findViewById(R.id.toolbar))
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        setContentView(R.layout.activity_main)
        val supportFragmentManager = supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // Replace fragment with main screen when implemented
        fragmentTransaction.replace(id.frame_layout, BrowseFragment(), null).commit()

        // Call helper method to swap the FrameLayout with the fragment
//        replaceFragment(BrowseFragment())
    }

    /*
     * Swap FrameLayout with the fragments when user navigate between screen in bottom nav bar
     */
    private fun replaceFragment(BrowseFragment: BrowseFragment) {
        val fragmentManager = supportFragmentManager

        // define your fragments here
        val fragment1: Fragment = BrowseFragment()  // Replace fragment with main screen when implemented
        // val fragment2: Fragment =
        // val fragment3: Fragment =
        // etc....

        fragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, BrowseFragment())
            commit()
        }

//        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
//
//        // handle navigation selection
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            lateinit var fragment: Fragment
//            when (item.itemId) {
//                R.id.x -> fragment = fragment1
//                R.id.x -> fragment = fragment2
//                R.id.x -> fragment = fragment3
//            }
//            fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit()
//            true
//        }
//        // Set default selection
//        bottomNavigationView.selectedItemId = R.id.x
    }
}