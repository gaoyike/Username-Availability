package akka

import Utils._
import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import api.DB

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * Online search the result
 * Created by gaoyike on 7/3/15.
 */
class OnlineSearchActor  extends Actor{
  override def receive: Receive = {
    case r:request => {
      // search online!
      val senderActor = sender
      val companies = myUtils.CompanyNameToActorProp
      val futures:ArrayBuffer[Future[ans]] = ArrayBuffer[Future[ans]]()
      companies.foreach {
        c => {
          implicit val timeout = Timeout(5 seconds)
          val newActor = context.actorOf(c._2,c._1)
          val future = newActor ? r
          futures.append(future.mapTo[ans])
        }
      }
      implicit val ec = ExecutionContext.Implicits.global
      val seq = Future.sequence(futures.toList)//Future[list[ans]]
      implicit val timeout = Timeout(10 seconds)
      val list = Await.result(seq, timeout.duration)  // a List[ans]
      var res: ListBuffer[result] = ListBuffer[result]()
      list.map {
        l => res += result(l.company, l.answer.toString)
      }
      val resp = response(r.id, r.username, res.toList)
      DB.dbActor ! HighPriorityResponse(resp,DBOps.Insert)
      senderActor ! resp
    }
  }
}
