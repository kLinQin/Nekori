package eu.kanade.tachiyomi.ui.reader.replace

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

private val HEX_REGEX = Regex("^#[0-9a-fA-F]{6}$")

@Composable
fun ReplaceColorPickerDialog(
    initial: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    val initialHsv = remember(initial) {
        val rgb = hexToRgb(initial) ?: Triple(255, 255, 255)
        rgbToHsv(rgb.first, rgb.second, rgb.third)
    }
    var hue by remember { mutableStateOf(initialHsv[0]) }
    var sat by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }
    var hex by remember { mutableStateOf(initial.lowercase()) }

    fun syncHex() {
        hex = hsvToHex(hue, sat, value)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih warna") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // SV square
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    sat = (offset.x / size.width).coerceIn(0f, 1f)
                                    value = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                                    syncHex()
                                },
                            ) { change, _ ->
                                val pos = change.position
                                sat = (pos.x / size.width).coerceIn(0f, 1f)
                                value = 1f - (pos.y / size.height).coerceIn(0f, 1f)
                                syncHex()
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                sat = (offset.x / size.width).coerceIn(0f, 1f)
                                value = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                                syncHex()
                            }
                        },
                ) {
                    // Thumb SV
                    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        val thumbX = sat * size.width
                        val thumbY = (1f - value) * size.height
                        drawCircle(
                            color = Color.White,
                            radius = 8.dp.toPx(),
                            center = Offset(thumbX, thumbY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                        )
                    }
                }

                // Hue bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red,
                                ),
                            ),
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    hue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                                    syncHex()
                                },
                            ) { change, _ ->
                                hue = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                                syncHex()
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                hue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                                syncHex()
                            }
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                        val thumbX = (hue / 360f) * size.width
                        drawCircle(
                            color = Color.White,
                            radius = 10.dp.toPx(),
                            center = Offset(thumbX, size.height / 2f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                        )
                    }
                }

                // Preview + hex
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(android.graphics.Color.parseColor(hex))),
                    )
                    OutlinedTextField(
                        value = hex,
                        onValueChange = { input ->
                            hex = input
                            if (HEX_REGEX.matches(input)) {
                                val rgb = hexToRgb(input) ?: return@OutlinedTextField
                                val hsv = rgbToHsv(rgb.first, rgb.second, rgb.third)
                                hue = hsv[0]
                                sat = hsv[1]
                                value = hsv[2]
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (HEX_REGEX.matches(hex)) onPick(hex.lowercase())
                    else onDismiss()
                },
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}

// ---------- HSV <-> RGB <-> Hex helpers ----------

private fun hexToRgb(hex: String): Triple<Int, Int, Int>? {
    val m = Regex("^#([0-9a-fA-F]{6})$").find(hex.trim()) ?: return null
    val v = m.groupValues[1]
    return Triple(
        v.substring(0, 2).toInt(16),
        v.substring(2, 4).toInt(16),
        v.substring(4, 6).toInt(16),
    )
}

private fun rgbToHsv(r: Int, g: Int, b: Int): FloatArray {
    val rn = r / 255f
    val gn = g / 255f
    val bn = b / 255f
    val max = maxOf(rn, gn, bn)
    val min = minOf(rn, gn, bn)
    val d = max - min
    var h = 0f
    if (d != 0f) {
        h = when (max) {
            rn -> ((gn - bn) / d) % 6f
            gn -> (bn - rn) / d + 2f
            else -> (rn - gn) / d + 4f
        } * 60f
        if (h < 0f) h += 360f
    }
    val s = if (max == 0f) 0f else d / max
    return floatArrayOf(h, s, max)
}

private fun hsvToHex(h: Float, s: Float, v: Float): String {
    val c = v * s
    val x = c * (1 - kotlin.math.abs(((h / 60f) % 2f) - 1f))
    val m = v - c
    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val r = ((r1 + m) * 255).toInt().coerceIn(0, 255)
    val g = ((g1 + m) * 255).toInt().coerceIn(0, 255)
    val b = ((b1 + m) * 255).toInt().coerceIn(0, 255)
    return "#%02x%02x%02x".format(r, g, b)
}
