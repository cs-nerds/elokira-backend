package web

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import model.*
import org.apache.hc.core5.http.HttpStatus
import service.*
import java.lang.IllegalStateException
import java.util.*
import kotlin.collections.LinkedHashMap

@ExperimentalCoroutinesApi
fun Route.election(electionService: ElectionService, positionService: PositionService, candidateService: CandidateService, voterService: VoterService, voteService: VoteService) {
    route("/elections") {

        authenticate {

            get("/") {
                call.respond(electionService.getAllElections())
            }

            post("/") {
                val election = call.receive<NewElection>()
                val loggedInUser = call.authentication.principal as User
                if (loggedInUser.admin) {
                    call.respond(
                        HttpStatusCode.Created,
                        electionService.createElection(election, loggedInUser.userId)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Insufficient Permissions")
                    )
                }
            }

            get("/{electionId}") {
                val electionId = call.parameters["electionId"] ?: throw IllegalStateException("Must provide id")
                when(val election = electionService.getElection(UUID.fromString(electionId))) {
                    null -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(election)
                }
            }

            get("/{electionId}/positions") {
                val electionId = call.parameters["electionId"] ?: throw IllegalStateException("Must provide id")
                val electionPositions = positionService.getElectionPositions(UUID.fromString(electionId))
                call.respond(electionPositions)
            }

            post("/{electionId}/positions"){
                val electionId = call.parameters["electionId"] ?: throw IllegalStateException("Must provide id")
                val electionPosition = call.receive<NewPosition>()
                val loggedInUser = call.authentication.principal as User
                if (loggedInUser.admin && electionPosition.electionId == UUID.fromString(electionId)) {
                    call.respond(
                        HttpStatusCode.Created,
                        positionService.addElectionPosition(electionPosition, loggedInUser.userId)
                    )
                }
            }

            get("/{electionId}/positions/{positionId}/candidates") {
                val electionId = call.parameters["electionId"] ?: throw IllegalStateException("Must provide id")
                val positionId = call.parameters["positionId"] ?: throw IllegalStateException("Must provide id")
                val candidatesByPosition = candidateService.getElectionCandidatesByPosition(
                    UUID.fromString(electionId), UUID.fromString(positionId)
                )
                call.respond(candidatesByPosition)
            }

            post("/{electionId}/positions/{positionId}/candidates") {
                val positionId = call.parameters["positionId"] ?: throw IllegalStateException("Must provide id")
                val loggedInUser = call.authentication.principal as User
                val newCandidate = call.receive<NewCandidate>()
                if (loggedInUser.admin && newCandidate.positionId == UUID.fromString(positionId)) {
                    call.respond(
                        HttpStatusCode.Created,
                        candidateService.addCandidate(newCandidate, loggedInUser.userId)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Insufficient permissions")
                    )
                }
            }

            post("/{electionId}/participate") {
                val electionId = call.parameters["electionId"] ?: throw IllegalStateException("Must provide id")
                val election = call.receive<Election>()
                val loggedInUser = call.authentication.principal as User
                val existingVoter = (voterService.getVoter(loggedInUser.userId, election.electionId) != null)
                if (!existingVoter && election.electionId == UUID.fromString(electionId)) {
                    val newVoter = NewVoter(loggedInUser.userId, election.electionId)
                    call.respond(
                        HttpStatusCode.Created,
                        voterService.registerVoter(newVoter)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Already registered for this election")
                    )
                }
            }
            post("/{electionId}/vote") {
                val electionIdParam = call.parameters["electionId"] ?: throw IllegalStateException("Must provide id")
                val electionId = UUID.fromString(electionIdParam)
                val votes = call.receive<List<LinkedHashMap<String, String>>>()
                val election = electionService.getElection(electionId)
                val votingTime = System.currentTimeMillis()
                val loggedInUser = call.authentication.principal as User
                val voterId = voterService.getVoter(loggedInUser.userId, electionId)!!.voterId
                // check if election is open
                if (election!!.startDate < votingTime && election.stopDate > votingTime) {
                    // check if user has voted
                    val voter = voterService.getVoter(loggedInUser.userId, electionId)!!
                    if (!voter.voted) {
                        for (voteMap in votes){
                            val candidateId = UUID.fromString(voteMap.getValue("candidateId"))
                            val vote = Vote(candidateId, voterId)
                            voteService.addVote(vote)
                        }
                        call.respond(
                            HttpStatusCode.Created,
                            voterService.updateToVoted(voterId)
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Already voted in this election")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Election is not open")
                    )
                }
            }
            get("/{electionId}/results") {
                val electionIdParam = call.parameters["electionId"] ?: throw IllegalStateException("Must provide id")
                val electionId = UUID.fromString(electionIdParam)
                call.respond(
                    HttpStatusCode.OK,
                    voteService.countVotes(electionId)
                )
            }
        }
    }
}