import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

suspend fun main(): Unit = coroutineScope {
    val project = runCommand("gcloud config get project") ?: error("Could not find google cloud project id.")
    embeddedServer(Netty, port = 80) {
        routing {
            get {
                call.respond("Running on project \"$project\".")
            }
        }
    }.start(wait = true)
}

suspend fun runCommand(
    command: String,
    dir: File = File("./")
): String? = withContext(Dispatchers.IO) {
    val process = ProcessBuilder()
        .command(command.split(" "))
        .directory(dir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    if (process.waitFor() != 0) return@withContext null
    return@withContext process.inputReader().readText().removeSuffix("\n")
}