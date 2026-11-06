package com.example.lofitown.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LofiCardBg
import com.example.ui.theme.LofiCoral
import com.example.ui.theme.LofiCream
import com.example.ui.theme.LofiLavender
import com.example.ui.theme.LofiMatcha
import com.example.ui.theme.LofiMutedText
import com.example.ui.theme.LofiPeach
import com.example.ui.theme.LofiSurface
import com.example.ui.theme.LofiYellowGlow
import com.example.ui.theme.SilkscreenFontFamily

@Composable
fun FocusTimerDialog(
    viewModel: TownViewModel,
    onDismiss: () -> Unit
) {
    val isFocusActive by viewModel.isFocusActive.collectAsState()
    val secondsLeft by viewModel.focusSecondsRemaining.collectAsState()
    var selectedMinutes by remember { mutableIntStateOf(25) }

    val infiniteTransition = rememberInfiniteTransition(label = "steam")
    val steamAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "steam_glow"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LofiSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF382947), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = LofiPeach,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHILL STUDY TIMER",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 13.sp,
                            color = LofiCream
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_focus_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LofiMutedText)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Steaming coffee icon box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(LofiCardBg)
                        .border(1.dp, LofiPeach.copy(alpha = steamAlpha), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = "Coffee",
                        tint = LofiPeach,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isFocusActive) {
                    val mins = secondsLeft / 60
                    val secs = secondsLeft % 60
                    val formatted = String.format("%02d:%02d", mins, secs)

                    Text(
                        text = formatted,
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 32.sp,
                        color = LofiYellowGlow
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Take a breath. Time flows gently.",
                        fontSize = 12.sp,
                        color = LofiMutedText
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.cancelFocusSession() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LofiCoral,
                            contentColor = Color(0xFF2B1810)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("cancel_focus_button")
                    ) {
                        Text("End Session Early", fontFamily = SilkscreenFontFamily, fontSize = 11.sp)
                    }
                } else {
                    Text(
                        text = "Select Study Session",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 12.sp,
                        color = LofiCream
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Listen to calm lofi while working or reading. Your town gently earns bonus Zen Stars when completed.",
                        fontSize = 11.sp,
                        color = LofiMutedText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 25, 45).forEach { min ->
                            val isSel = selectedMinutes == min
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) LofiPeach else LofiCardBg)
                                    .clickable { selectedMinutes = min }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$min Min",
                                    fontFamily = SilkscreenFontFamily,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color(0xFF2B1810) else LofiCream
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.startFocusSession(selectedMinutes) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LofiMatcha,
                            contentColor = Color(0xFF102818)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_focus_button")
                    ) {
                        Text(
                            text = "Begin $selectedMinutes Min Chill Session",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
