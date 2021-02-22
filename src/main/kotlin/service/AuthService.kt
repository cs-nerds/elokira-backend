package service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.config.*

import model.Login
import java.util.Date

class AuthService(
    config: ApplicationConfig
) {
    val audience = config.property("jwt.audience").getString()
    val realm = config.property("jwt.realm").getString()
    private val secret= config.property("jwt.secret").getString()
    private val validityInMs = config.property("jwt.validityMs").getString().toInt()
    private val issuer = config.property("jwt.issuer").getString()
    private val algorithm = Algorithm.HMAC256(secret)

    val jwtVerifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    fun generateToken(login: Login): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("loginId", login.loginId.toString())
        .withClaim("loginCode", login.loginCode)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}

