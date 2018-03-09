//Alex Shah
//MSCS 679 - Assignment 2

package assign2

import org.apache.log4j.Logger
import parascale.actor.last.{Dispatcher, Task}
import parascale.util._


case class Partition(start: Long, end: Long, candidate: Long)
  extends Serializable

case class Result(sum: Long) extends Serializable

/**
  * Spawns a dispatcher to connect to multiple workers.
  */

object PerfectDispatcher extends App {
  val LOG = Logger.getLogger(getClass)
  LOG.info("started")
  // For initial testing on a single host, use this socket.
  // When deploying on multiple hosts, use the VM argument,
  // -Dsocket=<ip address>:9000 which points to the second
  // host.
  val socket2 = getPropertyOrElse("socket", "localhost:9000")
  // Construction forks a thread which automatically runs the actor act method.
  new PerfectDispatcher(List("localhost:8000", socket2))
}

/**
  * Template dispatcher which tests readiness of
  *
  * @param sockets
  */
class PerfectDispatcher(sockets: List[String]) extends Dispatcher(sockets) {

  import PerfectDispatcher._
  import parascale.future.perfect.candidates

  /**
    * Handles actor startup after construction.
    */
  def act: Unit = {
    //iterate through all the perfect numbers to test
    //as "candidate"
    (0 until 3).foreach { index =>
      val candidate = candidates(index)
      println("candidate = " + candidate)

      //get and print whether candidate is perfect
      println(ask(isPerfect, candidate))
    }
  }

  def isPerfect(candidate: Long): Boolean = {
    //first half goes to host A
    val aTask = Partition(1, candidate / 2 - 1, candidate)
    //second half goes to host B
    val bTask = Partition(candidate / 2, candidate, candidate)

    LOG.info("sockets to workers = " + sockets)

    // iterate through hosts,
    // hard code numbers since we're only using A/B
    (0 until sockets.length).foreach { k =>

      LOG.info("sending message to worker " + k)

      workers(0) ! aTask
      workers(1) ! bTask
    }

    //TODO: not receiving partial results correctly
    //Sum partial results from workers
    var sum = 0L //placeholder
    var count = 0
    sleep(1000)
    while (count < 2) {
      receive match {
        case task: Task if (task.kind == Task.REPLY) =>
          task.payload match {
            case result: Result =>
              sum += result.sum
              println("sum for " + candidate + " = " + sum)
              count += 1
          }

      }
      println("count = " + count)
    }
    //is the candidate perfect? return a Boolean
    sum == (2 * candidate)
  }

  def ask(method: Long => Boolean, number: Long): String = {
    val t0 = System.nanoTime
    val result = method(number)
    val t1 = System.nanoTime
    val answer = if (result) "YES" else "NO"
    "Is " + number + " perfect? " + answer + "! dt = " + (t1 - t0) / 1000000000.0 + "s"
  }
}
