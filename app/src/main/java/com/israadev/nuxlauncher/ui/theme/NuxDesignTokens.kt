package com.israadev.nuxlauncher.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * NuxLite Cartoon Minimal Palette
 * Latar krem hangat, kartu putih, garis tepi tebal warna tinta, aksen hijau permen.
 * Semua nama properti sama dengan versi lama supaya kode lain tidak perlu diubah.
 */
object NuxColors {
    // Aksen utama (hijau permen)
    val ForestGreen = Color(0xFF2EC46A) // Warna utama: tombol, kursor
    val MintGreen = Color(0xFF86EFAC)   // Aksen terang
    val SageGreen = Color(0xFF15803D)   // Hijau tua untuk teks aksen
    val SoftLime = Color(0xFFD7F7E0)    // Wadah hijau muda (badge, chip)
    val LightGreen = Color(0xFFE6F9EC)  // Wadah hijau paling muda

    // Netral hangat
    val Background = Color(0xFFFFF6E5)      // Krem hangat
    val SurfaceWhite = Color(0xFFFFFFFF)    // Kartu
    val SurfaceElevated = Color(0xFFFFFDF8) // Dialog dan kartu terangkat
    val SurfaceInput = Color(0xFFFFEFCF)    // Kolom isian
    val DarkGray = Color(0xFF1F1B2E)        // Teks utama (tinta)
    val GrayNeutral = Color(0xFF6B6578)     // Teks pendukung
    val LightGray = Color(0x331F1B2E)       // Garis pemisah (tinta 20%)
    val CardBorder = Color(0xFF1F1B2E)      // Garis tepi cartoon (tinta penuh)
    val ErrorRed = Color(0xFFE11D48)        // Merah galat
    val Amber = Color(0xFFFFB400)           // Kuning peringatan
    val AmberDark = Color(0xFFB45309)       // Amber gelap untuk teks di latar terang
    val SkyBlueDark = Color(0xFF0369A1)     // Biru gelap untuk teks di latar terang
    val PurpleDark = Color(0xFF7E22CE)      // Ungu gelap untuk teks di latar terang
    val TextPrimary = Color(0xFF1F1B2E)     // Sama dengan tinta

    // Alias lama (jangan dihapus, masih dipakai di kode lain)
    val Mint = SoftLime
    val MintDark = MintGreen
    val ForestDark = DarkGray
    val ForestLight = GrayNeutral
    val ForestMuted = GrayNeutral
    val Cream = Background
    val Coral = ErrorRed
    val TextMuted = GrayNeutral
    val SuccessGreen = ForestGreen
    val SkyBlue = Color(0xFF2AA9F0)
}

object NuxSizes {
    val BorderWidth = 2.dp
    val CornerRadius = 20.dp
    val CornerRadiusLarge = 28.dp
    val CornerRadiusSmall = 14.dp
    val ShadowOffset = 3.dp
    val BorderDefault = BorderStroke(BorderWidth, NuxColors.CardBorder)
    val ShapeDefault = RoundedCornerShape(CornerRadius)
    val ShapeLarge = RoundedCornerShape(CornerRadiusLarge)
    val ShapeSmall = RoundedCornerShape(CornerRadiusSmall)
}
