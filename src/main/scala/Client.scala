import Msg._
import akka.actor.{Actor, ActorRef, Terminated}

class Client extends Actor {
  var user: Users.Role = Users.unauthorized
  var out: ActorRef = _
  val receive: Receive = {
    case a: ActorRef => out = a; context.watch(out)
    case Terminated(a) => context.stop(self)
    case None => context.stop(self)
    case msg: Base =>
      println(s"$msg")
      msg match {
        case login: login => out ! (Users.role(login.username, login.password) match {
          case Some(role) =>
            user = role
            login_successful(role.toString)
          case None =>
            user = Users.unauthorized
            login_failed
        })
        case ping: ping => out ! pong(ping.seq)
        case msg: Authorized => msg match {
          case Msg.subscribe_tables if (user.isUser) =>
            Tables.subscribers += out
            out ! table_list(Tables.tables.zipWithIndex.map { case (t, id) => table(Option(id), t.name, t.participants) })
          case Msg.unsubscribe_tables if (user.isUser) => Tables.subscribers -= out
          case cmd: add_table if (user.isAdmin) => Tables.add(cmd.after_id, cmd.table)
          case cmd: update_table if (user.isAdmin) => if (!Tables.update(cmd.table)) out ! update_failed
          case cmd: remove_table if (user.isAdmin) => if (!Tables.remove(cmd.id)) out ! removal_failed
          case _ => out ! not_authorized
        }
        case msg => out ! msg
      }
  }

  override def postStop() {
    Tables.subscribers -= out
    context.stop(out)
  }
}