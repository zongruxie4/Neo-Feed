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

import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.account.AccountConfig
import com.saulhdev.feeder.ui.components.dialog.DialogNegativeButton
import com.saulhdev.feeder.ui.components.dialog.DialogPositiveButton
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.viewmodels.MastodonAuthViewModel

@Composable
fun MastodonConfigDialog(
    account: AccountConfig.MastodonAccount? = null,
    authViewModel: MastodonAuthViewModel = koinNeoViewModel(),
    onSave: (AccountConfig.MastodonAccount) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val authUiState by authViewModel.uiState.collectAsState()
    var instance by remember { mutableStateOf("") }

    var backgroundSync by remember { mutableStateOf(account?.backgroundSyncEnabled ?: true) }
    var requireLink by remember { mutableStateOf(account?.requireLink ?: true) }
    var requireImage by remember { mutableStateOf(account?.requireImage ?: true) }
    var excludeReplies by remember { mutableStateOf(account?.excludeReplies ?: true) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val handleDismiss = {
        authViewModel.reset()
        onDismiss()
    }

    LaunchedEffect(authUiState.launchUrl) {
        authUiState.launchUrl?.let { url ->
            try {
                CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(context, url.toUri())
            } catch (_: Exception) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
            authViewModel.onLaunchHandled()
            handleDismiss()
        }
    }

    Dialog(
        onDismissRequest = handleDismiss,
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
                    text = if (account != null) "${account.username}@${account.instance}"
                    else stringResource(id = R.string.service_mastodon),
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .weight(1f, false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (account == null) {
                        Text(
                            text = stringResource(id = R.string.service_mastodon_desc),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = instance,
                            onValueChange = {
                                instance = it
                                if (authUiState.error != null) {
                                    authViewModel.dismissError()
                                }
                            },
                            label = { Text(text = stringResource(id = R.string.mastodon_instance_hint)) },
                            placeholder = { Text("mastodon.social") },
                            singleLine = true,
                            enabled = !authUiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (authUiState.isLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.mastodon_auth_loading),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        authUiState.error?.let { error ->
                            Text(
                                text = stringResource(id = R.string.mastodon_auth_error, error),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.mastodon_instance,
                                    account.instance
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.mastodon_username,
                                    account.username
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.mastodon_require_link),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = requireLink,
                                onCheckedChange = { requireLink = it }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.mastodon_require_image),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = requireImage,
                                onCheckedChange = { requireImage = it }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.mastodon_exclude_replies),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = excludeReplies,
                                onCheckedChange = { excludeReplies = it }
                            )
                        }
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
                            onClick = handleDismiss
                        )
                        if (account != null) {
                            DialogPositiveButton(
                                text = stringResource(id = R.string.action_save),
                                onClick = {
                                    onSave(
                                        account.copy(
                                            backgroundSyncEnabled = backgroundSync,
                                            requireLink = requireLink,
                                            requireImage = requireImage,
                                            excludeReplies = excludeReplies
                                        )
                                    )
                                    handleDismiss()
                                }
                            )
                        } else {
                            DialogPositiveButton(
                                text = stringResource(id = R.string.connect_account),
                                enabled = instance.isNotBlank() && !authUiState.isLoading,
                                onClick = {
                                    authViewModel.startAuth(instance)
                                }
                            )
                        }
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
