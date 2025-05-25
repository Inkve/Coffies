package com.example.coffies.ui.addcoffee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.coffeetype.CoffeeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CoffeeInputViewModel(
    private val db: AppDatabase
) : ViewModel() {

    private val _formState = MutableStateFlow(CoffeeInputFormState())
    val formState: StateFlow<CoffeeInputFormState> = _formState

    suspend fun getCoffeeTypesOnce(): List<CoffeeType> =
        withContext(Dispatchers.IO) { db.coffeeTypeDao().getAll().first() }

    fun onCoffeeTypeSelected(type: CoffeeType?) {
        if (type == null) return
        val now = Calendar.getInstance().time
        val dispDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(now)
        val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        _formState.value = _formState.value.copy(
            coffeeType = type,
            volume = type.default_volume_ml?.toString() ?: "",
            caffeine = type.default_caffeine_mg_ml?.let { mg -> type.default_volume_ml?.let { v -> "%.1f".format(mg * v) } } ?: "",
            quantity = "1",
            date = dispDate,
            dateIso = isoDate,
            time = time,
            coffeeTypeError = null
        )
    }

    fun onFieldChanged(field: String, value: String) {
        _formState.value = when (field) {
            "volume" -> _formState.value.copy(volume = value, volumeError = null)
            "quantity" -> _formState.value.copy(quantity = value, quantityError = null)
            "caffeine" -> _formState.value.copy(caffeine = value, caffeineError = null)
            "cost" -> _formState.value.copy(cost = value, costError = null)
            "date" -> _formState.value.copy(date = value, dateError = null)
            "time" -> _formState.value.copy(time = value, timeError = null)
            "place" -> _formState.value.copy(place = value)
            "comment" -> _formState.value.copy(comment = value)
            else -> _formState.value
        }
    }

    fun clearSuccessFlag() {
        _formState.value = _formState.value.copy(success = false, errorMsg = null)
    }

    fun submit() {
        val state = _formState.value
        var ok = true

        var coffeeTypeError: String? = null
        var volumeError: String? = null
        var quantityError: String? = null
        var caffeineError: String? = null
        var costError: String? = null
        var dateError: String? = null
        var timeError: String? = null

        val volume = state.volume.toIntOrNull()
        val quantity = state.quantity.toIntOrNull()
        val caffeine = state.caffeine.replace(',', '.').toDoubleOrNull()
        val cost = state.cost.replace(',', '.').toDoubleOrNull()

        if (state.coffeeType == null) {
            coffeeTypeError = "Выберите тип кофе"
            ok = false
        }
        if (volume == null || volume <= 0) {
            volumeError = "Введите объем"
            ok = false
        }
        if (quantity == null || quantity <= 0) {
            quantityError = "Введите количество"
            ok = false
        }
        if (caffeine == null || caffeine <= 0.0) {
            caffeineError = "Введите кофеин"
            ok = false
        }
        if (state.cost.isBlank()) {
            costError = "Введите стоимость"
            ok = false
        } else if (cost == null || cost < 0.0) {
            costError = "Некорректная цена"
            ok = false
        }
        if (state.dateIso.isBlank()) {
            dateError = "Введите дату"
            ok = false
        }
        if (state.time.isBlank()) {
            timeError = "Введите время"
            ok = false
        }

        if (!ok) {
            _formState.value = state.copy(
                coffeeTypeError = coffeeTypeError,
                volumeError = volumeError,
                quantityError = quantityError,
                caffeineError = caffeineError,
                costError = costError,
                dateError = dateError,
                timeError = timeError
            )
            return
        }

        viewModelScope.launch {
            try {
                val entry = CoffeeEntry(
                    coffee_type_id = state.coffeeType!!.id,
                    volume_ml = volume!!,
                    quantity = quantity!!,
                    caffeine_mg = caffeine!!,
                    price = cost,
                    date = state.dateIso,
                    time = state.time,
                    place = state.place.ifBlank { null },
                    comment = state.comment.ifBlank { null }
                )
                withContext(Dispatchers.IO) {
                    db.coffeeEntryDao().insert(entry)
                }
                _formState.value = CoffeeInputFormState(success = true)
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(errorMsg = e.message)
            }
        }
    }

    fun onDatePicked(displayDate: String, isoDate: String) {
        _formState.value = _formState.value.copy(date = displayDate, dateIso = isoDate, dateError = null)
    }

    fun onTimePicked(time: String) {
        _formState.value = _formState.value.copy(time = time, timeError = null)
    }
}
