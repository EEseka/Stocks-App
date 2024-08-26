package com.example.stocks.util

sealed class Screen(val route: String) {
    data object StockListings : Screen("stock_listings")
    data object StockDetails : Screen("stock_details/{symbol}"){
        fun createRoute(symbol: String) = "stock_details/$symbol"
    }
}