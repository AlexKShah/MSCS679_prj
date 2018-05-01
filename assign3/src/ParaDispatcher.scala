import org.apache.log4j.{Level, Logger}
import parascale.actor.last.{Dispatcher, Task}
import parascale.util.getPropertyOrElse
import parabond.cluster.{Analysis, Partition, check, checkReset}
import parabond.cluster._

object ParaDispatcher extends App {
  val LOG = Logger.getLogger(getClass)
  Logger.getRootLogger.setLevel(Level.OFF)
  //-Dnhosts=2 -Dsocket=IP:socket
  val socket2 = getPropertyOrElse("socket", "localhost:9000")
  new ParaDispatcher(List("localhost:8000", socket2))
}

class ParaDispatcher(sockets: List[String]) extends Dispatcher(sockets) {

  def act: Unit = {
    import ParaDispatcher._

    //a. Output the report header.
    println("ParaBond Portfolio Analysis")
    println("Alex Shah")
    println("5/1/18")
    val numCores = Runtime.getRuntime().availableProcessors() //8
    println("Cores: " + numCores)
    println("Number of workers: " + workers.length)
    println;
    println("Memory Bound Node Report:");
    println;

    //Header
    println(String.format("%-9s", "n") +
      String.format("%-9s", "Missed") +
      String.format("%-9s", "T1") +
      String.format("%-9s", "TN") +
      String.format("%-6s", "R") +
      String.format("%-6s", "e")
    )

    //b. Get the next n, that is, number of portfolios to price.
    val n = getPropertyOrElse("n", 100)

    val ladder = List(1000, 2000, 4000, 8000, 16000, 32000, 64000, 100000)

    //j. Repeat step b for each rung
    ladder.foreach { rung =>
      //println("rung = " + rung)

      //c. Reset the check portfolio prices.
      val checkIds = checkReset(rung)

      //start timing tn
      val t0 = System.nanoTime()

      //d. Create two workers by passing the dispatcher constructor two sockets.
      //e. Create two partitions:
      //  A) Partition(seed=0, n=n/2, begin=0) and
      //  B) Partition(seed=0, n=n/2, begin=n/2)
      //f. Send worker(0) the first partition and worker(1) the second partition.
      import parabond.cluster.Partition
      workers(0) ! Partition(rung / 2, 0)
      workers(1) ! Partition(rung / 2, rung / 2)

      //receive replies from the 2 workers
      val replies = for (k <- 0 until workers.length) yield receive

      // sum up partial T1's
      val T1 = replies.foldLeft(0L) { (sum, reply) =>
        reply match {
          case task: Task if (task.kind == Task.REPLY) =>
            import parascale.parabond.util.Result
            val result = task.payload.asInstanceOf[Result]

            sum + (result.t1 - result.t0)
        }
      } seconds

      //i. Output the performance statistics.
      val t1 = System.nanoTime()
      val TN = (t1 - t0) seconds
      val speedup = T1 / TN
      val e = speedup / numCores
      val missed = check(checkIds).length

      println("%-9s %-7s %-7.2f %-7.2f %2.4f %2.4f ".format(rung, missed, T1, TN, speedup, e))
    }
  }
}