//package akka
//
//import Utils._
//import akka.actor.{Actor, Props}
//import com.website.Boot
//
///**
// * Created by chenguanghe on 6/30/15.
// */
//class MainActor extends Actor{
//  override def receive: Receive = {
//    case r:response =>{
//      print(r.toString)
//      context.stop(self)
//    }
//    case r:request => {
//      val newActor = Boot.system.actorOf(Props(classOf[RequestActor]))
//      newActor ! r
//    }
//  }
//}
