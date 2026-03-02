package com.bono.mentalbot.data.remote.model

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageDto
)