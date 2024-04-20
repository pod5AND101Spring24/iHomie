package com.example.ihomie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DetailImageAdapter (
    private val imageUrls: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<DetailImageAdapter.DetailImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.thumbnail_item, parent, false)
        return DetailImageViewHolder(view,onImageClick)
    }


    override fun onBindViewHolder(holder: DetailImageViewHolder, position: Int) {

        val imageUrl = imageUrls[position]
        holder.bind(imageUrl)
    }


    override fun getItemCount(): Int = imageUrls.size

    inner class DetailImageViewHolder(itemView: View,val onImageClick:(String)->Unit) : RecyclerView.ViewHolder(itemView)
    {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)

        init {
            itemView.setOnClickListener{
                // Invoke the callback with the image URL when the item view is clicked
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onImageClick(imageUrls[absoluteAdapterPosition])
                }
            }
        }


        fun bind(imageUrl: String) {
            Glide.with(itemView)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .centerInside()
                .into(thumbnailImageView)
        }

    }

}

