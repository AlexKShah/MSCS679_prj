//Alex Shah
//MSCS 679 - Assignment 2

/*
TODO
Formal report 25/30
o Format the double precision numbers to the appropriate number of decimal places;
  most have too many decimal places. -2
o The number of decimal places is not constant but varies. Did you see my report? -2
o Right-justify header T1. -1
o Right-justify header TN. -1
o Left-justify header candidate. -1
o Why the low efficiency, especially for the largest number? (Actually, I think I
  may know the answer.)

Diagnostic logs 0/10
o logb.out contains only the exception backtrace. -5
o loga.out is empty. -5

PerfectDispatcher 20/40
o Where's the code that makes the report?
o Where is the calculation of T1?
o Where is the calculation of TN?
o Where's the calculation of R?
o Where's the calculation of e?
o Does sleep(100) fix a race condition?

PerfectWorker 20/40
o In the comprehension calculate partial dt for each future to estimate T1.
  (I thought this was the answer to my question about the low efficiency but the code
   has no timing measurement.) -20

Good programming style 22/30
o PerfectDispatcher: count should be a val. -2
o PerfectDispatcher: sum should be a val. -2
o PerfectDispatcher: instead of val aTask = Partition(1, candidate / 2 - 1, candidate)
  try val aTask = Partition(1, candidate/2 - 1, candidate)

o In general, use more javadoc. -2
o In general, use more internal comments. -2
o Please submit the deliverables as separate files, see the specs. -2
 */

package assign2

import org.apache.log4j.Logger
import parascale.actor.last.{Dispatcher, Task}
import parascale.util._


case class Partition(start: Long, end: Long, candidate: Long)
  extends Serializable

case class Result(dt: Long, sum: Long) extends Serializable

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
    (0 until candidates.length).foreach { index =>
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

      workers(0) ! aTask
      workers(1) ! bTask

    //Sum partial results from workers
    var sum = 0L //placeholder
    var count = 0
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
