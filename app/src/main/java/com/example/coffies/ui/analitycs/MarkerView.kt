package com.example.coffies.ui.analitycs

import android.content.Context
import android.widget.TextView
import com.example.coffies.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

class ChartMarkerView(
    context: Context,
    private val xLabels: List<String>, // Передавай список дат сюда!
    private val yFormatter: ((Float) -> String)? = null // Если нужен особый формат значения
) : MarkerView(context, R.layout.chart_marker_view) {

    private val textView: TextView = findViewById(R.id.marker_text)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val xIndex = e.x.toInt()
            val date = if (xIndex in xLabels.indices) {
                // Преобразуем yyyy-MM-dd -> dd.MM
                val str = xLabels[xIndex]
                if (str.length == 10) "${str.substring(8,10)}.${str.substring(5,7)}" else str
            } else {
                (xIndex + 1).toString()
            }
            val value = yFormatter?.invoke(e.y) ?: String.format("%.2f", e.y)
            textView.text = "Дата: $date\nЗначение: $value"
        }
        super.refreshContent(e, highlight)
    }
}
