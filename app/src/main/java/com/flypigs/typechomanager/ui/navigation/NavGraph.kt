package com.flypigs.typechomanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flypigs.typechomanager.ui.components.BottomNavBar

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
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
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Splash.route) {
                // TODO: Splash screen
            }

            composable(Screen.Setup.route) {
                // TODO: Setup screen
            }

            composable(Screen.Main.route) {
                // Main screen with nested bottom nav destinations
            }

            composable(Screen.Home.route) {
                // TODO: Home screen
            }

            composable(Screen.Posts.route) {
                // TODO: Posts list screen
            }

            composable(Screen.Attachments.route) {
                com.flypigs.typechomanager.ui.attachments.AttachmentsScreen()
            }

            composable(Screen.Settings.route) {
                // TODO: Settings screen
            }

            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("cid") { type = NavType.IntType }),
            ) { backStackEntry ->
                val cid = backStackEntry.arguments?.getInt("cid") ?: return@composable
                // TODO: PostDetail screen with cid
            }

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
                // TODO: Editor screen with optional cid
            }
        }
    }
}
