package folders.drive.commands

import play.api.libs.json.JsValue

trait Command {
  val path: String
  val payload: JsValue
}
