package folders.drive.commands

import play.api.libs.json.{JsValue, Json}

case class CreateFolderCommand(name: String, parents: Seq[String] = Seq.empty) extends Command {
  val path: String     = "/drive/v3/files"
  val payload: JsValue =
    Json.obj(
      "mimeType" -> "application/vnd.google-apps.folder",
      "name"     -> name,
      "parents"  -> parents
    )
}
