package com.example.ihomie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        val propertyImage: ImageView = itemView.findViewById(R.id.iv_property)
        var priceTextView: TextView = itemView.findViewById(R.id.tv_price)
        val listingStatusTextView: TextView = itemView.findViewById(R.id.tv_listing_status)
        val bedroomTextView: TextView = itemView.findViewById(R.id.tv_bedroom)
        val bathroomTextView: TextView = itemView.findViewById(R.id.tv_bathroom)
        val sqftTextView: TextView = itemView.findViewById(R.id.tv_sqft)
        val addressTextView: TextView = itemView.findViewById(R.id.tv_address)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]

        // Populate views with property information
        holder.mPropertyModel = property

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.maximumFractionDigits = 0
        val formattedPrice = currencyFormat.format(property.price)
        holder.priceTextView.text = formattedPrice

        holder.listingStatusTextView.text = "House for sale"
        holder.bedroomTextView.text = "${property.bedrooms} bds  |  "
        holder.bathroomTextView.text = "${property.bathrooms} ba  |  "
        holder.sqftTextView.text = "${property.sqft} sqft"
        holder.addressTextView.text = property.address

        holder.itemView.setOnClickListener {
            holder.mPropertyModel?.let { movie ->
                mListener?.onItemClick(movie)
            }
        }
    }

    override fun getItemCount(): Int {
        return properties.size
    }

//    fun updateData(newProperties: List<PropertyModel>) {
//        properties = newProperties
//        notifyDataSetChanged()
//    }
}