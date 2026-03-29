package ru.techgid.presentation.screen.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.components.PrimaryButton
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Экран авторизации: вход / регистрация.
 * Регистрация по телефону → OTP → имя + пароль.
 * Вход по телефону + пароль.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var phone by remember { mutableStateOf("+7") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Заголовок
        Text(
            text = if (isLoginMode) "Вход в ТехГид" else "Регистрация",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isLoginMode) {
                "Войдите, чтобы оставлять комментарии\nи сохранять инструкции"
            } else {
                "Создайте аккаунт по номеру телефона"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Телефон
        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 12) phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Телефон") },
            placeholder = { Text("+79001234567") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // OTP-код (при регистрации)
        AnimatedVisibility(visible = !isLoginMode && showOtpField) {
            Column {
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Код из SMS") },
                    placeholder = { Text("000000") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Имя (при регистрации)
        AnimatedVisibility(visible = !isLoginMode) {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя") },
                    placeholder = { Text("Как вас зовут?") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Пароль
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            placeholder = { Text("Минимум 8 символов") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
        )

        // Ошибка
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка действия
        if (isLoginMode) {
            PrimaryButton(
                text = "Войти",
                onClick = {
                    // TODO: вызов authRepository.login()
                    onAuthSuccess()
                },
                enabled = phone.length >= 12 && password.length >= 8,
            )
        } else {
            if (!showOtpField) {
                PrimaryButton(
                    text = "Получить код",
                    onClick = {
                        // TODO: вызов authRepository.requestOtp()
                        showOtpField = true
                    },
                    enabled = phone.length >= 12,
                )
            } else {
                PrimaryButton(
                    text = "Зарегистрироваться",
                    onClick = {
                        // TODO: вызов authRepository.register()
                        onAuthSuccess()
                    },
                    enabled = phone.length >= 12 && otpCode.length == 6
                            && displayName.length >= 2 && password.length >= 8,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Переключение режима
        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                errorMessage = null
                showOtpField = false
                otpCode = ""
            },
        ) {
            Text(
                text = if (isLoginMode) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
