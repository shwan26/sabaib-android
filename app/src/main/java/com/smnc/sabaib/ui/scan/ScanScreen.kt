package com.smnc.sabaib.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.smnc.sabaib.R
import com.smnc.sabaib.domain.scan.ReceiptParser
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel
import com.smnc.sabaib.util.createScanImageUri
import com.smnc.sabaib.util.loadRotatedBitmap
import com.smnc.sabaib.util.recognizeTextFrom
import kotlinx.coroutines.launch

private enum class ScanState {
    Idle, Preview, Processing, Error
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    billViewModel: BillViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var scanState by remember { mutableStateOf(ScanState.Idle) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun processImage(uri: Uri) {
        scanState = ScanState.Processing

        coroutineScope.launch {
            try {
                val bitmap = loadRotatedBitmap(context, uri)

                if (bitmap == null) {
                    errorMessage = "Couldn't read that photo. Please try again."
                    scanState = ScanState.Error
                    return@launch
                }

                previewBitmap = bitmap

                val recognizedText = recognizeTextFrom(bitmap)
                val parsedItems = ReceiptParser.parse(recognizedText)

                if (parsedItems.isEmpty()) {
                    errorMessage =
                        "Couldn't find any items on that receipt. " +
                                "You can retake the photo, or continue and " +
                                "add items manually."
                }

                billViewModel.updateItems(parsedItems)
                scanState = ScanState.Preview

            } catch (e: Exception) {
                errorMessage = "Something went wrong reading that photo: " +
                        (e.message ?: "unknown error")
                scanState = ScanState.Error
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            processImage(uri)
        } else {
            scanState = ScanState.Idle
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            processImage(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createScanImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            errorMessage = "Camera permission is needed to scan a receipt. " +
                    "You can still pick a photo from your gallery instead."
        }
    }

    fun launchCamera() {
        errorMessage = null

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val uri = createScanImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchGallery() {
        errorMessage = null
        galleryLauncher.launch("image/*")
    }

    fun retake() {
        scanState = ScanState.Idle
        previewBitmap = null
        errorMessage = null
        billViewModel.updateItems(emptyList())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Receipt",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when (scanState) {

                ScanState.Idle, ScanState.Error -> {

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(R.drawable.penguin_think),
                        contentDescription = "Penguin thinking about your receipt",
                        modifier = Modifier.size(180.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Lay it flat and make sure the prices " +
                                "are clearly visible. You'll be able to " +
                                "review and fix everything on the next step.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedButton(
                        onClick = { launchCamera() },
                        border = BorderStroke(1.dp, SabaiLightGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SabaiBlack),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.photo_camera_24),
                            contentDescription = null,
                            tint = SabaiBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Take a Photo", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val galleryInteractionSource = remember { MutableInteractionSource() }
                    val isGalleryPressed by galleryInteractionSource.collectIsPressedAsState()

                    OutlinedButton(
                        onClick = { launchGallery() },
                        interactionSource = galleryInteractionSource,
                        border = BorderStroke(1.dp, if (isGalleryPressed) SabaiYellow else SabaiBlack),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SabaiWhite,
                            contentColor = SabaiBlack
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.photo_library_24),
                            contentDescription = null,
                            tint = SabaiBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Choose from Gallery", fontWeight = FontWeight.Bold)
                    }

                    errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                ScanState.Processing -> {

                    Spacer(modifier = Modifier.height(64.dp))

                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Reading your receipt...")
                }

                ScanState.Preview -> {

                    previewBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured receipt photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val itemCount = billViewModel.bill.value.items.size

                    Text(
                        text = if (itemCount > 0) {
                            "Found $itemCount item" +
                                    if (itemCount == 1) "" else "s"
                        } else {
                            "No items found"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SabaiYellow,
                            contentColor = SabaiBlack
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Continue", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { retake() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retake Photo")
                    }
                }
            }
        }
    }
}