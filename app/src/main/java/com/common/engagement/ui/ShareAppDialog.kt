package com.common.engagement.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun ShareAppDialog(
    appName: String = "CoinBridge",
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("share_app_dialog"),
        title = {
            Text(
                text = stringResource(R.string.share_dialog_title, appName),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = stringResource(R.string.share_dialog_msg, appName))
        },
        confirmButton = {
            TextButton(
                onClick = onShare,
                modifier = Modifier.testTag("share_confirm_btn")
            ) {
                Text(stringResource(R.string.share_dialog_confirm), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("share_dismiss_btn")
            ) {
                Text(stringResource(R.string.action_maybe_later), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
