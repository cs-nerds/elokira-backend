package model

import java.util.UUID

enum class ChangeType { CREATE, UPDATE, DELETE}

data class Notification<T>(val type: ChangeType, val id: UUID, val entity: T)
