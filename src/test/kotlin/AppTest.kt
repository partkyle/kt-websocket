import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun `simple conversation - using a semaphore to prevent crashing from making too many requests`() {
        withTestApplication(tobj.buildModules()) {
            runBlocking {
                val s = Semaphore(100)
                val jobs = List(100_000) {
                    s.acquire()
                    launch {
                        println("starting $it")
                        handleWebSocketConversation("/v1/rpc") { incoming, outgoing ->
                            outgoing.send(Frame.Text("ehlo $it"))
                            validateMessage(incoming, "Goodbye!")
                        }
                        s.release()
                    }
                }

                jobs.joinAll()
                assertTrue(jobs.all { it.isCompleted })
                assertEquals(tobj.requestCount.get(), jobs.size)
            }
        }

    }

    private suspend fun validateMessage(incoming: ReceiveChannel<Frame>, expected: String) {
        val msg = incoming.receive()
        assertEquals(expected, (msg as Frame.Text).readText())
    }
}
