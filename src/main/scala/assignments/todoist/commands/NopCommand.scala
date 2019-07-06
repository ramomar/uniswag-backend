package assignments.todoist.commands

import assignments.todoist.api.CommandTypes.Nop

object NopCommand extends Command {
  val typ  = Nop
  val args = Map.empty[String, String]
}
