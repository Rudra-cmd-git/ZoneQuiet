package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SearchResult
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.IndigoNeon
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    searchResults: List<SearchResult>,
    onSelectResult: (SearchResult) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search address, landmark, or city...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = IndigoNeon,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = IndigoNeon
                    )
                } else if (query.isNotBlank()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoNeon,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = NavySurfaceVariant,
                unfocusedContainerColor = NavySurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("location_search_field")
        )

        // Dropdown Search Suggestions
        AnimatedVisibility(visible = searchResults.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NavySurface,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Column {
                    searchResults.forEachIndexed { index, result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    focusManager.clearFocus()
                                    onSelectResult(result)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("search_result_item_$index"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = EmeraldNeon,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "${String.format("%.4f", result.latitude)}, ${String.format("%.4f", result.longitude)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                )
                            }
                        }
                        if (index < searchResults.lastIndex) {
                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
