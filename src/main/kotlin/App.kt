import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*

class App {

    fun buildModules(): Application.() -> Unit {
        return fun Application.() {
            install(WebSockets)

            routing {
                get("/") {
                    call.respond(HttpStatusCode.OK)
                }

                webSocket("/v1/rpc") {

                }
            }
        }
    }

}
