package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.model.SkinLogEntry
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CautionTagText
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private enum class ReportStage { SELECT, LOADING, RESULT }
private enum class ReportCalendarType(val label: String) { SKIN("피부달력"), FOOD("음식달력") }
private enum class ReportPeriodPreset { LAST_7, LAST_30, CUSTOM }

@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    skinLogViewModel: SkinLogViewModel = viewModel(),
    mealLogViewModel: MealLogViewModel = viewModel()
) {
    var stage by remember { mutableStateOf(ReportStage.SELECT) }
    var calendarType by remember { mutableStateOf(ReportCalendarType.SKIN) }
    var preset by remember { mutableStateOf(ReportPeriodPreset.LAST_7) }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    var customStart by remember { mutableStateOf<LocalDate?>(null) }
    var customEnd by remember { mutableStateOf<LocalDate?>(null) }

    val today = remember { LocalDate.now() }
    val skinEntries by skinLogViewModel.entries.collectAsState()
    val mealEntries by mealLogViewModel.entries.collectAsState()

    val rangeStart: LocalDate
    val rangeEnd: LocalDate
    when (preset) {
        ReportPeriodPreset.LAST_7 -> { rangeStart = today.minusDays(6); rangeEnd = today }
        ReportPeriodPreset.LAST_30 -> { rangeStart = today.minusDays(29); rangeEnd = today }
        ReportPeriodPreset.CUSTOM -> {
            val start = customStart ?: today
            val end = customEnd ?: start
            rangeStart = if (start.isAfter(end)) end else start
            rangeEnd = if (start.isAfter(end)) start else end
        }
    }

    fun onDateTap(date: LocalDate) {
        if (preset != ReportPeriodPreset.CUSTOM || (customStart != null && customEnd != null)) {
            preset = ReportPeriodPreset.CUSTOM
            customStart = date
            customEnd = null
        } else if (customStart == null) {
            customStart = date
        } else {
            customEnd = date
        }
    }

    LaunchedEffect(stage) {
        if (stage == ReportStage.LOADING) {
            delay(1200)
            stage = ReportStage.RESULT
        }
    }

    when (stage) {
        ReportStage.SELECT -> ReportCalendarSelectContent(
            modifier = modifier,
            calendarType = calendarType,
            onCalendarTypeChange = { calendarType = it },
            displayMonth = displayMonth,
            onMonthChange = { displayMonth = it },
            preset = preset,
            onPresetSelected = { selected ->
                preset = selected
                if (selected != ReportPeriodPreset.CUSTOM) {
                    customStart = null
                    customEnd = null
                }
            },
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            onDateTap = ::onDateTap,
            skinEntries = skinEntries,
            mealLogViewModel = mealLogViewModel,
            mealEntriesVersion = mealEntries.size,
            onGenerateClick = { stage = ReportStage.LOADING }
        )

        ReportStage.LOADING -> ReportLoadingContent(modifier = modifier)

        ReportStage.RESULT -> GeneratedReportContent(
            modifier = modifier,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            skinLogViewModel = skinLogViewModel,
            onBackToSelect = { stage = ReportStage.SELECT }
        )
    }
}

