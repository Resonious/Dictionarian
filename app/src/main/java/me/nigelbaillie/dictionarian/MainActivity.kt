package me.nigelbaillie.dictionarian

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

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
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                Log.d("NIGELMSG", "MainActivity.onCreate called with SEND intent")
                model.analyze(intent, contentResolver)
            }
            else -> {
                Log.d("NIGELMSG", "MainActivity.onCreate called with no intent")
                // ...
            }
        }
    }
}
