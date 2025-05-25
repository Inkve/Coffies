package com.example.coffies.ui.addmood

import MoodInputViewModelFactory
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentMoodInputBinding
import kotlinx.coroutines.launch

class MoodInputFragment : Fragment() {

    private var _binding: FragmentMoodInputBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoodInputViewModel by viewModels {
        MoodInputViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoodInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefillFromArguments()
        observeState()

        listOf(binding.mood1, binding.mood2, binding.mood3, binding.mood4, binding.mood5).forEachIndexed { idx, iv ->
            iv.setOnClickListener { viewModel.onMoodSelected(idx + 1) }
        }

        binding.momentRadioGroup.check(R.id.moment_before)

        binding.momentRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val moment = when (checkedId) {
                R.id.moment_before -> MoodMomentType.BEFORE
                R.id.moment_after -> MoodMomentType.AFTER
                else -> null
            }
            viewModel.onMomentSelected(moment)
        }

        binding.relatedEntryInputLayout.setEndIconOnClickListener { showCoffeeDialog() }
        binding.relatedEntrySpinner.setOnClickListener { showCoffeeDialog() }

        binding.relatedEntrySpinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.relatedEntrySpinner.text.isEmpty()) {
                binding.relatedEntrySpinner.setText("")
            }
        }

        binding.dateInput.setOnClickListener { showDatePicker() }
        binding.timeInput.setOnClickListener { showTimePicker() }

        binding.commentInput.addTextChangedListener { viewModel.onCommentChanged(it?.toString() ?: "") }

        binding.saveButton.setOnClickListener { viewModel.submit() }
    }

    private fun prefillFromArguments() {
        val args = arguments ?: return

        val relatedCoffeeId = args.getInt("relatedCoffeeId", -1).takeIf { it > 0 }
        val relatedCoffeeTime = args.getString("relatedCoffeeTime")
        val relatedCoffeeDate = args.getString("relatedCoffeeDate")
        val momentTypeStr = args.getString("momentType")
        val momentType = when (momentTypeStr) {
            "before and during", "before", "BEFORE" -> MoodMomentType.BEFORE
            "after", "AFTER" -> MoodMomentType.AFTER
            else -> null
        }

        viewModel.prefill(relatedCoffeeId, relatedCoffeeTime, relatedCoffeeDate, momentType)
    }


    private fun observeState() {
        lifecycleScope.launch {
            viewModel.formState.collect { st ->
                listOf(binding.mood1, binding.mood2, binding.mood3, binding.mood4, binding.mood5).forEachIndexed { idx, iv ->
                    iv.isSelected = idx + 1 == st.moodLevel
                    iv.background = if (iv.isSelected) ContextCompat.getDrawable(requireContext(), R.drawable.mood_selector_background) else null
                }
                binding.momentRadioGroup.check(
                    when (st.momentType) {
                        MoodMomentType.BEFORE -> R.id.moment_before
                        MoodMomentType.AFTER -> R.id.moment_after
                        else -> -1
                    }
                )
                val relatedText = st.relatedCoffeeId?.let { id ->
                    st.coffeeList.firstOrNull { it.id == id }?.let { entry ->
                        val typeName = st.coffeeTypeNameMap[entry.coffee_type_id] ?: "Тип ${entry.coffee_type_id}"
                        val displayDate = entry.date.split("-").let {
                            if (it.size == 3) "${it[2]}.${it[1]}.${it[0]}" else entry.date
                        }
                        "$typeName • ${entry.volume_ml} мл • ${entry.time} $displayDate"
                    }
                } ?: ""
                if (relatedText.isEmpty() && binding.relatedEntrySpinner.text.isNotEmpty()) {
                    binding.relatedEntrySpinner.setText("")
                }
                if (relatedText.isNotEmpty() && binding.relatedEntrySpinner.text.toString() != relatedText) {
                    binding.relatedEntrySpinner.setText(relatedText, false)
                }
                if (relatedText.isEmpty()) {
                    binding.relatedEntrySpinner.hint = "Выберите приём кофе"
                } else {
                    binding.relatedEntrySpinner.hint = ""
                }
                binding.dateInput.setText(st.displayDate)
                binding.timeInput.setText(st.time)
                if (binding.commentInput.text.toString() != st.comment) {
                    binding.commentInput.setText(st.comment)
                    binding.commentInput.setSelection(st.comment.length)
                }

                binding.moodError.text = st.moodError
                binding.moodError.visibility = if (st.moodError != null) View.VISIBLE else View.GONE
                binding.momentError.text = st.momentError
                binding.momentError.visibility = if (st.momentError != null) View.VISIBLE else View.GONE
                binding.relatedCoffeeError.text = st.relatedCoffeeError
                binding.relatedCoffeeError.visibility = if (st.relatedCoffeeError != null) View.VISIBLE else View.GONE

                val datetimeError = st.dateError ?: st.timeError
                binding.datetimeError.text = datetimeError
                binding.datetimeError.visibility = if (datetimeError != null) View.VISIBLE else View.GONE

                if (st.success) {
                    Toast.makeText(requireContext(), "Настроение успешно сохранено", Toast.LENGTH_SHORT).show()
                    binding.relatedEntrySpinner.setText("", false)
                    binding.relatedEntrySpinner.hint = "Выберите приём кофе"
                    viewModel.resetForm()
                }
            }
        }
    }

    private fun showCoffeeDialog() {
        val st = viewModel.formState.value
        val context = requireContext()
        val list = st.coffeeList
        if (list.isEmpty()) {
            Toast.makeText(context, "Нет подходящих приемов кофе", Toast.LENGTH_SHORT).show()
            return
        }
        val items = list.map { entry ->
            val typeName = st.coffeeTypeNameMap[entry.coffee_type_id] ?: "Тип ${entry.coffee_type_id}"
            val displayDate = entry.date.split("-").let {
                if (it.size == 3) "${it[2]}.${it[1]}.${it[0]}" else entry.date
            }
            "$typeName • ${entry.volume_ml} мл • ${entry.time} $displayDate"
        }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Выберите прием кофе")
            .setItems(items) { _, which ->
                viewModel.onRelatedCoffeeSelected(list[which])
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            R.style.CoffiesDialogTheme,
            { _, y, m, d ->
                val display = String.format("%02d.%02d.%04d", d, m + 1, y)
                val iso = String.format("%04d-%02d-%02d", y, m + 1, d)
                viewModel.onDateChanged(display, iso)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        val dialog = TimePickerDialog(
            requireContext(),
            R.style.CoffiesDialogTheme,
            { _, h, min ->
                val formatted = String.format("%02d:%02d", h, min)
                viewModel.onTimeChanged(formatted)
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        )
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
