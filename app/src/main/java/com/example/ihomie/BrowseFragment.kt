package com.example.ihomie

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale

const val API_KEY =  "REPLACE"
//const val API_KEY =  "4e72379795msh0fffca9887c3f3dp1b4723jsnfccbc46b9845"

class BrowseFragment : Fragment(), OnListFragmentInteractionListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var itemsRv: RecyclerView

    private var noResultView: View? = null
    private lateinit var savedHomesDao: SavedHomesDao
    private var lastSearchQuery: String? = null
    private var lastOptionalUrl: String? = null

    private var optionalUrl: String? = null
    private var zipcode: String? = null

    private var statusTypeSpinner: Spinner? = null
    private var homeTypeSpinner: Spinner? = null
    private var sortSpinner: Spinner? = null
    private var minPriceEditText: EditText? = null
    private var maxPriceEditText: EditText? = null
    private var minRentPriceEditText: EditText? = null
    private var maxRentPriceEditText: EditText? = null
    private var minBedsEditText: EditText?= null
    private var maxBedsEditText: EditText? = null
    private var minBathsEditText: EditText? = null
    private var maxBathsEditText: EditText? = null
    private var minSqftEditText: EditText? = null
    private var maxSqftEditText: EditText? = null
    private var hasGarageCheckbox: CheckBox? = null
    private var hasPoolCheckbox: CheckBox? = null
    private var savedOptionUrl:String?=null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("BrowseFragment", "onCreateView: savedInstanceState=$savedInstanceState")

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_browse, container, false)
        itemsRv = view.findViewById<View>(R.id.rv_browse_list) as RecyclerView
        val searchView = view.findViewById<View>(R.id.search_view) as SearchView
        searchView.isQueryRefinementEnabled = false
        searchView.isFocusable = false
        noResultView = view.findViewById(R.id.tv_no_result)
        savedHomesDao = AppDatabase.getInstance(requireContext()).savedHomesDao()

        val context = view.context
        itemsRv.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter
        itemsRv.adapter = PropertyItemAdapter(
            context,
            (activity?.application as SavedHomesApplication).db.savedHomesDao(),
            mutableListOf(),
            isSavedHomesScreen = false,
            this@BrowseFragment)

      /*  // Restore the last search query if it exists
        if (savedInstanceState != null) {
            lastSearchQuery = savedInstanceState.getString(LAST_SEARCH_QUERY_KEY)
            Log.d("BrowseFragment", "onCreate: lastSearchQuery=$lastSearchQuery")
        }*/

        statusTypeSpinner=view.findViewById(R.id.status_type_spinner)
        homeTypeSpinner = view.findViewById(R.id.home_type_spinner)
        sortSpinner = view.findViewById(R.id.sort_spinner)
        minPriceEditText= view.findViewById(R.id.min_price_edit_text)
        maxPriceEditText = view.findViewById(R.id.max_price_edit_text)
        minRentPriceEditText = view.findViewById(R.id.min_rent_price_edit_text)
        maxRentPriceEditText = view.findViewById(R.id.max_rent_price_edit_text)
        minBedsEditText = view.findViewById(R.id.min_beds_edit_text)
        maxBedsEditText = view.findViewById(R.id.max_beds_edit_text)
        minBathsEditText = view.findViewById(R.id.min_baths_edit_text)
        maxBathsEditText = view.findViewById(R.id.max_baths_edit_text)
        minSqftEditText = view.findViewById(R.id.min_sqft_edit_text)
        maxSqftEditText = view.findViewById(R.id.max_sqft_edit_text)
        hasGarageCheckbox = view.findViewById(R.id.has_garage_checkbox)
        hasPoolCheckbox = view.findViewById(R.id.has_pool_checkbox)



        // Check if there is a saved search query
        if (lastSearchQuery != null) {
            // Use the saved search query to update the adapter
            updateAdapter(itemsRv, lastSearchQuery!!,lastOptionalUrl?:"")
        } else {
            // Initialize the default recycler view with user's current location
            setDefaultViewWithCurrentLocation(itemsRv)
        }
        // Parse query and use API endpoint
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                val statusType = statusTypeSpinner?.selectedItem.toString()
                val homeType = homeTypeSpinner?.selectedItem.toString()
                val sort = sortSpinner?.selectedItem.toString()
                val minPrice = minPriceEditText?.text.toString().toIntOrNull()
                val maxPrice = maxPriceEditText?.text.toString().toIntOrNull()
                val minRentPrice = minRentPriceEditText?.text.toString().toIntOrNull()
                val maxRentPrice = maxRentPriceEditText?.text.toString().toIntOrNull()
                val minBeds = minBedsEditText?.text.toString().toIntOrNull()
                val maxBeds = maxBedsEditText?.text.toString().toIntOrNull()
                val minBaths = minBathsEditText?.text.toString().toIntOrNull()
                val maxBaths = maxBathsEditText?.text.toString().toIntOrNull()
                val minSqft = minSqftEditText?.text.toString().toIntOrNull()
                val maxSqft = maxSqftEditText?.text.toString().toIntOrNull()
                val hasGarage = hasGarageCheckbox?.isChecked
                val hasPool = hasPoolCheckbox?.isChecked

                val urlBuilder = StringBuilder("")
                if (statusType.isNotEmpty()) {
                    urlBuilder.append("&status_type=$statusType")
                }
                if (homeType.isNotEmpty()) {
                    urlBuilder.append("&home_type=$homeType")
                }
                if (sort.isNotEmpty()) {
                    urlBuilder.append("&sort=$sort")
                }
                minPrice?.let { urlBuilder.append("&minPrice=$it") }
                maxPrice?.let { urlBuilder.append("&maxPrice=$it") }
                minRentPrice?.let { urlBuilder.append("&rentMinPrice=$it") }
                maxRentPrice?.let { urlBuilder.append("&rentMaxPrice=$it") }
                minBeds?.let { urlBuilder.append("&bedsMin=$it") }
                maxBeds?.let { urlBuilder.append("&bedsMax=$it") }
                minBaths?.let { urlBuilder.append("&bathsMin=$it") }
                maxBaths?.let { urlBuilder.append("&bathsMax=$it") }
                minSqft?.let { urlBuilder.append("&sqftMin=$it") }
                maxSqft?.let { urlBuilder.append("&sqftMax=$it") }
                if (hasGarage == true) urlBuilder.append("&hasGarage=true")
                if (hasPool == true) urlBuilder.append("&hasPool=true")
                optionalUrl= urlBuilder.toString()


                if (optionalUrl!!.isNotEmpty()) {
                    Log.d("optionalUrl", optionalUrl!!)
                    lastOptionalUrl = optionalUrl
                }


                //on submit send query to be encoded
                val locationQuery = encodeQuery(query)
                // pass encoded query to API
                if (locationQuery != null) {
                    Log.d("Location Query", locationQuery)
                    lastSearchQuery = locationQuery
                    updateAdapter(itemsRv, locationQuery,lastOptionalUrl?:"")
                }

                // Hide the keyboard
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)

                return true
            }
        })

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views by their ID
        val toggleButton = view.findViewById<ToggleButton>(R.id.toggle_filters)
        val filterLayout = view.findViewById<LinearLayout>(R.id.filter_layout)

        // Set up a listener to toggle the visibility of the filter layout
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If toggle is enabled, show filters
                filterLayout.visibility = View.VISIBLE
            } else {
                // Otherwise, hide filters
                filterLayout.visibility = View.GONE
            }
        }

        statusTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatusType = parent.getItemAtPosition(position).toString()
                updateSpinnersBasedOnStatusType(selectedStatusType)
                updatePriceFieldsBasedOnStatusType(selectedStatusType)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

    }

    val REQUEST_CODE = 1000
    /*
    * Navigate to Property Detail page when clicked
    */
    override fun onItemClick(item: PropertyModel) {
        //Toast.makeText(context, "test: " + item.zpid, Toast.LENGTH_LONG).show()
        val intent = Intent(context, PropertyDetail::class.java).apply {
            putExtra(PROPERTY_EXTRA, item)
        }
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("BrowseFragment", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        var locationQuery: String?
        if (lastSearchQuery != null) {
            locationQuery = lastSearchQuery
        } else if (zipcode != null) {
            locationQuery = zipcode
        } else {
            locationQuery = "90024"
        }
        updateAdapter(itemsRv, locationQuery!!, lastOptionalUrl ?: "")
    }

    private fun updateSpinnersBasedOnStatusType(statusType: String) {
        when (statusType) {
            "ForRent" -> {
                homeTypeSpinner?.adapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.home_type_options_for_rent,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                sortSpinner?.adapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.sort_options_for_rent,
                    android.R.layout.simple_spinner_item
                )
            }
            "ForSale", "RecentlySold" -> {
                homeTypeSpinner?.adapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.home_type_options_others,
                    android.R.layout.simple_spinner_item
                )
                sortSpinner?.adapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.sort_options_for_sale_or_sold,
                    android.R.layout.simple_spinner_item
                )
            }
        }
        // Reset spinner selections
        homeTypeSpinner?.setSelection(0)
        sortSpinner?.setSelection(0)
    }

    private fun updatePriceFieldsBasedOnStatusType(statusType: String) {
        when (statusType) {
            "ForRent" -> {
                minPriceEditText?.visibility = View.GONE
                maxPriceEditText?.visibility = View.GONE
                minRentPriceEditText?.visibility = View.VISIBLE
                maxRentPriceEditText?.visibility = View.VISIBLE
            }
            "ForSale", "RecentlySold" -> {
                minPriceEditText?.visibility = View.VISIBLE
                maxPriceEditText?.visibility = View.VISIBLE
                minRentPriceEditText?.visibility = View.GONE
                maxRentPriceEditText?.visibility = View.GONE
            }
        }
    }
    private fun updateAdapter(recyclerView: RecyclerView, query: String, lastOptionalUrl: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val properties = withContext(Dispatchers.IO) {
                fetchProperties(query, lastOptionalUrl).toMutableList()
            }

            if (properties.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                noResultView?.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                noResultView?.visibility = View.GONE
                // Ensure the context and the database access are valid
                context?.let { ctx ->
                    val dbDao = (activity?.application as? SavedHomesApplication)?.db?.savedHomesDao()
                    dbDao?.let { dao ->
                        recyclerView.adapter = PropertyItemAdapter(
                            ctx,
                            dao,
                            properties,
                            isSavedHomesScreen = false,
                            this@BrowseFragment
                        )
                    }
                }
            }
            Log.d("BrowseFragment", "Adapter updated successfully")
        }
    }
    private suspend fun fetchProperties(query: String, lastOptionalUrl: String?): List<PropertyModel> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://zillow-com1.p.rapidapi.com/propertyExtendedSearch?location=${query}${lastOptionalUrl}")
            .addHeader("X-RapidAPI-Key", API_KEY)
            .addHeader("X-RapidAPI-Host", "zillow-com1.p.rapidapi.com")
            .get()
            .build()

        return client.newCall(request).execute().use { response ->
            response.body?.string()?.let { responseBody ->
                parseProperty(responseBody) // Assume parseProperty returns List<PropertyModel>
            } ?: emptyList()
        }
    }



    /*
    * Update recycler view adapter with the list of properties for the property cards
    */
    /*private fun updateAdapter(recyclerView: RecyclerView, query: String, lastOptionalUrl: String?) {

        GlobalScope.launch(IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://zillow-com1.p.rapidapi.com/propertyExtendedSearch?location=${query}${lastOptionalUrl}")
                .addHeader("X-RapidAPI-Key", API_KEY)
                .addHeader("X-RapidAPI-Host", "zillow-com1.p.rapidapi.com")
                .get()
                .build()

           // val response = client.newCall(request).execute()
            //val properties = response.body?.let { parseProperty(it.string()) }


            val response = client.newCall(request).execute()
            val properties = mutableListOf<PropertyModel>()
            response.body?.let { responseBody ->
                parseProperty(responseBody.string())?.let { properties.addAll(it) }
            }

            withContext(Dispatchers.Main) {
                if (properties.isNullOrEmpty()) {
                    recyclerView.visibility = View.GONE
                    noResultView!!.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    noResultView!!.visibility = View.GONE
                    recyclerView.adapter = context?.let { PropertyItemAdapter(
                        it,
                        (activity?.application as SavedHomesApplication).db.savedHomesDao(),
                        properties,
                        isSavedHomesScreen = false,
                        this@BrowseFragment) }
                }
                Log.d("BrowseFragment", "response successful")
            }
        }
    }*/

    /*
    * Encode user search query to URL
    */
    private fun encodeQuery(query: String): String? {
        return URLEncoder.encode(query, "UTF-8")
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
                    val latitude = propObject.optDouble("latitude")
                    val longitude = propObject.optDouble("longitude")

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
                        imageUrl = imageUrl,
                        longitude = longitude,
                        latitude = latitude
                    )
                    properties.add(property)
                }
            } else {
                Log.d("BrowseFragment", "No results found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return properties
    }


    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.isNotEmpty() && permissions.all { it.value }) {
            itemsRv?.let { rv ->
                setDefaultViewWithCurrentLocation(rv)
            }
        } else {
            // Handle permission denied
        }
    }

    private fun setDefaultViewWithCurrentLocation(recyclerView: RecyclerView) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        lifecycleScope.launch {
            val location = withContext(Dispatchers.IO) {
                val locationTask = fusedLocationClient.lastLocation
                locationTask.await()
            }

            if (location != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                zipcode = addresses?.firstOrNull()?.postalCode

                if (zipcode != null) {
                    withContext(Dispatchers.Main) {
                        updateAdapter(recyclerView, zipcode!!,"")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        updateAdapter(recyclerView, "90024","") // default zip code
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    updateAdapter(recyclerView, "90024","") // default zip code
                }
            }
        }
    }
}