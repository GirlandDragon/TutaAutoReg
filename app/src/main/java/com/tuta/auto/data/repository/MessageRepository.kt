package com.tuta.auto.data.repository

import com.tuta.auto.data.dao.MessageDao
import com.tuta.auto.data.model.Message
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    fun getMessagesForAccount(accountId: Long): Flow<List<Message>> =
        messageDao.getMessagesForAccount(accountId)

    suspend fun insertMessage(message: Message): Long = messageDao.insertMessage(message)

    suspend fun insertMessages(messages: List<Message>) = messageDao.insertMessages(messages)

    suspend fun deleteMessage(message: Message) = messageDao.deleteMessage(message)

    suspend fun deleteMessagesForAccount(accountId: Long) =
        messageDao.deleteMessagesForAccount(accountId)
}
