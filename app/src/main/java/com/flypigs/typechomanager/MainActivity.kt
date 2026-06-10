package com.flypigs.typechomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.flypigs.typechomanager.data.local.ConfigDataStore
import com.flypigs.typechomanager.data.repository.ConfigRepository
import com.flypigs.typechomanager.data.repository.PostRepository
import com.flypigs.typechomanager.ui.navigation.NavGraph
import com.flypigs.typechomanager.ui.settings.ThemeMode
import com.flypigs.typechomanager.ui.theme.TypechoManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var configRepository: ConfigRepository

    @Inject
    lateinit var postRepository: PostRepository

    @Inject
    lateinit var configDataStore: ConfigDataStore

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val themeMode = _themeMode.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load theme preference
        lifecycleScope.launch {
            val mode = configDataStore.getThemeMode()
            _themeMode.value = mode
        }

        setContent {
            val currentTheme by themeMode.collectAsState()
            val darkTheme = when (currentTheme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            TypechoManagerTheme(darkTheme = darkTheme) {
                NavGraph(
                    configRepository = configRepository,
                    postRepository = postRepository
                )
            }
        }
    }
}
