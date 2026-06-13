package com.flypigs.typechomanager.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Setup : Screen("setup")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Posts : Screen("posts")
    data object Creator : Screen("creator")
    data object Attachments : Screen("attachments")
    data object Profile : Screen("profile")
    data object PostDetail : Screen("postDetail/{cid}") {
        fun createRoute(cid: Int) = "postDetail/$cid"
    }
    data object Editor : Screen("editor?postId={postId}") {
        fun createRoute(postId: String? = null): String {
            return if (postId != null) "editor?postId=$postId" else "editor"
        }
    }
    data object Changelog : Screen("changelog")
    data object Stats : Screen("stats")
}
