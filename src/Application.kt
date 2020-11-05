package com.inauth

import com.inauth.repository.DatabaseFactory
import com.inauth.auth.JwtService
import com.inauth.auth.MySession
import com.inauth.auth.hash
import com.inauth.repository.TodoRepository
import com.inauth.routes.todos
import com.inauth.routes.users
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.locations.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) {
        header("Access-Control-Allow-Origin: *");
        header("Access-Control-Allow-Credentials: true");
        header("Access-Control-Allow-Methods: GET, HEAD, OPTIONS, POST, PUT, DELETE");
        header("Access-Control-Allow-Headers: Authorization, Origin, X-Requested-With, Content-Type, Accept");
        header("Access-Control-Expose-Headers: Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
    }
    install(Locations) {
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    DatabaseFactory.init()
    val db = TodoRepository()
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }


    install(Authentication) {
        jwt("jwt") {
            verifier(jwtService.verifier)
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("id")
                val claimString = claim.asInt()
                val user = db.findUser(claimString)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        users(db, jwtService, hashFunction)
        todos(db)
    }
}


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

const val API_VERSION = "/v1"


