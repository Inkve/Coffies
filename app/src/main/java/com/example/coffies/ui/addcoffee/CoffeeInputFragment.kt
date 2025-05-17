package com.example.coffies.ui.addcoffee

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.coffeetype.CoffeeType
import com.example.coffies.databinding.FragmentCoffeeInputBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CoffeeInputFragment : Fragment() {

    private var _binding: FragmentCoffeeInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CoffeeInputViewModel by viewModels {
        CoffeeInputViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    private var coffeeTypes: List<CoffeeType> = emptyList()
    private var selectedType: CoffeeType? = null
    private var userChangedVolume = false
    private var userChangedCaffeine = false
    private var userChangedDate = false
    private var userChangedTime = false
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

        // Time picker
        binding.timeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                requireContext(), R.style.CoffiesDialogTheme,
                { _, h, m ->
                    val formatted = String.format("%02d:%02d", h, m)
                    binding.timeInput.setText(formatted)
                    userChangedTime = true
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Date picker (display dd.MM.yyyy, save yyyy-MM-dd)
        binding.dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(), R.style.CoffiesDialogTheme,
                { _, y, mon, day ->
                    // display
                    val display = String.format("%02d.%02d.%04d", day, mon + 1, y)
                    binding.dateInput.setText(display)
                    // but store ISO internally in hidden tag
                    binding.dateInput.tag = String.format("%04d-%02d-%02d", y, mon + 1, day)
                    userChangedDate = true
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // navigate to add type
        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_addTypeCoffee)
        }

        // spinner
        lifecycleScope.launch {
            coffeeTypes = viewModel.getCoffeeTypesOnce()
            val names = listOf(getString(R.string.choose_coffee_type)) +
                    coffeeTypes.map { it.name }
            spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.coffeeTypeSpinner.adapter = spinnerAdapter
            binding.coffeeTypeSpinner.setSelection(0)
        }

        binding.coffeeTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                private var first = true
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, pos: Int, id: Long
                ) {
                    selectedType = coffeeTypes.getOrNull(pos - 1)
                    if (first) {
                        first = false
                    } else {
                        spinnerAdapter?.notifyDataSetChanged()
                    }
                    selectedType?.let { type ->
                        if (!userChangedVolume && type.default_volume_ml != null) {
                            binding.volumeInput.setText(type.default_volume_ml.toString())
                        }
                        binding.quantityInput.setText("1")
                        if (!userChangedDate) {
                            // set today display + store tag
                            val now = Calendar.getInstance().time
                            val disp = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(now)
                            val iso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
                            binding.dateInput.setText(disp)
                            binding.dateInput.tag = iso
                        }
                        if (!userChangedTime) {
                            val now = Calendar.getInstance().time
                            binding.timeInput.setText(SimpleDateFormat("HH:mm", Locale.getDefault()).format(now))
                        }
                        if (!userChangedCaffeine &&
                            type.default_caffeine_mg_ml != null &&
                            binding.volumeInput.text.toString().toIntOrNull() != null
                        ) {
                            recalculateCaffeine(force = true)
                        }
                    }
                    userChangedCaffeine = false
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.volumeInput.setOnFocusChangeListener { _, has ->
            if (!has) {
                userChangedVolume = true
                userChangedCaffeine = false
                recalculateCaffeine()
            }
        }
        binding.caffeineInput.setOnFocusChangeListener { _, has ->
            if (!has) userChangedCaffeine = true
        }

        binding.saveButton.setOnClickListener {
            if (!validateFields()) return@setOnClickListener

            val coffeeTypeId = selectedType!!.id
            val volume = binding.volumeInput.text.toString().toInt()
            val quantity = binding.quantityInput.text.toString().toInt()
            val caffeine = binding.caffeineInput.text.toString().replace(',', '.').toDouble()
            val price = binding.costInput.text.toString().replace(',', '.').toDoubleOrNull()
            // get ISO date from tag (fallback to parse display)
            val isoDate = binding.dateInput.tag as? String ?: run {
                val parts = binding.dateInput.text.toString().split(".")
                String.format("%04d-%02d-%02d", parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
            }
            val time = binding.timeInput.text.toString()
            val place = binding.coffeeShopInput.text.toString().takeIf { it.isNotBlank() }
            val comment = binding.commentInput.text.toString().takeIf { it.isNotBlank() }

            val entry = CoffeeEntry(
                coffee_type_id = coffeeTypeId,
                volume_ml = volume,
                quantity = quantity,
                caffeine_mg = caffeine,
                price = price,
                date = isoDate,
                time = time,
                place = place,
                comment = comment
            )

            viewModel.insertEntry(entry,
                onSuccess = {
                    clearFields()
                    Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_LONG).show()
                },
                onError = {
                    Toast.makeText(requireContext(), "Ошибка при сохранении: ${it.message}", Toast.LENGTH_LONG).show()
                })
        }
    }

    private fun recalculateCaffeine(force: Boolean = false) {
        val type = selectedType ?: return
        val vol = binding.volumeInput.text.toString().toIntOrNull() ?: return
        if (userChangedCaffeine && !force) return
        val caf = vol * (type.default_caffeine_mg_ml ?: 0f)
        binding.caffeineInput.setText(String.format("%.1f", caf))
    }

    private fun clearFields() {
        binding.apply {
            volumeInput.text?.clear()
            caffeineInput.text?.clear()
            costInput.text?.clear()
            timeInput.text?.clear()
            dateInput.text?.clear()
            dateInput.tag = null
            coffeeShopInput.text?.clear()
            commentInput.text?.clear()
            coffeeTypeSpinner.setSelection(0)
            quantityInput.setText("1")
        }
        userChangedVolume = false
        userChangedCaffeine = false
        userChangedDate = false
        userChangedTime = false
        selectedType = null
    }

    private fun validateFields(): Boolean {
        var ok = true
        if (binding.coffeeTypeSpinner.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), R.string.need_choose_coffee_type, Toast.LENGTH_LONG).show()
            ok = false
        }
        listOf(binding.volumeInput, binding.quantityInput, binding.caffeineInput, binding.costInput,
            binding.dateInput, binding.timeInput).forEach { edit ->
            if (edit.text.isNullOrBlank()) {
                edit.error = getString(R.string.field_required)
                ok = false
            } else {
                edit.error = null
            }
        }
        return ok
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
