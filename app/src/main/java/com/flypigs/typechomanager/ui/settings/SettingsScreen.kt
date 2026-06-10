package com.flypigs.typechomanager.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToSetup: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var themeDropdownExpanded by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗？本地配置将被清除。") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout { onNavigateToSetup() }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("设置") },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // -- Blog Info Section --
            item {
                Text(
                    text = "博客信息",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(uiState.blogName.ifEmpty { "未命名博客" }) },
                    supportingContent = { Text(uiState.endpoint.ifEmpty { "未配置" }) },
                    leadingContent = {
                        Icon(Icons.Default.Web, contentDescription = null)
                    },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // -- Account Section --
            item {
                Text(
                    text = "账户",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(uiState.username.ifEmpty { "未登录" }) },
                    leadingContent = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("退出登录") },
                    leadingContent = {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 0.dp,
                )
                // Wrap the whole ListItem in a clickable via the trailing icon approach
                // Actually, use a TextButton for clarity
                Box(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                    TextButton(onClick = { showLogoutDialog = true }) {
                        Text("退出登录", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // -- Appearance Section --
            item {
                Text(
                    text = "外观",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("主题模式") },
                    supportingContent = {
                        Text(
                            when (uiState.themeMode) {
                                ThemeMode.SYSTEM -> "跟随系统"
                                ThemeMode.LIGHT -> "浅色"
                                ThemeMode.DARK -> "深色"
                            }
                        )
                    },
                    trailingContent = {
                        ExposedDropdownMenuBox(
                            expanded = themeDropdownExpanded,
                            onExpandedChange = { themeDropdownExpanded = it },
                        ) {
                            TextField(
                                value = when (uiState.themeMode) {
                                    ThemeMode.SYSTEM -> "跟随系统"
                                    ThemeMode.LIGHT -> "浅色"
                                    ThemeMode.DARK -> "深色"
                                },
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeDropdownExpanded)
                                },
                                singleLine = true,
                            )
                            ExposedDropdownMenu(
                                expanded = themeDropdownExpanded,
                                onDismissRequest = { themeDropdownExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("跟随系统") },
                                    onClick = {
                                        viewModel.saveThemeMode(ThemeMode.SYSTEM)
                                        themeDropdownExpanded = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("浅色") },
                                    onClick = {
                                        viewModel.saveThemeMode(ThemeMode.LIGHT)
                                        themeDropdownExpanded = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("深色") },
                                    onClick = {
                                        viewModel.saveThemeMode(ThemeMode.DARK)
                                        themeDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    },
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("深色模式") },
                    trailingContent = {
                        Switch(
                            checked = uiState.isDark,
                            onCheckedChange = { viewModel.toggleDark() },
                        )
                    },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // -- About Section --
            item {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("版本") },
                    supportingContent = { Text("1.0.0") },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                )
            }
        }
    }
}
