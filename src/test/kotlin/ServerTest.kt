import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.junit.Test
import java.net.ServerSocket
import kotlin.test.assertTrue

internal class ServerTest {
    private val app = App()
    private val port = randomPort()
    private val tobj = Server(app, port)

    @Test
    fun startAndStop() {
        tobj.start()
        tobj.stop()
    }

    @Test
    fun `make requests against a real server`() {
        tobj.start()

        val client = HttpClient(CIO) {
            install(WebSockets)
        }

        runBlocking {
            val semaphore = Semaphore(100)
            val jobs = List(100_000) {
                semaphore.acquire()
                launch {
                    val host = "localhost"
                    val path = "/v1/rpc"

                    var websocket: DefaultWebSocketSession? = null
                    try {
                        websocket = client.webSocketSession(HttpMethod.Get, host, port, path)

                        websocket.send(Frame.Text("ehlo $it"))
                        println((websocket.incoming.receive() as Frame.Text).readText())
                    } finally {
                        websocket?.close()
                        semaphore.release()
                    }
                }
            }

            jobs.joinAll()
            assertTrue(jobs.all { it.isCompleted })
        }

        tobj.stop()
    }

    private fun randomPort(): Int {
        return ServerSocket(0).let { it.close(); it.localPort }
    }
}
