package com.example.ihomie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 * Use the [SavedHomesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedHomesFragment : Fragment(), OnListFragmentInteractionListener  {
    private lateinit var itemsRv: RecyclerView
    private lateinit var adapter: PropertyItemAdapter
    private lateinit var savedHomesDao: SavedHomesDao
    private lateinit var progressBar: ContentLoadingProgressBar
    private lateinit var noResultView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SavedHomesFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SavedHomesFragment", "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved_homes, container, false)
        itemsRv = view.findViewById(R.id.rv_saved_list)
        progressBar = view.findViewById(R.id.progress)
        noResultView = view.findViewById(R.id.tv_no_result)
        savedHomesDao = AppDatabase.getInstance(requireContext()).savedHomesDao()

        itemsRv.layoutManager = LinearLayoutManager(context)

        adapter = PropertyItemAdapter(
            requireContext(),
            (activity?.application as SavedHomesApplication).db.savedHomesDao(),
            mutableListOf(),
            isSavedHomesScreen = true,
            this@SavedHomesFragment)
        itemsRv.adapter = adapter

        updateAdapter(progressBar, itemsRv)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SavedHomesFragment", "onViewCreated")
        updateAdapter(progressBar, itemsRv)
    }

    /*
    * Navigate to Property Detail page when clicked
    */
    override fun onItemClick(item: PropertyModel) {
        //Toast.makeText(context, "test: " + item.zpid, Toast.LENGTH_LONG).show()
        val intent = Intent(context, PropertyDetail::class.java).apply {
            putExtra(PROPERTY_EXTRA, item)
        }
        startActivity(intent)
    }

    /*
    * Update recycler view adapter with the list of properties for the property cards
    */
    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAdapter(progressBar: ContentLoadingProgressBar, recyclerView: RecyclerView) {

        progressBar.show()

        GlobalScope.launch(Dispatchers.Main) {
            val zpidList = savedHomesDao.getAllSavedHomes()     // Get the zpid from the database
            Log.d("fetchSavedHomesZpidFromDatabase", zpidList.toString())
            if (zpidList.isNotEmpty()) {
                val queryUrls = constructQueryUrls(zpidList)
                Log.d("queryUrls", queryUrls.toString())

                val propertiesDetails = fetchPropertyDetailsFromEndpoints(queryUrls)
                Log.d("propertiesDetails", propertiesDetails.toString())

                // Parse the list of saved properties
                val properties = propertiesDetails.flatMap { responseBody ->
                    responseBody?.let { parseProperty(it) } ?: emptyList()
                }

                progressBar.hide()

                if (properties.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    noResultView.visibility = View.GONE

                    adapter.setItems(properties)
                    adapter.notifyDataSetChanged()
                } else {
                    recyclerView.visibility = View.GONE
                    noResultView.visibility = View.VISIBLE
                }
            } else {
                progressBar.hide()
                recyclerView.visibility = View.GONE
                noResultView.visibility = View.VISIBLE
            }
        }
    }

    /*
    * Update recycler view adapter with the list of saved properties for the property cards
    */
    private fun constructQueryUrls(zpidList: List<SavedHomes>): List<String> {
        return zpidList.map { "https://zillow-com1.p.rapidapi.com/property?zpid=${it.zpid}" }
    }

    /*
    * Feed the URLs of the saved homes to the endpoint and return the responses
    */
    private suspend fun fetchPropertyDetailsFromEndpoints(urls: List<String>): List<String?> {
        return withContext(IO) {
            val client = OkHttpClient()

            val responses = mutableListOf<String?>()
            for (url in urls) {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("X-RapidAPI-Key", API_KEY)
                    .addHeader("X-RapidAPI-Host", "zillow-com1.p.rapidapi.com")
                    .build()

                val response = client.newCall(request).execute()

                delay(500)

                try {
                    if (response.isSuccessful) {
                        val responseBodyString = response.body?.string()
                        responses.add(responseBodyString)
                    } else {
                        Log.e("fetchPropertyDetailsFromEndpoints", "Error: ${response.code} ${response.message}")
                        responses.add(null)
                    }
                } finally {
                    response.body?.close()
                }
            }
            Log.d("SavedHomesFragment", "response successful")
            return@withContext responses
        }
    }

    /*
    * Parse JSON to get the highlight data, add them to a list of properties
    */
    private fun parseProperty(responseBody: String): List<PropertyModel> {
        val properties = mutableListOf<PropertyModel>()
        try {
            val jsonObject = JSONObject(responseBody)

            val zpid = jsonObject.optLong("zpid").toString()
            val city = jsonObject.optString("city")
            val state = jsonObject.optString("state")
            val streetAddress = jsonObject.optString("streetAddress")
            val zipcode = jsonObject.optString("zipcode")
            val price = jsonObject.optInt("price")
            val bedrooms = jsonObject.optInt("bedrooms")
            val bathrooms = jsonObject.optDouble("bathrooms")
            val livingAreaValue = jsonObject.optInt("livingAreaValue")
            val homeStatus = jsonObject.optString("homeStatus")
            val homeType = jsonObject.optString("homeType")
            val imageUrl = jsonObject.optString("imgSrc")

            // Create a PropertyModel object and add it to the list
            val property = PropertyModel(
                zpid = zpid,
                address = "$streetAddress, $city, $state $zipcode",
                price = price,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                sqft = livingAreaValue,
                listingStatus = homeStatus,
                propertyType = homeType,
                imageUrl = imageUrl,
            )
            properties.add(property)
        } catch (e: Exception) {
            Log.e("SavedHomesFragment", "No results found")
            e.printStackTrace()
        }

        return properties
    }
}