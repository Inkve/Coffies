package com.example.coffies.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentHistoryBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupDateRangePicker()
        setupSwipeRefresh()
        setupChipFilters()
        observeViewModel()
        observeDeleteResult()
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
        binding.dateRangeLayout.setOnClickListener {
            val currentStart = viewModel.dateRange.value.first
            val currentEnd = viewModel.dateRange.value.second

            val todayMillis = MaterialDatePicker.todayInUtcMilliseconds()
            val validator = MaxDateValidator(todayMillis)
            val constraints = CalendarConstraints.Builder()
                .setValidator(validator)
                .build()

            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.CoffiesCalendarPickerTheme)
                .setTitleText("Выберите период")
                .setCalendarConstraints(constraints)
                .setSelection(
                    androidx.core.util.Pair(
                        currentStart.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
                        currentEnd.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
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

    private fun setupChipFilters() {
        binding.chipCoffee.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCoffeeFilter(isChecked)
        }
        binding.chipMood.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleMoodFilter(isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.historyItems
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { items ->
                historyAdapter.submitList(items)
                binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.dateRange
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { (start, end) ->
                binding.dateRangeText.text = formatDateRange(start, end)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.filters
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { filters ->
                if (binding.chipCoffee.isChecked != filters.showCoffee)
                    binding.chipCoffee.isChecked = filters.showCoffee
                if (binding.chipMood.isChecked != filters.showMood)
                    binding.chipMood.isChecked = filters.showMood
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isLoading
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { isLoading ->
                binding.swipeRefresh.isRefreshing = isLoading
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
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

    private fun observeDeleteResult() {
        // Старый обработчик для удаления кофе
        parentFragmentManager.setFragmentResultListener(
            "COFFEE_DELETED",
            viewLifecycleOwner
        ) { _, bundle ->
            val deletedId = bundle.getInt("deleted_id")
            viewModel.forceReload()
            Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
        }
        // Новый обработчик для удаления настроения
        parentFragmentManager.setFragmentResultListener(
            "MOOD_DELETED",
            viewLifecycleOwner
        ) { _, bundle ->
            val deletedId = bundle.getInt("deleted_id")
            viewModel.forceReload()
            Toast.makeText(requireContext(), "Настроение удалено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.forceReload()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
