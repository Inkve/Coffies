package com.example.coffies.ui.coffeetype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentCoffeeTypeBinding

class CoffeeTypeFragment : Fragment() {

    private var _binding: FragmentCoffeeTypeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CoffeeTypeViewModel by viewModels {
        val dao = AppDatabase.getInstance(requireContext()).coffeeTypeDao()
        CoffeeTypeViewModelFactory(dao)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoffeeTypeBinding.inflate(inflater, container, false)

        binding.saveTypeButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val volStr = binding.defaultVolumeInput.text.toString().trim()
            val caffeineStr = binding.defaultCaffeineInput.text.toString().trim()
            val vol = volStr.toIntOrNull()
            val caffeine = caffeineStr.toFloatOrNull()

            var valid = true

            if (name.isEmpty()) {
                binding.nameInput.error = getString(com.example.coffies.R.string.enter_coffee_type_name)
                valid = false
            } else {
                binding.nameInput.error = null
            }

            if (volStr.isEmpty()) {
                binding.defaultVolumeInput.error = "Введите объем"
                valid = false
            } else if (vol == null || vol <= 0) {
                binding.defaultVolumeInput.error = "Некорректное число"
                valid = false
            } else {
                binding.defaultVolumeInput.error = null
            }

            if (caffeineStr.isEmpty()) {
                binding.defaultCaffeineInput.error = "Введите кофеин (мг/мл)"
                valid = false
            } else if (caffeine == null || caffeine < 0f) {
                binding.defaultCaffeineInput.error = "Некорректное число"
                valid = false
            } else {
                binding.defaultCaffeineInput.error = null
            }

            if (valid) {
                viewModel.insertType(name, vol, caffeine) {
                    binding.nameInput.text.clear()
                    binding.defaultVolumeInput.text.clear()
                    binding.defaultCaffeineInput.text.clear()
                    hideKeyboard()
                    Toast.makeText(requireContext(), com.example.coffies.R.string.saved, Toast.LENGTH_LONG).show()
                }
            }
        }
        return binding.root
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: binding.root
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}