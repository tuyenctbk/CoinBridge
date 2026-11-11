package com.example

import android.os.Bundle
import android.view.KeyEvent
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // TV remote and hardware keyboard direct numeric/calc input
        val keyChar = when (keyCode) {
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> "0"
            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> "1"
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> "2"
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> "3"
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> "4"
            KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> "5"
            KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> "6"
            KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> "7"
            KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> "8"
            KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> "9"
            KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_NUMPAD_DOT -> "."
            KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_NUMPAD_ADD -> "+"
            KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> "-"
            KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> "×"
            KeyEvent.KEYCODE_SLASH, KeyEvent.KEYCODE_NUMPAD_DIVIDE -> "÷"
            KeyEvent.KEYCODE_EQUALS, KeyEvent.KEYCODE_NUMPAD_EQUALS -> "="
            KeyEvent.KEYCODE_DEL -> "⌫"
            KeyEvent.KEYCODE_C -> "C"
            else -> null
        }
        if (keyChar != null) {
            viewModel.onHardwareKeyInput(keyChar)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
