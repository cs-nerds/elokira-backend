package service

import model.*
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery
import java.util.*


class VoterService {

    suspend fun registerVoter(voter: NewVoter) = dbQuery {
        Voters.insert {
            it[voterId] = voter.voterId
            it[electionId] = voter.electionId
            it[voted] = voter.voted
            it[userId] = voter.userId
            it[registrationDate] = voter.registrationDate
        }
    }

    suspend fun updateToVoted(voterId: UUID) = dbQuery {
        Voters.update({Voters.voterId eq voterId}) {
            it[voted] = true
        }
    }
}