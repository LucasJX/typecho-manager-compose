package com.flypigs.typechomanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.ui.navigation.Screen

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    NavItem(Screen.Home.route, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Screen.Posts.route, "文章", Icons.Filled.Article, Icons.Outlined.Article),
    NavItem(Screen.Creator.route, "创作", Icons.Filled.Edit, Icons.Outlined.Edit),
    NavItem(Screen.Attachments.route, "资源", Icons.Filled.Attachment, Icons.Outlined.Attachment),
    NavItem(Screen.Profile.route, "我的", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.height(DesignSystem.Component.NavBarHeight),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route

            // 弹性缩放动画
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.0f else 0.95f,
                animationSpec = spring(
                    dampingRatio = DesignSystem.Animation.FabSpringDamping,
                    stiffness = DesignSystem.Animation.FabSpringStiffness
                ),
                label = "navScale"
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.scale(scale)
                    )
                },
                label = {
                    AnimatedVisibility(
                        visible = selected,
                        enter = expandIn(expandFrom = Alignment.Center),
                        exit = shrinkOut(shrinkTowards = Alignment.Center),
                    ) {
                        Text(text = item.label, style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        }
    }
}
