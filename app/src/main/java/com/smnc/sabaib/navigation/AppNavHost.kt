package com.smnc.sabaib.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.BillingRepository
import com.smnc.sabaib.model.BillStage
import com.smnc.sabaib.ui.components.SabaiBottomNavBar
import com.smnc.sabaib.ui.components.SabaiTab
import com.smnc.sabaib.ui.groups.GroupsScreen
import com.smnc.sabaib.ui.groups.GroupsViewModel
import com.smnc.sabaib.ui.home.HomeScreen
import com.smnc.sabaib.ui.home.RecentGroupUi
import com.smnc.sabaib.ui.join.JoinBillScreen
import com.smnc.sabaib.ui.landing.LandingScreen
import com.smnc.sabaib.ui.login.ForgotPasswordScreen
import com.smnc.sabaib.ui.login.LoginScreen
import com.smnc.sabaib.ui.participants.ParticipantsScreen
import com.smnc.sabaib.ui.paywall.PaywallScreen
import com.smnc.sabaib.ui.paywall.PaywallViewModel
import com.smnc.sabaib.ui.payment.PaymentScreen
import com.smnc.sabaib.ui.payment.UserPaymentScreen
import com.smnc.sabaib.ui.profile.ProfileScreen
import com.smnc.sabaib.ui.profile.ProfileViewModel
import com.smnc.sabaib.ui.profile.resolveDisplayName
import com.smnc.sabaib.ui.review.ReviewScreen
import com.smnc.sabaib.ui.room.BillRoomScreen
import com.smnc.sabaib.ui.scan.ScanScreen
import com.smnc.sabaib.ui.settings.SettingsScreen
import com.smnc.sabaib.ui.split.SplitScreen
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val MAIN_TAB_ROUTES = setOf(Screen.Home.route, Screen.Groups.route, Screen.Profile.route)

