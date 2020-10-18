package me.nigelbaillie.dictionarian

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.ResourceFont
import androidx.compose.ui.text.font.asFontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.nigelbaillie.dictionarian.ocr.Failure
import me.nigelbaillie.dictionarian.ocr.InProgress
import me.nigelbaillie.dictionarian.ocr.Success
import me.nigelbaillie.dictionarian.ocr.TextBlock
import me.nigelbaillie.dictionarian.ui.DictionarianTheme
import me.nigelbaillie.dictionarian.ui.LiveScale
import me.nigelbaillie.dictionarian.ui.logoDark
import me.nigelbaillie.dictionarian.ui.logoTeal


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
                            null -> HomePage()
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
                    onClick = { model.opacity = 0F }
            ) { Icon(asset = Icons.Rounded.Clear) }

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
            model.query = TextFieldValue()
        }

        fun kanjiStudy() {
            val kanjiStudyIntent: Intent = Intent().apply {
                putExtra(Intent.EXTRA_TEXT, model.query.text)
                action = Intent.ACTION_SEND
                type = "text/plain"
            }
            startActivity(kanjiStudyIntent)
            model.query = TextFieldValue()
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
                    onClick = { kanjiStudy() }
            ) { Icon(asset = Icons.Rounded.Search) }
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

        val data = TextBlockData(scale * densityScale, model.opacity)
        for (block in result.blocks) {
            ShowTextBlock(block, data, scrollState)
        }
    }

    class TextBlockData(
            val scale: Float,
            val opacity: Float
    ) {
        val backdrop = if (opacity < 0.05F) Color.Transparent
                       else Color.White.copy(alpha = opacity)
        val border = if (opacity < 0.05F) Color.Transparent
                     else Color.White
        val text = Color.Black.copy(alpha = opacity)
    }

    val mainColumnMods =
        Modifier
            .fillMaxSize()
            .padding(20.dp)

    @Composable
    fun HomePage() {
        val chooseFile = {
            val selectFile = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            val chooser = Intent.createChooser(selectFile, "Image to analyze")

            // This is NASTY... wtf
            val startForResult =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        result: ActivityResult? ->
                        if (result?.resultCode == Activity.RESULT_OK) {
                            result.data?.let { intent: Intent ->
                                model.analyzeData(intent)
                            }
                        }
                    }
            startForResult.launch(chooser)
        }

        val goToCamera = {
            val activity = model.activity

            if (activity != null) {
                val camIntent = Intent(activity, CameraActivity::class.java)
                activity.startActivity(camIntent)
            }
        }

        Column(mainColumnMods, verticalArrangement = Arrangement.SpaceBetween) {
            Column(
                Modifier.align(Alignment.CenterHorizontally)
            ) {
                DictionarianLogo()

                Text("Share from another app or choose here", fontFamily = juraFont.asFontFamily())
            }

            Icon(
                vectorResource(R.drawable.ic_downarrow),
                Modifier.align(Alignment.CenterHorizontally),
                if (isSystemInDarkTheme()) logoTeal else logoDark
            )

            Column {
                val buttonModifier = Modifier.fillMaxWidth()

                OutlinedButton(
                    chooseFile,
                    buttonModifier
                ) {
                    Text("Browse", fontFamily = juraFont.asFontFamily())
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    goToCamera,
                    buttonModifier
                ) {
                    Text("Camera", fontFamily = juraFont.asFontFamily())
                }
            }
        }
    }

    @Composable
    fun DictionarianLogo() {
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
    }

    @Composable
    fun FailurePage(result: Failure) {
        Column(mainColumnMods.fillMaxSize()) {
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
        Column(Modifier.fillMaxSize()) {
            Column(
                mainColumnMods.align(Alignment.CenterHorizontally)
            ) {
                DictionarianLogo()
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
    fun ShowTextBlock(block: TextBlock, data: TextBlockData, scrollState: ScrollState? = null) {
        if (data.scale == 0F) return

        val thinness = if (block.height > block.width)
            block.width
        else
            block.height

        Box(
                Modifier
                        .offset((block.x * data.scale).dp, (block.y * data.scale).dp)
                        .width((block.width * data.scale).dp)
                        .height((block.height * data.scale).dp)
                        .padding(0.dp)
                        .background(data.backdrop)
                        .border(0.dp, data.border, RectangleShape)
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
            if (data.opacity < 0.05F) return@Box
            Text(
                    block.text,
                    fontSize = TextUnit.Sp(thinness * data.scale * 0.75F),
                    color = data.text,
                    overflow = TextOverflow.Clip,
                    softWrap = true,
                    lineHeight = TextUnit.Em(0.97F)
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
            /*
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
             */
        }
    }
}
