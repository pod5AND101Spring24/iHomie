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

    private fun updateAdapter(recyclerView: RecyclerView) {
//        val client = OkHttpClient()
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val request = Request.Builder()
//                    .url("https://zillow-com1.p.rapidapi.com/property?zpid=2080998890&test=true")
//                    .get()
//                    .addHeader("X-RapidAPI-Key", "38a4dad85bmshc1ddee66fc918cdp16b120jsn56be4568a157")
//                    .addHeader("X-RapidAPI-Host", "zillow-com1.p.rapidapi.com")
//                    .build()
//
//                val response = client.newCall(request).execute()
//                val responseBody = response.body()?.string()
//                val properties = parseProperty(responseBody)
//
//                // Update UI on the main thread if the fragment is still attached
////                if (isAdded) {
////                    withContext(Dispatchers.Main) {
////                        recyclerView.adapter = PropertyItemAdapter(properties, this@BrowseFragment)
////                    }
////                }
//                GlobalScope.launch(Dispatchers.Main) {
//                    recyclerView.adapter = PropertyItemAdapter(properties, this@BrowseFragment)
//                }
//                Log.d("BrowseFragment", "response successful")
//
//            } catch (e: Exception) {
//                //e.printStackTrace()
//                // Handle error gracefully (e.g., show a toast or log the error)
//                Log.e("BrowseFragment", e.toString())
//            }
//        }

        val properties = mutableListOf<PropertyModel>()

        // Sample property 1
        val property1 = PropertyModel(
            id = "1",
            address = "123 Capital Rd, Anytown, CA 12345",
            price = 250000,
            bedrooms = 3,
            bathrooms = 2.5,
            sqft = 1472
        )
        properties.add(property1)

        // Sample property 2
        val property2 = PropertyModel(
            id = "2",
            address = "456 W St, Othertown, NY 67890",
            price = 350000,
            bedrooms = 4,
            bathrooms = 3.5,
            sqft = 1268
        )
        properties.add(property2)

        // Sample property 3
        val property3 = PropertyModel(
            id = "3",
            address = "123 Main St, Chicago, IL 62701",
            price = 250000,
            bedrooms = 4,
            bathrooms = 2.5,
            sqft = 2143
        )
        properties.add(property3)

        // Sample property 4
        val property4 = PropertyModel(
            id = "4",
            address = "456 Langford St, Springfield, IL 62702",
            price = 300000,
            bedrooms = 3,
            bathrooms = 2.0,
            sqft = 1091
        )
        properties.add(property4)

        recyclerView.adapter = PropertyItemAdapter(properties, this@BrowseFragment)
        Log.d("BrowseFragment", "response successful")
    }

    private fun parseProperty(responseBody: String): List<PropertyModel> {
        val properties = mutableListOf<PropertyModel>()
        try {
            val jsonObject = JSONObject(responseBody)
            val listingObject = jsonObject.optJSONObject("listing")
            val metaObject = jsonObject.optJSONObject("meta")

            // Check if the required JSON objects exist before accessing their properties
            if (listingObject != null && metaObject != null) {
                val propertyId = metaObject.optString("listing_id")
                val price = listingObject.optInt("price")
                val bedrooms = listingObject.optInt("beds")
                val bathrooms = listingObject.optDouble("baths_full")
                val sqft = listingObject.optInt("livingAreaValue")

                val addressObject = listingObject.optJSONObject("address")
                val city = addressObject?.optString("city") ?: ""
                val line = addressObject?.optString("line") ?: ""
                val postalCode = addressObject?.optString("postal_code") ?: ""
                val state = addressObject?.optString("state") ?: ""

                val property = PropertyModel(
                    id = propertyId,
                    address = "$line, $city, $state $postalCode",
                    price = price,
                    bedrooms = bedrooms,
                    bathrooms = bathrooms,
                    sqft = sqft
                )

                properties.add(property)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return properties
    }

    override fun onItemClick(item: PropertyModel) {
        Toast.makeText(context, "test: " + item.address, Toast.LENGTH_LONG).show()
    }
}