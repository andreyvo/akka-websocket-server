import io.circe.Printer
import io.circe.generic.extras.Configuration
import io.circe.syntax._
import io.circe.generic.extras.auto._

object Msg {
  sealed trait Base

  implicit val genDevConfig: Configuration = Configuration.default.withDiscriminator("$type")
  def decode(str : String) : scala.Either[io.circe.Error, Base] = io.circe.parser.decode[Base](str)
  def toJson(msg : Base): String = msg.asJson.pretty(Printer.spaces2.copy(dropNullValues = true))

  case class login(username: String, password: String) extends Base
  sealed class login_failed extends Base
  val login_failed: Base = new login_failed()
  case class login_successful(user_type : String) extends Base
  case class ping(seq : Int) extends Base
  case class pong(seq: Int) extends Base

  case class fail(message: String) extends Base

  case object not_authorized extends Base

  sealed trait Authorized extends Base
  object subscribe_tables extends Authorized
  object unsubscribe_tables extends Authorized

  case class table_list(tables : List[table]) extends Authorized

  case class add_table(after_id: Int, table: table) extends Authorized
  case class update_table(table: table) extends Authorized
  case class remove_table(id: Int) extends Authorized

  case class add_failed(id: Int) extends Base
  case class removal_failed(id : Int) extends Base
  case class update_failed(id : Int) extends Base

  case class table_added(after_id : Int, table : table) extends Base
  case class table_removed(id : Int) extends Base
  case class table_updated(table : table) extends Base

  case class table(id: Option[Int], name: String, participants: Int) extends Base
}