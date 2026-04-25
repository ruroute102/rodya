package ru.techgid.presentation.screen.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.presentation.components.PrimaryButton
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (state.isLoginMode) "Вход в ТехГид" else "Регистрация",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (state.isLoginMode) {
                "Войдите, чтобы оставлять комментарии\nи сохранять инструкции"
            } else {
                "Создайте аккаунт по номеру телефона"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TechGidTheme.extendedColors.textTertiary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.phone,
            onValueChange = { if (it.length <= 12) viewModel.updatePhone(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Телефон") },
            placeholder = { Text("+79001234567") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
            ),
            isError = state.phone.isNotEmpty() && state.phone.length < 12,
            supportingText = if (state.phone.isNotEmpty() && state.phone.length < 12) {
                { Text("Введите полный номер (+7XXXXXXXXXX)") }
            } else null,
        )

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(visible = !state.isLoginMode && state.showOtpField) {
            Column {
                OutlinedTextField(
                    value = state.otpCode,
                    onValueChange = { if (it.length <= 6) viewModel.updateOtpCode(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Код из SMS") },
                    placeholder = { Text("000000") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        AnimatedVisibility(visible = !state.isLoginMode) {
            Column {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = { viewModel.updateDisplayName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя") },
                    placeholder = { Text("Как вас зовут?") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.updatePassword(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            placeholder = { Text("Минимум 8 символов") },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Скрыть" else "Показать",
                        tint = TechGidTheme.extendedColors.textTertiary,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
            ),
            isError = state.password.isNotEmpty() && state.password.length < 8,
            supportingText = if (state.password.isNotEmpty() && state.password.length < 8) {
                { Text("Минимум 8 символов") }
            } else null,
        )

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.isLoginMode) {
            PrimaryButton(
                text = "Войти",
                onClick = { viewModel.login() },
                enabled = state.phone.length >= 12 && state.password.length >= 8,
            )
        } else {
            if (!state.showOtpField) {
                PrimaryButton(
                    text = "Получить код",
                    onClick = { viewModel.requestOtp() },
                    enabled = state.phone.length >= 12,
                )
            } else {
                PrimaryButton(
                    text = "Зарегистрироваться",
                    onClick = { viewModel.register() },
                    enabled = state.phone.length >= 12 && state.otpCode.length == 6
                            && state.displayName.length >= 2 && state.password.length >= 8,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.toggleMode() }) {
            Text(
                text = if (state.isLoginMode) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
