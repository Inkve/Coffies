package com.example.coffies.ui.mooddetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.moodentry.MoodEntry
import com.example.coffies.databinding.FragmentMoodDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MoodDetailsFragment : Fragment() {

    private var _binding: FragmentMoodDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: MoodDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = AppDatabase.getInstance(requireContext())

        MainScope().launch {
            val mood = withContext(Dispatchers.IO) {
                db.moodEntryDao().getById(args.moodEntryId)
            }
            mood?.let {
                bindMood(it)

                val linkedCoffee = withContext(Dispatchers.IO) {
                    val links = db.coffeeMoodLinkDao().getLinksForMoodEntry(it.id)
                    links.firstOrNull()?.let { link ->
                        db.coffeeEntryDao().getById(link.coffee_entry_id)
                    }
                }

                val coffeeTypeName = withContext(Dispatchers.IO) {
                    linkedCoffee?.let { coffee ->
                        val types = db.coffeeTypeDao().getAllCoffeeTypeNames()
                        types.find { it.id == coffee.coffee_type_id }?.name ?: getString(R.string.no_data)
                    } ?: ""
                }

                linkedCoffee?.let { coffee ->
                    binding.linkedCoffeeContainer.visibility = View.VISIBLE
                    val coffeeText = getString(R.string.linked_coffee_template, coffeeTypeName, coffee.time)
                    binding.linkedCoffeeInfo.text = coffeeText
                    binding.linkedCoffeeContainer.setOnClickListener {
                        val action = MoodDetailsFragmentDirections.actionMoodDetailsToCoffeeDetails(coffee.id)
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun bindMood(entry: MoodEntry) {
        binding.moodIcon.setImageResource(moodLevelToDrawable(entry.mood_level))
        binding.moodLevelValue.text = moodLevelToText(entry.mood_level)
        binding.dateValue.text = formatDate(entry.date)
        binding.timeValue.text = entry.time
        binding.commentValue.text = entry.comment ?: getString(R.string.no_data)
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
