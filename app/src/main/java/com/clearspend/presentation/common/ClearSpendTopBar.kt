package com.clearspend.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clearspend.presentation.theme.ClearSpendColors

@Composable
fun ClearSpendTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        color = ClearSpendColors.Surface0,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = ClearSpendColors.TextPrimary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = ClearSpendColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )

            actions?.invoke(this)
        }
    }
}
