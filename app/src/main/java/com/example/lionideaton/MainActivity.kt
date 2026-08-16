
package com.example.lionideaton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lionideaton.navigation.SkinBasketDestination
import com.example.lionideaton.ui.screens.AnalysisScreen
import com.example.lionideaton.ui.screens.AuthScreen
import com.example.lionideaton.ui.screens.BasketScreen
import com.example.lionideaton.ui.screens.CartScreen
import com.example.lionideaton.ui.screens.CartViewModel
import com.example.lionideaton.ui.screens.FoodLogScreen
import com.example.lionideaton.ui.screens.HomeScreen
import com.example.lionideaton.ui.screens.MealLogViewModel
import com.example.lionideaton.ui.screens.MyScreen
import com.example.lionideaton.ui.screens.OnboardingScreen
import com.example.lionideaton.ui.screens.ReportScreen
import com.example.lionideaton.ui.screens.SkinLogScreen
import com.example.lionideaton.ui.screens.SkinLogViewModel
import com.example.lionideaton.ui.screens.UserProfileViewModel
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.LionideatonTheme
import com.example.lionideaton.ui.theme.PeachSecondary
import com.example.lionideaton.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LionideatonTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SkinBasketApp()
                }
            }
        }
    }
}

// Screens whose mockup shows a back button + its own title instead of the generic
// "Skin Basket" bar with the bell icon. Most of these are detail screens pushed on top of a
// bottom-nav tab, but Report is a bottom-nav tab itself whose design still includes a back arrow
// (the bottom bar still renders separately/independently of this, so it stays highlighted).
private val detailScreenTitles = mapOf(
    SkinBasketDestination.Analysis.route to "식단 피부 분석",
    SkinBasketDestination.SkinLog.route to "피부기록",
    SkinBasketDestination.Cart.route to "장바구니",
    SkinBasketDestination.Report.route to "피부 분석 리포트"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinBasketApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val detailTitle = detailScreenTitles[currentRoute]
    // Scoped to this composable's owner (the Activity), so Basket and Cart screens share one cart.
    val cartViewModel: CartViewModel = viewModel()
    val cartItems by cartViewModel.items.collectAsState()
    // Same idea: shared across Home/FoodLog/Analysis/Basket so a logged meal shows up everywhere.
    val mealLogViewModel: MealLogViewModel = viewModel()
    // Shared across SkinLog/Report/My so a captured skin photo shows up in the Before/After + history.
    val skinLogViewModel: SkinLogViewModel = viewModel()
    // Shared across Auth/Onboarding/Home/My so sign-in state + the survey result show up everywhere.
    val userProfileViewModel: UserProfileViewModel = viewModel()
    val isLoggedIn by userProfileViewModel.isLoggedIn.collectAsState()
    val onboardingCompleted by userProfileViewModel.onboardingCompleted.collectAsState()
    val isHomeRoute = currentRoute == SkinBasketDestination.Home.route
    val showAuth = isHomeRoute && !isLoggedIn
    val showOnboarding = isHomeRoute && isLoggedIn && !onboardingCompleted
    val hideChrome = showAuth || showOnboarding

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            when {
                hideChrome -> Unit

                detailTitle != null -> CenterAlignedTopAppBar(
                    title = { Text(text = detailTitle) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                currentRoute == SkinBasketDestination.Basket.route -> CenterAlignedTopAppBar(
                    title = { Text(text = "피부 바스켓 추천") },
                    actions = {
                        IconButton(onClick = { navController.navigate(SkinBasketDestination.Cart.route) }) {
                            BadgedBox(badge = {
                                if (cartItems.isNotEmpty()) Badge { Text(text = "${cartItems.size}") }
                            }) {
                                Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "장바구니")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                currentRoute == SkinBasketDestination.My.route -> CenterAlignedTopAppBar(
                    title = { Text(text = "마이페이지") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = "설정")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                else -> CenterAlignedTopAppBar(
                    title = { Text(text = "Skin Basket") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = "알림")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (!hideChrome) {
                SkinBasketBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SkinBasketDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(SkinBasketDestination.Home.route) {
                when {
                    !isLoggedIn -> AuthScreen(userProfileViewModel = userProfileViewModel)
                    !onboardingCompleted -> OnboardingScreen(userProfileViewModel = userProfileViewModel)
                    else -> HomeScreen(
                        onLogFoodClick = { navController.navigate(SkinBasketDestination.FoodLog.route) },
                        onSkinScoreClick = { navController.navigate(SkinBasketDestination.Analysis.route) },
                        mealLogViewModel = mealLogViewModel,
                        userProfileViewModel = userProfileViewModel
                    )
                }
            }
            composable(SkinBasketDestination.FoodLog.route) { FoodLogScreen(mealLogViewModel = mealLogViewModel) }
            composable(SkinBasketDestination.Analysis.route) { AnalysisScreen(mealLogViewModel = mealLogViewModel) }
            composable(SkinBasketDestination.Basket.route) {
                BasketScreen(cartViewModel = cartViewModel, mealLogViewModel = mealLogViewModel)
            }
            composable(SkinBasketDestination.Cart.route) {
                CartScreen(cartViewModel = cartViewModel)
            }
            composable(SkinBasketDestination.SkinLog.route) { SkinLogScreen(skinLogViewModel = skinLogViewModel) }
            composable(SkinBasketDestination.Report.route) { ReportScreen(skinLogViewModel = skinLogViewModel) }
            composable(SkinBasketDestination.My.route) {
                MyScreen(
                    cartViewModel = cartViewModel,
                    skinLogViewModel = skinLogViewModel,
                    userProfileViewModel = userProfileViewModel
                )
            }
        }
    }
}

@Composable
private fun SkinBasketBottomBar(navController: NavHostController) {
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentRoute = currentDestination?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        SkinBasketDestination.entries
            .filter { it.showInBottomBar }
            .forEach { destination ->
                NavigationBarItem(
                    selected = currentRoute == destination.route,
                    onClick = {
                        if (currentRoute != destination.route) {
                            navController.navigate(destination.route) {
                                popUpTo(SkinBasketDestination.Home.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        destination.icon?.let {
                            Icon(imageVector = it, contentDescription = destination.label)
                        }
                    },
                    label = { Text(text = destination.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CoralPrimary,
                        selectedTextColor = CoralPrimary,
                        indicatorColor = PeachSecondary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
    }
}
