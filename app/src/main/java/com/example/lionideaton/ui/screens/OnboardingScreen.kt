package com.example.lionideaton.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.lionideaton.data.model.SkinConcern
import com.example.lionideaton.data.model.SkinType
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PhotoPromptAccent
import com.example.lionideaton.ui.theme.PhotoPromptBackground
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onCompleted: () -> Unit = {}
) {
    var selectedSkinType by remember { mutableStateOf<SkinType?>(null) }
    var selectedConcerns by remember { mutableStateOf(setOf<SkinConcern>()) }
    var allergyInput by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf(listOf<String>()) }
    var dislikeInput by remember { mutableStateOf("") }
    var dislikes by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "Skin Basket에 오신 걸 환영해요",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "몇 가지만 알려주시면 맞춤 분석과 추천을 시작할게요.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        OnboardingCard(title = "피부타입을 선택해주세요") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkinType.entries.forEach { type ->
                    SelectableChip(
                        text = type.label,
                        selected = selectedSkinType == type,
                        onClick = { selectedSkinType = type }
                    )
                }
            }
        }

        OnboardingCard(title = "피부 고민을 선택해주세요 (여러 개 가능)") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkinConcern.entries.forEach { concern ->
                    SelectableChip(
                        text = concern.label,
                        selected = selectedConcerns.contains(concern),
                        onClick = {
                            selectedConcerns = if (selectedConcerns.contains(concern)) {
                                selectedConcerns - concern
                            } else {
                                selectedConcerns + concern
                            }
                        }
                    )
                }
            }
        }

        ChipInputCard(
            title = "알레르기가 있는 재료",
            placeholder = "예: 밀가루",
            inputValue = allergyInput,
            onInputChange = { allergyInput = it },
            items = allergies,
            onAdd = {
                if (allergyInput.isNotBlank()) {
                    allergies = allergies + allergyInput.trim()
                    allergyInput = ""
                }
            },
            onRemove = { name -> allergies = allergies - name }
        )

        ChipInputCard(
            title = "못 먹거나 선호하지 않는 음식",
            placeholder = "예: 오이",
            inputValue = dislikeInput,
            onInputChange = { dislikeInput = it },
            items = dislikes,
            onAdd = {
                if (dislikeInput.isNotBlank()) {
                    dislikes = dislikes + dislikeInput.trim()
                    dislikeInput = ""
                }
            },
            onRemove = { name -> dislikes = dislikes - name }
        )

        Button(
            onClick = {
                val skinType = selectedSkinType ?: return@Button
                userProfileViewModel.completeOnboarding(
                    skinType = skinType,
                    concerns = selectedConcerns.toList(),
                    allergies = allergies,
                    dislikes = dislikes
                )
                onCompleted()
            },
            enabled = selectedSkinType != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
        ) {
            Text(text = "시작하기", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OnboardingCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SelectableChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun ChipInputCard(
    title: String,
    placeholder: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    items: List<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    OnboardingCard(title = title) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = placeholder) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onAdd) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "추가", tint = CoralPrimary)
            }
        }
        if (items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item -> RemovableChip(text = item, onRemove = { onRemove(item) }) }
            }
        }
    }
}

@Composable
private fun RemovableChip(text: String, onRemove: () -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = PhotoPromptBackground) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = PhotoPromptAccent, fontWeight = FontWeight.Medium)
            IconButton(onClick = onRemove, modifier = Modifier.size(20.dp).padding(start = 2.dp)) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "삭제", tint = PhotoPromptAccent, modifier = Modifier.size(14.dp))
            }
        }
    }
}
