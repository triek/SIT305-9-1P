package com.example.a7_1p.navigation

sealed class AppScreen(val route: String) {
    data object Listing : AppScreen("listing")
    data object CreatePost : AppScreen("create_post")
    data object Detail : AppScreen("detail/{itemId}") {
        const val ARG_ITEM_ID = "itemId"
        fun createRoute(itemId: Long) = "detail/$itemId"
    }
}
