import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.concurrent.atomic.AtomicInteger

class App {

    val requestCount = AtomicInteger(0)

    fun buildModules(): Application.() -> Unit {
        return fun Application.() {
            install(WebSockets)

            routing {
                get("/") {
                    call.respond(HttpStatusCode.OK)
                }

                webSocket("/v1/rpc") {
                    try {
                        requestCount.addAndGet(1)

                        val msg = incoming.receive()
                        println((msg as Frame.Text).readText())
                        send(Frame.Text("Goodbye!"))
                    } finally {
                        close()
                    }
                }
            }
        }
    }

}
