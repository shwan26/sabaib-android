package com.smnc.sabaib.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smnc.sabaib.ui.charges.ChargesScreen
import com.smnc.sabaib.ui.group.GroupScreen
import com.smnc.sabaib.ui.home.HomeScreen
import com.smnc.sabaib.ui.payment.PaymentScreen
import com.smnc.sabaib.ui.review.ReviewScreen
import com.smnc.sabaib.ui.scan.ScanScreen
import com.smnc.sabaib.ui.split.SplitScreen

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = {
                    navController.navigate(Screen.Scan.route)
                }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onContinue = {
                    navController.navigate(Screen.Review.route)
                }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(
                onContinue = {
                    navController.navigate(Screen.Group.route)
                }
            )
        }

        composable(Screen.Group.route) {
            GroupScreen(
                onContinue = {
                    navController.navigate(Screen.Split.route)
                }
            )
        }

        composable(Screen.Split.route) {
            SplitScreen(
                onContinue = {
                    navController.navigate(Screen.Charges.route)
                }
            )
        }

        composable(Screen.Charges.route) {
            ChargesScreen(
                onContinue = {
                    navController.navigate(Screen.Payment.route)
                }
            )
        }

        composable(Screen.Payment.route) {
            PaymentScreen()
        }
    }
}