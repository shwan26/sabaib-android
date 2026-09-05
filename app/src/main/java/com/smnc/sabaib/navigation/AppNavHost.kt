package com.smnc.sabaib.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.ui.charges.ChargesScreen
import com.smnc.sabaib.ui.group.GroupScreen
import com.smnc.sabaib.ui.home.HomeScreen
import com.smnc.sabaib.ui.join.JoinBillScreen
import com.smnc.sabaib.ui.landing.LandingScreen
import com.smnc.sabaib.ui.login.ForgotPasswordScreen
import com.smnc.sabaib.ui.login.LoginScreen
import com.smnc.sabaib.ui.participants.ParticipantsScreen
import com.smnc.sabaib.ui.payment.PaymentScreen
import com.smnc.sabaib.ui.review.ReviewScreen
import com.smnc.sabaib.ui.room.BillRoomScreen
import com.smnc.sabaib.ui.scan.ScanScreen
import com.smnc.sabaib.ui.split.SplitScreen
import com.smnc.sabaib.util.OnboardingPrefs
import com.smnc.sabaib.viewmodel.BillViewModel

@Composable
fun AppNavHost() {

    val navController = rememberNavController()
    val billViewModel: BillViewModel = viewModel()
    val authRepository = remember { AuthRepository() }
    val context = LocalContext.current
    val startDestination = remember {
        if (OnboardingPrefs.hasSeenLanding(context)) Screen.Home.route else Screen.Landing.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        }
    ) {

        composable(Screen.Landing.route) {
            LandingScreen(
                onGetStarted = {
                    OnboardingPrefs.markLandingSeen(context)
                    navController.navigate("login/home") {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = {
                    if (authRepository.isLoggedIn()) {
                        navController.navigate(Screen.Scan.route)
                    } else {
                        navController.navigate("login/scan")
                    }
                },
                onJoinBill = {
                    navController.navigate(Screen.JoinBillWithCode.route)
                }
            )
        }

        composable(
            route = "login/{redirect}",
            arguments = listOf(navArgument("redirect") { type = NavType.StringType })
        ) { backStackEntry ->
            val redirect = backStackEntry.arguments?.getString("redirect") ?: "home"
            LoginScreen(
                onAuthSuccess = {
                    val target = when (redirect) {
                        "scan" -> Screen.Scan.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(target) {
                        popUpTo("login/$redirect") { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Scan.route) {
            // Guard again in case of deep link or back navigation into this route
            if (authRepository.isLoggedIn()) {
                ScanScreen(
                    billViewModel = billViewModel,
                    onContinue = {
                        navController.navigate(Screen.Review.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login/scan") {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            }
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