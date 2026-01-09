package com.example.oone.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.oone.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val DotoFont = GoogleFont("Doto")
val SpaceMono = GoogleFont("Space Mono")

val DotoFontFamily = FontFamily(
    Font(googleFont = DotoFont, fontProvider = provider),
)

val SpaceMonoFontFamily = FontFamily(
    Font(googleFont = SpaceMono, fontProvider = provider),
)

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = DotoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DotoFontFamily,
        fontSize = 20.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontSize = 14.sp
    )
)

