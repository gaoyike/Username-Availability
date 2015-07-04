package akka

import Utils._
import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
/**
 * Created by gaoyike on 7/2/15.
 */
class MyPriorityMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  // Create a new PriorityGenerator, lower prio means more important
  PriorityGenerator {
    case x:HighPriorityRequest => 1
    // String Messages
    case x:LowPriorityRequest => 2
    // Long messages
    case x: HighPriorityResponse => 3
    // other messages
    case x: LowPriorityResponse => 4

    case _ => 5
  })