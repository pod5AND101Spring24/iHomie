package com.example.ihomie

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import androidx.recyclerview.widget.DividerItemDecoration
import java.text.NumberFormat
import java.util.Locale
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.MaskFilter
import androidx.core.content.ContextCompat
import android.text.style.*
import androidx.appcompat.widget.Toolbar


class PropertyDetail  : BaseActivity() {

    private lateinit var bigImageView: ImageView
    private lateinit var thumbnailRecyclerView: RecyclerView
    private lateinit var dataTextView: TextView
    private  var thumbnails: MutableList<String> = mutableListOf()
    private lateinit var adapter:DetailImageAdapter
    var propertyzpid:String=""

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
        // Set up the RecyclerView
        thumbnailRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        thumbnailRecyclerView.addItemDecoration(DividerItemDecoration(this@PropertyDetail,LinearLayoutManager.HORIZONTAL))

        // Fetch the real estate images from the API
        fetchRealEstateImages()
        fetchScrollContent()


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
                ForegroundColorSpan(ContextCompat.getColor(this@PropertyDetail, R.color.teal)),
                descriptionStart,
                descriptionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            spannableStringBuilder.setSpan(
                AbsoluteSizeSpan(20, true),
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

        // Display open house schedule
        val openHouseSchedule = property.openHouseSchedule
        if (!openHouseSchedule.isNullOrEmpty()) {
            val openHouseScheduleStart = spannableStringBuilder.length
            spannableStringBuilder.append("Open House Schedule:\n")
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
        // Display atAGlanceFacts
        if (resoFacts?.atAGlanceFacts?.isNotEmpty()==true) {
            val atAGlanceFactsStart = spannableStringBuilder.length
            spannableStringBuilder.append("At A Glance Facts:\n")
            val atAGlanceFactsEnd = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                atAGlanceFactsStart,
                atAGlanceFactsEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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


        if (property.price != 0&&property.price!=null) {
            spannableStringBuilder.append("Price: $${property.price}\n")
        }


        if (property.propertyTaxRate != 0.0) {
            spannableStringBuilder.append("Property Tax Rate: ${property.propertyTaxRate}\n\n")
        }
        if (property.zestimate != 0&&property.zestimate!=null) {
            spannableStringBuilder.append("Zestimate: $${property.zestimate}\n\n")
        }
        if (property.rentZestimate != 0&&property.rentZestimate!=null) {
            spannableStringBuilder.append("Rent Zestimate: $${property.rentZestimate}\n\n")
        }
        if(property.address!=null){
            spannableStringBuilder.append("Address:\n")
            spannableStringBuilder.append("${property.address.streetAddress}\n")
            spannableStringBuilder.append("${property.address.city}, ${property.address.state} ${property.address.zipcode}\n\n")
        }


        if (property.zpid != 0) {
            spannableStringBuilder.append("ZPID: ${property.zpid}\n\n")
        }
        if (property.longitude != 0.0) {
            spannableStringBuilder.append("Longitude: ${property.longitude}\n")
        }
        if (property.latitude != 0.0) {
            spannableStringBuilder.append("Latitude: ${property.latitude}\n\n")
        }
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

// Display ResoFacts

        if (!resoFacts?.parcelNumber.isNullOrEmpty()) {
            spannableStringBuilder.append("Parcel Number: ${resoFacts?.parcelNumber}\n")
        }

        if (resoFacts?.taxAnnualAmount!=null) {
            spannableStringBuilder.append("Tax Annual Amount: $${resoFacts?.taxAnnualAmount}\n")
        }

        if (!resoFacts?.zoning.isNullOrEmpty()) {
            spannableStringBuilder.append("Zoning: ${resoFacts?.zoning}\n\n")
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

        val feesAndDues = resoFacts?.feesAndDues
        if (feesAndDues is List<*> && feesAndDues.isNotEmpty()) {
            spannableStringBuilder.append("Fees and Dues:\n")
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
                    spannableStringBuilder.append("\n")

                }
            }
        }

        if (!resoFacts?.lotSize.isNullOrEmpty()) {
            spannableStringBuilder.append("Lot Size: ${resoFacts?.lotSize}\n\n")
        }
        if (resoFacts?.stories!=null) {
            spannableStringBuilder.append("Stories: ${resoFacts?.stories}\n")
        }
        if (resoFacts?.bedrooms!=null) {
            spannableStringBuilder.append("Bedrooms: ${resoFacts?.bedrooms}\n")
        }
        if (resoFacts?.bathrooms!=null) {
            spannableStringBuilder.append("Bathrooms: ${resoFacts?.bathrooms}\n\n")
        }

        if (resoFacts?.yearBuilt!=null) {
            spannableStringBuilder.append("Year Built: ${resoFacts?.yearBuilt}\n")
        }
        if (!resoFacts?.yearBuiltEffective.isNullOrEmpty()) {
            spannableStringBuilder.append("Year Built Effective: ${resoFacts?.yearBuiltEffective}\n\n")
        }

        if (resoFacts?.isSeniorCommunity==true) {
            spannableStringBuilder.append("Is Senior Community: ${resoFacts.isSeniorCommunity}\n")
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
        if (resoFacts?.interiorFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append("Interior Features: ${resoFacts.interiorFeatures.joinToString(", ")}\n")
        }

        if (resoFacts?.basementYN==true) {
            spannableStringBuilder.append("Basement: ${resoFacts.basementYN}\n")
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
            spannableStringBuilder.append("Below Grade Finished Area: ${resoFacts?.belowGradeFinishedArea} sqft\n\n")
        }

        if (resoFacts?.hasAttachedProperty == true) {
            spannableStringBuilder.append("Has Attached Property: ${resoFacts.hasAttachedProperty}\n\n")
        }


        val parkingFeatures = resoFacts?.parkingFeatures
        if (parkingFeatures != null){
            when (parkingFeatures) {
                is List<*> -> {
                    if (parkingFeatures.isNotEmpty()) {
                        spannableStringBuilder.append("Parking Features:\n")
                        parkingFeatures.forEach { detail ->
                            spannableStringBuilder.append("- $detail\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
                is String -> {
                    if (parkingFeatures.isNotBlank()) {
                        spannableStringBuilder.append("Parking Features:\n")
                        parkingFeatures.forEach { detail ->
                            spannableStringBuilder.append("- $parkingFeatures\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
                is Map<*, *> -> {
                    if (parkingFeatures.isNotEmpty()) {
                        spannableStringBuilder.append("Parking Features:\n")
                        parkingFeatures.forEach { (key, value) ->
                            spannableStringBuilder.append("$key: $value\n")
                        }
                        spannableStringBuilder.append("\n")
                    }
                }
            }
        }

        if (resoFacts?.hasGarage==true) {
            spannableStringBuilder.append("Has Garage: ${resoFacts.hasGarage}\n")
        }
        if (resoFacts?.hasAttachedGarage==true) {
            spannableStringBuilder.append("Attached Garage: ${resoFacts.hasAttachedGarage}\n")
        }
        if (!resoFacts?.garageParkingCapacity.isNullOrEmpty()&& resoFacts?.garageParkingCapacity!="0") {
            spannableStringBuilder.append("Garage Parking Capacity: ${resoFacts?.garageParkingCapacity}\n\n")
        }
        if (!resoFacts?.carportParkingCapacity.isNullOrEmpty()&& resoFacts?.carportParkingCapacity!="0") {
            spannableStringBuilder.append("Carport Spaces: ${resoFacts?.carportParkingCapacity}\n\n")
        }

        if (resoFacts?.hasHeating==true) {
            spannableStringBuilder.append("\n")
            spannableStringBuilder.append("Has Heating: ${resoFacts.hasHeating}\n")
        }

        if (resoFacts?.heating?.isNotEmpty()==true) {
            spannableStringBuilder.append("Heating: ${resoFacts.heating.joinToString(", ")}\n\n")
        }

        if (resoFacts?.hasCooling==true) {
            spannableStringBuilder.append("Has Cooling: ${resoFacts.hasCooling}\n")
        }
        if (resoFacts?.cooling?.isNotEmpty()==true) {
            spannableStringBuilder.append("Heating: ${resoFacts.cooling.joinToString(", ")}\n\n")
        }


        if (resoFacts?.hasPetsAllowed==true) {
            spannableStringBuilder.append("Pets Allowed: ${resoFacts.hasPetsAllowed}\n")
        }
        if (resoFacts?.waterViewYN==true) {
            spannableStringBuilder.append("Water View: ${resoFacts.waterViewYN}\n")
        }
        if (!resoFacts?.waterView.isNullOrEmpty()) {
            spannableStringBuilder.append("Water View: ${resoFacts?.waterView}\n")
        }
        if(resoFacts?.hasPrivatePool==true){
            spannableStringBuilder.append("Private Pool: ${resoFacts.hasPrivatePool}\n")
        }
        if (resoFacts?.poolFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append("Pool Features: ${resoFacts.poolFeatures.joinToString(", ")}\n\n")
        }
        if (resoFacts?.flooring?.isNotEmpty()==true) {
            spannableStringBuilder.append("Flooring: ${resoFacts.flooring.joinToString(", ")}\n\n")
        }


        //Display watersource
        val waterSource = resoFacts?.waterSource
        if (!waterSource.isNullOrEmpty()) {
            spannableStringBuilder.append("Water Source: ${waterSource.joinToString(", ")}\n\n")
        }
        // Display gas
        val gas = resoFacts?.gas
        if (!gas.isNullOrEmpty()) {
            spannableStringBuilder.append("Gas: ${gas.joinToString(", ")}\n\n")
        }

// Display sewer
        val sewer = resoFacts?.sewer
        if (!sewer.isNullOrEmpty()) {
            spannableStringBuilder.append("Sewer: ${sewer.joinToString(", ")}\n\n")
        }
        if (!resoFacts?.electric.isNullOrEmpty()) {
            spannableStringBuilder.append("Electric: ${resoFacts?.electric?.joinToString(", ")}\n\n")
        }


// Display other ResoFacts fields



        if(resoFacts?.appliances?.isNotEmpty()==true){
            spannableStringBuilder.append("Appliances: ${resoFacts.appliances.joinToString(", ")}\n")
        }
        if (!resoFacts?.fencing.isNullOrEmpty()) {
            spannableStringBuilder.append("Fencing: ${resoFacts?.fencing}\n")
        }


        if (!resoFacts?.securityFeatures.isNullOrEmpty()) {
            spannableStringBuilder.append("Security Features: ${resoFacts?.securityFeatures?.joinToString(", ")}\n")
        }
        if (!resoFacts?.attic.isNullOrEmpty()) {
            spannableStringBuilder.append("Attic: ${resoFacts?.attic}\n")
        }
        if(resoFacts?.hasFireplace==true){
            spannableStringBuilder.append("Fireplace: ${resoFacts.hasFireplace}\n")
        }

        if (resoFacts?.fireplaceFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append("Fireplace Features: ${resoFacts?.fireplaceFeatures.joinToString(", ")}\n\n")
        }


        if (resoFacts?.laundryFeatures?.isNotEmpty()==true) {
            spannableStringBuilder.append("Laundry Features: ${resoFacts.laundryFeatures.joinToString(", ")}\n\n")
        }



        if (property.bedrooms != 0&&property.bedrooms!=null) {
            spannableStringBuilder.append("Bathrooms: ${property.bathrooms}\n")
        }
        if (property.bathrooms != 0&&property.bathrooms!=null) {
            spannableStringBuilder.append("Bathrooms: ${property.bathrooms}\n")
        }
        if (!resoFacts?.mainLevelBathrooms.isNullOrEmpty()) {
            spannableStringBuilder.append("Main Level Bathrooms: ${resoFacts?.mainLevelBathrooms}\n")
        }


        if (!resoFacts?.doorFeatures.isNullOrEmpty()) {
            spannableStringBuilder.append("Door Features: ${resoFacts?.doorFeatures?.joinToString(", ")}\n")

        }
        if (!resoFacts?.hoaFeeTotal.isNullOrEmpty()) {
            spannableStringBuilder.append("HOA Fee Total: ${resoFacts?.hoaFeeTotal}\n\n")
        }

        if (property.annualHomeownersInsurance != 0 &&property.annualHomeownersInsurance!=null) {
            spannableStringBuilder.append("Annual Homeowners Insurance: $${property.annualHomeownersInsurance}\n\n")
        }
        if (property.mortgageRates?.thirtyYearFixedRate != 0.0&&property.mortgageRates?.thirtyYearFixedRate!=null) {
            spannableStringBuilder.append("Mortgage Rates - 30 Year Fixed Rate: ${property.mortgageRates?.thirtyYearFixedRate}\n\n")
        }
        if (!property.datePosted.isNullOrEmpty()) {
            spannableStringBuilder.append("Date Posted: ${property.datePosted}\n\n")
        }

// Display price history
        if (property.priceHistory?.isNotEmpty()==true) {
            val priceHistoryStart = spannableStringBuilder.length
            spannableStringBuilder.append("Price History:\n")
            val priceHistoryEnd = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
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

    }


}