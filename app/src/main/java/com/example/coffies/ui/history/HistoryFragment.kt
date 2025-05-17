package com.example.coffies.ui.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentHistoryBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    private val historyAdapter = HistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupDateRangePicker()
        setupFilterButton()
        setupSwipeRefresh()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.historyItems.collectLatest { items ->
                        historyAdapter.submitList(items)
                        binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.dateRange.collectLatest { (start, end) ->
                        binding.dateRangeText.text = formatDateRange(start, end)
                    }
                }

                launch {
                    viewModel.filters.collectLatest { filters ->
                        updateFilterChips(filters)
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.swipeRefresh.isRefreshing = isLoading
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        historyAdapter.onItemClick = { item ->
            when (item) {
                is HistoryItem.CoffeeItem -> {
                    val action = HistoryFragmentDirections.actionHistoryToCoffeeDetails(item.id)
                    findNavController().navigate(action)
                }
                is HistoryItem.MoodItem -> {
                    val action = HistoryFragmentDirections.actionHistoryToMoodDetails(item.id)
                    findNavController().navigate(action)
                }
                else -> Unit
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.forceReload()
        }
    }

    private fun setupDateRangePicker() {
        binding.dateRangeText.setOnClickListener {
            val currentStart = viewModel.dateRange.value.first
            val currentEnd = viewModel.dateRange.value.second

            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.CoffiesCalendarPickerTheme)
                .setTitleText("Выберите период")
                .setSelection(
                    androidx.core.util.Pair(
                        currentStart.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        currentEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                )
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val start = Instant.ofEpochMilli(selection.first)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val end = Instant.ofEpochMilli(selection.second)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.updateDateRange(start, end)
            }

            picker.show(parentFragmentManager, "date_range_picker")
        }
    }


    private fun setupFilterButton() {
        binding.filterButton.setOnClickListener {
            HistoryFilterBottomSheet.newInstance(viewModel.filters.value) { filters ->
                viewModel.updateFilters(filters)
            }.show(childFragmentManager, "filters")
        }
    }

    private fun updateFilterChips(filters: HistoryFilters) {
        val group = binding.activeFiltersGroup
        group.removeAllViews()

        val context = requireContext()
        var hasFilters = false

        if (!filters.showCoffee) {
            group.addView(createChip(context, "Без кофе"))
            hasFilters = true
        }

        if (!filters.showMood) {
            group.addView(createChip(context, "Без настроения"))
            hasFilters = true
        }

        group.visibility = if (hasFilters) View.VISIBLE else View.GONE
    }

    private fun createChip(context: android.content.Context, text: String): com.google.android.material.chip.Chip {
        return com.google.android.material.chip.Chip(context).apply {
            this.text = text
            isCheckable = false
            isClickable = false
        }
    }

    private fun formatDateRange(start: LocalDate, end: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return when {
            start == end && start == LocalDate.now() -> "Сегодня"
            start == end && start == LocalDate.now().minusDays(1) -> "Вчера"
            start == end -> start.format(formatter)
            else -> "${start.format(formatter)} - ${end.format(formatter)}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
