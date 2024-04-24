package com.example.ihomie

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale
import androidx.room.Insert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val PROPERTY_EXTRA = "PROPERTY_EXTRA"
class PropertyItemAdapter(
    private val context: Context,
    private val savedHomesDao: SavedHomesDao,
    private var properties: List<PropertyModel>,
    private val mListener: OnListFragmentInteractionListener?
) :
    RecyclerView.Adapter<PropertyItemAdapter.PropertyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_property_item, parent, false)
        return PropertyViewHolder(view)
    }

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mPropertyModel: PropertyModel? = null

        val addressTextView: TextView = itemView.findViewById(R.id.tv_address)
        var priceTextView: TextView = itemView.findViewById(R.id.tv_price)
        val listingStatusTextView: TextView = itemView.findViewById(R.id.tv_listing_status)
        val bedroomTextView: TextView = itemView.findViewById(R.id.tv_bedroom)
        val bathroomTextView: TextView = itemView.findViewById(R.id.tv_bathroom)
        val sqftTextView: TextView = itemView.findViewById(R.id.tv_sqft)
        val propertyImage: ImageView = itemView.findViewById(R.id.iv_property)
        val saveButton: FloatingActionButton = itemView.findViewById(R.id.save_button)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]

        // Populate views with property information
        holder.mPropertyModel = property

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.maximumFractionDigits = 0
        val formattedPrice = currencyFormat.format(property.price)
        holder.priceTextView.text = formattedPrice

        val formattedListingStatus = property.listingStatus?.replace("_", " ")
        holder.listingStatusTextView.text = formattedListingStatus

        holder.bedroomTextView.text = "${property.bedrooms} bds  |  "
        holder.bathroomTextView.text = "${property.bathrooms} ba  |  "
        holder.sqftTextView.text = "${property.sqft} sqft"
        holder.addressTextView.text = property.address

        Glide.with(holder.itemView)
            .load(property.imageUrl)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.propertyImage)


        // Change button state based on if the property zpid is found in the saved home list
        var savedHomesList: List<SavedHomes>
        CoroutineScope(Dispatchers.IO).launch {
            savedHomesList = savedHomesDao.getAllSavedHomes()

            // Update UI on the main thread
            withContext(Dispatchers.Main) {
                // Set save button background tint based on saved status
                if (savedHomesList.any { it.zpid == property.zpid }) {
                    holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1BAAE"))
                } else {
                    holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                }
            }
        }

        // Handle save button click
        holder.saveButton.setOnClickListener {

            Log.d("PropertyItemAdapter", "isSaved before database operation: ${property.isSaved}")

            // Toggle saved status
            property.isSaved = !property.isSaved

            Log.d("PropertyItemAdapter", "isSaved before database operation: ${property.isSaved}")

            // Insert/remove item from the database based on saved status
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("PropertyItemAdapter", "Inside coroutine block: property.isSaved = ${property.isSaved}")
                if (property.isSaved) {
                    val zpid = property.zpid?.let { SavedHomes(zpid = it) }
                    zpid?.let {
                        savedHomesDao.insert(it)
                        Log.d("Database", "Inserted ZPID: ${it.zpid}")
                    }
                } else {
                    property.zpid?.let {
                        savedHomesDao.delete(it)
                        Log.d("Database", "Deleted ZPID: $it")
                    }
                }

                // Update button background tint on the main thread
                withContext(Dispatchers.Main) {
                    if (property.isSaved) {
                        holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1BAAE"))
                    } else {
                        holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }

        // Handle save button click
        holder.itemView.setOnClickListener {
            holder.mPropertyModel?.let { movie ->
                mListener?.onItemClick(movie)
            }
        }
    }

    override fun getItemCount(): Int {
        return properties.size
    }

}