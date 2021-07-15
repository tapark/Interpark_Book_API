package com.example.book_finder.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.book_finder.databinding.ItemBookBinding
import com.example.book_finder.model.Book

class BookAdapter(val itemClickedListener: (Book) -> Unit): ListAdapter<Book, BookAdapter.BooKItemViewHolder>(diffUtil) {

    inner class BooKItemViewHolder(
        private val binding: ItemBookBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(bookModel: Book) {
            binding.titleTextView.text = bookModel.title
            binding.descriptionTextView.text = bookModel.description
            Glide.with(binding.coverImageView).load(bookModel.coverSmallUrl).into(binding.coverImageView)

            binding.root.setOnClickListener {
                itemClickedListener(bookModel)
            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooKItemViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemBookBinding.inflate(layoutInflater, parent, false)

        return BooKItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BooKItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }

}