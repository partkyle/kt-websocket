import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

internal class AppTest {
    val tobj = App()

    @Test
    fun test() {
        withTestApplication(tobj.buildModules()) {
            val response = handleRequest {
                method = HttpMethod.Get
                uri = "/"
            }

            assertEquals(HttpStatusCode.OK, response.response.status())
        }
    }

    @Test
    fun `supports websocket`() {
        withTestApplication(tobj.buildModules()) {
            handleWebSocketConversation("/v1/rpc") { _, _ -> }
        }
    }

    @Test
    fun `simple conversation - fails with too many`() {
        withTestApplication(tobj.buildModules()) {
            val jobs = (1..10).map {
                launch {
                    println("starting $it")
                    handleWebSocketConversation("/v1/rpc") { incoming, outgoing ->
                        outgoing.send(Frame.Text("ehlo $it"))
                        validateMessage(incoming, "Goodbye!")
                    }
                }
            }

            runBlocking {
                jobs.joinAll()
            }
        }

    }

    private suspend fun validateMessage(incoming: ReceiveChannel<Frame>, expected: String) {
        val msg = incoming.receive()
        assertEquals(expected, (msg as Frame.Text).readText())
    }
}
