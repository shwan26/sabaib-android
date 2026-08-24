package com.smnc.sabaib.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smnc.sabaib.ui.charges.ChargesScreen
import com.smnc.sabaib.ui.group.GroupScreen
import com.smnc.sabaib.ui.home.HomeScreen
import com.smnc.sabaib.ui.join.JoinBillScreen
import com.smnc.sabaib.ui.participants.ParticipantsScreen
import com.smnc.sabaib.ui.payment.PaymentScreen
import com.smnc.sabaib.ui.review.ReviewScreen
import com.smnc.sabaib.ui.room.BillRoomScreen
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
                },
                onJoinBill = {
                    navController.navigate(Screen.JoinBillWithCode.route)
                }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                billViewModel = billViewModel,
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
                onGroupCreated = {
                    navController.navigate(Screen.BillRoom.route)
                }
            )
        }

        composable(Screen.Split.route) {
            SplitScreen(
                billViewModel = billViewModel,
                onContinue = {
                    navController.navigate(Screen.Charges.route)
                }
            )
        }

        composable(Screen.Charges.route) {
            ChargesScreen(
                billViewModel = billViewModel,
                onContinue = {
                    navController.navigate(Screen.Payment.route)
                }
            )
        }

        composable(Screen.JoinBillWithCode.route) {
            backStackEntry ->

            val code =
                backStackEntry.arguments
                    ?.getString("code")
                    .orEmpty()

            JoinBillScreen(
                billViewModel = billViewModel,
                initialCode = code,
                onJoined = {
                    navController.navigate(
                        Screen.Participants.route
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Participants.route) {
            ParticipantsScreen(
                billViewModel = billViewModel,
                onContinue = {
                    navController.navigate(
                        Screen.Split.route
                    )
                }
            )
        }

        composable(Screen.Payment.route) {
            PaymentScreen()
        }
        composable(Screen.BillRoom.route) {
            BillRoomScreen(
                billViewModel = billViewModel,
                onStartSplitting = {
                    navController.navigate(
                        Screen.Split.route
                    )
                }
            )
        }
    }
}