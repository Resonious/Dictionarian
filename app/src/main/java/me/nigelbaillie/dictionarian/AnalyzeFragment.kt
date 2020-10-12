package me.nigelbaillie.dictionarian

import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.TextField
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                    Surface {
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
        val scrollState = rememberScrollState(0F)

        ScrollableColumn(scrollState = scrollState) {
            Box {
                ImageAndTextBlocks(result, display)
            }

            OutlinedTextField(
                model.query, {
                    model.query = it
                },
                Modifier.align(Alignment.CenterHorizontally),
                imeAction = ImeAction.Search,
                onImeActionPerformed = { _, keyboard ->
                    keyboard?.hideSoftwareKeyboard()
                },
                onTextInputStarted = {
                    lifecycleScope.launch {
                        delay(500)
                        scrollState.smoothScrollTo(scrollState.maxValue)
                    }
                }
            )

            Box(Modifier.background(Color.Red).height(50.dp)) {  }
        }
    }

    @Composable
    fun ImageAndTextBlocks(result: Success, display: DisplayMetrics) {
        val (scale, setScale) = remember { mutableStateOf(0F) }

        val bm = result.image
        val densityScale = if (bm.density == Bitmap.DENSITY_NONE)
            DisplayMetrics.DENSITY_DEFAULT.toFloat() / display.densityDpi.toFloat()
        else
            bm.density.toFloat() / display.densityDpi.toFloat()

        Log.d("NIGELMSG", "Display is ${display.densityDpi} DPI")
        Log.d("NIGELMSG", "Scale is effectively $scale * $densityScale = ${scale * densityScale}")

        Image(
            bm.asImageAsset(),
            Modifier.layoutId("image"),
            contentScale = LiveScale(ContentScale.Inside, setScale)
        )

        for (block in result.blocks) {
            ShowTextBlock(block, scale * densityScale)
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

        val backdrop = Color.White
        val foreground = Color.Black

        val thinness = if (block.height > block.width)
            block.width
        else
            block.height

        Box(
            Modifier
                .offset((block.x * scale).dp, (block.y * scale).dp)
                .width((block.width * scale).dp)
                .height((block.height * scale).dp)
                .padding(0.dp)
                .background(backdrop.copy(alpha = 0.9F))
                .border(0.dp, backdrop, RectangleShape)
                .layoutId("text:${block.text}")
                .clickable {
                    val newText = model.query.text + block.text
                    model.query = model.query.copy(
                        newText,
                        TextRange(newText.length - block.text.length, newText.length)
                    )
                }
        ) {
            Text(
                block.text,
                fontSize = TextUnit.Sp(thinness * scale * 0.75F),
                color = foreground,
                overflow = TextOverflow.Clip,
                softWrap = true
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
}
