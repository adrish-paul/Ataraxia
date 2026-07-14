package com.example.ataraxia.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun ProfileAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    imageUri: String = "",
    onClick: (() -> Unit)? = null
) {
    val initials = if (name.isNotBlank()) {
        name.trim().take(1).uppercase()
    } else {
        "👤"
    }

    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        if (imageUri.isNotEmpty()) {
            try {
                val uri = android.net.Uri.parse(imageUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                }?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )

    Surface(
        modifier = avatarModifier,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f), // Soft desaturated lavender fill
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials,
                    style = if (size > 60.dp) {
                        MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = DesignTokens.TextPrimary
                        )
                    } else {
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = DesignTokens.TextPrimary
                        )
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
