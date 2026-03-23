package com.farmerchat.sdk.data.repository

import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.base.BaseUseCase
import com.farmerchat.sdk.data.remote.ChatApiService
import com.farmerchat.sdk.domain.model.conversation.NewConversationRequest
import com.farmerchat.sdk.domain.model.conversation.NewConversationResponse

internal class ConversationRepository(
    private val apiService: ChatApiService
) : BaseUseCase() {

    suspend fun newConversation(request: NewConversationRequest): ApiResult<NewConversationResponse> {
        return executeApiCall("newConversation") { apiService.newConversation(request) }
    }
}
