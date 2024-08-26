package com.example.stocks

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stocks.presentation.company_info.CompanyInfoScreen
import com.example.stocks.presentation.company_listings.CompanyListingsScreen
import com.example.stocks.util.Screen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.StockListings.route
    ) {
        composable(Screen.StockListings.route) {
            CompanyListingsScreen { symbol ->
                navController.navigate(Screen.StockDetails.createRoute(symbol))
            }
        }
        composable(Screen.StockDetails.route) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol")
            if (symbol != null) {
                CompanyInfoScreen(symbol = symbol)
            }
        }
    }
}