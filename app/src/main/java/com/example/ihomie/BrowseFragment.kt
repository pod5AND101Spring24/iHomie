package com.example.ihomie

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import java.net.URLEncoder


const val API_KEY = "38a4dad85bmshc1ddee66fc918cdp16b120jsn56be4568a157"

class BrowseFragment : Fragment(), OnListFragmentInteractionListener  {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        val recyclerView = view.findViewById<View>(R.id.rv_browse_list) as RecyclerView
        val searchView = view.findViewById<View>(R.id.search_view) as SearchView

//        searchView.setupWithSearchBar(searchBar)
        val context = view.context
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter
        val adapter = PropertyItemAdapter(emptyList(), this@BrowseFragment)
        recyclerView.adapter = adapter

        // Parse query and use API endpoint
        searchView.setOnQueryTextListener(object :  SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                //on submit send query to be encoded
                val locationQuery = encodeQuery(query)
                // pass encoded query to API
                if (locationQuery != null) {
                    Log.d("Location Query", locationQuery)
                    updateAdapter(recyclerView, locationQuery)
                }

                // Hide the keyboard
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)

                return true
            }
        })

        // OR use Sample data
//        val sampleProperties = parseSampleProperties()
//        recyclerView.adapter = PropertyItemAdapter(sampleProperties, this@BrowseFragment)

        return view
    }

    override fun onItemClick(item: PropertyModel) {
        Toast.makeText(context, "test: " + item.address, Toast.LENGTH_LONG).show()
    }

    /*
    * Update recycler view adapter with the list of properties for the property cards
    */
    private fun updateAdapter(recyclerView: RecyclerView, query: String) {
        GlobalScope.launch(IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://zillow-com1.p.rapidapi.com/propertyExtendedSearch?location=${query}&home_type=Houses")
                .get()
                .addHeader("X-RapidAPI-Key", API_KEY)
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

                    val zpid = propObject.optString("zpid")
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
                        zpid = zpid,
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

    private fun parseSampleProperties(): List<PropertyModel> {
        val properties = mutableListOf<PropertyModel>()

        // Add your sample properties here
        val property1 = PropertyModel(
            zpid = "20475379",
            address = "933 24th St, Santa Monica, CA 90403",
            price = 2995000,
            bedrooms = 4,
            bathrooms = 1.0,
            sqft = 1926,
            listingStatus = "FOR_SALE (Sample Data)",
            propertyType = "SINGLE_FAMILY",
            imageUrl = "https://photos.zillowstatic.com/fp/bc247265a45fdecc77e2b337ebd2d111-p_e.jpg"
        )

        val property2 = PropertyModel(
            zpid = "20471856",
            address = "2508 28th St, Santa Monica, CA 90405",
            price = 2595000,
            bedrooms = 3,
            bathrooms = 3.0,
            sqft = 2005,
            listingStatus = "FOR_SALE",
            propertyType = "SINGLE_FAMILY",
            imageUrl = "https://photos.zillowstatic.com/fp/a4efc91c50534ba45aaf6d0478e3f3be-p_e.jpg"
        )

        val property3 = PropertyModel(
            zpid = "20482836",
            address = "2716 6th St, Santa Monica, CA 90405",
            price = 1675000,
            bedrooms = 2,
            bathrooms = 2.0,
            sqft = 1407,
            listingStatus = "FOR_SALE",
            propertyType = "SINGLE_FAMILY",
            imageUrl = "https://photos.zillowstatic.com/fp/123c0dd47c9efff24af41cd051cdb838-p_e.jpg"
        )

        val property4 = PropertyModel(
            zpid = "20475390",
            address = "942 25th St, Santa Monica, CA 90403",
            price = 4250000,
            bedrooms = 5,
            bathrooms = 5.0,
            sqft = 3476,
            listingStatus = "FOR_SALE",
            propertyType = "SINGLE_FAMILY",
            imageUrl = "https://photos.zillowstatic.com/fp/e66601acea6f48405a93ef8534491a04-p_e.jpg"
        )

        val property5 = PropertyModel(
            zpid = "37767919",
            address = "723 10th St, Santa Monica, CA 90402",
            price = 6349000,
            bedrooms = 6,
            bathrooms = 6.0,
            sqft = 5635,
            listingStatus = "FOR_SALE",
            propertyType = "SINGLE_FAMILY",
            imageUrl = "https://photos.zillowstatic.com/fp/43c4639f2f61bc59f32d303543d35832-p_e.jpg"
        )

        properties.addAll(listOf(property1, property2, property3, property4, property5))

        return properties
    }

    private fun encodeQuery(query: String): String? {
        return URLEncoder.encode(query, "UTF-8");
    }
}