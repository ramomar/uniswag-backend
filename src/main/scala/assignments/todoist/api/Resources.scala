package assignments.todoist.api

object Resources {
  trait Resource {
    protected val resource: String
    override def toString: String = resource
  }

  val Projects = new Resource { protected val resource = "projects"}
}
