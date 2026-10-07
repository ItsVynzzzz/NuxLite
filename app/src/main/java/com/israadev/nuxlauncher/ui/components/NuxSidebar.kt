package com.israadev.nuxlauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.israadev.nuxlauncher.R
import com.israadev.nuxlauncher.ui.theme.NuxColors
import com.israadev.nuxlauncher.ui.theme.NuxSizes

data class NuxNavItem(
    val id: String,
    val icon: ImageVector,
    val contentDescription: String
)

val NUX_NAV_ITEMS = listOf(
    NuxNavItem("home", Icons.Outlined.Home, "Home"),
    NuxNavItem("accounts", Icons.Outlined.Person, "Akun"),
    NuxNavItem("mods", Icons.Outlined.Extension, "Mod"),
    NuxNavItem("settings", Icons.Outlined.Settings, "Pengaturan")
)

/**
 * Sidebar berbentuk pil putih (tepi tinta + bayangan keras): logo di atas,
 * 4 menu di bawahnya. Tiap menu 44dp supaya mudah disentuh jempol.
 */
@Composable
fun NuxSidebar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NuxCard(
        modifier = modifier
            .width(56.dp)
            .fillMaxHeight(),
        backgroundColor = NuxColors.SurfaceWhite,
        cornerRadius = 28.dp,
        fillMaxHeight = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo (ketuk = kembali ke Home)
            val logoShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(logoShape)
                    .background(NuxColors.Ink, logoShape)
                    .clickable { onTabSelected("home") },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nux_icon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(26.dp)
                )
            }

            // Menu, rata di tengah sisa tinggi
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NUX_NAV_ITEMS.forEach { item ->
                    val isSelected = activeTab == item.id
                    val shape = RoundedCornerShape(16.dp)

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isSelected) NuxColors.Yellow else Color.Transparent,
                                shape = shape
                            )
                            .border(
                                width = NuxSizes.BorderWidth,
                                color = if (isSelected) NuxColors.CardBorder else Color.Transparent,
                                shape = shape
                            )
                            .clip(shape)
                            .clickable { onTabSelected(item.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription,
                            tint = if (isSelected) NuxColors.Ink else NuxColors.GrayNeutral,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
