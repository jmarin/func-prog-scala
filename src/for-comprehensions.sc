case class User(id: String, name: String, roleId: Option[String] = None)
case class Role(id: String, isAdmin: Boolean)
case class Authorization(
    roleId: String,
    capabilities: List[String] = List.empty
)

object UserRepository:
  private val users =
    List(
      User("1", "John", Some("1")),
      User("2", "Alice", Some("2")),
      User("3", "Bob", Some("2"))
    )
  def retrieve(id: String): Option[User] = users.filter(_.id == id).headOption

object RoleRepository:
  private val roles = List(Role("1", true), Role("2", false))
  def retrieve(id: String): Option[Role] = roles.filter(_.id == id).headOption

def getUser(userId: String): Option[User] = UserRepository.retrieve(userId)
def getRole(roleId: String): Option[Role] = RoleRepository.retrieve(roleId)
def getAuthorization(role: Role): Option[Authorization] =
  if role.isAdmin then Some(Authorization(role.id, List("all")))
  else None

def authorize(userId: String): Option[Authorization] =
  getUser(userId)
    .flatMap(u => getRole(u.roleId.getOrElse("")))
    .flatMap(r => getAuthorization(r))

def authorize2(userId: String): Option[Authorization] =
  for
    user <- getUser(userId)
    role <- getRole(user.roleId.getOrElse(""))
    auth <- getAuthorization(role)
  yield auth

val bob = User("1", "Bob")
authorize(bob.id).map(println)
