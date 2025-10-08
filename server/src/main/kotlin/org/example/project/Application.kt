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
        try {
            data
                .catch {
                    println("Catch: ${it.message}")
                    throw it
                }
                .collect {
                    println("Data: ${it.decodeToString()}")
                    delay(5.seconds)
                }
            println("Collector done...")
        } catch (e: Exception) {
            println("Collector error: ${e.message}")
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