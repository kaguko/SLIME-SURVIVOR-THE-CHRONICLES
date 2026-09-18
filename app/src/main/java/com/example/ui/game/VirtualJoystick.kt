package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlimeBlue
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    onMove: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var touchCenter by remember { mutableStateOf<Offset?>(null) }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 130f

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
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
        touchCenter?.let { center ->
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .offset {
                        IntOffset(
                            (center.x - 80.dp.toPx()).roundToInt(),
                            (center.y - 80.dp.toPx()).roundToInt()
                        )
                    }
            ) {
                val c = Offset(size.width / 2f, size.height / 2f)
                // Outer ring
                drawCircle(
                    color = Color.Black.copy(alpha = 0.35f),
                    radius = maxRadius,
                    center = c
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.5f),
                    radius = maxRadius,
                    center = c,
                    style = Stroke(width = 3f)
                )

                // Inner knob
                val knobPos = c + knobOffset
                drawCircle(
                    color = SlimeBlue.copy(alpha = 0.8f),
                    radius = 32f,
                    center = knobPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 32f,
                    center = knobPos,
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }
}
