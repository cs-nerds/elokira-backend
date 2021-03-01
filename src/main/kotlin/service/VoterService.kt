package service

import model.*
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery
import java.util.*


class VoterService {

    suspend fun registerVoter(voter: NewVoter): Voter{
        val thisVoterId = UUID.randomUUID()
        dbQuery {
            Voters.insert {
                it[voterId] = thisVoterId
                it[electionId] = voter.electionId
                it[voted] = false
                it[userId] = voter.userId
                it[registrationDate] = System.currentTimeMillis()
            }
        }

        return getVoter(voter.userId, voter.electionId)!!
    }

    suspend fun getVoter(userId: UUID, electionId: UUID): Voter? = dbQuery {
        Voters.select{
            (Voters.userId eq userId and (Voters.electionId eq electionId))
        }.mapNotNull { toVoter(it) }.singleOrNull()
    }

    suspend fun getVoterById(voterId: UUID): Voter = dbQuery {
        Voters.select{
            (Voters.voterId eq voterId)
        }.mapNotNull { toVoter(it) }.single()
    }

    suspend fun updateToVoted(voterId: UUID) = dbQuery {
        Voters.update({Voters.voterId eq voterId}) {
            it[voted] = true
        }

        getVoterById(voterId)
    }

    private fun toVoter(row: ResultRow): Voter = Voter(
        voterId = row[Voters.voterId],
        userId = row[Voters.userId],
        electionId = row[Voters.electionId],
        registrationDate = row[Voters.registrationDate],
        voted = row[Voters.voted]
    )
}