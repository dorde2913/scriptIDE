package org.example.scripta.composables

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.scripta.getErrorIndex

@Composable
fun ErrorPane(
    height: Dp,
    verticalState: ScrollState,
    horizontalState: ScrollState,
    outputList: List<String>,
    errorList: List<String>,
    moveCursor: (Int)->Unit,
    script: String
){

    Column(
        modifier = Modifier.fillMaxWidth()
            .height(height)
            .background(Color.Black)
            .verticalScroll(
                verticalState
            )
            .horizontalScroll(horizontalState)

    ) {



        outputList.forEachIndexed { index, line ->
            Text(line, color = Color.White)//,modifier = Modifier.horizontalScroll(horizontalState)
        }


        errorList.forEach {
            if (it.contains("tempscript.kt")) {//replace if we implement multiple files
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.clickable {

                        val substring = it.substringAfter("tempscript.kt")
                        val segments = substring.split(':')

                        val row = segments[1].substringBefore(')').toInt()
                        val column = if (segments.size > 2) segments[2].toInt() else 1

                        /*
                         some exception messages contain only the line number, while others contain the character aswell
                         */
                        val errorIndex = getErrorIndex(script, row, column)

                        moveCursor(errorIndex)
                    }
                        .pointerHoverIcon(PointerIcon.Hand)
                        .drawBehind {

                            drawLine(
                                color = Color.Red,
                                strokeWidth = 1.dp.toPx(),
                                start = Offset(
                                    if (it[0] == ' ') 20.sp.toPx() else 0.sp.toPx(),
                                    size.height - 2.sp.toPx()
                                ),
                                end = Offset(size.width, size.height - 2.sp.toPx())
                            )
                        })
            } else
                Text(it, color = Color.Red)
        }
    }
}