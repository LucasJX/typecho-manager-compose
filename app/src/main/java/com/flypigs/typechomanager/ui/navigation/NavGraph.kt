package com.flypigs.typechomanager.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flypigs.typechomanager.data.repository.ConfigRepository
import com.flypigs.typechomanager.ui.attachments.AttachmentsScreen
import com.flypigs.typechomanager.ui.components.BottomNavBar
import com.flypigs.typechomanager.ui.home.HomeScreen
import com.flypigs.typechomanager.ui.posts.PostsScreen
import com.flypigs.typechomanager.ui.settings.SettingsScreen
import com.flypigs.typechomanager.ui.setup.SetupScreen
import kotlinx.coroutines.delay

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    configRepository: ConfigRepository,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Posts.route,
        Screen.Attachments.route,
        Screen.Settings.route,
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BottomNavBar(navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // Splash — checks if already configured
            composable(Screen.Splash.route) {
                SplashScreen(
                    configRepository = configRepository,
                    onNavigateToSetup = {
                        navController.navigate(Screen.Setup.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Setup — first-time config
            composable(Screen.Setup.route) {
                SetupScreen(
                    onNavigateToMain = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home — post list
            composable(Screen.Home.route) {
                HomeScreen(
                    onNewPost = {
                        navController.navigate(Screen.Editor.createRoute())
                    },
                    onPostClick = { cid ->
                        navController.navigate(Screen.PostDetail.createRoute(cid))
                    }
                )
            }

            // Posts — all posts list
            composable(Screen.Posts.route) {
                PostsScreen(
                    onPostClick = { cid ->
                        navController.navigate(Screen.PostDetail.createRoute(cid))
                    }
                )
            }

            // Attachments
            composable(Screen.Attachments.route) {
                AttachmentsScreen()
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToSetup = {
                        navController.navigate(Screen.Setup.route)
                    }
                )
            }

            // Post Detail
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("cid") { type = NavType.IntType }),
            ) { backStackEntry ->
                val cid = backStackEntry.arguments?.getInt("cid") ?: return@composable
                // TODO: PostDetail screen
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("文章详情 #$cid", style = MaterialTheme.typography.headlineMedium)
                }
            }

            // Editor
            composable(
                route = Screen.Editor.route,
                arguments = listOf(
                    navArgument("cid") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                ),
            ) { backStackEntry ->
                val cid = backStackEntry.arguments?.getInt("cid")?.takeIf { it != -1 }
                // TODO: Editor screen
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (cid != null) "编辑文章 #$cid" else "新建文章",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(
    configRepository: ConfigRepository,
    onNavigateToSetup: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    var checking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(800) // Brief splash
        val isLoggedIn = configRepository.isLoggedIn()
        checking = false
        if (isLoggedIn) onNavigateToHome() else onNavigateToSetup()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (checking) {
            CircularProgressIndicator()
        }
    }
}
