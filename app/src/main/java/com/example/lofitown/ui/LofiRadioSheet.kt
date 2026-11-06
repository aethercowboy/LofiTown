package com.example.lofitown.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lofitown.audio.LofiTrack
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
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LofiRadioSheet(
    viewModel: TownViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPlaying by viewModel.audioEngine.isPlaying.collectAsState()
    val currentTrackIdx by viewModel.audioEngine.currentTrackIndex.collectAsState()
    val musicVol by viewModel.audioEngine.musicVolume.collectAsState()
    val rainVol by viewModel.audioEngine.rainVolume.collectAsState()
    val vinylVol by viewModel.audioEngine.vinylVolume.collectAsState()
    val amplitudes by viewModel.audioEngine.visualizerAmplitudes.collectAsState()

    val currentTrack = viewModel.audioEngine.currentTrack

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = "Radio",
                        tint = LofiPeach,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LO-FI CASSETTE DECK",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 15.sp,
                        color = LofiCream
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_radio_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = LofiMutedText)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Retro Cassette Illustration
            CassetteTapeWidget(
                isPlaying = isPlaying,
                trackTitle = currentTrack.title,
                keySignature = currentTrack.key,
                bpm = currentTrack.bpm
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Visualizer Spectrum Bars
            VisualizerBars(amplitudes = amplitudes)

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.audioEngine.previousTrack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("prev_track_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = LofiCream,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(LofiPeach)
                        .clickable { viewModel.audioEngine.togglePlayPause() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF2B1810),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { viewModel.audioEngine.nextTrack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("next_track_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = LofiCream,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ambient Mixer Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = LofiCardBg
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AMBIENT SOUND MIXER",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 11.sp,
                        color = LofiMutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Music volume
                    MixerSliderRow(
                        label = "Melody Chords",
                        value = musicVol,
                        accentColor = LofiPeach,
                        onValueChange = { viewModel.audioEngine.setMusicVolume(it) }
                    )

                    // Rain volume
                    MixerSliderRow(
                        label = "Gentle Rain",
                        value = rainVol,
                        accentColor = LofiMatcha,
                        onValueChange = { viewModel.audioEngine.setRainVolume(it) }
                    )

                    // Vinyl volume
                    MixerSliderRow(
                        label = "Vinyl Crackle",
                        value = vinylVol,
                        accentColor = LofiLavender,
                        onValueChange = { viewModel.audioEngine.setVinylVolume(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tracklist Section
            Text(
                text = "TAPE CASSETTES",
                fontFamily = SilkscreenFontFamily,
                fontSize = 11.sp,
                color = LofiMutedText,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                itemsIndexed(LofiTrack.ALL_TRACKS) { index, track ->
                    val isCurrent = index == currentTrackIdx
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) LofiSurfaceVariant else Color.Transparent)
                            .clickable { viewModel.audioEngine.selectTrack(index) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "A-${index + 1}",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 11.sp,
                                color = if (isCurrent) LofiPeach else LofiMutedText
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = track.title,
                                    fontSize = 13.sp,
                                    color = if (isCurrent) LofiCream else LofiMutedText
                                )
                                Text(
                                    text = "${track.key} • ${track.bpm} BPM",
                                    fontSize = 10.sp,
                                    color = LofiMutedText.copy(alpha = 0.7f)
                                )
                            }
                        }
                        if (isCurrent && isPlaying) {
                            Text(
                                text = "PLAYING",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 9.sp,
                                color = LofiMatcha
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CassetteTapeWidget(
    isPlaying: Boolean,
    trackTitle: String,
    keySignature: String,
    bpm: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tape_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spool_rotation"
    )

    val currentRotation = if (isPlaying) rotation else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1728))
            .border(2.dp, Color(0xFF382947), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val w = size.width
            val h = size.height

            // Cassette body outer bevel
            drawRoundRect(
                color = Color(0xFF2C2238),
                topLeft = Offset(12f, 10f),
                size = Size(w - 24f, h - 20f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Cassette paper label
            drawRoundRect(
                color = Color(0xFFF7EBE1),
                topLeft = Offset(30f, 20f),
                size = Size(w - 60f, h - 40f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Center transparent window
            val windowW = 150f
            val windowH = 46f
            val winLeft = (w - windowW) / 2f
            val winTop = (h - windowH) / 2f

            drawRoundRect(
                color = Color(0xFF140F1B),
                topLeft = Offset(winLeft, winTop),
                size = Size(windowW, windowH),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Two tape spools
            val spoolRadius = 14f
            val leftSpoolX = winLeft + 36f
            val rightSpoolX = winLeft + windowW - 36f
            val spoolY = winTop + windowH / 2f

            // Left Spool
            drawCircle(Color(0xFFEDE8E3), spoolRadius, Offset(leftSpoolX, spoolY))
            drawCircle(Color(0xFF140F1B), 6f, Offset(leftSpoolX, spoolY))
            // Spool spokes rotating
            val rad = Math.toRadians(currentRotation.toDouble())
            for (spoke in 0..2) {
                val ang = rad + spoke * (2 * Math.PI / 3)
                val sx = leftSpoolX + cos(ang).toFloat() * 10f
                val sy = spoolY + sin(ang).toFloat() * 10f
                drawLine(Color(0xFF888492), Offset(leftSpoolX, spoolY), Offset(sx, sy), 2f)
            }

            // Right Spool
            drawCircle(Color(0xFFEDE8E3), spoolRadius, Offset(rightSpoolX, spoolY))
            drawCircle(Color(0xFF140F1B), 6f, Offset(rightSpoolX, spoolY))
            for (spoke in 0..2) {
                val ang = rad + spoke * (2 * Math.PI / 3)
                val sx = rightSpoolX + cos(ang).toFloat() * 10f
                val sy = spoolY + sin(ang).toFloat() * 10f
                drawLine(Color(0xFF888492), Offset(rightSpoolX, spoolY), Offset(sx, sy), 2f)
            }

            // Magnetic tape ribbon connecting the spools
            drawLine(
                color = Color(0xFF4A3828),
                start = Offset(leftSpoolX, spoolY + spoolRadius),
                end = Offset(rightSpoolX, spoolY + spoolRadius),
                strokeWidth = 3f
            )
        }

        // Title text positioned on cassette label
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = trackTitle,
                fontFamily = SilkscreenFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF2B1810)
            )
            Text(
                text = "$keySignature  •  $bpm BPM",
                fontSize = 9.sp,
                color = Color(0xFF6E5642)
            )
        }
    }
}

@Composable
fun VisualizerBars(amplitudes: List<Float>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        amplitudes.forEachIndexed { index, amp ->
            val barColor = when (index % 3) {
                0 -> LofiPeach
                1 -> LofiMatcha
                else -> LofiLavender
            }
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height((24.dp * amp.coerceIn(0.12f, 1f)))
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun MixerSliderRow(
    label: String,
    value: Float,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = LofiCream,
            modifier = Modifier.width(105.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = LofiSurfaceVariant
            )
        )
        Text(
            text = "${(value * 100).toInt()}%",
            fontFamily = SilkscreenFontFamily,
            fontSize = 10.sp,
            color = LofiMutedText,
            modifier = Modifier.width(36.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
