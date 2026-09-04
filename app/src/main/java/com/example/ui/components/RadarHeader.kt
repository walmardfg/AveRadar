package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.UserLocation
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoOutline
import com.example.ui.theme.GeoPrimaryGreen
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfacePill
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.viewmodel.BirdFilter

@Composable
fun RadarHeader(
    currentLocation: UserLocation,
    isRefreshing: Boolean,
    searchQuery: String,
    activeFilter: BirdFilter,
    isSearchingOnline: Boolean = false,
    onRefreshLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onTriggerSearchOnline: (String) -> Unit = {},
    onSelectFilter: (BirdFilter) -> Unit,
    onShowInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditingLocation by remember { mutableStateOf(false) }
    var locationInputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GeoBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Title & Radar Icon Row (Geometric Balance)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GeoPrimaryGreen,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Radar de Aves Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Radar de Aves",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = GeoTextPrimary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info & Sources Button
                Surface(
                    shape = CircleShape,
                    color = GeoSurfaceVariant,
                    border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("info_sources_button"),
                    onClick = onShowInfoClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Fuentes y créditos",
                            tint = GeoTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // GPS Refresh Button (Geometric Balance)
                Surface(
                    shape = CircleShape,
                    color = GeoSurfaceVariant,
                    border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("refresh_gps_button"),
                    onClick = onRefreshLocation
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar ubicación GPS",
                            tint = GeoTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Location Info Pill / Location Search input (Geometric Balance Full Capsule)
        if (!isEditingLocation) {
            Surface(
                shape = CircleShape,
                color = GeoSurfaceVariant,
                border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable {
                        isEditingLocation = true
                        locationInputText = currentLocation.cityName
                    }
                    .testTag("location_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (currentLocation.isGpsActive) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = GeoTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UBICACIÓN ACTUAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = GeoTextSecondary.copy(alpha = 0.75f)
                        )
                        Text(
                            text = currentLocation.cityName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            color = GeoTextPrimary,
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.EditLocationAlt,
                        contentDescription = "Cambiar ciudad",
                        tint = GeoTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = locationInputText,
                onValueChange = { locationInputText = it },
                label = { Text("Escribe una ciudad o región", color = GeoTextSecondary) },
                placeholder = { Text("Ej: Bariloche, Madrid, Ciudad de México...", color = GeoTextSecondary.copy(alpha = 0.6f)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = GeoTextPrimary, fontSize = 14.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("location_search_input"),
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GeoTextPrimary,
                    unfocusedTextColor = GeoTextPrimary,
                    focusedBorderColor = GeoPrimaryGreen,
                    unfocusedBorderColor = GeoOutline.copy(alpha = 0.4f),
                    focusedContainerColor = GeoSurface,
                    unfocusedContainerColor = GeoSurface,
                    cursorColor = GeoPrimaryGreen,
                    focusedLeadingIconColor = GeoPrimaryGreen,
                    unfocusedLeadingIconColor = GeoTextSecondary,
                    focusedTrailingIconColor = GeoPrimaryGreen,
                    unfocusedTrailingIconColor = GeoTextSecondary,
                    focusedPlaceholderColor = GeoTextSecondary.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = GeoTextSecondary.copy(alpha = 0.6f)
                ),
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            if (locationInputText.isNotBlank()) {
                                onSearchCity(locationInputText)
                            }
                            isEditingLocation = false
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar región", tint = GeoPrimaryGreen)
                        }
                        IconButton(onClick = {
                            isEditingLocation = false
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Cancelar", tint = GeoTextSecondary)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (locationInputText.isNotBlank()) {
                        onSearchCity(locationInputText)
                    }
                    isEditingLocation = false
                    focusManager.clearFocus()
                })
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = listOf("📍 Mi GPS", "Bariloche", "Buenos Aires", "Córdoba", "Mendoza", "Madrid", "Bogotá", "Cancún", "Santiago")
                suggestions.forEach { place ->
                    Surface(
                        shape = CircleShape,
                        color = GeoSurfaceVariant,
                        border = BorderStroke(1.dp, GeoOutline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clickable {
                                if (place == "📍 Mi GPS") {
                                    onRefreshLocation()
                                } else {
                                    onSearchCity(place)
                                }
                                isEditingLocation = false
                                focusManager.clearFocus()
                            }
                    ) {
                        Text(
                            text = place,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = GeoTextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bird Name Search Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onUpdateSearchQuery,
            placeholder = { Text("Buscar aves por nombre o canto...", fontSize = 13.5.sp, color = GeoTextSecondary.copy(alpha = 0.6f)) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = GeoTextPrimary, fontSize = 14.sp),
            leadingIcon = {
                IconButton(onClick = {
                    if (searchQuery.isNotBlank()) {
                        onTriggerSearchOnline(searchQuery)
                        focusManager.clearFocus()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar ave",
                        tint = if (searchQuery.isNotBlank()) GeoPrimaryGreen else GeoTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = {
                if (isSearchingOnline) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = GeoPrimaryGreen
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onUpdateSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar búsqueda", tint = GeoTextSecondary)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchQuery.isNotBlank()) {
                    onTriggerSearchOnline(searchQuery)
                }
                focusManager.clearFocus()
            }),
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GeoTextPrimary,
                unfocusedTextColor = GeoTextPrimary,
                focusedContainerColor = GeoSurface,
                unfocusedContainerColor = GeoSurface,
                focusedBorderColor = GeoPrimaryGreen,
                unfocusedBorderColor = GeoOutline.copy(alpha = 0.35f),
                cursorColor = GeoPrimaryGreen,
                focusedLeadingIconColor = GeoPrimaryGreen,
                unfocusedLeadingIconColor = GeoTextSecondary,
                focusedPlaceholderColor = GeoTextSecondary.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = GeoTextSecondary.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bird_search_bar")
        )

        // Quick bird suggestions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val birdSuggestions = listOf("🦅 Águila", "🦉 Lechuza", "🐦 Canario", "🌿 Colibrí", "🪵 Carpintero", "🦆 Pato", "🦜 Loro", "🕊️ Torcaza", "🦩 Flamenco")
            birdSuggestions.forEach { tag ->
                val cleanTag = tag.substringAfter(" ")
                val isSelected = searchQuery.equals(cleanTag, ignoreCase = true)
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) GeoPrimaryGreen.copy(alpha = 0.2f) else GeoSurfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, if (isSelected) GeoPrimaryGreen else GeoOutline.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable {
                        onUpdateSearchQuery(cleanTag)
                        onTriggerSearchOnline(cleanTag)
                        focusManager.clearFocus()
                    }
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = GeoTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Horizontal Scroll (Geometric Balance Pills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BirdFilter.values().forEach { filter ->
                val selected = activeFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { onSelectFilter(filter) },
                    label = {
                        Text(
                            text = filter.labelEs,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GeoPrimaryGreen,
                        selectedLabelColor = Color.White,
                        containerColor = GeoSurfaceVariant,
                        labelColor = GeoTextSecondary
                    ),
                    border = BorderStroke(1.dp, if (selected) GeoPrimaryGreen else GeoOutline.copy(alpha = 0.15f)),
                    shape = CircleShape,
                    modifier = Modifier.testTag("filter_chip_${filter.name}")
                )
            }
        }
    }
}
