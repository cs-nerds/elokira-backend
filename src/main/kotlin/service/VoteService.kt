package service

import model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.countDistinct
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import service.DatabaseFactory.dbQuery
import java.util.*

class VoteService {

    suspend fun addVote(vote: Vote) = dbQuery {
        Votes.insert {
            it[candidateId] = vote.candidateId
            it[voterId] = vote.voterId!!
            it[dateVoted] = vote.dateVoted!!
        }
    }

    suspend fun countVotes(electionId: UUID): List<VoteCount> = dbQuery {
        Votes
            .innerJoin(Candidates)
            .innerJoin(Positions)
            .innerJoin(Elections)
            .slice(
                Votes.candidateId,
                Elections.electionName,
                Votes.candidateId.countDistinct()
            )
            .select{
                (Elections.electionId eq electionId)
            }
            .map { toVoteCount(it) }
    }

    private fun toVoteCount(row: ResultRow): VoteCount = VoteCount(
        candidateId = row[Votes.candidateId],
        votes = row[Votes.candidateId.countDistinct()],
        electionName = row[Elections.electionName]
    )
}