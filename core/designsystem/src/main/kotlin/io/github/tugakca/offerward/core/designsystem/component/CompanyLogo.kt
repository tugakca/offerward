package io.github.tugakca.offerward.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

/**
 * A company's logo, or its initial on a tinted square while loading, on error, or when the
 * listing has no logo. Decorative: the company name is always shown next to it.
 */
@Composable
fun CompanyLogo(logoUrl: String?, companyName: String, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    val shape = RoundedCornerShape(size / 4)
    val initial: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(size)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = companyName.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
    Box(modifier = modifier.size(size).clip(shape)) {
        if (logoUrl.isNullOrBlank()) {
            initial()
        } else {
            SubcomposeAsyncImage(
                model = logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
                loading = { initial() },
                error = { initial() },
            )
        }
    }
}
