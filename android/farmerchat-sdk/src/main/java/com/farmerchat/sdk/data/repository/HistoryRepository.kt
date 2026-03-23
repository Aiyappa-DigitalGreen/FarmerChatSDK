package com.farmerchat.sdk.data.repository

import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.base.BaseUseCase
import com.farmerchat.sdk.data.remote.ChatApiService
import com.farmerchat.sdk.domain.model.history.ConversationListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class HistoryRepository(
    private val apiService: ChatApiService
) : BaseUseCase() {

    fun getConversationList(page: Int, userId: String): Flow<ApiResult<List<ConversationListItem>>> = flow {
        emit(executeApiCall("getConversationList") { apiService.getConversationList(page, userId) })
    }
}
