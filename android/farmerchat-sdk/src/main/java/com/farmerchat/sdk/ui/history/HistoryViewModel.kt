package com.farmerchat.sdk.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.domain.model.history.ConversationListItem
import com.farmerchat.sdk.domain.usecase.HistoryUseCase
import com.farmerchat.sdk.preference.SdkPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val conversations: List<ConversationListItem> = emptyList(),
    val groupedConversations: Map<String, List<ConversationListItem>> = emptyMap(),
    // isLoading = true on initial load (shows shimmer, hides empty state)
    val isLoading: Boolean = true,
    // isRefreshing = true only when user explicitly pulls to refresh
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 1
)

internal class HistoryViewModel(
    private val historyUseCase: HistoryUseCase,
    private val preferenceManager: SdkPreferenceManager
) : ViewModel() {

    // Start with isLoading = true so shimmer shows immediately on first frame,
    // preventing any flash of the empty-state placeholder.
    private val _state = MutableStateFlow(HistoryUiState(isLoading = true))
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        loadPage(page = 1, isUserRefresh = false)
    }

    /** Called by pull-to-refresh gesture — shows spinner, not shimmer. */
    fun refresh() {
        loadPage(page = 1, isUserRefresh = true)
    }

    /** Called when the list scrolls near the bottom. */
    fun loadNextPage() {
        val current = _state.value
        if (current.isLoading || current.isRefreshing || !current.hasMore) return
        loadPage(page = current.currentPage + 1, isUserRefresh = false)
    }

    private fun loadPage(page: Int, isUserRefresh: Boolean) {
        if (isUserRefresh) {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }
        } else {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
        }

        val userId = preferenceManager.getUserId() ?: run {
            _state.update { it.copy(isLoading = false, isRefreshing = false) }
            return
        }

        viewModelScope.launch {
            historyUseCase.getConversationList(page, userId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val newItems = result.data
                        val allItems = if (page == 1) newItems else _state.value.conversations + newItems
                        _state.update {
                            it.copy(
                                conversations = allItems,
                                groupedConversations = groupConversations(allItems),
                                isLoading = false,
                                isRefreshing = false,
                                hasMore = newItems.isNotEmpty(),
                                currentPage = page,
                                errorMessage = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message ?: "Failed to load conversations"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun groupConversations(
        items: List<ConversationListItem>
    ): Map<String, List<ConversationListItem>> {
        val grouped = LinkedHashMap<String, MutableList<ConversationListItem>>()
        for (item in items) {
            val key = item.grouping ?: "Other"
            grouped.getOrPut(key) { mutableListOf() }.add(item)
        }
        return grouped
    }
}
