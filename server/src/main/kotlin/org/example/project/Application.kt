package org.example.project

import MyRpc
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

class MyRpcImpl: MyRpc {
    override fun myFunction(data: Flow<ByteArray>) = flow {
        emit("OK, now delay 5s. Client should close the connection...")
        println("First data: ${data.first().decodeToString()}")
        delay(5.seconds)
        println("Delay completed, listening for second...")

        try {
            val value = data
                .catch { e -> println(e.message); throw e }
                .first().decodeToString()

            println("Second data: $value")
        } catch (e: Exception) {
            println("Failed to read second data: ${e.message}")
            throw e
        }

        try {
            emit("I should fail...")
            println("Second sent!")
        } catch (e: Exception) {
            println("Failed to send second data: ${e.message}")
            throw e
        }
    }
}

fun Application.module() {
    install(Krpc) {
        serialization {
            json {  }
        }
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }

    routing {
        rpc("/rpc") {
            registerService<MyRpc> { MyRpcImpl() }
        }
    }
}