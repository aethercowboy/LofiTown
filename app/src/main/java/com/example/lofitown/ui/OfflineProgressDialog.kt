package com.example.lofitown.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LofiCardBg
import com.example.ui.theme.LofiCream
import com.example.ui.theme.LofiMatcha
import com.example.ui.theme.LofiMutedText
import com.example.ui.theme.LofiPeach
import com.example.ui.theme.LofiSurface
import com.example.ui.theme.LofiYellowGlow
import com.example.ui.theme.SilkscreenFontFamily

@Composable
fun OfflineProgressDialog(
    reward: OfflineReward,
    onCollect: () -> Unit
) {
    Dialog(onDismissRequest = onCollect) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LofiSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF382947), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(LofiCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Rest",
                        tint = LofiYellowGlow,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "WHILE YOU WERE AWAY...",
                    fontFamily = SilkscreenFontFamily,
                    fontSize = 13.sp,
                    color = LofiCream
                )

                Spacer(modifier = Modifier.height(6.dp))

                val hours = reward.minutesAway / 60
                val mins = reward.minutesAway % 60
                val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins} minutes"

                Text(
                    text = "Your quiet town rested peacefully for $timeStr. The river flowed gently, and citizens gathered comfort in the quiet air.",
                    fontSize = 12.sp,
                    color = LofiMutedText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LofiCardBg)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Vibes Gathered:", fontSize = 12.sp, color = LofiCream)
                            Text(
                                "♫ +${reward.vibesCollected}",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 12.sp,
                                color = LofiPeach
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cups of Tea Brewed:", fontSize = 12.sp, color = LofiCream)
                            Text(
                                "☕ ${reward.cupsOfTea}",
                                fontFamily = SilkscreenFontFamily,
                                fontSize = 12.sp,
                                color = LofiMatcha
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onCollect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LofiPeach,
                        contentColor = Color(0xFF2B1810)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("collect_offline_reward_button")
                ) {
                    Text(
                        text = "Collect & Return to Town",
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
