import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

object Main extends App {

  val port = 9000
  val url = "ws_api"
  val host = "localhost"

  implicit val system: ActorSystem = ActorSystem()
  implicit val flowMaterializer: ActorMaterializer = ActorMaterializer()

  def flow: Flow[Msg.Base, Msg.Base, Any] = {
    val clientRef = system.actorOf(Props(classOf[Client]))
    val in = Sink.actorRef(clientRef, None)
    val out = Source.actorRef(8, OverflowStrategy.fail).mapMaterializedValue { actor =>
      Tables.subscribers += actor
      clientRef ! actor
    }
    Flow.fromSinkAndSource(in, out)
  }

  def webservice: Flow[Message, Message, Any] = Flow[Message]
    .collect { case TextMessage.Strict(msg) => msg }
    .map(Msg.decode)
    .map {
      case Right(msg) => msg
      case Left(err) => Msg.fail(s"Error: ${err.getMessage}")
    }
    .via(flow)
    .map {
      msg: Msg.Base => TextMessage.Strict(Msg.toJson(msg))
    }

  import akka.http.scaladsl.server.Directives._
  val binding = Http().bindAndHandle(
    path(url) {
      get {
        handleWebSocketMessages(webservice)
      }
    }, host, port)

  println(s"Server is now online at ws://$host:$port/$url")
}


