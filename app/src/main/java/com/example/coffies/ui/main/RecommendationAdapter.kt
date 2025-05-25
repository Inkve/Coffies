package com.example.coffies.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffies.R

class RecommendationAdapter : RecyclerView.Adapter<RecommendationAdapter.RecViewHolder>() {
    private val data = mutableListOf<String>()

    fun updateFromStats(stats: MainScreenStats?) {
        data.clear()
        if (stats != null) {
            data.addAll(generateRecommendations(stats))
        }
        notifyDataSetChanged()
    }

    private fun generateRecommendations(stats: MainScreenStats): List<String> {
        val recs = mutableListOf<String>()

        if (stats.caffeine != null) {
            when {
                stats.caffeine > 400 -> recs += "☕ Превышен дневной лимит кофеина. Рекомендуется снизить потребление."
                stats.caffeine >= 300 -> recs += "⚠️ Вы близки к лимиту кофеина. Будьте внимательны!"
                stats.caffeine < 100  -> recs += "🌱 Сегодня мало кофеина — вы молодец, организм скажет спасибо!"
            }
        }

        if (stats.spendGoal != null && stats.totalSpent != null) {
            when {
                stats.totalSpent > stats.spendGoal -> recs += "💸 Расходы на кофе превысили цель. Попробуйте проанализировать траты."
                stats.totalSpent < stats.spendGoal * 0.5 -> recs += "👍 Вы хорошо контролируете траты на кофе!"
                (stats.spendGoal - stats.totalSpent) < 100 && stats.totalSpent <= stats.spendGoal -> recs += "💰 До финансового лимита осталось меньше 100 ₽."
            }
        }

        if (stats.avgMood != null) {
            when {
                stats.avgMood <= 2 -> recs += "😊 Постарайтесь больше отдыхать. Хорошее настроение — залог продуктивности!"
                stats.avgMood == 3 -> recs += "🙂 Ваше настроение сегодня стабильное."
                stats.avgMood == 4 -> recs += "😃 Ваше настроение сегодня хорошее."
                stats.avgMood == 5 -> recs += "🎉 Отличное настроение сегодня! Так держать!"
            }
        }

        if (recs.isEmpty()) {
            recs += "💡 Не забывайте пить воду и делать небольшие перерывы!"
            recs += "❤ Всё в норме! Продолжайте в том же духе."
        }

        return recs.distinct().take(3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    class RecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recText: TextView = itemView.findViewById(R.id.recommendation_text)
        fun bind(text: String) {
            recText.text = text
        }
    }
}
