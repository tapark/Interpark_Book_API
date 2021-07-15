package com.example.book_finder.adapter

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.book_finder.databinding.ItemHistoryBinding
import com.example.book_finder.model.History

class HistoryAdapter(val historyDeleteClickedListener: (String) -> Unit) : ListAdapter<History, HistoryAdapter.HistoryItemViewHolder>(diffUtil){

    inner class HistoryItemViewHolder(
        private val binding: ItemHistoryBinding
        ): RecyclerView.ViewHolder(binding.root) {

            fun bind(historyModel: History) {
                binding.historyTextView.text = historyModel.keyword

                binding.historyDeleteButton.setOnClickListener {
                    historyDeleteClickedListener(historyModel.keyword.orEmpty())
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemHistoryBinding.inflate(layoutInflater, parent, false)

        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<History>() {
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem.keyword == newItem.keyword
            }

        }
    }
}