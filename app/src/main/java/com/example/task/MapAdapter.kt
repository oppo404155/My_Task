package com.example.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.task.databinding.SourceItemBinding

class MapAdapter : androidx.recyclerview.widget.ListAdapter<Data, MapAdapter.viewholder>(MapAdapter.itemCallback) {


        var onItemClick: ((Data) -> Unit)? = null


        inner class viewholder(val binding: SourceItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                itemView.setOnClickListener{
                    onItemClick?.invoke(getItem(adapterPosition))
                }
            }
            fun bind(data: Data) {
                binding.sourceTxv.text=data.name
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
           val view = SourceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return viewholder(view)

        }

        override fun onBindViewHolder(holder: viewholder, position: Int) {
            val currentItem = getItem(position)
            if (currentItem != null) {
                holder.bind(currentItem)
            }


        }
        companion object {
            private val itemCallback=object : DiffUtil.ItemCallback<Data>() {
                override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
                    return oldItem==newItem
                }

                override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
                    return oldItem.name==newItem.name
                }

            }
        }
    }
