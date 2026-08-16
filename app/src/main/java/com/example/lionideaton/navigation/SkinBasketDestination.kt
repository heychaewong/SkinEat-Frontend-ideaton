package com.example.lionideaton.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.ui.graphics.vector.ImageVector

enum class SkinBasketDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val showInBottomBar: Boolean = false
) {
    Home(route = "home", label = "홈", icon = Icons.Filled.Home, showInBottomBar = true),
    FoodLog(route = "foods", label = "음식기록", icon = Icons.Filled.RestaurantMenu, showInBottomBar = true),
    Analysis(route = "analysis", label = "분석"),
    Basket(route = "basket", label = "추천식재료", icon = Icons.Filled.ShoppingBasket, showInBottomBar = true),
    Cart(route = "cart", label = "장바구니"),
    SkinLog(route = "skin", label = "피부기록"),
    Report(route = "report", label = "리포트", icon = Icons.Filled.BarChart, showInBottomBar = true),
    My(route = "my", label = "마이", icon = Icons.Filled.Person, showInBottomBar = true)
}
