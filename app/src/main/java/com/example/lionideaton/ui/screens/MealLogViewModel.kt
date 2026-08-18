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

        // Demo history so Analysis/Basket/Report have enough data to show meaningful patterns
        // before the user logs anything themselves in a live demo. "Today" starts empty and
        // fills in as they register meals. Covers a full ~30 days (not just 7) so the Report
        // screen's "최근 7일" vs "최근 30일" views actually differ — the older 3 weeks lean
        // toward processed/high-GI food, easing into the more balanced most-recent week, so the
        // 30-day average, deficiencies, and patterns read differently from the 7-day ones.
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

            val recentWeek = listOf(
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

            val unhealthyPool = listOf("라면", "치킨", "피자", "떡볶이", "햄버거", "마라탕", "김치찌개")
            val balancedPool = listOf("샐러드", "연어", "토마토", "고등어", "그릭 요거트와 블루베리", "아보카도 연어 샐러드", "브로콜리", "키위", "아몬드")

            val olderHistory = (7..29L).flatMap { daysAgo ->
                val leaningUnhealthy = daysAgo >= 14 // weeks 3-4 back lean unhealthy; week 2 back trends healthier
                val lunchPool = if (leaningUnhealthy) unhealthyPool else balancedPool
                val dinnerPool = if (leaningUnhealthy) unhealthyPool else balancedPool
                val lunch = entryFor(daysAgo, 12, 30, lunchPool[(daysAgo % lunchPool.size).toInt()])
                val dinner = if (daysAgo % 2 == 0L) entryFor(daysAgo, 19, 0, dinnerPool[((daysAgo + 3) % dinnerPool.size).toInt()]) else null
                listOfNotNull(lunch, dinner)
            }

            return recentWeek + olderHistory
        }
    }
}
