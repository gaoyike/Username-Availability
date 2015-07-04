package api

import akka.DataBaseActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http
import spray.servlet.WebBoot
import scala.concurrent.duration._

class Boot extends WebBoot{

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("mysys",  ConfigFactory.load)
  // create and start our service actor
  val serviceActor = system.actorOf(Props[MyServiceActor], "service")
  // start a new HTTP server on port 8080 with our service actor as the handler
  implicit val timeout = Timeout(10 seconds)
  IO(Http) ? Http.Bind(serviceActor, interface = "localhost", port = 8080)
  val dbActor = system.actorOf(Props[DataBaseActor].withDispatcher("my-priority-dispatcher"),"db")
  DB.setDBActor(dbActor)
  system.registerOnTermination {
    system.shutdown()
    system.log.info("Application shut down")
  }
}
object DB {
var dbActor:ActorRef = null
  def setDBActor(db:ActorRef) = {dbActor = db}
}