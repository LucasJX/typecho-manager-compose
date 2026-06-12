package com.flypigs.typechomanager.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import com.flypigs.typechomanager.ui.designsystem.DesignSystem

// ── Brand Colors ──────────────────────────────────────
private val GradientStart = Color(0xFF5B6EFF)
private val GradientEnd = Color(0xFF7C8CFF)

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    onNavigateToMain: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate on successful connection
    LaunchedEffect(uiState.connected) {
        if (uiState.connected) {
            onNavigateToMain()
        }
    }

    // Show error as Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            // ════════════════════════════════════════════
            //  Logo Hero Section — 260dp gradient banner
            // ════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Book emoji as brand icon
                    Text(
                        text = "\uD83D\uDCD6",
                        fontSize = 48.sp,
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.XXLarge))

                    // Brand name
                    Text(
                        text = "Blogga",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.XXLarge))

                    // Subtitle
                    Text(
                        text = "Your Mobile Blogging Workspace",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            // ════════════════════════════════════════════
            //  Login Form Section
            // ════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignSystem.Spacing.Large)
                    .padding(top = DesignSystem.Spacing.ExtraLarge, bottom = DesignSystem.Spacing.XXLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Section title
                Text(
                    text = "连接你的博客",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DesignSystem.Spacing.Large),
                )

                // ── Endpoint ──────────────────────────
                BlogTextField(
                    value = uiState.endpoint,
                    onValueChange = { viewModel.updateField(endpoint = it) },
                    label = "博客地址",
                    placeholder = "https://yourblog.com",
                    icon = Icons.Default.Public,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                // ── Username ──────────────────────────
                BlogTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.updateField(username = it) },
                    label = "用户名",
                    placeholder = "admin",
                    icon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                // ── Password ──────────────────────────
                BlogTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updateField(password = it) },
                    label = "密码",
                    placeholder = "••••••••",
                    icon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    onDone = { viewModel.connect() },
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))

                // ── Connect Button ────────────────────
                Button(
                    onClick = { viewModel.connect() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = DesignSystem.Corner.Button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GradientStart,
                        contentColor = Color.White,
                    ),
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "连接博客",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // ── Error ─────────────────────────────
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = DesignSystem.Spacing.Medium),
                    )
                }
            }
        }
    }
}

/**
 * Reusable styled text field for the login form.
 * Material3 OutlinedTextField with leading icon, 56dp height, 16dp corners.
 */
@Composable
private fun BlogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    onDone: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        singleLine = true,
        shape = DesignSystem.Corner.Input,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() },
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    )
}
