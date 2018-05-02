/**
  * Assignment 3
  * Alex Shah
  * MSCS 679
  * 5/1/18
  *
  * ParaWorker receives partitions from dispatcher to calculate portfolio cost
  * sends back the time it took
  */

import org.apache.log4j.Logger
import parascale.actor.last.{Task, Worker}
import parascale.util._
import parabond.cluster._
import parascale.parabond.util.Result

object ParaWorker extends App {
  //initiate Logger
  val LOG = Logger.getLogger(getClass)
  LOG.info("started")

  //a. If worker running on a single host, spawn two workers else spawn one worker.
  val nhosts = getPropertyOrElse("nhosts", 1)

  //receive parameter to change node type, defaults to basic node
  val prop = getPropertyOrElse("node","parabond.cluster.BasicNode")
  val clazz = Class.forName(prop)
  import parabond.cluster.Node
  val node = clazz.newInstance.asInstanceOf[Node]

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
    //start up new worker
    new ParaWorker(port)
  }
}

class ParaWorker(port: Int) extends Worker(port) {
  import ParaWorker._

  /**
    * continuously wait for work to:
    *   get a partition out of the task
    *   analyze the partition with the chosen node type
    *   sum up the time it took for the work
    *   send back the partial t1 time to dispatcher
    */
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

          //sum up workers time to create partial t1
          val partialT1 = analysis.results.foldLeft(0L) { (sum, job)=>
            sum + (job.result.t1 - job.result.t0)
          }
          //send back the partial time for t1
          sender ! Result(partialT1)
      }
    }
  }
}
