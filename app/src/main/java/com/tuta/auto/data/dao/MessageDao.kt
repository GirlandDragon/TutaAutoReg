package com.tuta.auto.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tuta.auto.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE accountId = :accountId ORDER BY receivedAt DESC")
    fun getMessagesForAccount(accountId: Long): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("DELETE FROM messages WHERE accountId = :accountId")
    suspend fun deleteMessagesForAccount(accountId: Long)
}
