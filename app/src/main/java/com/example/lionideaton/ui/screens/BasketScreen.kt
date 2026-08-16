package com.example.lionideaton.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.IngredientSeedData
import com.example.lionideaton.data.model.Ingredient
import com.example.lionideaton.data.model.PriceBand
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PhotoPromptAccent
import com.example.lionideaton.ui.theme.PhotoPromptBackground
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary

private data class RecommendationGroup(val keyNutrient: String, val options: List<Ingredient>)

private fun optionsFor(keyNutrient: String): List<Ingredient> =
    listOfNotNull(IngredientSeedData.primaryFor(keyNutrient)) + IngredientSeedData.subsFor(keyNutrient)

private fun nutrientLabel(keyNutrient: String): String = when (keyNutrient) {
    IngredientSeedData.KEY_OMEGA3 -> "오메가3"
    IngredientSeedData.KEY_VIT_C -> "비타민C"
    IngredientSeedData.KEY_VIT_E -> "비타민E"
    IngredientSeedData.KEY_ZINC -> "아연"
    else -> keyNutrient
}

private fun iconFor(keyNutrient: String): ImageVector = when (keyNutrient) {
    IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_ZINC -> Icons.Filled.SetMeal
    else -> Icons.Filled.Eco
}

// Placeholder pricing (KRW) derived from price_band until this is sourced from a real product API.
private fun estimatedPrice(priceBand: PriceBand?): Int = when (priceBand) {
    PriceBand.LOW -> 3000
    PriceBand.MID -> 6000
    PriceBand.HIGH -> 12000
    null -> 5000
}

private data class RecommendedRecipe(val name: String, val cookingTime: String, val barrierScore: String)

private val recommendedRecipe = RecommendedRecipe("연어 브로콜리 지중해식 포케", "15분", "94%")

@Composable
fun BasketScreen(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel,
    mealLogViewModel: MealLogViewModel = viewModel(),
    onRecipeClick: () -> Unit = {}
) {
    val allEntries by mealLogViewModel.entries.collectAsState()
    val recentEntries = remember(allEntries) { mealLogViewModel.recentEntries() }
    val deficiency = remember(recentEntries) { SkinScoreCalculator.analyzeDeficiency(recentEntries) }

    val deficientKeys = remember(deficiency) {
        buildList {
            if (deficiency.omega3Deficient) add(IngredientSeedData.KEY_OMEGA3)
            if (deficiency.vitCDeficient) add(IngredientSeedData.KEY_VIT_C)
            if (deficiency.vitEDeficient) add(IngredientSeedData.KEY_VIT_E)
            if (deficiency.zincDeficient) add(IngredientSeedData.KEY_ZINC)
        }
    }
    // Nothing deficient? Fall back to all 4 axes so the screen still has something to show.
    val activeKeys = deficientKeys.ifEmpty {
        listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_C, IngredientSeedData.KEY_VIT_E, IngredientSeedData.KEY_ZINC)
    }
    val groups = remember(activeKeys) { activeKeys.map { key -> RecommendationGroup(key, optionsFor(key)) } }
    var groupIndices by remember(groups) { mutableStateOf(List(groups.size) { 0 }) }

    val cartItems by cartViewModel.items.collectAsState()

    val subtitle = if (deficientKeys.isNotEmpty()) {
        "최근 7일간 부족한 영양을 채우기 위해 골라본 식재료예요."
    } else {
        "최근 7일간 식습관이 균형 잡혀 있어요. 꾸준히 챙기면 좋은 식재료예요."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "내일의 장바구니 리스트",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        groups.forEachIndexed { index, group ->
            val ingredient = group.options[groupIndices[index]]
            val isDeficient = deficientKeys.contains(group.keyNutrient)
            IngredientRecommendationCard(
                ingredient = ingredient,
                keyNutrient = group.keyNutrient,
                isDeficient = isDeficient,
                isInBasket = cartItems.any { it.name == ingredient.name },
                onShowAlternative = {
                    groupIndices = groupIndices.toMutableList().apply {
                        this[index] = (this[index] + 1) % group.options.size
                    }
                },
                onAddToBasket = {
                    cartViewModel.addItem(ingredient.name, estimatedPrice(ingredient.priceBand), iconFor(group.keyNutrient))
                }
            )
        }

        RecommendedRecipeSection(recipe = recommendedRecipe, onClick = onRecipeClick)
    }
}

@Composable
private fun IngredientRecommendationCard(
    ingredient: Ingredient,
    keyNutrient: String,
    isDeficient: Boolean,
    isInBasket: Boolean,
    onShowAlternative: () -> Unit,
    onAddToBasket: () -> Unit
) {
    val reasonText = if (isDeficient) {
        "최근 7일간 ${nutrientLabel(keyNutrient)} 섭취가 부족해서 추천돼요"
    } else {
        "${nutrientLabel(keyNutrient)} 보충에 꾸준히 도움이 되는 재료예요"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            IngredientHeroImage(icon = iconFor(keyNutrient))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                NutrientBadge(text = nutrientLabel(keyNutrient))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = reasonText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            ingredient.appealNote?.let { note ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = note, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShowAlternative,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(text = "다른 재료 보기")
                }
                Button(
                    onClick = onAddToBasket,
                    enabled = !isInBasket,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
                ) {
                    Text(text = if (isInBasket) "담았어요" else "장바구니 담기", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun IngredientHeroImage(icon: ImageVector) {
    // Placeholder illustration until real ingredient photos/an image pipeline are wired up.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceMuted
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun NutrientBadge(text: String) {
    Surface(shape = RoundedCornerShape(50), color = PhotoPromptBackground) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = PhotoPromptAccent,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RecommendedRecipeSection(recipe: RecommendedRecipe, onClick: () -> Unit) {
    Column {
        Text(
            text = "추천 피부 구원 레시피",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = PositiveTagBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.SetMeal,
                            contentDescription = null,
                            tint = PositiveTagText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "조리시간 ${recipe.cookingTime} | 장벽 강화도 ${recipe.barrierScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}


