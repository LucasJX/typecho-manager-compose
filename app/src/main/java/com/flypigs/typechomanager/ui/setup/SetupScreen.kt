package com.flypigs.typechomanager.ui.setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import com.flypigs.typechomanager.ui.designsystem.DesignSystem

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

    // Shake animation state
    val shakeOffset = remember { Animatable(0f) }

    // Trigger shake on error
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-15f) at 50
                    15f at 100
                    (-10f) at 150
                    10f at 200
                    (-5f) at 250
                    5f at 300
                    0f at 400
                }
            )
        }
    }

    // Entrance animations
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }
    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        // Logo fade-in + scale-up
        logoAlpha.animateTo(1f, animationSpec = tween(500))
        logoScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            )
        )
        // Card fade-in + slide-up (staggered)
        cardAlpha.animateTo(1f, animationSpec = tween(400))
        cardOffsetY.animateTo(
            0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DesignSystem.Spacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // ════════════════════════════════════════════
                //  Brand — Icon + Brand Name + Tagline
                // ════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .graphicsLayer {
                            alpha = logoAlpha.value
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    DesignSystem.BrandColors.Primary,
                                    DesignSystem.BrandColors.Secondary,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Blogga Logo",
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                Text(
                    text = "Blogga",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = DesignSystem.Typography.Display,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.graphicsLayer { alpha = logoAlpha.value },
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.ExtraSmall))

                Text(
                    text = "移动博客工作台",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.graphicsLayer { alpha = logoAlpha.value },
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.ExtraLarge))

                // ════════════════════════════════════════════
                //  Login Form Card
                // ════════════════════════════════════════════
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeOffset.value.dp)
                        .graphicsLayer {
                            alpha = cardAlpha.value
                            translationY = cardOffsetY.value
                        },
                    shape = DesignSystem.Corner.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = DesignSystem.Elevation.Card,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.Large),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                    ) {
                        // Section title
                        Text(
                            text = "连接你的博客",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Text(
                            text = "输入 Typecho 博客地址和登录凭据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.ExtraSmall))

                        // ── Endpoint ──────────────────────────
                        BlogTextField(
                            value = uiState.endpoint,
                            onValueChange = { viewModel.updateField(endpoint = it) },
                            label = "博客地址",
                            placeholder = "https://yourblog.com",
                            icon = Icons.Default.Public,
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next,
                            isError = uiState.error != null,
                        )

                        // ── Username ──────────────────────────
                        BlogTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.updateField(username = it) },
                            label = "用户名",
                            placeholder = "admin",
                            icon = Icons.Default.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            isError = uiState.error != null,
                        )

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
                            isError = uiState.error != null,
                        )

                        // ── Error Text ─────────────────────────
                        if (uiState.error != null) {
                            Text(
                                text = uiState.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))

                        // ── Connect Button ────────────────────
                        FilledTonalButton(
                            onClick = { viewModel.connect() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = DesignSystem.Corner.Button,
                            enabled = !uiState.isLoading,
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            } else {
                                Text(
                                    text = "连接博客",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
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
    isError: Boolean = false,
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
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        },
        singleLine = true,
        shape = DesignSystem.Corner.Input,
        isError = isError,
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
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLeadingIconColor = MaterialTheme.colorScheme.error,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    )
}
