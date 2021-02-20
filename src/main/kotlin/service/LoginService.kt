package service

import model.*
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery

class LoginService {

    private val listeners = mutableMapOf<Int, suspend (Notification<Login?>) -> Unit>()

    fun addChangeListener(loginId: Int, listener: suspend (Notification<Login?>) -> Unit) {
        listeners[loginId] = listener
    }

    fun removeChangeListener(loginId: Int) = listeners.remove(loginId)

    private suspend fun onChange(type: ChangeType, loginId: Int, entity: Login? = null) {
        listeners.values.forEach {
            it.invoke(Notification(type, loginId, entity))
        }
    }

    suspend fun getAllLogins(): List<Login> = dbQuery {
        Logins.selectAll().map { toLogin(it) }
    }

    suspend fun getLogin(loginId: Int): Login? = dbQuery {
        Logins.select{
            (Logins.loginId eq loginId)
        }.mapNotNull { toLogin(it) }.singleOrNull()
    }

    suspend fun updateLogin(login: Login): Login? {

        return when (val loginId =  login.loginId) {
            null -> {
                addLogin(login)
            }
            else -> {
                dbQuery {
                    Logins.update({Logins.loginId eq loginId}) {
                        it[activated] = login.activated ?: false
                    }
                }
                dbQuery {
                    getLogin(loginId).also {
                        onChange(ChangeType.UPDATE, loginId, it)
                    }
                }
            }
        }
    }

    suspend fun addLogin(login: Login): Login {
        var loginId = 0
        dbQuery {
            loginId = (Logins.insert {
                it[loginCode] = login.loginCode
                it[userId] = login.userId!!
                it[activated] = login.activated!!
            } get Logins.loginId)
        }

        return getLogin(loginId)!!.also{
            onChange(ChangeType.CREATE, loginId, it)
        }
    }

    suspend fun deleteLogin(loginId: Int): Boolean { // log out
        return dbQuery {
            Logins.deleteWhere {
                Logins.loginId eq loginId
            } > 0
        }.also {
            if (it) onChange(ChangeType.DELETE, loginId)
        }
    }

    private fun toLogin(row: ResultRow): Login = Login(
        loginId = row[Logins.loginId],
        loginCode = row[Logins.loginCode],
        userId = row[Logins.userId],
        activated = row[Logins.activated]
    )
}