package com.example.coffies.ui.coffeedetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.moodentry.MoodEntry
import com.example.coffies.databinding.FragmentCoffeeDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CoffeeDetailsFragment : Fragment() {

    private var _binding: FragmentCoffeeDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: CoffeeDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoffeeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = AppDatabase.getInstance(requireContext())
        val id = args.coffeeEntryId

        lifecycleScope.launch {
            val coffee = withContext(Dispatchers.IO) {
                db.coffeeEntryDao().getById(id)
            }
            coffee?.let { entry ->
                bindCoffee(entry)
                bindMoodBlocks(db, entry.id)
            }
        }
    }

    private fun bindCoffee(entry: CoffeeEntry) {
        binding.coffeeTypeValue.text = getCoffeeTypeName(entry.coffee_type_id)
        binding.volumeValue.text = entry.volume_ml.toString()
        binding.quantityValue.text = entry.quantity.toString()
        binding.caffeineValue.text = String.format("%.0f", entry.caffeine_mg)
        binding.priceValue.text = entry.price?.let { "${it}₽" } ?: "—"
        binding.dateValue.text = formatDate(entry.date)
        binding.timeValue.text = entry.time
        binding.placeValue.text = entry.place ?: getString(R.string.no_data)
        binding.commentValue.text = entry.comment ?: getString(R.string.no_data)
    }

    private suspend fun bindMoodBlocks(db: AppDatabase, coffeeId: Int) {
        val links = withContext(Dispatchers.IO) {
            db.coffeeMoodLinkDao().getLinksForCoffeeEntry(coffeeId)
        }
        val moodMap = mutableMapOf<String, MoodEntry>()
        for (link in links) {
            val mood = withContext(Dispatchers.IO) {
                db.moodEntryDao().getById(link.mood_entry_id)
            }
            if (mood != null) moodMap[link.relation_type] = mood
        }

        setupMoodBlock(
            container = binding.moodBeforeContainer,
            label = binding.moodBeforeLabel,
            value = binding.moodBeforeValue,
            icon = binding.moodBeforeIcon,
            mood = moodMap["before and during"],
            onClick = {
                moodMap["before and during"]?.let {
                    findNavController().navigate(
                        R.id.action_coffeeDetails_to_moodDetails,
                        Bundle().apply { putInt("moodEntryId", it.id) }
                    )
                } ?: findNavController().navigate(R.id.action_coffeeDetails_to_addMood)
            }
        )

        setupMoodBlock(
            container = binding.moodAfterContainer,
            label = binding.moodAfterLabel,
            value = binding.moodAfterValue,
            icon = binding.moodAfterIcon,
            mood = moodMap["after"],
            onClick = {
                moodMap["after"]?.let {
                    findNavController().navigate(
                        R.id.action_coffeeDetails_to_moodDetails,
                        Bundle().apply { putInt("moodEntryId", it.id) }
                    )
                } ?: findNavController().navigate(R.id.action_coffeeDetails_to_addMood)
            }
        )
    }

    private fun setupMoodBlock(
        container: View,
        label: View,
        value: android.widget.TextView,
        icon: android.widget.ImageView,
        mood: MoodEntry?,
        onClick: () -> Unit
    ) {
        container.setOnClickListener { onClick() }
        if (mood != null) {
            value.text = getString(
                R.string.mood_brief_template,
                moodLevelToText(mood.mood_level),
                formatDate(mood.date),
                mood.time
            )
            icon.setImageResource(moodLevelToDrawable(mood.mood_level))
        } else {
            value.text = getString(R.string.mood_unknown)
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

    private fun getCoffeeTypeName(typeId: Int): String {
        // todo: загрузи из ViewModel или базы, если нужно
        return "Американо" // заглушка
    }

    private fun moodLevelToText(level: Int): String {
        return when (level) {
            1 -> getString(R.string.mood_1_short)
            2 -> getString(R.string.mood_2_short)
            3 -> getString(R.string.mood_3_short)
            4 -> getString(R.string.mood_4_short)
            5 -> getString(R.string.mood_5_short)
            else -> getString(R.string.mood_unknown)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