@Composable
private fun ReportCalendarSelectContent(
    modifier: Modifier = Modifier,
    calendarType: ReportCalendarType,
    onCalendarTypeChange: (ReportCalendarType) -> Unit,
    displayMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    preset: ReportPeriodPreset,
    onPresetSelected: (ReportPeriodPreset) -> Unit,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    onDateTap: (LocalDate) -> Unit,
    skinEntries: List<SkinLogEntry>,
    mealLogViewModel: MealLogViewModel,
    mealEntriesVersion: Int,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(text = "리포트 만들기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "확인하고 싶은 기간과 종류를 선택해주세요", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(SurfaceMuted)
                .padding(4.dp)
        ) {
            ReportCalendarType.entries.forEach { type ->
                val selected = type == calendarType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) CardWhite else Color.Transparent)
                        .clickable { onCalendarTypeChange(type) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) CoralPrimary else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onMonthChange(displayMonth.minusMonths(1)) }) {
                        Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "이전 달", tint = TextSecondary)
                    }
                    Text(
                        text = "${displayMonth.year}년 ${displayMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onMonthChange(displayMonth.plusMonths(1)) }) {
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "다음 달", tint = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                val firstOfMonth = displayMonth.atDay(1)
                val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // Sunday(7)->0, Monday(1)->1, ...
                val daysInMonth = displayMonth.lengthOfMonth()
                val totalCells = leadingBlanks + daysInMonth
                val rowCount = (totalCells + 6) / 7

                for (row in 0 until rowCount) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - leadingBlanks + 1
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (dayNumber in 1..daysInMonth) {
                                    val date = displayMonth.atDay(dayNumber)
                                    val dotColor = if (calendarType == ReportCalendarType.SKIN) {
                                        skinDotColor(date, skinEntries)
                                    } else {
                                        foodDotColor(date, mealLogViewModel)
                                    }
                                    CalendarDayCell(
                                        date = date,
                                        isToday = date == LocalDate.now(),
                                        inRange = !date.isBefore(rangeStart) && !date.isAfter(rangeEnd),
                                        dotColor = dotColor,
                                        onClick = { onDateTap(date) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodChip(text = "최근 7일", selected = preset == ReportPeriodPreset.LAST_7, modifier = Modifier.weight(1f), onClick = { onPresetSelected(ReportPeriodPreset.LAST_7) })
            PeriodChip(text = "최근 30일", selected = preset == ReportPeriodPreset.LAST_30, modifier = Modifier.weight(1f), onClick = { onPresetSelected(ReportPeriodPreset.LAST_30) })
            PeriodChip(text = "직접 선택", selected = preset == ReportPeriodPreset.CUSTOM, modifier = Modifier.weight(1f), onClick = { onPresetSelected(ReportPeriodPreset.CUSTOM) })
        }

        if (preset == ReportPeriodPreset.CUSTOM) {
            Text(text = "캘린더에서 시작일과 종료일을 눌러 선택해주세요", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        Text(
            text = "선택 기간: ${rangeStart.monthValue}.${rangeStart.dayOfMonth} ~ ${rangeEnd.monthValue}.${rangeEnd.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
        ) {
            Text(text = "리포트 생성하기", fontWeight = FontWeight.Bold)
        }
    }
}

private fun skinDotColor(date: LocalDate, entries: List<SkinLogEntry>): Color? {
    val dayEntries = entries.filter { it.loggedAt.toLocalDate() == date }
    if (dayEntries.isEmpty()) return null
    val average = dayEntries.flatMap { listOf(it.troubleLevel, it.oilLevel, it.drynessLevel) }.average()
    return when {
        average <= 2.0 -> PositiveTagText
        average <= 3.5 -> CautionTagText
        else -> WarningTagText
    }
}

private fun foodDotColor(date: LocalDate, mealLogViewModel: MealLogViewModel): Color? {
    val dayEntries = mealLogViewModel.entriesForDate(date)
    if (dayEntries.isEmpty()) return null
    val score = SkinScoreCalculator.scoreForEntries(dayEntries)
    return when {
        score >= 75 -> PositiveTagText
        score >= 55 -> CautionTagText
        else -> WarningTagText
    }
}

@Composable
private fun CalendarDayCell(date: LocalDate, isToday: Boolean, inRange: Boolean, dotColor: Color?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = if (inRange) CoralPrimary.copy(alpha = 0.15f) else Color.Transparent,
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "${date.dayOfMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) CoralPrimary else TextPrimary,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(color = dotColor ?: Color.Transparent, shape = CircleShape)
        )
    }
}

@Composable
private fun PeriodChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) CoralPrimary else SurfaceMuted
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun ReportLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = CoralPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "리포트를 생성하고 있어요...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

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
private fun GeneratedReportContent(
    modifier: Modifier = Modifier,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    skinLogViewModel: SkinLogViewModel,
    onBackToSelect: () -> Unit
) {
    val entries by skinLogViewModel.entries.collectAsState()
    val rangedEntries = remember(entries, rangeStart, rangeEnd) {
        entries.filter { !it.loggedAt.toLocalDate().isBefore(rangeStart) && !it.loggedAt.toLocalDate().isAfter(rangeEnd) }
            .sortedBy { it.loggedAt }
    }
    val first = rangedEntries.firstOrNull()
    val last = rangedEntries.lastOrNull()
    val hasEnoughHistory = rangedEntries.size >= 2 && first != null && last != null

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBackToSelect),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = null, tint = CoralPrimary)
            Text(text = "기간 다시 선택", style = MaterialTheme.typography.bodyMedium, color = CoralPrimary, fontWeight = FontWeight.Bold)
        }

        Column {
            Text(
                text = "${rangeStart.monthValue}.${rangeStart.dayOfMonth} ~ ${rangeEnd.monthValue}.${rangeEnd.dayOfMonth} 피부 변화",
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
