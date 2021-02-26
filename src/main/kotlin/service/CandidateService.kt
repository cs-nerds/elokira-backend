package service

import model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import service.DatabaseFactory.dbQuery
import java.util.UUID

class CandidateService {

    suspend fun getAllCandidates(): List<Candidate> = dbQuery {
        Candidates.selectAll().map { toCandidate(it)}
    }

    suspend fun getCandidate(candidateId: UUID): Candidate? = dbQuery {
        Candidates.select{
            (Candidates.candidateId eq candidateId)
        }.mapNotNull { toCandidate(it) }.singleOrNull()
    }

    suspend fun getElectionCandidatesByPosition(electionId: UUID, positionId: UUID): List<CandidateDetails> = dbQuery {

        Positions.innerJoin(Candidates).innerJoin(Users, {Candidates.userId}, {userId})
            .slice(
                Candidates.candidateId,
                Users.firstName,
                Users.lastName,
                Positions.positionId,
                Positions.positionName
            )
            .select{
                ((Positions.electionId eq electionId) and (Positions.positionId eq positionId))
            }
            .map { toCandidateDetails(it) }
    }

    private fun toCandidate(row: ResultRow): Candidate = Candidate(
        userId = row[Candidates.userId],
        candidateId = row[Candidates.candidateId],
        positionId = row[Candidates.positionId],
        registrationDate = row[Candidates.registrationDate],
        verified = row[Candidates.verified],
        verifiedBy = row[Candidates.verifiedBy]
    )

    private fun toCandidateDetails(row: ResultRow): CandidateDetails = CandidateDetails(
        candidateId = row[Candidates.candidateId],
        positionId = row[Positions.positionId],
        candidateFirstName = row[Users.firstName],
        candidateLastName = row[Users.lastName],
        positionName = row[Positions.positionName]
    )
}