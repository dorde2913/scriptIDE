package org.example.scripta.composables.textEditor

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import org.example.scripta.KeyWordColor
import org.example.scripta.StringColor
import org.example.scripta.TextFieldDetailColor
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens
import kotlin.math.roundToInt

class KeyWordColorTransformation: VisualTransformation {

    //visual transformation class used for syntax highlighting
    companion object{
        lateinit var range: IntRange//public companion object used to set the range which represents the visible lines in our composable
    }

    var numTabs = 0

    override fun filter(text: AnnotatedString): TransformedText {
        val transformedText = if (text.isBlank()) text
        else buildAnnotatedStringWithColors(text.toString())

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset.coerceIn(0, transformedText.length)//+4*numTabs

            }

            override fun transformedToOriginal(offset: Int): Int {
                return offset.coerceIn(0, text.text.length)
            }
        }

        return TransformedText(
            transformedText,
            offsetMapping
        )
    }

    private fun buildAnnotatedStringWithColors(text: String): AnnotatedString {

        //here we use the visible range to more efficiently apply our colors to the text editor
        //makes the text editor more usable since without this optimization it would start lagging at a much smaller text size

        numTabs = 0
        val lexer = KotlinLexer()

        val beforeText = text.substring(0..<range.first)
        val toBeAnnotated = text.substring(range)
        val afterText = text.substring(range.last+1..<text.length)

        val builder = AnnotatedString.Builder()
        builder.append(beforeText)
        lexer.start(toBeAnnotated)
        while (lexer.tokenType != null) {

            val tokenText = lexer.tokenText
            val tokenType = lexer.tokenType


           // println("text: $tokenText, type: $tokenType")
            builder.withStyle(
                style = SpanStyle(
                    color =
                        when(tokenType){
                            in KtTokens.KEYWORDS -> KeyWordColor
                            in KtTokens.SOFT_KEYWORDS -> KeyWordColor

                            in KtTokens.COMMENTS -> TextFieldDetailColor //gray

                            in KtTokens.STRINGS -> StringColor
                            KtTokens.OPEN_QUOTE -> StringColor
                            KtTokens.CLOSING_QUOTE -> StringColor

                            else -> Color.White
                        },
                )
            ){
                //numTabs += tokenText.count{it == '\t'}
                append(tokenText.replace("\t"," "))//textfield can't display tab properly
            }

            lexer.advance()
        }

        builder.append(afterText)
        return builder.toAnnotatedString()
    }
}

//helper functions for calculating which lines of our text editor are visible based
//on the position of our vertical scrollbar

fun getLineForVerticalPosition(position: Float, density: Float): Int{
    //line is about 20px, maybe change this so it's not hard-coded
    return (position / (20 * density)).roundToInt()
}

fun getLineStart(line: Int, text: String): Int {
    var ret = 0
    var l = 0
    while(true){
        if (l == line || ret == text.length) return ret
        if (text[ret] == '\n') l++
        ret++
    }
}

fun getLineEnd(line: Int, text: String): Int{
    var ret = 0
    var l = 0
    while(true){
        if (l == line || ret == text.length-1) break
        if (text[ret] == '\n') l++
        ret++
    }
    while (text[ret] != '\n' && ret < text.length -1) ret++
    return ret
}


fun getVisibleRange(stateVertical: ScrollState, containerSize: IntSize, numLines: Int, text: String, density: Float): IntRange{

    if (text.isEmpty()) return 0..0

    val vertScroll = stateVertical.value
    val containerHeight = containerSize.height

    val firstVisibleLine = getLineForVerticalPosition(vertScroll.toFloat(),density).coerceAtLeast(0)
    val lastVisibleLine = getLineForVerticalPosition((vertScroll + containerHeight).toFloat(),density).coerceAtMost(numLines-1)


    val start = getLineStart(firstVisibleLine,text)
    val end = getLineEnd(lastVisibleLine,text)


    return start..end
}