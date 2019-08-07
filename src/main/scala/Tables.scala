import Msg.{table, table_added, table_updated}
import akka.actor.ActorRef

object Tables {
  var tables = List.empty[table]
  var subscribers = Set.empty[ActorRef]

  def add(id: Int, t: table) {
    tables = tables.take(id) ++ List(t) ++ tables.drop(id)
    subscribers.foreach(_ ! table_added(id, t))
  }

  def update(t: table): Boolean = {
    t.id match {
      case Some(id) if (tables.indices contains id) =>
        tables = tables.patch(id, Seq(table(None, t.name, t.participants)), 1)
        subscribers.foreach(_ ! table_updated(t))
        true
      case None => false
    }
  }

  def remove(id: Int): Boolean = {
    if (tables.indices contains id) {
      tables = tables.patch(id, Nil, 1)
      subscribers.foreach(_ ! Msg.table_removed(id))
      true
    } else false
  }
}
