package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BirdCard
import com.example.ui.components.BirdDetailSheet
import com.example.ui.components.CameraIdentificationSheet
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.RadarHeader
import com.example.ui.components.SoundWaveAnimation
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoPrimaryGreen
import com.example.ui.theme.GeoSurfaceContainer
import com.example.ui.theme.GeoSurfacePill
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.theme.MintGreenSoft
import com.example.ui.theme.SunYellow
import com.example.ui.viewmodel.BirdFilter
import com.example.ui.viewmodel.BirdViewModel

@Composable
fun MainBirdRadarScreen(
    viewModel: BirdViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState()
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_bird_radar_screen"),
        containerColor = GeoBackground,
        topBar = {
            RadarHeader(
                currentLocation = uiState.currentLocation,
                isRefreshing = uiState.isRefreshing,
                searchQuery = uiState.searchQuery,
                activeFilter = uiState.activeFilter,
                onRefreshLocation = { viewModel.detectGpsAndFetch() },
                onSearchCity = { city -> viewModel.searchLocationOrCity(city) },
                onUpdateSearchQuery = { q -> viewModel.updateSearchQuery(q) },
                onSelectFilter = { f -> viewModel.setFilter(f) },
                onShowInfoClick = { showInfoDialog = true }
            )
        },
        floatingActionButton = {
            // Geometric Balance 18dp rounded-2xl FAB
            FloatingActionButton(
                onClick = { viewModel.openCamera() },
                containerColor = GeoPrimaryGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .size(60.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp))
                    .testTag("camera_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Identificar ave con cámara",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
        },
        bottomBar = {
            // Geometric Balance Bottom Navigation Bar
            Surface(
                color = GeoSurfaceContainer,
                border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 1: Explorar (Active)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setFilter(BirdFilter.ALL) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (uiState.activeFilter != BirdFilter.DISCOVERED) GeoPrimaryContainer else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Explorar",
                                tint = if (uiState.activeFilter != BirdFilter.DISCOVERED) GeoPrimaryGreen else GeoTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Explorar",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (uiState.activeFilter != BirdFilter.DISCOVERED) GeoPrimaryGreen else GeoTextSecondary
                        )
                    }

                    // Tab 2: Colección / Descubiertas
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setFilter(BirdFilter.DISCOVERED) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (uiState.activeFilter == BirdFilter.DISCOVERED) GeoPrimaryContainer else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Colección",
                                tint = if (uiState.activeFilter == BirdFilter.DISCOVERED) GeoPrimaryGreen else GeoTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Colección",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (uiState.activeFilter == BirdFilter.DISCOVERED) GeoPrimaryGreen else GeoTextSecondary
                        )
                    }

                    // Tab 3: Amenazadas / Conservación
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setFilter(BirdFilter.THREATENED) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (uiState.activeFilter == BirdFilter.THREATENED) GeoPrimaryContainer else Color.Transparent)
                                .padding(horizontal = 18.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NaturePeople,
                                contentDescription = "Amenazadas",
                                tint = if (uiState.activeFilter == BirdFilter.THREATENED) GeoPrimaryGreen else GeoTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Amenazadas",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (uiState.activeFilter == BirdFilter.THREATENED) GeoPrimaryGreen else GeoTextSecondary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GeoBackground)
        ) {
            if (uiState.isLoading && uiState.birds.isEmpty()) {
                // Loading State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = GeoPrimaryGreen,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sintonizando radar de aves...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GeoTextSecondary
                        )
                    }
                }
            } else if (uiState.filteredBirds.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GeoSurfaceVariant,
                            border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.2f)),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NaturePeople,
                                    contentDescription = null,
                                    tint = GeoPrimaryGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron aves",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Prueba buscando con otro término o seleccionando 'Todas'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GeoTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.updateSearchQuery("")
                                viewModel.setFilter(com.example.ui.viewmodel.BirdFilter.ALL)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryGreen),
                            shape = CircleShape
                        ) {
                            Text("Ver todas las aves")
                        }
                    }
                }
            } else {
                // 2-Column Grid (Geometric Balance Spaced)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("birds_lazy_grid")
                ) {
                    // Summary status bar item
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.filteredBirds.size} especies en la zona",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoTextSecondary
                                )
                            )

                            if (playbackState.isPlaying) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(GeoSurfacePill)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    SoundWaveAnimation(
                                        isPlaying = true,
                                        color = GeoPrimaryGreen,
                                        maxHeight = 12.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audio activo",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GeoPrimaryGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Bird Cards
                    items(
                        items = uiState.filteredBirds,
                        key = { it.scientificName }
                    ) { bird ->
                        val isPlaying = playbackState.currentBirdId == bird.scientificName && playbackState.isPlaying
                        val isBuffering = playbackState.currentBirdId == bird.scientificName && playbackState.isBuffering

                        BirdCard(
                            bird = bird,
                            isCurrentlyPlaying = isPlaying,
                            isBuffering = isBuffering,
                            onCardClick = { viewModel.selectBirdForDetail(bird) },
                            onPlayAudioClick = { viewModel.playBirdSong(bird) }
                        )
                    }
                }
            }

            // Expanded Detail Bottom Sheet
            uiState.selectedBirdForDetail?.let { bird ->
                BirdDetailSheet(
                    bird = bird,
                    playbackState = playbackState,
                    onDismiss = { viewModel.closeDetail() },
                    onPlayAudio = { viewModel.playBirdSong(bird) },
                    onSeekAudio = { progress -> viewModel.audioPlayer.seekTo(progress) },
                    onToggleFavorite = { viewModel.toggleFavorite(bird) },
                    onMarkDiscovered = { viewModel.markDiscovered(bird) }
                )
            }

            // Camera Identification View
            if (uiState.isCameraOpen) {
                CameraIdentificationSheet(
                    isIdentifying = uiState.isIdentifying,
                    classificationResult = uiState.classificationResult,
                    onCapturePhoto = { bmp -> viewModel.classifyCapturedPhoto(bmp) },
                    onClose = { viewModel.closeCamera() },
                    onViewDetail = { bird -> viewModel.selectBirdForDetail(bird) },
                    onPlayAudio = { bird -> viewModel.playBirdSong(bird) }
                )
            }

            // New Discovery Celebration Dialog
            if (uiState.showCelebration && uiState.celebrationBird != null) {
                CelebrationDialog(
                    bird = uiState.celebrationBird!!,
                    onDismiss = { viewModel.dismissCelebration() }
                )
            }

            // Sources, Conservation & IUCN Citation Dialog
            if (showInfoDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    confirmButton = {
                        Button(
                            onClick = { showInfoDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryGreen),
                            shape = CircleShape
                        ) {
                            Text("Entendido", fontWeight = FontWeight.Bold)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GeoPrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Fuentes de Datos y Conservación",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Radar de Aves conecta observadores de la naturaleza con bases científicas abiertas, aplicando políticas de consulta responsable y almacenamiento local.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoTextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // UICN Citation Section
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GeoSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Estado de Conservación (UICN)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GeoPrimaryGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "IUCN 2026. IUCN Red List of Threatened Species. Version 2026-1 <www.iucnredlist.org>",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = GeoTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Consultas puntuales bajo demanda\n• Caché persistente en dispositivo (Room)\n• Control estricto de límites de tasa",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GeoTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Other Open Sources
                            Text(
                                text = "Otras fuentes y colaboraciones:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GeoTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• eBird API (Cornell Lab of Ornithology)\n• Xeno-Canto (Bioacústica comunitaria)\n• iNaturalist Taxa Encyclopedia",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoTextSecondary
                            )
                        }
                    },
                    containerColor = GeoSurfaceContainer,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}
