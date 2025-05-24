package com.example.coffies.ui.addmood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.coffemoodlink.CoffeeMoodLink
import com.example.coffies.database.moodentry.MoodEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MoodInputViewModel(private val db: AppDatabase) : ViewModel() {

    private val _formState = MutableStateFlow(MoodInputFormState(momentType = MoodMomentType.BEFORE))
    val formState: StateFlow<MoodInputFormState> = _formState.asStateFlow()

    init {
        loadCoffeeTypeNames()
        refreshCoffeeList()
    }

    fun prefill(
        relatedCoffeeId: Int?,
        relatedCoffeeTime: String?,
        relatedCoffeeDate: String?,
        momentType: MoodMomentType?
    ) {
        _formState.update { st ->
            st.copy(
                relatedCoffeeId = relatedCoffeeId,
                relatedCoffeeTime = relatedCoffeeTime,
                relatedCoffeeDate = relatedCoffeeDate,
                momentType = momentType,
                // Заполним дату и время, если есть
                date = relatedCoffeeDate ?: "",
                displayDate = relatedCoffeeDate?.split("-")?.reversed()?.joinToString(".") ?: "",
                time = relatedCoffeeTime ?: ""
            )
        }
        refreshCoffeeList()
    }


    private fun loadCoffeeTypeNames() {
        viewModelScope.launch {
            val map = db.coffeeTypeDao().getAllCoffeeTypeNames().associate { it.id to it.name }
            _formState.update { it.copy(coffeeTypeNameMap = map) }
        }
    }

    fun onMoodSelected(level: Int) {
        _formState.update { it.copy(moodLevel = level, moodError = null) }
    }

    fun onMomentSelected(moment: MoodMomentType?) {
        _formState.update { st ->
            val relatedTime = st.relatedCoffeeTime
            val relatedDate = st.relatedCoffeeDate
            var newTime = st.time
            var newDate = st.date

            if (moment != null && relatedTime != null && relatedDate != null) {
                val cmp = "${st.date}T${st.time}".compareTo("$relatedDate" + "T" + relatedTime)
                if (moment == MoodMomentType.BEFORE && cmp > 0) {
                    newTime = relatedTime
                    newDate = relatedDate
                }
                if (moment == MoodMomentType.AFTER && cmp < 0) {
                    val nowTime = java.time.LocalTime.now().toString().substring(0,5)
                    newTime = nowTime
                    newDate = relatedDate
                }
            }
            st.copy(
                momentType = moment,
                momentError = null,
                time = newTime,
                date = newDate,
                displayDate = if (newDate.isNotEmpty()) newDate.split("-").reversed().joinToString(".") else ""
            )
        }
        refreshCoffeeList()
    }

    fun onRelatedCoffeeSelected(entry: CoffeeEntry?) {
        _formState.update { st ->
            if (entry == null) return@update st.copy(
                relatedCoffeeId = null,
                relatedCoffeeError = null,
                relatedCoffeeTime = null,
                relatedCoffeeDate = null,
                date = "",
                displayDate = "",
                time = ""
            )
            // При выборе — ставим дату и время приема кофе
            val newDate = entry.date
            val newTime = entry.time
            st.copy(
                relatedCoffeeId = entry.id,
                relatedCoffeeError = null,
                relatedCoffeeTime = entry.time,
                relatedCoffeeDate = entry.date,
                date = newDate,
                displayDate = if (newDate.isNotEmpty()) newDate.split("-").reversed().joinToString(".") else "",
                time = newTime
            )
        }
    }

    fun onDateChanged(display: String, iso: String) {
        _formState.update { it.copy(date = iso, displayDate = display, dateError = null, timeError = null) }
    }

    fun onTimeChanged(value: String) {
        _formState.update { it.copy(time = value, dateError = null, timeError = null) }
    }

    fun onCommentChanged(text: String) {
        _formState.update { it.copy(comment = text) }
    }

    fun refreshCoffeeList() {
        viewModelScope.launch {
            val usedBefore = db.coffeeMoodLinkDao().getCoffeeEntryIdsByType(MoodMomentType.BEFORE.dbValue)
            val usedAfter = db.coffeeMoodLinkDao().getCoffeeEntryIdsByType(MoodMomentType.AFTER.dbValue)
            val coffeeList = db.coffeeEntryDao().getAllDescLimited(20).first()
            val filter = _formState.value.momentType
            val filtered = when (filter) {
                MoodMomentType.BEFORE -> coffeeList.filter { it.id !in usedBefore }
                MoodMomentType.AFTER -> coffeeList.filter { it.id !in usedAfter }
                else -> coffeeList
            }
            // Проверяем, остался ли выбранный relatedCoffeeId в новом списке
            val currentRelatedId = _formState.value.relatedCoffeeId
            val isRelatedValid = currentRelatedId != null && filtered.any { it.id == currentRelatedId }
            _formState.update {
                it.copy(
                    coffeeList = filtered,
                    // если связанный прием больше не валиден — сбросить его
                    relatedCoffeeId = if (isRelatedValid) currentRelatedId else null,
                    relatedCoffeeTime = if (isRelatedValid) it.relatedCoffeeTime else null,
                    relatedCoffeeDate = if (isRelatedValid) it.relatedCoffeeDate else null
                )
            }
        }
    }

    fun submit() {
        viewModelScope.launch {
            val st = _formState.value
            var valid = true
            var moodError: String? = null
            var momentError: String? = null
            var relatedCoffeeError: String? = null
            var dateError: String? = null
            var timeError: String? = null

            if (st.moodLevel !in 1..5) {
                moodError = "Выберите настроение"
                valid = false
            }
            if (st.momentType == null) {
                momentError = "Выберите момент"
                valid = false
            }
            if (st.relatedCoffeeId == null) {
                relatedCoffeeError = "Свяжите с приемом кофе"
                valid = false
            }
            if (st.date.isBlank()) {
                dateError = "Выберите дату"
                valid = false
            }
            if (st.time.isBlank()) {
                timeError = "Выберите время"
                valid = false
            }
            // Проверка на валидность времени по связи
            if (st.momentType != null && st.relatedCoffeeTime != null && st.relatedCoffeeDate != null) {
                val cmp = "${st.date}T${st.time}".compareTo("${st.relatedCoffeeDate}T${st.relatedCoffeeTime}")
                if (st.momentType == MoodMomentType.BEFORE && cmp > 0) {
                    timeError = "Дата/время должны быть не позже приёма кофе"
                    valid = false
                }
                if (st.momentType == MoodMomentType.AFTER && cmp < 0) {
                    timeError = "Дата/время должны быть не раньше приёма кофе"
                    valid = false
                }
            }
            // Будущие даты блокируем
            val now = java.time.LocalDateTime.now()
            val inputDT = try { java.time.LocalDateTime.parse("${st.date}T${st.time}") } catch (_: Exception) { null }
            if (inputDT != null && inputDT > now) {
                timeError = "Дата/время не могут быть из будущего"
                valid = false
            }

            _formState.update {
                it.copy(
                    moodError = moodError,
                    momentError = momentError,
                    relatedCoffeeError = relatedCoffeeError,
                    dateError = dateError,
                    timeError = timeError
                )
            }

            if (!valid) return@launch

            try {
                val moodId = db.moodEntryDao().insert(
                    MoodEntry(
                        mood_level = st.moodLevel,
                        date = st.date,
                        time = st.time,
                        comment = st.comment.ifBlank { null }
                    )
                ).toInt()
                db.coffeeMoodLinkDao().insert(
                    CoffeeMoodLink(
                        coffee_entry_id = st.relatedCoffeeId!!,
                        mood_entry_id = moodId,
                        relation_type = st.momentType!!.dbValue
                    )
                )
                _formState.update { it.copy(success = true) }
                resetForm()
            } catch (e: Exception) {
                // обработка ошибок по желанию
            }
        }
    }

    fun resetForm() {
        _formState.value = MoodInputFormState(momentType = MoodMomentType.BEFORE)
        loadCoffeeTypeNames()
        refreshCoffeeList()
    }
}
