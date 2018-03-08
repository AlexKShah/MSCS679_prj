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

  val candidates: List[Long] =
    List(
      6,
      28,
      496,
      8128,
      33550336,
      33550336 + 1,
      8589869056L + 1,
      8589869056L,
      137438691328L,
      2305843008139952128L)

  /**
    * Handles actor startup after construction.
    */
  def act: Unit = {
    (0 until candidates.length).foreach { index =>
      val candidate = candidates(index)
      val t0 = System.nanoTime()

      val aTask = Partition(1, candidate / 2 - 1, candidate)
      val bTask = Partition(candidate / 2, candidate, candidate)

      LOG.info("sockets to workers = " + sockets)

      (0 until sockets.length).foreach { k =>
        LOG.info("sending message to worker " + k)
        workers(0) ! aTask
        workers(1) ! bTask
      }

      while (true) {
        var sum = 0L
        receive match {
          case task: Task if (task.kind == Task.REPLY) =>
            LOG.info("received reply " + task)
            val result = task.payload.asInstanceOf[Result]
            sum += result.sum
        }

        val t1 = System.nanoTime()
        val dt = t1 - t0 / 1000000000.0

        val answer = if (sum == (candidate * 2)) "YES" else "NO"
        println("Is " + candidate + " perfect? " + answer + ". TN= " + dt)
      }
    }
  }
}
