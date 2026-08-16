package com.example.lionideaton.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.model.MealItem
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CautionTagBackground
import com.example.lionideaton.ui.theme.CautionTagText
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText

private enum class SkinImpactLevel(val backgroundColor: Color, val textColor: Color) {
    Good(PositiveTagBackground, PositiveTagText),
    Moderate(CautionTagBackground, CautionTagText),
    Bad(WarningTagBackground, WarningTagText)
}

private data class ConcernIngredient(val label: String, val level: SkinImpactLevel)

private data class MenuSkinContribution(
    val name: String,
    val score: Int,
    val level: SkinImpactLevel,
    val statusLabel: String,
    val description: String
)

private fun levelForScore(score: Int): SkinImpactLevel = when {
    score >= 80 -> SkinImpactLevel.Good
    score >= 60 -> SkinImpactLevel.Moderate
    else -> SkinImpactLevel.Bad
}

private fun statusLabelForScore(score: Int): String = when {
    score >= 80 -> "좋음"
    score >= 60 -> "보통"
    else -> "나쁨"
}

private fun descriptionFor(item: MealItem): String {
    val food = item.food
    return when {
        food.omega3Mg != null && food.omega3Mg * item.portionRatio >= 500.0 -> "오메가3 풍부, 피부 장벽 강화 효과"
        food.satFatG != null && food.satFatG * item.portionRatio >= 5.0 -> "포화지방 다소 높음, 염증성 피부 트러블 유발 가능"
        food.sugarG * item.portionRatio >= 15.0 -> "당류 다소 높음, 콜라겐 손상 유발 가능"
        food.vitCMg != null && food.vitCMg * item.portionRatio >= 30.0 -> "비타민C 풍부, 항산화 효과"
        else -> "특별한 주의 성분 없이 무난한 식사예요"
    }
}

private data class SkinConcernExplanation(val nutrient: String, val explanation: String)

private val skinConcernExplanations = listOf(
    SkinConcernExplanation(
        "나트륨",
        "나트륨을 과다 섭취하면 체내 수분이 세포 밖으로 빠져나가며 피부가 건조해지고 붓기가 잘 생겨요."
    ),
    SkinConcernExplanation(
        "당류",
        "당류(Sugar)는 체내 단백질과 결합해 '최종당화산물(AGEs)'을 만들고, 이는 콜라겐과 엘라스틴 섬유를 파괴해 피부 탄력을 줄여요."
    ),
    SkinConcernExplanation(
        "포화지방",
        "포화지방을 과다 섭취하면 피지 분비가 늘고 염증 반응이 촉진되어 트러블이 잘 생기는 피부 환경이 만들어져요."
    )
)

@Composable
fun AnalysisScreen(modifier: Modifier = Modifier, mealLogViewModel: MealLogViewModel = viewModel()) {
    val allEntries by mealLogViewModel.entries.collectAsState()
    val todayEntries = remember(allEntries) { mealLogViewModel.todayEntries() }
    val recentEntries = remember(allEntries) { mealLogViewModel.recentEntries() }
    val todayItems = todayEntries.flatMap { it.items }

    val sugarConcern = todayItems.any { it.food.sugarG * it.portionRatio >= 15.0 }
    val satFatConcern = todayItems.any { item -> item.food.satFatG?.let { it * item.portionRatio >= 5.0 } == true }

    val concernIngredients = listOf(
        ConcernIngredient(if (sugarConcern) "당류 (주의)" else "당류 (안전)", if (sugarConcern) SkinImpactLevel.Moderate else SkinImpactLevel.Good),
        ConcernIngredient(if (satFatConcern) "포화지방 (주의)" else "포화지방 (안전)", if (satFatConcern) SkinImpactLevel.Bad else SkinImpactLevel.Good)
    )

    val concernDescription = when {
        satFatConcern -> "오늘 섭취한 고포화지방 음식으로 인해 피지 분비가 늘고 트러블이 생기기 쉬워질 수 있습니다."
        sugarConcern -> "오늘 섭취한 당류가 다소 높아 최종당화산물(AGEs) 생성으로 피부 탄력이 줄어들 수 있습니다."
        todayItems.isEmpty() -> "아직 오늘 기록한 식사가 없어요. 음식을 기록하면 분석이 시작돼요."
        else -> "오늘 식단에서 특별히 주의할 성분은 발견되지 않았어요."
    }

    val menuContributions = todayEntries.sortedByDescending { it.eatenAt }.flatMap { entry ->
        entry.items.map { item ->
            val score = SkinScoreCalculator.scoreForItem(item)
            MenuSkinContribution(
                name = item.food.name,
                score = score,
                level = levelForScore(score),
                statusLabel = statusLabelForScore(score),
                description = descriptionFor(item)
            )
        }
    }

    val deficiency = SkinScoreCalculator.analyzeDeficiency(recentEntries)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "먹은 만큼 예뻐지는 맞춤 분석",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "오늘 섭취한 음식이 내일의 피부에 주는 영향이에요.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        ConcernIngredientCard(ingredients = concernIngredients, description = concernDescription)

        if (menuContributions.isNotEmpty()) {
            Column {
                Text(
                    text = "메뉴별 피부 기여도",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    menuContributions.forEach { MenuContributionCard(it) }
                }
            }
        }

        DeficientNutrientCard(deficiency = deficiency)

        WhyThisResultCard(explanations = skinConcernExplanations)
    }
}

@Composable
private fun ConcernIngredientCard(ingredients: List<ConcernIngredient>, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = WarningTagText)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "피부 트러블 주의 성분",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredients.forEach { ingredient ->
                    LevelTag(text = ingredient.label, level = ingredient.level)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "나트륨 데이터는 아직 준비되지 않아 이 분석에는 포함되지 않았어요.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LevelTag(text: String, level: SkinImpactLevel) {
    Surface(shape = RoundedCornerShape(50), color = level.backgroundColor) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = level.textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MenuContributionCard(item: MenuSkinContribution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.score}점",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = item.level.textColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LevelTag(text = item.statusLabel, level = item.level)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun DeficientNutrientCard(deficiency: SkinScoreCalculator.DeficiencySummary) {
    val deficientNames = buildList {
        if (deficiency.omega3Deficient) add("오메가3")
        if (deficiency.vitCDeficient) add("비타민C")
        if (deficiency.vitEDeficient) add("비타민E")
        if (deficiency.zincDeficient) add("아연")
    }
    val hasDeficiency = deficientNames.isNotEmpty()
    val backgroundColor = if (hasDeficiency) WarningTagBackground else PositiveTagBackground
    val titleColor = if (hasDeficiency) WarningTagText else PositiveTagText
    val message = if (hasDeficiency) {
        "최근 7일간 ${deficientNames.joinToString(", ")} 섭취가 권장 수준보다 부족해요. 콜라겐 합성과 장벽 복구를 위해 이 성분이 담긴 식재료를 추천해요!"
    } else {
        "최근 7일간 주요 피부 영양소를 고르게 섭취하고 있어요. 지금 흐름을 유지해보세요!"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "부족한 피부 유효 성분",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
    }
}

@Composable
private fun WhyThisResultCard(explanations: List<SkinConcernExplanation>) {
    var expanded by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "왜 이런 결과가 나왔나요?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "접기" else "펼치기",
                    tint = TextSecondary
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    explanations.forEach { item ->
                        Column {
                            Text(
                                text = item.nutrient,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
