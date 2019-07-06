package assignments.todoist.commands

import org.joda.time.DateTime

import assignments.todoist.api.CommandTypes.AddItem

case class AddItemCommand(content: String,
                          dueDate: Option[DateTime] = None,
                          projectId: Option[String] = None) extends Command {
  val typ = AddItem
  val args = Map("content" -> content) ++
    projectId.map("project_id" -> _)   ++
    dueDate.fold(Seq.empty[(String, String)]) { date =>
      Seq(
        "date_lang"    -> "en",
        "due_date_utc" -> date.toString("YYYY-MM-dd'T'HH:mmZ"),
        "date_string"  -> date.toString("MM/dd/YYYY @ HH:mm")
      )
    }
}
