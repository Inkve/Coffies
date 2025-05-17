package com.example.coffies.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coffies.databinding.BottomSheetHistoryFiltersBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HistoryFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHistoryFiltersBinding? = null
    private val binding get() = _binding!!

    private var onFiltersApplied: ((HistoryFilters) -> Unit)? = null
    private var currentFilters: HistoryFilters? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHistoryFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        applyCurrentFilters()
    }

    private fun setupUI() {
        binding.apply {
            showCoffeeCheckbox.setOnCheckedChangeListener { _, isChecked ->
                showCoffeeCheckbox.isChecked = isChecked
            }

            showMoodCheckbox.setOnCheckedChangeListener { _, isChecked ->
                showMoodCheckbox.isChecked = isChecked
            }

            applyButton.setOnClickListener {
                val filters = HistoryFilters(
                    showCoffee = showCoffeeCheckbox.isChecked,
                    showMood = showMoodCheckbox.isChecked
                )
                onFiltersApplied?.invoke(filters)
                dismiss()
            }
        }
    }

    private fun applyCurrentFilters() {
        currentFilters?.let { filters ->
            binding.showCoffeeCheckbox.isChecked = filters.showCoffee
            binding.showMoodCheckbox.isChecked = filters.showMood
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            currentFilters: HistoryFilters,
            onFiltersApplied: (HistoryFilters) -> Unit
        ): HistoryFilterBottomSheet {
            return HistoryFilterBottomSheet().apply {
                this.currentFilters = currentFilters
                this.onFiltersApplied = onFiltersApplied
            }
        }
    }
}
