package com.rk.components.compose.preferences.base

/*
 * Copyright 2021, Lawnchair.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun DividerColumn(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
    dividersToSkip: Int = 0,
    content: @Composable () -> Unit,
) {
    val state = remember { DividersState() }
    val density = LocalDensity.current
    val thicknessPx = with(density) { thickness.toPx() }
    val startIndentPx = with(density) { (startIndent + 16.dp).toPx() }
    val endIndentPx = with(density) { (endIndent + 16.dp).toPx() }
    Layout(
        modifier = modifier.drawDividers(state, color, thicknessPx, startIndentPx, endIndentPx),
        content = content,
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables =
            measurables.map { measurable ->
                // Measure each children
                measurable.measure(constraints)
            }

        val width = constraints.maxWidth
        val dividersHeight = thicknessPx.roundToInt() * (placeables.size - 1)
        val height = placeables.sumOf { it.height } + dividersHeight

        layout(width, height) {
            val dividerPositions = mutableListOf<Int>()
            // Track the y co-ord we have placed children up to
            var yPosition = 0

            // Place children in the parent layout
            placeables.forEachIndexed { index, placeable ->
                if (index > dividersToSkip) {
                    dividerPositions.add(yPosition)
                    yPosition += thicknessPx.roundToInt()
                }

                // Position item on the screen
                placeable.placeRelative(x = 0, y = yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }

            state.dividerPositions = dividerPositions
        }
    }
}

private fun Modifier.drawDividers(
    state: DividersState,
    color: Color,
    thickness: Float,
    startIndentPx: Float,
    endIndentPx: Float,
): Modifier {
    return drawBehind {
        state.dividerPositions.forEach { yPos ->
            drawRect(
                color = color,
                topLeft = Offset(startIndentPx, yPos.toFloat()),
                size = Size(size.width - startIndentPx - endIndentPx, thickness),
            )
        }
    }
}

private class DividersState {
    var dividerPositions by mutableStateOf(emptyList<Int>())
}
