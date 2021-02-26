package model

import org.jetbrains.exposed.sql.Table
import java.util.*

object Elections : Table() {
    val electionId = uuid("electionId")
    val electionName = varchar("electionName", 255)
    val startDate = long("startDate")
    val stopDate = long("stopDate")
    val createdBy = reference("createdBy", Users.userId)
    val dateModified = long("dateModified")
    override val primaryKey = PrimaryKey(electionId)
}

data class Election(
    val electionId: UUID,
    val electionName: String,
    val startDate: Long,
    val stopDate: Long,
    val createdBy: UUID,
    val dateModified: Long
)

data class NewElection(
    val electionName: String,
    val startDate: Long,
    val stopDate: Long,
)