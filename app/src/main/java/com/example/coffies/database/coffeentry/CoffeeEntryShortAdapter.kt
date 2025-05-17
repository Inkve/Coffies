package com.example.coffies.ui.addmood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffies.R
import com.example.coffies.database.coffeentry.CoffeeEntry
import java.text.SimpleDateFormat
import java.util.*

class CoffeeEntryShortAdapter(
    private var entries: List<CoffeeEntry>,
    private val getCoffeeTypeName: (Int) -> String,
    private val onClick: (CoffeeEntry) -> Unit
) : RecyclerView.Adapter<CoffeeEntryShortAdapter.ViewHolder>() {

    fun updateData(newEntries: List<CoffeeEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coffeeInfo: TextView = view.findViewById(R.id.coffee_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coffee_entry_short, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = entries.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        val context = holder.itemView.context
        val coffeeType = getCoffeeTypeName(entry.coffee_type_id)

        val formattedDate = try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.date)
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(parsed!!)
        } catch (e: Exception) {
            entry.date // если что-то пошло не так, показываем как есть
        }

        holder.coffeeInfo.text = context.getString(
            R.string.coffee_entry_short_format,
            coffeeType,
            entry.volume_ml,
            formattedDate,
            entry.time
        )
        holder.itemView.setOnClickListener { onClick(entry) }
    }
}
