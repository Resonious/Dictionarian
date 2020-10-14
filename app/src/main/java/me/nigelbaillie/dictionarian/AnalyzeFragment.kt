package me.nigelbaillie.dictionarian

import android.content.Intent
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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawOpacity
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.selection.Selection
import androidx.compose.ui.selection.SelectionContainer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
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

    private val juraFont = ResourceFont(R.font.jura)
    private val latoFont = ResourceFont(R.font.lato)

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
                            null -> InProgressPage(InProgress("'Share' an image from another app!"))
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

        ScrollableColumn(
                scrollState = scrollState,
                modifier = Modifier.fillMaxHeight()
        ) {
            Box { ImageAndTextBlocks(result, display, scrollState) }

            QueryTextInput(scrollState)

            OpacitySlider()

            Box(
                    Modifier
                            .background(Color.Red)
                            .preferredHeight(50.dp)
            )
        }
    }

    @Composable
    fun OpacitySlider() {
        Row {
            IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { model.opacity = 0.9F }
            ) { Icon(asset = Icons.Rounded.Refresh) }

            Slider(value = model.opacity, onValueChange = { model.opacity = it })
        }
    }

    @Composable
    fun QueryTextInput(scrollState: ScrollState) {
        fun shareQuery() {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, model.query.text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Lookup")
            startActivity(shareIntent)
        }

        Row {
            IconButton(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = { model.query = TextFieldValue() }
            ) { Icon(asset = Icons.Rounded.Clear) }

            OutlinedTextField(
                model.query, {
                    model.query = it
                },
                imeAction = ImeAction.Search,
                onImeActionPerformed = { _, keyboard ->
                    keyboard?.hideSoftwareKeyboard()
                    shareQuery()
                },
                onTextInputStarted = {
                    lifecycleScope.launch {
                        delay(600)
                        scrollState.smoothScrollTo(scrollState.maxValue)
                    }
                }
            )

            IconButton(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = { shareQuery() }
            ) { Icon(asset = Icons.Rounded.Share) }
        }
    }

    @Composable
    fun ImageAndTextBlocks(result: Success, display: DisplayMetrics, scrollState: ScrollState) {
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
            ShowTextBlock(block, scale * densityScale, scrollState)
        }
    }

    @Composable
    fun FailurePage(result: Failure) {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            Text(
                "Something went wrong",
                fontFamily = juraFont.asFontFamily(),
                fontSize = TextUnit.Em(10)
            )
            Text(result.reason, fontFamily = juraFont.asFontFamily())
        }
    }

    @Composable
    fun InProgressPage(result: InProgress) {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            Column(
                    Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row {
                    Icon(
                            asset = vectorResource(id = R.drawable.dict_logo),
                            modifier = Modifier.preferredSize(60.dp)
                    )
                    Text(
                            "Dictionarian",
                            Modifier.align(Alignment.CenterVertically),
                            fontFamily = latoFont.asFontFamily(),
                            fontSize = TextUnit.Em(10)
                    )
                }
                Text(result.message, fontFamily = juraFont.asFontFamily())
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewFailurePage() {
        DictionarianTheme {
            FailurePage(Failure(reason = "I suck. This is an example"))
        }
    }
    @Preview(showBackground = true)
    @Composable
    fun PreviewWaitingPage() {
        DictionarianTheme {
            InProgressPage(InProgress(message = "Tada. Nothing to do."))
        }
    }

    @Composable
    fun ShowTextBlock(block: TextBlock, scale: Float, scrollState: ScrollState? = null) {
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
                .background(backdrop.copy(alpha = model.opacity))
                .border(0.dp, backdrop, RectangleShape)
                .layoutId("text:${block.text}")
                .clickable {
                    val newText = model.query.text + block.text
                    model.query = model.query.copy(
                        newText,
                        TextRange(newText.length - block.text.length, newText.length)
                    )
                    scrollState?.smoothScrollTo(scrollState.maxValue)
                }
        ) {
            Text(
                block.text,
                fontSize = TextUnit.Sp(thinness * scale * 0.75F),
                color = foreground.copy(alpha = model.opacity),
                overflow = TextOverflow.Clip,
                softWrap = true,
                lineHeight = TextUnit.Em(0.94F)
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
