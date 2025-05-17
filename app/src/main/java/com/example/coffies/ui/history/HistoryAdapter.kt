package com.example.coffies.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffies.R
import com.example.coffies.databinding.ItemHistoryDateHeaderBinding
import com.example.coffies.databinding.ItemHistoryEntryBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HistoryAdapter : ListAdapter<HistoryItem, RecyclerView.ViewHolder>(DiffCallback()) {

    var onItemClick: ((HistoryItem) -> Unit)? = null

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HistoryItem.DateHeader -> VIEW_TYPE_DATE_HEADER
        is HistoryItem.CoffeeItem -> VIEW_TYPE_COFFEE
        is HistoryItem.MoodItem -> VIEW_TYPE_MOOD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> DateHeaderViewHolder(
                ItemHistoryDateHeaderBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_COFFEE -> CoffeeViewHolder(
                ItemHistoryEntryBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_MOOD -> MoodViewHolder(
                ItemHistoryEntryBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HistoryItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is HistoryItem.CoffeeItem -> (holder as CoffeeViewHolder).bind(item)
            is HistoryItem.MoodItem -> (holder as MoodViewHolder).bind(item)
        }
    }

    inner class DateHeaderViewHolder(private val binding: ItemHistoryDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem.DateHeader) {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            binding.dateHeaderText.text = when (item.date) {
                today -> "Сегодня"
                yesterday -> "Вчера"
                else -> item.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }
        }
    }

    inner class CoffeeViewHolder(private val binding: ItemHistoryEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(item: HistoryItem.CoffeeItem) {
            with(binding) {
                entryIcon.setImageResource(R.drawable.coffee_cup_large)
                entryTitle.text = "Кофе: ${item.type}"
                val details = buildString {
                    append("${item.quantity} шт")
                    item.volume?.let { append(" • $it мл") }
                    item.price?.let { append(" • ${it}₽") }
                }
                entryDetails.text = details
                entryTime.text = item.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }
    }

    inner class MoodViewHolder(private val binding: ItemHistoryEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }

        fun bind(item: HistoryItem.MoodItem) {
            with(binding) {
                entryIcon.setImageResource(getMoodIcon(item.level))
                entryTitle.text = "Настроение"
                entryDetails.text = getMoodText(item.level)
                entryTime.text = item.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        }

        private fun getMoodIcon(level: Int): Int {
            return when (level) {
                1 -> R.drawable.mood_1
                2 -> R.drawable.mood_2
                3 -> R.drawable.mood_3
                4 -> R.drawable.mood_4
                5 -> R.drawable.mood_5
                else -> R.drawable.mood_3
            }
        }

        private fun getMoodText(level: Int): String {
            return when (level) {
                1 -> "Очень плохое"
                2 -> "Плохое"
                3 -> "Нормальное"
                4 -> "Хорошее"
                5 -> "Отличное"
                else -> "Неизвестно"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return when {
                oldItem is HistoryItem.DateHeader && newItem is HistoryItem.DateHeader -> oldItem.date == newItem.date
                oldItem is HistoryItem.CoffeeItem && newItem is HistoryItem.CoffeeItem -> oldItem.id == newItem.id
                oldItem is HistoryItem.MoodItem && newItem is HistoryItem.MoodItem -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_COFFEE = 1
        private const val VIEW_TYPE_MOOD = 2
    }
}