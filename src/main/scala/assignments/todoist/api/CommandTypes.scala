package assignments.todoist.api

private[todoist] object CommandTypes {
  trait CommandType {
    protected val typ: String
    override def toString: String = typ
  }

  val AddProject = new CommandType { protected val typ = "project_add" }
  val AddItem    = new CommandType { protected val typ = "item_add" }
  val Nop        = new CommandType { protected val typ = "nop" }
}
