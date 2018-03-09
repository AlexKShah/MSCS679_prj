//Alex Shah
//MSCS 679 - Assignment 2

package assign2

import org.apache.log4j.Logger
import parascale.actor.last.{Task, Worker}
import parascale.future.perfect._sumOfFactorsInRange
import parascale.util._

/**
  * Spawns workers on the localhost.
  */
object PerfectWorker extends App {
  val LOG = Logger.getLogger(getClass)

  LOG.info("started")

  // Number of hosts in this configuration
  val nhosts = getPropertyOrElse("nhosts", 1)

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
    new PerfectWorker(port)
  }
}

/**
  * Template worker for finding a perfect number.
  *
  * @param port Localhost port this worker listens to
  */
class PerfectWorker(port: Int) extends Worker(port) {

  import PerfectWorker._

  /**
    * Handles actor startup after construction.
    */
  override def act: Unit = {
    val name = getClass.getSimpleName
    LOG.info("started " + name + " (id=" + id + ")")

    // Wait for inbound messages as tasks
    while (true) {
      receive match {
        case task: Task =>
          LOG.info("got task = " + task + " sending reply")

          //get Partition out of task
          val part = task.payload.asInstanceOf[Partition]
          println("worker part = " + part)

          //repartition and get the partialresult
          val partialResult: Result = getPartialResult(part)
          println("partial result = " + partialResult)

          sleep(100)
          //reply with partial result
          sender ! partialResult
      }
    }

    def getPartialResult(part: Partition): Result = {
      val RANGE = 1000000L
      val numPartitions = (part.candidate.toDouble / RANGE).ceil.toInt

      println(" number partitions = " + numPartitions)

      // Start with a par collection which propagates through all forward calculations
      val partitions = (0L until numPartitions).par
      val ranges = for (k <- partitions) yield {
        val lower: Long = (k * RANGE + 1) max part.start
        val upper: Long = part.end min (k + 1) * RANGE
        println("lower = " + lower + " upper = " + upper + " for partition: " + k)

        (lower, upper)
      }

      // Ranges is a collection of 2-tuples of the lower-to-upper partition bounds
      val sums = ranges.map { lowerUpper =>
        val (lower, upper) = lowerUpper
        _sumOfFactorsInRange(lower, upper, part.candidate)
      }
      println("sums = " + sums)

      val total = sums.sum

      println("total = " + total)

      //put worker's result in a Result and return
      val partialresult = Result(total)
      partialresult
    }
  }
}

