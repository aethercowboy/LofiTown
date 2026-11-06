package com.example.lofitown.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lofitown.model.TimeOfDay
import com.example.ui.theme.LofiCardBg
import com.example.ui.theme.LofiCoral
import com.example.ui.theme.LofiCream
import com.example.ui.theme.LofiDuskBg
import com.example.ui.theme.LofiLavender
import com.example.ui.theme.LofiMatcha
import com.example.ui.theme.LofiMutedText
import com.example.ui.theme.LofiPeach
import com.example.ui.theme.LofiSurface
import com.example.ui.theme.LofiSurfaceVariant
import com.example.ui.theme.LofiYellowGlow
import com.example.ui.theme.SilkscreenFontFamily
import kotlinx.coroutines.delay
import kotlin.math.hypot

@Composable
fun LofiTownScreen(viewModel: TownViewModel) {
    val tiles by viewModel.tiles.collectAsState()
    val citizens by viewModel.citizens.collectAsState()
    val selectedTile by viewModel.selectedTile.collectAsState()
    val vibes by viewModel.vibes.collectAsState()
    val zenStars by viewModel.zenStars.collectAsState()
    val minuteOfDay by viewModel.minuteOfDay.collectAsState()
    val timeOfDay by viewModel.timeOfDay.collectAsState()
    val timeSpeed by viewModel.timeSpeed.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val activeThought by viewModel.activeThought.collectAsState()
    val offlineReward by viewModel.offlineReward.collectAsState()

    val isBuildMenuOpen by viewModel.isBuildMenuOpen.collectAsState()
    val isRadioSheetOpen by viewModel.isRadioSheetOpen.collectAsState()
    val isJournalOpen by viewModel.isJournalOpen.collectAsState()
    val isFocusDialogOpen by viewModel.isFocusDialogOpen.collectAsState()
    val isZenMode by viewModel.isZenMode.collectAsState()

    val isMusicPlaying by viewModel.audioEngine.isPlaying.collectAsState()
    val currentTrack = viewModel.audioEngine.currentTrack
    val totalRate = viewModel.totalVibesPerSec

    // Animation ticker for pixel art shaders, ripples, and particles
    var animTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L) // ~60fps smooth loop
            animTime += 0.016f
        }
    }

    // Camera pan and zoom
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(timeOfDay.skyTop, timeOfDay.skyBottom)
                )
            )
    ) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()
        val defaultOriginX = screenW / 2f + panOffsetX
        val defaultOriginY = screenH / 2.7f + panOffsetY

        // 1. Interactive Town Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                        zoomScale = (zoomScale * zoom).coerceIn(0.7f, 1.8f)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val tapX = (tapOffset.x - defaultOriginX) / zoomScale + defaultOriginX
                        val tapY = (tapOffset.y - defaultOriginY) / zoomScale + defaultOriginY

                        // Check if tap hit a citizen
                        var citizenTapped = false
                        for (citizen in citizens) {
                            val cScreen = PixelRenderer.gridToScreen(
                                citizen.gridX,
                                citizen.gridY,
                                defaultOriginX,
                                defaultOriginY
                            )
                            val dist = hypot((tapX - cScreen.x).toDouble(), (tapY - cScreen.y).toDouble())
                            if (dist < 32f) {
                                viewModel.tapCitizen(citizen)
                                citizenTapped = true
                                break
                            }
                        }

                        if (!citizenTapped) {
                            val (gx, gy) = PixelRenderer.screenToGrid(
                                tapX,
                                tapY,
                                defaultOriginX,
                                defaultOriginY
                            )
                            if (gx in 0 until 6 && gy in 0 until 6) {
                                viewModel.selectTile(gx, gy)
                            } else {
                                viewModel.clearSelection()
                            }
                        }
                    }
                }
        ) {
            // Scale and draw scene
            withTransform({
                scale(scaleX = zoomScale, scaleY = zoomScale, pivot = Offset(defaultOriginX, defaultOriginY))
            }) {
                PixelRenderer.drawTownScene(
                    scope = this,
                    tiles = tiles,
                    citizens = citizens,
                    selectedTile = selectedTile,
                    timeOfDay = timeOfDay,
                    weather = weather,
                    animTime = animTime,
                    originX = defaultOriginX,
                    originY = defaultOriginY
                )
            }
        }

        // 2. HUD: Top Bar (Hidden in Zen Mode)
        AnimatedVisibility(
            visible = !isZenMode,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time and Atmosphere pill button
                    val hour = (minuteOfDay / 60) % 24
                    val min = minuteOfDay % 60
                    val timeStr = String.format("%02d:%02d", hour, min)

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LofiSurface.copy(alpha = 0.92f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382947)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.cycleTimeSpeed() }
                            .testTag("time_speed_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (timeOfDay) {
                                    TimeOfDay.DAWN -> "🌅"
                                    TimeOfDay.DAY -> "☀️"
                                    TimeOfDay.DUSK -> "🌇"
                                    TimeOfDay.NIGHT -> "🌙"
                                },
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$timeStr ${timeOfDay.title}",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 10.sp,
                                color = LofiCream
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LofiSurfaceVariant)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = timeSpeed.title,
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 8.sp,
                                    color = LofiPeach
                                )
                            }
                        }
                    }

                    // Weather toggle pill button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LofiSurface.copy(alpha = 0.92f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382947)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.cycleWeather() }
                            .testTag("weather_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = weather.icon, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = weather.title,
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 10.sp,
                                color = LofiCream
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Resource counters row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vibes & Rate
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LofiCardBg.copy(alpha = 0.94f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382947))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "♫",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 13.sp,
                                color = LofiPeach
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${vibes.toLong()} Vibes",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 12.sp,
                                color = LofiCream
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(+${String.format("%.1f", totalRate)}/s)",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 10.sp,
                                color = LofiMatcha
                            )
                        }
                    }

                    // Zen Stars & Zen Mode Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LofiCardBg.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382947))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "★",
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 12.sp,
                                    color = LofiYellowGlow
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$zenStars",
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 11.sp,
                                    color = LofiCream
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Zen / Photo Mode toggle
                        IconButton(
                            onClick = { viewModel.toggleZenMode() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LofiSurface.copy(alpha = 0.92f))
                                .border(1.dp, Color(0xFF382947), CircleShape)
                                .testTag("zen_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Screensaver Mode",
                                tint = LofiPeach,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Camera zoom buttons (Left edge)
        AnimatedVisibility(
            visible = !isZenMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { zoomScale = (zoomScale + 0.15f).coerceAtMost(1.8f) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LofiSurface.copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF382947), CircleShape)
                        .testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = LofiCream, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.7f) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LofiSurface.copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF382947), CircleShape)
                        .testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = LofiCream, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomScale = 1.0f
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LofiSurface.copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF382947), CircleShape)
                        .testTag("recenter_camera_button")
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Recenter", tint = LofiPeach, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 3. Citizen Thought Speech Bubble
        activeThought?.let { thoughtData ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LofiSurface.copy(alpha = 0.96f))
                    .border(1.5.dp, LofiPeach, RoundedCornerShape(14.dp))
                    .clickable { viewModel.dismissThought() }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (thoughtData.citizen.isCat) "🐱" else "☕",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = thoughtData.citizen.name,
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 11.sp,
                                color = LofiPeach
                            )
                            Text(
                                text = " • ${thoughtData.citizen.role}",
                                fontSize = 10.sp,
                                color = LofiMutedText
                            )
                        }
                        Text(
                            text = "+5 Vibes",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 9.sp,
                            color = LofiMatcha
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${thoughtData.thought}\"",
                        fontSize = 12.sp,
                        color = LofiCream,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // 4. In Zen Mode: subtle exit button in corner
        if (isZenMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleZenMode() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LofiSurface.copy(alpha = 0.85f))
                        .testTag("exit_zen_mode_button")
                ) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = "Exit Screensaver", tint = LofiPeach)
                }
            }
        }

        // 5. Bottom HUD: Cassette Mini-Player & Action Buttons
        AnimatedVisibility(
            visible = !isZenMode,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Cassette Mini-Player Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LofiSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF382947)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.setRadioSheetOpen(true) }
                        .testTag("mini_player_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Spinning tape icon
                            MiniCassetteIcon(isPlaying = isMusicPlaying)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentTrack.title,
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 11.sp,
                                    color = LofiCream
                                )
                                Text(
                                    text = "${currentTrack.key} • Tap for Radio Deck",
                                    fontSize = 10.sp,
                                    color = LofiMutedText
                                )
                            }
                        }

                        // Play/Pause quick toggle
                        IconButton(
                            onClick = { viewModel.audioEngine.togglePlayPause() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(LofiPeach)
                                .testTag("mini_play_button")
                        ) {
                            Icon(
                                imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isMusicPlaying) "Pause" else "Play",
                                tint = Color(0xFF2B1810),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Journal / Milestones Button
                    ActionButton(
                        icon = Icons.Default.MenuBook,
                        label = "Journal",
                        tag = "open_journal_button",
                        accentColor = LofiLavender,
                        onClick = { viewModel.setJournalOpen(true) }
                    )

                    // Chill Study / Focus Timer Button
                    ActionButton(
                        icon = Icons.Default.HourglassBottom,
                        label = "Study",
                        tag = "open_focus_button",
                        accentColor = LofiMatcha,
                        onClick = { viewModel.setFocusDialogOpen(true) }
                    )

                    // Main Build FAB Button
                    FloatingActionButton(
                        onClick = { viewModel.setBuildMenuOpen(true) },
                        containerColor = LofiPeach,
                        contentColor = Color(0xFF2B1810),
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("open_build_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Build", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Build & Place",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Modals & Sheets
        if (isBuildMenuOpen) {
            BuildMenuSheet(viewModel = viewModel, onDismiss = { viewModel.setBuildMenuOpen(false) })
        }

        if (isRadioSheetOpen) {
            LofiRadioSheet(viewModel = viewModel, onDismiss = { viewModel.setRadioSheetOpen(false) })
        }

        if (isJournalOpen) {
            JournalDialog(viewModel = viewModel, onDismiss = { viewModel.setJournalOpen(false) })
        }

        if (isFocusDialogOpen) {
            FocusTimerDialog(viewModel = viewModel, onDismiss = { viewModel.setFocusDialogOpen(false) })
        }

        offlineReward?.let { reward ->
            OfflineProgressDialog(
                reward = reward,
                onCollect = { viewModel.dismissOfflineReward() }
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tag: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = LofiSurface.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382947)),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = SilkscreenFontFamily,
                fontSize = 11.sp,
                color = LofiCream
            )
        }
    }
}

@Composable
fun MiniCassetteIcon(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_tape")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mini_spool"
    )

    val currentRot = if (isPlaying) rotation else 0f

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF241A30))
            .border(1.dp, Color(0xFF382947), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val r = size.width / 2f
            drawCircle(Color(0xFFEDE8E3), 6f, Offset(r - 5f, r))
            drawCircle(Color(0xFFEDE8E3), 6f, Offset(r + 5f, r))
            drawCircle(Color(0xFF140F1B), 2.5f, Offset(r - 5f, r))
            drawCircle(Color(0xFF140F1B), 2.5f, Offset(r + 5f, r))
        }
    }
}
