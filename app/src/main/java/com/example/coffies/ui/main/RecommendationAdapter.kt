package com.example.coffies.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffies.R

class RecommendationAdapter : RecyclerView.Adapter<RecommendationAdapter.RecViewHolder>() {
    private val data = mutableListOf<String>()

    fun submitList(list: List<String>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    class RecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recText: TextView = itemView.findViewById(R.id.recommendation_text)
        fun bind(text: String) { recText.text = text }
    }
}
