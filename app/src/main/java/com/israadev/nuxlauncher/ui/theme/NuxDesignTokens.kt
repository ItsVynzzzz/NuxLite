package com.israadev.nuxlauncher.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * NuxLite Yellow & Black Palette
 * Kuning cerah sebagai warna merek, hitam sebagai tinta (teks, garis tepi, bayangan keras),
 * permukaan putih hangat. Kuning HANYA dipakai sebagai isian (tombol, pill, bingkai),
 * jangan dipakai sebagai warna teks di atas latar terang karena kontrasnya rendah.
 * Nama properti lama dipertahankan sebagai alias supaya kode lain tidak perlu diubah.
 */
object NuxColors {
    // Kuning (isian)
    val Yellow = Color(0xFFFFD60A)       // Warna merek: tombol, pill, bingkai luar
    val YellowSoft = Color(0xFFFFE566)   // Kuning muda: aksen, progres
    val YellowPale = Color(0xFFFFF3B8)   // Kuning pucat: badge, item terpilih, kolom isian
    val YellowMist = Color(0xFFFFF9DB)   // Kuning sangat pucat

    // Hitam / netral
    val Ink = Color(0xFF111111)          // Tinta: teks utama, garis tepi, bayangan
    val Background = Color(0xFFFFFCEF)   // Panel utama (putih hangat)
    val SurfaceWhite = Color(0xFFFFFFFF) // Kartu
    val SurfaceElevated = Color(0xFFFFFFFF) // Dialog dan kartu terangkat
    val SurfaceInput = YellowPale        // Kolom isian
    val DarkGray = Ink                   // Teks utama
    val GrayNeutral = Color(0xFF5B5748)  // Teks pendukung (kontras 7:1 di atas putih)
    val LightGray = Color(0x33111111)    // Garis pemisah (tinta 20%)
    val CardBorder = Ink                 // Garis tepi cartoon
    val ErrorRed = Color(0xFFE11D48)     // Merah galat
    val Amber = Color(0xFFFF8A00)        // Oranye peringatan (dibedakan dari kuning merek)
    val AmberDark = Color(0xFFB45309)
    val SkyBlueDark = Color(0xFF0369A1)
    val PurpleDark = Color(0xFF7E22CE)
    val TextPrimary = Ink
    val SuccessGreen = Color(0xFF16A34A) // Hijau khusus status berhasil/online

    // Alias lama (jangan dihapus, masih dipakai di kode lain)
    val ForestGreen = Yellow             // Dulu hijau utama, sekarang kuning merek
    val MintGreen = YellowSoft
    val SageGreen = Ink                  // Dulu hijau tua untuk teks aksen, sekarang tinta
    val SoftLime = YellowPale
    val LightGreen = YellowMist
    val Mint = YellowPale
    val MintDark = YellowSoft
    val ForestDark = Ink
    val ForestLight = GrayNeutral
    val ForestMuted = GrayNeutral
    val Cream = Background
    val Coral = ErrorRed
    val TextMuted = GrayNeutral
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
