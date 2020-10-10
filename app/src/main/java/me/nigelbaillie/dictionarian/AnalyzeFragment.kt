package me.nigelbaillie.dictionarian

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.ui.tooling.preview.Preview
import me.nigelbaillie.dictionarian.ocr.Failure
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

        return ComposeView(requireContext()).apply {
            setContent {
                DictionarianTheme {
                    when (val result = model.result.value) {
                        null -> Text("PLEASE WAIT!!!")
                        is Success -> AnalyzeResultImage(result = result)
                        is Failure -> FailurePage(result = result)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyzeResultImage(result: Success) {
    val (scale, setScale) = remember { mutableStateOf(0F) }
    Log.d("NIGEL", "Scale is $scale")

    Image(
            result.image.asImageAsset(),
            contentScale = LiveScale(ContentScale.Inside, setScale)
    )
    for (block in result.blocks) {
        ShowTextBlock(block, scale)
    }
}

@Composable
fun FailurePage(result: Failure) {
    Column {
        Text("Something went wrong:")
        Text(result.reason)
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
    Log.d("NIGEL", "Scale is $scale")

    DictionarianTheme {
        Image(
                asset = imageResource(id = R.drawable.sample_image),
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
