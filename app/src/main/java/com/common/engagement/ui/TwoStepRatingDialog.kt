package com.common.engagement.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun TwoStepRatingDialog(
    appName: String = "CoinBridge",
    onLaunchStoreReview: () -> Unit,
    onRedirectInternalFeedback: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    if (step == 1) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier.testTag("two_step_rating_dialog"),
            title = {
                Text(
                    text = stringResource(R.string.rating_dialog_title, appName),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.rating_dialog_question))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        step = 2
                        onLaunchStoreReview()
                    },
                    modifier = Modifier.testTag("rating_confirm_positive_btn")
                ) {
                    Text(stringResource(R.string.rating_dialog_positive), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onRedirectInternalFeedback()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("rating_needs_work_btn")
                ) {
                    Text(stringResource(R.string.rating_dialog_feedback), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
