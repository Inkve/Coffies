package com.example.coffies.ui.coffeetype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentCoffeeTypeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
            viewModel.submit(
                binding.nameInput.text.toString().trim(),
                binding.defaultVolumeInput.text.toString().trim(),
                binding.defaultCaffeineInput.text.toString().trim()
            )
        }

        binding.nameInput.addTextChangedListener { viewModel.clearNameError() }
        binding.defaultVolumeInput.addTextChangedListener { viewModel.clearVolumeError() }
        binding.defaultCaffeineInput.addTextChangedListener { viewModel.clearCaffeineError() }

        lifecycleScope.launch {
            viewModel.formState.collectLatest { state ->
                binding.nameInput.error = state.nameError
                binding.defaultVolumeInput.error = state.volumeError
                binding.defaultCaffeineInput.error = state.caffeineError

                if (state.success) {
                    // Очистка полей, скрытие клавиатуры и тост
                    binding.nameInput.text?.clear()
                    binding.defaultVolumeInput.text?.clear()
                    binding.defaultCaffeineInput.text?.clear()
                    hideKeyboard()
                    Toast.makeText(requireContext(), com.example.coffies.R.string.saved, Toast.LENGTH_LONG).show()
                    viewModel.clearSuccessFlag()
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
