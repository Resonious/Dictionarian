package me.nigelbaillie.dictionarian

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.ui.tooling.preview.Preview
import com.google.android.material.navigation.NavigationView
import me.nigelbaillie.dictionarian.ui.DictionarianTheme

class MainActivity : AppCompatActivity() {
    private val model: AnalyzeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Analyze image
        // TODO this step should only happen if an image was passed in via intent etc
        val dummyExampleImage = ResourcesCompat.getDrawable(
                resources,
                R.drawable.sample_image,
                null
        )
        Log.d("NIGELMSG", "WHAT HTE FLUCK? ${dummyExampleImage!!.toBitmap().height}")
        model.analyze(dummyExampleImage!!.toBitmap())

        // Setup navigator/content. See nav_graph.xml
        setContentView(R.layout.activity_main)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
    }
}
