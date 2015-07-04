package Retrieve

import Utils._
import akka.actor.Actor
import spray.client.pipelining._
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by chenguanghe on 6/30/15.
 */

class Google extends RetrieveInfo with Actor{
  val URL = "https://accounts.google.com/InputValidator?resource=SignUp"
  override def CompanyName(): String = "Google"

  override def RetrieveURL(): String = URL

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val inputFormat = jsonFormat4(input)
    implicit val inFormat = jsonFormat2(in)
    implicit val outputFormat = jsonFormat2(output)
    implicit val outFormat = jsonFormat2(out)
  }
  override def UserNameInUser(username: String, processId:Long) = {
    import MyJsonProtocol._
    import spray.httpx.SprayJsonSupport._

    import scala.concurrent.ExecutionContext.Implicits.global
    val pipeline = sendReceive ~> unmarshal[out]
    val response:Future[out]= pipeline(Post(URL,in(input("GmailAddress",username,"",""),"en")))
    val senderActor = sender
    response.onComplete{
      case Success(r:out) => {
        senderActor ! ans("google", username, processId, r.input01.Valid.toBoolean)
        context.stop(self)
      }// true / false
      case Failure(e) => print(e.printStackTrace())
    }
  }

  override def receive: Receive = {
    case r:request => {
      UserNameInUser(r.username,r.id)
    }
    case _ => print("Error in Google")
  }
}
