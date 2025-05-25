package com.example.coffies.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    private var isDialogShown = false
    private var isResetDialogShown = false
    private var isUpdatingEditText = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_settings_actions, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        if (viewModel.state.value.isEditing) {
                            if (viewModel.hasChanges()) {
                                if (!isDialogShown) {
                                    isDialogShown = true
                                    showUnsavedChangesDialog()
                                }
                            } else {
                                viewModel.exitEditMode()
                                hideKeyboard()
                            }
                        } else {
                            viewModel.enterEditMode()
                        }
                        true
                    }
                    R.id.action_reset -> {
                        if (!isResetDialogShown) showResetDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.nameEdit.addTextChangedListener {
            if (viewModel.state.value.isEditing && !isUpdatingEditText) {
                viewModel.onNameChanged(it?.toString().orEmpty())
            }
        }
        binding.cupGoalEdit.addTextChangedListener {
            if (viewModel.state.value.isEditing && !isUpdatingEditText) {
                viewModel.onCupGoalChanged(it?.toString().orEmpty())
            }
        }
        binding.monthlyCupGoalEdit.addTextChangedListener {
            if (viewModel.state.value.isEditing && !isUpdatingEditText) {
                viewModel.onMonthlyCupGoalChanged(it?.toString().orEmpty())
            }
        }
        binding.spendGoalEdit.addTextChangedListener {
            if (viewModel.state.value.isEditing && !isUpdatingEditText) {
                viewModel.onSpendGoalChanged(it?.toString().orEmpty())
            }
        }
        binding.monthlySpendGoalEdit.addTextChangedListener {
            if (viewModel.state.value.isEditing && !isUpdatingEditText) {
                viewModel.onMonthlySpendGoalChanged(it?.toString().orEmpty())
            }
        }

        binding.saveButton.setOnClickListener {
            viewModel.save()
            hideKeyboard()
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> render(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.state.value.isEditing) {
                        if (viewModel.hasChanges()) {
                            if (!isDialogShown) {
                                isDialogShown = true
                                showUnsavedChangesDialog()
                            }
                        } else {
                            viewModel.exitEditMode()
                            hideKeyboard()
                        }
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun showUnsavedChangesDialog() {
        android.app.AlertDialog.Builder(requireContext(), R.style.CoffiesDialogTheme)
            .setTitle("Сохранить изменения?")
            .setMessage("У вас есть несохранённые изменения. Сохранить перед выходом?")
            .setPositiveButton("Сохранить") { dialog, _ ->
                viewModel.save()
                dialog.dismiss()
                Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Выйти без сохранения") { dialog, _ ->
                viewModel.discardChangesAndExitEditMode()
                hideKeyboard()
                dialog.dismiss()
            }
            .setOnDismissListener { isDialogShown = false }
            .show()
    }

    private fun showResetDialog() {
        isResetDialogShown = true
        android.app.AlertDialog.Builder(requireContext(), R.style.CoffiesDeleteDialogTheme)
            .setTitle("Сбросить настройки?")
            .setMessage("Вы уверены, что хотите сбросить настройки пользователя? Это действие удалит все ваши настройки и не может быть отменено.")
            .setPositiveButton("Сбросить") { dialog, _ ->
                viewModel.resetSettings()
                dialog.dismiss()
                Toast.makeText(requireContext(), "Настройки сброшены", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener { isResetDialogShown = false }
            .show()
    }

    private fun render(state: SettingsViewState) {
        if (state.isEditing) {
            isUpdatingEditText = true
            if (binding.nameEdit.text.toString() != state.name)
                binding.nameEdit.setText(state.name)
            if (binding.cupGoalEdit.text.toString() != (state.cupGoal?.toString() ?: ""))
                binding.cupGoalEdit.setText(state.cupGoal?.toString() ?: "")
            if (binding.monthlyCupGoalEdit.text.toString() != (state.monthlyCupGoal?.toString() ?: ""))
                binding.monthlyCupGoalEdit.setText(state.monthlyCupGoal?.toString() ?: "")
            if (binding.spendGoalEdit.text.toString() != (state.spendGoal?.let { formatMoney(it) } ?: ""))
                binding.spendGoalEdit.setText(state.spendGoal?.let { formatMoney(it) } ?: "")
            if (binding.monthlySpendGoalEdit.text.toString() != (state.monthlySpendGoal?.let { formatMoney(it) } ?: ""))
                binding.monthlySpendGoalEdit.setText(state.monthlySpendGoal?.let { formatMoney(it) } ?: "")
            isUpdatingEditText = false
        }
        binding.nameText.text = if (state.name.isNotBlank()) state.name else "Не задано"
        binding.cupGoalText.text = state.cupGoal?.toString() ?: "Не задано"
        binding.monthlyCupGoalText.text = state.monthlyCupGoal?.toString() ?: "Не задано"
        binding.spendGoalText.text = state.spendGoal?.let { formatMoney(it) + " ₽" } ?: "Не задано"
        binding.monthlySpendGoalText.text = state.monthlySpendGoal?.let { formatMoney(it) + " ₽" } ?: "Не задано"

        setEditMode(state.isEditing)
    }

    private fun setEditMode(isEditing: Boolean) {
        binding.nameText.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.nameEdit.visibility = if (isEditing) View.VISIBLE else View.GONE

        binding.cupGoalText.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.cupGoalEdit.visibility = if (isEditing) View.VISIBLE else View.GONE

        binding.monthlyCupGoalText.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.monthlyCupGoalEdit.visibility = if (isEditing) View.VISIBLE else View.GONE

        binding.spendGoalText.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.spendGoalEdit.visibility = if (isEditing) View.VISIBLE else View.GONE

        binding.monthlySpendGoalText.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.monthlySpendGoalEdit.visibility = if (isEditing) View.VISIBLE else View.GONE

        binding.saveButton.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus ?: view
        view?.let {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun formatMoney(value: Float): String {
        return if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
