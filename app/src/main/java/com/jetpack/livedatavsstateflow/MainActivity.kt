package com.jetpack.livedatavsstateflow

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.jetpack.livedatavsstateflow.ui.theme.LiveDataVsStateFlowTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged

class MainActivity : AppCompatActivity() {
    private val viewModel: LiveDataVsStateFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.liveData
            .distinctUntilChanged()
            .observe(this) {
                findViewById<TextView>(R.id.my_text_live).text = it
            }

        lifecycleScope.launchWhenCreated {
            viewModel.stateFlow.flowWithLifecycle(
                this@MainActivity.lifecycle,
                Lifecycle.State.STARTED
            )
                .distinctUntilChanged()
                .collect {
                    findViewById<TextView>(R.id.my_text_state).text = it
                }
        }

        viewModel
            .liveDataTrigger
            .distinctUntilChanged()
            .observe(this) {
                findViewById<Button>(R.id.my_button_live).text = it
            }

        lifecycleScope.launchWhenCreated {
            viewModel.stateFlowTrigger
                .flowWithLifecycle(
                    this@MainActivity.lifecycle,
                    Lifecycle.State.STARTED
                )
                .distinctUntilChanged()
                .collect {
                    findViewById<Button>(R.id.my_button_state).text = it
                }
        }

        findViewById<Button>(R.id.my_button_live).setOnClickListener { viewModel.triggerLive() }
        findViewById<Button>(R.id.my_button_state).setOnClickListener { viewModel.triggerState() }
    }
}
























