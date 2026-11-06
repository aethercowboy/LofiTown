package com.example.lofitown.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lofitown.model.BuildingCategory
import com.example.lofitown.model.BuildingType
import com.example.lofitown.model.TerrainType
import com.example.ui.theme.LofiCardBg
import com.example.ui.theme.LofiCoral
import com.example.ui.theme.LofiCream
import com.example.ui.theme.LofiLavender
import com.example.ui.theme.LofiMatcha
import com.example.ui.theme.LofiMutedText
import com.example.ui.theme.LofiPeach
import com.example.ui.theme.LofiSurface
import com.example.ui.theme.LofiSurfaceVariant
import com.example.ui.theme.LofiYellowGlow
import com.example.ui.theme.SilkscreenFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildMenuSheet(
    viewModel: TownViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedTilePos by viewModel.selectedTile.collectAsState()
    val tiles by viewModel.tiles.collectAsState()
    val currentVibes by viewModel.vibes.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Establishments", "Nature & Decor", "Paths & River")

    val selectedTile = selectedTilePos?.let { (x, y) ->
        tiles.firstOrNull { it.x == x && it.y == y }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LofiSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COZY TOWN BUILDER",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 15.sp,
                        color = LofiCream
                    )
                    Text(
                        text = "Balance: ♫ ${currentVibes.toLong()} Vibes",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 11.sp,
                        color = LofiPeach
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_build_menu")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = LofiMutedText)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active tile banner or selection prompt
            if (selectedTile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LofiSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Plot (${selectedTile.x}, ${selectedTile.y})",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 11.sp,
                                color = LofiPeach
                            )
                            if (selectedTile.building != null) {
                                Text(
                                    text = "Level ${selectedTile.level}",
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 10.sp,
                                    color = LofiYellowGlow
                                )
                            }
                        }

                        if (selectedTile.building != null) {
                            val building = selectedTile.building
                            val upCost = building.upgradeCost(selectedTile.level)
                            val currentRate = building.vibesPerSecForLevel(selectedTile.level)
                            val nextRate = building.vibesPerSecForLevel(selectedTile.level + 1)

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = building.displayName,
                                fontSize = 14.sp,
                                color = LofiCream
                            )
                            Text(
                                text = "Current generation: +${String.format("%.1f", currentRate)}/s",
                                fontSize = 12.sp,
                                color = LofiMatcha
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.upgradeSelectedBuilding() },
                                    enabled = currentVibes >= upCost,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LofiPeach,
                                        contentColor = Color(0xFF2B1810)
                                    ),
                                    modifier = Modifier.testTag("upgrade_building_button")
                                ) {
                                    Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Upgrade for ♫ $upCost (+${String.format("%.1f", nextRate - currentRate)}/s)",
                                        fontFamily = SilkscreenFontFamily,
                                        fontSize = 10.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.demolishSelectedBuilding() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LofiCoral),
                                    modifier = Modifier.testTag("demolish_building_button")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", fontFamily = SilkscreenFontFamily, fontSize = 10.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Current: ${selectedTile.terrain.title} (Ready to build!)",
                                fontSize = 12.sp,
                                color = LofiMutedText
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF20192A))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tip: Tap any tile on the town grid to select where to build!",
                        fontSize = 11.sp,
                        color = LofiMutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab bar
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = LofiSurface,
                contentColor = LofiPeach,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 10.sp,
                                color = if (selectedTabIndex == index) LofiPeach else LofiMutedText
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Catalog list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // Establishments
                        items(BuildingType.entries) { building ->
                            BuildingCatalogCard(
                                building = building,
                                canAfford = currentVibes >= building.cost,
                                hasTileSelected = selectedTile != null && selectedTile.building == null,
                                onBuild = { viewModel.buildOnSelectedTile(building) }
                            )
                        }
                    }
                    1 -> {
                        // Nature & Decor
                        val decorList = listOf(
                            TerrainType.SAKURA_TREE,
                            TerrainType.LAMP_POST,
                            TerrainType.PARK_BENCH
                        )
                        items(decorList) { terrain ->
                            TerrainCatalogCard(
                                terrain = terrain,
                                canAfford = currentVibes >= terrain.cost,
                                hasTileSelected = selectedTile != null,
                                onBuild = { viewModel.setTerrainOnSelectedTile(terrain) }
                            )
                        }
                    }
                    2 -> {
                        // Paths & River
                        val pathList = listOf(
                            TerrainType.COBBLESTONE,
                            TerrainType.RIVER,
                            TerrainType.BRIDGE,
                            TerrainType.MEADOW
                        )
                        items(pathList) { terrain ->
                            TerrainCatalogCard(
                                terrain = terrain,
                                canAfford = currentVibes >= terrain.cost,
                                hasTileSelected = selectedTile != null,
                                onBuild = { viewModel.setTerrainOnSelectedTile(terrain) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuildingCatalogCard(
    building: BuildingType,
    canAfford: Boolean,
    hasTileSelected: Boolean,
    onBuild: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LofiCardBg)
            .border(1.dp, Color(0xFF382947), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(building.accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = building.displayName,
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 12.sp,
                        color = LofiCream
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${building.vibesPerSec}/s",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 10.sp,
                        color = LofiMatcha
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = building.description,
                    fontSize = 11.sp,
                    color = LofiMutedText,
                    lineHeight = 15.sp
                )
                Text(
                    text = "\"${building.ambientQuote}\"",
                    fontSize = 10.sp,
                    color = LofiLavender.copy(alpha = 0.8f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onBuild,
                enabled = canAfford && hasTileSelected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LofiPeach,
                    contentColor = Color(0xFF2B1810),
                    disabledContainerColor = Color(0xFF352B42),
                    disabledContentColor = LofiMutedText.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("build_${building.id}_button")
            ) {
                Text(
                    text = "♫ ${building.cost}",
                    fontFamily = SilkscreenFontFamily,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun TerrainCatalogCard(
    terrain: TerrainType,
    canAfford: Boolean,
    hasTileSelected: Boolean,
    onBuild: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LofiCardBg)
            .border(1.dp, Color(0xFF382947), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = terrain.title,
                    fontFamily = SilkscreenFontFamily,
                    fontSize = 12.sp,
                    color = LofiCream
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = terrain.description,
                    fontSize = 11.sp,
                    color = LofiMutedText
                )
            }

            Button(
                onClick = onBuild,
                enabled = canAfford && hasTileSelected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LofiMatcha,
                    contentColor = Color(0xFF102818),
                    disabledContainerColor = Color(0xFF352B42),
                    disabledContentColor = LofiMutedText.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("place_${terrain.name.lowercase()}_button")
            ) {
                Text(
                    text = if (terrain.cost > 0) "♫ ${terrain.cost}" else "Free",
                    fontFamily = SilkscreenFontFamily,
                    fontSize = 11.sp
                )
            }
        }
    }
}
