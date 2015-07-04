package Retrieve

import Utils._
import akka.actor.Actor
import spray.client.pipelining._
import spray.http.FormData
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.Future
import scala.util.{Failure, Success}
/**
 * Search Github ID
 * Created by gaoyike on 6/30/15.
 */
class Github extends RetrieveInfo with Actor{
  val URL = "https://github.com/signup_check/username"

  override def CompanyName(): String = "Github"

  override def RetrieveURL(): String = URL

  override def receive: Receive = {
    case r: request => {
      UserNameInUser(r.username, r.id)
    }
    case _ => print("Error in Github")
  }

  override def UserNameInUser(username: String, processId:Long) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val data = Map("value" -> username)
    val pipeline = sendReceive ~> unmarshal[String]
    val response: Future[String] = pipeline(Post(URL, FormData(data)))
    val senderActor = sender
    response.onComplete {
      case Success(s) => {
        senderActor ! ans("github", username, processId, true)
        context.stop(self)
      }
      case Failure(e) => e match {
        case ure: UnsuccessfulResponseException =>  {
          senderActor ! ans("github", username, processId, false)
          context.stop(self)
        }// [false] pipe to actor
        case _ => None
      }
    }
    response.map(s => s.contains("used"))
  }
}
