package com.clearspend.presentation.screens.coach

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.clearspend.domain.model.CoachInsight
import com.clearspend.domain.repository.CoachRepository
import com.clearspend.presentation.common.ClearSpendTopBar
import com.clearspend.presentation.theme.ClearSpendColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CoachViewModel @Inject constructor(
    coachRepo: CoachRepository
) : ViewModel() {
    val insights: StateFlow<List<CoachInsight>> = coachRepo.observeRecent().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

@Composable
fun CoachScreen(
    onNavigateBack: () -> Unit,
    viewModel: CoachViewModel = hiltViewModel()
) {
    val insights by viewModel.insights.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        topBar = {
            ClearSpendTopBar(title = "AI Financial Coach", onBack = onNavigateBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = ClearSpendColors.Amber500.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ClearSpendColors.Amber500.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Weekly Savings Intelligence", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.Amber500)
                            Text(
                                "Your coach generates weekly summaries every Sunday morning based on your spending patterns.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClearSpendColors.TextPrimary
                            )
                        }
                    }
                }
            }

            if (insights.isEmpty()) {
                item {
                    Surface(
                        color = ClearSpendColors.Surface1,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ClearSpendColors.Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Reports Generated Yet", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                            Text("Reports generate automatically as you log expenses.", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
                        }
                    }
                }
            } else {
                items(insights, key = { it.id }) { insight ->
                    Surface(
                        color = ClearSpendColors.Surface1,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ClearSpendColors.Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Week of ${insight.weekStartDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ClearSpendColors.Amber500
                                )
                                val changeText = if (insight.changeVsLastWeek > 0) "+${insight.changeVsLastWeek.toInt()}% vs last week" else "${insight.changeVsLastWeek.toInt()}% vs last week"
                                Text(
                                    text = changeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (insight.changeVsLastWeek > 0) ClearSpendColors.RedDanger else ClearSpendColors.GreenSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = insight.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ClearSpendColors.TextPrimary
                            )
                        }
                    }
                }
            }

            // Legal & Regulatory Disclaimer
            item {
                Surface(
                    color = ClearSpendColors.Surface1,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Disclaimer: AI Financial Coach provides observational budgeting feedback. It does not provide certified financial, tax, or investment advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClearSpendColors.TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
