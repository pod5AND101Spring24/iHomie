package com.example.ihomie

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [StatisticsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatisticsFragment : Fragment() {
    private lateinit var statsTextView: TextView
    private lateinit var viewModel: List<SavedHomes>
    private var noResultView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)
        val statsTextView = view.findViewById<View>(R.id.statsTextView) as TextView
        val progressBar = view.findViewById<View>(R.id.statsProgress) as ContentLoadingProgressBar
        noResultView = view.findViewById(R.id.no_result)

        updateStatistics(progressBar, statsTextView)
        return view
    }

    /*
    * Get the ZPID of the saved homes
    */
    private suspend fun fetchSavedHomesZpidFromDatabase(): List<SavedHomes> {
        return withContext(Dispatchers.IO) {
            (activity?.application as SavedHomesApplication).db.savedHomesDao().getAllSavedHomes()
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun updateStatistics(progressBar: ContentLoadingProgressBar, statsTextView: TextView) {

        progressBar.show()

        GlobalScope.launch(Dispatchers.Main) {
            val zpidList = fetchSavedHomesZpidFromDatabase()
            if (zpidList.isNotEmpty()) {
                val queryUrls = constructQueryUrls(zpidList)
                Log.d("queryUrls", "$queryUrls")

                val propertiesDetails = fetchPropertyDetailsFromEndpoints(queryUrls)
                Log.d("propertiesDetails", "$propertiesDetails")

                // Parse the list of saved properties
                val properties = propertiesDetails.flatMap { responseBody ->
                    responseBody?.let { parseProperty(it) } ?: emptyList()
                }

                progressBar.hide()

                if (properties.isNotEmpty()) {
                    // Calculate statistics for prices
                    val averagePrice = roundOffDecimal(properties.map { it.price?.toDouble() ?: 0.00 }.average(), 2)
                    val maxPrice = properties.maxByOrNull { it.price ?: 0 }?.price
                    val minPrice = properties.minByOrNull { it.price ?: 0 }?.price

                    // Calculate statistics for bedrooms
                    val averageBedrooms = roundOffDecimal(properties.map { it.bedrooms?.toDouble() ?: 0.00 }.average(), 2)
                    val maxBedrooms = properties.maxByOrNull { it.bedrooms ?: 0 }?.bedrooms
                    val minBedrooms = properties.minByOrNull { it.bedrooms ?: 0 }?.bedrooms

                    // Calculate statistics for bathrooms
                    val averageBathrooms = roundOffDecimal(properties.map { it.bathrooms?.toDouble() ?: 0.00 }.average(), 2)
                    val maxBathrooms = properties.maxByOrNull { it.bathrooms?.toDouble() ?: 0.0}?.bathrooms
                    val minBathrooms = properties.minByOrNull { it.bathrooms?.toDouble() ?: 0.0}?.bathrooms

                    // Calculate statistics for sqft
                    val averageSqft = roundOffDecimal(properties.map { it.sqft?.toDouble() ?: 0.00 }.average(), 2)
                    val maxSqft = properties.maxByOrNull { it.sqft ?: 0 }?.sqft
                    val minSqft = properties.minByOrNull { it.sqft ?: 0 }?.sqft

                    // Update UI with statistics
                    withContext(Dispatchers.Main) {
                        statsTextView.append("Average price: $$averagePrice\n")
                        statsTextView.append("Max price: $$maxPrice\n")
                        statsTextView.append("Min price: $$minPrice\n")
                        statsTextView.append("\n")

                        statsTextView.append("Average bedrooms: $averageBedrooms\n")
                        statsTextView.append("Max bedrooms: $maxBedrooms\n")
                        statsTextView.append("Min bedrooms: $minBedrooms\n")
                        statsTextView.append("\n")

                        statsTextView.append("Average bathrooms: $averageBathrooms\n")
                        statsTextView.append("Max bathrooms: $maxBathrooms\n")
                        statsTextView.append("Min bathrooms: $minBathrooms\n")
                        statsTextView.append("\n")

                        statsTextView.append("Average sqft: $averageSqft\n")
                        statsTextView.append("Max sqft: $maxSqft\n")
                        statsTextView.append("Min sqft: $minSqft\n")
                        statsTextView.append("\n")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        statsTextView.visibility = View.GONE
                        noResultView?.visibility = View.VISIBLE
                    }
                }
            } else {
                statsTextView.visibility = View.GONE
                noResultView?.visibility = View.VISIBLE
            }
        }

    }

    // Rounding a double function
    fun roundOffDecimal(number: Double, decimalPlace: Int): Double {
        val factor = 10.0.pow(decimalPlace.toDouble())
        return round(number * factor) / factor
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
        return withContext(Dispatchers.IO) {
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
            return@withContext responses
        }
    }

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
            val longitude = jsonObject.optDouble("longitude")
            val latitude = jsonObject.optDouble("latitude")

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
                longitude = longitude,
                latitude = latitude
            )
            properties.add(property)
        } catch (e: Exception) {
            Log.e("SavedHomesFragment", "No results found")
            e.printStackTrace()
        }

        return properties
    }


}