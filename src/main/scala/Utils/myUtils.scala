package Utils

import Retrieve._
import Utils.DBOps.DBOps
import akka.actor.Props
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * useful tools
 * Created by gaoyike on 6/30/15.
 */
object myUtils {
  val CompanyNameToID = Map[String,Int](
    "github" -> 0,
    "google" -> 1
  )
  val CompanyNameToActorProp = Map[String,Props](
    "github" -> Props[Github],
    "google" -> Props[Google]
  )
}
sealed trait RequestMessage
sealed trait ResponseMessage

case class ans(company:String, userName:String, id:Long,answer:Boolean)
case class in(input01:input,Locale:String)
case class input(Input:String,GmailAddress:String,FirstName:String,LastName:String)
case class out(input01:output,Locale:String)
case class output(Valid:String,ErrorData:List[String])
case class request(id:Long, username:String)extends RequestMessage
case class response(id:Long,username:String,res:List[result])extends ResponseMessage
case class result(company:String, available:String)
case class Error(message: String)
object DBOps extends Enumeration{
  type DBOps = Value
  val Insert,Search,Update = Value
}
case class HighPriorityRequest(r:request, op:DBOps)
case class LowPriorityRequest(r:request,op:DBOps)
case class HighPriorityResponse(r:response,op:DBOps)
case class LowPriorityResponse(r:response,op:DBOps)

object requestJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val requestFormats = jsonFormat2(request)
}
object responseJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val resultFormats = jsonFormat2(result)
  implicit val responseFormats = jsonFormat3(response)
}
