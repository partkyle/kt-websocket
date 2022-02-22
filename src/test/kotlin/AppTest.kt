import io.ktor.http.*
import io.ktor.server.testing.*
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
            handleWebSocketConversation("/v1/rpc") { _, _ ->

            }
        }
    }
}
