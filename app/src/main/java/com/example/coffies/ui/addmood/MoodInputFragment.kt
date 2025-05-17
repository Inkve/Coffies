package com.example.coffies.ui.addmood

import MoodInputViewModelFactory
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.databinding.DialogCoffeeEntrySelectBinding
import com.example.coffies.databinding.FragmentMoodInputBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MoodInputFragment : Fragment() {

    private var _binding: FragmentMoodInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoodInputViewModel by viewModels {
        MoodInputViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    private var selectedMood = 3
    private var selectedMoment: String? = null
    private var selectedCoffeeEntry: CoffeeEntry? = null

    private var isoDate: String = ""
    private var time: String = ""

    private var userChangedDate = false
    private var userChangedTime = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, saved: Bundle?) {
        super.onViewCreated(view, saved)
        setupMoodSelection()
        setupMomentSelection()
        setupDateTimeDefaults()
        setupPickers()
        setupLinkedCoffee()
        setupSaveButton()
    }

    private fun setupMoodSelection() {
        updateMoodUI()
        binding.apply {
            mood1.setOnClickListener { selectMood(1) }
            mood2.setOnClickListener { selectMood(2) }
            mood3.setOnClickListener { selectMood(3) }
            mood4.setOnClickListener { selectMood(4) }
            mood5.setOnClickListener { selectMood(5) }
        }
    }

    private fun selectMood(level: Int) {
        selectedMood = level
        updateMoodUI()
        binding.moodError.visibility = View.GONE
    }

    private fun updateMoodUI() {
        binding.apply {
            listOf(mood1, mood2, mood3, mood4, mood5).forEachIndexed { idx, iv ->
                iv.isSelected = idx + 1 == selectedMood
                iv.background = if (iv.isSelected)
                    ContextCompat.getDrawable(requireContext(), R.drawable.mood_selector_bg)
                else null
            }
        }
    }

    private fun setupMomentSelection() {
        binding.momentRadioGroup.setOnCheckedChangeListener { _, id ->
            selectedMoment = when (id) {
                R.id.moment_before -> "before and during"
                R.id.moment_after -> "after"
                else -> null
            }
            binding.momentError.visibility = View.GONE
            if (!userChangedDate && !userChangedTime) setupDateTimeDefaults()
        }
    }

    private fun setupDateTimeDefaults() {
        val now = Calendar.getInstance().time
        if (!userChangedDate) {
            binding.dateInput.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(now))
            isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        }
        if (!userChangedTime) {
            binding.timeInput.setText(SimpleDateFormat("HH:mm", Locale.getDefault()).format(now))
            time = binding.timeInput.text.toString()
        }
    }

    private fun setupPickers() {
        binding.dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                R.style.CoffiesDialogTheme,
                { _, y, m, d ->
                    binding.dateInput.setText(String.format("%02d.%02d.%04d", d, m + 1, y))
                    isoDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    binding.dateError.visibility = View.GONE
                    userChangedDate = true
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.timeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                R.style.CoffiesDialogTheme,
                { _, h, min ->
                    binding.timeInput.setText(String.format("%02d:%02d", h, min))
                    time = String.format("%02d:%02d", h, min)
                    binding.timeError.visibility = View.GONE
                    userChangedTime = true
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupLinkedCoffee() {
        binding.linkedCoffeeField.setOnClickListener {
            showCoffeeDialog()
        }
    }

    private fun showCoffeeDialog() {
        val dialogBinding = DialogCoffeeEntrySelectBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CoffiesDialogTheme)
            .setTitle(R.string.mood_linked_coffee_choose)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        val adapter = CoffeeEntryShortAdapter(
            entries = emptyList(),
            getCoffeeTypeName = { id -> viewModel.getCoffeeTypeName(id) }
        ) { entry ->
            selectedCoffeeEntry = entry
            val name = viewModel.getCoffeeTypeName(entry.coffee_type_id)
            binding.linkedCoffeeText.text = "$name • ${entry.volume_ml} мл • ${entry.date} ${entry.time}"
            binding.linkedCoffeeError.visibility = View.GONE
            dialog.dismiss()
        }

        dialogBinding.coffeeEntriesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        lifecycleScope.launch {
            viewModel.lastCoffeeEntriesFlow.collectLatest { list ->
                Log.d("CoffeeDialog", "Обновляем адаптер, записей: ${list.size}")
                adapter.updateData(list)
            }
        }

        dialog.show()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            viewModel.saveMoodWithCoffeeLink(
                moodLevel = selectedMood,
                date = isoDate,
                time = time,
                comment = binding.commentInput.text.toString().takeIf { it.isNotBlank() },
                coffeeEntryId = selectedCoffeeEntry!!.id,
                relationType = selectedMoment!!,
                onSuccess = {
                    Toast.makeText(requireContext(), R.string.mood_save_success, Toast.LENGTH_LONG).show()
                    resetForm()
                },
                onError = {
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun resetForm() {
        selectedMood = 3
        updateMoodUI()
        selectedMoment = null
        binding.momentRadioGroup.clearCheck()
        selectedCoffeeEntry = null
        binding.linkedCoffeeText.setText(R.string.mood_linked_coffee_choose)
        binding.linkedCoffeeError.visibility = View.GONE
        binding.commentInput.text?.clear()
        userChangedDate = false
        userChangedTime = false
        setupDateTimeDefaults()
    }

    private fun validateInputs(): Boolean {
        var valid = true

        if (selectedMood !in 1..5) {
            binding.moodError.text = getString(R.string.mood_error_required)
            binding.moodError.visibility = View.VISIBLE
            valid = false
        } else binding.moodError.visibility = View.GONE

        if (selectedMoment == null) {
            binding.momentError.text = getString(R.string.mood_moment_error_required)
            binding.momentError.visibility = View.VISIBLE
            valid = false
        } else binding.momentError.visibility = View.GONE

        if (selectedCoffeeEntry == null) {
            binding.linkedCoffeeError.text = getString(R.string.mood_linked_coffee_error_required)
            binding.linkedCoffeeError.visibility = View.VISIBLE
            valid = false
        } else binding.linkedCoffeeError.visibility = View.GONE

        if (binding.dateInput.text.isNullOrBlank()) {
            binding.dateError.text = getString(R.string.mood_date_error_required)
            binding.dateError.visibility = View.VISIBLE
            valid = false
        } else binding.dateError.visibility = View.GONE

        if (binding.timeInput.text.isNullOrBlank()) {
            binding.timeError.text = getString(R.string.mood_time_error_required)
            binding.timeError.visibility = View.VISIBLE
            valid = false
        } else binding.timeError.visibility = View.GONE

        return valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
