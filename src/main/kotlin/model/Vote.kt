package model

import org.jetbrains.exposed.sql.Table

object Votes : Table() {
    val candidateId = reference("candidateId", Candidates.candidateId)
    val voterId = reference("voterId", Voters.voterId)
    val dateVoted = long("dateVoted")
    override val primaryKey = PrimaryKey(candidateId, voterId)
}