package com.example.coffies.ui.coffeetype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.coffeetype.CoffeeType
import com.example.coffies.database.coffeetype.CoffeeTypeDao
import kotlinx.coroutines.launch

class CoffeeTypeViewModel(private val dao: CoffeeTypeDao) : ViewModel() {

    fun insertType(name: String, defaultVolume: Int?, defaultCaffeine: Float?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val coffeeType = CoffeeType(
                name = name,
                default_volume_ml = defaultVolume,
                default_caffeine_mg_ml = defaultCaffeine
            )
            dao.insert(coffeeType)
            onSuccess()
        }
    }
}