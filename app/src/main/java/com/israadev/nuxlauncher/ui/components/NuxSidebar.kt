package com.israadev.nuxlauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.israadev.nuxlauncher.ui.theme.resp

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
 * Sidebar kecil bergaya cartoon minimalis: logo di atas, 4 menu di bawahnya.
 */
@Composable
fun NuxSidebar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .width((52.dp).resp())
                .fillMaxHeight()
                .background(NuxColors.Background)
                .padding(top = (6.dp).resp(), bottom = (8.dp).resp(), start = (3.dp).resp(), end = (3.dp).resp()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            val logoShape = RoundedCornerShape((8.dp).resp())
            Box(
                modifier = Modifier
                    .size((32.dp).resp())
                    .clip(logoShape)
                    .background(NuxColors.SurfaceElevated)
                    .border(NuxSizes.BorderWidth, NuxColors.CardBorder, logoShape)
                    .clickable { onTabSelected("home") },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nux_icon),
                    contentDescription = "Logo",
                    modifier = Modifier.size((20.dp).resp())
                )
            }

            Spacer(modifier = Modifier.height((8.dp).resp()))

            // Menu
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy((6.dp).resp()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NUX_NAV_ITEMS.forEach { item ->
                    val isSelected = activeTab == item.id
                    val shape = RoundedCornerShape((10.dp).resp())

                    Box(
                        modifier = Modifier
                            .size((36.dp).resp())
                            .background(
                                color = if (isSelected) NuxColors.SoftLime else Color.Transparent,
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
                            tint = if (isSelected) NuxColors.DarkGray else NuxColors.GrayNeutral,
                            modifier = Modifier.size((18.dp).resp())
                        )
                    }
                }
            }
        }

        // Garis pemisah kanan
        Box(
            modifier = Modifier
                .width(NuxSizes.BorderWidth)
                .fillMaxHeight()
                .background(NuxColors.CardBorder)
        )
    }
}
