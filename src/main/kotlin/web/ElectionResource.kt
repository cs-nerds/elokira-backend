package web

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import model.NewCandidate
import model.NewElection
import model.NewPosition
import model.User
import service.*
import java.lang.IllegalStateException
import java.util.*

@ExperimentalCoroutinesApi
fun Route.election(electionService: ElectionService, positionService: PositionService, candidateService: CandidateService) {
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

        }
    }
}