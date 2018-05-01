///**
//  * This actor initiates communication with remote actor on a different host.
//  */
//
//class ActorA extends Actor {
//  // Get relay actor bound to port 9000 (or whatever) on remote host.
//  // This actor is also bound to the relay for replies from the remote.
//  val socket = getPropertyOrElse("remote", InetAddress.getLocalHost.getHostAddress + ":" + 9000)
//  val relay = new Relay(socket, this)
//
//  // Give actor time to start.
//  sleep(250)
//
//  // Relay message to remote actor but remote actor must already be running otherwise
//  // we'll get connection refused exception. So if this succeeds, we know the remote
//  // actor is running and listening.
//  relay ! Y("hello there from " + this)
//
//  /** Actor starts running here immediately after construction. */
//  def act = {
//    import ActorA._
//    LOG.info("started")
//
//    // Wait to receive for a message in the mailbox.
//    receive match {
//      case reply: Task if reply.kind == Task.REPLY =>
//        LOG.info("got reply = " + reply)
//
//      // This is idiosyncratic Scala syntax to mean "default", similar to _ except
//      // we have a reference to that instance.
//      case that =>
//        LOG.info("got some that message = " + that)
//    }
//  }
//}
