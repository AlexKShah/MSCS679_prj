import org.apache.log4j.Logger
import parascale.actor.last.Worker
import parascale.util._
import parabond.cluster._

object ParaWorker extends App {

}

class ParaWorker(port: Int) extends Worker(port) {
  import ParaWorker._
  override def act: Unit = {

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