@Composable
fun AppNavHost() {

    val navController = rememberNavController()
    val billViewModel: BillViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val groupsViewModel: GroupsViewModel = viewModel()
    val paywallViewModel: PaywallViewModel = viewModel()
    val authRepository = remember { AuthRepository() }
    val billingRepository = remember { BillingRepository() }
    val coroutineScope = rememberCoroutineScope()

    val onGroupClick: (RecentGroupUi) -> Unit = { group ->
        coroutineScope.launch {
            val loaded = billViewModel.loadExistingBill(group.id, authRepository.currentUserId())
            if (loaded) {
                val destination = if (billViewModel.bill.value.stage == BillStage.PAYMENT) {
                    Screen.Payment.route
                } else {
                    Screen.BillRoom.route
                }
                navController.navigate(destination)
            }
        }
    }

    LaunchedEffect(Unit) {
        authRepository.sessionStatusFlow().collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    authRepository.currentUserId()?.let { billingRepository.logIn(it) }
                }
                is SessionStatus.NotAuthenticated -> billingRepository.logOut()
                else -> Unit
            }
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = SabaiOffWhite,
        bottomBar = {
            if (currentRoute in MAIN_TAB_ROUTES) {
                val selectedTab = when (currentRoute) {
                    Screen.Groups.route -> SabaiTab.GROUPS
                    Screen.Profile.route -> SabaiTab.PROFILE
                    else -> SabaiTab.HOME
                }
                SabaiBottomNavBar(
                    selectedTab = selectedTab,
                    onHomeClick = { navController.navigateToTab(Screen.Home.route) },
                    onGroupsClick = { navController.navigateToTab(Screen.Groups.route) },
                    onProfileClick = { navController.navigateToTab(Screen.Profile.route) }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Gate.route,
            modifier = Modifier.padding(paddingValues),
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

        composable(Screen.Gate.route) {
            LaunchedEffect(Unit) {
                val status = authRepository.sessionStatusFlow()
                    .first { it !is SessionStatus.Initializing }
                val destination = if (status is SessionStatus.Authenticated) {
                    Screen.Home.route
                } else {
                    Screen.Landing.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Gate.route) { inclusive = true }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SabaiOffWhite),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SabaiYellow)
            }
        }

        composable(Screen.Landing.route) {
            LandingScreen(
                onGetStarted = {
                    navController.navigate("login/home") {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            // Guard again in case of session expiry/refresh failure while already on this route
            if (authRepository.isLoggedIn()) {
                val profileUiState by profileViewModel.uiState.collectAsState()
                val displayName = resolveDisplayName(profileUiState, authRepository.currentUserEmail())
                HomeScreen(
                    userName = displayName,
                    groupsViewModel = groupsViewModel,
                    onGroupClick = onGroupClick,
                    onScanClick = {
                        billViewModel.startNewBill()
                        if (authRepository.isLoggedIn()) {
                            navController.navigate(Screen.Scan.route)
                        } else {
                            navController.navigate("login/scan")
                        }
                    },
                    onJoinBill = {
                        billViewModel.startNewBill()
                        navController.navigate("join_bill")
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Groups.route) {
            GroupsScreen(groupsViewModel = groupsViewModel, onGroupClick = onGroupClick)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authRepository = authRepository,
                profileViewModel = profileViewModel,
                onUpgradeClick = {
                    navController.navigate(Screen.Paywall.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onLoggedOut = {
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                profileViewModel = profileViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onAccountDeleted = {
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(0) { inclusive = true }
                    }
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
                        "join_bill" -> "join_bill"
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

        composable(Screen.Paywall.route) {
            PaywallScreen(
                profileViewModel = profileViewModel,
                paywallViewModel = paywallViewModel,
                onBack = {
                    navController.popBackStack()
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
                    authRepository = authRepository,
                    onContinue = {
                        navController.navigate(Screen.Review.route)
                    },
                    onLimitReached = {
                        navController.navigate(Screen.Paywall.route)
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
                authRepository = authRepository,
                onContinue = {
                    navController.navigate(Screen.BillRoom.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Split.route) {
            SplitScreen(
                billViewModel = billViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onContinue = {
                    navController.navigate(Screen.Payment.route)
                }
            )
        }

        composable(
            route = Screen.JoinBillWithCode.route,
            arguments = listOf(navArgument("code") { type = NavType.StringType; defaultValue = "" })
        ) {
                backStackEntry ->

            val code =
                backStackEntry.arguments
                    ?.getString("code")
                    .orEmpty()

            if (authRepository.isLoggedIn()) {
                JoinBillScreen(
                    billViewModel = billViewModel,
                    authRepository = authRepository,
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
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login/join_bill") {
                        popUpTo("join_bill") { inclusive = true }
                    }
                }
            }
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
            // Only the host sees everyone's payment status/list - a
            // participant only ever sees their own amount and the host's
            // QR, never anyone else's total.
            val participants by billViewModel.participants
            val currentParticipantId by billViewModel.currentParticipantId
            val viewerIsHost = participants.find { it.id == currentParticipantId }?.isHost == true

            val backToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            }

            if (viewerIsHost) {
                PaymentScreen(
                    billViewModel = billViewModel,
                    onParticipantClick = { participantId ->
                        navController.navigate("user_payment/$participantId")
                    },
                    onBackToHome = backToHome
                )
            } else {
                val selfId = currentParticipantId ?: participants.firstOrNull()?.id.orEmpty()

                UserPaymentScreen(
                    billViewModel = billViewModel,
                    participantId = selfId,
                    readOnly = true,
                    onBack = { navController.popBackStack() },
                    onDone = {},
                    onUndo = {},
                    onBackToHome = backToHome
                )
            }
        }

        composable(
            route = Screen.UserPayment.route,
            arguments = listOf(navArgument("participantId") { type = NavType.StringType })
        ) { backStackEntry ->

            val participantId = backStackEntry.arguments
                ?.getString("participantId")
                .orEmpty()

            UserPaymentScreen(
                billViewModel = billViewModel,
                participantId = participantId,
                onBack = {
                    navController.popBackStack()
                },
                onDone = {
                    billViewModel.markParticipantPaid(participantId)
                },
                onUndo = {
                    billViewModel.markParticipantUnpaid(participantId)
                },
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        composable(Screen.BillRoom.route) {
            BillRoomScreen(
                billViewModel = billViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onContinue = {
                    navController.navigate(
                        Screen.Split.route
                    )
                }
            )
        }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}