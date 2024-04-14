package com.example.ihomie

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class BrowseFragment : Fragment(), OnListFragmentInteractionListener  {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        val recyclerView = view.findViewById<View>(R.id.rv_browse_list) as RecyclerView
        val context = view.context
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter here
        val adapter = PropertyItemAdapter(emptyList(), this@BrowseFragment)
        recyclerView.adapter = adapter

        updateAdapter(recyclerView)

        return view
    }

    override fun onItemClick(item: PropertyModel) {
        Toast.makeText(context, "test: " + item.address, Toast.LENGTH_LONG).show()
    }

    /*
    * Update recycler view adapter with the list of properties for the property cards
    */
    private fun updateAdapter(recyclerView: RecyclerView) {

        GlobalScope.launch(IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://zillow-com1.p.rapidapi.com/propertyExtendedSearch?location=santa%20monica%2C%20ca&home_type=Houses")
                .get()
                .addHeader("X-RapidAPI-Key", "38a4dad85bmshc1ddee66fc918cdp16b120jsn56be4568a157")
                .addHeader("X-RapidAPI-Host", "zillow-com1.p.rapidapi.com")
                .build()

            val response = client.newCall(request).execute()
            val properties = response.body()?.let { parseProperty(it.string()) }

            withContext(Dispatchers.Main) {
                recyclerView.adapter = properties?.let { PropertyItemAdapter(it, this@BrowseFragment) }
                Log.d("BrowseFragment", "response successful")
            }
        }
    }

    /*
    * Parse JSON to get the highlight data, add them to a list of properties
    */
    private fun parseProperty(responseBody: String): List<PropertyModel> {
        val properties = mutableListOf<PropertyModel>()
        try {
            val jsonObject = JSONObject(responseBody)
            val propsArray = jsonObject.optJSONArray("props")

            // Check if the required JSON array exists before accessing its elements
            if (propsArray != null) {
                for (i in 0 until propsArray.length()) {
                    val propObject = propsArray.getJSONObject(i)

                    val address = propObject.optString("address")
                    val price = propObject.optInt("price")
                    val bedrooms = propObject.optInt("bedrooms")
                    val bathrooms = propObject.optDouble("bathrooms")
                    val sqft = propObject.optInt("livingArea")
                    val listingStatus = propObject.optString("listingStatus")
                    val propertyType = propObject.optString("propertyType")
                    val imageUrl = propObject.optString("imgSrc")

                    // Create a PropertyModel object and add it to the list
                    val property = PropertyModel(
                        address = address,
                        price = price,
                        bedrooms = bedrooms,
                        bathrooms = bathrooms,
                        sqft = sqft,
                        listingStatus = listingStatus,
                        propertyType = propertyType,
                        imageUrl = imageUrl
                    )
                    properties.add(property)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return properties
    }
}