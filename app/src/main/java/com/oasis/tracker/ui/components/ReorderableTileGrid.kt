package com.oasis.tracker.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * A small, non-lazy grid (meant for dozens of items, not thousands) that
 * supports Android-homescreen-style long-press drag to reorder: hold a tile,
 * drag it over a neighbor, and they swap. [onOrderChanged] fires once the
 * drag ends so the caller can persist the new order.
 */
@Composable
fun <T> ReorderableTileGrid(
    items: List<T>,
    itemId: (T) -> String,
    onOrderChanged: (List<T>) -> Unit,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    horizontalSpacing: Dp = 14.dp,
    verticalSpacing: Dp = 14.dp,
    tileHeight: Dp = 96.dp,
    content: @Composable (T) -> Unit
) {
    var order by remember(items.map(itemId)) { mutableStateOf(items) }
    val itemCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var pointerPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var dragVisualOffset by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
        order.chunked(columns).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
                rowItems.forEach { item ->
                    val id = itemId(item)
                    key(id) {
                        val isDragging = draggingId == id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(tileHeight)
                                .onGloballyPositioned { coords -> itemCoordinates[id] = coords }
                                .zIndex(if (isDragging) 1f else 0f)
                                .then(
                                    if (isDragging) {
                                        Modifier.offset {
                                            IntOffset(dragVisualOffset.x.roundToInt(), dragVisualOffset.y.roundToInt())
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                                .pointerInput(id) {
                                    detectTapGestures(onTap = { onItemClick(item) })
                                }
                                .pointerInput(id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { localStart ->
                                            draggingId = id
                                            val myCoords = itemCoordinates[id]
                                            pointerPositionInWindow = myCoords?.localToWindow(localStart) ?: Offset.Zero
                                            dragVisualOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            pointerPositionInWindow += dragAmount
                                            dragVisualOffset += dragAmount

                                            val currentId = draggingId
                                            if (currentId != null) {
                                                val target = itemCoordinates.entries.firstOrNull { (otherId, coords) ->
                                                    otherId != currentId && coords.boundsInWindow().contains(pointerPositionInWindow)
                                                }
                                                if (target != null) {
                                                    val fromIndex = order.indexOfFirst { itemId(it) == currentId }
                                                    val toIndex = order.indexOfFirst { itemId(it) == target.key }
                                                    if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                                                        order = order.toMutableList().apply {
                                                            add(toIndex, removeAt(fromIndex))
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggingId = null
                                            dragVisualOffset = Offset.Zero
                                            onOrderChanged(order)
                                        },
                                        onDragCancel = {
                                            draggingId = null
                                            dragVisualOffset = Offset.Zero
                                        }
                                    )
                                }
                        ) {
                            content(item)
                        }
                    }
                }
            }
        }
    }
}
