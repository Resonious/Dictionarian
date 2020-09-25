package me.nigelbaillie.dictionarian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import me.nigelbaillie.dictionarian.ui.DictionarianTheme

class AnalyzeFragment  : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                DictionarianTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        Column() {
                            Text("YES I AM")
                            Text("ANOTHER ONE")
                        }
                    }
                }
            }
        }
    }
}