package assignments.todoist.commands

import java.util.UUID

import assignments.todoist.api.CommandTypes.CommandType

trait Command {
  val typ: CommandType
  val uuid:   UUID = UUID.randomUUID
  val tempId: UUID = UUID.randomUUID
  val args: Map[String, String]
}