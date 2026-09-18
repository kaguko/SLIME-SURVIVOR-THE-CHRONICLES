package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.JoystickPosition
import com.example.data.model.JoystickSize
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlimeBlue
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    onMove: (dx: Float, dy: Float) -> Unit,
    joystickPosition: JoystickPosition = JoystickPosition.LEFT,
    joystickSize: JoystickSize = JoystickSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    var touchCenter by remember { mutableStateOf<Offset?>(null) }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = (joystickSize.diameterDp * 1.1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(joystickSize) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchCenter = offset
                        knobOffset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val currentKnob = knobOffset + dragAmount
                        val dist = sqrt(currentKnob.x * currentKnob.x + currentKnob.y * currentKnob.y)
                        val clampedKnob = if (dist > maxRadius) {
                            Offset(currentKnob.x / dist * maxRadius, currentKnob.y / dist * maxRadius)
                        } else currentKnob

                        knobOffset = clampedKnob
                        val normalizedX = clampedKnob.x / maxRadius
                        val normalizedY = clampedKnob.y / maxRadius
                        onMove(normalizedX, normalizedY)
                    },
                    onDragEnd = {
                        touchCenter = null
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        touchCenter = null
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        // Subtle hint circle when not touching
        if (touchCenter == null) {
            val align = if (joystickPosition == JoystickPosition.LEFT) Alignment.BottomStart else Alignment.BottomEnd
            Box(
                modifier = Modifier
                    .align(align)
                    .padding(32.dp)
                    .size(joystickSize.diameterDp.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null,
                    tint = NeonCyan.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Active dynamic joystick
        touchCenter?.let { center ->
            val sizeDp = joystickSize.diameterDp.dp
            val halfPx = (maxRadius + 20f)
            Canvas(
                modifier = Modifier
                    .size(sizeDp + 40.dp)
                    .offset {
                        IntOffset(
                            (center.x - halfPx).roundToInt(),
                            (center.y - halfPx).roundToInt()
                        )
                    }
            ) {
                val c = Offset(size.width / 2f, size.height / 2f)
                // Outer ring
                drawCircle(
                    color = Color.Black.copy(alpha = 0.45f),
                    radius = maxRadius,
                    center = c
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.6f),
                    radius = maxRadius,
                    center = c,
                    style = Stroke(width = 3.5f)
                )

                // Inner knob
                val knobPos = c + knobOffset
                drawCircle(
                    color = SlimeBlue.copy(alpha = 0.85f),
                    radius = 28f,
                    center = knobPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 28f,
                    center = knobPos,
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }
}
