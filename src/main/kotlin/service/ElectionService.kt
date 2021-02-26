package service

import model.Election
import model.Elections
import model.NewElection
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery
import java.util.UUID

class ElectionService {

    suspend fun getAllElections(): List<Election> = dbQuery {
        Elections.selectAll().map { toElection(it)}
    }

    suspend fun getElection(electionId: UUID): Election? = dbQuery {
        Elections.select{
            (Elections.electionId eq electionId)
        }.mapNotNull { toElection(it) }.singleOrNull()
    }

    suspend fun createElection(election: NewElection, userId: UUID): Election {
        val thisElectionId = UUID.randomUUID()
        dbQuery {
            Elections.insert {
                it[electionId] = thisElectionId
                it[electionName] = election.electionName
                it[startDate] = election.startDate
                it[stopDate] = election.stopDate
                it[createdBy] = userId
                it[dateModified] = System.currentTimeMillis()
            }
        }

        return getElection(thisElectionId)!!
    }

    private fun toElection(row: ResultRow): Election = Election(
        electionId = row[Elections.electionId],
        electionName = row[Elections.electionName],
        startDate = row[Elections.startDate],
        stopDate = row[Elections.stopDate],
        createdBy = row[Elections.createdBy],
        dateModified = row[Elections.dateModified]
    )
}