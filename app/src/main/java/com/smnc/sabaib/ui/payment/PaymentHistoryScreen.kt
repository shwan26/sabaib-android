package com.smnc.sabaib.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smnc.sabaib.ui.home.RecentGroupUi
import com.smnc.sabaib.ui.home.RecentGroupsSection
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiOffWhite

@Composable
fun PaymentHistoryScreen(
    paymentHistoryViewModel: PaymentHistoryViewModel = viewModel(),
    onBack: () -> Unit,
    onGroupClick: (RecentGroupUi) -> Unit = {}
) {
    val uiState by paymentHistoryViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SabaiOffWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                color = SabaiBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Payment History",
                color = SabaiBlack,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        when (val state = uiState) {
            is PaymentHistoryUiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is PaymentHistoryUiState.Loaded -> if (state.bills.isEmpty()) {
                Text(
                    text = "Bills you've joined as a participant will show up here.",
                    color = SabaiGray,
                    fontSize = 14.sp
                )
            } else {
                RecentGroupsSection(
                    groups = state.bills,
                    onGroupClick = onGroupClick,
                    title = "Joined Bills"
                )
            }

            is PaymentHistoryUiState.Error -> Text(
                text = state.message,
                color = SabaiGray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
