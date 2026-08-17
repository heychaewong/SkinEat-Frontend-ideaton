package com.example.lionideaton.ui.screens

import androidx.lifecycle.ViewModel
import com.example.lionideaton.data.FoodSeedData
import com.example.lionideaton.data.model.Food
import com.example.lionideaton.data.model.FoodSource
import com.example.lionideaton.data.model.MealItem
import com.example.lionideaton.data.model.MealLogEntry
import com.example.lionideaton.data.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Activity-scoped shared meal log (see CartViewModel for the same pattern), read by
// Home/FoodLog/Analysis/Basket so a meal registered once shows up everywhere.
class MealLogViewModel : ViewModel() {

    private var nextId = 1000L

    private val _entries = MutableStateFlow(seedDemoHistory())
    val entries: StateFlow<List<MealLogEntry>> = _entries.asStateFlow()

    fun registerMeal(foodName: String, servingCount: Int, eatenAt: LocalDateTime = LocalDateTime.now()) {
        val food = FoodSeedData.findByName(foodName) ?: unknownFood(foodName)
        val entry = MealLogEntry(
            id = nextId++,
            eatenAt = eatenAt,
            mealType = inferMealType(eatenAt.toLocalTime()),
            items = listOf(MealItem(food = food, portionRatio = servingCount.toDouble()))
        )
        _entries.update { it + entry }
    }

    fun todayEntries(): List<MealLogEntry> = entriesForDate(LocalDate.now())

    fun entriesForDate(date: LocalDate): List<MealLogEntry> =
        entries.value.filter { it.eatenAt.toLocalDate() == date }

    fun recentEntries(days: Long = 7): List<MealLogEntry> {
        val cutoff = LocalDateTime.now().minusDays(days)
        return entries.value.filter { it.eatenAt.isAfter(cutoff) }
    }

    companion object {
        // A custom/typed food (not in the seed DB) contributes no nutrient data at all —
        // that's intentional: it should be excluded from averages, not counted as zero.
        private fun unknownFood(name: String) = Food(
            id = -1,
            name = name,
            source = FoodSource.USER_ADDED,
            servingG = 100,
            energyKcal = 0.0,
            carbG = 0.0,
            sugarG = 0.0,
            proteinG = 0.0,
            fatG = 0.0
        )

        private fun inferMealType(time: LocalTime): MealType = when {
            time.isBefore(LocalTime.of(11, 0)) -> MealType.BREAKFAST
            time.isBefore(LocalTime.of(15, 0)) -> MealType.LUNCH
            time.isBefore(LocalTime.of(18, 0)) -> MealType.SNACK
            else -> MealType.DINNER
        }

        // Demo history (matches the backend doc's "데모 계정 1개 + 7일치 기록" seed requirement)
        // so Analysis/Basket have enough data to show meaningful patterns before the user logs
        // anything themselves in a live demo. "Today" starts empty and fills in as they register meals.
        private fun seedDemoHistory(): List<MealLogEntry> {
            val today = LocalDate.now()
            var id = 1L

            fun entryFor(daysAgo: Long, hour: Int, minute: Int, foodName: String): MealLogEntry {
                val dateTime = today.minusDays(daysAgo).atTime(hour, minute)
                val food = FoodSeedData.findByName(foodName)
                return MealLogEntry(
                    id = id++,
                    eatenAt = dateTime,
                    mealType = inferMealType(dateTime.toLocalTime()),
                    items = listOfNotNull(food).map { MealItem(it) }
                )
            }

            return listOf(
                entryFor(6, 12, 30, "라면"),
                entryFor(6, 19, 0, "치킨"),
                entryFor(5, 12, 40, "마라탕"),
                entryFor(5, 16, 0, "그릭 요거트와 블루베리"),
                entryFor(4, 12, 30, "햄버거"),
                entryFor(4, 19, 30, "피자"),
                entryFor(3, 12, 30, "샐러드"),
                entryFor(3, 19, 0, "연어"),
                entryFor(2, 12, 30, "떡볶이"),
                entryFor(2, 19, 0, "토마토"),
                entryFor(1, 12, 30, "김치찌개"),
                entryFor(1, 19, 0, "고등어")
            )
        }
    }
}
