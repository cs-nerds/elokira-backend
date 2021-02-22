package service

import model.*
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery
import java.util.*

class UserService {

    private val listeners = mutableMapOf<Int, suspend (Notification<User?>) -> Unit>()

    fun addChangeListener(userId: Int, listener: suspend (Notification<User?>) -> Unit) {
        listeners[userId] = listener
    }

    fun removeChangeListener(userId: Int) = listeners.remove(userId)

    private suspend fun onChange(type: ChangeType, userId: UUID, entity: User? = null) {
        listeners.values.forEach{
            it.invoke(Notification(type,userId,entity))
        }
    }

    suspend fun getAllUsers(): List<User> = dbQuery {
        Users.selectAll().map { toUser(it) }
    }

    suspend fun getUser(userId: UUID): User? = dbQuery {
        Users.select{
            (Users.userId eq userId)
        }.mapNotNull { toUser(it) }.singleOrNull()
    }

    suspend fun getUserByIdNumber(idNumber: String): User? = dbQuery {
        Users.select{
            (Users.idNumber eq idNumber)
        }.mapNotNull { toUser(it) }.singleOrNull()
    }

    suspend fun updateUser(user: NewUser): User? {

        return when (val userId = user.userId) {
            null -> {
                addUser(user)
            }
            else -> {
                dbQuery {
                    Users.update({Users.userId eq userId}) {
                        it[firstName] = user.firstName
                        it[lastName] = user.lastName
                        it[phoneNumber] = user.phoneNumber
                        it[idNumber] = user.idNumber
                        it[dateUpdated] = System.currentTimeMillis()
                    }
                }
                dbQuery {
                    getUser(userId).also {
                        onChange(ChangeType.UPDATE, userId, it)
                    }
                }
            }
        }
    }

    suspend fun addUser(user: NewUser): User {
        val thisUserId: UUID = user.userId!!
        dbQuery {
            Users.insert{
                it[userId] = thisUserId
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[phoneNumber] = user.phoneNumber
                it[idNumber] = user.idNumber
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
                it[lastUpdatedBy] = thisUserId
                it[admin] = false
            }
        }

        return getUser(thisUserId)!!.also {
            onChange(ChangeType.CREATE, thisUserId, it)
        }
    }

    suspend fun deleteUser(userId: UUID): Boolean {
        return dbQuery {
            Users.deleteWhere {
                Users.userId eq userId
            } > 0
        }.also {
            if (it) onChange(ChangeType.DELETE, userId)
        }
    }

    private fun toUser(row: ResultRow): User = User(
        userId = row[Users.userId],
        firstName = row[Users.firstName],
        lastName = row[Users.lastName],
        phoneNumber = row[Users.phoneNumber],
        idNumber = row[Users.idNumber],
        dateCreated = row[Users.dateCreated],
        dateUpdated = row[Users.dateUpdated],
        lastUpdatedBy = row[Users.lastUpdatedBy],
        admin = row[Users.admin]
    )
}