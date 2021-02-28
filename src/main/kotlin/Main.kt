import com.viartemev.ktor.flyway.FlywayFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.exposed.sql.Database
import service.*
import util.JsonMapper.defaultMapper
import web.election
import web.index
import web.user
import java.util.*

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)
    
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Head)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.AccessControlRequestHeaders)
        header(HttpHeaders.AccessControlRequestMethod)
        exposeHeader(HttpHeaders.AccessControlAllowOrigin)
        exposeHeader(HttpHeaders.AccessControlAllowCredentials)
        exposeHeader(HttpHeaders.AccessControlAllowMethods)
        exposeHeader(HttpHeaders.AccessControlAllowHeaders)
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        maxAgeInSeconds = 1728000 // preflight valid for 90 days
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(defaultMapper))
    }

    val db = DatabaseFactory.create()
    Database.connect(db)
    install(FlywayFeature) {
        dataSource = db
    }


    val authService = AuthService(environment.config)
    install(Authentication) { jwt {
            verifier(authService.jwtVerifier)
            realm = authService.realm
            validate { credential ->
                if (credential.payload.audience.contains(authService.audience)) {
                    val loginId = credential.payload.getClaim("loginId").asString()
                    val userId = LoginService().getLogin(UUID.fromString(loginId))?.userId
                    if (userId != null) {
                        UserService().getUser(userId)
                    } else null
                } else null
            }
        }
    }

    val userService = UserService()
    val loginService = LoginService()
    val electionService = ElectionService()
    val positionService = PositionService()
    val candidateService = CandidateService()
    val voterService = VoterService()
    val voteService = VoteService()
    install(Routing) {
        index()
        user(userService, loginService, authService, environment.config)
        election(electionService, positionService, candidateService, voterService, voteService)
    }

}

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}