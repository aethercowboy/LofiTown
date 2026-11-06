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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lofitown.model.Milestone
import com.example.ui.theme.LofiCardBg
import com.example.ui.theme.LofiCream
import com.example.ui.theme.LofiLavender
import com.example.ui.theme.LofiMatcha
import com.example.ui.theme.LofiMutedText
import com.example.ui.theme.LofiPeach
import com.example.ui.theme.LofiSurface
import com.example.ui.theme.LofiYellowGlow
import com.example.ui.theme.SilkscreenFontFamily

@Composable
fun JournalDialog(
    viewModel: TownViewModel,
    onDismiss: () -> Unit
) {
    val milestones by viewModel.milestones.collectAsState()
    val zenStars by viewModel.zenStars.collectAsState()
    val vibes by viewModel.vibes.collectAsState()
    val timeOfDay by viewModel.timeOfDay.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val totalRate = viewModel.totalVibesPerSec

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LofiSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF382947), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = LofiYellowGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOWN SCRAPBOOK",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 13.sp,
                            color = LofiCream
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_journal_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LofiMutedText)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LofiCardBg)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Atmosphere:", fontSize = 11.sp, color = LofiMutedText)
                            Text("${timeOfDay.title} • ${weather.title}", fontSize = 11.sp, color = LofiCream)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Vibe Generation:", fontSize = 11.sp, color = LofiMutedText)
                            Text("+${String.format("%.1f", totalRate)}/s", fontFamily = SilkscreenFontFamily, fontSize = 11.sp, color = LofiMatcha)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Zen Stars Earned:", fontSize = 11.sp, color = LofiMutedText)
                            Text("★ $zenStars", fontFamily = SilkscreenFontFamily, fontSize = 11.sp, color = LofiYellowGlow)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "COZY MILESTONES",
                    fontFamily = SilkscreenFontFamily,
                    fontSize = 11.sp,
                    color = LofiPeach
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(milestones) { milestone ->
                        MilestoneItem(milestone)
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneItem(milestone: Milestone) {
    val isDone = milestone.isCompleted
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDone) Color(0xFF233227) else LofiCardBg)
            .border(
                1.dp,
                if (isDone) LofiMatcha.copy(alpha = 0.5f) else Color(0xFF382947),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isDone) LofiMatcha else Color(0xFF382947)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF102818),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "★",
                            fontFamily = SilkscreenFontFamily,
                            fontSize = 11.sp,
                            color = LofiMutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = milestone.title,
                        fontFamily = SilkscreenFontFamily,
                        fontSize = 11.sp,
                        color = if (isDone) LofiCream else LofiMutedText
                    )
                    Text(
                        text = milestone.requirement,
                        fontSize = 11.sp,
                        color = if (isDone) LofiCream.copy(alpha = 0.7f) else LofiMutedText.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = "+${milestone.rewardZen} ★",
                fontFamily = SilkscreenFontFamily,
                fontSize = 10.sp,
                color = if (isDone) LofiYellowGlow else LofiMutedText
            )
        }
    }
}
