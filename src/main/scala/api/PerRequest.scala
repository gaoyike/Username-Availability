package api

/**
 * Created by gaoyike on 6/30/15.
 */


import Utils.responseJsonSupport._
import Utils.{Error, RequestMessage, response}
import akka.actor.SupervisorStrategy.Stop
import akka.actor.{OneForOneStrategy, _}
import api.PerRequest.WithProps
import org.json4s.DefaultFormats
import spray.http.StatusCodes._
import spray.http.{HttpHeader, StatusCode}
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration._
/**
 * Per request pattern, taken from https://github.com/NET-A-PORTER/spray-actor-per-request
 *
 * Created by rguderlei on 25.02.14.
 */
trait PerRequest extends Actor  with Json4sSupport{
  def r: RequestContext
  def target: ActorRef
  def message: RequestMessage
  val json4sFormats = DefaultFormats

  import context._

  setReceiveTimeout(5.seconds)

  target ! message

  def receive = {
    case r:response => complete(spray.http.StatusCodes.OK,responseToJson(r))
    case Error(message) => complete(BadRequest, message)
    case ReceiveTimeout => complete(GatewayTimeout, "Request timeout")
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T, headers: List[HttpHeader] = List()) = {
    r.withHttpResponseHeadersMapped(oldheaders => oldheaders:::headers).complete(status, obj)
    stop(self)
  }
  def responseToJson(r:response) = {
    import spray.json._
    r.toJson
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, Error(e.getMessage))
        Stop
      }
    }
}

object PerRequest {
  case class WithProps(r: RequestContext, props: Props, message: RequestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator {
  def perRequest(actorRefFactory: ActorRefFactory, r: RequestContext, props: Props, message: RequestMessage) =
    actorRefFactory.actorOf(Props(new WithProps(r, props, message)))
}