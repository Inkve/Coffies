package com.example.coffies.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentMainBinding
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getInstance(requireContext()))
    }
    private val recAdapter = RecommendationAdapter()

    private val colorRed by lazy { ContextCompat.getColor(requireContext(), R.color.red) }
    private val colorBlack by lazy { ContextCompat.getColor(requireContext(), R.color.black) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recommendationsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recommendationsRecycler.adapter = recAdapter

        binding.addCoffeeButton.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_addCoffee)
        }
        binding.addMoodButton.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_addMood)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.stats.collect { stats ->
                    binding.cupsText.text = when {
                        stats?.cupsCount != null && stats.cupGoal != null ->
                            getString(R.string.cups_count_with_goal, stats.cupsCount, stats.cupGoal)
                        stats?.cupsCount != null ->
                            getString(R.string.cups_count_simple, stats.cupsCount)
                        else -> getString(R.string.no_data)
                    }
                    val isCupExceeded = stats?.cupGoal != null && stats.cupsCount != null && stats.cupGoal > 0 && stats.cupsCount > stats.cupGoal
                    binding.cupsText.setTextColor(if (isCupExceeded) colorRed else colorBlack)

                    binding.caffeineText.text = stats?.caffeine?.let { "$it мг" } ?: getString(R.string.no_data)
                    val isCaffeineExceeded = stats?.caffeine != null && stats.caffeine > 400
                    binding.caffeineText.setTextColor(if (isCaffeineExceeded) colorRed else colorBlack)
                    binding.caffeineLimitText.text = "/ 400 мг"
                    binding.caffeineLimitText.setTextColor(if (isCaffeineExceeded) colorRed else colorBlack)

                    if (stats?.avgMood != null && stats.avgMood > 0) {
                        binding.currentMoodIcon.setImageResource(moodLevelToDrawable(stats.avgMood))
                        binding.moodText.text = moodLevelToText(stats.avgMood)
                        binding.moodText.setTextColor(colorBlack)
                    } else {
                        binding.currentMoodIcon.setImageResource(R.drawable.mood_3)
                        binding.moodText.text = getString(R.string.no_data)
                        binding.moodText.setTextColor(colorBlack)
                    }

                    binding.moodLimitText.text = "/ Отличное"
                    binding.moodLimitText.setTextColor(colorBlack)

                    binding.spentText.text = stats?.totalSpent?.let { "%.2f ₽".format(it) } ?: getString(R.string.no_data)
                    val isSpendExceeded = stats?.spendGoal != null && stats.totalSpent != null && stats.totalSpent > stats.spendGoal
                    binding.spentText.setTextColor(if (isSpendExceeded) colorRed else colorBlack)
                    binding.spentLimitText.text =
                        if (stats?.spendGoal != null) "/ %.2f ₽".format(stats.spendGoal) else "/ " + getString(R.string.infinity)
                    binding.spentLimitText.setTextColor(if (isSpendExceeded) colorRed else colorBlack)

                    binding.recommendationTitle.text = if (!stats?.userName.isNullOrBlank()) {
                        "${stats!!.userName}, вот ваши рекомендации дня"
                    } else {
                        getString(R.string.recomendation)
                    }
                    recAdapter.updateFromStats(stats)
                }
            }
        }

        viewModel.loadStatsForToday()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStatsForToday()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun moodLevelToDrawable(level: Int): Int {
        return when (level) {
            1 -> R.drawable.mood_1
            2 -> R.drawable.mood_2
            3 -> R.drawable.mood_3
            4 -> R.drawable.mood_4
            5 -> R.drawable.mood_5
            else -> R.drawable.mood_3
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
