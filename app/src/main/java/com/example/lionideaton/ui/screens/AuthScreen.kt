package com.example.lionideaton.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagText

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onAuthenticated: () -> Unit = {}
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun switchMode(signUp: Boolean) {
        isSignUpMode = signUp
        errorMessage = null
    }

    fun submit() {
        if (isSignUpMode) {
            when {
                nickname.isBlank() || email.isBlank() || password.isBlank() -> errorMessage = "모든 항목을 입력해주세요"
                password != passwordConfirm -> errorMessage = "비밀번호가 일치하지 않아요"
                else -> {
                    val success = userProfileViewModel.signUp(email.trim(), password, nickname.trim())
                    if (success) onAuthenticated() else errorMessage = "이미 가입된 이메일이에요"
                }
            }
        } else {
            when {
                email.isBlank() || password.isBlank() -> errorMessage = "이메일과 비밀번호를 입력해주세요"
                else -> {
                    val success = userProfileViewModel.login(email.trim(), password)
                    if (success) onAuthenticated() else errorMessage = "이메일 또는 비밀번호가 올바르지 않아요"
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "Skin Basket",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "먹은 만큼 예뻐지는 맞춤 피부 케어를 시작해보세요.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Text(
            text = if (isSignUpMode) "회원가입" else "로그인",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "닉네임") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "이메일") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "비밀번호") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp)
                )
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "비밀번호 확인") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                errorMessage?.let { message ->
                    Text(text = message, style = MaterialTheme.typography.bodySmall, color = WarningTagText)
                }
                Button(
                    onClick = ::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
                ) {
                    Text(text = if (isSignUpMode) "회원가입" else "로그인", fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUpMode) "이미 계정이 있으신가요? " else "계정이 없으신가요? ",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = if (isSignUpMode) "로그인" else "회원가입",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = CoralPrimary,
                modifier = Modifier.clickable { switchMode(!isSignUpMode) }
            )
        }
    }
}
