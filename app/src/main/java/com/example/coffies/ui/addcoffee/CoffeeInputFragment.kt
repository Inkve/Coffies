package com.example.coffies.ui.addcoffee

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeetype.CoffeeType
import com.example.coffies.databinding.FragmentCoffeeInputBinding
import kotlinx.coroutines.launch

class CoffeeInputFragment : Fragment() {

    private var _binding: FragmentCoffeeInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CoffeeInputViewModel by viewModels {
        CoffeeInputViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    private var coffeeTypes: List<CoffeeType> = emptyList()
    private var spinnerAdapter: ArrayAdapter<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoffeeInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addCoffeeType.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_addTypeCoffee)
        }

        lifecycleScope.launch {
            coffeeTypes = viewModel.getCoffeeTypesOnce()
            val names = coffeeTypes.map { it.name }
            spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                names
            )
            binding.coffeeTypeSpinner.setAdapter(spinnerAdapter)
        }

        fun handleCoffeeTypeClick() {
            if (coffeeTypes.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Добавьте хотя бы один вид кофе для продолжения.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.coffeeTypeSpinner.showDropDown()
            }
        }

        binding.coffeeTypeInputLayout.setOnClickListener { handleCoffeeTypeClick() }
        binding.coffeeTypeInputLayout.setEndIconOnClickListener { handleCoffeeTypeClick() }
        binding.coffeeTypeSpinner.setOnClickListener { handleCoffeeTypeClick() }

        binding.coffeeTypeSpinner.setOnItemClickListener { _, _, position, _ ->
            val type = coffeeTypes.getOrNull(position)
            if (type != null) {
                viewModel.onCoffeeTypeSelected(type)
                binding.coffeeTypeInputLayout.isHintEnabled = false
            }
        }

        binding.coffeeTypeSpinner.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                binding.coffeeTypeInputLayout.isHintEnabled = true
            }
        }

        binding.volumeInput.addTextChangedListener {
            viewModel.onFieldChanged("volume", it?.toString() ?: "")
        }
        binding.quantityValue.addTextChangedListener {
            viewModel.onFieldChanged("quantity", it?.toString() ?: "")
        }
        binding.caffeineInput.addTextChangedListener {
            viewModel.onFieldChanged("caffeine", it?.toString() ?: "")
        }
        binding.costInput.addTextChangedListener {
            viewModel.onFieldChanged("cost", it?.toString() ?: "")
        }
        binding.dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val dialog = DatePickerDialog(
                requireContext(), R.style.CoffiesDialogTheme,
                { _, y, mon, day ->
                    val display = String.format("%02d.%02d.%04d", day, mon + 1, y)
                    val iso = String.format("%04d-%02d-%02d", y, mon + 1, day)
                    viewModel.onDatePicked(display, iso)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }
        binding.timeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                requireContext(), R.style.CoffiesDialogTheme,
                { _, h, m ->
                    val formatted = String.format("%02d:%02d", h, m)
                    viewModel.onTimePicked(formatted)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        binding.coffeeShopInput.addTextChangedListener {
            viewModel.onFieldChanged("place", it?.toString() ?: "")
        }
        binding.commentInput.addTextChangedListener {
            viewModel.onFieldChanged("comment", it?.toString() ?: "")
        }

        binding.quantityPlus.setOnClickListener {
            val current = binding.quantityValue.text.toString().toIntOrNull() ?: 1
            binding.quantityValue.setText((current + 1).toString())
        }
        binding.quantityMinus.setOnClickListener {
            val current = binding.quantityValue.text.toString().toIntOrNull() ?: 1
            if (current > 1) {
                binding.quantityValue.setText((current - 1).toString())
            }
        }

        binding.saveButton.setOnClickListener {
            viewModel.submit()
        }

        lifecycleScope.launch {
            viewModel.formState.collect { state ->
                if (binding.coffeeTypeSpinner.text.toString() != state.coffeeType?.name.orEmpty()) {
                    binding.coffeeTypeSpinner.setText(state.coffeeType?.name.orEmpty(), false)
                    binding.coffeeTypeInputLayout.isHintEnabled = state.coffeeType?.name.isNullOrEmpty()
                }
                if (binding.volumeInput.text.toString() != state.volume) {
                    binding.volumeInput.setText(state.volume)
                }
                if (binding.quantityValue.text.toString() != state.quantity) {
                    binding.quantityValue.setText(state.quantity)
                }
                if (binding.caffeineInput.text.toString() != state.caffeine) {
                    binding.caffeineInput.setText(state.caffeine)
                }
                if (binding.dateInput.text.toString() != state.date) {
                    binding.dateInput.setText(state.date)
                }
                if (binding.timeInput.text.toString() != state.time) {
                    binding.timeInput.setText(state.time)
                }
                if (binding.costInput.text.toString() != state.cost) {
                    binding.costInput.setText(state.cost)
                }
                if (binding.coffeeShopInput.text.toString() != state.place) {
                    binding.coffeeShopInput.setText(state.place)
                }
                if (binding.commentInput.text.toString() != state.comment) {
                    binding.commentInput.setText(state.comment)
                }

                binding.coffeeTypeInputLayout.error = state.coffeeTypeError
                binding.volumeInput.error = state.volumeError
                binding.quantityValue.error = state.quantityError
                binding.caffeineInput.error = state.caffeineError
                binding.costInput.error = state.costError
                binding.dateInput.error = state.dateError
                binding.timeInput.error = state.timeError

                state.errorMsg?.let {
                    Toast.makeText(requireContext(), "Ошибка при сохранении: $it", Toast.LENGTH_LONG).show()
                }
                if (state.success) {
                    clearFields()
                    Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_LONG).show()
                    viewModel.clearSuccessFlag()
                }
            }
        }
    }

    private fun clearFields() {
        binding.coffeeTypeSpinner.setText("")
        binding.volumeInput.text?.clear()
        binding.caffeineInput.text?.clear()
        binding.costInput.text?.clear()
        binding.timeInput.text?.clear()
        binding.dateInput.text?.clear()
        binding.dateInput.tag = null
        binding.coffeeShopInput.text?.clear()
        binding.commentInput.text?.clear()
        binding.quantityValue.setText("1")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
