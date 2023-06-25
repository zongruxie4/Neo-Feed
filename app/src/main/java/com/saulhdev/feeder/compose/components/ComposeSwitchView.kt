package com.saulhdev.feeder.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulhdev.feeder.compose.util.addIf

@Composable
fun ComposeSwitchView(
    title: String,
    modifier: Modifier = Modifier,
    summary: String = "",
    iconId: Int = 0,
    onCheckedChange: ((Boolean) -> Unit),
    isChecked: Boolean = false,
    isEnabled: Boolean = true,
    showDivider: Boolean = false,
    applyPaddings: Boolean = false,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    index: Int = -1,
    groupSize: Int = -1,
) {
    val (checked, check) = remember(isChecked) { mutableStateOf(isChecked) }
    val rank = (index + 1f) / groupSize
    val base = index.toFloat() / groupSize
    Column(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = if (base == 0f) 16.dp else 6.dp,
                    topEnd = if (base == 0f) 16.dp else 6.dp,
                    bottomStart = if (rank == 1f) 16.dp else 6.dp,
                    bottomEnd = if (rank == 1f) 16.dp else 6.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation((rank * 24).dp))
            .clickable(enabled = isEnabled) {
                check(!checked)
                onCheckedChange(!checked)
            }
    ) {
        if (showDivider) {
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .addIf(applyPaddings) {
                    padding(horizontal = horizontalPadding, vertical = verticalPadding)
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconId != 0) {

                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = "",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12F))
                )

                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp
                )
                if (summary != "") {
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Switch(
                modifier = Modifier
                    .height(24.dp),
                checked = checked,
                onCheckedChange = {
                    check(it)
                    onCheckedChange(it)
                },
                enabled = isEnabled,
            )
        }
    }
}