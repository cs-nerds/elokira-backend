package model

import org.jetbrains.exposed.sql.Table

object Elections : Table() {
    val electionId = uuid("electionId")
    val electionName = varchar("electionName", 255)
    val startDate = long("startDate")
    val stopDate = long("stopDate")
    val createdBy = reference("createdBy", Users.userId)
    val dateModified = long("dateModified")
    override val primaryKey = PrimaryKey(electionId)
}