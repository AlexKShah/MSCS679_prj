package assign1

import org.apache.log4j.Logger
import parascale.actor.last.{Actor, Remote, Task}
import parascale.util.getPropertyOrElse

/**
  * This object spawns an actor and binds it to the remote relay.
  */
object ActorB extends App {
  val LOG = Logger.getLogger(getClass)
  val actorb = new ActorB

  // Learned by trial & error need to give actor time to start.
  Thread.sleep(250)

  // If the actor started correctly, this causes diagnostic output in the logger
  actorb ! "testing 1-2-3"

  // Bind this actor on default port 9000 as a remote receiver
  val port: Int = getPropertyOrElse("port", 9000)
  new Remote(port, actorb)
}

/**
  * This actor awaits an inbound message from either a local or remote actor.
  */
class ActorB extends Actor {

  import ActorB._

  def act = {
    // Wait forever for messages.
    while (true) {
      receive match {
        case task: Task =>
          LOG.info("got task = " + task)

          task.payload match {
            case y: Y =>
              LOG.info("payload is Y = " + y.s)
              task.reply("back at ya!")

            case s: String =>
              LOG.info("got " + s)
          }
      }
    }
  }
}