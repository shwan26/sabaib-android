package com.smnc.sabaib.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SabaiYellow,
    onPrimary = SabaiBlack,
    primaryContainer = SabaiYellowDark,
    onPrimaryContainer = SabaiWhite,

    secondary = SabaiNavyLight,
    onSecondary = SabaiWhite,
    secondaryContainer = SabaiNavy,
    onSecondaryContainer = SabaiYellowLight,

    tertiary = SabaiBeakOrange,
    onTertiary = SabaiBlack,
    tertiaryContainer = SabaiNavyDark,
    onTertiaryContainer = SabaiYellowLight,

    background = SabaiBlack,
    onBackground = SabaiOffWhite,

    surface = SabaiCharcoal,
    onSurface = SabaiOffWhite,
    surfaceVariant = SabaiNavyDark,
    onSurfaceVariant = SabaiLightGray,

    outline = SabaiGray,

    error = SabaiErrorLight,
    onError = SabaiBlack,
)

private val LightColorScheme = lightColorScheme(
    primary = SabaiNavy,
    onPrimary = SabaiWhite,
    primaryContainer = SabaiYellow,
    onPrimaryContainer = SabaiNavyDark,

    secondary = SabaiYellow,
    onSecondary = SabaiBlack,
    secondaryContainer = SabaiYellowLight,
    onSecondaryContainer = SabaiNavyDark,

    tertiary = SabaiBeakOrange,
    onTertiary = SabaiBlack,
    tertiaryContainer = SabaiYellowLight,
    onTertiaryContainer = SabaiNavyDark,

    background = SabaiOffWhite,
    onBackground = SabaiBlack,

    surface = SabaiWhite,
    onSurface = SabaiBlack,
    surfaceVariant = SabaiLightGray,
    onSurfaceVariant = SabaiCharcoal,

    outline = SabaiGray,

    error = SabaiError,
    onError = SabaiWhite,
)

@Composable
fun SabaiBTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color defaults to false so the brand palette (yellow/navy/white)
    // is always used instead of the device wallpaper-derived colors.
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}