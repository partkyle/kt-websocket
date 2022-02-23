import io.ktor.server.engine.*
import io.ktor.server.netty.*

class Server(private val app: App, private val port: Int = 58080) {
    private val server = embeddedServer(Netty, port, module = app.buildModules())

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop(0,0)
    }

}