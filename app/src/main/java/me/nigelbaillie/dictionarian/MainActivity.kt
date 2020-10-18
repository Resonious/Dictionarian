package me.nigelbaillie.dictionarian

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import me.nigelbaillie.dictionarian.ocr.Failure

class MainActivity : AppCompatActivity() {
    private val model: AnalyzeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        // Setup navigator/content. See nav_graph.xml
        setContentView(R.layout.activity_main)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        model.context = applicationContext
        model.activity = this

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                Log.d("NIGELMSG", "MainActivity.handleIntent called with SEND intent")
                model.analyzeExtraStream(intent)
            }
            else -> {
                Log.d("NIGELMSG", "MainActivity.handleIntent called with no intent")
                // ...
            }
        }
    }
}
