package com.tuta.auto

import android.app.Application
import com.tuta.auto.data.AppDatabase
import com.tuta.auto.data.repository.AccountRepository
import com.tuta.auto.data.repository.MessageRepository
import com.tuta.auto.util.PreferenceManager

class TutaApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var accountRepository: AccountRepository
        private set
    lateinit var messageRepository: MessageRepository
        private set
    lateinit var preferenceManager: PreferenceManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        accountRepository = AccountRepository(database.accountDao())
        messageRepository = MessageRepository(database.messageDao())
        preferenceManager = PreferenceManager(this)
    }
}
