package com.smnc.sabaib.ui.join

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.smnc.sabaib.R
import com.smnc.sabaib.model.JoinMethod
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel

private enum class JoinTab {
    EnterCode, ScanQr
}

private fun extractGroupCode(scannedText: String): String {
    val marker = "/join/"
    val markerIndex = scannedText.indexOf(marker)

    val raw = if (markerIndex >= 0) {
        scannedText
            .substring(markerIndex + marker.length)
            .substringBefore('/')
            .substringBefore('?')
    } else {
        scannedText
    }

    return raw.trim().uppercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinBillScreen(
    billViewModel: BillViewModel,
    initialCode: String = "",
    onJoined: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(JoinTab.EnterCode) }

    var billCode by remember { mutableStateOf(initialCode) }
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scannedCode by remember { mutableStateOf<String?>(null) }

    fun attemptJoin() {
        if (billViewModel.isValidGroupCode(billCode)) {

            billViewModel.addParticipant(
                name = name,
                joinMethod = JoinMethod.SELF_JOINED
            )

            errorMessage = null
            onJoined()

        } else {
            errorMessage = "Group code not found"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Join a Group", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {

            JoinTabSwitcher(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (selectedTab) {

                JoinTab.EnterCode -> {

                    Text(
                        text = "Ask the host for their 6-digit group code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SabaiGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        label = { Text("Your code") },
                        value = billCode,
                        onValueChange = { billCode = it.uppercase() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SabaiBlack,
                            unfocusedBorderColor = SabaiBlack
                        )
                    )
                }

                JoinTab.ScanQr -> {

                    QrScannerBox(
                        scannedCode = scannedCode,
                        onCodeScanned = { rawText ->
                            val code = extractGroupCode(rawText)
                            scannedCode = code
                            billCode = code
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Your name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SabaiBlack,
                    unfocusedBorderColor = SabaiBlack
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { attemptJoin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SabaiYellow,
                    contentColor = SabaiBlack
                ),
                enabled = billCode.isNotBlank() && name.isNotBlank()
            ) {
                Text("Join Group", fontWeight = FontWeight.Bold)
            }

            errorMessage?.let { message ->

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun JoinTabSwitcher(
    selectedTab: JoinTab,
    onTabSelected: (JoinTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SabaiOffWhite)
            .padding(4.dp)
    ) {
        JoinTabSegment(
            text = "Enter Code",
            selected = selectedTab == JoinTab.EnterCode,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(JoinTab.EnterCode) }
        )

        JoinTabSegment(
            text = "Scan QR",
            selected = selectedTab == JoinTab.ScanQr,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(JoinTab.ScanQr) }
        )
    }
}

@Composable
private fun JoinTabSegment(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) SabaiBlack else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) SabaiYellow else SabaiGray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun QrScannerBox(
    scannedCode: String?,
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SabaiBlack),
        contentAlignment = Alignment.Center
    ) {

        if (hasCameraPermission && scannedCode == null) {

            LiveQrPreview(onCodeScanned = onCodeScanned)

            // Dim the live feed so the yellow guide frame stands out.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SabaiBlack.copy(alpha = 0.45f))
            )
        }

        when {

            !hasCameraPermission -> {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "Camera permission is needed to scan a QR code.",
                        color = SabaiWhite,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SabaiYellow,
                            contentColor = SabaiBlack
                        )
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }

            scannedCode != null -> {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "QR code detected",
                        color = SabaiYellow,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = scannedCode,
                        color = SabaiWhite,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            else -> {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        QrViewfinderFrame()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "POINT AT THE HOST'S QR CODE",
                        color = SabaiYellow,
                        letterSpacing = 1.5.sp,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QrViewfinderFrame() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRoundRect(
            color = SabaiYellow,
            style = Stroke(width = 4.dp.toPx()),
            cornerRadius = CornerRadius(20.dp.toPx())
        )
    }
}

@Composable
private fun LiveQrPreview(
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasScanned by remember { mutableStateOf(false) }

    val barcodeView = remember {
        BarcodeView(context).apply {
            decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        }
    }

    DisposableEffect(lifecycleOwner, barcodeView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> barcodeView.resume()
                Lifecycle.Event.ON_PAUSE -> barcodeView.pause()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (!hasScanned) {
                    hasScanned = true
                    barcodeView.pause()
                    onCodeScanned(result.text)
                }
            }
        })

        barcodeView.resume()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            barcodeView.pause()
        }
    }

    AndroidView(
        factory = { barcodeView },
        modifier = Modifier.fillMaxSize()
    )
}
