package de.readeckapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.readeckapp.R
import de.readeckapp.domain.model.AutoSyncTimeframe
import de.readeckapp.ui.theme.Typography

@Composable
fun AutoSyncTimeframeDialog(
    autoSyncTimeframeOptions: List<AutoSyncTimeframeOption>,
    onDismissRequest: () -> Unit,
    onElementSelected: (selected: AutoSyncTimeframe) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(
                    minWidth = 280.dp,
                    maxWidth = 560.dp
                ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_sync_dialog_support_text),
                    style = Typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                HorizontalDivider()
                autoSyncTimeframeOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = option.selected,
                                onClick = { onElementSelected(option.autoSyncTimeframe) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option.selected,
                            onClick = null
                        )
                        Text(
                            text = stringResource(option.label),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                HorizontalDivider()
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        modifier = Modifier.padding(top = 24.dp),
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AutoSyncTimeframeDialogPreview() {
    AutoSyncTimeframeDialog(
        autoSyncTimeframeOptions = listOf(
            AutoSyncTimeframeOption(
                autoSyncTimeframe = AutoSyncTimeframe.MANUAL,
                label = R.string.auto_sync_timeframe_manual,
                selected = true
            ),
            AutoSyncTimeframeOption(
                autoSyncTimeframe = AutoSyncTimeframe.HOURS_06,
                label = R.string.auto_sync_timeframe_06_hours,
                selected = false
            )
        ),
        onDismissRequest = {},
        onElementSelected = {}
    )
}
