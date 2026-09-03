package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.audio.PlaybackState
import com.example.model.BirdSpecies
import com.example.ui.theme.GeoAmber
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoPrimaryGreen
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfacePill
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.theme.GeoVulnerableBg
import com.example.ui.theme.GeoVulnerableBorder
import com.example.ui.theme.GeoVulnerableText
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdDetailSheet(
    bird: BirdSpecies,
    playbackState: PlaybackState,
    onDismiss: () -> Unit,
    onPlayAudio: () -> Unit,
    onSeekAudio: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkDiscovered: () -> Unit
) {
    val context = LocalContext.current
    val isCurrentBirdPlaying = playbackState.currentBirdId == bird.scientificName && playbackState.isPlaying
    val isCurrentBirdBuffering = playbackState.currentBirdId == bird.scientificName && playbackState.isBuffering

    val photos = if (bird.photoUrls.isNotEmpty()) bird.photoUrls else listOf(bird.primaryPhotoUrl)
    val pagerState = rememberPagerState(pageCount = { photos.size })

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = GeoSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Image Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photos[page])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto ${page + 1} de ${bird.commonName}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top Gradient for controls visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                // Close Button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(44.dp),
                    onClick = onDismiss
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Favorite Toggle Button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(44.dp)
                        .testTag("favorite_button_${bird.scientificName}"),
                    onClick = onToggleFavorite
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (bird.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (bird.isFavorite) Color(0xFFFF5252) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Pager Indicator dots (if multiple photos)
                if (photos.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(photos.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                            val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Common Name & Scientific Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bird.commonName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = GeoTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bird.scientificName + if (bird.familyName.isNotBlank()) " • Familia ${bird.familyName}" else "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = GeoTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Song Audio Player Bar (Geometric Balance)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentBirdPlaying) GeoPrimaryContainer else GeoSurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause Big Button (52dp)
                        Surface(
                            shape = CircleShape,
                            color = GeoPrimaryGreen,
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(4.dp, CircleShape)
                                .testTag("detail_play_audio_button"),
                            onClick = onPlayAudio
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCurrentBirdBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isCurrentBirdPlaying) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isCurrentBirdPlaying) "Pausar canto" else "Escuchar canto",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCurrentBirdPlaying) "¡Cantando en vivo!" else "Canto del ave",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextPrimary
                                )
                                if (isCurrentBirdPlaying) {
                                    SoundWaveAnimation(
                                        isPlaying = true,
                                        color = GeoPrimaryGreen,
                                        maxHeight = 16.dp
                                    )
                                } else {
                                    Text(
                                        text = bird.soundDuration,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GeoTextSecondary
                                    )
                                }
                            }

                            if (isCurrentBirdPlaying) {
                                Slider(
                                    value = playbackState.progress,
                                    onValueChange = onSeekAudio,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GeoPrimaryGreen,
                                        activeTrackColor = GeoPrimaryGreen,
                                        inactiveTrackColor = GeoPrimaryGreen.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "Toca el botón para escuchar su melodía silvestre",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // IUCN Conservation Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (bird.conservationStatus.isThreatened) GeoVulnerableBg else GeoSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ConservationBadge(status = bird.conservationStatus, showWhenSafe = true)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Estado de Conservación (UICN)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextSecondary
                                )
                                Text(
                                    text = bird.conservationStatus.descriptionEs,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mandatory IUCN citation
                        Text(
                            text = "Fuente oficial: IUCN 2026. IUCN Red List of Threatened Species. Version 2026-1 <www.iucnredlist.org>",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                fontStyle = FontStyle.Italic
                            ),
                            color = GeoTextSecondary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Habits & Habitat (Description in maximum 2 friendly paragraphs)
                Text(
                    text = "Hábitos y Hábitat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GeoTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Splitting into paragraphs
                val paragraphs = bird.description.split("\n\n").filter { it.isNotBlank() }.take(2)
                paragraphs.forEach { paragraph ->
                    Text(
                        text = paragraph.trim(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontSize = 14.5.sp
                        ),
                        color = GeoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Explorer Stats (Wingspan, Diet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = "Envergadura",
                                tint = GeoAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Envergadura",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GeoTextSecondary
                                )
                                Text(
                                    text = bird.wingspan,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextPrimary
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Alimentación",
                                tint = GeoPrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Dieta",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GeoTextSecondary
                                )
                                Text(
                                    text = bird.diet,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Fun Fact Card (Geometric Balance warm yellow)
                if (bird.funFact.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GeoAmber.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Dato curioso",
                                tint = GeoAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "¿Sabías que...?",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GeoTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = bird.funFact,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = GeoTextSecondary,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // External Encyclopedic & Ornithological References
                Text(
                    text = "Fuentes Ornitológicas y Enciclopedia",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GeoTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val encodedQuery = try {
                    URLEncoder.encode(bird.scientificName, StandardCharsets.UTF_8.toString())
                } catch (e: Exception) {
                    bird.scientificName.replace(" ", "%20")
                }
                val wikiUrl = "https://es.wikipedia.org/wiki/$encodedQuery"
                val ebirdUrl = "https://ebird.org/species/$encodedQuery"
                val inatUrl = "https://www.inaturalist.org/taxa/search?q=$encodedQuery"

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary Bird Source: eBird / Cornell Lab of Ornithology
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GeoSurfaceVariant,
                        border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ebirdUrl))
                                context.startActivity(intent)
                            }
                            .testTag("ebird_external_link")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = GeoPrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ficha en eBird / Cornell Lab",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextPrimary
                                )
                                Text(
                                    text = "Mapas de distribución, registros y taxonomía ornitológica",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Abrir enlace",
                                tint = GeoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Encyclopedic Reference: Wikipedia
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GeoSurfaceVariant,
                        border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wikiUrl))
                                context.startActivity(intent)
                            }
                            .testTag("wikipedia_external_link")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = GeoAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Artículo en Wikipedia",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextPrimary
                                )
                                Text(
                                    text = "Historia natural, subespecies y morfología detallada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GeoTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Abrir enlace",
                                tint = GeoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Discovery Action Button (Geometric Balance Rounded Capsule)
                Button(
                    onClick = onMarkDiscovered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("mark_discovered_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoPrimaryGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (bird.isDiscovered) "¡Especie en tu Cuaderno de Explorador!" else "¡He visto / escuchado esta ave!",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
