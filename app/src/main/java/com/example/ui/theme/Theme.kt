package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = GeoPrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = GeoPrimaryContainer,
    onPrimaryContainer = GeoOnPrimaryContainer,
    secondary = GeoAmber,
    onSecondary = Color.White,
    secondaryContainer = GeoSurfaceVariant,
    onSecondaryContainer = GeoTextPrimary,
    tertiary = SkyBluePrimary,
    onTertiary = Color.White,
    tertiaryContainer = SkyBlueLight,
    onTertiaryContainer = Color(0xFF003544),
    background = GeoBackground,
    onBackground = GeoTextPrimary,
    surface = GeoSurface,
    onSurface = GeoTextPrimary,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = GeoTextSecondary,
    outline = GeoOutline.copy(alpha = 0.25f),
    outlineVariant = GeoOutline.copy(alpha = 0.12f),
    error = IucnCriticallyEndangered,
    onError = Color.White,
    errorContainer = GeoVulnerableBg,
    onErrorContainer = GeoVulnerableText
)

private val DarkColorScheme = darkColorScheme(
    primary = SageGreenLight,
    onPrimary = ForestGreenDark,
    primaryContainer = GeoPrimaryGreen,
    onPrimaryContainer = GeoPrimaryContainer,
    secondary = WarmOchre,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = GeoAmber,
    onSecondaryContainer = Color(0xFFFFE8D6),
    tertiary = Color(0xFF48CAE4),
    onTertiary = Color(0xFF003544),
    background = Color(0xFF121411),
    onBackground = Color(0xFFE2E4DC),
    surface = Color(0xFF191C18),
    onSurface = Color(0xFFE2E4DC),
    surfaceVariant = Color(0xFF282C25),
    onSurfaceVariant = Color(0xFFC4C8B8),
    outline = Color(0xFF53584E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
