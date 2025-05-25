package com.example.coffies.ui.analitycs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.coffies.R
import com.example.coffies.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch

class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels {
        AnalyticsViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.caffeineCard.setOnClickListener { }
        binding.cupsCard.setOnClickListener { }
        binding.moneyCard.setOnClickListener { }
        binding.moodCard.setOnClickListener { }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Легенды
                binding.caffeineTitle.text = getString(R.string.caffeine_legend)
                binding.cupsTitle.text = getString(R.string.cups_legend)
                binding.moneyTitle.text = getString(R.string.money_legend)
                binding.moodTitle.text = getString(R.string.mood_legend)

                // КОФЕИН
                setupLineChart(
                    chart = binding.caffeineChart,
                    points = state.caffeinePoints,
                    lineColor = ContextCompat.getColor(requireContext(), R.color.brown),
                    fillColor = ContextCompat.getColor(requireContext(), R.color.dark_beige),
                    yValueFormatter = null
                )
                binding.caffeineSummary.text = getString(R.string.caffeine_summary, state.caffeineAvg)
                if (state.caffeineGoal != null) {
                    binding.caffeineGoal.visibility = View.VISIBLE
                    binding.caffeineGoal.text = getString(R.string.caffeine_goal_template, state.caffeineGoal)
                } else {
                    binding.caffeineGoal.visibility = View.GONE
                }
                binding.caffeineSummary.setTextColor(
                    if (state.isCaffeineExceeded)
                        ContextCompat.getColor(requireContext(), R.color.red)
                    else ContextCompat.getColor(requireContext(), R.color.black)
                )

                // ЧАШКИ
                setupLineChart(
                    chart = binding.cupsChart,
                    points = state.cupsPoints,
                    lineColor = ContextCompat.getColor(requireContext(), R.color.black),
                    fillColor = ContextCompat.getColor(requireContext(), R.color.dark_beige),
                    yValueFormatter = null
                )
                binding.cupsSummary.text = getString(R.string.cups_summary, state.cupsSum)
                if (state.cupsGoal != null) {
                    binding.cupsGoal.visibility = View.VISIBLE
                    binding.cupsGoal.text = getString(R.string.cups_goal_template, state.cupsGoal)
                } else {
                    binding.cupsGoal.visibility = View.GONE
                }
                binding.cupsSummary.setTextColor(
                    if (state.isCupsExceeded)
                        ContextCompat.getColor(requireContext(), R.color.red)
                    else ContextCompat.getColor(requireContext(), R.color.black)
                )

                // ДЕНЬГИ
                setupLineChart(
                    chart = binding.moneyChart,
                    points = state.moneyPoints,
                    lineColor = ContextCompat.getColor(requireContext(), R.color.black),
                    fillColor = ContextCompat.getColor(requireContext(), R.color.dark_beige),
                    yValueFormatter = MoneyYFormatter()
                )
                binding.moneySummary.text = getString(R.string.money_summary, state.moneySum)
                if (state.moneyGoal != null) {
                    binding.moneyGoal.visibility = View.VISIBLE
                    binding.moneyGoal.text = getString(R.string.money_goal_template, state.moneyGoal)
                } else {
                    binding.moneyGoal.visibility = View.GONE
                }
                binding.moneySummary.setTextColor(
                    if (state.isMoneyExceeded)
                        ContextCompat.getColor(requireContext(), R.color.red)
                    else ContextCompat.getColor(requireContext(), R.color.black)
                )

                // НАСТРОЕНИЕ
                setupLineChart(
                    chart = binding.moodChart,
                    points = state.moodPoints,
                    lineColor = ContextCompat.getColor(requireContext(), R.color.black),
                    fillColor = ContextCompat.getColor(requireContext(), R.color.dark_beige),
                    yValueFormatter = MoodAxisFormatter()
                )
                binding.moodSummary.text = getString(R.string.mood_summary, moodLevelToText(state.moodAvg))
                binding.moodSummary.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLineChart(
        chart: LineChart,
        points: List<Pair<String, Double>>,
        lineColor: Int,
        fillColor: Int,
        yValueFormatter: ValueFormatter? = null
    ) {
        val entries = points.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }
        val dataSet = LineDataSet(entries, "").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            setDrawCircles(true)
            lineWidth = 2.2f
            circleRadius = 3.5f
            color = lineColor
            setCircleColor(lineColor)
            fillAlpha = 60
            setDrawFilled(true)
            setFillColor(fillColor)
        }
        chart.data = LineData(dataSet)
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setExtraOffsets(0f, 0f, 0f, 20f)
        chart.axisLeft.setDrawGridLines(true)

        // Отключить масштабирование и прокрутку:
        chart.setScaleEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.isDragEnabled = false

        chart.setTouchEnabled(true)
        chart.isHighlightPerTapEnabled = true
        chart.isHighlightPerDragEnabled = true

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.labelRotationAngle = -45f
        chart.xAxis.granularity = 1f
        chart.xAxis.valueFormatter = DayAxisFormatter(points.map { it.first })
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.axisMaximum = (entries.lastIndex).toFloat()

        if (yValueFormatter != null) {
            chart.axisLeft.valueFormatter = yValueFormatter
        } else {
            chart.axisLeft.valueFormatter = DefaultYFormatter()
        }

        // Настраиваем маркер для отображения данных по тапу
        val markerView = ChartMarkerView(
            requireContext(),
            points.map { it.first },
            { y -> yValueFormatter?.getFormattedValue(y) ?: String.format("%.2f", y) }
        )
        markerView.chartView = chart
        chart.marker = markerView

        chart.invalidate()
    }


    class DayAxisFormatter(private val dates: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val idx = value.toInt()
            return if (idx in dates.indices && dates[idx].length == 10)
                "${dates[idx].substring(8,10)}.${dates[idx].substring(5,7)}"
            else ""
        }
    }

    class DefaultYFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    class MoneyYFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value > 0) "%d".format(value.toInt()) else "0"
        }
    }

    class MoodAxisFormatter : ValueFormatter() {
        private val moods = arrayOf("❔", "😞", "🙁", "😐", "🙂", "😃")
        override fun getFormattedValue(value: Float): String {
            val idx = value.toInt().coerceIn(0, 5)
            return moods[idx]
        }
    }

    private fun moodLevelToText(level: Int): String {
        return when (level) {
            1 -> getString(R.string.mood_1_short)
            2 -> getString(R.string.mood_2_short)
            3 -> getString(R.string.mood_3_short)
            4 -> getString(R.string.mood_4_short)
            5 -> getString(R.string.mood_5_short)
            else -> getString(R.string.no_data)
        }
    }
}
