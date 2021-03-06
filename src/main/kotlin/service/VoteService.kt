package service

import model.*
import org.jetbrains.exposed.sql.*
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
            .innerJoin(Users, {Candidates.userId}, { userId })
            .slice(
                Users.firstName,
                Users.lastName,
                Elections.electionName,
                Positions.positionName,
                Votes.candidateId.countDistinct()
            )
            .select{
                (Elections.electionId eq electionId)
            }
            .groupBy(
                Votes.candidateId,
                Elections.electionName,
                Users.firstName,
                Users.lastName,
                Positions.positionName
            )
            .map { toVoteCount(it) }
    }

    private fun toVoteCount(row: ResultRow): VoteCount = VoteCount(
        candidateFirstName = row[Users.firstName],
        candidateLastName = row[Users.lastName],
        electionName = row[Elections.electionName],
        positionName = row[Positions.positionName],
        votes = row[Votes.candidateId.countDistinct()],
    )
}