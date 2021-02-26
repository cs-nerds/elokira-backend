package service

import model.NewPosition
import model.Position
import model.Positions
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery
import java.util.UUID

class PositionService {

    suspend fun getAllPositions(): List<Position> = dbQuery {
        Positions.selectAll().map { toPosition(it)}
    }

    suspend fun getPosition(positionId: UUID): Position? = dbQuery {
        Positions.select{
            (Positions.positionId eq positionId)
        }.mapNotNull { toPosition(it) }.singleOrNull()
    }

    suspend fun getElectionPositions(electionId: UUID): List<Position> = dbQuery {
        Positions.select{
            (Positions.electionId eq electionId)
        }.mapNotNull {  toPosition(it)  }
    }

    suspend fun addElectionPosition(position: NewPosition, userId: UUID): Position {
        val thisPositionId = UUID.randomUUID()
        dbQuery {
            Positions.insert{
                it[positionId] = thisPositionId
                it[positionName] = position.positionName
                it[electionId] = position.electionId
                it[createdBy] = userId
                it[dateModified] = System.currentTimeMillis()
            }
        }

        return getPosition(thisPositionId)!!
    }

    private fun toPosition(row: ResultRow): Position = Position(
        positionId = row[Positions.positionId],
        positionName = row[Positions.positionName],
        electionId = row[Positions.electionId],
        createdBy = row[Positions.createdBy],
        dateModified = row[Positions.dateModified]
    )
}