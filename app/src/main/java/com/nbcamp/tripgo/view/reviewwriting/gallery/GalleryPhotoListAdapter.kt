package com.nbcamp.tripgo.view.reviewwriting.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.data.repository.model.GalleryPhotoEntity
import com.nbcamp.tripgo.databinding.ItemGalleryPhotoBinding

class GalleryPhotoListAdapter(
    private val checkPhotoListener: (GalleryPhotoEntity) -> Unit
) : ListAdapter<GalleryPhotoEntity, GalleryPhotoListAdapter.PhotoItemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemViewHolder {
        return PhotoItemViewHolder(
            ItemGalleryPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhotoItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryPhotoEntity>() {
            override fun areItemsTheSame(
                oldItem: GalleryPhotoEntity,
                newItem: GalleryPhotoEntity
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: GalleryPhotoEntity,
                newItem: GalleryPhotoEntity
            ): Boolean = oldItem == newItem
        }
    }

    inner class PhotoItemViewHolder(
        private val binding: ItemGalleryPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GalleryPhotoEntity) = with(binding) {
            itemView.setOnClickListener {
                checkPhotoListener(data)
            }
            photoImageView.load(data.uri.toString())
        }
    }
}
