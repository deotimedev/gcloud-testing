import com.google.cloud.compute.v1.AggregatedListInstancesRequest
import com.google.cloud.compute.v1.InstancesClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

suspend fun main(): Unit = coroutineScope {
    val projectId = runCommand("gcloud config get project") ?: error("Could not find google cloud project id.")
    embeddedServer(Netty, port = 80) {

        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/project") {
                call.respond(projectId)
            }
            get("/drones") {
                val client = InstancesClient.create()


                val request = AggregatedListInstancesRequest.newBuilder()
                    .apply {
                        project = projectId
                        // doesnt work on the rest api for whatever reason (https://b.corp.google.com/issues/119209458)
                        // filter = "metadata.items.key['bee']['value']='drone'"
                    }
                    .build()

                val response = client.aggregatedList(request)
                val drones = response.iterateAll()
                    .flatMap { (_, l) -> l.instancesList }
                    .filter {
                        it.metadata.itemsList.any {
                            it.key == "bee" && it.value == "drone"
                        }
                    }
                call.respond(drones.map { it.name })
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