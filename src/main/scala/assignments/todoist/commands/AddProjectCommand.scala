package assignments.todoist.commands

import assignments.todoist.api.CommandTypes.AddProject

case class AddProjectCommand(name: String, color: Option[Int]) extends Command {
  val typ = AddProject
  val args = Map("name" -> name) ++
    color.map("color" -> _.toString)
}
