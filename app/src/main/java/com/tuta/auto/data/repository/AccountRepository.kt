package com.tuta.auto.data.repository

import com.tuta.auto.data.dao.AccountDao
import com.tuta.auto.data.model.Account
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun getAccountById(id: Long): Account? = accountDao.getAccountById(id)

    suspend fun getAccountByEmail(email: String): Account? = accountDao.getAccountByEmail(email)

    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)

    suspend fun deleteAll() = accountDao.deleteAll()
}
