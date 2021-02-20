package web

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.withContext
import model.*
import service.*
import util.CodeGenerator
import util.JsonMapper.defaultMapper
import java.lang.IllegalStateException

suspend fun createLogin(user: User, loginService: LoginService, config: ApplicationConfig): Login {
    val code = CodeGenerator().generate().toInt()
    val message = """
        Hi ${user.firstName}, your login code for Elokira Vote is:
        $code 
    """.trimIndent()
    val login = loginService.addLogin(Login(null, user.userId, code))
    val smsService = SMSService(config)
    smsService.send(message, listOf(user.phoneNumber))
    return login
}

suspend fun verifyUser(unverifiedUser: UnverifiedUser, config: ApplicationConfig): Any {
    val verificationService = VerificationService(
        config.property("verification.url").getString(),
    )
    return verificationService.verifyUser(unverifiedUser)
}

@ExperimentalCoroutinesApi
fun Route.user(userService: UserService, loginService: LoginService, authService: AuthService, config: ApplicationConfig) {
    route("/users") {

        get("/") {
            call.respond(userService.getAllUsers())
        }

        // test authentication
        authenticate {
            get("/{userId}") {
                val userId = call.parameters["userId"]?.toInt() ?: throw IllegalStateException("Must provide id")
                when (val user = userService.getUser(userId)) {
                    null -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(user)
                }
            }
        }

        post("/verify") {
            val unverifiedUser = call.receive<UnverifiedUser>()
            try {
                val verificationService = VerificationService(
                    config.property("verification.url").getString(),
                )
                val verifiedUser = verifyUser(unverifiedUser, config) as VerifiedUser
                call.respond(verifiedUser)
            } catch (e: Exception) {
                println(e)
                call.respond(
                    HttpStatusCode.Forbidden,
                    UnverifiedUser(unverifiedUser.firstName.toLowerCase().capitalize(), unverifiedUser.idNumber)
                )
            }
        }


        post("/") {
            val newUser = call.receive<NewUser>()
            val unverifiedUser = UnverifiedUser(newUser.firstName, newUser.idNumber)
            if (userService.getUserByIdNumber(newUser.idNumber) != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "User already exists"))
            } else {
                try {
                    verifyUser(unverifiedUser, config)
                    val user = userService.addUser(newUser)
                    val login = createLogin(user, loginService, config)
                    call.respond(HttpStatusCode.Created, mapOf("loginId" to login.loginId))
                } catch (e: Exception) {
                    println(e)
                    call.respond(
                        HttpStatusCode.Forbidden,
                        UnverifiedUser(unverifiedUser.firstName.toLowerCase().capitalize(), unverifiedUser.idNumber)
                    )
                }
            }
        }

        post("/login/request") {
            val loginAttempt = call.receive<LoginRequest>()
            val user = userService.getUserByIdNumber(loginAttempt.idNumber)
            if (user == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "User with id ${loginAttempt.idNumber} not found.")
                )
            } else {
                val login = createLogin(user, loginService, config)
                call.respond(HttpStatusCode.Created, mapOf("loginId" to login.loginId))
            }
        }

        post("/login") {
            val loginAttempt = call.receive<Login>()
            val login = loginService.getLogin(loginAttempt.loginId!!)
            if (login == null || login.activated!!) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Code used, request for another")
                )
            } else {
                login.activated = true
                loginService.updateLogin(login)
                val token = authService.generateToken(login)
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("token" to token)
                )
            }
        }

        put("/") {
            val user = call.receive<NewUser>()
            when(val updated = userService.updateUser(user)) {
                null -> call.respond(HttpStatusCode.NotFound)
                else -> call.respond(HttpStatusCode.OK, updated)
            }
        }

        delete("/{userId}") {
            val userId = call.parameters["userId"]?.toInt() ?: throw IllegalStateException("Must provide id")
            val removed = userService.deleteUser(userId)
            if (removed) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound)
        }
    }

    webSocket("/user-updates") {
        try {
            userService.addChangeListener(this.hashCode()) {
                val output = withContext(Dispatchers.IO) {
                    defaultMapper.writeValueAsString(it)
                }
                outgoing.send(Frame.Text(output))
            }
            while (true) {
                incoming.receiveOrNull() ?: break
            }
        } finally {
            userService.removeChangeListener(this.hashCode())
        }
    }
}