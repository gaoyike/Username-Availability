package akka
import Utils._
import akka.actor.Actor
import datastore.Username

/**
 * Created by chenguanghe on 7/2/15.
 */
class DataBaseActor extends Actor {
  override def receive: Receive = {
    case highR:HighPriorityRequest =>{
      val senderActor = sender
      if(highR.op == DBOps.Search) {
        Username.init()
        var response = Username.search(highR.r.username, highR.r.id)
          senderActor ! response
      }
    }
    case lowR:LowPriorityRequest => {
      val senderActor = sender
      if(lowR.op == DBOps.Search) {
        Username.init()
        var response = Username.search(lowR.r.username, lowR.r.id)
        senderActor ! response
      }
    }
    case highR:HighPriorityResponse => {
      val senderActor = sender
      if(highR.op == DBOps.Insert) {
        Username.init()
        var response = Username.Insert(highR.r.username,highR.r.res)
        if (!response)
          self ! LowPriorityResponse(highR.r,DBOps.Update)
      }
    }
    case lowR:LowPriorityResponse => {
      val senderActor = sender
      if(lowR.op == DBOps.Update) {
        Username.init()
        var response = Username.Update(lowR.r.username,lowR.r.res)
      }
    }
    case _ =>{}
  }
}
