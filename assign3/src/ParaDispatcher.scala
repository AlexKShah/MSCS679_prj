import org.apache.log4j.Logger
import parascale.actor.last.{Dispatcher, Task}
import parascale.util.getPropertyOrElse
import parabond.cluster._

case class Result(t0: Int, t1: Int) extends Serializable
//not in parabond.cluster?
//case class Partition(seed: Long, n: Long, begin: Long) extends Serializable

object ParaDispatcher extends App {

  class ParaDispatcher(sockets: List[String]) extends Dispatcher(sockets) {

    def act: Unit = {
      val LOG = Logger.getLogger(getClass)
      LOG.info("started")
      /*
      a. Output the report header.
      b. Get the next n, that is, number of portfolios to price.
      c. Reset the check portfolio prices.
      d. Create two workers by passing the dispatcher constructor two sockets.
      e. Create two partitions:
        A) Partition(seed=0, n=n/2, begin=0) and
        B) Partition(seed=0, n=n/2, begin=n/2)
      f. Send worker(0) the first partition and worker(1) the second partition.
      g. Wait for results.
      h. Test the check portfolios.
      i. Output the performance statistics.
      j. Repeat step b = Get the next n, that is, number of portfolios to price.
       */

      //a
      println("header TODO")
      //b
      val nportf = getPropertyOrElse("nportf", "1000")

      val ramp = List(1000, 2000, 4000, 8000, 16000, 32000, 64000, 100000)

      ramp.foreach { n =>
        //c
        val checkIds = checkReset(n)

        val t0 = System.nanoTime()
        //d, e, f
        workers(0) ! Partition(0, n/2, 0)
        workers(1) ! Partition(0, n/2, n/2)

        val replies = for (k <- 0 to  workers.length) yield receive

        val t1 = System.nanoTime()

        val dtsList = for (_ <- replies) yield receive match {
          case task: Task if (task.kind == Task.REPLY) =>
            task.payload match {
              case result: Result =>
                result.t1-result.t0
            }
        }
        val T1 = dtsList.sum seconds
        val TN = t1 - t0 seconds

        //g, h
        check(checkIds)

        //i
        report(LOG, analysis, checkIds)
      }

    }
  }

}
