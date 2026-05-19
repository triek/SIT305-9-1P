package com.example.a7_1p

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a7_1p.navigation.AppScreen
import com.example.a7_1p.ui.screens.CreatePostScreen
import com.example.a7_1p.ui.screens.ItemDetailScreen
import com.example.a7_1p.ui.screens.ListingScreen
import com.example.a7_1p.ui.theme._71PTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _71PTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    NavHost(
                        navController = navController,
                        startDestination = AppScreen.Listing.route
                    ) {
                        composable(AppScreen.Listing.route) {
                            ListingScreen(
                                onCreatePostClick = {
                                    navController.navigate(AppScreen.CreatePost.route)
                                },
                                onShowOnMapClick = {
                                    startActivity(Intent(this@MainActivity, MapsActivity::class.java))
                                },
                                onItemClick = { itemId ->
                                    navController.navigate(AppScreen.Detail.createRoute(itemId))
                                }
                            )
                        }

                        composable(AppScreen.CreatePost.route) {
                            CreatePostScreen(onPostSaved = {
                                navController.popBackStack()
                            })
                        }

                        composable(
                            route = AppScreen.Detail.route,
                            arguments = listOf(navArgument(AppScreen.Detail.ARG_ITEM_ID) {
                                type = NavType.LongType
                            })
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getLong(AppScreen.Detail.ARG_ITEM_ID)
                                ?: 0L
                            ItemDetailScreen(itemId = itemId, onItemRemoved = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}
