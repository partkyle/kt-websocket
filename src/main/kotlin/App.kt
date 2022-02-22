import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

class App {

    fun buildModules(): Application.() -> Unit {
        return fun Application.() {
            routing {
                get("/") {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

}
