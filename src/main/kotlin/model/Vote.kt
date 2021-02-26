package model

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object Votes : Table() {
    val candidateId = reference("candidateId", Candidates.candidateId)
    val voterId = reference("voterId", Voters.voterId)
    val dateVoted = long("dateVoted")
    override val primaryKey = PrimaryKey(candidateId, voterId)
}

data class Vote (
    val candidateId: UUID,
    var voterId: UUID?,
    val dateVoted: Long? = System.currentTimeMillis()
)

data class VoteCount (
    val candidateId: UUID,
    val votes: Long,
    val electionName: String
)