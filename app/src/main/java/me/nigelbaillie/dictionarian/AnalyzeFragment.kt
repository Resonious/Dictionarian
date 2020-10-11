package me.nigelbaillie.dictionarian

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.selection.Selection
import androidx.compose.ui.selection.SelectionContainer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.ui.tooling.preview.Preview
import me.nigelbaillie.dictionarian.ocr.Failure
import me.nigelbaillie.dictionarian.ocr.InProgress
import me.nigelbaillie.dictionarian.ocr.Success
import me.nigelbaillie.dictionarian.ocr.TextBlock
import me.nigelbaillie.dictionarian.ui.DictionarianTheme
import me.nigelbaillie.dictionarian.ui.LiveScale

class AnalyzeFragment : Fragment() {
    private val model: AnalyzeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()
        val display = context.resources.displayMetrics

        return ComposeView(context).apply {
            setContent {
                DictionarianTheme {
                    when (val value = model.result) {
                        null -> Text("No image selected!")
                        is Success -> AnalyzeResultImage(result = value, display = display)
                        is Failure -> FailurePage(result = value)
                        is InProgress -> InProgressPage(result = value)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyzeResultImage(result: Success, display: DisplayMetrics) {
    Box {
        ImageAndTextBlocks(result, display)
    }
}

@Composable
fun ImageAndTextBlocks(result: Success, display: DisplayMetrics) {
    val (scale, setScale) = remember { mutableStateOf(0F) }

    val bm = result.image
    val densityScale = if (bm.density == 0) 1F
                       else bm.density.toFloat() / display.densityDpi.toFloat()

    Log.d("NIGELMSG", "Scale is effectively $scale * $densityScale = ${scale * densityScale}")

    Image(
            bm.asImageAsset(),
            Modifier.layoutId("image"),
            contentScale = LiveScale(ContentScale.Inside, setScale)
    )
    Box(
            Modifier
                    .offset(0.dp, 20.dp)
                    .width(10.dp)
                    .height(10.dp)
                    .background(Color.Red, CircleShape),
    ) {}

    TextBlocks(result.blocks, scale * densityScale)
}

@Composable
fun TextBlocks(blocks: Array<TextBlock>, scale: Float) {
    val (selection, setSelection) = remember { mutableStateOf<Selection?>(null) }
    // TODO can use this selection to uh... render dictionary or something

    val realSetSelection = { selection: Selection? ->
        setSelection(selection)
        if (selection != null) {
            Log.d("NIGELMSG", "Selected: ${selection.start.selectable.getText()} : ${selection.end.selectable.getText()}")
        }
    }

    SelectionContainer(selection = selection, onSelectionChange = realSetSelection) {
        for (block in blocks) {
            ShowTextBlock(block, scale)
        }
    }
}

@Composable
fun FailurePage(result: Failure) {
    Column {
        Text("Something went wrong:")
        Text(result.reason)
    }
}

@Composable
fun InProgressPage(result: InProgress) {
    Column {
        Text("Wahoo")
        Text(result.message)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFailurePage() {
    DictionarianTheme {
        FailurePage(Failure(reason = "I suck. This is an example"))
    }
}

@Composable
fun ShowTextBlock(block: TextBlock, scale: Float) {
    if (scale == 0F) return

    Box(
            Modifier
                    .offset((block.x * scale).dp, (block.y * scale).dp)
                    .width((block.width * scale).dp)
                    .height((block.height * scale).dp)
                    .padding(0.dp)
                    .background(Color.Cyan)
                    .border(0.dp, Color.Cyan, RectangleShape)
                    .layoutId("text:${block.text}")
    ) {
        Text(
                block.text,
                fontSize = TextUnit.Sp(block.height * scale * 0.55F),
                softWrap = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTextBlocks() {
    val (scale, setScale) = remember { mutableStateOf(0F) }
    Log.d("NIGELMSG", "Scale is $scale")

    val bitmap = imageResource(id = R.drawable.sample_image).asAndroidBitmap()
    Log.d("NIGELMSG", "Image:  ${bitmap.width} x ${bitmap.height}")
    Log.d("NIGELMSG", "Density: ${bitmap.density}")

    DictionarianTheme {
        Image(
                asset = bitmap.asImageAsset(),
                contentScale = LiveScale(ContentScale.Inside, setScale)
        )
        Box(
                Modifier
                        .offset(900.dp, 0.dp)
                        .preferredWidth(90.dp)
                        .preferredHeight(90.dp)
                        .background(Color.Red)
                        .border(0.dp, Color.White, CircleShape),
        ) {}
        ShowTextBlock(TextBlock(
                bounds= Rect(Offset(195.0F, 142.0F), Offset(878.0F, 242.0F)),
                text="台風の前にやっておく..."
        ), scale)
        ShowTextBlock(TextBlock(
                bounds= Rect(Offset(155.0F, 380.0F), Offset(294.0F, 455.0F)),
                text="目次"
        ), scale)
        ShowTextBlock(TextBlock(
                bounds= Rect(Offset(155.0F, 530.0F), Offset(791.0F, 592.0F)),
                text="台風の接近が予想される場合"
        ), scale)
    }
}
