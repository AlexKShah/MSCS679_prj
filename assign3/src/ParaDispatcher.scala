import parascale.actor.last.Dispatcher
import parascale.util.getPropertyOrElse
import parabond.cluster._

case class Result(t0: Int, t1: Int) extends Serializable
//not in parabond.cluster?
case class Partition(n: Long, begin: Long) extends Serializable

object ParaDispatcher extends App {

  class ParaDispatcher(sockets: List[String]) extends Dispatcher(sockets) {

    def act: Unit = {

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
        workers(0) ! Partition(n/2, 0)
        workers(1) ! Partition(n/2, n/2)
      }

    }
  }

}
