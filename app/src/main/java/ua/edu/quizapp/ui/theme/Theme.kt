package ua.edu.quizapp.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = BrandIndigoDark,
    secondary = BrandTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = BrandTealDark,
    tertiary = BrandAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = BrandAmberDark,
    background = QuizBackgroundLight,
    onBackground = QuizOnSurfaceLight,
    surface = QuizSurfaceLight,
    onSurface = QuizOnSurfaceLight,
    surfaceVariant = Color(0xFFE8E6F0),
    onSurfaceVariant = Color(0xFF49454F),
    error = QuizError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandIndigoLight,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = BrandIndigoDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = BrandTealLight,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = BrandTealDark,
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = BrandAmberLight,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = BrandAmberDark,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = QuizBackgroundDark,
    onBackground = QuizOnSurfaceDark,
    surface = QuizSurfaceDark,
    onSurface = QuizOnSurfaceDark,
    surfaceVariant = Color(0xFF2D2E42),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = QuizErrorDark,
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

@Composable
fun QuizAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyWishListAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) = QuizAppTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
