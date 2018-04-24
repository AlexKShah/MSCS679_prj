import parabond.cluster
import org.apache.log4j.Logger
import parascale.actor.last.{Task, Worker}
import parascale.util._

object ParaWorker extends App {

}

class ParaWorker(port: Int) extends Worker(port) {
  import ParaWorker._
  override def act: Unit = {

  }
}
