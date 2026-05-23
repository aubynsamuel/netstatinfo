package com.aubynsamuel.netstatinfo.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aubynsamuel.netstatinfo.data.model.TimePeriod

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeFilterChips(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            FilterChip(
                selected = isSelected,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = period.displayName,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Selected",
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    borderWidth = if (isSelected) 0.dp else 1.dp,
                    selectedBorderWidth = 0.dp,
                )
            )
        }
    }
}
