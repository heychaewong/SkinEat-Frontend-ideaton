package com.example.lionideaton.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.GaugeOrange
import com.example.lionideaton.ui.theme.GaugeRed
import com.example.lionideaton.ui.theme.GaugeTrack
import com.example.lionideaton.ui.theme.LionideatonTheme
import com.example.lionideaton.ui.theme.PhotoPromptAccent
import com.example.lionideaton.ui.theme.PhotoPromptBackground
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class TodayFoodLog(
    val name: String,
    val skinTag: String,
    val mealTime: String,
    val isWarning: Boolean
)

private val homeTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private const val ANTIOXIDANT_VIT_C_TARGET = 100.0
private const val ANTIOXIDANT_VIT_E_TARGET = 15.0
private const val COLLAGEN_PROTEIN_TARGET = 60.0
private const val COLLAGEN_ZINC_TARGET = 8.0

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogFoodClick: () -> Unit = {},
    onSkinScoreClick: () -> Unit = {},
    mealLogViewModel: MealLogViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val profile by userProfileViewModel.profile.collectAsState()
    val allEntries by mealLogViewModel.entries.collectAsState()
    val todayEntries = remember(allEntries) { mealLogViewModel.todayEntries() }
    val todayItems = todayEntries.flatMap { it.items }

    val score = SkinScoreCalculator.scoreForEntries(todayEntries)

    val vitC = SkinScoreCalculator.averageNutrient(todayEntries) { it.vitCMg }.average ?: 0.0
    val vitE = SkinScoreCalculator.averageNutrient(todayEntries) { it.vitEMg }.average ?: 0.0
    val protein = SkinScoreCalculator.averageNutrient(todayEntries) { it.proteinG }.average ?: 0.0
    val zinc = SkinScoreCalculator.averageNutrient(todayEntries) { it.zincMg }.average ?: 0.0

    val antioxidantIndex = (((vitC / ANTIOXIDANT_VIT_C_TARGET) * 50) + ((vitE / ANTIOXIDANT_VIT_E_TARGET) * 50))
        .roundToInt().coerceIn(0, 100)
    val collagenIndex = (((protein / COLLAGEN_PROTEIN_TARGET) * 50) + ((zinc / COLLAGEN_ZINC_TARGET) * 50))
        .roundToInt().coerceIn(0, 100)
    val hasInflammationConcern = todayItems.any { item ->
        (item.food.satFatG?.let { it * item.portionRatio >= 5.0 } == true) || (item.food.sugarG * item.portionRatio >= 15.0)
    }

    val comment = when {
        todayEntries.isEmpty() -> "아직 오늘 기록한 식사가 없어요. 음식을 기록하면 피부 점수가 바로 계산돼요."
        hasInflammationConcern -> "오늘 포화지방·당류가 높은 식사가 있었어요. 다음 식사에서는 채소와 오메가3 공급 식품을 함께 섭취해보세요."
        else -> "오늘 섭취한 식단이 전반적으로 균형 잡혀 있어요. 이 흐름을 유지해보세요!"
    }

    val todayFoods = todayEntries.sortedBy { it.eatenAt }.flatMap { entry ->
        entry.items.map { item ->
            val itemScore = SkinScoreCalculator.scoreForItem(item)
            TodayFoodLog(
                name = item.food.name,
                skinTag = when {
                    itemScore >= 80 -> "Skin Friendly"
                    itemScore >= 60 -> "보통"
                    else -> "주의 필요"
                },
                mealTime = "${entry.mealType.label} ${entry.eatenAt.toLocalTime().format(homeTimeFormatter)}",
                isWarning = itemScore < 60
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkinScoreCard(
            skinType = profile.skinType?.let { "${it.label} 피부" } ?: "피부타입 미설정",
            score = score,
            comment = comment,
            onClick = onSkinScoreClick
        )
        SkinPhotoPrompt()
        NutrientIntakeSection(
            antioxidant = antioxidantIndex,
            collagenSynthesis = collagenIndex,
            inflammationStatus = if (hasInflammationConcern) "주의" else "양호"
        )
        if (todayFoods.isNotEmpty()) {
            TodayFoodLogSection(foods = todayFoods)
        }
        LogTodayFoodButton(onClick = onLogFoodClick)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SkinScoreCard(skinType: String, score: Int, comment: String, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "오늘의 피부 점수",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                SkinTypeTag(skinType)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SkinScoreGauge(score = score, maxScore = 100, modifier = Modifier.size(160.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = comment,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SkinTypeTag(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SkinScoreGauge(score: Int, maxScore: Int, modifier: Modifier = Modifier) {
    val progress = (score.toFloat() / maxScore).coerceIn(0f, 1f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val startAngle = 150f
            val maxSweep = 240f
            drawArc(
                color = GaugeTrack,
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(GaugeOrange, GaugeRed),
                    center = Offset(size.width / 2f, size.height / 2f)
                ),
                startAngle = startAngle,
                sweepAngle = maxSweep * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(text = "/ $maxScore 점", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun SkinPhotoPrompt(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PhotoPromptBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = CardWhite) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = PhotoPromptAccent,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "오늘의 피부 사진 기록하기",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PhotoPromptAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "사진을 기록하면 식단 매칭 분석율이 40% 올라가요!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = PhotoPromptAccent)
        }
    }
}

@Composable
private fun NutrientIntakeSection(antioxidant: Int, collagenSynthesis: Int, inflammationStatus: String) {
    Column {
        Text(
            text = "오늘의 영양 섭취",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NutrientStatCard(
                label = "항산화지수",
                value = "$antioxidant%",
                valueColor = GaugeOrange,
                modifier = Modifier.weight(1f)
            )
            NutrientStatCard(
                label = "콜라겐합성",
                value = "$collagenSynthesis%",
                valueColor = PositiveTagText,
                modifier = Modifier.weight(1f)
            )
            NutrientStatCard(
                label = "염증지수",
                value = inflammationStatus,
                valueColor = WarningTagText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NutrientStatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun TodayFoodLogSection(foods: List<TodayFoodLog>) {
    Column {
        Text(
            text = "오늘 먹은 음식",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            foods.forEach { food -> FoodLogRow(food) }
        }
    }
}

@Composable
private fun FoodLogRow(food: TodayFoodLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodThumbnail()
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = food.mealTime, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            FoodTag(text = food.skinTag, isWarning = food.isWarning)
        }
    }
}

// Placeholder thumbnail until real food photos are wired up.
@Composable
private fun FoodThumbnail() {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FoodTag(text: String, isWarning: Boolean) {
    val backgroundColor = if (isWarning) WarningTagBackground else PositiveTagBackground
    val textColor = if (isWarning) WarningTagText else PositiveTagText
    Surface(shape = RoundedCornerShape(50), color = backgroundColor) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LogTodayFoodButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "오늘의 음식 기록하기", fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    LionideatonTheme {
        HomeScreen()
    }
}
