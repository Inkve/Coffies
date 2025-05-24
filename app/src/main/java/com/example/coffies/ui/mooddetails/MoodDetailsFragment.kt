package com.example.coffies.ui.mooddetails

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.databinding.FragmentMoodDetailsBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MoodDetailsFragment : Fragment() {

    private var _binding: FragmentMoodDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: MoodDetailsFragmentArgs by navArgs()

    private val viewModel: MoodDetailsViewModel by viewModels {
        MoodDetailsViewModelFactory(AppDatabase.getInstance(requireContext()), args.moodEntryId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.mood_details_menu, menu)
            }
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                if (state.loading) return@onEach
                if (state.error != null) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    return@onEach
                }
                val mood = state.mood
                val coffee = state.coffee
                // Обязательное наличие связанного кофе
                if (mood == null || coffee == null) {
                    Toast.makeText(requireContext(), "Запись или связанный кофе не найдены", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return@onEach
                }
                binding.moodIcon.setImageResource(viewModel.moodLevelToDrawable(mood.mood_level))
                binding.moodLevelValue.text = viewModel.moodLevelToText(mood.mood_level)
                binding.dateValue.text = viewModel.formatDate(mood.date)
                binding.timeValue.text = mood.time
                binding.commentValue.text = mood.comment ?: getString(R.string.no_data)
                binding.linkedCoffeeInfo.text = viewModel.getLinkedCoffeeInfo()

                // Переход к деталям кофе
                binding.linkedCoffeeContainer.setOnClickListener {
                    val action = MoodDetailsFragmentDirections.actionMoodDetailsToCoffeeDetails(coffee.id)
                    findNavController().navigate(action)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext(), R.style.CoffiesDialogTheme)
            .setTitle("Удалить запись?")
            .setMessage("Вы действительно хотите удалить это настроение? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { dialog, _ ->
                viewModel.deleteMood {
                    parentFragmentManager.setFragmentResult(
                        "MOOD_DELETED",
                        bundleOf("deleted_id" to args.moodEntryId)
                    )
                    findNavController().popBackStack()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
