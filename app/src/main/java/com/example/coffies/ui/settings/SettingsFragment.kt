package com.example.coffies.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.coffies.R
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.usersettings.UserSettings
import com.example.coffies.databinding.FragmentSettingsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    private var editing = false
    private var selectedBirthDate: String? = null // yyyy-MM-dd
    private var avatarUri: String? = null
    private var currentId: Int = 0

    private val avatarPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri = result.data?.data
            if (uri != null) {
                val localPath = copyAvatarToAppStorage(uri)
                if (localPath != null) {
                    avatarUri = localPath
                    showAvatarFromLocalPath(localPath)
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.settings.collectOnLifecycle(this) { settings ->
            showSettings(settings)
        }

        binding.avatarImage.setOnClickListener {
            if (editing) pickAvatarFromGallery()
        }

        binding.saveButton.setOnClickListener {
            val userSettings = collectSettingsFromUI()
            viewModel.saveSettings(userSettings)
            setEditingMode(false)
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        setEditingMode(false)

        binding.nameEdit.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) setEditingMode(true) }
        binding.cupGoalEdit.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) setEditingMode(true) }
        binding.spendGoalEdit.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) setEditingMode(true) }
        binding.ageValue.setOnClickListener {
            if (editing) showDatePicker()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_settings_actions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                setEditingMode(true)
                return true
            }
            R.id.action_reset -> {
                viewModel.resetSettings()
                Toast.makeText(requireContext(), "Настройки сброшены", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_full_reset -> {
                Toast.makeText(requireContext(), "Функция сброса всех данных будет реализована позже", Toast.LENGTH_LONG).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSettings(settings: UserSettings?) {
        if (settings == null) {
            currentId = 0
            binding.nameEdit.setText("")
            binding.cupGoalEdit.setText("")
            binding.spendGoalEdit.setText("")
            avatarUri = null
            binding.avatarImage.setImageResource(R.drawable.ic_avatar_placeholder)
            selectedBirthDate = null
            binding.ageValue.text = "—"
            return
        }
        currentId = settings.id
        binding.nameEdit.setText(settings.name ?: "")
        binding.cupGoalEdit.setText(settings.daily_cup_goal?.toString() ?: "")
        binding.spendGoalEdit.setText(settings.daily_spend_goal?.let {
            if (it == it.toInt().toFloat()) it.toInt().toString() else it.toString()
        } ?: "")
        avatarUri = settings.avatarUri
        showAvatarFromLocalPath(avatarUri)
        selectedBirthDate = settings.birth_date

        if (editing) {
            binding.ageValue.text = selectedBirthDate?.let { formatDateRu(it) } ?: "Выбрать дату"
        } else {
            binding.ageValue.text = settings.birth_date?.let { bd ->
                val years = calculateAge(bd)
                "$years лет"
            } ?: "—"
        }
    }

    private fun showAvatarFromLocalPath(path: String?) {
        if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists()) {
                binding.avatarImage.setImageURI(Uri.fromFile(file))
            } else {
                binding.avatarImage.setImageResource(R.drawable.ic_avatar_placeholder)
            }
        } else {
            binding.avatarImage.setImageResource(R.drawable.ic_avatar_placeholder)
        }
    }

    private fun copyAvatarToAppStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun setEditingMode(edit: Boolean) {
        editing = edit
        binding.nameEdit.isEnabled = edit
        binding.cupGoalEdit.isEnabled = edit
        binding.spendGoalEdit.isEnabled = edit
        binding.saveButton.visibility = if (edit) View.VISIBLE else View.GONE
        binding.avatarImage.alpha = if (edit) 0.85f else 1f

        if (edit) {
            binding.ageValue.setOnClickListener { showDatePicker() }
            binding.ageValue.setTextColor(resources.getColor(R.color.dark_beige, null))
            binding.ageValue.text = selectedBirthDate?.let { formatDateRu(it) } ?: "Выбрать дату"
        } else {
            binding.ageValue.setOnClickListener(null)
            binding.ageValue.setTextColor(resources.getColor(R.color.black, null))
            binding.ageValue.text = selectedBirthDate?.let { bd ->
                val years = calculateAge(bd)
                "$years лет"
            } ?: "—"
        }
    }

    private fun pickAvatarFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        avatarPicker.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun showDatePicker() {
        val currentDate = selectedBirthDate?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        } ?: LocalDate.now()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Дата рождения")
            .setTheme(R.style.CoffiesCalendarPickerTheme)
            .setSelection(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            val date = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            selectedBirthDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            binding.ageValue.text = formatDateRu(selectedBirthDate!!)
        }

        picker.show(parentFragmentManager, "birth_date_picker")
    }

    private fun formatDateRu(dateIso: String): String {
        return try {
            val d = LocalDate.parse(dateIso, DateTimeFormatter.ISO_LOCAL_DATE)
            d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) { dateIso }
    }

    private fun collectSettingsFromUI(): UserSettings {
        val name = binding.nameEdit.text?.toString()?.takeIf { it.isNotBlank() }
        val cups = binding.cupGoalEdit.text?.toString()?.toIntOrNull()
        val spend = binding.spendGoalEdit.text?.toString()?.toFloatOrNull()
        return UserSettings(
            id = currentId,
            name = name,
            birth_date = selectedBirthDate,
            daily_cup_goal = cups,
            daily_spend_goal = spend,
            avatarUri = avatarUri
        )
    }

    private fun calculateAge(birthDate: String): Int {
        return try {
            val dob = LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val today = LocalDate.now()
            var age = today.year - dob.year
            if (today < dob.plusYears(age.toLong())) age--
            age
        } catch (e: Exception) { 0 }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun <T> StateFlow<T>.collectOnLifecycle(fragment: Fragment, observer: (T) -> Unit) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                collect { observer(it) }
            }
        }
    }
}
