package com.example.coffies.ui.coffeetype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.coffeetype.CoffeeType
import com.example.coffies.database.coffeetype.CoffeeTypeDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoffeeTypeViewModel(private val dao: CoffeeTypeDao) : ViewModel() {

    private val _formState = MutableStateFlow(CoffeeTypeFormState())
    val formState: StateFlow<CoffeeTypeFormState> = _formState

    fun submit(name: String, volStr: String, caffeineStr: String) {
        var valid = true
        var nameError: String? = null
        var volumeError: String? = null
        var caffeineError: String? = null

        val vol = volStr.toIntOrNull()
        val caffeine = caffeineStr.toFloatOrNull()

        if (name.isBlank()) {
            nameError = "Введите название"
            valid = false
        }
        if (volStr.isBlank()) {
            volumeError = "Введите объем"
            valid = false
        } else if (vol == null || vol <= 0) {
            volumeError = "Некорректное число"
            valid = false
        }
        if (caffeineStr.isBlank()) {
            caffeineError = "Введите кофеин (мг/мл)"
            valid = false
        } else if (caffeine == null || caffeine < 0f) {
            caffeineError = "Некорректное число"
            valid = false
        }

        if (!valid) {
            _formState.value = CoffeeTypeFormState(
                nameError = nameError,
                volumeError = volumeError,
                caffeineError = caffeineError,
                success = false
            )
            return
        }

        viewModelScope.launch {
            val coffeeType = CoffeeType(
                name = name,
                default_volume_ml = vol,
                default_caffeine_mg_ml = caffeine
            )
            dao.insert(coffeeType)
            _formState.value = CoffeeTypeFormState(success = true)
        }
    }

    fun clearNameError() {
        _formState.value = _formState.value.copy(nameError = null)
    }
    fun clearVolumeError() {
        _formState.value = _formState.value.copy(volumeError = null)
    }
    fun clearCaffeineError() {
        _formState.value = _formState.value.copy(caffeineError = null)
    }
    fun clearSuccessFlag() {
        _formState.value = _formState.value.copy(success = false)
    }
}
