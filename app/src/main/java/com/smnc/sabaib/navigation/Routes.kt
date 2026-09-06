package com.smnc.sabaib.navigation

sealed class Screen(val route: String) {
    data object Landing : Screen("landing")
    data object Home : Screen("home")
    data object Groups : Screen("groups")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Scan : Screen("scan")
    data object Review : Screen("review")
    data object Split : Screen("split")
    data object Payment : Screen("payment")
    data object UserPayment : Screen("user_payment/{participantId}")
    //data object JoinBill : Screen("join_bill")
    data object JoinBillWithCode : Screen("join_bill?code={code}")
    data object Participants : Screen("participants")
    data object BillRoom: Screen("bill_room")
    data object ForgotPassword : Screen("forgot_password")
}