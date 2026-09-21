/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.feeder.ui.pages.plugins

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.account.AccountConfig
import com.saulhdev.feeder.ui.components.dialog.DialogNegativeButton
import com.saulhdev.feeder.ui.components.dialog.DialogPositiveButton
import java.util.UUID

@Composable
fun NextcloudConfigDialog(
    account: AccountConfig.NextcloudNewsAccount? = null,
    isTestingConnection: Boolean = false,
    testConnectionMessage: String? = null,
    testConnectionSuccess: Boolean? = null,
    onTestConnection: (String, String, String) -> Unit,
    onSave: (AccountConfig.NextcloudNewsAccount) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    var serverUrl by remember { mutableStateOf(account?.serverUrl ?: "") }
    var username by remember { mutableStateOf(account?.username ?: "") }
    var token by remember { mutableStateOf(account?.passwordOrToken ?: "") }
    var syncInterval by remember { mutableIntStateOf(account?.syncIntervalMinutes ?: 30) }
    var backgroundSync by remember { mutableStateOf(account?.backgroundSyncEnabled ?: true) }
    var excludedFolders by remember {
        mutableStateOf(
            account?.excludedFolders?.joinToString(", ") ?: ""
        )
    }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (account == null) stringResource(id = R.string.service_nextcloud_news)
                    else stringResource(id = R.string.service_nextcloud_news) + " (${account.username})",
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .weight(1f, false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text(stringResource(id = R.string.nextcloud_server_url)) },
                        placeholder = { Text(stringResource(id = R.string.nextcloud_server_url_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(stringResource(id = R.string.nextcloud_username)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text(stringResource(id = R.string.nextcloud_password_token)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = excludedFolders,
                        onValueChange = { excludedFolders = it },
                        label = { Text(stringResource(id = R.string.excluded_folders)) },
                        placeholder = { Text(stringResource(id = R.string.excluded_folders_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.sync_background_workmanager),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = backgroundSync,
                            onCheckedChange = { backgroundSync = it }
                        )
                    }

                    // Test Connection Button & Result
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onTestConnection(serverUrl, username, token) },
                            enabled = serverUrl.isNotBlank() && username.isNotBlank() && token.isNotBlank() && !isTestingConnection,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(id = R.string.nextcloud_testing_connection))
                            } else {
                                Text(stringResource(id = R.string.nextcloud_test_connection))
                            }
                        }
                    }

                    testConnectionMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (testConnectionSuccess == true) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = if (account != null && onDelete != null) Arrangement.SpaceBetween else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (account != null && onDelete != null) {
                        TextButton(
                            shape = MaterialTheme.shapes.large,
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Text(
                                text = stringResource(id = R.string.delete_account),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DialogNegativeButton(
                            onClick = onDismiss
                        )
                        DialogPositiveButton(
                            text = stringResource(id = R.string.action_save),
                            enabled = serverUrl.isNotBlank() && username.isNotBlank() && token.isNotBlank(),
                            onClick = {
                                val parsedExcluded = excludedFolders.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                    .toSet()

                                val finalAccount = AccountConfig.NextcloudNewsAccount(
                                    id = account?.id ?: UUID.randomUUID().toString(),
                                    displayName = "${username.trim()}@${
                                        serverUrl.trim().substringAfter("://").substringBefore("/")
                                    }",
                                    serverUrl = serverUrl.trim(),
                                    username = username.trim(),
                                    passwordOrToken = token.trim(),
                                    syncIntervalMinutes = syncInterval,
                                    backgroundSyncEnabled = backgroundSync,
                                    excludedFolders = parsedExcluded,
                                    isEnabled = account?.isEnabled ?: true,
                                )
                                onSave(finalAccount)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation && account != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = stringResource(id = R.string.delete_account_confirm),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.delete_account_confirm_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete?.invoke(account.id)
                        onDismiss()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_account),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}
