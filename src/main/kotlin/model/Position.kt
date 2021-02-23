package model

import org.jetbrains.exposed.sql.Table
import java.util.*

object Positions : Table() {
    val positionId = uuid("positionId")
    val positionName = varchar("positionName", 255)
    val electionId = reference("electionId", Elections.electionId)
    val createdBy = reference("createdBy", Users.userId)
    val dateModified = long("dateModified")
    override val primaryKey = PrimaryKey(positionId)
}

data class Position(
    val positionId: UUID,
    val positionName: String,
    val electionId: UUID,
    val createdBy: UUID,
    val dateModified: Long
)