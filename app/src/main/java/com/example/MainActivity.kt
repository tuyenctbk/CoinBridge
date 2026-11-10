package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.example.ui.CoinBridgeApp
import com.example.ui.theme.CoinBridgeTheme
import com.example.ui.viewmodel.CoinBridgeViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: CoinBridgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoinBridgeTheme {
                CoinBridgeApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBiometricStatus()
    }
}
