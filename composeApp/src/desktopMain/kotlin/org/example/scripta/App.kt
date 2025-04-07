package org.example.scripta


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.scripta.composables.ErrorPane
import org.example.scripta.composables.textEditor.TextEditor
import org.example.scripta.data.ScriptViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Cursor

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    
    MaterialTheme {


        val viewModel: ScriptViewModel = viewModel()
        val state by viewModel.state.collectAsState()

        val windowState = rememberWindowState()

        var resultTabHeight by rememberSaveable{ mutableStateOf(windowState.size.height/2) }


        var textState by remember {
            mutableStateOf(
                TextFieldValue(
                    text = "",
                    selection = TextRange(0),
                )
            )
        }


        val stateVertical = rememberScrollState(0)
        val stateHorizontal = rememberScrollState(0)
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        val focusRequester = remember{ FocusRequester()}
        var selectedLine by remember{ mutableStateOf(1) }
        val bringIntoViewRequester = remember { BringIntoViewRequester() }


        DisposableEffect(Unit){
            onDispose {
                viewModel.stopScript()
            }
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().background(TextFieldBackgroundColor),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text("Script", color = Color.White)

                Row(){
                    Button(onClick = {
                        CoroutineScope(Dispatchers.Default).launch {
                            viewModel.executeScript(textState.text)
                        }
                    },

                        enabled = !state.running,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Green
                        )

                    ){
                        Text("Execute Script")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(onClick = {
                        CoroutineScope(Dispatchers.Default).launch {
                            viewModel.stopScript()
                        }
                    },
                        enabled = state.running,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red
                        )
                    ){
                       Text("Stop Execution")
                    }
                }

            }
            HorizontalDivider(
                color = TextFieldDetailColor
            )




            Box(modifier = Modifier.fillMaxWidth()
                .weight(0.5f)){

                TextEditor(
                    stateVertical = stateVertical,
                    stateHorizontal = stateHorizontal,
                    selectedLine = selectedLine,
                    setSelectedLine = {selectedLine = it},
                    bringIntoViewRequester = bringIntoViewRequester,
                    focusRequester = focusRequester,
                    textState = textState,
                    setTextState = {textState = it},
                    textLayoutResult = textLayoutResult,
                    setTextLayoutResult = {textLayoutResult = it},

                )
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(stateVertical),
                    style = LocalScrollbarStyle.current.copy(thickness = 10.dp)
                )
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                    adapter = rememberScrollbarAdapter(stateHorizontal)
                )

            }

            val errorStateHorizontal = rememberScrollState(0)
            val errorStateVertical = rememberScrollState(0)


            HorizontalDivider(color = TextFieldDetailColor)
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(TextFieldBackgroundColor)
                    .pointerInput(Unit) {
                        detectDragGestures { _, drag ->
                            resultTabHeight -= drag.y.toDp()
                        }
                    }
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))),
                verticalAlignment = Alignment.CenterVertically

            ) {
                Text("Output: ", color = Color.White)
                if (state.running) IndeterminateCircularIndicator()
                else {
                    if (!state.error) Icon(Icons.Default.CheckCircle,null,tint = Color.Green)
                    else Icon(Icons.Default.Close,null,tint = Color.Red)
                }
            }




            Box(
                modifier = Modifier.height(resultTabHeight)
            ){

                ErrorPane(
                    height = resultTabHeight,
                    verticalState = errorStateVertical,
                    horizontalState = errorStateHorizontal,
                    outputList = state.outputList,
                    errorList = state.errorList,
                    moveCursor = {errorIndex ->
                        focusRequester.requestFocus()
                        //println("ERROR INDEX: $errorIndex")
                        textState = textState.copy(selection = TextRange(errorIndex))
                        CoroutineScope(Dispatchers.Main).launch {
                            bringIntoViewRequester.bringIntoView(
                                textLayoutResult!!.getCursorRect(
                                    textState.selection.start
                                )
                            )
                            selectedLine =
                                textState.getTextBeforeSelection(Int.MAX_VALUE)
                                    .count { it == '\n' } + 1
                        }
                    },
                    script = textState.text
                )

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(errorStateVertical),
                    style = LocalScrollbarStyle.current.copy(thickness = 10.dp, hoverColor = Color.DarkGray, unhoverColor = Color.White)
                )
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                    adapter = rememberScrollbarAdapter(errorStateHorizontal),
                    style = LocalScrollbarStyle.current.copy(thickness = 10.dp, hoverColor = Color.DarkGray, unhoverColor = Color.White)
                )

            }


        }
    }

}



@Composable
fun IndeterminateCircularIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = Color.Green
    )
}

fun getErrorIndex(script: String, row: Int, column: Int): Int{

    //println("row: $row" +
            //"column: $column")

    var r = 1
    var c = 1

    for (i in 0 until script.length){
        //println("row: $r ; column: $c --- goal: $row / $column")
        if (column == 1 && r == row) return i
        else if (r == row && c == column - 1) return i + 1

        if (script[i] == '\n'){
            c = 1
            r++
        }
        else c++
    }

    return 0
}
