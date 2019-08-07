object Users {
  sealed trait Role {
    def isUser: Boolean = isInstanceOf[User]
    def isAdmin: Boolean = isInstanceOf[Admin]
  }
  sealed trait User extends Role
  sealed trait Admin extends User
  case object unauthorized extends Role
  case object user extends User
  case object admin extends Admin

  def role(key: String, value: String): Option[Role] = roles.get(key, value)

  val roles: Map[(String, String), Role] = Map(
    ("admin", "admin") -> admin,
    ("user", "user") -> user)
}