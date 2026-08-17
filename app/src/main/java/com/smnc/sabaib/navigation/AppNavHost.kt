package com.smnc.sabaib.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.smnc.sabaib.viewmodel.BillViewModel

@Composable
fun AppNavHost() {

    val navController = rememberNavController()
    val billViewModel: BillViewModel = viewModel()

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
                billViewModel = billViewModel,
                onContinue = {
                    navController.navigate(Screen.Group.route)
                }
            )
        }

        composable(Screen.Group.route) {
            GroupScreen(
                billViewModel = billViewModel,
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