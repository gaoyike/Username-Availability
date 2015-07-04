package api

import Utils.requestJsonSupport._
import Utils.{RequestMessage, request}
import akka.RequestActor
import akka.actor.{Actor, Props}
import spray.http.{StatusCodes, StatusCode}
import spray.httpx.SprayJsonSupport
import spray.routing._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(webappRoute~apiRoute)
}


// this trait defines our service behavior independently from the service actor
trait  MyService extends HttpService with PerRequestCreator with SprayJsonSupport {

  implicit def executionContext = actorRefFactory.dispatcher

  val apiRoute =
    path("check") {
      post {
        respondWithStatus(StatusCode.int2StatusCode(200)) {
          implicit val requestFormats = jsonFormat2(request)
          entity(as[request]) { r =>
            handlePerRequest(r)
          }
        }
      }
    }
  val webappRoute = {
    pathSingleSlash {
      redirect("webapp/", StatusCodes.PermanentRedirect)
    } ~
      pathPrefix("webapp") {
        pathEnd {
          redirect("webapp/", StatusCodes.PermanentRedirect)
        } ~
          pathEndOrSingleSlash {
            getFromResource("webapp/index.html")
          } ~
          getFromResourceDirectory("webapp")
      }
  }
  def handlePerRequest(message: RequestMessage): Route =
    ctx => perRequest(actorRefFactory, ctx, Props[RequestActor], message)

}




