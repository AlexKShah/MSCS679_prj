import org.apache.log4j.Logger
import parascale.actor.last.{Dispatcher, Task}
import parascale.util._
import parabond.cluster.Partition

case class Result(t0: Int, t1: Int) extends Serializable

object ParaDispatcher extends App {
  class ParaDispatcher(sockets: List[String]) extends Dispatcher(sockets) {
    import ParaDispatcher._

    def act: Unit = {

    }
  }
}
