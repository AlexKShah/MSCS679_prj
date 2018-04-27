import org.apache.log4j.Logger
import parascale.actor.last.{Task, Worker}
import parascale.util._
import parabond.cluster._
//import parascale.parabond.util

object ParaWorker extends App {
  val LOG = Logger.getLogger(getClass)
  LOG.info("started")

  //a. If worker running on a single host, spawn two workers else spawn one worker.
  val nhosts = getPropertyOrElse("nhosts", 1)

  //receive parameter to change node type, defaults to basic node
  val prop = getPropertyOrElse("node","parabond.cluster.BasicNode")
  val clazz = Class.forName(prop)
  import parabond.cluster.Node
  val node = clazz.newInstance.asInstanceOf[Node]
  //val nodetype = getPropertyOrElse("nodetype", 0)

  // One-port configuration
  val port1 = getPropertyOrElse("port", 8000)

  // If there is just one host, then the ports will include 9000 by default
  // Otherwise, if there are two hosts in this configuration, use just one
  // port which must be specified by VM options
  val ports = if (nhosts == 1) List(port1, 9000) else List(port1)

  // Spawn the worker(s).
  // Note: for initial testing with a single host, "ports" contains two ports.
  // When deploying on two hosts, "ports" will contain one port per host.
  for (port <- ports) {
    // Construction forks a thread which automatically runs the actor act method.
    new ParaWorker(port)
  }
}

class ParaWorker(port: Int) extends Worker(port) {
  import ParaWorker._
  override def act: Unit = {
    //b. Wait for a task.
    while (true) {
      receive match {
        case task: Task =>
          LOG.info("got task = " + task + " sending reply")

          //get Partition out of task
          //c. Create a Partition.
          val part = task.payload.asInstanceOf[Partition]
          println("worker part = " + part)

          //d. Create a Node with the Partition.
          //e. Invoke analyze on the Node and wait for it to finish.
          val analysis = node analyze(part)
          val partialT1 = analysis.results.foldLeft(0L) { (sum, job)=>
            sum + (job.result.t1 - job.result.t0)
          }
          sender ! partialT1
      }
    }
    /*
    a. If worker running on a single host, spawn two workers else spawn one worker.
    b. Wait for a task.
    c. Create a Partition.
    d. Create a Node with the Partition.
    e. Invoke analyze on the Node and wait for it to finish.
    f. Reduce the partial T1 results.
    g. Reply to dispatcher with T1
    h. Repeat step b = Wait for a task.
     */
  }
}
