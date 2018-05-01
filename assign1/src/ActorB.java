///**
//  * This actor awaits an inbound message from either a local or remote actor.
//  */
//class ActorB extends Actor {
//
//  import ActorB._
//
//  def act = {
//    // Wait forever for messages.
//    while (true) {
//      receive match {
//        case task: Task =>
//          LOG.info("got task = " + task)
//
//          task.payload match {
//            case y: Y =>
//              LOG.info("payload is Y = " + y.s)
//              task.reply("back at ya!")
//
//            case s: String =>
//              LOG.info("got " + s)
//          }
//      }
//    }
//  }
//}
