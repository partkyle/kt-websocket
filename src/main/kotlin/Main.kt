import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val app = App()
    val server = embeddedServer(Netty, module = app.buildModules())
    server.start()
}

