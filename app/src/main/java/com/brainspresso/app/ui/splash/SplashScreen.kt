package com.brainspresso.app.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.brainspresso.app.R

/** Brand splash: logo, name, tagline, and the maker credit at the bottom. */
@Composable
fun SplashScreen(modifier: Modifier = Modifier, animate: Boolean = true) {
    var revealed by remember { mutableStateOf(!animate) }
    LaunchedEffect(Unit) { revealed = true }

    val progress by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "splash-reveal",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(420.dp)
                .background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f), Color.Transparent),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val logoShape = RoundedCornerShape(44.dp)
            Image(
                painter = painterResource(R.drawable.brand_logo),
                contentDescription = "Brainspresso",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(176.dp)
                    .scale(0.85f + 0.15f * progress)
                    .alpha(progress)
                    .shadow(
                        elevation = 24.dp,
                        shape = logoShape,
                        ambientColor = MaterialTheme.colorScheme.primaryContainer,
                        spotColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                    .clip(logoShape),
            )
            Text(
                text = "Brainspresso",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 28.dp)
                    .alpha(progress),
            )
            Text(
                text = "Never sleep in again.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(progress),
            )
        }
        Text(
            text = "Made by Ajay Chandar",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(progress),
        )
    }
}
