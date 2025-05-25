package com.example.coffies.ui.coffeedetails

import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import com.example.coffies.databinding.FragmentCoffeeDetailsBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

class CoffeeDetailsFragment : Fragment() {

    private var _binding: FragmentCoffeeDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: CoffeeDetailsFragmentArgs by navArgs()
    private val viewModel: CoffeeDetailsViewModel by viewModels {
        CoffeeDetailsViewModelFactory(
            com.example.coffies.database.AppDatabase.getInstance(requireContext()),
            args.coffeeEntryId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoffeeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Подписка на state
        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                if (state.isLoading) return@onEach
                state.coffee?.let { bindCoffee(it, state) }
                bindMoodBlocks(state)
                state.error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.coffee_details_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun bindCoffee(entry: com.example.coffies.database.coffeentry.CoffeeEntry, state: CoffeeDetailsState) {
        binding.coffeeTypeValue.text = state.coffeeTypeNames[entry.coffee_type_id] ?: "Тип ${entry.coffee_type_id}"
        binding.volumeValue.text = entry.volume_ml.toString()
        binding.quantityValue.text = entry.quantity.toString()
        binding.caffeineValue.text = String.format("%.0f", entry.caffeine_mg)
        binding.priceValue.text = entry.price?.let { "${it}₽" } ?: "—"
        binding.dateValue.text = formatDate(entry.date)
        binding.timeValue.text = entry.time
        binding.placeValue.text = entry.place ?: getString(R.string.no_data)
        binding.commentValue.text = entry.comment ?: getString(R.string.no_data)
    }

    private fun bindMoodBlocks(state: CoffeeDetailsState) {
        bindMoodBlock(
            container = binding.moodBeforeContainer,
            label = binding.moodBeforeLabel,
            value = binding.moodBeforeValue,
            icon = binding.moodBeforeIcon,
            mood = state.moodMap["before and during"],
            momentType = "before and during"
        )
        bindMoodBlock(
            container = binding.moodAfterContainer,
            label = binding.moodAfterLabel,
            value = binding.moodAfterValue,
            icon = binding.moodAfterIcon,
            mood = state.moodMap["after"],
            momentType = "after"
        )
    }

    private fun bindMoodBlock(
        container: View,
        label: View,
        value: android.widget.TextView,
        icon: android.widget.ImageView,
        mood: com.example.coffies.database.moodentry.MoodEntry?,
        momentType: String
    ) {
        container.setOnClickListener {
            val entry = viewModel.state.value.coffee ?: return@setOnClickListener
            if (mood != null) {
                findNavController().navigate(
                    R.id.action_coffeeDetails_to_moodDetails,
                    bundleOf("moodEntryId" to mood.id)
                )
            } else {
                findNavController().navigate(
                    R.id.action_coffeeDetails_to_addMood,
                    bundleOf(
                        "relatedCoffeeId" to entry.id,
                        "relatedCoffeeTime" to entry.time,
                        "relatedCoffeeDate" to entry.date,
                        "momentType" to momentType
                    )
                )
            }
        }
        if (mood != null) {
            value.text = getString(
                R.string.mood_brief_template,
                moodLevelToText(mood.mood_level),
                mood.time,
                formatDate(mood.date)
            )
            icon.setImageResource(moodLevelToDrawable(mood.mood_level))
        } else {
            value.text = getString(R.string.no_data)
            icon.setImageResource(R.drawable.mood_3)
        }
    }

    private fun formatDate(iso: String): String {
        return try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(iso)
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(parsed!!)
        } catch (e: Exception) {
            iso
        }
    }

    private fun moodLevelToText(level: Int): String {
        return when (level) {
            1 -> getString(R.string.mood_1_short)
            2 -> getString(R.string.mood_2_short)
            3 -> getString(R.string.mood_3_short)
            4 -> getString(R.string.mood_4_short)
            5 -> getString(R.string.mood_5_short)
            else -> getString(R.string.no_data)
        }
    }

    private fun moodLevelToDrawable(level: Int): Int {
        return when (level) {
            1 -> R.drawable.mood_1
            2 -> R.drawable.mood_2
            3 -> R.drawable.mood_3
            4 -> R.drawable.mood_4
            5 -> R.drawable.mood_5
            else -> R.drawable.mood_3
        }
    }

    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(
            requireContext(),
            R.style.CoffiesDeleteDialogTheme
        )
            .setTitle("Удалить запись?")
            .setMessage("Вы действительно хотите удалить эту запись кофе и все связанные с ней настроения? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { dialog, _ ->
                viewModel.deleteWithMoods {
                    parentFragmentManager.setFragmentResult(
                        "COFFEE_DELETED",
                        bundleOf("deleted_id" to args.coffeeEntryId)
                    )
                    activity?.runOnUiThread { findNavController().popBackStack() }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
