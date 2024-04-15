package com.example.ihomie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.text.NumberFormat
import java.util.Locale

class PropertyItemAdapter(
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
        holder.sqftTextView.text = "${property.sqft} sqft |  ${property.zpid} zpid"
        holder.addressTextView.text = property.address

        Glide.with(holder.itemView)
            .load(property.imageUrl)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.propertyImage)

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