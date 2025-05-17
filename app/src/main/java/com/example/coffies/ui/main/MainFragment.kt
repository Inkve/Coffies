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

    private val DAILY_CAFFEINE_LIMIT = 400

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
                    if (stats != null) {
                        // --- Чашки кофе ---
                        val cupGoal = stats.cupGoal
                        if (cupGoal != null) {
                            binding.cupsText.text = "Чашек кофе: ${stats.cupsCount} / $cupGoal"
                            val exceeded = if (cupGoal == 0) stats.cupsCount > 0 else stats.cupsCount > cupGoal
                            if (exceeded) {
                                binding.cupsText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                            } else {
                                binding.cupsText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            }
                        } else {
                            binding.cupsText.text = "Чашек кофе: ${stats.cupsCount}"
                            binding.cupsText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        }

                        // --- Кофеин ---
                        val caffeineText = "Кофеин: \n ${stats.caffeine} / $DAILY_CAFFEINE_LIMIT мг"
                        binding.caffeineText.text = caffeineText
                        if (stats.caffeine > DAILY_CAFFEINE_LIMIT) {
                            binding.caffeineText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                        } else {
                            binding.caffeineText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        }

                        // --- Потрачено ---
                        val spendGoal = stats.spendGoal
                        if (spendGoal != null) {
                            binding.spentText.text = "Потрачено: \n %.2f / %.2f ₽".format(stats.totalSpent, spendGoal)
                            val exceeded = if (spendGoal == 0f) stats.totalSpent > 0f else stats.totalSpent > spendGoal
                            if (exceeded) {
                                binding.spentText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                            } else {
                                binding.spentText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            }
                        } else {
                            binding.spentText.text = "Потрачено: \n %.2f ₽".format(stats.totalSpent)
                            binding.spentText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        }

                        // --- Настроение ---
                        binding.currentMoodIcon.setImageResource(moodLevelToDrawable(stats.avgMood))
                        binding.moodText.text = "Настроение: \n ${moodLevelToText(stats.avgMood)}"

                        recAdapter.submitList(stats.recommendations)
                    }
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
            else -> getString(R.string.mood_unknown)
        }
    }
}
