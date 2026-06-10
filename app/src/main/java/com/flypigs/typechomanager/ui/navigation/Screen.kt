package com.flypigs.typechomanager.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Setup : Screen("setup")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Posts : Screen("posts")
    data object Attachments : Screen("attachments")
    data object Settings : Screen("settings")
    data object PostDetail : Screen("postDetail/{cid}") {
        fun createRoute(cid: Int) = "postDetail/$cid"
    }
    data object Editor : Screen("editor?cid={cid}") {
        fun createRoute(cid: Int? = null) = if (cid != null) "editor?cid=$cid" else "editor"
    }
}
