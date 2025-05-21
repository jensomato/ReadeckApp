package de.readeckapp.ui.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.readeckapp.R

@Composable
fun NotificationRationaleDialog(
    onRationaleDialogConfirm: () -> Unit,
    onRationaleDialogDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRationaleDialogDismiss,
        title = { Text(stringResource(R.string.auto_sync_notification_rationale_dialog_title)) },
        text = { Text(stringResource(R.string.auto_sync_notification_rationale_dialog_text)) },
        confirmButton = {
            Button(onClick = onRationaleDialogConfirm) {
                Text(stringResource(R.string.auto_sync_notification_rationale_dialog_grant_button))
            }
        },
        dismissButton = {
            Button(onClick = onRationaleDialogDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}