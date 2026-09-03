package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConservationStatus
import com.example.ui.theme.GeoVulnerableBg
import com.example.ui.theme.GeoVulnerableBorder
import com.example.ui.theme.GeoVulnerableText

@Composable
fun ConservationBadge(
    status: ConservationStatus,
    modifier: Modifier = Modifier,
    showWhenSafe: Boolean = false
) {
    if (!status.isThreatened && !showWhenSafe) {
        return
    }

    val isThreatened = status.isThreatened
    val bgColor = if (isThreatened) GeoVulnerableBg else status.color.copy(alpha = 0.15f)
    val textColor = if (isThreatened) GeoVulnerableText else status.color
    val borderColor = if (isThreatened) GeoVulnerableBorder else status.color.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isThreatened) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Estado de amenaza",
                    tint = textColor,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = status.labelEs.uppercase(),
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

