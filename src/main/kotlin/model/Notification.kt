package model

import java.util.*

enum class ChangeType { CREATE, UPDATE, DELETE}

data class Notification<T>(val type: ChangeType, val id: UUID, val entity: T)
