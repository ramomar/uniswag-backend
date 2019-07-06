package assignments.todoist

import assignments.todoist.commands.{NopCommand, Command}
import assignments.todoist.api.Resources.Resource
import play.api.libs.json.{JsNull, Writes, Json}
import scalaj.http.{Http, HttpResponse}
import com.typesafe.config.Config

import scala.concurrent.{Future, ExecutionContext}

// It's not cool to wrap blocking calls inside futures but since this project is never going to have that much traffic
// this is ok.
trait TodoistClient {
  import TodoistClient._

  protected implicit def ec: ExecutionContext
  def params: Parameters

  def oauthUrl(state: String): String = s"${params.authEndpoint}?" +
    s"client_id=${params.clientId}&scope=${params.scope}&state=$state"

  def accessToken(code: String): Future[HttpResponse[String]] =
    Future {
      Http(params.accessTokenEndpoint)
        .param("client_id", params.clientId)
        .param("client_secret", params.clientSecret)
        .param("code", code)
        .param("redirect_uri", params.redirectUri)
        .postForm
        .asString
    }

  def submitCommands(token: String)(commands: Command*): Future[HttpResponse[String]] =
    Future {
      Http(params.resourcesEndpoint)
        .param("token", token)
        .param("commands", Json.toJson(commands).toString)
        .postForm
        .asString
    }

  def fetchResourceDetails(token: String)
                          (syncToken: Option[String],
                           resources: Resource*): Future[HttpResponse[String]] =
    Future {
      Http(params.resourcesEndpoint)
        .param("token", token)
        .param("sync_token", syncToken.getOrElse("*"))
        .param("resource_types", s"[${resources.map('"' + _.toString + '"').mkString(", ")}]")
        .postForm
        .asString
    }
}

object TodoistClient {
  case class Parameters(clientId: String,
                        clientSecret: String,
                        resourcesEndpoint: String,
                        authEndpoint: String,
                        accessTokenEndpoint: String,
                        redirectUri: String,
                        scope: String)

  protected implicit val commandWrites: Writes[Command] = new Writes[Command] {
    def writes(c: Command) =
      c match {
        case NopCommand => JsNull
        case _          => Json.obj(
          "type"    -> c.typ.toString,
          "temp_id" -> c.tempId,
          "uuid"    -> c.uuid,
          "args"    -> c.args
        )
      }
  }
}

object Client {
  import TodoistClient.Parameters

  def apply(conf: Config)(implicit exec: ExecutionContext): TodoistClient =
    new TodoistClient {

      protected implicit def ec: ExecutionContext = exec

      def params: Parameters =
        Parameters(
          clientId            = conf.getString("todoist.credentials.clientId"),
          clientSecret        = conf.getString("todoist.credentials.clientSecret"),
          resourcesEndpoint   = conf.getString("todoist.endpoints.resources"),
          authEndpoint        = conf.getString("todoist.endpoints.auth"),
          accessTokenEndpoint = conf.getString("todoist.endpoints.accessToken"),
          redirectUri         = conf.getString("todoist.endpoints.redirect"),
          scope               = conf.getString("todoist.scope")
        )
    }
}
