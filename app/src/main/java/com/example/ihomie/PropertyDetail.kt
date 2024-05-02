package com.example.ihomie

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.NumberFormat
import java.util.Locale
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.content.pm.PackageManager
import android.text.method.LinkMovementMethod
import android.widget.Toast



class PropertyDetail  : BaseActivity() {

    private lateinit var bigImageView: ImageView
    private lateinit var thumbnailRecyclerView: RecyclerView
    private lateinit var dataTextView: TextView
    private  var thumbnails: MutableList<String> = mutableListOf()
    private lateinit var adapter:DetailImageAdapter
    private lateinit var saveButton: FloatingActionButton
    private lateinit var savedHomesDao: SavedHomesDao

    var propertyzpid:String=""
    // Declare currentOrientation as a member variable
    private var currentOrientation = Configuration.ORIENTATION_UNDEFINED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.property_detail)

        bigImageView = findViewById(R.id.bigImageView)
        thumbnailRecyclerView = findViewById(R.id.thumbnailRecyclerView)
        dataTextView = findViewById(R.id.dataTextView)

        var mPropertyModel: PropertyModel? = null

        val addressTextView: TextView = findViewById(R.id.tv_address)
        var priceTextView: TextView = findViewById(R.id.tv_price)
        val listingStatusTextView: TextView = findViewById(R.id.tv_listing_status)
        val bedroomTextView: TextView = findViewById(R.id.tv_bedroom)
        val bathroomTextView: TextView = findViewById(R.id.tv_bathroom)
        val sqftTextView: TextView = findViewById(R.id.tv_sqft)

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.maximumFractionDigits = 0
        val propertyModel = intent.getSerializableExtra(PROPERTY_EXTRA) as PropertyModel?
        propertyModel?.let {
            val formattedPrice = currencyFormat.format(propertyModel.price)
            priceTextView.text = formattedPrice
            bedroomTextView.text = "${propertyModel.bedrooms} bds  |  "
            bathroomTextView.text ="${propertyModel.bathrooms} ba  |  "
            addressTextView.text= propertyModel.address
            sqftTextView.text = "${propertyModel.sqft} sqft |  ${propertyModel.zpid} zpid"
            listingStatusTextView.text = propertyModel.listingStatus?.replace("_", " ")
            propertyzpid = propertyModel.zpid!!
        }


        adapter = DetailImageAdapter(thumbnails) { imageUrl ->
            // Update the big image when a thumbnail is clicked
            Glide.with(this@PropertyDetail)
                .load(imageUrl)
                .into(bigImageView)
        }
        thumbnailRecyclerView.adapter = adapter

        //Store the current orientation
        currentOrientation = resources.configuration.orientation

        // Register a listener to detect orientation changes
        val orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                // Update the current orientation
                currentOrientation = resources.configuration.orientation
                // Update the orientation of the LinearLayoutManager
                updateRecyclerViewOrientation()
            }
        }

        // Start listening for orientation changes
        orientationEventListener.enable()

        // Set up the RecyclerView initially
        updateRecyclerViewOrientation()

        // Fetch the real estate images from the API
        fetchRealEstateImages()
        fetchScrollContent()

        saveButton = findViewById(R.id.add_button)
        savedHomesDao = AppDatabase.getInstance(applicationContext).savedHomesDao()
        var savedHomesList: List<SavedHomes>

        // Handle save button click
        saveButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                savedHomesList = savedHomesDao.getAllSavedHomes()

                if (propertyModel != null) {
                    if (savedHomesList.any { it.zpid == propertyModel.zpid }) {
                        propertyModel.zpid?.let {
                            savedHomesDao.delete(it)
                            saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                            Log.d("Database", "Deleted ZPID: $it")
                        }
                    } else {
                        val zpid = propertyModel.zpid?.let { SavedHomes(zpid = it) }
                        zpid?.let {
                            savedHomesDao.insert(it)
                            saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1BAAE"))
                            Log.d("Database", "Inserted ZPID: ${it.zpid}")
                        }
                    }
                }
            }
        }
        // Update button color according to whether the zpid is in the database
        CoroutineScope(Dispatchers.IO).launch {
            savedHomesList = savedHomesDao.getAllSavedHomes()
            if (propertyModel != null) {
                if (savedHomesList.any { it.zpid == propertyModel.zpid }) {
                    saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1BAAE"))
                } else {
                    saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    override fun onBackPressed() {
        // If a property was saved or unsaved, set result OK to indicate a change
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    private fun updateRecyclerViewOrientation() {
        // Update the orientation of the LinearLayoutManager based on device orientation
        thumbnailRecyclerView.layoutManager = if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        } else {
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun fetchRealEstateImages(){

        val client = AsyncHttpClient()
        val params = RequestParams()
        params["rapidapi-key"]= API_KEY
        params["zpid"]= "$propertyzpid"

        client["https://zillow-com1.p.rapidapi.com/images?",params,object:
            JsonHttpResponseHandler() {
            // Inside fetchRealEstateImages function, onSuccess callback
            override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                Log.d("images Success", "$json")
                val thumbnailImageArray = json.jsonObject.getJSONArray("images")
                thumbnails.clear() // Clear existing items
                for (i in 0 until thumbnailImageArray.length()) {
                    val imageUrl = thumbnailImageArray.getString(i)
                    thumbnails.add(imageUrl) // Add the URL directly to the list
                }
                adapter.notifyDataSetChanged() // Notify the adapter of the dataset change
                if (thumbnails.isNotEmpty()) {
                    Glide.with(this@PropertyDetail).load(thumbnails[0]).into(bigImageView) // Load the first image into the bigImageView
                }
            }


            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String,
                throwable: Throwable?
            ) {
                Log.d("Thumbnail Error", errorResponse)
            }
        }]

    }
    private fun fetchScrollContent() {
        // Launch the network request in a coroutine
        lifecycleScope.launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://zillow-com1.p.rapidapi.com/property?zpid=$propertyzpid")
                    .get()
                    .addHeader("X-RapidAPI-Key", API_KEY)
                    .addHeader("X-RapidAPI-Host", "zillow-com1.p.rapidapi.com")
                    .build()

                // Perform the network request in a background thread using withContext
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                val responseBody = response.body?.string()

                // Parse the JSON response using Gson
                val gson = Gson()
                val property = gson.fromJson(responseBody, Property::class.java)

                // Update the UI with the parsed data in the main thread
                withContext(Dispatchers.Main) {
                    updateUI(property)
                }
            } catch (e: Exception) {
                // Handle any exceptions
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(property: Property) {
        // Update the UI with the parsed data
        val spannableStringBuilder = SpannableStringBuilder()


        if (!property.description.isNullOrEmpty()) {
            val descriptionStart = spannableStringBuilder.length
            spannableStringBuilder.append("${property.description}\n\n")
            val descriptionEnd = spannableStringBuilder.length
            /* val shadowColor = Color.WHITE
             spannableStringBuilder.setSpan(
                 BackgroundColorSpan(ContextCompat.getColor(this@PropertyDetail, R.color.light_background)),
                 descriptionStart,
                 descriptionEnd,
                 Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
             )*/
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@PropertyDetail, R.color.black)),
                descriptionStart,
                descriptionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (property.isListedByOwner==true) {
            spannableStringBuilder.append("Is Listed By Owner: ${property.isListedByOwner}\n\n")
        }
        // Display contact recipients
        if(!property.contactRecipients.isNullOrEmpty()) {
            property.contactRecipients?.forEach { recipient ->
                if (!recipient.displayName.isNullOrEmpty() &&
                    !recipient.phone?.areacode.isNullOrEmpty() && !recipient.phone?.prefix.isNullOrEmpty() &&
                    !recipient.phone?.number.isNullOrEmpty()
                ) {
                    spannableStringBuilder.append("${recipient.badgeType}: ${recipient.displayName}\n")
                    spannableStringBuilder.append("Phone: (${recipient.phone?.areacode}) ${recipient.phone?.prefix}-${recipient.phone?.number}\n\n")
                }
            }
        }

        // Inside the updateUI function in PropertyDetail.kt

// Display listed_by information
        val listedBy = property.listed_by
        if (listedBy != null) {
            if (!listedBy.display_name.isNullOrEmpty()) {
                spannableStringBuilder.append("${listedBy.display_name}")
                if (!listedBy.badge_type.isNullOrEmpty()) {
                    spannableStringBuilder.append(" (${listedBy.badge_type})")
                }
                spannableStringBuilder.append("\n")
            }
            if (!listedBy.business_name.isNullOrEmpty()) {
                spannableStringBuilder.append("${listedBy.business_name}\n")
            }
            val phone = listedBy.phone
            if (phone != null && !phone.areacode.isNullOrEmpty() && !phone.prefix.isNullOrEmpty() && !phone.number.isNullOrEmpty()) {
                val phoneNumber = "(${phone.areacode}) ${phone.prefix}-${phone.number}"
                val phoneSpan = SpannableString(phoneNumber)
                phoneSpan.setSpan(UnderlineSpan(), 0, phoneNumber.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                phoneSpan.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // Check if the device supports phone calls
                        val packageManager = widget.context.packageManager
                        if (packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                            // Device supports phone calls, start the phone intent
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:$phoneNumber")
                            startActivity(intent)
                        } else {
                            // Device doesn't support phone calls, show a toast message
                            Toast.makeText(widget.context, "Phone calls are not supported on this device", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, 0, phoneNumber.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableStringBuilder.append("Phone: ")
                spannableStringBuilder.append(phoneSpan)
                spannableStringBuilder.append("\n\n")
            }
            spannableStringBuilder.append("\n")
        }

        // Display open house schedule
        val openHouseSchedule = property.openHouseSchedule
        if (!openHouseSchedule.isNullOrEmpty()) {
            val openHouseScheduleStart = spannableStringBuilder.length
            spannableStringBuilder.append("Open House Schedule\n")
            val openHouseScheduleEnd = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                openHouseScheduleStart,
                openHouseScheduleEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            openHouseSchedule.forEach { schedule ->
                if (!schedule.startTime.isNullOrEmpty() && !schedule.endTime.isNullOrEmpty()) {
                    spannableStringBuilder.append("Start Time: ${schedule.startTime}\n")
                    spannableStringBuilder.append("End Time: ${schedule.endTime}\n")
                    spannableStringBuilder.append("\n")
                }
            }

        }
        val resoFacts = property.resoFacts
        if (!resoFacts?.availabilityDate.isNullOrEmpty()) {
            spannableStringBuilder.append("Available Date: ${resoFacts?.availabilityDate}\n\n")
        }

        /*
        Display atAGlanceFacts
         */
        if (resoFacts?.atAGlanceFacts?.isNotEmpty()==true) {
            val atAGlanceFactsStart = spannableStringBuilder.length
            spannableStringBuilder.append("At a Glance\n").apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    atAGlanceFactsStart,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    AbsoluteSizeSpan(22, true),
                    atAGlanceFactsStart,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            resoFacts?.atAGlanceFacts?.forEach { fact ->
                if (!fact.factLabel.isNullOrEmpty() && !fact.factValue.isNullOrEmpty()) {
                    spannableStringBuilder.append("${fact.factLabel}: ${fact.factValue}\n")
                }
            }
            spannableStringBuilder.append("\n")
        }

        val otherFacts = resoFacts?.otherFacts
        if (otherFacts != null) {
            when (otherFacts) {
                is List<*> -> {
                    if (otherFacts.isNotEmpty()) {
                        spannableStringBuilder.append("Other Facts:\n")
                        otherFacts.forEach { otherFact ->
                            spannableStringBuilder.append("- $otherFact\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
                is String -> {
                    if (otherFacts.isNotBlank()) {
                        spannableStringBuilder.append("Other Facts:\n")
                        spannableStringBuilder.append("- $otherFacts\n")
                        spannableStringBuilder.append("\n")
                    }
                }
                is Map<*, *> -> {
                    if (otherFacts.isNotEmpty()) {
                        spannableStringBuilder.append("Other Facts:\n")
                        otherFacts.forEach { (key, value) ->
                            spannableStringBuilder.append("$key: $value\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
            }
        }

//        if (property.price != 0&&property.price!=null) {
//            spannableStringBuilder.append("Price: $${property.price}\n")
//        }


        if (property.zestimate != 0&&property.zestimate!=null) {
            spannableStringBuilder.append("Zestimate: $${property.zestimate}\n")
        }
        if (property.rentZestimate != 0&&property.rentZestimate!=null) {
            spannableStringBuilder.append("Rent Zestimate: $${property.rentZestimate}\n")
        }
        if (property.propertyTaxRate != 0.0) {
            spannableStringBuilder.append("Property Tax Rate: ${property.propertyTaxRate}\n")
        }
//        if(property.address!=null){
//            spannableStringBuilder.append("Address:\n")
//            spannableStringBuilder.append("${property.address.streetAddress}\n")
//            spannableStringBuilder.append("${property.address.city}, ${property.address.state} ${property.address.zipcode}\n\n")
//        }
//
//        if (property.zpid != 0) {
//            spannableStringBuilder.append("ZPID: ${property.zpid}\n\n")
//        }
//        if (property.longitude != 0.0) {
//            spannableStringBuilder.append("Longitude: ${property.longitude}\n")
//        }
//        if (property.latitude != 0.0) {
//            spannableStringBuilder.append("Latitude: ${property.latitude}\n\n")
//        }
        // Display listingSubtype
        val listingSubtype = property.listingSubtype
        if (listingSubtype != null) {
            val listingSubtypeBuilder = SpannableStringBuilder()


            if (listingSubtype.isFSBA == true) {
                listingSubtypeBuilder.append("Is FSBA, ")
            }
            if (listingSubtype.iscomingSoon == true) {
                listingSubtypeBuilder.append("Is Coming Soon, ")
            }
            if (listingSubtype.isnewHome == true) {
                listingSubtypeBuilder.append("Is New Home, ")
            }
            if (listingSubtype.ispending == true) {
                listingSubtypeBuilder.append("Is Pending, ")
            }
            if (listingSubtype.isforAuction == true) {
                listingSubtypeBuilder.append("Is For Auction, ")
            }
            if (listingSubtype.isforeclosure == true) {
                listingSubtypeBuilder.append("Is Foreclosure, ")
            }
            if (listingSubtype.isbankOwned == true) {
                listingSubtypeBuilder.append("Is Bank Owned, ")
            }
            if (listingSubtype.isopenHouse == true) {
                listingSubtypeBuilder.append("Is Open House, ")
            }
            if (listingSubtype.isFSBO == true) {
                listingSubtypeBuilder.append("Is FSBO, ")
            }

            val listingSubtypeText = listingSubtypeBuilder.toString().trimEnd(',')
            if (!listingSubtypeText.isNullOrEmpty()) {
                spannableStringBuilder.append("Listing Subtype: $listingSubtypeText\n\n")
            }
            spannableStringBuilder.append("\n")
        }


        /*
        Display Interior
         */
        val interiorStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nInterior\n")
        val interiorEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            interiorStart,
            interiorEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.setSpan(
            AbsoluteSizeSpan(22, true),
            interiorStart,
            interiorEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Bedrooms & Bathrooms
        val bdbaStart = spannableStringBuilder.length
        spannableStringBuilder.append("Bedrooms & Bathrooms\n")
        val bdbaEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            bdbaStart,
            bdbaEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (resoFacts?.bedrooms!=null) {
            spannableStringBuilder.append(" - Bedrooms: ${resoFacts?.bedrooms}\n")
        }
        if (resoFacts?.bathrooms!=null) {
            spannableStringBuilder.append(" - Bathrooms: ${resoFacts?.bathrooms}\n")
        }

        // Heating
        val heatingStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nHeating\n")
        val heatingEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            heatingStart,
            heatingEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (resoFacts?.heating?.isNotEmpty()==true) {
            spannableStringBuilder.append(" - ${resoFacts.heating.joinToString(", ")}\n")
        } else {
            spannableStringBuilder.append(" - None\n")
        }

        // Cooling
        val coolingStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nCooling\n")
        val coolingEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            coolingStart,
            coolingEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (resoFacts?.cooling?.isNotEmpty()==true) {
            spannableStringBuilder.append(" - ${resoFacts.cooling.joinToString(", ")}\n")
        } else {
            spannableStringBuilder.append(" - None\n")
        }

        // Flooring
        val flooringStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nFlooring\n")
        val flooringEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            flooringStart,
            flooringEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (resoFacts?.flooring?.isNotEmpty()==true) {
            spannableStringBuilder.append(" - ${resoFacts.flooring.joinToString(", ")}\n")
        } else {
            spannableStringBuilder.append(" - None\n")
        }


        // Appliances
        val appliancesStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nAppliances\n")
        val appliancesEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            appliancesStart,
            appliancesEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (resoFacts?.appliances?.isNotEmpty()==true){
            spannableStringBuilder.append(" - ${resoFacts.appliances.joinToString(", ")}\n")
        } else {
            spannableStringBuilder.append(" - None\n")
        }

        // Interior Features
        val interiorFeaturesStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nFeatures\n")
        val interiorFeaturesEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            interiorFeaturesStart,
            interiorFeaturesEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (resoFacts?.interiorFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append(" - Interior: ${resoFacts.interiorFeatures.joinToString(", ")}\n")
        }
        if (!resoFacts?.doorFeatures.isNullOrEmpty()) {
            spannableStringBuilder.append(" - Door: ${resoFacts?.doorFeatures?.joinToString(", ")}\n")
        }
        if (resoFacts?.fireplaceFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append(" - Fireplace: ${resoFacts?.fireplaceFeatures.joinToString(", ")}\n")
        }
        if (resoFacts?.laundryFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append(" - Laundry: ${resoFacts.laundryFeatures.joinToString(", ")}\n")
        }
        if (!resoFacts?.securityFeatures.isNullOrEmpty()) {
            spannableStringBuilder.append(" - Security: ${resoFacts?.securityFeatures?.joinToString(", ")}\n")
        }
        if (resoFacts?.basementYN==true) {
            spannableStringBuilder.append(" - Basement: ${resoFacts.basementYN}\n")
        }
        if (resoFacts?.hasPetsAllowed==true) {
            spannableStringBuilder.append(" - Pets Allowed: ${resoFacts.hasPetsAllowed}\n")
        }
        if (!resoFacts?.attic.isNullOrEmpty()) {
            spannableStringBuilder.append("Attic: ${resoFacts?.attic}\n")
        }
        if (!resoFacts?.fencing.isNullOrEmpty()) {
            spannableStringBuilder.append("Fencing: ${resoFacts?.fencing}\n")
        }


        /*
        Display Property
         */
        val propertyStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nProperty\n")
        val propertyEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            propertyStart,
            propertyEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.setSpan(
            AbsoluteSizeSpan(22, true),
            propertyStart,
            propertyEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if (resoFacts?.yearBuilt!=null) {
            spannableStringBuilder.append("Year Built: ${resoFacts?.yearBuilt}\n")
        }
        if (!resoFacts?.yearBuiltEffective.isNullOrEmpty()) {
            spannableStringBuilder.append("Year Built Effective: ${resoFacts?.yearBuiltEffective}\n\n")
        }
        if (!property.homeType.isNullOrEmpty()) {
            spannableStringBuilder.append("Home Type: ${property.homeType}\n")
        }
        if (!resoFacts?.architecturalStyle.isNullOrEmpty()) {
            spannableStringBuilder.append("Architectural Style: ${resoFacts?.architecturalStyle}\n")
        }
        if (!resoFacts?.structureType.isNullOrEmpty()) {
            spannableStringBuilder.append("Structure Type: ${resoFacts?.structureType}\n")
        }
        if (resoFacts?.stories!=null) {
            spannableStringBuilder.append("Stories: ${resoFacts?.stories}\n")
        }
        if (!resoFacts?.lotSize.isNullOrEmpty()) {
            spannableStringBuilder.append("Lot Size: ${resoFacts?.lotSize}\n")
        }
        if (resoFacts?.isSeniorCommunity==true) {
            spannableStringBuilder.append("Is Senior Community: ${resoFacts.isSeniorCommunity}\n")
        }

        val foundationDetails = resoFacts?.foundationDetails
        if (foundationDetails != null){

            when (foundationDetails) {
                is List<*> -> {
                    if (foundationDetails.isNotEmpty()) {
                        spannableStringBuilder.append("Foundation Details:\n")
                        foundationDetails.forEach { detail ->
                            spannableStringBuilder.append("- $detail\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
                is String -> {
                    if (foundationDetails.isNotBlank()) {
                        spannableStringBuilder.append("Foundation Details:\n")
                        foundationDetails.forEach { detail ->
                            spannableStringBuilder.append("- $foundationDetails\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
                is Map<*, *> -> {
                    if (foundationDetails.isNotEmpty()) {
                        spannableStringBuilder.append("Foundation Details:\n")
                        foundationDetails.forEach { (key, value) ->
                            spannableStringBuilder.append("$key: $value\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
            }
        }

        if (!resoFacts?.roofType.isNullOrEmpty()) {
            spannableStringBuilder.append("Roof Type: ${resoFacts?.roofType}\n")
        }

        val constructionMaterials = resoFacts?.constructionMaterials
        if (constructionMaterials != null) {
            spannableStringBuilder.append("Construction Materials:\n")
            when (constructionMaterials) {
                is List<*> -> {
                    constructionMaterials.forEach { material ->
                        spannableStringBuilder.append("- $material\n")
                    }
                }
                is String -> {
                    spannableStringBuilder.append("- $constructionMaterials\n")
                }
            }
            spannableStringBuilder.append("\n")
        }

        if (!resoFacts?.belowGradeFinishedArea.isNullOrEmpty()&& resoFacts?.belowGradeFinishedArea!="0") {
            spannableStringBuilder.append("Below Grade Finished Area: ${resoFacts?.belowGradeFinishedArea} sqft\n")
        }

        if (resoFacts?.hasAttachedProperty == true) {
            spannableStringBuilder.append("Has Attached Property: ${resoFacts.hasAttachedProperty}\n")
        }


        val parkingFeatures = resoFacts?.parkingFeatures
        if (parkingFeatures != null){
            val parkingFeaturesStart = spannableStringBuilder.length
            spannableStringBuilder.append("\nParking Features\n")
            val parkingFeaturesEnd = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                parkingFeaturesStart,
                parkingFeaturesEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            when (parkingFeatures) {
                is List<*> -> {
                    if (parkingFeatures.isNotEmpty()) {
                        parkingFeatures.forEach { detail ->
                            spannableStringBuilder.append("- $detail\n")
                        }
                    }
                }
                is String -> {
                    if (parkingFeatures.isNotBlank()) {
                        spannableStringBuilder.append("- $parkingFeatures\n")
                    }
                }
                is Map<*, *> -> {
                    if (parkingFeatures.isNotEmpty()) {
                        parkingFeatures.forEach { (key, value) ->
                            spannableStringBuilder.append("$key: $value\n")
                        }
                    }
                }
            }
        }

//        if (resoFacts?.hasGarage==true) {
//            spannableStringBuilder.append("Has Garage: ${resoFacts.hasGarage}\n")
//        }
        if (resoFacts?.hasAttachedGarage==true) {
            spannableStringBuilder.append("Attached Garage: ${resoFacts.hasAttachedGarage}\n")
        }
        if (!resoFacts?.garageParkingCapacity.isNullOrEmpty()&& resoFacts?.garageParkingCapacity!="0") {
            spannableStringBuilder.append("Garage Parking Capacity: ${resoFacts?.garageParkingCapacity}\n")
        }
        if (!resoFacts?.carportParkingCapacity.isNullOrEmpty()&& resoFacts?.carportParkingCapacity!="0") {
            spannableStringBuilder.append("Carport Spaces: ${resoFacts?.carportParkingCapacity}\n")
        }


        // Water View
        if (resoFacts?.waterViewYN==true) {
            spannableStringBuilder.append("Water View: ${resoFacts.waterViewYN}\n")
        }

        if (!resoFacts?.waterView.isNullOrEmpty()) {
            spannableStringBuilder.append("Water View: ${resoFacts?.waterView}\n")
        }

        // Pool
        val poolStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nPool Features\n")
        val poolEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            poolStart,
            poolEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
//        if(resoFacts?.hasPrivatePool==true){
//            spannableStringBuilder.append("Private Pool: ${resoFacts.hasPrivatePool}\n")
//        }
        if (resoFacts?.poolFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append(" - ${resoFacts.poolFeatures.joinToString(", ")}\n")
        }

        // Display Other Property Information
        val otherPropertyInformationStart = spannableStringBuilder.length
        spannableStringBuilder.append("\nOther Property Information\n")
        val otherPropertyInformationEnd = spannableStringBuilder.length
        spannableStringBuilder.setSpan(
            StyleSpan(Typeface.BOLD),
            otherPropertyInformationStart,
            otherPropertyInformationEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if (!property.datePosted.isNullOrEmpty()) {
            spannableStringBuilder.append("Date Posted: ${property.datePosted}\n")
        }

        val waterSource = resoFacts?.waterSource
        if (!waterSource.isNullOrEmpty()) {
            spannableStringBuilder.append("Water Source: ${waterSource.joinToString(", ")}\n")
        }

        val gas = resoFacts?.gas
        if (!gas.isNullOrEmpty()) {
            spannableStringBuilder.append("Gas: ${gas.joinToString(", ")}\n")
        }

        val sewer = resoFacts?.sewer
        if (!sewer.isNullOrEmpty()) {
            spannableStringBuilder.append("Sewer: ${sewer.joinToString(", ")}\n")
        }

        if (!resoFacts?.electric.isNullOrEmpty()) {
            spannableStringBuilder.append("Electric: ${resoFacts?.electric?.joinToString(", ")}\n")
        }


        if (!resoFacts?.parcelNumber.isNullOrEmpty()) {
            spannableStringBuilder.append("\nParcel Number: ${resoFacts?.parcelNumber}\n")
        }
        if (!resoFacts?.zoning.isNullOrEmpty()) {
            spannableStringBuilder.append("Zoning: ${resoFacts?.zoning}\n")
        }
        if (!resoFacts?.ownershipType.isNullOrEmpty()) {
            spannableStringBuilder.append("Ownership Type: ${resoFacts?.ownershipType}\n")
        }
        if (!resoFacts?.associationName.isNullOrEmpty()) {
            spannableStringBuilder.append("Association Name: ${resoFacts?.associationName}\n")
        }
        if (!resoFacts?.hoaFee.isNullOrEmpty()) {
            spannableStringBuilder.append("HOA Fee: ${resoFacts?.hoaFee}\n")
        }
        if (resoFacts?.taxAnnualAmount!=null) {
            spannableStringBuilder.append("Tax Annual Amount: $${resoFacts?.taxAnnualAmount}\n")
        }
        if (property.annualHomeownersInsurance != 0 &&property.annualHomeownersInsurance!=null) {
            spannableStringBuilder.append("Annual Homeowners Insurance: $${property.annualHomeownersInsurance}\n")
        }
        if (property.mortgageRates?.thirtyYearFixedRate != 0.0&&property.mortgageRates?.thirtyYearFixedRate!=null) {
            spannableStringBuilder.append("Mortgage Rates - 30 Year Fixed Rate: ${property.mortgageRates?.thirtyYearFixedRate}\n")
        }

        val feesAndDues = resoFacts?.feesAndDues
        if (feesAndDues is List<*> && feesAndDues.isNotEmpty()) {
            spannableStringBuilder.append("\nFees and Dues:\n")
            feesAndDues.forEach { feeAndDue ->
                if (feeAndDue is Map<*, *>) {
                    val phone = feeAndDue["phone"] as? String
                    val name = feeAndDue["name"] as? String
                    val fee = feeAndDue["fee"] as? String
                    val type = feeAndDue["type"] as? String

                    if (!name.isNullOrBlank()) {
                        spannableStringBuilder.append("Name: $name\n")
                    }
                    if (!fee.isNullOrBlank()) {
                        spannableStringBuilder.append("Fee: $fee\n")
                    }
                    if (!type.isNullOrBlank()) {
                        spannableStringBuilder.append("Type: $type\n")
                    }
                    if (!phone.isNullOrBlank()) {
                        spannableStringBuilder.append("Phone: $phone\n")
                    }
                }
            }
        }

// Display price history
        if (property.priceHistory?.isNotEmpty()==true) {
            val priceHistoryStart = spannableStringBuilder.length
            spannableStringBuilder.append("\nPrice History\n")
            val priceHistoryEnd = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                priceHistoryStart,
                priceHistoryEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStringBuilder.setSpan(
                AbsoluteSizeSpan(22, true),
                priceHistoryStart,
                priceHistoryEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            property.priceHistory?.forEach { history ->
                if (!history.date.isNullOrEmpty() && !history.event.isNullOrEmpty()) {
                    spannableStringBuilder.append("Date: ${history.date}\n")
                    spannableStringBuilder.append("Event: ${history.event}\n")
                    if (history.price != null) {
                        spannableStringBuilder.append("Price: $${history.price}\n\n")
                    }
                }
            }
        }

// Display schools

        if (property.schools?.isNotEmpty() == true) {
            val schoolsStart = spannableStringBuilder.length
            spannableStringBuilder.append("Schools:\n")
            val schoolsEnd = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                schoolsStart,
                schoolsEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            property.schools?.forEach { school ->
                if (school.rating != 0 && school.distance != 0.0 && !school.name.isNullOrEmpty() &&
                    !school.level.isNullOrEmpty() && !school.grades.isNullOrEmpty() && !school.type.isNullOrEmpty()) {
                    spannableStringBuilder.append("Rating: ${school.rating}\n")
                    spannableStringBuilder.append("Distance: ${school.distance}\n")
                    spannableStringBuilder.append("Name: ${school.name}\n")
                    spannableStringBuilder.append("Level: ${school.level}\n")
                    spannableStringBuilder.append("Grades: ${school.grades}\n")
                    spannableStringBuilder.append("Type: ${school.type}\n\n")
                }
            }
        }





// Display the formatted data

        dataTextView.text = spannableStringBuilder
        dataTextView.movementMethod = LinkMovementMethod.getInstance()


    }


}