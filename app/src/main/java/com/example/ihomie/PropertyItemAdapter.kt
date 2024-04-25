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

        // Set save button background tint based on saved status
        if (property.isSaved) {
            holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1BAAE"))
        } else {
            holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }

        // Handle save button click
        holder.saveButton.setOnClickListener {
            property.isSaved = !property.isSaved

            // Insert or remove item from the database based on saved status
            if (property.isSaved) {
                CoroutineScope(Dispatchers.IO).launch {
                    val zpid = property.zpid?.let { SavedHomes(zpid = it) }
                    zpid?.let { savedHomesDao.insert(it) }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    property.zpid?.let { savedHomesDao.delete(it) }
                }
            }

            // Update save button background tint
            if (property.isSaved) {
                holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F1BAAE"))
            } else {
                holder.saveButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
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