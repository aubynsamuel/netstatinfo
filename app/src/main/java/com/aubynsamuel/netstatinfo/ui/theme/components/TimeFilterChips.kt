package com.aubynsamuel.netstatinfo.ui.theme.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aubynsamuel.netstatinfo.TimePeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterChips(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
//            .padding(vertical = 8.dp)
        ,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) },
                leadingIcon = if (period == selectedPeriod) {
                    { Icon(Icons.Filled.Done, contentDescription = "Selected period") }
                } else null
            )
        }
    }
}