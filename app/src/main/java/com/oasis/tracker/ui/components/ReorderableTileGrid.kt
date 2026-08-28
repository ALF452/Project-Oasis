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
    onPressActiveChanged: (Boolean) -> Unit = {},
    content: @Composable (T, Int) -> Unit
) {
    var order by remember(items.map(itemId)) { mutableStateOf(items) }
    val itemCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var pointerPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var dragVisualOffset by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
        order.chunked(columns).forEachIndexed { rowIndex, rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)) {
                rowItems.forEachIndexed { columnIndex, item ->
                    val index = rowIndex * columns + columnIndex
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
                                    // No onPress here on purpose: an earlier version disabled the
                                    // parent's scroll on every press to protect the long-press-drag
                                    // detector below from losing a race against the parent scrolling.
                                    // That race doesn't actually exist — Compose's own long-press
                                    // detector cancels itself the instant the pointer moves past touch
                                    // slop, using the raw distance moved, regardless of whether the
                                    // parent's scrollable "wins" consumption of that same movement. So
                                    // disabling the parent never changed whether the long press
                                    // survived; it only added latency to every tap and to every scroll
                                    // gesture that happened to start on a tile, which is most of the
                                    // screen. Scroll is now only ever blocked once a drag has actually
                                    // started (see onDragStart/onDragEnd/onDragCancel below), which is
                                    // the one case blocking it is genuinely needed.
                                    detectTapGestures(onTap = { onItemClick(item) })
                                }
                                .pointerInput(id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { localStart ->
                                            onPressActiveChanged(true)
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
                                            onPressActiveChanged(false)
                                            draggingId = null
                                            dragVisualOffset = Offset.Zero
                                            onOrderChanged(order)
                                        },
                                        onDragCancel = {
                                            onPressActiveChanged(false)
                                            draggingId = null
                                            dragVisualOffset = Offset.Zero
                                        }
                                    )
                                }
                        ) {
                            content(item, index)
                        }
                    }
                }
            }
        }
    }
}
