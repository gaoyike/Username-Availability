package akka

import Utils._
import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import api.DB

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Handle the request
 * Created by gaoyike on 7/1/15.
 */
class RequestActor extends Actor {
  override def receive: Receive = {
    case r: request => {
      val senderActor = sender
      // search in memcache

      // search in database
      implicit val timeout = Timeout(10 seconds)
      val f =  DB.dbActor ? HighPriorityRequest(r,DBOps.Search)
      val a = Await.result(f, timeout.duration).asInstanceOf[response]
      if(a.res.size != 0) {
        senderActor ! a
      }
      else {
        implicit val timeout = Timeout(10 seconds)
        val search = context.actorOf(Props[OnlineSearchActor])
        val f =  search ? r
        val a = Await.result(f, timeout.duration).asInstanceOf[response]
        senderActor ! a
      }
    }
  }
}
