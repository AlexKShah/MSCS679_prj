import org.apache.log4j.Logger
import parascale.actor.last.{Dispatcher, Task}
import parascale.util.getPropertyOrElse
import parabond.cluster.{report, check, checkReset, Partition, Analysis}
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

      //a. Output the report header.
      println("header TODO")
      //b. Get the next n, that is, number of portfolios to price.
      val nportf = getPropertyOrElse("nportf", "1000")

      val ladder = List(1000, 2000, 4000, 8000, 16000, 32000, 64000, 100000)

      //j. Repeat step b for each rung
      ladder.foreach { rung =>
        //c. Reset the check portfolio prices.
        val checkIds = checkReset(rung)

        val t0 = System.nanoTime()

        //d. Create two workers by passing the dispatcher constructor two sockets.
        //e. Create two partitions:
        //  A) Partition(seed=0, n=n/2, begin=0) and
        //  B) Partition(seed=0, n=n/2, begin=n/2)
        //f. Send worker(0) the first partition and worker(1) the second partition.
        workers(0) ! Partition(0, rung/2, 0)
        workers(1) ! Partition(0, rung/2, rung/2)

        val replies = for (k <- 0 to  workers.length) yield receive

        val dtsList = for (_ <- replies) yield receive match {
          case task: Task if (task.kind == Task.REPLY) =>
            task.payload match {
              case result: Analysis =>
                report(LOG, result, checkIds)
                (result.t0 - result.t1)
            }
        }

        // sum up partial T1's
        val T1 = replies.foldLeft(0L) { (sum, reply) =>
          reply match {
            case task: Task if (task.kind == Task.REPLY) =>
              val result = task.payload.asInstanceOf[Result]
              sum + (result.t1-result.t0)
          }
        } seconds

        val t1 = System.nanoTime()

        //calculate TN
        val TN = (t1 - t0) seconds

        //g. Wait for results.
        //h. Test the check portfolios.
        //TODO report the missed.length
        val missed = check(checkIds)

        //i. Output the performance statistics.
        //report(LOG, analysis, checkIds)
      }

    }
  }

}
