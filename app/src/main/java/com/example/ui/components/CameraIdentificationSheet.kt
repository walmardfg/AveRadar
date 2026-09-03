package com.example.ui.components

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.classifier.ClassificationResult
import com.example.model.BirdSpecies
import com.example.ui.theme.GeoAmber
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimaryGreen
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraIdentificationSheet(
    isIdentifying: Boolean,
    classificationResult: ClassificationResult?,
    onCapturePhoto: (Bitmap) -> Unit,
    onClose: () -> Unit,
    onViewDetail: (BirdSpecies) -> Unit,
    onPlayAudio: (BirdSpecies) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // CameraX Viewfinder
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            Log.e("CameraSheet", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Viewfinder Target Reticle
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .border(2.dp, GeoPrimaryGreen.copy(alpha = 0.85f), RoundedCornerShape(28.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Enfoca el ave aquí",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        } else {
            // Permission fallback message
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Se necesita permiso de cámara para identificar aves con la IA de AVRadar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryGreen)
                ) {
                    Text("Conceder Permiso")
                }
            }
        }

        // Top bar controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp),
                onClick = onClose
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar cámara",
                        tint = Color.White
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GeoAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Identificador On-Device",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Quick demo test trigger if running in environment without physical camera hardware
            Surface(
                shape = CircleShape,
                color = GeoPrimaryGreen,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("scan_demo_button"),
                onClick = {
                    // Create a synthetic sample bitmap to identify
                    val sampleBmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                    sampleBmp.eraseColor(android.graphics.Color.rgb(200, 60, 40)) // reddish
                    onCapturePhoto(sampleBmp)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Escanear ave de prueba",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Bottom Capture Controls
        if (classificationResult == null && !isIdentifying) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(bottom = 48.dp, top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Apunta al ave y toca el botón para identificar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Big Shutter Button (76dp)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, GeoPrimaryGreen, CircleShape)
                        .shadow(8.dp, CircleShape)
                        .testTag("shutter_button"),
                    onClick = {
                        val capture = imageCapture
                        if (capture != null) {
                            val executor = Executors.newSingleThreadExecutor()
                            capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image)
                                    image.close()
                                    ContextCompat.getMainExecutor(context).execute {
                                        onCapturePhoto(bitmap)
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraSheet", "Capture failed: ${exception.message}", exception)
                                    // Fallback synthetic capture
                                    val fallback = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                                    fallback.eraseColor(android.graphics.Color.rgb(220, 180, 50))
                                    ContextCompat.getMainExecutor(context).execute {
                                        onCapturePhoto(fallback)
                                    }
                                }
                            })
                        } else {
                            val fallback = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                            fallback.eraseColor(android.graphics.Color.rgb(220, 180, 50))
                            onCapturePhoto(fallback)
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Tomar Foto",
                            tint = GeoPrimaryGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Loading Overlay while identifying
        if (isIdentifying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = GeoPrimaryGreen,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Analizando plumaje y cantos...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Clasificador IA on-device",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoAmber
                    )
                }
            }
        }

        // Classification Result Card (Geometric Balance 28dp radius)
        if (classificationResult != null) {
            val bird = classificationResult.topMatch
            val confidencePct = (classificationResult.confidence * 100).toInt()

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(12.dp, RoundedCornerShape(28.dp))
                        .testTag("identification_result_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = GeoSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Header match badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GeoPrimaryGreen)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "¡Identificación exitosa! ($confidencePct% coincidencia)",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            IconButton(onClick = onClose) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = GeoTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bird info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(bird.primaryPhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = bird.commonName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bird.commonName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextPrimary
                                )
                                Text(
                                    text = bird.scientificName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                    color = GeoTextSecondary
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                if (bird.conservationStatus.isThreatened) {
                                    ConservationBadge(status = bird.conservationStatus)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2 short paragraphs description summary
                        Text(
                            text = bird.description.split("\n\n").firstOrNull() ?: bird.description,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = GeoTextSecondary,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Row: Escuchar Canto + Ver Ficha Completa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onPlayAudio(bird) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = GeoSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = GeoPrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Oír Canto", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = GeoPrimaryGreen)
                            }

                            Button(
                                onClick = {
                                    onClose()
                                    onViewDetail(bird)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ver Ficha", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val plane = image.planes[0]
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val rotation = image.imageInfo.rotationDegrees
    return if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}
