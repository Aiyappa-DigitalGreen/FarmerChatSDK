package com.farmerchat.sdk.domain.usecase

import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.data.repository.HistoryRepository
import com.farmerchat.sdk.domain.model.history.ConversationListItem
import kotlinx.coroutines.flow.Flow

internal class HistoryUseCase(
    private val historyRepository: HistoryRepository
) {

    fun getConversationList(page: Int = 1, userId: String): Flow<ApiResult<List<ConversationListItem>>> {
        return historyRepository.getConversationList(page, userId)
    }
}
