package org.example.scripta.composables.textEditor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.scripta.TextFieldBackgroundColor
import org.example.scripta.TextFieldDetailColor
import org.example.scripta.TextFieldSelectedColor


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextEditor(
     stateVertical: ScrollState,
     stateHorizontal: ScrollState,
     selectedLine: Int,
     setSelectedLine: (Int)->Unit,
     bringIntoViewRequester: BringIntoViewRequester,
     focusRequester: FocusRequester,
     textState: TextFieldValue,
     setTextState: (TextFieldValue)->Unit,
     textLayoutResult: TextLayoutResult?,
     setTextLayoutResult: (TextLayoutResult) ->Unit,
) {
     var numLines by rememberSaveable { mutableStateOf(1) }
     var old_scroll by rememberSaveable{ mutableStateOf(0) }
        var old_container by rememberSaveable{mutableStateOf(IntSize.Zero)}


     Box(
          modifier = Modifier.fillMaxWidth().background(TextFieldBackgroundColor)
     ){

          Box{

               Column(
                    modifier = Modifier
                         .verticalScroll(stateVertical)
                         .fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
               ) {
                    for (i in 1 until numLines + 1) {
                         Text(
                              i.toString(), fontSize = 15.sp,
                              textAlign = TextAlign.Left, lineHeight = 15.sp,color = TextFieldDetailColor,
                              modifier = Modifier.background(if (i == selectedLine) TextFieldSelectedColor else TextFieldBackgroundColor
                              ).fillMaxWidth()
                         )
                    }
               }
               VerticalDivider(
                    modifier = Modifier.padding(start = 45.dp),
                    color = TextFieldDetailColor
               )
          }

          val density = LocalDensity.current.fontScale
          var containerSize by remember{ mutableStateOf(IntSize.Zero) }


          Box(
               modifier = Modifier.onSizeChanged {
                   old_container = containerSize
                    containerSize = it
               }
          ){
               Row{
                    Spacer(modifier = Modifier.width(50.dp))

                    Column(
                         modifier = Modifier.fillMaxSize()
                              .verticalScroll(stateVertical)
                              .background(Color.Transparent)
                    ) {


                         KeyWordColorTransformation.range =
                                   getVisibleRange(stateVertical,containerSize, numLines,textState.text,density)

                         //old_scroll = stateVertical.value
                         BasicTextField(
                              modifier = Modifier.fillMaxSize()
                                   .horizontalScroll(
                                        state = stateHorizontal
                                   )
                                   .bringIntoViewRequester(bringIntoViewRequester)
                                   .focusRequester(focusRequester)
                                   .background(Color.Transparent),
                              value = textState,
                              onValueChange = {
//                                   println("------")
//                                   println("value change")
//                                   println("-------")
                                   setSelectedLine(
                                        it.getTextBeforeSelection(Int.MAX_VALUE)
                                             .count { it == '\n' } + 1)

                                 // println("old scroll: $old_cursor, current: ${stateVertical.value}")
                                   if (textState.selection.start != it.selection.start && textState.text == it.text){
                                        //move screen to cursor when moving cursor,
                                        //moving screen when typing handled in onTextLayout
                                     //  println("moving view")
                                        CoroutineScope(Dispatchers.Main).launch {
                                             val offset = it.selection.start
                                             val rect = textLayoutResult!!.getCursorRect(offset)
                                             bringIntoViewRequester.bringIntoView(rect.copy(right = rect.right + 20))
                                        }
                                   }

                                   KeyWordColorTransformation.range =
                                         getVisibleRange(stateVertical,containerSize, numLines,it.text,density)
                                   //update visible range

                                   if (it.text.isEmpty()) {
                                        setTextState(it)
                                        numLines = 1
                                        return@BasicTextField
                                   }


                                   numLines = it.text.count { it == '\n' } + 1
                                   setTextState(it)
                              },
                              onTextLayout = {
//                                   println("------")
//                                   println("layout")
//                                   println("-------")
                                   setTextLayoutResult(it)
                                 // println("old scroll: $old_scroll, current: ${stateVertical.value}")
                                 // println("current container: $containerSize, old: $old_container")
                                 // println("overflow: ${it.hasVisualOverflow}")
                                   if (old_scroll == stateVertical.value && ((containerSize != old_container && old_container == IntSize.Zero)
                                       || (containerSize == old_container)) || it.size != textLayoutResult?.size){


                                        //we don't want to lock onto the cursor when scrolling,
                                        //horizontal scroll doesn't seem to affect this?
                                      // println("moving view")
                                        CoroutineScope(Dispatchers.Main).launch {
                                             val offset = textState.selection.start
                                             val rect = it.getCursorRect(offset)
                                             bringIntoViewRequester.bringIntoView(rect.copy(right = rect.right + 40))
                                        }
                                   }
                                  else old_scroll = stateVertical.value


                                   KeyWordColorTransformation.range =
                                        getVisibleRange(stateVertical,containerSize, numLines,textState.text,density)
                                   //update visible range

                              },
                              textStyle = TextStyle(
                                   fontSize = 15.sp,
                              ),
                              visualTransformation = KeyWordColorTransformation(),
                              cursorBrush = SolidColor(Color.White)
                         )
                    }
               }

          }
     }
}




