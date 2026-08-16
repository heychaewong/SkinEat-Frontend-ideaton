package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText

private data class SkinChangeStat(val label: String, val value: String, val statusLabel: String, val isPositive: Boolean)

private val mockSkinChangeStats = listOf(
    SkinChangeStat("트러블 빈도", "-24%", "감소", isPositive = true),
    SkinChangeStat("유분 지수", "-12%", "조절됨", isPositive = true),
    SkinChangeStat("건조함", "+8%", "건조", isPositive = false)
)

private fun deltaStat(label: String, firstVal: Int, lastVal: Int): SkinChangeStat {
    val diff = lastVal - firstVal
    val isPositive = diff <= 0
    val statusLabel = when {
        label == "건조함" && !isPositive -> "건조"
        label == "건조함" -> "개선"
        label == "유분 지수" && !isPositive -> "증가"
        label == "유분 지수" -> "조절됨"
        !isPositive -> "증가"
        else -> "감소"
    }
    val sign = if (diff > 0) "+" else ""
    return SkinChangeStat(label, "$sign$diff", statusLabel, isPositive)
}

private data class FoodImpact(val emoji: String, val foodName: String, val restOfSentence: String, val isPositive: Boolean)

private val foodImpacts = listOf(
    FoodImpact("😊", "연어 구이", "를 먹은 다음 날 유독 유분 밸런스가 최고치였어요.", isPositive = true),
    FoodImpact("🙁", "마라탕", " 섭취 시 24시간 이내 볼 주변 붉은 기가 발현되었어요.", isPositive = false)
)

private val practiceTrend = listOf(30f, 55f, 48f, 62f, 90f)

@Composable
fun ReportScreen(modifier: Modifier = Modifier, skinLogViewModel: SkinLogViewModel = viewModel()) {
    val entries by skinLogViewModel.entries.collectAsState()
    val sortedEntries = remember(entries) { entries.sortedBy { it.loggedAt } }
    val first = sortedEntries.firstOrNull()
    val last = sortedEntries.lastOrNull()
    val hasEnoughHistory = sortedEntries.size >= 2 && first != null && last != null

    val skinChangeStats = if (hasEnoughHistory && first != null && last != null) {
        listOf(
            deltaStat("트러블 빈도", first.troubleLevel, last.troubleLevel),
            deltaStat("유분 지수", first.oilLevel, last.oilLevel),
            deltaStat("건조함", first.drynessLevel, last.drynessLevel)
        )
    } else {
        mockSkinChangeStats
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
                text = "2주간의 피부 변화",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            BeforeAfterComparison(beforePhoto = first?.photo, afterPhoto = last?.photo)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            skinChangeStats.forEach { stat ->
                SkinChangeStatCard(stat = stat, modifier = Modifier.weight(1f))
            }
        }

        PracticeTrendCard(values = practiceTrend)

        Column {
            Text(
                text = "내 피부에 가장 강력한 음식",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                foodImpacts.forEach { FoodImpactCard(it) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "패턴 기반 추정이며, 의학적 진단이 아니에요.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun BeforeAfterComparison(beforePhoto: Bitmap?, afterPhoto: Bitmap?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            BeforeAfterHalf(
                label = "Before",
                photo = beforePhoto,
                placeholderBackground = SurfaceMuted,
                badgeColor = Color(0xFF5C5850),
                badgeAlignment = Alignment.TopStart,
                modifier = Modifier.weight(1f)
            )
            BeforeAfterHalf(
                label = "After",
                photo = afterPhoto,
                placeholderBackground = Color(0xFFF5D9C8),
                badgeColor = CoralPrimary,
                badgeAlignment = Alignment.TopEnd,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BeforeAfterHalf(
    label: String,
    photo: Bitmap?,
    placeholderBackground: Color,
    badgeColor: Color,
    badgeAlignment: Alignment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = badgeAlignment
    ) {
        if (photo != null) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder until a real 피부기록 photo exists for this slot.
            Box(modifier = Modifier.fillMaxSize().background(placeholderBackground))
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeColor,
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SkinChangeStatCard(stat: SkinChangeStat, modifier: Modifier = Modifier) {
    val tagBackground = if (stat.isPositive) PositiveTagBackground else WarningTagBackground
    val tagText = if (stat.isPositive) PositiveTagText else WarningTagText
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stat.label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(50), color = tagBackground) {
                Text(
                    text = stat.statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = tagText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PracticeTrendCard(values: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "피부 개선 식습관 실천 추이",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            TrendLineChart(values = values, modifier = Modifier.fillMaxWidth().height(140.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "2주 전", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(text = "1주 전", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    text = "오늘",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TrendLineChart(values: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val minVal = values.min()
        val maxVal = values.max()
        val range = (maxVal - minVal).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (values.size - 1)
        val verticalPadding = 8.dp.toPx()
        val points = values.mapIndexed { index, value ->
            val normalized = (value - minVal) / range
            Offset(
                x = index * stepX,
                y = size.height - verticalPadding - normalized * (size.height - 2 * verticalPadding)
            )
        }

        // Baseline
        drawLine(
            color = SurfaceMuted,
            start = Offset(0f, size.height - verticalPadding),
            end = Offset(size.width, size.height - verticalPadding),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = path,
            color = CoralPrimary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        points.forEach { point ->
            drawCircle(color = CoralPrimary, radius = 4.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun FoodImpactCard(impact: FoodImpact) {
    val background = if (impact.isPositive) PositiveTagBackground else WarningTagBackground
    val textColor = if (impact.isPositive) PositiveTagText else WarningTagText
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = background
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Text(text = impact.emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(impact.foodName) }
                    append(impact.restOfSentence)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}
